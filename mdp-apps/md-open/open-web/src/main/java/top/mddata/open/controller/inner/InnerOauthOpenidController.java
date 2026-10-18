package top.mddata.open.controller.inner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.open.service.oauth.OauthOpenidService;
import top.mddata.open.vo.admin.OauthOpenidVo;

/**
 * 内部-openid 入口：仅供平台内部服务经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-openid")
@RequestMapping("/inner/oauthOpenid")
@RequiredArgsConstructor
public class InnerOauthOpenidController {

    private final OauthOpenidService oauthOpenidService;

    /**
     * 根据应用id和用户id 查询openid
     *
     * @param appKey 应用标识
     * @param userId 用户id
     * @return openid
     */
    @Operation(hidden = true)
    @GetMapping("/getByAppIdAndUserId")
    public R<OauthOpenidVo> getByAppKeyAndUserId(@RequestParam String appKey, @RequestParam Long userId) {
        return R.success(oauthOpenidService.getByAppKeyAndUserId(appKey, userId));
    }
}
