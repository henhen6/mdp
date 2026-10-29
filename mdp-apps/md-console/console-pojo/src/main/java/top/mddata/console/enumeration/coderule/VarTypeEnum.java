package top.mddata.console.enumeration.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 变量取值方式
 * [MAPPING-映射 REPLACE-替换]
 */
@Getter
@AllArgsConstructor
@Schema(title = "VarTypeEnum", description = "编码规则-变量取值方式")
public enum VarTypeEnum implements BaseEnum<String> {
    /** 映射 */
    MAPPING("MAPPING", "映射"),
    /** 替换 */
    REPLACE("REPLACE", "替换");
    private final String code;
    private final String desc;
}
