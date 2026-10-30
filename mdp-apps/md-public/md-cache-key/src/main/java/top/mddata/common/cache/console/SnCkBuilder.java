package top.mddata.common.cache.console;

import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 编码规则流水号计数器
 * key: [prefix:]code_sn:sn:{ruleItemId}:{periodKey}
 * value: 纯文本计数器（由 Lua 脚本 INIT_AND_INCR 原子写入，TTL 也由脚本 ARGV 管理，不走本 builder 的 expire）
 *
 * @author henhen6
 * @since 2026/10/30
 */
public class SnCkBuilder implements CacheKeyBuilder {

    private SnCkBuilder() {
    }

    /**
     * 构造流水号计数器 key
     *
     * @param ruleItemId 规则明细ID
     * @param periodKey  周期标签（归零维度，如 FOREVER/2026/202610/20261029）
     * @return cache key
     */
    public static CacheKey builder(Long ruleItemId, String periodKey) {
        return new SnCkBuilder().key(ruleItemId, periodKey);
    }

    /**
     * 按明细ID扫描其全部周期 key 的通配符
     *
     * @param ruleItemId 规则明细ID
     * @return SCAN 用的 match 表达式
     */
    public static String pattern(Long ruleItemId) {
        return new SnCkBuilder().key(ruleItemId, "*").getKey();
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Console.CODE_SN;
    }

    @Override
    public String getField() {
        return "sn";
    }

    /**
     * TTL 由 Lua 脚本的 ARGV 管理（按归零规则差异化设置），此处返回 null 表示不在 builder 层设置
     */
    @Override
    public Duration getExpire() {
        return null;
    }
}
