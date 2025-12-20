package top.mddata.open.admin.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用申请 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-11-27 03:31:55
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "应用申请")
public class AppApplyQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 应用简介
     */
    @Schema(description = "应用简介")
    private String intro;

    /**
     * 首页地址
     */
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
    @Schema(description = "备注")
    private String remark;

    /**
     * 资质文件
     */
    @Schema(description = "资质文件")
    private Long credentialFile;

    /**
     * 审核状态
     * [0-待提交 1-待审批 2-通过 99-退回]
     *
     */
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
    @Schema(description = "审核意见")
    private String reviewComments;

    /**
     * 添加时间
     */
    @Schema(description = "添加时间")
    private LocalDateTime createdAt;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;

    /**
     * 创建人id
     */
    @Schema(description = "创建人id")
    private Long createdBy;

    /**
     * 修改人id
     */
    @Schema(description = "修改人id")
    private Long updatedBy;

}
