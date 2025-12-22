package top.mddata.console.message.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

import java.util.stream.Stream;

/**
 * <p>
 * 消息分类
 * [1-待办 2-预警 3-提醒]
 * </p>
 *
 * @author henhen
 * @date 2021-11-15
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "消息分类-枚举")
public enum MsgCategoryEnum implements BaseEnum<Integer> {

    /**
     * TO_DO="待办"
     */
    TO_DO(1, "待办"),
    /**
     * WARN="预警"
     */
    EARLY_WARNING(2, "预警"),
    /**
     * NOTIFY="提醒"
     */
    NOTICE(3, "提醒");

    private Integer code;
    @Schema(description = "描述")
    private String desc;


    /**
     * 根据当前枚举的name匹配
     */
    public static MsgCategoryEnum match(String val, MsgCategoryEnum def) {
        return Stream.of(values()).parallel().filter(item -> item.name().equalsIgnoreCase(val)).findAny().orElse(def);
    }

    public static MsgCategoryEnum get(String val) {
        return match(val, null);
    }


}
