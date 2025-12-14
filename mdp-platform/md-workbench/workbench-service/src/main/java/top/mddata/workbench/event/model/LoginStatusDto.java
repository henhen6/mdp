//package top.mddata.workbench.event.model;
//
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.extra.servlet.JakartaServletUtil;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.NoArgsConstructor;
//import lombok.ToString;
//import lombok.experimental.Accessors;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.context.request.RequestAttributes;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//import top.mddata.workbench.enumeration.AuthTypeEnum;
//import top.mddata.workbench.enumeration.LoginChannelEnum;
//import top.mddata.workbench.enumeration.LoginEventTypeEnum;
//import top.mddata.workbench.enumeration.LoginStatusEnum;
//
//import java.io.Serial;
//import java.io.Serializable;
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//
///**
// * 登录状态DTO
// *
// * @author henhen6
// * @date 2020年03月18日17:25:44
// */
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Accessors(chain = true)
//@ToString(callSuper = true)
//@EqualsAndHashCode(callSuper = false)
//@Builder
//@Slf4j
//public class LoginStatusDto implements Serializable {
//    @Serial
//    private static final long serialVersionUID = -3124612657759050173L;
//
//    /**
//     * 登录用户ID
//     */
//    private Long userId;
//    /**
//     * 登录账号
//     */
//    private String account;
//    /**
//     * 事件类型
//     * [01-登录 02-退出 03-注销 04-切换 05-扮演]
//     */
//    private LoginEventTypeEnum eventType;
//
//    /**
//     * '登录状态
//     */
//    private LoginStatusEnum status;
//    /**
//     * 密码错误
//     */
//    private boolean passwordError;
//    /**
//     * 认证方式
//     * [01-用户名密码验证码登录 02-用户名密码登录 03-手机短信验证码 04-邮箱验证码登录]
//     */
//    private AuthTypeEnum authType;
//    /**
//     * 登录渠道
//     * [01-系统登录页 02-移动端]
//     */
//    private LoginChannelEnum loginChannel;
//    /**
//     * 登录状态原因
//     */
//    private String statusReason;
//    /**
//     * 登录IP
//     */
//    private String loginIp;
//    /**
//     * 浏览器请求头
//     */
//    private String ua;
//    /**
//     * 设备信息
//     */
//    private String deviceInfo;
//    /**
//     * 应用Key
//     */
//    private String appKey;
//
//    /**
//     * 应用名称
//     */
//    private String appName;
//
//    /**
//     * 应用地址
//     */
//    private String appRedirect;
//
//    /**
//     * 登录令牌
//     */
//    private String tokenInfo;
//
//
//    public static LoginStatusDto success(Long userId) {
//        return LoginStatusDto.builder()
//                .userId(userId)
//                .status(LoginStatusEnum.SUCCESS)
//                .statusReason("登录成功")
//                .build().setInfo();
//    }
//
//    public static LoginStatusDto fail(Long userId, LoginStatusEnum status, String statusReason) {
//        return LoginStatusDto.builder()
//                .userId(userId)
//                .status(status)
//                .statusReason(statusReason)
//                .build().setInfo();
//    }
//
//    public static LoginStatusDto fail(String account, String statusReason) {
//        return LoginStatusDto.builder()
//                .account(account)
//                .statusReason(statusReason)
//                .build().setInfo();
//    }
////
////    public static LoginStatusDto smsCodeError(String mobile, LoginStatusEnum status, String statusReason) {
////        return LoginStatusDto.builder()
////                .status(status).statusReason(statusReason)
////                .build().setInfo();
////    }
//
//    private LoginStatusDto setInfo() {
//        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
//        if (requestAttributes == null) {
//            return this;
//        }
//        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
//        String tempUa = StrUtil.sub(request.getHeader("user-agent"), 0, 500);
//        String tempIp = JakartaServletUtil.getClientIP(request);
//        log.info("tempIp={}, ua={}", tempIp, tempUa);
////        String tempLocation = isLocalHostIp(tempIp) ? "localhost" : AddressUtil.getRegion(tempIp);
//
//        this.ua = tempUa;
//        this.loginIp = tempIp;
////        this.location = tempLocation;
//        return this;
//    }
//
//    /**
//     * 判断是否为本地IP地址的方法
//     */
//    private boolean isLocalHostIp(String ipAddress) {
//        try {
//            InetAddress inetAddress = InetAddress.getByName(ipAddress);
//            return inetAddress.isLoopbackAddress();
//        } catch (UnknownHostException e) {
//            // 处理异常情况，如果无法解析IP地址，则不视为本地地址
//            return false;
//        }
//    }
//}
