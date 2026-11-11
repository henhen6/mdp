package top.mddata.console.facade.impl.organization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.base.exception.BizException;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.dto.UserSocialRegisterDto;
import top.mddata.common.entity.User;
import top.mddata.console.facade.api.organization.UserApi;
import top.mddata.console.facade.organization.UserFacade;

/**
 *
 * @author henhen
 * @since 2026/1/16 21:05
 */
@Service
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {
    private final UserApi userApi;

    @Override
    public void registerByEmail(User ssoUser) {
        userApi.registerByEmail(ssoUser);
    }

    @Override
    public void registerByPhone(User ssoUser) {
        userApi.registerByPhone(ssoUser);
    }

    @Override
    public void registerByUsername(User defUser) {
        userApi.registerByUsername(defUser);
    }

    @Override
    public Long registerBySocial(UserSocialRegisterDto register) {
        R<Long> result = userApi.registerBySocial(register);
        ArgumentAssert.notNull(result, () -> new BizException("社交账号注册无响应"));
        // 超时回退与业务失败均不得被当成成功
        ArgumentAssert.isTrue(result.getIsSuccess(),
                () -> new BizException("社交账号注册失败：" + result.getMsg()));
        return result.getData();
    }
}
