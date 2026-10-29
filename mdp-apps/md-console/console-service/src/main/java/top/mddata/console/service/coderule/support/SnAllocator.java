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
 * 无锁、集群安全；恢复时水位跳跃前进（允许跳号，绝不重复）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SnAllocator {

    /** 水位回写间隔：Redis 值每跨 1000 回写一次 DB */
    private static final long WATER_STEP = 1000L;

    /**
     * Lua：key 不存在时用 ARGV[1] 初始化（并设置 TTL，ARGV[3]<=0 不过期），然后 INCRBY ARGV[2]。
     * 初始化值由调用方算好：新周期=initValue-step；冷恢复=DB水位+1000。
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
     * @return 连续递增值列表（首个为本次分配的起始值）
     */
    public List<Long> allocate(Long ruleItemId, String periodKey, long initValue, long step, int count, long ttlMillis) {
        String key = "mdc:sn:" + ruleItemId + ":" + periodKey;
        long initBase = resolveInitBase(ruleItemId, periodKey, initValue, step);
        Long last = stringRedisTemplate.execute(INIT_AND_INCR, List.of(key),
                String.valueOf(initBase), String.valueOf(step * (long) count), String.valueOf(ttlMillis));
        if (last == null) {
            throw new IllegalStateException("流水号分配失败: " + key);
        }
        long before = last - step * (long) count;
        writeBackWaterMark(ruleItemId, periodKey, before, last);
        List<Long> result = new ArrayList<>(count);
        long first = last - step * (long) count + step;
        for (int i = 0; i < count; i++) {
            result.add(first + step * i);
        }
        return result;
    }

    /**
     * 将 Redis 当前值强制 flush 到 DB 水位表（key 不存在则跳过）。
     * 用于删 key 前的兜底：Redis 删除不随事务回滚，flush 后即使事务回滚，
     * 冷恢复也会从水位+{@value #WATER_STEP} 起步，保证绝不重号。
     *
     * @param ruleItemId 明细ID
     * @param periodKey 周期标签
     */
    public void flushWaterMark(Long ruleItemId, String periodKey) {
        String key = "mdc:sn:" + ruleItemId + ":" + periodKey;
        String current = stringRedisTemplate.opsForValue().get(key);
        if (current == null) {
            return;
        }
        upsertWaterMark(ruleItemId, periodKey, Long.parseLong(current));
    }

    /**
     * 计算 Redis 初始化基数（仅在 key 不存在时生效）：
     * DB 有水位 → 水位+1000（冷恢复跳跃，绝不重复）；无水位 → initValue-step（首个值正好为 initValue）
     */
    private long resolveInitBase(Long ruleItemId, String periodKey, long initValue, long step) {
        CodeSn sn = codeSnMapper.selectOneByQuery(QueryWrapper.create()
                .eq(CodeSn::getRuleItemId, ruleItemId).eq(CodeSn::getPeriodKey, periodKey));
        return sn != null ? sn.getCurrentValue() + WATER_STEP : initValue - step;
    }

    /**
     * 水位回写：本次分配跨越 1000 的整数倍边界（before/1000 与 after/1000 不同段）即回写。
     * 不能只判断 after 是否恰好为 1000 倍数：大批量 INCRBY 可能越过倍数却不落在倍数上，
     * 若宕机时 DB 无水位，冷恢复会重新起号，破坏“绝不重复”不变量。
     *
     * @param before 本次分配前的 Redis 值
     * @param after  本次分配后的 Redis 值（最新已分配值，作为水位写入）
     */
    private void writeBackWaterMark(Long ruleItemId, String periodKey, long before, long after) {
        if (after / WATER_STEP <= before / WATER_STEP) {
            return;
        }
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
