package top.mddata.workbench.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.workbench.dto.SocialBindExistingDto;
import top.mddata.workbench.dto.SocialRegisterDto;
import top.mddata.workbench.service.social.SocialOnboardingService;
import top.mddata.workbench.vo.LoginVo;

/**
 * 第三方登录首次选择接口（无需登录即可访问）。
 *
 * @author tangyh
 */
@RestController
@RequestMapping("/anyUser/social")
@AllArgsConstructor
@Tag(name = "第三方登录首次选择", description = "绑定已有账号或创建新账号")
public class SocialOnboardingController {

    private final SocialOnboardingService socialOnboardingService;

    @Operation(summary = "绑定已有账号", description = "验证已有MDP账号凭据后绑定第三方身份并登录")
    @PostMapping("/bindExisting")
    @RequestLog(value = "第三方身份绑定已有账号", logType = RequestLog.LogType.OTHER,
            request = false, response = false, requestByError = false)
    public R<LoginVo> bindExisting(@Validated @RequestBody SocialBindExistingDto dto) {
        return R.success(socialOnboardingService.bindExisting(dto));
    }

    @Operation(summary = "创建新账号", description = "创建新MDP账号并绑定第三方身份后自动登录")
    @PostMapping("/register")
    @RequestLog(value = "第三方身份创建新账号", logType = RequestLog.LogType.OTHER,
            request = false, response = false, requestByError = false)
    public R<LoginVo> register(@Validated @RequestBody SocialRegisterDto dto) {
        return R.success(socialOnboardingService.register(dto));
    }
}
