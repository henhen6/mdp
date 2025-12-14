package top.mddata.workbench.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.OS;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.log.util.AddressUtil;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.DateUtils;
import top.mddata.common.entity.User;
import top.mddata.workbench.dto.LoginLogDto;
import top.mddata.workbench.entity.LoginLog;
import top.mddata.workbench.mapper.LoginLogMapper;
import top.mddata.workbench.service.LoginLogService;

import java.net.InetAddress;
import java.net.UnknownHostException;
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
    public void save(LoginLogDto dto, User user) {
        LoginLog loginLog = new LoginLog();
        if (user != null) {
            loginLog.setUserId(user.getId());
            loginLog.setName(user.getName());
            loginLog.setAccount(dto.getAccount());
            loginLog.setStatus(dto.getStatus().getCode());
        }
        loginLog.setEventType(dto.getEventType().getCode());
        loginLog.setStatus(dto.getStatus().getCode());
        loginLog.setStatusReason(dto.getStatusReason());
        loginLog.setAuthType(dto.getAuthType().getCode());
        loginLog.setLoginChannel(dto.getLoginChannel().getCode());
        loginLog.setLoginDate(DateUtils.formatAsDate(LocalDateTime.now()));
        loginLog.setLoginIp(dto.getLoginIp());
        loginLog.setUa(dto.getUa());
        UserAgent userAgent = UserAgentUtil.parse(loginLog.getUa());
        Browser browser = userAgent.getBrowser();
        OS os = userAgent.getOs();
        String browserVersion = userAgent.getVersion();
        if (browser != null) {
            loginLog.setBrowserName(simplifyBrowser(browser.getName()));
        }
        if (browserVersion != null) {
            loginLog.setBrowserVersion(browserVersion);
        }
        if (os != null) {
            loginLog.setOs(simplifyOperatingSystem(os.getName()));
        }
        loginLog.setIpLocation(isLocalHostIp(dto.getLoginIp()) ? "localhost" : AddressUtil.getRegion(dto.getLoginIp()));
        loginLog.setDeviceInfo(dto.getDeviceInfo());
        loginLog.setAppKey(dto.getAppKey());
        loginLog.setAppName(dto.getAppName());
        loginLog.setAppRedirect(dto.getAppRedirect());
        loginLog.setTokenInfo(dto.getTokenInfo());

        loginLog.setCreatedBy(loginLog.getUserId());
        save(loginLog);
    }

    /**
     * 判断是否为本地IP地址的方法
     */
    private boolean isLocalHostIp(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            return inetAddress.isLoopbackAddress();
        } catch (UnknownHostException e) {
            // 处理异常情况，如果无法解析IP地址，则不视为本地地址
            return false;
        }
    }
}
