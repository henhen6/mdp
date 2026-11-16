package top.mddata.workbench.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.common.dto.IdDto;
import top.mddata.workbench.dto.FingerprintRegisterVerifyDto;
import top.mddata.workbench.dto.FingerprintRenameDto;
import top.mddata.workbench.service.FingerprintService;
import top.mddata.workbench.vo.FingerprintCredentialVo;
import top.mddata.workbench.vo.FingerprintRegisterOptionsVo;

import java.util.List;

/**
 * 安全设置-指纹凭证管理
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Slf4j
@RestController
@RequestMapping("/profile/fingerprint")
@AllArgsConstructor
@Tag(name = "指纹凭证管理", description = "安全设置-指纹登录")
public class FingerprintController {
    private final FingerprintService fingerprintService;

    @Operation(summary = "录入前验证当前密码", description = "录入指纹前验证当前登录密码")
    @PostMapping("/checkPassword")
    public R<Boolean> checkPassword(@RequestBody @Validated CheckPasswordDto dto) {
        return fingerprintService.checkPassword(dto.getPassword());
    }

    @Operation(summary = "获取录入挑战", description = "生成指纹录入（WebAuthn 注册）挑战")
    @PostMapping("/registration/options")
    public R<FingerprintRegisterOptionsVo> registrationOptions() {
        return R.success(fingerprintService.registrationOptions());
    }

    @Operation(summary = "验证录入结果并保存", description = "校验 WebAuthn 注册响应并保存凭证，需先通过 checkPassword")
    @RequestLog(value = "录入指纹", logType = RequestLog.LogType.ADD)
    @PostMapping("/registration/verify")
    public R<FingerprintCredentialVo> registrationVerify(
            @Validated @RequestBody FingerprintRegisterVerifyDto dto) {
        return R.success(fingerprintService.verifyRegistration(dto));
    }

    @Operation(summary = "我的指纹凭证列表", description = "查询当前登录用户已录入的指纹凭证")
    @GetMapping("/list")
    public R<List<FingerprintCredentialVo>> list() {
        return R.success(fingerprintService.listMine());
    }

    @Operation(summary = "修改设备名称", description = "修改本人指纹凭证的设备名称")
    @PostMapping("/rename")
    public R<Boolean> rename(@Validated @RequestBody FingerprintRenameDto dto) {
        return R.success(fingerprintService.rename(dto.getId(), dto));
    }

    @Operation(summary = "删除指纹凭证", description = "删除本人指纹凭证（逻辑删除）")
    @RequestLog(value = "删除指纹凭证", logType = RequestLog.LogType.DELETE)
    @PostMapping("/delete")
    public R<Boolean> delete(@Validated @RequestBody IdDto dto) {
        return R.success(fingerprintService.delete(dto.getId()));
    }

    /**
     * 验证密码入参
     */
    @Data
    public static class CheckPasswordDto {
        @NotBlank(message = "密码不能为空")
        private String password;
    }
}
