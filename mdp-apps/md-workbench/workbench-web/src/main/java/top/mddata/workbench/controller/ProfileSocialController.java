package top.mddata.workbench.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.util.ContextUtil;
import top.mddata.workbench.dto.SocialCallbackDto;
import top.mddata.workbench.dto.SocialReauthDto;
import top.mddata.workbench.dto.SocialReauthUrlDto;
import top.mddata.workbench.dto.SocialTicketDto;
import top.mddata.workbench.dto.SocialTransferDto;
import top.mddata.workbench.dto.SocialUnbindDto;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.service.social.SocialAccountService;
import top.mddata.workbench.vo.LoginVo;
import top.mddata.workbench.vo.SocialBindingVo;
import top.mddata.workbench.vo.SocialReauthTicketVo;

import java.util.List;

/**
 * 个人中心第三方账号接口。
 *
 * <p>全部接口从认证上下文获取当前用户，不接收前端传入的用户 ID。</p>
 *
 * @author tangyh
 */
@RestController
@RequestMapping("/profile/social")
@RequiredArgsConstructor
@Tag(name = "个人中心第三方账号", description = "绑定、解绑与转移")
public class ProfileSocialController {

    private final SocialAccountService socialAccountService;

    @Operation(summary = "第三方账号绑定列表")
    @GetMapping("/list")
    public R<List<SocialBindingVo>> list() {
        return R.success(socialAccountService.list(ContextUtil.getUserId()));
    }

    @Operation(summary = "账号凭据二次验证")
    @PostMapping("/reauth")
    @RequestLog(value = "第三方账号二次验证", logType = RequestLog.LogType.OTHER,
            request = false, response = false, requestByError = false)
    public R<SocialReauthTicketVo> reauth(@Validated @RequestBody SocialReauthDto dto) {
        return R.success(socialAccountService.reauth(ContextUtil.getUserId(), dto));
    }

    @Operation(summary = "第三方重新授权地址")
    @PostMapping("/{provider}/reauthUrl")
    @RequestLog(value = "第三方账号重新授权", logType = RequestLog.LogType.OTHER,
            request = false, response = false, requestByError = false)
    public R<String> reauthUrl(@PathVariable String provider,
                               @Validated @RequestBody SocialReauthUrlDto dto) {
        return R.success(socialAccountService.reauthUrl(ContextUtil.getUserId(),
                SocialProviderEnum.fromCode(provider), dto));
    }

    @Operation(summary = "第三方重新授权回调")
    @PostMapping("/reauthByCallback")
    @RequestLog(value = "第三方账号重新授权回调", logType = RequestLog.LogType.OTHER,
            request = false, response = false, requestByError = false)
    public R<SocialReauthTicketVo> reauthByCallback(
            @Validated @RequestBody SocialCallbackDto callback) {
        return R.success(socialAccountService.reauthByCallback(ContextUtil.getUserId(), callback));
    }

    @Operation(summary = "第三方账号绑定授权地址")
    @PostMapping("/{provider}/bindUrl")
    @RequestLog(value = "第三方账号绑定授权", logType = RequestLog.LogType.OTHER,
            request = false, response = false, requestByError = false)
    public R<String> bindUrl(@PathVariable String provider,
                             @Validated @RequestBody SocialTicketDto dto) {
        return R.success(socialAccountService.bindUrl(ContextUtil.getUserId(),
                SocialProviderEnum.fromCode(provider), dto));
    }

    @Operation(summary = "第三方账号绑定回调")
    @PostMapping("/bind")
    @RequestLog(value = "第三方账号绑定回调", logType = RequestLog.LogType.OTHER,
            request = false, response = false, requestByError = false)
    public R<Boolean> bind(@Validated @RequestBody SocialCallbackDto callback) {
        socialAccountService.bind(ContextUtil.getUserId(), callback);
        return R.success(true);
    }

    @Operation(summary = "第三方账号解绑")
    @PostMapping("/unbind")
    @RequestLog(value = "第三方账号解绑", logType = RequestLog.LogType.UPDATE,
            request = false, response = false, requestByError = false)
    public R<Boolean> unbind(@Validated @RequestBody SocialUnbindDto dto) {
        socialAccountService.unbind(ContextUtil.getUserId(), dto);
        return R.success(true);
    }

    @Operation(summary = "误注册后跨账号安全转移")
    @PostMapping("/transfer")
    @RequestLog(value = "第三方账号跨账号转移", logType = RequestLog.LogType.UPDATE,
            request = false, response = false, requestByError = false)
    public R<LoginVo> transfer(@Validated @RequestBody SocialTransferDto dto) {
        return R.success(socialAccountService.transfer(ContextUtil.getUserId(), dto));
    }
}