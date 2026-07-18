package top.mddata.workbench.sso.controller;

import cn.dev33.satoken.sso.config.SaSsoClientConfig;
import cn.dev33.satoken.sso.model.SaCheckTicketResult;
import cn.dev33.satoken.sso.processor.SaSsoClientProcessor;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;

/**
 * 单点登录 客户端接口
 *
 * @author henhen6
 * @date 2025年11月06日22:55:21
 */
@RestController
@Slf4j
@RequestMapping()
@Tag(name = "单点登录客户端")
public class SsoClientController {

    /**
     * 获取SSO服务端登录地址
     *
     * 支持一个后端接口同时兼容多个Client端的情况
     *
     * @param clientLoginUrl Client端登录地址
     * @param clientId 应用ID
     * @return SSO服务端登录地址
     */
    @Operation(summary = "获取SSO服务端登录地址", description = "获取SSO服务端登录地址")
    @GetMapping("/anyUser/client/getSsoAuthUrl")
    public R<String> getSsoAuthUrl(String clientLoginUrl, String clientId) {
        String serverAuthUrl = buildServerAuthUrl(clientLoginUrl, clientId);
        return R.success(serverAuthUrl);
    }


    /**
     * 构建URL：Server端 单点登录授权地址，
     *
     * @param clientLoginUrl Client端登录地址
     * @return SSO-Server端-认证地址
     */
    private String buildServerAuthUrl(String clientLoginUrl, String clientId) {
        SaSsoClientConfig ssoConfig = SaSsoClientProcessor.getInstance().getSsoClientTemplate().getClientConfig(clientId);

        // 服务端认证地址
        String serverUrl = ssoConfig.splicingAuthUrl();

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
     * @return token
     */
    @Operation(summary = "客户端根据ticket获取token", description = "校验ticket有限性，并返回token")
    @GetMapping("/anyUser/client/doLoginByTicket")
    @RequestLog(value = "SSO客户端根据ticket获取token", logType = RequestLog.LogType.QUERY)
    public R<String> doLoginByTicket(String clientId, String ticket) {
        SaCheckTicketResult ctr = SaSsoClientProcessor.getInstance().checkTicket(clientId, ticket);
        StpUtil.login(ctr.getLoginId(), new SaLoginParameter()
                        .setTimeout(ctr.getRemainTokenTimeout())
//                .setDeviceType(ctr.getDeviceType())
                        .setDeviceId(ctr.getDeviceId())
        );
        return R.success(StpUtil.getTokenValue());
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
    public R<Boolean> logout() {
        try {
            StpUtil.logout();
        } catch (Exception e) {
            log.debug("token已经过期，无需退出", e);
        }
        return R.success(true);
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