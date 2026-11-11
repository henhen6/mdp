package top.mddata.console.facade.api.organization.fallback;

import org.springframework.stereotype.Component;
import top.mddata.base.base.R;
import top.mddata.common.dto.UserSocialRegisterDto;
import top.mddata.common.entity.User;
import top.mddata.console.facade.api.organization.UserApi;

/**
 *
 * @author henhen
 * @since 2026/5/10 23:20
 */
@Component
public class UserApiFallback implements UserApi {
    @Override
    public R<Boolean> registerByEmail(User ssoUser) {
        return R.timeout();
    }

    @Override
    public R<Boolean> registerByPhone(User ssoUser) {
        return R.timeout();
    }

    @Override
    public R<Boolean> registerByUsername(User defUser) {
        return R.timeout();
    }

    @Override
    public R<Long> registerBySocial(UserSocialRegisterDto register) {
        return R.timeout();
    }
}
