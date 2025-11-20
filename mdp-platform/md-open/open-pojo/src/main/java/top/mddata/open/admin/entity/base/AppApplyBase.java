package top.mddata.open.admin.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用申请实体类。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AppApplyBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_app_apply";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    private String name;

    /**
     * 应用简介
     */
    private String intro;

    /**
     * 首页地址
     */
    private String homeUrl;

    /**
     * 图标
     */
    private Long logo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 资质文件
     */
    private String credentialFile;

    /**
     * 审核状态
     * [0-待提交 1-待审批 2-通过 99-退回]
     *
     */
    private Integer auditStatus;

    /**
     * 审核时间
     */
    private LocalDateTime auditAt;

    /**
     * 提交时间
     */
    private LocalDateTime submissionAt;

    /**
     * 审核意见
     */
    private String reviewComments;

}
