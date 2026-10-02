package top.mddata.common.fieldperm;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.fieldperm.model.FieldRule;
import top.mddata.base.fieldperm.model.UserFieldPerm;
import top.mddata.base.fieldperm.spi.FieldPermProvider;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.common.apiperm.ApiPermProviderImpl.RoleRow;
import top.mddata.common.cache.console.permission.ResourceFieldUriMenuCacheKeyBuilder;
import top.mddata.common.cache.console.permission.UserFieldPermCacheKeyBuilder;
import top.mddata.common.constant.RoleCode;
import top.mddata.common.properties.IgnoreProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 字段权限数据提供方。
 *
 * <p>跨模块直查 mdc_ 表（MyBatis-Flex Row Db），与 ApiPermProviderImpl 同一模式；
 * 缓存失效由 console 侧写操作执行（共享 Redis key）。</p>
 *
 * @author henhen6
 * @since 2026-10-02
 */
@Component
@RequiredArgsConstructor
public class FieldPermProviderImpl implements FieldPermProvider {
    private final CacheOps cacheOps;
    private final IgnoreProperties ignoreProperties;

    @Override
    public boolean isAuthEnabled() {
        return Boolean.TRUE.equals(ignoreProperties.getFieldAuthEnabled());
    }

    @Override
    public UserFieldPerm findUserPerm(Long userId) {
        CacheKey key = UserFieldPermCacheKeyBuilder.build(userId);
        CacheResult<UserFieldPerm> result = cacheOps.get(key, k -> loadUserPerm(userId));
        return result.getValue();
    }

    private UserFieldPerm loadUserPerm(Long userId) {
        List<Row> roleRows = Db.selectListByQuery(QueryWrapper.create()
                .select("r.id AS roleId", "r.code AS code")
                .from("mdc_user_role_rel").as("ur")
                .innerJoin("mdc_role").as("r").on("ur.role_id = r.id")
                .where("ur.user_id = ?", userId)
                .and("r.state = ?", Boolean.TRUE)
                .and("r.deleted_at = 0"));
        List<RoleRow> roles = roleRows.stream()
                .map(r -> new RoleRow(r.getLong("roleId"), r.getString("code")))
                .toList();
        return assemble(roles, this::findFieldRowsByRoleIds);
    }

    /**
     * 组装用户字段受限集（纯函数，便于单测）。
     * 运营者短路：含 OPERATIONS_ADMIN 角色即豁免，不再查字段表。
     */
    static UserFieldPerm assemble(List<RoleRow> roles,
                                  Function<List<Long>, List<FieldRow>> fieldLoader) {
        boolean operationsAdmin = roles.stream()
                .anyMatch(r -> RoleCode.OPERATIONS_ADMIN.equals(r.code()));
        if (operationsAdmin) {
            return new UserFieldPerm(true, Map.of());
        }
        List<Long> roleIds = roles.stream().map(RoleRow::roleId).toList();
        if (roleIds.isEmpty()) {
            return new UserFieldPerm(false, Map.of());
        }
        Map<Long, Map<String, FieldRule>> menuRules = new HashMap<>();
        for (FieldRow row : fieldLoader.apply(roleIds)) {
            menuRules.computeIfAbsent(row.menuId(), k -> new HashMap<>())
                    .put(row.property(), new FieldRule(row.ruleType(), row.maskRule()));
        }
        return new UserFieldPerm(false, menuRules);
    }

    /** 角色 → 受限字段规则（仅启用且未删除的字段配置） */
    private List<FieldRow> findFieldRowsByRoleIds(List<Long> roleIds) {
        String ids = roleIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<Row> rows = Db.selectListByQuery(QueryWrapper.create()
                .select("f.menu_id AS menuId", "f.property AS property",
                        "f.rule_type AS ruleType", "f.mask_rule AS maskRule")
                .from("mdc_role_field_rel").as("rfr")
                .innerJoin("mdc_resource_field").as("f").on("f.id = rfr.field_id")
                .where("rfr.role_id IN (" + ids + ")")
                .and("f.state = ?", Boolean.TRUE)
                .and("f.deleted_at = 0"));
        return rows.stream()
                .map(r -> new FieldRow(r.getLong("menuId"), r.getString("property"),
                        r.getInt("ruleType"), r.getString("maskRule")))
                .toList();
    }

    @Override
    public Long findMenuId(String uri, String method) {
        CacheKey key = ResourceFieldUriMenuCacheKeyBuilder.build();
        CacheResult<Map<String, Long>> result = cacheOps.get(key, k -> loadUriMenuMap());
        Map<String, Long> map = result.getValue();
        if (map == null) {
            return null;
        }
        return map.get(uriMenuKey(uri, method));
    }

    /** 缓存 key：METHOD + 空格 + uri，与 findMenuId 查询侧保持一致 */
    static String uriMenuKey(String uri, String method) {
        return method.toUpperCase() + " " + uri;
    }

    /**
     * 预解析"URI → 字段权限菜单"映射：只保留上级链上存在启用字段规则的接口，
     * 鉴权侧一次 map 查空即放行，绝大多数请求零开销。
     */
    private Map<String, Long> loadUriMenuMap() {
        List<Row> apiRows = Db.selectListByQuery(QueryWrapper.create()
                .select("uri", "request_method AS requestMethod", "resource_id AS resourceId")
                .from("mdc_resource_api"));
        List<ApiRow> apis = apiRows.stream()
                .map(r -> new ApiRow(r.getString("uri"), r.getString("requestMethod"), r.getLong("resourceId")))
                .toList();

        // 不能用 Collectors.toMap：根菜单 parent_id 为 NULL，其底层 HashMap.merge 遇 null value 抛 NPE
        Map<Long, Long> parentOf = new HashMap<>();
        Db.selectListByQuery(QueryWrapper.create()
                        .select("id", "parent_id AS parentId")
                        .from("mdc_resource_menu")
                        .where("deleted_at = 0"))
                .forEach(r -> parentOf.put(r.getLong("id"), r.getLong("parentId")));

        Set<Long> fieldMenuIds = new HashSet<>(Db.selectListByQuery(QueryWrapper.create()
                        .select("DISTINCT menu_id AS menuId")
                        .from("mdc_resource_field")
                        .where("state = ?", Boolean.TRUE)
                        .and("deleted_at = 0"))
                .stream().map(r -> r.getLong("menuId")).toList());

        return resolveUriMenu(apis, parentOf, fieldMenuIds);
    }

    /** 预解析（纯函数，便于单测）：URI+method → 沿上级链找到的最近字段规则菜单 */
    static Map<String, Long> resolveUriMenu(List<ApiRow> apis, Map<Long, Long> parentOf, Set<Long> fieldMenuIds) {
        Map<String, Long> result = new HashMap<>();
        for (ApiRow api : apis) {
            Long menuId = resolveMenu(api.resourceId(), parentOf, fieldMenuIds);
            if (menuId != null) {
                result.put(uriMenuKey(api.uri(), api.method()), menuId);
            }
        }
        return result;
    }

    /** 从绑定资源（菜单或按钮）沿上级链找最近一个配置了启用字段规则的菜单 */
    private static Long resolveMenu(Long resourceId, Map<Long, Long> parentOf, Set<Long> fieldMenuIds) {
        // 防御脏数据 parent 成环
        Set<Long> seen = new HashSet<>();
        Long id = resourceId;
        while (id != null && seen.add(id)) {
            if (fieldMenuIds.contains(id)) {
                return id;
            }
            id = parentOf.get(id);
        }
        return null;
    }

    /**
     * 接口绑定资源行（仅供 resolveUriMenu 使用）。
     *
     * @param uri            接口路径（已去前缀）
     * @param method         请求方法
     * @param resourceId     绑定的菜单/按钮ID
     */
    public record ApiRow(String uri, String method, Long resourceId) {
    }

    /**
     * 字段规则行（仅供 assemble 使用）。
     *
     * @param menuId   所属菜单
     * @param property 受控字段
     * @param ruleType 处理动作
     * @param maskRule 脱敏规则
     */
    public record FieldRow(Long menuId, String property, Integer ruleType, String maskRule) {
    }
}
