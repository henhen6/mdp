package top.mddata.base.mybatisflex.datascope.spi;

import top.mddata.base.mybatisflex.datascope.model.DataScopeCurrentUser;

/**
 * 数据权限数据提供方 SPI。
 *
 * <p>引擎（md-base）不依赖业务模块，由 md-common-config 提供实现：
 * 从登录上下文取用户、按菜单 code 查菜单开关、按用户角色查授权。</p>
 *
 * @author henhen
 * @since 2026年09月26日
 */
public interface DataScopeProvider {

    /**
     * 是否启用过滤（预留总开关）
     */
    boolean isFilter();

    /**
     * 按菜单 code 查"已启用数据权限"的菜单 id
     *
     * @param menuCode 菜单 code（注解上声明）
     * @return 菜单 id；菜单不存在或未启用数据权限时返回 null
     */
    Long findEnabledMenuId(String menuCode);

    /**
     * 取当前用户视图及其对目标菜单的授权集合
     *
     * @param menuId 目标菜单 id
     * @return 当前用户视图（userId 可能为 null，表示系统线程）
     */
    DataScopeCurrentUser getCurrentUser(Long menuId);
}
