package top.mddata.open.admin.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.admin.entity.GroupScopeRel;

import java.util.List;

/**
 * 分组拥有的oauth2权限 服务层。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
public interface GroupScopeRelService extends SuperService<GroupScopeRel> {
    /**
     * 删除分组下的指定Oauth2权限
     * @param groupId 分组
     * @param scopeIdList Oauth2权限
     * @return 是否成功
     */
    Boolean delete(Long groupId, List<Long> scopeIdList);
}
