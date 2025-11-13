package top.mddata.workbench.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.common.entity.User;
import top.mddata.workbench.entity.LoginLog;
import top.mddata.workbench.event.model.LoginStatusDto;

/**
 * 登录日志 服务层。
 *
 * @author henhen6
 * @since 2025-11-12 23:46:53
 */
public interface LoginLogService extends SuperService<LoginLog> {

    /**
     * 保存登录日志
     * @param loginStatus 登录信息
     * @param user 用户信息
     */
    void save(LoginStatusDto loginStatus, User user);
}
