package top.mddata.open.facade.admin.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;
import top.mddata.open.vo.admin.AppVo;

import java.util.List;

/**
 * 应用接口
 *
 * @author henhen6
 * @since 2025/8/12 11:24
 */
@FeignClient(name = AppConstants.OPEN_SERVER, path = "/admin/app")
public interface AppApi {
    /**
     * 查询需要 接收事件推送的应用
     *
     * @return 应用
     */
    @GetMapping("/listNeedPushApp")
    R<List<AppVo>> listNeedPushApp();

    /**
     * 校验用户是否拥有该应用
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @return 是否拥有该应用
     */
    @GetMapping("/checkAppByUserId")
    R<Boolean> checkAppByUserId(@RequestParam Long userId, @RequestParam Long appId);

    /**
     * 根据id查询应用
     *
     * @param appKey 应用标识
     * @return 应用
     */
    @GetMapping("/getAppByAppKey")
    R<AppVo> getAppByAppKey(@RequestParam String appKey);

    /**
     * 查询角色已分配的应用（按权重降序）
     *
     * @param roleId 角色ID
     * @return 应用列表
     */
    @GetMapping("/listByRoleId")
    R<List<AppVo>> listByRoleId(@RequestParam Long roleId);

}
