package top.mddata.open.service.oauth;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.entity.admin.ScopeGroup;
import top.mddata.open.vo.admin.ScopeGroupDetailVo;
import top.mddata.open.vo.admin.ScopeGroupVo;

import java.util.List;

/**
 * 应用权限分组 服务层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
public interface ScopeGroupService extends SuperService<ScopeGroup> {

    /**
     * 查询应用可申请的权限组（启用 且 未拥有 且 无待审批申请单）
     *
     * @param appId 应用ID
     * @return 可申请权限组
     */
    List<ScopeGroupVo> listAppliable(Long appId);

    /**
     * 查询应用已拥有的权限组
     *
     * @param appId 应用ID
     * @return 已拥有权限组
     */
    List<ScopeGroupVo> listOwned(Long appId);

    /**
     * 查询权限组详情（含组内开放接口与 oauth2 权限）
     *
     * @param groupId 权限组ID
     * @return 组详情
     */
    ScopeGroupDetailVo getGroupDetail(Long groupId);
}
