package top.mddata.open.controller.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.open.service.oauth.ScopeGroupService;
import top.mddata.open.vo.admin.ScopeGroupDetailVo;
import top.mddata.open.vo.admin.ScopeGroupVo;

import java.util.List;

/**
 * 权限组 控制层（开发者端）。
 *
 * @author henhen6
 */
@RestController
@Validated
@Tag(name = "权限组-开发者端")
@RequestMapping("/client/scopeGroup")
@RequiredArgsConstructor
public class ClientScopeGroupController {

    private final ScopeGroupService scopeGroupService;
    private final ClientAppHelper clientAppHelper;

    /**
     * 查询应用可申请的权限组
     *
     * @param appId 应用ID
     * @return 可申请权限组
     */
    @GetMapping("/listAppliable")
    @Operation(summary = "查询可申请权限组", description = "启用且未拥有且无在审申请")
    public R<List<ScopeGroupVo>> listAppliable(@RequestParam Long appId) {
        clientAppHelper.checkOwner(appId);
        return R.success(scopeGroupService.listAppliable(appId));
    }

    /**
     * 查询应用已拥有的权限组
     *
     * @param appId 应用ID
     * @return 已拥有权限组
     */
    @GetMapping("/listOwned")
    @Operation(summary = "查询已拥有权限组", description = "查询应用已拥有的权限组")
    public R<List<ScopeGroupVo>> listOwned(@RequestParam Long appId) {
        clientAppHelper.checkOwner(appId);
        return R.success(scopeGroupService.listOwned(appId));
    }

    /**
     * 查询权限组详情（组内开放接口与 oauth2 权限）
     *
     * @param id 权限组ID
     * @return 组详情
     */
    @GetMapping("/getGroupDetail")
    @Operation(summary = "查询权限组详情", description = "含组内开放接口与oauth2权限")
    public R<ScopeGroupDetailVo> getGroupDetail(@RequestParam Long id) {
        return R.success(scopeGroupService.getGroupDetail(id));
    }
}
