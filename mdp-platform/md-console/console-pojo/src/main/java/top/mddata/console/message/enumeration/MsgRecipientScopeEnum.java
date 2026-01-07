package top.mddata.console.message.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 接收范围
 * [0-所有人 1-指定用户 2-指定角色 3-指定部门]
 *
 * @author henhen6
 * @date 2022/7/10 0010 15:00
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "消息接收范围-枚举")
public enum MsgRecipientScopeEnum implements BaseEnum<Integer> {
    ALL(0, "所有人"),
    USER(1, "指定用户"),
    ROLE(2, "指定角色"),
    DEPT(3, "指定部门");

    private Integer code;
    private String desc;

    public static MsgRecipientScopeEnum match(Integer code, MsgRecipientScopeEnum def) {
        for (MsgRecipientScopeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return def;
    }

    public static MsgRecipientScopeEnum of(Integer val) {
        return match(val, null);
    }
}
