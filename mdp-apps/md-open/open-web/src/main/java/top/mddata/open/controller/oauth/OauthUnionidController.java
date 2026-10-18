package top.mddata.open.controller.oauth;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.open.entity.admin.OauthUnionid;
import top.mddata.open.service.oauth.OauthUnionidService;

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
}
