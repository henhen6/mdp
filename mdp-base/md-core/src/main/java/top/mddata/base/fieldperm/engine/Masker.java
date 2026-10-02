package top.mddata.base.fieldperm.engine;

/**
 * 脱敏函数（可插拔）。
 *
 * <p>md-core 默认使用 {@link BuiltinMasker}；业务侧可替换为委托
 * mybatis-flex {@code MaskManager} 的实现，从而共享其自定义注册规则。</p>
 *
 * @author henhen6
 * @since 2026-10-02
 */
@FunctionalInterface
public interface Masker {

    /**
     * 按规则对值脱敏。
     *
     * @param maskRule 规则名
     * @param value    原值
     * @return 脱敏后的值（规则未知时实现方应原样返回并告警）
     */
    Object mask(String maskRule, Object value);
}
