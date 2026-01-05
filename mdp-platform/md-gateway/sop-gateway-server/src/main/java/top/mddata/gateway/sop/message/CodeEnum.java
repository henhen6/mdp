package top.mddata.gateway.sop.message;

import com.gitee.sop.support.message.I18nMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author 六如
 */
@Getter
@AllArgsConstructor
public enum CodeEnum implements I18nMessage {
    SUCCESS("0", "成功"),
    /**
     * 认证异常
     */
    AUTH("20001", "认证异常"),
    /**
     * 缺少参数
     */
    MISSING("40001", "缺少参数"),
    /**
     * 错误参数
     */
    INVALID("40002", "错误参数"),
    /**
     * 业务异常
     */
    BIZ("50003", "业务异常"),
    /**
     * 权限异常
     */
    ISV_PERM("40006", "权限异常"),
    /**
     * 未知异常
     */
    UNKNOWN("99999", "未知异常");

    private final String configKey;
    private final String configValue;

    public static CodeEnum of(String code) {
        for (CodeEnum value : CodeEnum.values()) {
            if (Objects.equals(value.configKey, code)) {
                return value;
            }
        }
        return UNKNOWN;
    }


    public String getCode() {
        return configKey;
    }
}
