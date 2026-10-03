package top.mddata.console.service.permission;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.dto.permission.RoleDataScopeRelDto;
import top.mddata.console.entity.permission.RoleDataScopeRel;
import top.mddata.console.vo.permission.DataScopeMenuTreeVo;
import top.mddata.console.vo.permission.RoleDataScopeRelVo;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 角色数据范围授权 服务层。
 *
 * @author henhen6
 * @since 2026-09-26
 */
public interface RoleDataScopeRelService extends SuperService<RoleDataScopeRel> {

    /**
     * 保存角色的数据权限授权（全量覆盖：先删后插）
     *
     * @param dto 授权项
     * @return true-成功
     */
    Boolean saveRoleDataScope(RoleDataScopeRelDto dto);

    /**
     * 查询角色的数据权限授权
     *
     * @param roleId 角色id
     * @return 授权列表
     */
    List<RoleDataScopeRelVo> findDataScopeByRoleId(Long roleId);

    /**
     * 查询可授权的数据权限菜单树
     *
     * <p>树中包含 dataScopeState=1 的可配置节点（带可分配档位，
     * 已按操作人权限集合过滤），以及为展示树结构补全的祖先节点
     * （仅展示，不可配置）。</p>
     *
     * @return 菜单树（按 weight 升序）
     */
    List<DataScopeMenuTreeVo> findAssignableDataScopeMenuTree();

    /**
     * 按角色删除数据权限授权（删除角色时级联调用），
     * 并按涉及的角色与菜单失效 RoleDataScope 缓存
     *
     * @param roleIdList 角色ID集合
     */
    void removeByRoleIds(Collection<? extends Serializable> roleIdList);
}
