package top.mddata.workbench.event.listener;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.mddata.common.entity.User;
import top.mddata.workbench.dto.LoginLogDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.enumeration.LoginEventTypeEnum;
import top.mddata.workbench.enumeration.LoginStatusEnum;
import top.mddata.workbench.event.LoginEvent;
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
        LoginLogDto loginLogDto = (LoginLogDto) event.getSource();
        log.debug("loginStatus:{}", loginLogDto);

        User user = resolveUser(loginLogDto);

        if (user != null) {
            // 重置/累计密码错误次数仅对登录事件有意义，退出、注销等事件不触发
            if (loginLogDto.getEventType() == LoginEventTypeEnum.LOGIN) {
                if (LoginStatusEnum.SUCCESS.eq(loginLogDto.getStatus())) {
                    // 重置错误次数 和 最后登录时间
                    this.ssoUserService.resetPwErrorNum(user.getId());
                } else if (loginLogDto.isPasswordError()) {
                    // 密码错误
                    this.ssoUserService.incrPwErrorNumById(user.getId());
                }
            }
        } else {
            log.warn("用户 {} 不存在", JSON.toJSONString(loginLogDto));
        }

        loginLogService.save(loginLogDto, user);
    }

    /**
     * 按认证方式反查用户：优先按账号精确匹配，其次按 userId 回查
     */
    private User resolveUser(LoginLogDto loginLogDto) {
        if (StrUtil.isNotEmpty(loginLogDto.getAccount())) {
            AuthTypeEnum authType = loginLogDto.getAuthType();
            if (authType == AuthTypeEnum.PHONE) {
                return ssoUserService.getByPhone(loginLogDto.getAccount());
            }
            if (authType == AuthTypeEnum.EMAIL) {
                return ssoUserService.getByEmail(loginLogDto.getAccount());
            }
            return ssoUserService.getByUsername(loginLogDto.getAccount());
        }
        if (loginLogDto.getUserId() != null) {
            return ssoUserService.getByIdCache(loginLogDto.getUserId());
        }
        return null;
    }

}
