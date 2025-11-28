package top.mddata.open.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用审核
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "应用审核")
public class AppApplyReviewDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 申请ID
     */
    @NotNull(message = "申请ID不能为空")
    private Long id;

    /**
     * 是否通过审核
     *
     */
    @NotNull(message = "是否通过审核不能为空")
    private Boolean approve;

    /**
     * 审核意见
     */
    private String reviewComments;
}
