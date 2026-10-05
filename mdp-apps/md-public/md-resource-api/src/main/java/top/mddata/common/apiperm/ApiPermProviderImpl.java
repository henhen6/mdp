package top.mddata.common.apiperm;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.mddata.base.apiperm.model.ApiPattern;
import top.mddata.base.apiperm.model.UserApiPerm;
import top.mddata.base.apiperm.spi.ApiPermProvider;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.common.cache.console.permission.ResourceApiAllCacheKeyBuilder;
import top.mddata.common.cache.console.permission.UserResourceApiCacheKeyBuilder;
import top.mddata.common.constant.RoleCode;
import top.mddata.common.properties.IgnoreProperties;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 接口权限数据提供方（单体/网关共享）。
 *
 * <p>跨模块直查 mdc_ 表（MyBatis-Flex Row Db），与 DataScopeProviderImpl 同一模式；
 * 缓存失效由 console 侧写操作执行（共享 Redis key）。</p>
 */
@Component
@RequiredArgsConstructor
public class ApiPermProviderImpl implements ApiPermProvider {
    private final CacheOps cacheOps;
    private final IgnoreProperties ignoreProperties;

    /**
     * 组装用户放行集（纯函数，便于单测）。
     * 运营者短路：含 OPERATIONS_ADMIN 角色即豁免，不再查接口表。
     */
    static UserApiPerm assemble(List<RoleRow> roles,
                                Function<List<Long>, List<ApiPattern>> apiLoader) {
        // 运营者管理员，视为拥有所有权限
        boolean operationsAdmin = roles.stream()
                .anyMatch(r -> RoleCode.OPERATIONS_ADMIN.equals(r.code()));
        if (operationsAdmin) {
            return new UserApiPerm(true, Set.of());
        }
        List<Long> roleIds = roles.stream().map(RoleRow::roleId).toList();
        if (roleIds.isEmpty()) {
            return new UserApiPerm(false, Set.of());
        }
        return new UserApiPerm(false, new HashSet<>(apiLoader.apply(roleIds)));
    }

    @Override
    public boolean isAuthEnabled() {
        return Boolean.TRUE.equals(ignoreProperties.getAuthEnabled());
    }

    @Override
    public boolean isNotConfigAllow() {
        return Boolean.TRUE.equals(ignoreProperties.getNotConfigUriAllow());
    }

    @Override
    public String getGatewayPrefix() {
        return ignoreProperties.getGatewayPrefix();
    }

    @Override
    public Set<String> getServicePrefixes() {
        return ignoreProperties.getServicePrefixes();
    }

    @Override
    public List<ApiPattern> findAllPatterns() {
        CacheKey key = ResourceApiAllCacheKeyBuilder.build();
        CacheResult<List<ApiPattern>> result = cacheOps.get(key, k -> {
            List<Row> rows = Db.selectListByQuery(QueryWrapper.create()
                    .select("DISTINCT uri", "request_method AS requestMethod")
                    .from("mdc_resource_api"));
            return rows.stream()
                    .map(r -> new ApiPattern(r.getString("uri"), r.getString("requestMethod")))
                    .toList();
        });
        return result.getValue();
    }

    @Override
    public UserApiPerm findUserPerm(Long userId) {
        CacheKey key = UserResourceApiCacheKeyBuilder.build(userId);
        CacheResult<UserApiPerm> result = cacheOps.get(key, k -> loadUserPerm(userId));
        return result.getValue();
    }

    private UserApiPerm loadUserPerm(Long userId) {
        // 查询用户拥有的所有角色ID和角色编码。
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
        return assemble(roles, this::findPatternsByRoleIds);
    }

    /**
     * 根据角色ID查询角色拥有的接口权限集。
     *
     * 角色 → 资源 → 接口：先查授权资源 id，再按资源 id 反查接口。
     *
     * <p>忽略 resource_type：授权表 mdc_role_resource_rel 的 resource_type 列从未写入（全为空串），
     * resource_id 统一是菜单表 id 空间（按钮是 menu_type='50' 的菜单行，非独立表）；
     * mdc_resource_api.resource_type 仅作配置回显的展示元数据，运行期匹配按 resource_id。</p>
     */
    private List<ApiPattern> findPatternsByRoleIds(List<Long> roleIds) {
        List<Row> relRows = Db.selectListByQuery(QueryWrapper.create()
                .select("DISTINCT resource_id AS resourceId")
                .from("mdc_role_resource_rel")
                .where("role_id IN (" + roleIds.stream().map(String::valueOf)
                        .collect(Collectors.joining(",")) + ")"));
        if (relRows.isEmpty()) {
            return List.of();
        }
        // 菜单禁用（B1 读时过滤）：自身或祖先被禁用的资源，其接口授权能力整枝失效，启用后自动恢复
        Set<Long> disabledBranchIds = resolveDisabledBranchIds(loadMenuStates());
        List<Long> resourceIds = relRows.stream()
                .map(r -> r.getLong("resourceId"))
                .filter(id -> !disabledBranchIds.contains(id))
                .toList();
        if (resourceIds.isEmpty()) {
            return List.of();
        }
        String ids = resourceIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        List<Row> apiRows = Db.selectListByQuery(QueryWrapper.create()
                .select("uri", "request_method AS requestMethod")
                .from("mdc_resource_api")
                .where("resource_id IN (" + ids + ")"));
        return apiRows.stream()
                .map(r -> new ApiPattern(r.getString("uri"), r.getString("requestMethod")))
                .distinct()
                .toList();
    }

    /**
     * 禁用分支闭包（纯函数，便于单测）：禁用菜单自身 + 沿 parent 链向下的全部后代。
     * state 为 null 的历史数据视为启用；父子互指的脏数据不会死循环。
     */
    static Set<Long> resolveDisabledBranchIds(List<MenuStateRow> menus) {
        Map<Long, List<Long>> childrenOf = new HashMap<>();
        for (MenuStateRow menu : menus) {
            childrenOf.computeIfAbsent(menu.parentId(), k -> new ArrayList<>()).add(menu.id());
        }
        Set<Long> disabled = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        for (MenuStateRow menu : menus) {
            if (Boolean.FALSE.equals(menu.state())) {
                queue.add(menu.id());
            }
        }
        while (!queue.isEmpty()) {
            Long id = queue.poll();
            if (!disabled.add(id)) {
                continue;
            }
            queue.addAll(childrenOf.getOrDefault(id, List.of()));
        }
        return disabled;
    }

    private List<MenuStateRow> loadMenuStates() {
        return Db.selectListByQuery(QueryWrapper.create()
                        .select("id", "parent_id AS parentId", "state")
                        .from("mdc_resource_menu")
                        .where("deleted_at = 0"))
                .stream()
                .map(r -> new MenuStateRow(r.getLong("id"), r.getLong("parentId"), r.getBoolean("state")))
                .toList();
    }

    /**
     * 菜单状态行（仅供 resolveDisabledBranchIds 使用）。
     *
     * @param id       菜单ID
     * @param parentId 上级菜单ID
     * @param state    启用状态（null 视为启用）
     */
    public record MenuStateRow(Long id, Long parentId, Boolean state) {
    }

    /**
     * 用户角色行（仅供 assemble 使用）。
     *
     * @param roleId 角色ID
     * @param code   角色编码
     */
    public record RoleRow(Long roleId, String code) {
    }
}
