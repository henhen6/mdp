package top.mddata.open.entity.admin.base;

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
 * 权限组申请实体类。
 *
 * @author henhen6
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AppScopeApplyBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_app_scope_apply";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属应用
     */
    private Long appId;

    /**
     * 申请权限组
     */
    private Long groupId;

    /**
     * 申请理由
     */
    private String applyReason;

    /**
     * 审核状态
     * [1-待审批 2-通过 99-退回]
     */
    private Integer auditStatus;

    /**
     * 审核时间
     */
    private LocalDateTime auditAt;

    /**
     * 审核意见
     */
    private String reviewComments;

}
