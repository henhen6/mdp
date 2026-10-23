package top.mddata.open.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 权限组申请 审核入参。
 *
 * @author henhen6
 */
@Data
@Accessors(chain = true)
@Schema(description = "权限组申请审核")
public class AppScopeApplyReviewDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 申请单ID
     */
    @NotNull(message = "id不能为空")
    @Schema(description = "申请单ID")
    private Long id;

    /**
     * 是否通过
     */
    @NotNull(message = "审核结果不能为空")
    @Schema(description = "是否通过")
    private Boolean approve;

    /**
     * 审核意见（退回时必填，Service 层校验）
     */
    @Size(max = 255, message = "审核意见长度不能超过{max}")
    @Schema(description = "审核意见")
    private String reviewComments;
}
