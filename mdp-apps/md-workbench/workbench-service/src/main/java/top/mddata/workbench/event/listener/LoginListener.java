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

        User user = null;
        switch (loginLogDto.getAuthType()) {
            case PHONE:
                if (StrUtil.isNotEmpty(loginLogDto.getAccount())) {
                    user = ssoUserService.getByPhone(loginLogDto.getAccount());
                } else if (loginLogDto.getUserId() != null) {
                    user = ssoUserService.getByIdCache(loginLogDto.getUserId());
                }
                break;
            case EMAIL:
                if (StrUtil.isNotEmpty(loginLogDto.getAccount())) {
                    user = ssoUserService.getByEmail(loginLogDto.getAccount());
                } else if (loginLogDto.getUserId() != null) {
                    user = ssoUserService.getByIdCache(loginLogDto.getUserId());
                }
                break;
            default:
                if (StrUtil.isNotEmpty(loginLogDto.getAccount())) {
                    user = ssoUserService.getByUsername(loginLogDto.getAccount());
                } else if (loginLogDto.getUserId() != null) {
                    user = ssoUserService.getByIdCache(loginLogDto.getUserId());
                }
                break;
        }

        if (user != null) {


            if (LoginStatusEnum.SUCCESS.eq(loginLogDto.getStatus())) {
                // 重置错误次数 和 最后登录时间
                this.ssoUserService.resetPwErrorNum(user.getId());
            } else if (loginLogDto.isPasswordError()) {
                // 密码错误
                this.ssoUserService.incrPwErrorNumById(user.getId());
            }
        } else {
            log.warn("用户 {} 不存在", JSON.toJSONString(loginLogDto));
        }

        loginLogService.save(loginLogDto, user);
    }

}
