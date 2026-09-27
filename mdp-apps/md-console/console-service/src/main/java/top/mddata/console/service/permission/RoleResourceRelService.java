package top.mddata.console.service.permission;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.dto.permission.RoleResourceRelDto;
import top.mddata.console.entity.permission.RoleResourceRel;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
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

    /**
     * 批量删除角色资源关系
     * @param roleIdList 角色ID
     */
    void removeByRoleIds(Collection<? extends Serializable> roleIdList);

    /**
     * 根据角色ID和应用ID 批量删除角色资源关系
     * @param roleId 角色ID
     * @param appIdList 应用ID
     */
    void removeByRoleIdAndAppIds(Long roleId, List<Long> appIdList);

    /**
     * 失效指定角色集合下所有用户的接口放行集缓存B。
     *
     * <p>低频操作（角色授权变更、角色启停等），可接受全量失效。
     *
     * @param roleIdList 角色ID集合
     */
    void invalidateUserResourceApiCacheByRoleIds(Collection<? extends Serializable> roleIdList);
}
