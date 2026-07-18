package top.mddata.workbench.controller;

import cn.dev33.satoken.sso.name.ParamName;
import cn.dev33.satoken.sso.processor.SaSsoServerProcessor;
import cn.dev33.satoken.util.SaFoxUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;

/**
 * 我的应用
 *
 * @author henhen
 * @since 2026/1/11 23:01
 */

@Slf4j
@RestController
@RequestMapping("/myApp")
@AllArgsConstructor
@Tag(name = "我的应用", description = "我的应用")
public class MyAppController {
    /**
     * 拼接应用自动登录地址
     *
     * @param serverUrl 单点登录服务端登录的地址
     * @param ssoAutoLoginUrl 自动登录地址
     * @param appKey 应用ID
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @GetMapping("/getAutoLoginUrl")
    @Operation(summary = "拼接应用自动登录地址", description = "拼接应用自动登录地址")
    @RequestLog(value = "拼接应用自动登录地址", logType = RequestLog.LogType.QUERY)
    public R<String> getAutoLoginUrl(String serverUrl, String ssoAutoLoginUrl, String appKey) {
        // TODO 还需测试 oauth2 方式的地址
        // 服务端认证地址
//        String serverUrl = "http://localhost:7700/#/auth/login";
        ParamName paramName = SaSsoServerProcessor.getInstance().getSsoServerTemplate().getParamName();

        // 拼接应用id
        serverUrl = SaFoxUtil.joinParam(serverUrl, paramName.getClient(), appKey);

        // 拼接 重定向到应用的 登录地址
        String url = SaFoxUtil.joinParam(serverUrl, paramName.getRedirect(), ssoAutoLoginUrl);
        return R.success(url);
    }
}
