package top.mddata.open.controller.inner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.open.service.oauth.OauthScopeService;
import top.mddata.open.vo.admin.OauthScopeVo;

import java.util.List;

/**
 * 内部-oauth2权限 入口：仅供平台内部服务经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-oauth2权限")
@RequestMapping("/inner/oauthScope")
@RequiredArgsConstructor
public class InnerOauthScopeController {

    private final OauthScopeService oauthScopeService;

    /**
     * 根据应用id查询应用拥有的权限
     *
     * @param appId 应用id
     * @return 权限
     */
    @Operation(hidden = true)
    @GetMapping("/listByAppId")
    public R<List<OauthScopeVo>> listByAppId(@RequestParam Long appId) {
        return R.success(oauthScopeService.listByAppId(appId));
    }

    /**
     * 根据权限编码查询 应用权限
     *
     * @param scopes 权限编码
     * @return 应用权限
     */
    @Operation(hidden = true)
    @PostMapping("/getScopeListByCode")
    public R<List<OauthScopeVo>> getScopeListByCode(@RequestBody List<String> scopes) {
        return R.success(oauthScopeService.getScopeListByCode(scopes));
    }
}
