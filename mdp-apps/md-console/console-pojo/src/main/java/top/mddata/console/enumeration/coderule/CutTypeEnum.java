package top.mddata.console.enumeration.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 父单号截取方式
 * [INDEX-按下标截取 SEPARATOR-按分隔符截取]
 */
@Getter
@AllArgsConstructor
@Schema(title = "CutTypeEnum", description = "编码规则-父单号截取方式")
public enum CutTypeEnum implements BaseEnum<String> {
    /** 按下标截取 */
    INDEX("INDEX", "按下标截取"),
    /** 按分隔符截取 */
    SEPARATOR("SEPARATOR", "按分隔符截取");
    private final String code;
    private final String desc;
}
