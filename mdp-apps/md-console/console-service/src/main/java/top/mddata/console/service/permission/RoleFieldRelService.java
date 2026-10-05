package top.mddata.console.service.permission;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.dto.permission.RoleFieldRelDto;
import top.mddata.console.entity.permission.RoleFieldRel;
import top.mddata.console.vo.permission.RoleFieldAuthVo;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 角色字段受限关系 服务层。
 *
 * @author henhen6
 * @since 2026-10-02
 */
public interface RoleFieldRelService extends SuperService<RoleFieldRel> {

    /**
     * 查询角色已受限的字段规则ID集合
     *
     * @param roleId 角色ID
     * @return 字段规则ID集合
     */
    List<Long> findFieldIdsByRoleId(Long roleId);

    /**
     * 保存角色字段受限关系（按 roleId 全量重写）
     *
     * @param dto 授权入参
     * @return 保存结果
     */
    Boolean saveRoleField(RoleFieldRelDto dto);

    /**
     * 字段权限授权页面聚合数据：按应用分组的「菜单+字段规则」树（仅含字段规则的分支），
     * 以及各应用下已受限的字段规则id集合
     *
     * @param roleId    角色ID
     * @param fullScope true=全量（角色模板：应用全量菜单+全部启用字段）；
     *                  false=权限集合约束（角色管理：同组织性质权限集合已分配的菜单与字段池）
     * @return 聚合数据
     */
    RoleFieldAuthVo fieldAuthTree(Long roleId, boolean fullScope);

    /**
     * 失效指定角色集合下所有用户的字段受限集缓存
     *
     * @param roleIdList 角色ID集合
     */
    void invalidateUserFieldPermCacheByRoleIds(Collection<? extends Serializable> roleIdList);

    /**
     * 失效与指定字段规则相关的所有用户的字段受限集缓存
     * （字段配置变更/删除时调用）
     *
     * @param fieldIdList 字段规则ID集合
     */
    void invalidateUserFieldPermCacheByFieldIds(Collection<Long> fieldIdList);

    /**
     * 按角色删除字段受限关系（删除角色时级联调用），
     * 并失效相关用户的字段受限集缓存
     *
     * @param roleIdList 角色ID集合
     */
    void removeByRoleIds(Collection<? extends Serializable> roleIdList);
}
