package top.mddata.workbench.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.exception.BizException;

import java.util.Arrays;

/**
 * 第三方授权动作类型
 *
 * <p>state 与 reauthTicket 绑定具体动作，票据不能跨动作使用。</p>
 *
 * @author tangyh
 */
@Getter
@AllArgsConstructor
public enum SocialActionEnum {
    /** 登录 */
    LOGIN("login", "登录"),
    /** 绑定 */
    BIND("bind", "绑定"),
    /** 解绑 */
    UNBIND("unbind", "解绑"),
    /** 跨账号转移 */
    TRANSFER("transfer", "转移"),
    /** 第三方重新授权（二次验证） */
    REAUTH("reauth", "二次验证");

    @JsonValue
    private final String code;
    private final String label;

    /**
     * 按前端编码解析动作
     *
     * @param code 前端编码
     * @return 动作枚举
     */
    @JsonCreator
    public static SocialActionEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new BizException("不支持的第三方授权动作"));
    }
}
