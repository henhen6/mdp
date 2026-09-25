package top.mddata.console.service.organization;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.console.dto.organization.UserRoleRelDto;

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

    /**
     * 保存角色-用户关系
     * @param dto 角色-用户关系
     * @return 保存结果
     */
    Boolean saveByDto(UserRoleRelDto dto);

    /**
     * 删除角色-用户关系
     * @param dto 角色-用户关系
     * @return 删除结果
     */
    Boolean delete(UserRoleRelDto dto);

    /**
     * 校验角色用户绑定：用户所属树的组织性质必须等于角色的组织性质（防身份漂移）。
     * 用户无任何组织关系、或角色不存在时 fail fast。
     *
     * @param roleId  角色id
     * @param userIds 待绑定用户id
     */
    void checkRoleUserNatureMatch(Long roleId, Collection<Long> userIds);
}
