package top.mddata.console.service.permission;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.base.mybatisflex.datascope.DataScopeEnum;
import top.mddata.console.entity.permission.Role;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 角色 服务层。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
public interface RoleService extends SuperService<Role> {
    /**
     * 获取用户角色编码
     * @param userId 用户ID
     * @return 角色编码
     */
    List<String> findUserRoleCodes(Long userId);

    /**
     * 检测角色编码是否已存在
     * @param roleCategory 角色类别
     * @param code 角色编码
     * @param id 角色ID
     * @return true-已存在，false-不存在
     */
    Boolean checkCode(String roleCategory, String code, Long id);

    /**
     * 根据编码查找角色
     * @param code 编码
     * @return 角色
     */
    Role getByCode(String code);

    /**
     * 检测角色类别和组织性质是否已经存在
     * @param roleCategory 角色类别
     * @param orgNature 组织性质
     * @param id 角色ID
     * @return true-已存在，false-不存在
     */
    Boolean checkCategoryAndOrgNature(String roleCategory, Integer orgNature, Long id);

    /**
     * 将用户加入角色
     * @param code 角色编码
     * @param userId 用户id
     */
    void joinTheRole(String code, Long userId);

    /**
     * 查询当前操作人顶级公司性质对应的权限集合角色
     *
     * @return 权限集合角色，不存在时返回 null
     */
    Role getPermSetRoleOfCurrentOperator();

    /**
     * 当前操作人对指定菜单可分配的数据范围档位
     *
     * @param menuId 菜单id
     * @return 可分配档位列表（按优先级从高到低）
     */
    List<DataScopeEnum> getAssignableScopesOfMenu(Long menuId);

    /**
     * 查询角色已分配的应用id集合
     *
     * @param roleId 角色id
     * @return 应用id列表
     */
    List<Long> listAppIdsByRoleId(Long roleId);

    /**
     * 计算操作人可分配的数据范围档位：不得高于权限集合对该菜单的授权档。
     *
     * <p>按优先级过滤：全部 &gt; 自定义 &gt; 公司及以下 &gt;
     * 部门及以下 &gt; 部门 &gt; 仅本人；返回列表按优先级从高到低排序，
     * 供前端下拉展示。</p>
     *
     * @param permSetScope 权限集合角色对该菜单的授权档，
     *                     null 表示未授权（不可分配）
     * @return 可分配档位列表
     */
    static List<DataScopeEnum> getAssignableDataScopes(DataScopeEnum permSetScope) {
        if (permSetScope == null) {
            return List.of();
        }
        return Arrays.stream(DataScopeEnum.values())
                .filter(scope -> scope.priority() <= permSetScope.priority())
                .sorted(Comparator.comparingInt(DataScopeEnum::priority).reversed())
                .toList();
    }
}
