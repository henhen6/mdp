package top.mddata.workbench.sso.controller;

import cn.dev33.satoken.sso.config.SaSsoClientConfig;
import cn.dev33.satoken.sso.model.SaCheckTicketResult;
import cn.dev33.satoken.sso.processor.SaSsoClientProcessor;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSON;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.open.facade.admin.AppFacade;
import top.mddata.open.vo.admin.AppVo;
import top.mddata.workbench.dto.LoginLogDto;
import top.mddata.workbench.event.LoginEvent;
import top.mddata.workbench.service.AuthService;

/**
 * 单点登录 客户端接口
 *
 * @author henhen6
 * @date 2025年11月06日22:55:21
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping()
@Tag(name = "单点登录客户端")
public class SsoClientController {

    private final AuthService authService;
    private final AppFacade appFacade;

    /**
     * 获取SSO服务端登录地址
     *
     * 支持一个后端接口同时兼容多个Client端的情况
     *
     * @param clientLoginUrl Client端登录地址
     * @param clientId 应用ID
     * @param ssoUrl 直接获取单点登录授权地址
     * @return SSO服务端登录地址
     */
    @Operation(summary = "获取SSO服务端登录地址", description = "获取SSO服务端登录地址")
    @GetMapping("/anyUser/client/getSsoAuthUrl")
    public R<String> getSsoAuthUrl(Boolean ssoUrl, String clientLoginUrl, String clientId) {
        log.info("获取SSO服务端登录地址: ssoUrl={} clientLoginUrl={}, clientId={}", ssoUrl, clientLoginUrl, clientId);
        String serverAuthUrl = buildServerAuthUrl(ssoUrl, clientLoginUrl, clientId);
        return R.success(serverAuthUrl);
    }


    /**
     * 构建URL：Server端 单点登录授权地址，
     *
     * @param clientLoginUrl Client端登录地址
     * @param clientId 应用ID
     * @param ssoUrl 直接获取单点登录授权地址
     * @return SSO-Server端-认证地址
     */
    private String buildServerAuthUrl(Boolean ssoUrl, String clientLoginUrl, String clientId) {
        SaSsoClientConfig ssoConfig = SaSsoClientProcessor.getInstance().getSsoClientTemplate().getClientConfig(clientId);

        // 服务端认证地址
        String serverUrl = ssoConfig.splicingAuthUrl();
        if (ssoUrl != null && ssoUrl) {
            return serverUrl;
        }

        if (StrUtil.isEmpty(clientId)) {
            // 拼接客户端标识
            String client = SaSsoClientProcessor.getInstance().getSsoClientTemplate().getClient();
            if (SaFoxUtil.isNotEmpty(client)) {
                serverUrl = SaFoxUtil.joinParam(serverUrl, SaSsoClientProcessor.getInstance().getSsoClientTemplate().getParamName().getClient(), client);
            }
        } else {
            serverUrl = SaFoxUtil.joinParam(serverUrl, SaSsoClientProcessor.getInstance().getSsoClientTemplate().getParamName().getClient(), clientId);
        }

        // 返回
        return SaFoxUtil.joinParam(serverUrl, SaSsoClientProcessor.getInstance().getSsoClientTemplate().getParamName().getRedirect(), clientLoginUrl);
    }

    /**
     * 根据ticket获取token
     * <p>
     * 该接口会根据is-http判断是否调用 center-server 的pushS接口
     *
     * @param ticket ticket
     * @param appKey 应用Key，兼作 SSO clientId 用于校验 ticket 归属（前端固定传入）
     * @return token
     */
    @Operation(summary = "客户端根据ticket获取token", description = "校验ticket有限性，并返回token")
    @GetMapping("/anyUser/client/doLoginByTicket")
    @RequestLog(value = "SSO客户端根据ticket获取token", logType = RequestLog.LogType.QUERY)
    public R<String> doLoginByTicket(String ticket, String appKey) {
        SaCheckTicketResult ctr = SaSsoClientProcessor.getInstance().checkTicket(appKey, ticket);
        return R.success(authService.loginByTicket(ctr.getLoginId(), ctr.getRemainTokenTimeout(), ctr.getDeviceId()));
    }


    /**
     * 全端退出
     */
    @Operation(summary = "客户端-全端退出", description = "客户端-全端退出")
    @RequestMapping("/anyUser/client/signout")
    public Object ssoSignout(@RequestParam(required = false) String clientId) {
        try {
            SaResult result = (SaResult) SaSsoClientProcessor.getInstance().ssoLogout(clientId);
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
     * 退出登录
     */
    @Operation(summary = "客户端-退出当前应用", description = "客户端-退出当前应用")
    @PostMapping("/anyUser/client/logout")
    @RequestLog(value = "SSO客户端退出当前应用", logType = RequestLog.LogType.DELETE)
    public R<Boolean> logout(@RequestParam(required = false) String clientId) {
        try {
            // 退出前取当前会话信息，退出后无法再获取
            Object loginId = StpUtil.getLoginIdDefaultNull();
            String tokenInfo = loginId == null ? null : JSON.toJSONString(StpUtil.getTokenInfo());

            StpUtil.logout();
            // token 已过期时 logout 抛异常进入 catch，不产生真实退出动作，不记录日志
            publishLogoutLog(loginId, tokenInfo, clientId);
        } catch (Exception e) {
            log.debug("token已经过期，无需退出", e);
        }
        return R.success(true);
    }

    /**
     * 记录客户端退出日志（与登录日志共用 LoginEvent 通道）。
     * <p>
     * 仅记录"退出当前应用"；全端注销统一由 SSO 服务端 pushS 收到 signout 消息时记录，此处不重复
     *
     * @param loginId   用户id，为空时不记录
     * @param tokenInfo 令牌信息
     * @param clientId  发起方应用标识，为空时取当前默认 client 配置
     */
    private void publishLogoutLog(Object loginId, String tokenInfo, String clientId) {
        if (loginId == null) {
            return;
        }
        String appKey = StrUtil.isNotEmpty(clientId)
                ? clientId
                : SaSsoClientProcessor.getInstance().getSsoClientTemplate().getClient();

        LoginLogDto dto = LoginLogDto.logout(null, "退出当前应用", tokenInfo);
        dto.setUserId(Convert.toLong(loginId));
        dto.setAppKey(appKey);

        R<AppVo> appResult = appFacade.getAppByAppKey(appKey);
        if (appResult.getIsSuccess() && appResult.getData() != null) {
            dto.setAppName(appResult.getData().getName());
        }
        SpringUtil.publishEvent(new LoginEvent(dto));
    }


    /**
     * 接收单点登录的服务端接口调用，根据传递的 消息类型 决定处理逻辑
     * 参考： {link https://sa-token.cc/doc.html#/sso/message-push}
     * 消息类型：logoutCall（单点注销回调）
     */
    @Operation(summary = "接收单点登录的服务端接口调用", description = "接收单点登录的服务端接口调用，根据传递的 消息类型 决定处理逻辑")
    @GetMapping("/anyUser/client/pushC")
    @RequestLog(value = "SSO客户端接收服务端推送", logType = RequestLog.LogType.OTHER)
    public Object push(String clientId) {
        try {
            return SaSsoClientProcessor.getInstance().ssoPushC(clientId);
        } catch (Exception e) {
            log.error("pushC", e);
            return SaResult.error(e.getMessage());
        }
    }
}