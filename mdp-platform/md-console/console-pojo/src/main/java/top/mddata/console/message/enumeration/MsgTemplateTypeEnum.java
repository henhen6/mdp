package top.mddata.console.message.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

import java.util.stream.Stream;

/**
 * <p>
 * 消息表
 * </p>
 *
 * @author henhen
 * @date 2021-11-15
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "MsgTemplateTypeEnum", description = "消息类型-枚举")
public enum MsgTemplateTypeEnum implements BaseEnum<Integer> {
    /**
     * NOTIFY="站内信"
     */
    NOTICE(1, "站内信"),
    /**
     * TO_DO="短信"
     */
    SMS(2, "短信"),
    /**
     * WARN="邮件"
     */
    MAIL(3, "邮件");

    private Integer code;
    @Schema(description = "描述")
    private String desc;


    /**
     * 根据当前枚举的name匹配
     */
    public static MsgTemplateTypeEnum match(String val, MsgTemplateTypeEnum def) {
        return Stream.of(values()).parallel().filter(item -> item.name().equalsIgnoreCase(val)).findAny().orElse(def);
    }

    public static MsgTemplateTypeEnum get(String val) {
        return match(val, null);
    }

}
