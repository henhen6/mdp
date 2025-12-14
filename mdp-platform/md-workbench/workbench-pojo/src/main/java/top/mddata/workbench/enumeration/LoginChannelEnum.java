package top.mddata.workbench.enumeration;

import com.mybatisflex.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

/**
 * <p>
 * 登录渠道
 * [01-系统登录页 02-移动端登录]
 * </p>
 *
 * @author henhen6
 * @date 2025年07月10日08:32:48
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "LoginChannelEnum", description = "登录渠道-枚举")
public enum LoginChannelEnum implements BaseEnum<String> {
    PC_LOGIN("01", "系统登录页"),
    UNIAPP_LOGIN("02", "移动端登录");

    @Schema(description = "code")
    @EnumValue
    private String code;
    @Schema(description = "描述")
    private String desc;
}
