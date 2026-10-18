package top.mddata.open.controller.inner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.open.dto.admin.OauthLogDto;
import top.mddata.open.service.oauth.OauthLogService;

/**
 * 内部-授权记录 入口：仅供平台内部服务（workbench 登录授权等）经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-授权记录")
@RequestMapping("/inner/oauthLog")
@RequiredArgsConstructor
public class InnerOauthLogController {

    private final OauthLogService oauthLogService;

    /**
     * 保存授权记录
     *
     * @param dlt 记录
     * @return 记录ID
     */
    @Operation(hidden = true)
    @PostMapping("/save")
    public R<Long> save(@Validated @RequestBody OauthLogDto dlt) {
        return R.success(oauthLogService.saveDto(dlt).getId());
    }
}
