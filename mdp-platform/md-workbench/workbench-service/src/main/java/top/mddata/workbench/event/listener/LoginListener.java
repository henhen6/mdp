package top.mddata.workbench.event.listener;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.mddata.common.entity.User;
import top.mddata.workbench.enumeration.LoginStatusEnum;
import top.mddata.workbench.event.LoginEvent;
import top.mddata.workbench.event.model.LoginStatusDto;
import top.mddata.workbench.service.LoginLogService;
import top.mddata.workbench.service.SsoUserService;

/**
 * 登录事件监听，用于记录登录日志
 *
 * @author henhen6
 * @date 2020年03月18日17:39:59
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LoginListener {
    private final SsoUserService ssoUserService;
    private final LoginLogService loginLogService;

    @Async
    @EventListener({LoginEvent.class})
    public void saveSysLog(LoginEvent event) {
        LoginStatusDto loginStatus = (LoginStatusDto) event.getSource();
        log.debug("loginStatus:{}", loginStatus);

        User user = null;
        if (loginStatus.getUserId() != null) {
            user = ssoUserService.getByIdCache(loginStatus.getUserId());
        } else if (StrUtil.isNotBlank(loginStatus.getUsername())) {
            user = ssoUserService.getByUsername(loginStatus.getUsername());
        }

        if (user == null) {
            log.debug("用户 {} 不存在", loginStatus.getUserId());
            return;
        }

        if (LoginStatusEnum.SUCCESS.eq(loginStatus.getStatus())) {
            // 重置错误次数 和 最后登录时间
            this.ssoUserService.resetPwErrorNum(loginStatus.getUserId());
        } else if (LoginStatusEnum.PASSWORD_ERROR.eq(loginStatus.getStatus())) {
            // 密码错误
            this.ssoUserService.incrPwErrorNumById(loginStatus.getUserId());
        }

        loginLogService.save(loginStatus, user);
    }

}
