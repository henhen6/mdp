package top.mddata.open.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 权限组申请 入参。
 *
 * @author henhen6
 */
@Data
@Accessors(chain = true)
@Schema(description = "权限组申请")
public class AppScopeApplyDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属应用
     */
    @NotNull(message = "应用不能为空")
    @Schema(description = "所属应用")
    private Long appId;

    /**
     * 申请的权限组（一单一组，批量提交时后端循环生成多张申请单）
     */
    @NotEmpty(message = "请至少勾选一个权限组")
    @Schema(description = "申请权限组")
    private List<Long> groupIds;

    /**
     * 申请理由
     */
    @NotBlank(message = "请填写申请理由")
    @Size(max = 512, message = "申请理由长度不能超过{max}")
    @Schema(description = "申请理由")
    private String applyReason;
}
