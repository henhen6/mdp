package top.mddata.open.controller.oauth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.open.entity.admin.OauthOpenid;
import top.mddata.open.service.oauth.OauthOpenidService;
import top.mddata.open.vo.admin.OauthOpenidVo;

/**
 * openid 控制层。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@RestController
@Validated
@Tag(name = "openid")
@RequestMapping("/admin/oauthOpenid")
@RequiredArgsConstructor
public class OauthOpenidController extends SuperController<OauthOpenidService, OauthOpenid> {

    @GetMapping("/getByAppIdAndUserId")
    @Operation(summary = "根据应用标识查询用户的openid", description = "根据应用标识查询用户的openid")
    @RequestLog(value = "'根据应用标识查询用户的openid:' + #subjectId + '---' + #userId", logType = RequestLog.LogType.QUERY)
    public R<OauthOpenidVo> getByAppKeyAndUserId(@RequestParam String appKey, @RequestParam Long userId) {
        return R.success(superService.getByAppKeyAndUserId(appKey, userId));
    }
}
