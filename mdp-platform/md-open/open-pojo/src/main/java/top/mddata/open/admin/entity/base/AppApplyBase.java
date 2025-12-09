package top.mddata.open.admin.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用申请实体类。
 *
 * @author henhen6
 * @since 2025-11-27 03:31:55
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
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
    private Long credentialFile;

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
