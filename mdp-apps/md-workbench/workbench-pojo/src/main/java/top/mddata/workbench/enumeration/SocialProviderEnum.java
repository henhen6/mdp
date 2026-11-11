package top.mddata.workbench.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.exception.BizException;

import java.util.Arrays;

/**
 * 第三方登录平台
 *
 * @author tangyh
 */
@Getter
@AllArgsConstructor
public enum SocialProviderEnum {
    /** 微信开放平台 */
    WECHAT_OPEN("wechat-open", "微信", AuthTypeEnum.WECHAT_OPEN),
    /** QQ */
    QQ("qq", "QQ", AuthTypeEnum.QQ),
    /** 钉钉 */
    DINGTALK("dingtalk", "钉钉", AuthTypeEnum.DINGTALK),
    /** Gitee */
    GITEE("gitee", "Gitee", AuthTypeEnum.GITEE);

    @JsonValue
    private final String code;
    private final String label;
    private final AuthTypeEnum authType;

    /**
     * 按前端编码解析平台
     *
     * @param code 前端编码
     * @return 平台枚举
     */
    @JsonCreator
    public static SocialProviderEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new BizException("不支持的第三方登录平台"));
    }
}
