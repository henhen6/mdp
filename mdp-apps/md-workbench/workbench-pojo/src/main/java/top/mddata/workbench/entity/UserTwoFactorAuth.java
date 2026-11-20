package top.mddata.workbench.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.workbench.entity.base.UserTwoFactorAuthBase;
import top.mddata.workbench.enumeration.TwoFactorChannelEnum;

/**
 * 双因素认证开通记录
 *
 * @author tangyh
 * @since 2026-11-16
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(UserTwoFactorAuthBase.TABLE_NAME)
public class UserTwoFactorAuth extends UserTwoFactorAuthBase {

    /**
     * 获取验证渠道枚举
     */
    public TwoFactorChannelEnum channelEnum() {
        return TwoFactorChannelEnum.fromCode(getChannel());
    }
}
