package top.mddata.console.service.permission;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.base.mybatisflex.datapermission.DataScope;
import top.mddata.console.entity.permission.Role;

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
     * 当前操作人可分配的数据范围档位
     *
     * @return 可分配档位列表
     */
    List<DataScope> getAssignableDataScopes();

    /**
     * 查询角色已分配的应用id集合
     *
     * @param roleId 角色id
     * @return 应用id列表
     */
    List<Long> listAppIdsByRoleId(Long roleId);

    /**
     * 计算可分配的数据范围档位：不超过权限集合档位的内置档；
     * 自定义实现档仅当权限集合为全部数据时可分配。
     *
     * <p>内置档按 全部 &gt; 本公司及以下 &gt; 本部门及以下 &gt; 本部门
     * &gt; 仅本人 排序，可分配档位 = 档位 ≤ 权限集合档位（排序下标越小档位越大）；
     * 例如权限集合为本公司及以下时，可分配
     * 本公司及以下/本部门及以下/本部门/仅本人，
     * 不可分配"全部数据"与"自定义实现"。</p>
     *
     * <p>注意：入参 null 表示"权限集合角色存在但 data_scope 列未配置"，
     * 视同全部；调用方不得用 null 表达"权限集合角色不存在"
     * （该场景应在调用前拦截并返回空集合）。</p>
     *
     * @param permSetScope 权限集合角色的数据范围，null 视为全部
     * @return 可分配的档位列表
     */
    static List<DataScope> getAssignableDataScopes(DataScope permSetScope) {
        if (permSetScope == null || DataScope.ALL.equals(permSetScope)) {
            return List.of(DataScope.values());
        }
        List<DataScope> builtIn = List.of(DataScope.COMPANY_AND_CHILD, DataScope.DEPT_AND_CHILD,
                DataScope.DEPT, DataScope.SELF);
        int maxRank = builtIn.indexOf(permSetScope);
        if (maxRank < 0) {
            // 权限集合自身是 CUSTOM：没有"比全部小"的对应内置档，
            // 从严只允许仅本人
            return List.of(DataScope.SELF);
        }
        // 下标越小档位越大，可分配的是不高于权限集合的档位，
        // 即下标 ≥ maxRank
        return List.copyOf(builtIn.subList(maxRank, builtIn.size()));
    }
}
