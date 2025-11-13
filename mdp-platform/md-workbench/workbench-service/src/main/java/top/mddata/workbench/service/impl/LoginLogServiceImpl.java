package top.mddata.workbench.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.OS;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.DateUtils;
import top.mddata.common.entity.User;
import top.mddata.workbench.entity.LoginLog;
import top.mddata.workbench.event.model.LoginStatusDto;
import top.mddata.workbench.mapper.LoginLogMapper;
import top.mddata.workbench.service.LoginLogService;

import java.time.LocalDateTime;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * 登录日志 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 23:46:53
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LoginLogServiceImpl extends SuperServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {
    private static final Supplier<Stream<String>> BROWSER = () -> Stream.of(
            "Chrome", "Firefox", "Microsoft Edge", "Safari", "Opera"
    );
    private static final Supplier<Stream<String>> OPERATING_SYSTEM = () -> Stream.of(
            "Android", "Linux", "Mac OS X", "Ubuntu", "Windows 10", "Windows 8", "Windows 7", "Windows XP", "Windows Vista"
    );

    private static String simplifyOperatingSystem(String operatingSystem) {
        return OPERATING_SYSTEM.get().parallel().filter(b -> StrUtil.containsIgnoreCase(operatingSystem, b)).findAny().orElse(operatingSystem);
    }

    private static String simplifyBrowser(String browser) {
        return BROWSER.get().parallel().filter(b -> StrUtil.containsIgnoreCase(browser, b)).findAny().orElse(browser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(LoginStatusDto loginStatus, User user) {
        LoginLog sysLoginLog = BeanUtil.toBean(loginStatus, LoginLog.class);
        if (user != null) {
            sysLoginLog.setName(user.getName());
            sysLoginLog.setUserId(user.getId());
            sysLoginLog.setUsername(user.getUsername());
            sysLoginLog.setStatus(loginStatus.getStatus().getCode());
        }
        sysLoginLog.setLoginDate(DateUtils.formatAsDate(LocalDateTime.now()));

        UserAgent userAgent = UserAgentUtil.parse(sysLoginLog.getUa());
        Browser browser = userAgent.getBrowser();
        OS os = userAgent.getOs();
        String browserVersion = userAgent.getVersion();
        if (browser != null) {
            sysLoginLog.setBrowser(simplifyBrowser(browser.getName()));
        }
        if (browserVersion != null) {
            sysLoginLog.setBrowserVersion(browserVersion);
        }
        if (os != null) {
            sysLoginLog.setOperatingSystem(simplifyOperatingSystem(os.getName()));
        }
        sysLoginLog.setCreatedBy(sysLoginLog.getUserId());

        save(sysLoginLog);
    }
}
