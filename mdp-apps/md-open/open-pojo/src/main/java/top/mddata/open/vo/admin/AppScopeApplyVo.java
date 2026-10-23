package top.mddata.open.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限组申请 出参。
 *
 * @author henhen6
 */
@Data
@Accessors(chain = true)
@Schema(description = "权限组申请")
public class AppScopeApplyVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "所属应用")
    private Long appId;

    @Schema(description = "申请权限组")
    private Long groupId;

    @Schema(description = "申请理由")
    private String applyReason;

    @Schema(description = "审核状态[1-待审批 2-通过 99-退回]")
    private Integer auditStatus;

    @Schema(description = "审核时间")
    private LocalDateTime auditAt;

    @Schema(description = "审核意见")
    private String reviewComments;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "创建人ID")
    private Long createdBy;

    // ===== 回显字段（非表字段，Service/Controller 层组装） =====

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "权限组名称")
    private String groupName;
}
