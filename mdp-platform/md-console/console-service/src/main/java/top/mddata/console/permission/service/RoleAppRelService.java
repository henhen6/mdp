package top.mddata.console.permission.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.permission.dto.RoleAppRelDto;
import top.mddata.console.permission.entity.RoleAppRel;

import java.io.Serializable;
import java.util.Collection;

/**
 * 角色应用关联 服务层。
 *
 * @author henhen6
 * @since 2025-12-03 14:54:25
 */
public interface RoleAppRelService extends SuperService<RoleAppRel> {
    /**
     * 批量删除角色应用关系
     * @param roleIdList 角色ID
     */
    void removeByRoleIds(Collection<? extends Serializable> roleIdList);


    /**
     * 删除 角色和应用的关联
     * @param dto 参数
     * @return 是否成功
     */
    Boolean delete(RoleAppRelDto dto);

    /**
     * 给角色授权应用
     * @param dto 参数
     * @return 是否成功
     */
    Boolean saveByDto(RoleAppRelDto dto);
}
