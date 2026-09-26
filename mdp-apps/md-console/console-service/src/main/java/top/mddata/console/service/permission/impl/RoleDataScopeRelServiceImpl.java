package top.mddata.console.service.permission.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.mybatisflex.datascope.DataScopeEnum;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.MyTreeUtil;
import top.mddata.common.cache.console.permission.RoleDataScopeCacheKeyBuilder;
import top.mddata.console.dto.permission.RoleDataScopeRelDto;
import top.mddata.console.entity.permission.ResourceMenu;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.entity.permission.RoleDataScopeRel;
import top.mddata.console.mapper.permission.ResourceMenuMapper;
import top.mddata.console.mapper.permission.RoleDataScopeRelMapper;
import top.mddata.console.service.permission.RoleDataScopeRelService;
import top.mddata.console.service.permission.RoleService;
import top.mddata.console.vo.permission.DataScopeMenuTreeVo;
import top.mddata.console.vo.permission.RoleDataScopeRelVo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色数据范围授权 服务层实现。
 *
 * @author henhen6
 * @since 2026-09-26
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoleDataScopeRelServiceImpl
        extends SuperServiceImpl<RoleDataScopeRelMapper, RoleDataScopeRel>
        implements RoleDataScopeRelService {

    private final RoleService roleService;
    // 用 Mapper 在 DAO 层打破循环依赖，避免 @Lazy
    private final ResourceMenuMapper resourceMenuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRoleDataScope(RoleDataScopeRelDto dto) {
        Long roleId = dto.getRoleId();
        List<RoleDataScopeRelDto.Item> items = dto.getItems() == null ? List.of() : dto.getItems();
        Role permSet = roleService.getPermSetRoleOfCurrentOperator();

        Set<Long> menuIdSet = new HashSet<>();
        for (RoleDataScopeRelDto.Item item : items) {
            validateItem(permSet, item);
            ArgumentAssert.isTrue(menuIdSet.add(item.getMenuId()),
                    "菜单[{}]存在重复授权项", item.getMenuId());
        }

        List<RoleDataScopeRel> oldList = list(QueryWrapper.create()
                .eq(RoleDataScopeRel::getRoleId, roleId));
        mapper.deleteByQuery(QueryWrapper.create().eq(RoleDataScopeRel::getRoleId, roleId));

        List<RoleDataScopeRel> entityList = items.stream()
                .map(item -> {
                    // builder 由基类生成、build 返回基类类型，故直接构造子类
                    RoleDataScopeRel entity = new RoleDataScopeRel();
                    entity.setRoleId(roleId);
                    entity.setMenuId(item.getMenuId());
                    entity.setDataScope(item.getDataScope());
                    entity.setDataScopeImpl(item.getDataScopeImpl());
                    return entity;
                })
                .toList();
        if (CollUtil.isNotEmpty(entityList)) {
            saveBatch(entityList);
        }

        // 失效新旧授权涉及的缓存，保证引擎立即读到最新授权
        oldList.forEach(old -> menuIdSet.add(old.getMenuId()));
        List<CacheKey> keys = menuIdSet.stream()
                .map(menuId -> RoleDataScopeCacheKeyBuilder.build(roleId, menuId))
                .toList();
        cacheOps.del(keys);
        return true;
    }

    /**
     * 校验单个授权项：菜单已启用数据权限、档位有效、
     * 自定义档 Bean 名约束、档位不超出权限集合可分配范围
     */
    private void validateItem(Role permSet, RoleDataScopeRelDto.Item item) {
        ResourceMenu menu = resourceMenuMapper.selectOneById(item.getMenuId());
        ArgumentAssert.notNull(menu, "菜单[{}]不存在", item.getMenuId());
        ArgumentAssert.isTrue(Boolean.TRUE.equals(menu.getDataScopeState()),
                "菜单[{}]未启用数据权限", menu.getName());

        DataScopeEnum scope = DataScopeEnum.getByCode(item.getDataScope());
        ArgumentAssert.notNull(scope, "数据范围档位[{}]无效", item.getDataScope());
        if (DataScopeEnum.CUSTOM.equals(scope)) {
            ArgumentAssert.isTrue(StrUtil.isNotBlank(item.getDataScopeImpl()),
                    "自定义实现档必须填写实现类 Bean 名");
        } else {
            ArgumentAssert.isTrue(StrUtil.isBlank(item.getDataScopeImpl()),
                    "非自定义档不能填写实现类 Bean 名");
        }

        DataScopeEnum permSetScope = findScopeOfRole(permSet, item.getMenuId());
        List<DataScopeEnum> assignable = RoleService.getAssignableDataScopes(permSetScope);
        ArgumentAssert.isTrue(assignable.contains(scope),
                "档位[{}]超出权限集合可分配范围", scope.getDesc());
    }

    /**
     * 查权限集合角色对某菜单的授权档
     *
     * @return 档位；权限集合角色不存在或未授权该菜单时返回 null
     */
    private DataScopeEnum findScopeOfRole(Role permSet, Long menuId) {
        if (permSet == null) {
            return null;
        }
        RoleDataScopeRel rel = getOne(QueryWrapper.create()
                .eq(RoleDataScopeRel::getRoleId, permSet.getId())
                .eq(RoleDataScopeRel::getMenuId, menuId));
        return rel == null ? null : DataScopeEnum.getByCode(rel.getDataScope());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDataScopeRelVo> findDataScopeByRoleId(Long roleId) {
        return listAs(QueryWrapper.create().eq(RoleDataScopeRel::getRoleId, roleId),
                RoleDataScopeRelVo.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataScopeMenuTreeVo> findAssignableDataScopeMenuTree() {
        List<ResourceMenu> enabledMenus = resourceMenuMapper.selectListByQuery(QueryWrapper.create()
                .eq(ResourceMenu::getDataScopeState, Boolean.TRUE)
                .eq(ResourceMenu::getState, Boolean.TRUE));
        if (CollUtil.isEmpty(enabledMenus)) {
            return List.of();
        }
        List<DataScopeMenuTreeVo> nodes = new ArrayList<>();
        for (ResourceMenu menu : enabledMenus) {
            List<DataScopeEnum> scopes = roleService.getAssignableScopesOfMenu(menu.getId());
            if (CollUtil.isEmpty(scopes)) {
                continue;
            }
            DataScopeMenuTreeVo node = toNode(menu, true);
            node.setScopes(scopes.stream()
                    .map(scope -> new DataScopeMenuTreeVo.ScopeOption(scope.getCode(), scope.getDesc()))
                    .toList());
            nodes.add(node);
        }
        if (nodes.isEmpty()) {
            return List.of();
        }
        // 可配置菜单可能挂在任意层级，补全祖先链保证树结构完整（祖先仅展示）
        Set<Long> ancestorIds = parseAncestorIds(enabledMenus);
        if (CollUtil.isNotEmpty(ancestorIds)) {
            List<ResourceMenu> ancestors = resourceMenuMapper.selectListByIds(ancestorIds);
            ancestors.forEach(ancestor -> nodes.add(toNode(ancestor, false)));
        }
        return buildTree(nodes);
    }

    private static DataScopeMenuTreeVo toNode(ResourceMenu menu, boolean configurable) {
        DataScopeMenuTreeVo node = new DataScopeMenuTreeVo();
        node.setMenuId(menu.getId());
        node.setName(menu.getName());
        node.setAppId(menu.getAppId());
        node.setParentId(menu.getParentId());
        node.setWeight(menu.getWeight());
        node.setConfigurable(configurable);
        return node;
    }

    /**
     * 解析已启用菜单的全部祖先 id（排除已启用菜单自身，避免重复节点）
     */
    public static Set<Long> parseAncestorIds(Collection<ResourceMenu> enabledMenus) {
        Set<Long> enabledIds = enabledMenus.stream()
                .map(ResourceMenu::getId)
                .collect(Collectors.toSet());
        Set<Long> ancestorIds = new HashSet<>();
        for (ResourceMenu menu : enabledMenus) {
            if (StrUtil.isBlank(menu.getTreePath())) {
                continue;
            }
            for (String idStr : StrUtil.split(menu.getTreePath(), MyTreeUtil.TREE_SPLIT)) {
                Long ancestorId = StrUtil.isBlank(idStr) ? null : Convert.toLong(idStr);
                if (ancestorId != null && !enabledIds.contains(ancestorId)) {
                    ancestorIds.add(ancestorId);
                }
            }
        }
        return ancestorIds;
    }

    /**
     * 按 parentId 组树；父不在集合中的节点提升为根；每层按 weight 升序
     */
    public static List<DataScopeMenuTreeVo> buildTree(List<DataScopeMenuTreeVo> nodes) {
        Map<Long, DataScopeMenuTreeVo> byId = new LinkedHashMap<>();
        nodes.forEach(node -> byId.putIfAbsent(node.getMenuId(), node));
        List<DataScopeMenuTreeVo> roots = new ArrayList<>();
        for (DataScopeMenuTreeVo node : byId.values()) {
            DataScopeMenuTreeVo parent = node.getParentId() == null
                    ? null : byId.get(node.getParentId());
            if (parent == null || parent == node) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        Comparator<DataScopeMenuTreeVo> byWeight = Comparator.comparing(
                node -> node.getWeight() == null ? Integer.MAX_VALUE : node.getWeight());
        byId.values().forEach(node -> node.getChildren().sort(byWeight));
        roots.sort(byWeight);
        return roots;
    }
}
