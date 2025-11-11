package top.mddata.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核
 * @author henhen6
 * @since 2025/5/12 09:22
 */
@Data
public class AudioVO {
    @NotNull(message = "请填写ID")
    private Long id;

    /** 审核状态  0-初始化  1-申请中 2-通过  99-退回 */
    @NotNull(message = "请填写审核状态")
    @Schema(description = "审核状态")
    private Integer auditStatus;

    /** 审核意见 */
    @Schema(description = "审核意见")
    private String reviewComments;
}
