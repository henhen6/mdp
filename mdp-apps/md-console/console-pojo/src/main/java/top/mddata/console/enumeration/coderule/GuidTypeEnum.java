package top.mddata.console.enumeration.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * GUID 类型
 * [GUID-GUID SNOWFLAKE-雪花ID]
 */
@Getter
@AllArgsConstructor
@Schema(title = "GuidTypeEnum", description = "编码规则-GUID类型")
public enum GuidTypeEnum implements BaseEnum<String> {
    /** GUID */
    GUID("GUID", "GUID"),
    /** 雪花ID */
    SNOWFLAKE("SNOWFLAKE", "雪花ID");
    private final String code;
    private final String desc;
}
