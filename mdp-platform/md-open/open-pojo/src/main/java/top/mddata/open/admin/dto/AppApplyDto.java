package top.mddata.open.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用申请 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "应用申请")
public class AppApplyDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @NotNull(message = "请填写id", groups = BaseEntity.Update.class)
    @Schema(description = "id")
    private Long id;

    /**
     * 名称
     */
    @NotEmpty(message = "请填写名称")
    @Size(max = 255, message = "名称长度不能超过{max}")
    @Schema(description = "名称")
    private String name;

    /**
     * 应用简介
     */
    @NotEmpty(message = "请填写应用简介")
    @Size(max = 512, message = "应用简介长度不能超过{max}")
    @Schema(description = "应用简介")
    private String intro;

    /**
     * 首页地址
     */
    @Size(max = 512, message = "首页地址长度不能超过{max}")
    @Schema(description = "首页地址")
    private String homeUrl;

    /**
     * 图标
     */
    @Schema(description = "图标")
    private Long logo;

    /**
     * 备注
     */
    @Size(max = 1024, message = "备注长度不能超过{max}")
    @Schema(description = "备注")
    private String remark;

    /**
     * 资质文件
     */
    @Size(max = 255, message = "资质文件长度不能超过{max}")
    @Schema(description = "资质文件")
    private String credentialFile;

    /**
     * 审核状态
     * [0-待提交 1-待审批 2-通过 99-退回]
     *
     */
    @NotNull(message = "请填写审核状态")
    @Schema(description = "审核状态")
    private Integer auditStatus;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private LocalDateTime auditAt;

    /**
     * 提交时间
     */
    @Schema(description = "提交时间")
    private LocalDateTime submissionAt;

    /**
     * 审核意见
     */
    @Size(max = 255, message = "审核意见长度不能超过{max}")
    @Schema(description = "审核意见")
    private String reviewComments;

}
