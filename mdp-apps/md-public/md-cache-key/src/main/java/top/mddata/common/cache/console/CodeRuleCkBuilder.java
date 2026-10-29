package top.mddata.common.cache.console;

import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 编码规则缓存
 * 根据规则编码缓存 规则+启用明细（CodeRuleCache）
 *
 * key: code_rule:code:{ruleCode}
 * value: CodeRuleCache
 *
 * @author henhen
 * @since 2026/10/29
 */
public class CodeRuleCkBuilder implements CacheKeyBuilder {

    /**
     * 构造器
     *
     * @param ruleCode 规则编码
     * @return key
     */
    public static CacheKey builder(String ruleCode) {
        return new CodeRuleCkBuilder().key(ruleCode);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.Console.CODE_RULE;
    }

    @Override
    public String getField() {
        return "code";
    }

    @Override
    public Duration getExpire() {
        return Duration.ofDays(1);
    }
}
