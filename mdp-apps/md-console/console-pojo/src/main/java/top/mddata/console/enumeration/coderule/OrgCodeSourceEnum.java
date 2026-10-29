package top.mddata.console.enumeration.coderule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 组织编码来源
 * [DEPT-当前用户部门编码 COMPANY-当前用户机构编码]
 */
@Getter
@AllArgsConstructor
@Schema(title = "OrgCodeSourceEnum", description = "编码规则-组织编码来源")
public enum OrgCodeSourceEnum implements BaseEnum<String> {
    /** 当前用户部门编码 */
    DEPT("DEPT", "当前用户部门编码"),
    /** 当前用户机构编码 */
    COMPANY("COMPANY", "当前用户机构编码");
    private final String code;
    private final String desc;
}
