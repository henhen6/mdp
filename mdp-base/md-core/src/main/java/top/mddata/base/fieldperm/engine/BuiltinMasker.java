package top.mddata.base.fieldperm.engine;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

/**
 * 内置脱敏器：注册名与算法和 mybatis-flex {@code Masks} 保持一致，
 * 使 mdc_resource_field.mask_rule 配置值与 MaskManager 生态兼容。
 *
 * @author henhen6
 * @since 2026-10-02
 */
@Slf4j
public class BuiltinMasker implements Masker {

    /** 手机号（保留前三后四） */
    public static final String MOBILE = "mobile";
    /** 固定电话（保留前三后二） */
    public static final String FIXED_PHONE = "fixed_phone";
    /** 身份证号（保留前三后四） */
    public static final String ID_CARD_NUMBER = "id_card_number";
    /** 中文名 */
    public static final String CHINESE_NAME = "chinese_name";
    /** 地址 */
    public static final String ADDRESS = "address";
    /** 邮件（保留域名） */
    public static final String EMAIL = "email";
    /** 密码（全掩码） */
    public static final String PASSWORD = "password";
    /** 车牌号（保留前三后一） */
    public static final String CAR_LICENSE = "car_license";
    /** 银行卡号（保留前四后四） */
    public static final String BANK_CARD_NUMBER = "bank_card_number";

    private static final Map<String, UnaryOperator<String>> RULES = new ConcurrentHashMap<>();

    static {
        register(MOBILE, s -> s.startsWith("1") && s.length() == 11 ? mask(s, 3, 4, 4) : s);
        register(FIXED_PHONE, s -> s.length() > 5 ? mask(s, 3, 2, s.length() - 5) : s);
        register(ID_CARD_NUMBER, s -> s.length() >= 15 ? mask(s, 3, 4, s.length() - 7) : s);
        register(CHINESE_NAME, BuiltinMasker::maskChineseName);
        register(ADDRESS, s -> {
            if (s.length() > 6) {
                return mask(s, 6, 0, 3);
            }
            return s.length() > 3 ? mask(s, 3, 0, 3) : s;
        });
        register(EMAIL, BuiltinMasker::maskEmail);
        register(PASSWORD, s -> "*".repeat(s.length()));
        register(CAR_LICENSE, s -> mask(s, 3, 1, s.length() - 4));
        register(BANK_CARD_NUMBER, s -> s.length() >= 8 ? mask(s, 4, 4, 4) : s);
    }

    /** 注册自定义脱敏规则（同名覆盖） */
    public static void register(String name, UnaryOperator<String> rule) {
        RULES.put(name, rule);
    }

    /** 当前已注册的全部规则名（供配置页下拉） */
    public static java.util.Set<String> registeredNames() {
        return java.util.Set.copyOf(RULES.keySet());
    }

    @Override
    public Object mask(String maskRule, Object value) {
        if (!(value instanceof String s) || s.isEmpty()) {
            return value;
        }
        UnaryOperator<String> rule = RULES.get(maskRule);
        if (rule == null) {
            // 配置保存时已校验规则名，走到这里说明配置与运行环境不一致，原样返回并告警
            log.warn("未注册的脱敏规则: {}, 字段值按原文返回", maskRule);
            return value;
        }
        return rule.apply(s);
    }

    private static String maskChineseName(String name) {
        return switch (name.length()) {
            case 2 -> name.charAt(0) + "*";
            case 3 -> name.charAt(0) + "*" + name.charAt(2);
            case 4 -> "**" + name.substring(2, 4);
            default -> name.length() > 4 ? mask(name, 2, 1, name.length() - 3) : name;
        };
    }

    private static String maskEmail(String fullEmail) {
        int indexOf = fullEmail.lastIndexOf('@');
        if (indexOf < 0) {
            return fullEmail;
        }
        String email = fullEmail.substring(0, indexOf);
        String domain = fullEmail.substring(indexOf);
        if (email.length() == 1) {
            return "*" + domain;
        }
        if (email.length() == 2) {
            return "**" + domain;
        }
        return email.length() < 5 ? mask(email, 2, 0, email.length() - 2) + domain
                : mask(email, 3, 0, email.length() - 3) + domain;
    }

    private static String mask(String s, int keepFirst, int keepLast, int maskCount) {
        if (keepFirst + keepLast >= s.length()) {
            return "*".repeat(Math.max(maskCount, 0));
        }
        return s.substring(0, keepFirst) + "*".repeat(maskCount) + s.substring(s.length() - keepLast);
    }
}
