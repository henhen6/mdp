package top.mddata.open.facade.admin;

import top.mddata.base.base.R;
import top.mddata.open.vo.admin.AppVo;

import java.util.List;

/**
 * 应用接口
 *
 * @author henhen6
 * @since 2025/8/12 11:24
 */
public interface AppFacade {
    /**
     * 查询需要 接收事件推送的应用
     *
     * @return 应用
     */
    R<List<AppVo>> listNeedPushApp();


    /**
     * 校验用户是否拥有该应用
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @return 是否拥有该应用
     */
    R<Boolean> checkAppByUserId(Long userId, Long appId);

    /**
     * 查询角色已分配的应用（按权重降序）
     *
     * @param roleId 角色ID
     * @return 应用列表
     */
    R<List<AppVo>> listByRoleId(Long roleId);

    /**
     * 根据id查询应用
     *
     * @param appKey 应用标识
     * @return 应用
     */
    R<AppVo> getAppByAppKey(String appKey);

}
