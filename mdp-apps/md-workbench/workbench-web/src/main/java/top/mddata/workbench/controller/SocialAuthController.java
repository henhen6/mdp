package top.mddata.workbench.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.workbench.dto.SocialCallbackDto;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.service.social.SocialLoginService;
import top.mddata.workbench.vo.SocialLoginResultVo;
import top.mddata.workbench.vo.SocialProviderVo;

import java.util.List;

/**
 * 第三方登录接口（无需登录即可访问）。
 *
 * @author tangyh
 */
@RestController
@RequestMapping("/anyUser/social")
@AllArgsConstructor
@Tag(name = "第三方登录", description = "微信、QQ、钉钉第三方登录接口")
public class SocialAuthController {

    private final SocialLoginService socialLoginService;

    @Operation(summary = "获取已启用的第三方登录平台", description = "前端只渲染已启用平台入口")
    @GetMapping("/providers")
    @RequestLog(value = "查询第三方登录平台", logType = RequestLog.LogType.QUERY)
    public R<List<SocialProviderVo>> providers() {
        return R.success(socialLoginService.providers());
    }

    @Operation(summary = "获取第三方授权地址", description = "创建登录用一次性 state 并返回官方授权地址")
    @GetMapping("/{provider}/loginUrl")
    @RequestLog(value = "获取第三方授权地址", logType = RequestLog.LogType.QUERY,
            response = false)
    public R<String> loginUrl(@PathVariable String provider) {
        return R.success(socialLoginService.loginUrl(SocialProviderEnum.fromCode(provider)));
    }

    @Operation(summary = "第三方账号登录", description = "已绑定直接登录，未绑定返回待选择票据")
    @PostMapping("/login")
    @RequestLog(value = "第三方账号登录", logType = RequestLog.LogType.OTHER,
            request = false, response = false, requestByError = false)
    public R<SocialLoginResultVo> login(@Validated @RequestBody SocialCallbackDto callback) {
        return R.success(socialLoginService.login(callback));
    }
}
