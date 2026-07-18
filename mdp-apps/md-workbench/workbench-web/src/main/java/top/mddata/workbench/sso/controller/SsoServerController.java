package top.mddata.workbench.sso.controller;

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
import top.mddata.open.facade.admin.AppFacade;
import top.mddata.open.vo.admin.AppVo;
import top.mddata.workbench.dto.LoginLogDto;
import top.mddata.workbench.dto.LoginRedirectUrlDto;
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

        // 记录登录日志
        if (SaFoxUtil.isNotEmpty(redirect) && redirect.contains("?")) {
            int index = redirect.indexOf("?");
            redirect = redirect.substring(0, index);
        }

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        // 发送登录成功事件
        LoginLogDto dto = LoginLogDto.success(param.getAuthType(), param.getDeviceInfo(), null, "登录成功", JSON.toJSONString(tokenInfo));
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
            SaResult result = (SaResult) SaSsoServerProcessor.getInstance().ssoSignout();
            if (result.getCode() == SaResult.CODE_SUCCESS) {
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

            R<AppVo> appResult = appFacade.getAppByAppKey(client);
            if (appResult.getIsSuccess() && appResult.getData() != null) {
                AppVo appVo = appResult.getData();
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

            return SaSsoServerProcessor.getInstance().ssoPushS();
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
            StpUtil.logout();
        } catch (Exception e) {
            log.debug("token已经过期，无需退出", e);
        }
        return R.success(true);
    }


}
