package top.mddata.console.service.coderule.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import top.mddata.console.entity.coderule.CodeSn;
import top.mddata.console.mapper.coderule.CodeSnMapper;
import com.mybatisflex.core.query.QueryWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 流水号分配器：Redis 原子递增（Lua 一次性完成 初始化+递增+TTL），DB 水位表冷备恢复。
 * 无锁、集群安全；恢复时按配置的跳跃量前进（recoverySkip=0 精确恢复，>0 跳号防重）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SnAllocator {

    /** 水位回写间隔：Redis 值每跨 1000 回写一次 DB */
    private static final long WATER_STEP = 1000L;

    /** 冷恢复跳跃量默认值（水位回写是跨段滞后的，Redis 数据丢失时实际值可能领先水位最多 999，默认跳 1000 保证绝不重复） */
    public static final int DEFAULT_RECOVERY_SKIP = 1000;

    /**
     * Lua：key 不存在时用 ARGV[1] 初始化（并设置 TTL，ARGV[3]<=0 不过期），然后 INCRBY ARGV[2]。
     * 初始化值由调用方算好：新周期=initValue-step；冷恢复=DB水位+recoverySkip。
     */
    private static final RedisScript<Long> INIT_AND_INCR = RedisScript.of("""
            if redis.call('exists', KEYS[1]) == 0 then
                redis.call('set', KEYS[1], ARGV[1])
                local ttl = tonumber(ARGV[3])
                if ttl > 0 then redis.call('pexpire', KEYS[1], ttl) end
            end
            return redis.call('incrby', KEYS[1], ARGV[2])
            """, Long.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final CodeSnMapper codeSnMapper;

    /**
     * 原子分配 count 个连续流水号
     *
     * @param ruleItemId 明细ID
     * @param periodKey 周期标签
     * @param initValue 初始值
     * @param step 递增值
     * @param count 分配数量
     * @param ttlMillis Redis key 的 TTL（<=0 不过期）
     * @param recoverySkip 冷恢复跳跃量（Redis key 丢失重建时在水位基础上跳跃前进；0=精确恢复不跳号，可能重号）
     * @return 连续递增值列表（首个为本次分配的起始值）
     */
    public List<Long> allocate(Long ruleItemId, String periodKey, long initValue, long step, int count, long ttlMillis,
            long recoverySkip) {
        String key = "mdc:sn:" + ruleItemId + ":" + periodKey;
        long initBase = resolveInitBase(ruleItemId, periodKey, initValue, step, recoverySkip);
        Long last = stringRedisTemplate.execute(INIT_AND_INCR, List.of(key),
                String.valueOf(initBase), String.valueOf(step * (long) count), String.valueOf(ttlMillis));
        if (last == null) {
            throw new IllegalStateException("流水号分配失败: " + key);
        }
        long before = last - step * (long) count;
        if (recoverySkip == 0) {
            // 精确模式：每次分配同步回写，水位永远等于 Redis 实际值，冷恢复精确衔接不跳号；
            // 代价是每次分配一次 DB 单行 upsert（内部单号生成频率下可接受）
            upsertWaterMark(ruleItemId, periodKey, last);
        } else {
            // 跳号模式：跨段回写（滞后快照），Redis 丢失后跳跃 recoverySkip 前进
            writeBackWaterMark(ruleItemId, periodKey, before, last);
        }
        List<Long> result = new ArrayList<>(count);
        long first = last - step * (long) count + step;
        for (int i = 0; i < count; i++) {
            result.add(first + step * i);
        }
        return result;
    }

    /**
     * 将 Redis 当前值强制 flush 到 DB 水位表（key 不存在则跳过）。
     * 用于删 key 前的兜底与水位校准：Redis 删除不随事务回滚，flush 后即使事务回滚，
     * 冷恢复也会从水位+跳跃量起步，保证绝不重号。
     *
     * @param ruleItemId 明细ID
     * @param periodKey 周期标签
     * @return flush 后的水位值；Redis 无对应 key 时返回 null
     */
    public Long flushWaterMark(Long ruleItemId, String periodKey) {
        String key = "mdc:sn:" + ruleItemId + ":" + periodKey;
        String current = stringRedisTemplate.opsForValue().get(key);
        if (current == null) {
            return null;
        }
        long value = Long.parseLong(current);
        upsertWaterMark(ruleItemId, periodKey, value);
        return value;
    }

    /**
     * 计算 Redis 初始化基数（仅在 key 不存在时生效）：
     * DB 有水位 → 水位+recoverySkip（跳跃防重，0=精确恢复）；无水位 → initValue-step（首个值正好为 initValue）
     */
    private long resolveInitBase(Long ruleItemId, String periodKey, long initValue, long step, long recoverySkip) {
        CodeSn sn = codeSnMapper.selectOneByQuery(QueryWrapper.create()
                .eq(CodeSn::getRuleItemId, ruleItemId).eq(CodeSn::getPeriodKey, periodKey));
        return sn != null ? sn.getCurrentValue() + recoverySkip : initValue - step;
    }

    /**
     * 水位维护：
     * 1. 水位记录不存在（首次分配或记录被删）→ 立即落库，保证冷恢复基数始终存在；
     * 2. 记录存在 → 仅在跨越 1000 的整数倍边界时更新（滞后快照，减少 DB 写压力）。
     * 跨段判断不能只看 after 是否恰好为 1000 倍数：大批量 INCRBY 可能越过倍数却不落在倍数上，
     * 若宕机时 DB 无水位，冷恢复会重新起号，破坏“绝不重复”不变量。
     *
     * @param before 本次分配前的 Redis 值
     * @param after  本次分配后的 Redis 值（最新已分配值，作为水位写入）
     */
    private void writeBackWaterMark(Long ruleItemId, String periodKey, long before, long after) {
        CodeSn sn = selectWaterMark(ruleItemId, periodKey);
        if (sn != null && after / WATER_STEP <= before / WATER_STEP) {
            // 已有水位记录且未跨段：滞后快照，不写
            return;
        }
        // 无记录 → 立即落库重建；跨段 → 更新为最新已分配值
        upsertWaterMark(ruleItemId, periodKey, after);
    }

    /**
     * 水位 upsert。两节点同时冷恢复/同时跨界时 select 都为空，insert 会撞 uk_item_period，
     * 捕获 DuplicateKeyException 后重查并走 update；其他异常照常抛出，不吞。
     */
    private void upsertWaterMark(Long ruleItemId, String periodKey, long current) {
        CodeSn sn = selectWaterMark(ruleItemId, periodKey);
        if (sn != null) {
            updateWaterMark(sn.getId(), current);
            return;
        }
        // 继承链下 chain setter 返回父类类型，不能直接赋值给 CodeSn，故逐条调用
        CodeSn insert = new CodeSn();
        insert.setRuleItemId(ruleItemId);
        insert.setPeriodKey(periodKey);
        insert.setCurrentValue(current);
        try {
            codeSnMapper.insert(insert);
        } catch (DuplicateKeyException e) {
            log.warn("水位记录并发插入冲突 uk_item_period，重查后更新: ruleItemId={}, periodKey={}",
                    ruleItemId, periodKey);
            CodeSn existing = selectWaterMark(ruleItemId, periodKey);
            if (existing == null) {
                throw new IllegalStateException(
                        "水位记录插入冲突后重查仍为空: ruleItemId=" + ruleItemId + ", periodKey=" + periodKey, e);
            }
            updateWaterMark(existing.getId(), current);
        }
    }

    private CodeSn selectWaterMark(Long ruleItemId, String periodKey) {
        return codeSnMapper.selectOneByQuery(QueryWrapper.create()
                .eq(CodeSn::getRuleItemId, ruleItemId).eq(CodeSn::getPeriodKey, periodKey));
    }

    private void updateWaterMark(Long id, long current) {
        CodeSn update = new CodeSn();
        update.setId(id);
        update.setCurrentValue(current);
        codeSnMapper.update(update);
    }
}
