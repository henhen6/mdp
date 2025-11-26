package top.mddata.workbench.sso.controller;

import cn.dev33.satoken.sso.processor.SaSsoServerProcessor;
import cn.dev33.satoken.sso.template.SaSsoServerUtil;
import cn.dev33.satoken.sso.util.SaSsoConsts;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.dev33.satoken.util.SaResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.open.manage.facade.AppFacade;
import top.mddata.open.admin.vo.AppVo;

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
    public R<String> getRedirectUrl(String client, String mode, String redirect) {
        // 前判断用户是否登录，没有登录时，前端根据401状态码强制用户登录
        if (!StpUtil.isLogin()) {
            return R.fail(401, JWT_TOKEN_EXPIRED.getMsg());
        }

        // 判断应用状态
        long loginId = StpUtil.getLoginIdAsLong();
        R<AppVo> appResult = appFacade.getAppByAppKey(client);
        if (appResult.getIsSuccess()) {
            AppVo opApplication = appResult.getData();

            if (opApplication == null) {
                return R.fail("无效的AppId：" + client);
            }
//             判断应用状态
            if (!opApplication.getState()) {
                return R.fail("当前应用 [" + opApplication.getName() + "] 已被禁用，无法使用");
            }

//             // 检查用户是否拥有此应用
//            if (!check(loginId, opApplication.getId())) {
//                return R.fail("当前账号暂无权限登入此应用，请联系管理员授权");
//            }
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
//        log.save(loginId, sysClient.getId(), redirect);

        return result;
    }

    /**
     * 接收单点登录的客户端接口调用，根据传递的 消息类型 决定处理逻辑
     * 消息类型：校验 ticket
     * 消息类型：单点注销
     * 消息类型：单点注销回调
     */
    @Operation(summary = "接收单点登录的客户端接口调用", description = "接收单点登录的客户端接口调用，根据传递的 消息类型 决定处理逻辑")
    @GetMapping("/anyUser/sso/pushS")
    public Object push() {
        try {
            return SaSsoServerProcessor.instance.ssoPushS();
        } catch (Exception e) {
            log.error("pushS", e);
            return SaResult.error(e.getMessage());
        }
    }

    /**
     * 全局注销登录，通知所有应用退出
     */
    @Operation(summary = "全端退出", description = "全端退出")
    @RequestMapping("/anyUser/sso/signout")
    public R<Boolean> ssoSignout() {
        try {
            SaResult result = (SaResult) SaSsoServerProcessor.instance.ssoSignout();
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


    /** 退出登录当前应用 */
    @PostMapping("/anyUser/sso/logout")
    public R<Boolean> logout() {
        try {
            StpUtil.logout();
        } catch (Exception e) {
            log.debug("token已经过期，无需退出", e);
        }
        return R.success(true);
    }


}
