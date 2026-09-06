package top.mddata.open.enumeration.admin;

import com.mybatisflex.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 应用登录方式
 * [10-ticket模式 20-oauth2]
 *
 * @author henhen6
 * @date 2025年11月20日19:53:37
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "应用登录方式-枚举")
public enum AppLoginTypeEnum implements BaseEnum<String> {

    TICKET("10", "ticket模式"),
    OAUTH2("20", "OAuth2认证");
    /**
     * 资源类型
     */
    @EnumValue
    private String code;

    /**
     * 资源描述
     */
    private String desc;


}
