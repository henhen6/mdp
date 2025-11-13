package top.mddata.workbench.service;


import top.mddata.common.entity.User;
import top.mddata.workbench.dto.LoginDto;

/**
 * 登录接口策略
 * @author henhen6
 * @since 2025/7/10 09:09
 */
public interface LoginStrategy {

    /**
     * 校验参数
     * @param login 登录参数
     */
    void checkParam(LoginDto login);

    /**
     * 校验密码
     * @param login 登录参数
     * @param user 用户信息
     */
    void checkUserPassword(LoginDto login, User user);

    /**
     * 查找用户
     * @param value 用户名或手机或邮箱
     * @return 用户
     */
    User getUser(String value);

    /**
     * 检测用户状态是否正常
     * @param user 用户
     */
    void checkUserState(User user);
}
