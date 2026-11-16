package top.mddata.workbench.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.workbench.dto.FingerprintLoginVerifyDto;
import top.mddata.workbench.service.FingerprintService;
import top.mddata.workbench.vo.FingerprintLoginOptionsVo;
import top.mddata.workbench.vo.LoginVo;

/**
 * 登录页指纹免密登录
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Slf4j
@RestController
@RequestMapping("/anyUser/fingerprint")
@AllArgsConstructor
@Tag(name = "指纹登录", description = "登录页指纹免密登录")
public class FingerprintLoginController {
    private final FingerprintService fingerprintService;

    @Operation(summary = "获取指纹登录挑战", description = "免登录，生成指纹登录（WebAuthn 认证）挑战")
    @GetMapping("/login/options")
    public R<FingerprintLoginOptionsVo> loginOptions() {
        return R.success(fingerprintService.loginOptions());
    }

    @Operation(summary = "指纹登录验证", description = "免登录，校验 WebAuthn 认证响应并完成登录")
    @PostMapping("/login/verify")
    public R<LoginVo> loginVerify(@Validated @RequestBody FingerprintLoginVerifyDto dto) {
        return fingerprintService.loginVerify(dto);
    }
}
