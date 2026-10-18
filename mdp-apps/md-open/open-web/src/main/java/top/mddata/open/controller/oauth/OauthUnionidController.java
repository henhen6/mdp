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
import top.mddata.open.entity.admin.OauthUnionid;
import top.mddata.open.service.oauth.OauthUnionidService;
import top.mddata.open.vo.admin.OauthUnionidVo;

/**
 * unionid 控制层。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@RestController
@Validated
@Tag(name = "unionid")
@RequestMapping("/admin/oauthUnionid")
@RequiredArgsConstructor
public class OauthUnionidController extends SuperController<OauthUnionidService, OauthUnionid> {

    @GetMapping("/getBySubjectIdAndUserId")
    @Operation(summary = "根据主体id和用户id单体查询", description = "根据主体id和用户id单体查询")
    @RequestLog(value = "'根据主体id和用户id单体查询:' + #subjectId + '---' + #userId", logType = RequestLog.LogType.QUERY)
    public R<OauthUnionidVo> getBySubjectIdAndUserId(@RequestParam Long subjectId, @RequestParam Long userId) {
        return R.success(superService.getBySubjectIdAndUserId(subjectId, userId));
    }
}
