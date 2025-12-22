package top.mddata.console.message.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

import java.util.stream.Stream;

/**
 * 接口执行日志状态
 * @author henhen
 * @date 2022/7/10 0010 15:52
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "接口执行日志状态-枚举")
public enum MsgInterfaceLogStatusEnum implements BaseEnum<Integer> {
    /**
     * 初始化
     */
    INIT(1, "初始化"),
    /**
     * 成功
     */
    SUCCESS(2, "成功"),
    /**
     * 失败
     */
    FAIL(3, "失败");
    private Integer code;
    private String desc;

    /**
     * 根据当前枚举的name匹配
     */
    public static MsgInterfaceLogStatusEnum match(String val, MsgInterfaceLogStatusEnum def) {
        return Stream.of(values()).parallel().filter(item -> item.name().equalsIgnoreCase(val)).findAny().orElse(def);
    }

    public static MsgInterfaceLogStatusEnum get(String val) {
        return match(val, null);
    }


}
