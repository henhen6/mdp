package top.mddata.workbench.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 双因素认证开通记录实体类。
 *
 * @author tangyh
 * @since 2026-11-16
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserTwoFactorAuthBase extends SuperEntity<Long> implements Serializable {
    /**
     * 表名称
     */
    public static final String TABLE_NAME = "mdw_user_two_factor_auth";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否开通 0-关闭 1-开通
     */
    private Boolean enabled;

    /**
     * 验证渠道 PHONE-仅手机 EMAIL-仅邮箱 BOTH-手机或邮箱均可
     */
    private String channel;

    /**
     * 删除用户
     */
    private Long deletedBy;

    /**
     * 删除标识
     */
    private Long deletedAt;
}
