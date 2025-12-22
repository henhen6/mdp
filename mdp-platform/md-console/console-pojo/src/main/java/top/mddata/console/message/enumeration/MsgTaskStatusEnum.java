package top.mddata.console.message.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

import java.util.stream.Stream;

/**
 * <p>
 * 消息执行状态\n[0-草稿 1-待执行 2-执行成功 3-执行失败]
 * </p>
 *
 * @author henhen
 * @date 2022-07-10 11:41:17
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "消息执行状态-枚举")
public enum MsgTaskStatusEnum implements BaseEnum<Integer> {

    /**
     * DRAFT
     */
    DRAFT(0, "草稿"),
    /**
     * WAITING
     */
    WAITING(1, "等待执行"),
    /**
     * SUCCESS
     */
    SUCCESS(2, "执行成功"),
    /**
     * FAIL
     */
    FAIL(3, "执行失败");
    private Integer code;

    @Schema(description = "描述")
    private String desc;

    /**
     * 根据当前枚举的name匹配
     */
    public static MsgTaskStatusEnum match(String val, MsgTaskStatusEnum def) {
        return Stream.of(values()).parallel().filter(item -> item.name().equalsIgnoreCase(val)).findAny().orElse(def);
    }

    public static MsgTaskStatusEnum get(String val) {
        return match(val, null);
    }

}
