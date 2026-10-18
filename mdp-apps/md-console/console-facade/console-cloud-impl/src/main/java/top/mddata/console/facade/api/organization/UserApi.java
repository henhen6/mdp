package top.mddata.console.facade.api.organization;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;
import top.mddata.common.entity.User;
import top.mddata.console.facade.api.organization.fallback.UserApiFallback;

/**
 *
 * @author henhen
 * @since 2026/5/10 23:07
 */
@FeignClient(name = AppConstants.CONSOLE_SERVER, fallback = UserApiFallback.class, path = "/inner/organization/user")
public interface UserApi {
    /**
     * 根据邮箱注册账号
     * @param ssoUser 用户
     */
    @PostMapping("/registerByEmail")
    R<Boolean> registerByEmail(@RequestBody User ssoUser);

    /**
     * 根据手机注册账号
     * @param ssoUser 用户
     */
    @PostMapping("/registerByPhone")
    R<Boolean> registerByPhone(@RequestBody User ssoUser);

    /**
     * 注册账号
     * @param defUser 用户信息
     */
    @PostMapping("/registerByUsername")
    R<Boolean> registerByUsername(@RequestBody User defUser);
}
