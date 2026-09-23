package top.mddata.workbench.sso.controller;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.sso.message.SaSsoMessage;
import cn.dev33.satoken.sso.name.ParamName;
import cn.dev33.satoken.sso.processor.SaSsoServerProcessor;
import cn.dev33.satoken.sso.template.SaSsoServerUtil;
import cn.dev33.satoken.sso.util.SaSsoConsts;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSON;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.utils.IpUtil;
import top.mddata.common.constant.DefValConstants;
import top.mddata.open.facade.admin.AppFacade;
import top.mddata.open.vo.admin.AppVo;
import top.mddata.workbench.dto.LoginLogDto;
import top.mddata.workbench.dto.LoginRedirectUrlDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.event.LoginEvent;

import java.util.List;

import static top.mddata.base.exception.code.ExceptionCode.JWT_TOKEN_EXPIRED;

/**
 * 单点登录 服务端Controller
 *
 * 此模块只处理SSO路由跳转，具体的登录接口在 AuthController
 *
 * @author henhen6
 * @since 2025/7/6 15:59
 */
@Slf4j
@RestController
@RequestMapping
@AllArgsConstructor
@Tag(name = "单点登录服务端", description = "此模块只处理SSO路由跳转，具体的登录接口在 AuthController")
public class SsoServerController {

    @Autowired
    private AppFacade appFacade;

    @Operation(summary = "根据客户端编码获取客户端的重定向地址", description = "根据客户端编码获取客户端的重定向地址")
    @PostMapping("/anyUser/sso/getRedirectUrl")
    public R<String> getRedirectUrl(LoginRedirectUrlDto param) {
        String client = param.getClient();
        String mode = param.getMode();
        String redirect = param.getRedirect();
        // 前判断用户是否登录，没有登录时，前端根据401状态码强制用户登录
        if (!StpUtil.isLogin()) {
            return R.fail(401, JWT_TOKEN_EXPIRED.getMsg());
        }

        // 判断应用状态
        long loginId = StpUtil.getLoginIdAsLong();
        R<AppVo> appResult = appFacade.getAppByAppKey(client);
        AppVo app = null;
        if (appResult.getIsSuccess()) {
            app = appResult.getData();

            if (app == null) {
                return R.fail("无效的AppId：" + client);
            }
//             判断应用状态
            if (!app.getState()) {
                return R.fail("当前应用 [" + app.getName() + "] 已被禁用，无法使用");
            }

            R<Boolean> checkResult = appFacade.checkAppByUserId(loginId, app.getId());
            if (checkResult.getIsSuccess() && !checkResult.getData()) {
                return R.fail("当前账号暂无权限登入此应用，请联系管理员授权");
            }
        }

        // 构建重定向到客户端的地址
        R<String> result;
        if (SaSsoConsts.MODE_SIMPLE.equals(mode)) {
            // 模式一，校验一下 redirect 是否合法，然后原样返回 redirect
            SaSsoServerUtil.checkRedirectUrl(client, SaFoxUtil.decoderUrl(redirect));
            result = R.success(redirect);
        } else {
            // 模式二或模式三，为 redirect 追加 ticket 参数，然后返回 （该ticket 是一次性的，客户端需要在规定时间内使用ticket换token，方可视为登录成功）
            String redirectUrl = SaSsoServerUtil.buildRedirectUrl(client, redirect, loginId, StpUtil.getTokenValue());
            log.info("重定向地址: {}", redirectUrl);
            result = R.success(redirectUrl);
        }

        // 记录登录日志：第三方应用换 ticket 不经过本系统客户端接口，登录日志统一在服务端签发 ticket 时记录
        if (SaFoxUtil.isNotEmpty(redirect) && redirect.contains("?")) {
            int index = redirect.indexOf("?");
            redirect = redirect.substring(0, index);
        }

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        // 发送登录成功事件
        LoginLogDto dto = LoginLogDto.success(AuthTypeEnum.TICKET, null, null, "免密自动登录", JSON.toJSONString(tokenInfo));
        dto.setUserId(Convert.toLong(tokenInfo.getLoginId()));
        if (app != null) {
            dto.setAppKey(app.getAppKey());
            dto.setAppName(app.getName());
        }
        dto.setAppRedirect(redirect);
        SpringUtil.publishEvent(new LoginEvent(dto));
        return result;
    }

    /**
     * 全局注销登录，通知所有应用退出
     */
    @Operation(summary = "服务端-全端退出", description = "服务端-全端退出")
    @RequestMapping("/anyUser/sso/signout")
    @RequestLog(value = "SSO服务端全端退出", logType = RequestLog.LogType.OTHER)
    public R<Boolean> ssoSignout() {
        try {
            // 注销前取当前会话信息，注销后无法再获取
            Object loginId = StpUtil.getLoginIdDefaultNull();
            String tokenInfo = loginId == null ? null : JSON.toJSONString(StpUtil.getTokenInfo());

            SaResult result = (SaResult) SaSsoServerProcessor.getInstance().ssoSignout();
            if (result.getCode() == SaResult.CODE_SUCCESS) {
                publishSsoLog(true, loginId, tokenInfo, DefValConstants.WORKBENCH_APP_KEY, DefValConstants.WORKBENCH_APP_NAME);
                return R.success();
            } else {
                return R.result(result.getCode(), false, result.getMsg());
            }
        } catch (Exception e) {
            log.error("signout", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 接收单点登录的客户端接口调用，根据传递的 消息类型 决定处理逻辑
     * 消息类型：校验 ticket
     * 消息类型：单点注销
     */
    @Operation(summary = "接收单点登录的客户端接口调用", description = "接收单点登录的客户端接口调用，根据传递的 消息类型 决定处理逻辑")
    @GetMapping("/anyUser/sso/pushS")
    @RequestLog(value = "SSO服务端接收客户端推送", logType = RequestLog.LogType.OTHER)
    public Object push(HttpServletRequest request) {
        try {
            /*
            注意：
            内置应用：部署在同一台服务器，配置为127.0.0.1或者服务器内网ip即可（如 单体版 boot-server 、微服务版 workbench-server ）
            第三方应用：则配置为服务器外网ip（如：若依sso、 若依oauth2）
             */
            String clientIp = IpUtil.normalizeLoopback(JakartaServletUtil.getClientIP(request));
            String client = SaSsoServerProcessor.getInstance().getClient();
            log.info("接收到客户端:[{}]， 应用:[{}] 的请求", clientIp, client);

            String appName = null;
            R<AppVo> appResult = appFacade.getAppByAppKey(client);
            if (appResult.getIsSuccess() && appResult.getData() != null) {
                AppVo appVo = appResult.getData();
                appName = appVo.getName();
                String allowIp = appVo.getAllowIp();
                if (StrUtil.isNotEmpty(allowIp)) {
                    List<String> allowIpList = SaFoxUtil.convertStringToList(allowIp);
                    if (CollUtil.isNotEmpty(allowIpList)) {
                        boolean matched = allowIpList.stream().anyMatch(ip -> IpUtil.matchIp(ip, clientIp));
                        if (!matched) {
                            log.warn("客户端IP:[{}]不在白名单中，应用:[{}], 允许IP:[{}]", clientIp, client, allowIp);
                            return SaResult.error("IP " + clientIp + " 不在允许访问的白名单中");
                        }
                    }
                }
            }

            // 注销消息（内置 console/open 与第三方应用发起的全端注销都会推送到此）统一在此记录，
            // 发起方客户端不再记录，避免重复
            ParamName paramName = SaSsoServerProcessor.getInstance().getSsoServerTemplate().getParamName();
            SaSsoMessage message = new SaSsoMessage(SaHolder.getRequest().getParamMap());
            boolean isSignout = SaSsoConsts.MESSAGE_SIGNOUT.equals(message.getType());

            Object result = SaSsoServerProcessor.getInstance().ssoPushS();
            if (isSignout && result instanceof SaResult saResult && saResult.getCode() == SaResult.CODE_SUCCESS) {
                publishSsoLog(true, message.get(paramName.getLoginId()), null, client, appName);
            }
            return result;
        } catch (Exception e) {
            log.error("pushS", e);
            return SaResult.error(e.getMessage());
        }
    }


    /** 退出登录当前应用 */
    @PostMapping("/anyUser/sso/logout")
    @Operation(summary = "服务端-退出当前应用", description = "服务端-退出当前应用")
    @RequestLog(value = "SSO服务端退出当前应用", logType = RequestLog.LogType.DELETE)
    public R<Boolean> logout() {
        try {
            // 退出前取当前会话信息，退出后无法再获取
            Object loginId = StpUtil.getLoginIdDefaultNull();
            String tokenInfo = loginId == null ? null : JSON.toJSONString(StpUtil.getTokenInfo());

            StpUtil.logout();
            // token 已过期时 logout 抛异常进入 catch，不产生真实退出动作，不记录日志
            publishSsoLog(false, loginId, tokenInfo, DefValConstants.WORKBENCH_APP_KEY, DefValConstants.WORKBENCH_APP_NAME);
        } catch (Exception e) {
            log.debug("token已经过期，无需退出", e);
        }
        return R.success(true);
    }

    /**
     * 记录 SSO 退出/注销日志（与登录日志共用 LoginEvent 通道）。
     * <p>
     * 注销事件只在发起处记录一次：工作台发起在 signout 记录；
     * 客户端（内置 console/open 及第三方应用）发起统一在 pushS 收到 signout 消息时记录
     *
     * @param signout   true=注销（全端），false=退出（当前应用）
     * @param loginId   用户id，为空时不记录
     * @param tokenInfo 令牌信息
     * @param appKey    发起方应用 appKey
     * @param appName   发起方应用名称
     */
    private void publishSsoLog(boolean signout, Object loginId, String tokenInfo, String appKey, String appName) {
        if (loginId == null) {
            return;
        }
        LoginLogDto dto = signout
                ? LoginLogDto.signout(null, "全端注销", tokenInfo)
                : LoginLogDto.logout(null, "退出当前应用", tokenInfo);
        dto.setUserId(Convert.toLong(loginId));
        dto.setAppKey(appKey);
        dto.setAppName(appName);
        SpringUtil.publishEvent(new LoginEvent(dto));
    }

}
