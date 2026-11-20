package top.mddata.workbench.controller.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.util.ContextUtil;
import top.mddata.workbench.dto.TwoFactorChannelDto;
import top.mddata.workbench.dto.TwoFactorDisableDto;
import top.mddata.workbench.dto.TwoFactorEnableDto;
import top.mddata.workbench.service.twofactor.TwoFactorAuthService;
import top.mddata.workbench.vo.TwoFactorManageSendVo;
import top.mddata.workbench.vo.TwoFactorStatusVo;

/**
 * 安全设置中的双因素认证管理。
 *
 * @author tangyh
 * @since 2026-11-16
 */
@RestController
@RequestMapping("/profile/twoFactor")
@AllArgsConstructor
@Tag(name = "双因素安全设置", description = "双因素状态、渠道与启停管理")
public class TwoFactorSecurityController {

    private final TwoFactorAuthService twoFactorAuthService;

    @Operation(summary = "查询双因素认证状态")
    @GetMapping("/status")
    public R<TwoFactorStatusVo> status() {
        return R.success(twoFactorAuthService.status(ContextUtil.getUserId()));
    }

    @Operation(summary = "发送开通/换渠道验证码")
    @PostMapping("/sendEnableCode")
    public R<TwoFactorManageSendVo> sendEnableCode(@Validated @RequestBody TwoFactorChannelDto dto) {
        return R.success(twoFactorAuthService.sendManageCode(ContextUtil.getUserId(), dto.getChannel()));
    }

    @Operation(summary = "开通双因素认证")
    @PostMapping("/enable")
    @RequestLog(value = "开通双因素认证", logType = RequestLog.LogType.UPDATE,
            request = false, response = false, requestByError = false)
    public R<Boolean> enable(@Validated @RequestBody TwoFactorEnableDto dto) {
        twoFactorAuthService.enable(ContextUtil.getUserId(), dto);
        return R.success();
    }

    @Operation(summary = "关闭双因素认证")
    @PostMapping("/disable")
    @RequestLog(value = "关闭双因素认证", logType = RequestLog.LogType.UPDATE,
            request = false, response = false, requestByError = false)
    public R<Boolean> disable(@Validated @RequestBody TwoFactorDisableDto dto) {
        twoFactorAuthService.disable(ContextUtil.getUserId(), dto);
        return R.success();
    }

    @Operation(summary = "更换验证渠道")
    @PostMapping("/changeChannel")
    @RequestLog(value = "更换双因素认证渠道", logType = RequestLog.LogType.UPDATE,
            request = false, response = false, requestByError = false)
    public R<Boolean> changeChannel(@Validated @RequestBody TwoFactorEnableDto dto) {
        twoFactorAuthService.changeChannel(ContextUtil.getUserId(), dto);
        return R.success();
    }
}
