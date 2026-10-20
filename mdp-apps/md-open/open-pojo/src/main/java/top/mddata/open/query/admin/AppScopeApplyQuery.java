package top.mddata.open.query.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;

/**
 * 权限组申请 查询入参。
 *
 * @author henhen6
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "权限组申请查询")
public class AppScopeApplyQuery extends ExtraParams implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "所属应用")
    private Long appId;

    @Schema(description = "申请权限组")
    private Long groupId;

    @Schema(description = "审核状态[1-待审批 2-通过 99-退回]")
    private Integer auditStatus;
}
