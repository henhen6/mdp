package top.mddata.workbench.enumeration;

import com.mybatisflex.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

/**
 * <p>
 * 事件类型
 * [01-登录 02-退出 03-注销 04-切换 05-扮演]
 * </p>
 *
 * @author henhen6
 * @date 2025年07月10日08:32:48
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "LoginEventTypeEnum", description = "事件类型-枚举")
public enum LoginEventTypeEnum implements BaseEnum<String> {
    LOGIN("01", "登录"),
    LOGOUT("02", "退出"),
    LOGOUT_CANCEL("03", "注销"),
    LOGIN_SWITCH("04", "切换"),
    LOGIN_PLAY("05", "扮演");

    @Schema(description = "code")
    @EnumValue
    private String code;
    @Schema(description = "描述")
    private String desc;
}
