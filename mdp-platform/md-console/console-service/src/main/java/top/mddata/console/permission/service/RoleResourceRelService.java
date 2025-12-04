package top.mddata.console.permission.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.permission.dto.RoleResourceRelDto;
import top.mddata.console.permission.entity.RoleResourceRel;

import java.util.Collection;
import java.util.Map;

/**
 * 角色资源关联 服务层。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:29
 */
public interface RoleResourceRelService extends SuperService<RoleResourceRel> {

    /**
     * 保存角色资源关系
     * @param dto dto
     * @return 保存结果
     */
    Boolean saveRoleResource(RoleResourceRelDto dto);

    /**
     * 查询角色拥有的资源集合
     * @param roleId 角色ID
     * @return 应用-资源集合
     */
    Map<Long, Collection<Long>> findResourceIdByRoleId(Long roleId);
}
