package top.mddata.console.organization.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.common.entity.UserRoleRel;

import java.io.Serializable;
import java.util.Collection;

/**
 * 用户角色关联 服务层。
 *
 * @author henhen6
 * @since 2025-11-12 15:50:00
 */
public interface UserRoleRelService extends SuperService<UserRoleRel> {
    /**
     * 批量删除角色-用户关系
     * @param roleIdList 角色ID
     */
    void removeByRoleIds(Collection<? extends Serializable> roleIdList);

}
