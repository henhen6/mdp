package top.mddata.base.oauth2.core.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * UserInfo 端点标准响应（OIDC Core §5.1）
 * <p>
 * 仅包含对用户公开的基础信息，禁止在此类中添加密码、盐、密码错误记录等敏感字段
 *
 * @author henhen6
 */
@Data
@Schema(title = "Oauth2UserInfoResponse", description = "UserInfo 端点标准响应")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Oauth2UserInfoResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识（OIDC 必填字段，值为用户id的字符串形式）
     */
    @Schema(description = "用户唯一标识")
    private String sub;

    /**
     * 登录账号
     */
    @Schema(description = "登录账号")
    private String username;

    /**
     * 姓名
     */
    @Schema(description = "姓名")
    private String name;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 性别
     */
    @Schema(description = "性别")
    private String sex;

    /**
     * 头像文件id
     */
    @Schema(description = "头像文件id")
    private Long avatar;

    @Schema(description = "扩展参数")
    private Map<String, Object> extra = new LinkedHashMap<>();

    /**
     * 添加扩展属性（序列化时平铺到顶层）
     *
     * @param key   属性名
     * @param value 属性值
     */
    @JsonAnySetter
    public void addExtra(String key, Object value) {
        this.extra.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getExtra() {
        return extra;
    }
}
