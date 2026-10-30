package top.mddata.coderule;

import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import top.mddata.console.entity.coderule.CodeSn;
import top.mddata.console.mapper.coderule.CodeSnMapper;
import top.mddata.console.service.coderule.support.SnAllocator;
import top.mddata.TestBootServerApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 流水号分配器并发与冷恢复测试（连开发环境 Redis/MySQL）。
 * 测试明细ID 一律使用负数，@AfterEach 强制清理 Redis key 与 mdc_code_sn 水位记录。
 */
@SpringBootTest(classes = TestBootServerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "mdp.log.ipv4.enabled=false",
        "mdp.log.ipv6.enabled=false"
})
class SnAllocatorConcurrencyTest {

    /** 并发测试用明细ID（负数，避免与真实数据冲突） */
    private static final long CONCURRENCY_ITEM_ID = -9901L;
    /** 冷恢复测试用明细ID */
    private static final long RECOVERY_ITEM_ID = -9902L;
    /** 跨边界回写测试用明细ID */
    private static final long CROSS_BOUNDARY_ITEM_ID = -9903L;
    /** 水位 upsert 竞态测试用明细ID */
    private static final long UPSERT_RACE_ITEM_ID = -9904L;
    private static final long SKIP0_ITEM_ID = -9905L;
    private static final long SKIP50_ITEM_ID = -9906L;
    /** 水位记录重建测试用明细ID */
    private static final long REBUILD_ITEM_ID = -9907L;
    /** 测试 Redis key 前缀匹配串 */
    private static final String TEST_KEY_PATTERN = "mdc:sn:-99*";

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SnAllocator snAllocator;
    @Resource
    private CodeSnMapper codeSnMapper;

    /**
     * 确保水位表存在（测试库可能未执行增量脚本）。凭证由 Spring 数据源托管，测试代码不接触密码。
     */
    @org.junit.jupiter.api.BeforeAll
    static void initSchema(@org.springframework.beans.factory.annotation.Autowired JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `mdc_code_sn` (
                  `id` bigint NOT NULL COMMENT '主键',
                  `rule_item_id` bigint NOT NULL COMMENT '规则明细ID',
                  `period_key` varchar(32) NOT NULL COMMENT '周期标签（归零维度）',
                  `current_value` bigint NOT NULL DEFAULT '0' COMMENT '当前水位（Redis 每跨 1000 步回写一次）',
                  `created_at` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                  `created_by` bigint DEFAULT '0' COMMENT '创建人ID',
                  `updated_at` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后修改时间',
                  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人ID',
                  PRIMARY KEY (`id`) USING BTREE,
                  UNIQUE KEY `uk_item_period` (`rule_item_id`, `period_key`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='编码规则流水号水位'
                """);
    }

    @BeforeEach
    void cleanBefore() {
        // 兜底：防止上次失败运行残留影响本次断言
        cleanTestData();
    }

    @AfterEach
    void cleanAfter() {
        cleanTestData();
        // 清理结果验证：Redis 无残留 key
        assertEquals(Collections.emptyList(), scanTestKeys(), "测试结束后 Redis 仍有残留 key");
        // DB 无残留水位记录
        assertEquals(0L, countWaterMarks(), "测试结束后 mdc_code_sn 仍有残留水位记录");
    }

    @Test
    void 并发分配_100线程各100个无重复且连续() throws InterruptedException {
        int threads = 100;
        int perThread = 100;
        String periodKey = "TEST" + System.currentTimeMillis();
        Set<Long> values = ConcurrentHashMap.newKeySet();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            for (int t = 0; t < threads; t++) {
                pool.submit(() -> {
                    try {
                        start.await();
                        for (int i = 0; i < perThread; i++) {
                            values.addAll(snAllocator.allocate(CONCURRENCY_ITEM_ID, periodKey,
                                    1L, 1L, 1, -1L, 1000L));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("并发分配被中断", e);
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            assertTrue(done.await(5, TimeUnit.MINUTES), "并发分配超时");
        } finally {
            pool.shutdownNow();
        }

        int total = threads * perThread;
        assertEquals(total, values.size(), "存在重复流水号");
        // 全新周期首值正好为初始值 1，并发下区间不重不漏，最大值即总数
        assertEquals(1L, Collections.min(values), "首个流水号不是初始值");
        assertEquals(total, Collections.max(values), "流水号区间不连续");
    }

    @Test
    void 批量分配_一次返回连续区间() {
        String periodKey = "BATCH" + System.currentTimeMillis();
        List<Long> batch = snAllocator.allocate(CONCURRENCY_ITEM_ID, periodKey, 1L, 1L, 5, -1L, 1000L);
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L), batch);
    }

    @Test
    void 冷恢复_删key后水位跳跃前进绝不重复() {
        String periodKey = "RECOVER" + System.currentTimeMillis();
        // 分配 1000 个：触发水位回写（current=1000 为 1000 的整数倍）
        List<Long> firstBatch = snAllocator.allocate(RECOVERY_ITEM_ID, periodKey, 1L, 1L, 1000, -1L, 1000L);
        assertEquals(1000, firstBatch.size());
        assertEquals(1L, firstBatch.get(0));
        assertEquals(1000L, firstBatch.get(999));
        assertEquals(1000L, readWaterMark(RECOVERY_ITEM_ID, periodKey), "DB 水位未按 1000 步长回写");

        // 模拟 Redis 宕机丢 key
        stringRedisTemplate.delete("mdc:sn:" + RECOVERY_ITEM_ID + ":" + periodKey);

        // 冷恢复：基数 = 水位1000 + 跳跃1000 = 2000，首个新值 2001（跳号但绝不重复）
        List<Long> recovered = snAllocator.allocate(RECOVERY_ITEM_ID, periodKey, 1L, 1L, 3, -1L, 1000L);
        assertEquals(List.of(2001L, 2002L, 2003L), recovered);
        assertTrue(Collections.disjoint(firstBatch, recovered), "冷恢复后出现重复流水号");
        assertTrue(recovered.get(0) >= firstBatch.get(999) + 1, "冷恢复值未前进");
    }

    @Test
    void 冷恢复_跳跃量为0时精确恢复不跳号() {
        String periodKey = "SKIP0" + System.currentTimeMillis();
        // 分配 500 个触发水位回写（跨越 1000 以下不回写，手动 flush 验证精确语义）
        List<Long> firstBatch = snAllocator.allocate(SKIP0_ITEM_ID, periodKey, 1L, 1L, 500, -1L, 0L);
        assertEquals(500, firstBatch.size());
        // flush 保证水位与 Redis 实际值一致（500）
        snAllocator.flushWaterMark(SKIP0_ITEM_ID, periodKey);
        assertEquals(500L, readWaterMark(SKIP0_ITEM_ID, periodKey));

        // 模拟 Redis 丢失，跳跃量 0：精确恢复 500+1 起，不跳号
        stringRedisTemplate.delete("mdc:sn:" + SKIP0_ITEM_ID + ":" + periodKey);
        List<Long> recovered = snAllocator.allocate(SKIP0_ITEM_ID, periodKey, 1L, 1L, 3, -1L, 0L);
        assertEquals(List.of(501L, 502L, 503L), recovered);
    }

    @Test
    void 冷恢复_自定义跳跃量() {
        String periodKey = "SKIP50" + System.currentTimeMillis();
        List<Long> firstBatch = snAllocator.allocate(SKIP50_ITEM_ID, periodKey, 1L, 1L, 500, -1L, 0L);
        snAllocator.flushWaterMark(SKIP50_ITEM_ID, periodKey);
        assertEquals(500L, readWaterMark(SKIP50_ITEM_ID, periodKey));

        // 跳跃量 50：恢复基数 = 水位500 + 50 = 550，首个新值 551
        stringRedisTemplate.delete("mdc:sn:" + SKIP50_ITEM_ID + ":" + periodKey);
        List<Long> recovered = snAllocator.allocate(SKIP50_ITEM_ID, periodKey, 1L, 1L, 3, -1L, 50L);
        assertEquals(List.of(551L, 552L, 553L), recovered);
    }

    @Test
    void 水位记录被删后_下次分配立即重建() {
        String periodKey = "REBUILD" + System.currentTimeMillis();
        // 首次分配即落库（水位记录不存在时立即重建，不再等跨 1000 边界）
        List<Long> first = snAllocator.allocate(REBUILD_ITEM_ID, periodKey, 1L, 1L, 100, -1L, 0L);
        assertEquals(100, first.size());
        assertEquals(100L, readWaterMark(REBUILD_ITEM_ID, periodKey), "首次分配应立即落水位");

        // 模拟水位记录被删除
        codeSnMapper.deleteByQuery(QueryWrapper.create()
                .eq(CodeSn::getRuleItemId, REBUILD_ITEM_ID).eq(CodeSn::getPeriodKey, periodKey));
        assertEquals(-1L, readWaterMark(REBUILD_ITEM_ID, periodKey), "前置：水位记录已删除");

        // 下一次分配：Redis 正常续号（101），且水位记录立即重建为 101
        List<Long> second = snAllocator.allocate(REBUILD_ITEM_ID, periodKey, 1L, 1L, 1, -1L, 0L);
        assertEquals(List.of(101L), second);
        assertEquals(101L, readWaterMark(REBUILD_ITEM_ID, periodKey), "水位记录未立即重建");
    }

    @Test
    void 批量分配_跨越1000倍数边界即使不落在倍数上也回写水位() {
        String periodKey = "CROSS" + System.currentTimeMillis();
        // initBase=900，INCRBY 200 → after=1100：越过 1000 但不落在倍数上，旧逻辑不回写
        List<Long> batch = snAllocator.allocate(CROSS_BOUNDARY_ITEM_ID, periodKey, 901L, 1L, 200, -1L, 1000L);
        assertEquals(200, batch.size());
        assertEquals(901L, batch.get(0));
        assertEquals(1100L, batch.get(199));
        assertEquals(1100L, readWaterMark(CROSS_BOUNDARY_ITEM_ID, periodKey),
                "批量跨越 1000 边界后 DB 水位缺失或未写入最新已分配值");
    }

    @Test
    void 水位首次写入_并发跨界不抛唯一键冲突() throws InterruptedException {
        int threads = 16;
        String periodKey = "RACE" + System.currentTimeMillis();
        // 每批 INCRBY 2000：任意一批都跨越 2 个 1000 段，所有线程都会触发水位 upsert，
        // 同一起跑栅栏放大 select-then-insert 窗口，撞 uk_item_period 概率接近必然
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
        try {
            for (int t = 0; t < threads; t++) {
                futures.add(pool.submit(() -> {
                    try {
                        start.await();
                        snAllocator.allocate(UPSERT_RACE_ITEM_ID, periodKey, 1L, 1L, 2000, -1L, 1000L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("并发水位写入被中断", e);
                    } finally {
                        done.countDown();
                    }
                }));
            }
            start.countDown();
            assertTrue(done.await(5, TimeUnit.MINUTES), "并发水位写入超时");
        } finally {
            pool.shutdownNow();
        }

        for (java.util.concurrent.Future<?> future : futures) {
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    (org.junit.jupiter.api.function.Executable) future::get,
                    "并发首次水位写入抛出异常（可能为 uk_item_period 唯一键冲突未捕获）");
        }
        assertTrue(readWaterMark(UPSERT_RACE_ITEM_ID, periodKey) > 0, "水位记录最终未落库");
    }

    /**
     * 查询 DB 当前水位；无记录时返回 -1 以便与水位 0 区分
     */
    private long readWaterMark(long ruleItemId, String periodKey) {
        CodeSn sn = codeSnMapper.selectOneByQuery(QueryWrapper.create()
                .eq(CodeSn::getRuleItemId, ruleItemId).eq(CodeSn::getPeriodKey, periodKey));
        return sn == null ? -1L : sn.getCurrentValue();
    }

    /**
     * 统计测试残留水位记录数
     */
    private long countWaterMarks() {
        return codeSnMapper.selectCountByQuery(QueryWrapper.create()
                .in(CodeSn::getRuleItemId, List.of(CONCURRENCY_ITEM_ID, RECOVERY_ITEM_ID,
                        CROSS_BOUNDARY_ITEM_ID, UPSERT_RACE_ITEM_ID, SKIP0_ITEM_ID, SKIP50_ITEM_ID,
                        REBUILD_ITEM_ID)));
    }

    /**
     * 清理全部测试数据：Redis key + mdc_code_sn 水位记录
     */
    private void cleanTestData() {
        List<String> keys = scanTestKeys();
        if (!keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
        codeSnMapper.deleteByQuery(QueryWrapper.create()
                .in(CodeSn::getRuleItemId, List.of(CONCURRENCY_ITEM_ID, RECOVERY_ITEM_ID,
                        CROSS_BOUNDARY_ITEM_ID, UPSERT_RACE_ITEM_ID, SKIP0_ITEM_ID, SKIP50_ITEM_ID,
                        REBUILD_ITEM_ID)));
    }

    /**
     * SCAN 测试用 Redis key（避免 KEYS 阻塞）
     */
    private List<String> scanTestKeys() {
        return stringRedisTemplate.execute((RedisCallback<List<String>>) connection -> {
            List<String> keys = new ArrayList<>();
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions()
                    .match(TEST_KEY_PATTERN).count(500).build());
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
            cursor.close();
            return keys;
        });
    }
}
