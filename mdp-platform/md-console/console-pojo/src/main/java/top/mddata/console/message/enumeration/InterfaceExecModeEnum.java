package top.mddata.console.message.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

/**
 * [01-实现类 02-脚本]
 *
 * @author henhen6
 * @date 2022/7/10 0010 15:00
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "接口执行模式-枚举")
public enum InterfaceExecModeEnum implements BaseEnum<Integer> {
    /**
     * 实现类
     */
    IMPL_CLASS(1, "实现类"),
    MAGIC_API(2, "Magic API"),
    /**
     * 脚本
     */
    SCRIPT(3, "脚本");
    private Integer code;
    private String desc;

}
