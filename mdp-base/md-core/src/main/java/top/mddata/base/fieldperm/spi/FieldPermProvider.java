package top.mddata.base.fieldperm.spi;

import top.mddata.base.fieldperm.model.UserFieldPerm;

/**
 * 字段权限数据提供方（单体实现，预留网关/其他服务共用）。
 *
 * @author henhen6
 * @since 2026-10-02
 */
public interface FieldPermProvider {

    /** 是否启用字段权限鉴权 */
    boolean isAuthEnabled();

    /** 当前用户字段受限集（缓存B） */
    UserFieldPerm findUserPerm(Long userId);

    /**
     * 解析请求对应的菜单ID：URI+method → 资源 → 沿菜单上级链找"最近一个配置了启用字段规则的菜单"。
     *
     * @param uri    归一化后的请求路径（已剥网关/服务前缀）
     * @param method 请求方法
     * @return 菜单ID；接口未绑定资源或上级链无字段规则时返回 null
     */
    Long findMenuId(String uri, String method);
}
