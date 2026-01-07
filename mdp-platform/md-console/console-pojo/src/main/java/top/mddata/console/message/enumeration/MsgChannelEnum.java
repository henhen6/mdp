package top.mddata.console.message.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

import java.util.stream.Stream;

/**
 * <p>
 * 发送渠道
 * [1-后台发送 2-API发送 3-JOB发送]
 * </p>
 *
 * @author henhen
 * @date 2022-07-10 11:41:17
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "MsgChannelEnum", description = "发送渠道-枚举")
public enum MsgChannelEnum implements BaseEnum<Integer> {

    /**
     * APP
     */
    WEB(1, "后台发送"),
    /**
     * API
     */
    API(2, "API发送"),
    JOB(3, "JOB发送");
    private Integer code;
    @Schema(description = "描述")
    private String desc;

    /**
     * 根据当前枚举的name匹配
     */
    public static MsgChannelEnum match(Integer val, MsgChannelEnum def) {
        return Stream.of(values()).parallel().filter(item -> item.getCode().equals(val)).findAny().orElse(def);
    }

    public static MsgChannelEnum of(Integer val) {
        return match(val, null);
    }


}
