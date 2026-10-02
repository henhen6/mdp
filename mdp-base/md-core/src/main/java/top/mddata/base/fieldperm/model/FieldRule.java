package top.mddata.base.fieldperm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字段规则。
 *
 * <p>作为 Redis 缓存模型的组成部分，必须保留无参构造供 Jackson 反序列化。</p>
 *
 * @author henhen6
 * @since 2026-10-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 处理动作：隐藏（无权限时置 null） */
    public static final int RULE_TYPE_HIDE = 10;
    /** 处理动作：脱敏（无权限时按 maskRule 变形） */
    public static final int RULE_TYPE_MASK = 20;

    /**
     * 处理动作 [10-隐藏 20-脱敏]
     */
    private Integer ruleType;

    /**
     * 脱敏规则名（ruleType=20 时必填）
     */
    private String maskRule;

    public static FieldRule hide() {
        return new FieldRule(RULE_TYPE_HIDE, null);
    }

    public static FieldRule mask(String maskRule) {
        return new FieldRule(RULE_TYPE_MASK, maskRule);
    }

    public boolean isHide() {
        return ruleType != null && ruleType == RULE_TYPE_HIDE;
    }

    public boolean isMask() {
        return ruleType != null && ruleType == RULE_TYPE_MASK;
    }
}
