package top.mddata.console.service.organization;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.console.dto.organization.UserRoleRelDto;
import top.mddata.console.dto.organization.UserRoleSaveDto;
import top.mddata.console.vo.organization.AssignableRoleVo;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

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

    /**
     * 查询用户在可分配范围内已拥有的角色ID（分配角色弹窗回显）。
     * 可分配范围：非模板角色 + 普通角色。
     *
     * @param userId 用户id
     * @return 角色ID集合
     */
    List<Long> findRoleIdByUserId(Long userId);

    /**
     * 全量覆盖保存用户的角色（用户侧分配角色）。
     * 只覆盖可分配范围内的关联，模板角色等其他类别绑定不受影响。
     *
     * @param dto 用户-角色关系
     * @return 保存结果
     */
    Boolean saveUserRole(UserRoleSaveDto dto);

    /**
     * 查询当前操作者可分配的角色列表（分配角色弹窗数据源）。
     * 运营人员：所有性质的管理员与普通角色；其他身份：本公司性质的普通角色。
     *
     * @return 可分配角色与操作者身份标识
     */
    AssignableRoleVo listAssignableRoles();
}
