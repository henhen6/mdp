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
import top.mddata.open.service.oauth.OauthUnionidService;
import top.mddata.open.vo.admin.OauthUnionidVo;

/**
 * 内部-unionid 入口：仅供平台内部服务经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-unionid")
@RequestMapping("/inner/oauthUnionid")
@RequiredArgsConstructor
public class InnerOauthUnionidController {

    private final OauthUnionidService oauthUnionidService;

    /**
     * 根据主体id和用户id 查询union
     *
     * @param subjectId 主体id
     * @param userId    用户id
     * @return union
     */
    @Operation(hidden = true)
    @GetMapping("/getBySubjectIdAndUserId")
    public R<OauthUnionidVo> getBySubjectIdAndUserId(@RequestParam Long subjectId, @RequestParam Long userId) {
        return R.success(oauthUnionidService.getBySubjectIdAndUserId(subjectId, userId));
    }
}
