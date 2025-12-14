package top.mddata.workbench.dto;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.enumeration.LoginChannelEnum;
import top.mddata.workbench.enumeration.LoginEventTypeEnum;
import top.mddata.workbench.enumeration.LoginStatusEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录日志 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-12-14 14:15:09
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "登录日志Dto")
public class LoginLogDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 登录账号
     */
    private String account;
    /**
     * 事件类型
     * [01-登录 02-退出 03-注销 04-切换 05-扮演]
     */
    private LoginEventTypeEnum eventType;

    /**
     * '登录状态
     */
    private LoginStatusEnum status;
    /**
     * 密码错误
     */
    private boolean passwordError;
    /**
     * 认证方式
     * [01-用户名密码验证码登录 02-用户名密码登录 03-手机短信验证码 04-邮箱验证码登录]
     */
    private AuthTypeEnum authType;
    /**
     * 登录渠道
     * [01-系统登录页 02-移动端]
     */
    private LoginChannelEnum loginChannel;
    /**
     * 登录状态原因
     */
    private String statusReason;
    /**
     * 登录IP
     */
    private String loginIp;
    /**
     * 浏览器请求头
     */
    private String ua;
    /**
     * 设备信息
     */
    private String deviceInfo;
    /**
     * 应用Key
     */
    private String appKey;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用地址
     */
    private String appRedirect;

    /**
     * 登录令牌
     */
    private String tokenInfo;

    public static LoginLogDto success(AuthTypeEnum authType, String deviceInfo, String account, String statusReason, String token) {
        LoginLogDto dto = new LoginLogDto();
        return dto
                .setAccount(account)
                .setEventType(LoginEventTypeEnum.LOGIN)
                .setStatus(LoginStatusEnum.SUCCESS)
                .setStatusReason(statusReason)
                .setAuthType(authType)
                .setLoginChannel(LoginChannelEnum.PC_LOGIN)
                .setDeviceInfo(deviceInfo)
                .setTokenInfo(token)
                .setInfo();
    }

    public static LoginLogDto failByCheck(AuthTypeEnum authType, String deviceInfo, String account, String statusReason) {
        LoginLogDto dto = new LoginLogDto();
        return dto
                .setAccount(account)
                .setEventType(LoginEventTypeEnum.LOGIN)
                .setStatus(LoginStatusEnum.FAIL)
                .setStatusReason(statusReason)
                .setAuthType(authType)
                .setLoginChannel(LoginChannelEnum.PC_LOGIN)
                .setDeviceInfo(deviceInfo)
                .setInfo();
    }
//    public static LoginLogDto failByCheck(AuthTypeEnum authType, String deviceInfo, String account, String statusReason) {
//        LoginLogDto dto = new LoginLogDto();
//        return dto
//                .setAccount(account)
//                .setEventType(LoginEventTypeEnum.LOGIN)
//                .setStatus(LoginStatusEnum.FAIL)
//                .setStatusReason(statusReason)
//                .setAuthType(authType)
//                .setLoginChannel(LoginChannelEnum.PC_LOGIN)
//                .setDeviceInfo(deviceInfo)
//                .setInfo();
//    }

    private LoginLogDto setInfo() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return this;
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String tempUa = StrUtil.sub(request.getHeader("user-agent"), 0, 500);
        String tempIp = JakartaServletUtil.getClientIP(request);

        this.ua = tempUa;
        this.loginIp = tempIp;
        return this;
    }

}
