package top.mddata.workbench.event.model;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.mddata.base.log.util.AddressUtil;
import top.mddata.workbench.enumeration.LoginStatusEnum;

import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 登录状态DTO
 *
 * @author henhen6
 * @date 2020年03月18日17:25:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Slf4j
public class LoginStatusDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -3124612657759050173L;

    /**
     * 登录用户
     */
    private Long userId;
    private String username;
    /**
     * 登录描述
     */
    private String description;
    /**
     * 登录IP
     */
    private String requestIp;
    /**
     * 浏览器请求头
     */
    private String ua;
    /**
     * 登录地点
     */
    private String location;

    /**
     * '登录状态;[01-登录成功 02-验证码错误 03-密码错误 04-账号锁定 05-切换租户 06-短信验证码错误]
     *
     * @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.LOGIN_STATUS)
     */

    private LoginStatusEnum status;

    public static LoginStatusDto success(Long userId) {
        return LoginStatusDto.builder()
                .userId(userId)
                .status(LoginStatusEnum.SUCCESS)
                .description("登录成功")
                .build().setInfo();
    }

    public static LoginStatusDto switchOrg(Long userId) {
        return LoginStatusDto.builder()
                .userId(userId)
                .status(LoginStatusEnum.SWITCH)
                .description("切换租户")
                .build().setInfo();
    }

    public static LoginStatusDto fail(Long userId, LoginStatusEnum status, String description) {
        return LoginStatusDto.builder()
                .userId(userId)
                .status(status).description(description)
                .build().setInfo();
    }

    public static LoginStatusDto fail(String username, LoginStatusEnum status, String description) {
        return LoginStatusDto.builder()
                .username(username)
                .status(status).description(description)
                .build().setInfo();
    }

    public static LoginStatusDto smsCodeError(String mobile, LoginStatusEnum status, String description) {
        return LoginStatusDto.builder()
                .status(status).description(description)
                .build().setInfo();
    }

    private LoginStatusDto setInfo() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return this;
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String tempUa = StrUtil.sub(request.getHeader("user-agent"), 0, 500);
        String tempIp = JakartaServletUtil.getClientIP(request);
        log.info("tempIp={}, ua={}", tempIp, tempUa);
        String tempLocation = isLocalHostIp(tempIp) ? "localhost" : AddressUtil.getRegion(tempIp);

        this.ua = tempUa;
        this.requestIp = tempIp;
        this.location = tempLocation;
        return this;
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
