package top.mddata.console.service.permission;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.dto.permission.RoleFieldRelDto;
import top.mddata.console.entity.permission.RoleFieldRel;
import top.mddata.console.vo.permission.ResourceFieldVo;

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
     * 查询当前操作人可分配的字段规则
     * （其组织性质的权限集合角色已授权、且启用中的字段）
     *
     * @return 字段规则集合；权限集合角色不存在时返回空集合
     */
    List<ResourceFieldVo> findAssignableFieldList();

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
}
