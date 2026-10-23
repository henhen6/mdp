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
import top.mddata.open.service.app.AppKeysService;
import top.mddata.open.service.app.AppService;
import top.mddata.open.vo.admin.AppKeysVo;
import top.mddata.open.vo.admin.AppVo;

import java.util.List;

/**
 * 内部-应用 入口：仅供平台内部服务经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-应用")
@RequestMapping("/inner/app")
@RequiredArgsConstructor
public class InnerAppController {

    private final AppService appService;
    private final AppKeysService appKeysService;

    /**
     * 查询需要 接收事件推送的应用
     *
     * @return 应用
     */
    @Operation(hidden = true)
    @GetMapping("/listNeedPushApp")
    public R<List<AppVo>> listNeedPushApp() {
        return R.success(appService.listNeedPushApp());
    }

    /**
     * 校验用户是否拥有该应用
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @return 是否拥有该应用
     */
    @Operation(hidden = true)
    @GetMapping("/checkAppByUserId")
    public R<Boolean> checkAppByUserId(@RequestParam Long userId, @RequestParam Long appId) {
        return R.success(appService.checkAppByUserId(userId, appId));
    }

    /**
     * 根据appKey查询应用
     *
     * @param appKey 应用标识
     * @return 应用
     */
    @Operation(hidden = true)
    @GetMapping("/getAppByAppKey")
    public R<AppVo> getAppByAppKey(@RequestParam String appKey) {
        return R.success(appService.getAppByAppKey(appKey));
    }

    /**
     * 查询角色已分配的应用（按权重降序）
     *
     * @param roleId 角色ID
     * @return 应用列表
     */
    @Operation(hidden = true)
    @GetMapping("/listByRoleId")
    public R<List<AppVo>> listByRoleId(@RequestParam Long roleId) {
        return R.success(appService.listByRoleId(roleId));
    }

    /**
     * 根据appKey查询应用秘钥（含通知加密配置）
     *
     * @param appKey 应用标识
     * @return 秘钥VO
     */
    @Operation(hidden = true)
    @GetMapping("/getAppKeysByAppKey")
    public R<AppKeysVo> getAppKeysByAppKey(@RequestParam String appKey) {
        return R.success(appKeysService.getByAppKey(appKey));
    }
}
