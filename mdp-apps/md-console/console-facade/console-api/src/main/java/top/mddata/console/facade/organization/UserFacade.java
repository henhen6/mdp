package top.mddata.console.facade.organization;

import top.mddata.common.dto.UserSocialRegisterDto;
import top.mddata.common.entity.User;

/**
 * 用户
 * @author henhen
 * @since 2026/1/16 21:02
 */
public interface UserFacade {
    /**
     * 根据邮箱注册账号
     * @param ssoUser 用户
     */
    void registerByEmail(User ssoUser);

    /**
     * 根据手机注册账号
     * @param ssoUser 用户
     */
    void registerByPhone(User ssoUser);

    /**
     * 注册账号
     * @param defUser 用户信息
     */
    void registerByUsername(User defUser);

    /**
     * 社交注册：同一本地事务内创建用户与初始绑定
     * @param register 注册入参
     * @return 新用户ID
     */
    Long registerBySocial(UserSocialRegisterDto register);
}
