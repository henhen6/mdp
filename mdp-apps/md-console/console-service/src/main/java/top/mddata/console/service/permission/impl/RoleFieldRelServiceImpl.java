package top.mddata.console.service.permission.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.base.R;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.console.permission.UserFieldPermCacheKeyBuilder;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.enumeration.BooleanEnum;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.common.mapper.UserRoleRelMapper;
import top.mddata.console.dto.permission.RoleFieldRelDto;
import top.mddata.console.entity.permission.ResourceField;
import top.mddata.console.entity.permission.ResourceMenu;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.entity.permission.RoleFieldRel;
import top.mddata.console.entity.permission.RoleResourceRel;
import top.mddata.console.mapper.permission.ResourceFieldMapper;
import top.mddata.console.mapper.permission.ResourceMenuMapper;
import top.mddata.console.mapper.permission.RoleFieldRelMapper;
import top.mddata.console.service.permission.RoleFieldRelService;
import top.mddata.console.service.permission.RoleService;
import top.mddata.console.vo.permission.RoleFieldAuthVo;
import top.mddata.open.facade.admin.AppFacade;
import top.mddata.open.vo.admin.AppVo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色字段受限关系 服务层实现。
 *
 * @author henhen6
 * @since 2026-10-02
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoleFieldRelServiceImpl extends SuperServiceImpl<RoleFieldRelMapper, RoleFieldRel> implements RoleFieldRelService {
    private final UserRoleRelMapper userRoleRelMapper;
    private final ResourceFieldMapper resourceFieldMapper;
    private final ResourceMenuMapper resourceMenuMapper;
    private final RoleService roleService;
    private final AppFacade appFacade;

    @Override
    @Transactional(readOnly = true)
    public List<Long> findFieldIdsByRoleId(Long roleId) {
        return mapper.selectListByQuery(QueryWrapper.create()
                        .select(RoleFieldRel::getFieldId)
                        .where(RoleFieldRel::getRoleId).eq(roleId))
                .stream().map(RoleFieldRel::getFieldId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRoleField(RoleFieldRelDto dto) {
        Long roleId = dto.getRoleId();
        Role targetRole = roleService.getById(roleId);
        ArgumentAssert.notNull(targetRole, "角色[{}]不存在", roleId);
        validateAssignable(dto, targetRole);

        mapper.deleteByQuery(QueryWrapper.create().where(RoleFieldRel::getRoleId).eq(roleId));

        boolean flag = true;
        if (CollUtil.isNotEmpty(dto.getFieldIdList())) {
            List<RoleFieldRel> list = dto.getFieldIdList().stream().distinct()
                    .map(fieldId -> {
                        RoleFieldRel rel = new RoleFieldRel();
                        rel.setRoleId(roleId);
                        rel.setFieldId(fieldId);
                        return rel;
                    })
                    .toList();
            flag = saveBatch(list);
        }

        // 授权变更影响该角色下所有用户的字段受限集
        invalidateUserFieldPermCacheByRoleIds(List.of(roleId));
        return flag;
    }

    @Override
    @Transactional(readOnly = true)
    public RoleFieldAuthVo fieldAuthTree(Long roleId, boolean fullScope) {
        Role targetRole = roleService.getById(roleId);
        ArgumentAssert.notNull(targetRole, "角色[{}]不存在", roleId);

        RoleFieldAuthVo result = new RoleFieldAuthVo();
        // 角色已分配的应用（经 open 服务门面，按权重降序）
        List<AppVo> apps = listAppsByRoleId(roleId);
        if (CollUtil.isEmpty(apps)) {
            return result;
        }

        // 字段规则：模板=全部启用字段；普通角色=权限集合字段池
        List<ResourceField> fields = fullScope ? listEnabledFields() : listPermSetPoolFields();
        // 菜单：一次查出全部应用，普通角色叠加权限集合 exists 过滤（消除逐应用查询的 N+1）
        List<Long> appIds = apps.stream().map(AppVo::getId).toList();
        List<ResourceMenu> menus = listMenus(appIds, fullScope);
        Set<Long> checkedFieldIds = new HashSet<>(findFieldIdsByRoleId(roleId));

        Map<Long, List<ResourceMenu>> menusByApp = menus.stream()
                .collect(Collectors.groupingBy(ResourceMenu::getAppId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<ResourceField>> fieldsByMenu = fields.stream()
                .collect(Collectors.groupingBy(ResourceField::getMenuId));

        for (AppVo app : apps) {
            RoleFieldAuthVo.AppGroup group = new RoleFieldAuthVo.AppGroup();
            group.setAppId(app.getId());
            group.setAppName(app.getName());
            List<RoleFieldAuthVo.MenuNode> tree = buildMenuTree(
                    menusByApp.getOrDefault(app.getId(), List.of()), fieldsByMenu);
            group.setMenuTree(tree);

            // 已受限字段按应用拆分回显
            Set<Long> leafFieldIds = new HashSet<>();
            collectFieldIds(tree, leafFieldIds);
            group.setCheckedFieldIds(checkedFieldIds.stream().filter(leafFieldIds::contains).toList());
            result.getAppGroupList().add(group);
        }
        return result;
    }

    /**
     * 查角色已分配的应用（经 open 服务门面，按权重降序）
     */
    private List<AppVo> listAppsByRoleId(Long roleId) {
        R<List<AppVo>> r = appFacade.listByRoleId(roleId);
        ArgumentAssert.isTrue(r != null && r.getIsSuccess(), "查询角色已分配应用失败");
        return r.getData() == null ? List.of() : r.getData();
    }

    /**
     * 全部启用中的字段规则
     */
    private List<ResourceField> listEnabledFields() {
        return resourceFieldMapper.selectListByQuery(
                QueryWrapper.create().where(ResourceField::getState).eq(true));
    }

    /**
     * 权限集合字段池：当前操作人组织性质的权限集合角色已授权、且启用中的字段
     */
    private List<ResourceField> listPermSetPoolFields() {
        Role permSet = roleService.getPermSetRoleOfCurrentOperator();
        if (permSet == null) {
            return List.of();
        }
        QueryWrapper inWrapper = QueryWrapper.create()
                .select(RoleFieldRel::getFieldId).from(RoleFieldRel.class)
                .where(RoleFieldRel::getRoleId).eq(permSet.getId());
        return resourceFieldMapper.selectListByQuery(QueryWrapper.create().from(ResourceField.class)
                .where(ResourceField::getState).eq(true)
                .and(ResourceField::getId).in(inWrapper));
    }

    /**
     * 查应用菜单：fullScope=应用全量；
     * 否则叠加权限集合 exists 过滤（与 treeByRoleId 语义一致：
     * 仅同组织性质权限集合角色已分配的菜单）
     */
    private List<ResourceMenu> listMenus(List<Long> appIds, boolean fullScope) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(ResourceMenu::getAppId).in(appIds)
                .orderBy(ResourceMenu::getMenuType, true)
                .orderBy(ResourceMenu::getWeight, true);
        if (!fullScope) {
            QueryWrapper existsWrapper = QueryWrapper.create().select("1").from(RoleResourceRel.class)
                    .innerJoin(Role.class).on(RoleResourceRel::getRoleId, Role::getId)
                    .where(ResourceMenu::getId).eq(RoleResourceRel::getResourceId)
                    // 多应用场景按列相关，替代 treeByRoleId 的单应用常量条件
                    .and(RoleResourceRel::getAppId).eq(ResourceMenu::getAppId)
                    .and(Role::getRoleCategory).eq(RoleCategoryEnum.PERM_SET.getCode())
                    .and(Role::getOrgNature).eq(ContextUtil.getCurrentCompanyNature())
                    .and(Role::getTemplateRole).eq(BooleanEnum.TRUE.getInteger())
                    .and(Role::getState).eq(BooleanEnum.TRUE.getInteger());
            wrapper.and(QueryMethods.exists(existsWrapper));
        }
        return resourceMenuMapper.selectListByQuery(wrapper);
    }

    /**
     * 把菜单平铺列表组树、挂字段叶子，并裁剪掉不含字段规则的分支
     */
    private List<RoleFieldAuthVo.MenuNode> buildMenuTree(List<ResourceMenu> menus,
            Map<Long, List<ResourceField>> fieldsByMenu) {
        Map<Long, RoleFieldAuthVo.MenuNode> byId = new LinkedHashMap<>();
        for (ResourceMenu menu : menus) {
            RoleFieldAuthVo.MenuNode node = new RoleFieldAuthVo.MenuNode();
            node.setMenuId(menu.getId());
            node.setName(menu.getName());
            byId.put(menu.getId(), node);
        }
        for (ResourceMenu menu : menus) {
            RoleFieldAuthVo.MenuNode node = byId.get(menu.getId());
            for (ResourceField field : fieldsByMenu.getOrDefault(menu.getId(), List.of())) {
                RoleFieldAuthVo.FieldNode fieldNode = new RoleFieldAuthVo.FieldNode();
                fieldNode.setFieldId(field.getId());
                fieldNode.setName(field.getName());
                fieldNode.setProperty(field.getProperty());
                fieldNode.setRuleType(field.getRuleType());
                node.getFields().add(fieldNode);
            }
        }
        List<RoleFieldAuthVo.MenuNode> roots = new ArrayList<>();
        for (ResourceMenu menu : menus) {
            RoleFieldAuthVo.MenuNode node = byId.get(menu.getId());
            RoleFieldAuthVo.MenuNode parent = menu.getParentId() == null ? null : byId.get(menu.getParentId());
            if (parent == null || parent == node) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return pruneEmptyBranch(roots);
    }

    /**
     * 自底向上裁剪：自身无字段规则且子树也被裁空的分支不展示
     */
    private List<RoleFieldAuthVo.MenuNode> pruneEmptyBranch(List<RoleFieldAuthVo.MenuNode> nodes) {
        List<RoleFieldAuthVo.MenuNode> result = new ArrayList<>();
        for (RoleFieldAuthVo.MenuNode node : nodes) {
            node.setChildren(pruneEmptyBranch(node.getChildren()));
            if (CollUtil.isNotEmpty(node.getFields()) || CollUtil.isNotEmpty(node.getChildren())) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * 收集树中的全部字段规则id
     */
    private void collectFieldIds(List<RoleFieldAuthVo.MenuNode> nodes, Set<Long> into) {
        for (RoleFieldAuthVo.MenuNode node : nodes) {
            node.getFields().forEach(field -> into.add(field.getFieldId()));
            collectFieldIds(node.getChildren(), into);
        }
    }

    /**
     * 校验普通角色的字段授权不超出权限集合的字段池；
     * 模板角色（权限集合/管理员角色）是字段池的定义方，豁免校验
     */
    private void validateAssignable(RoleFieldRelDto dto, Role targetRole) {
        if (!RoleCategoryEnum.NORMAL_ROLE.getCode().equals(targetRole.getRoleCategory())
                || CollUtil.isEmpty(dto.getFieldIdList())) {
            return;
        }
        Role permSet = roleService.getPermSetRoleOfCurrentOperator();
        ArgumentAssert.notNull(permSet, "当前组织性质未配置权限集合角色，无法分配字段权限");

        Set<Long> pool = new HashSet<>(findFieldIdsByRoleId(permSet.getId()));
        List<Long> exceeded = dto.getFieldIdList().stream()
                .filter(fieldId -> !pool.contains(fieldId)).distinct().toList();
        if (CollUtil.isNotEmpty(exceeded)) {
            List<String> names = resourceFieldMapper.selectListByIds(exceeded).stream()
                    .map(ResourceField::getName).toList();
            ArgumentAssert.isTrue(false, "字段[{}]超出权限集合可分配范围", CollUtil.join(names, "、"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByRoleIds(Collection<? extends Serializable> roleIdList) {
        if (CollUtil.isEmpty(roleIdList)) {
            return;
        }
        mapper.deleteByQuery(QueryWrapper.create()
                .where(RoleFieldRel::getRoleId).in(roleIdList));
        // 字段受限关系变更影响这些角色下所有用户的字段受限集
        invalidateUserFieldPermCacheByRoleIds(roleIdList);
    }

    @Override
    public void invalidateUserFieldPermCacheByRoleIds(Collection<? extends Serializable> roleIdList) {
        if (CollUtil.isEmpty(roleIdList)) {
            return;
        }
        List<Long> userIds = userRoleRelMapper.selectListByQuery(QueryWrapper.create()
                        .select(UserRoleRel::getUserId)
                        .where(UserRoleRel::getRoleId).in(roleIdList))
                .stream().map(UserRoleRel::getUserId).distinct().toList();
        delUserCache(userIds);
    }

    @Override
    public void invalidateUserFieldPermCacheByFieldIds(Collection<Long> fieldIdList) {
        if (CollUtil.isEmpty(fieldIdList)) {
            return;
        }
        List<Long> roleIds = mapper.selectListByQuery(QueryWrapper.create()
                        .select(RoleFieldRel::getRoleId)
                        .where(RoleFieldRel::getFieldId).in(fieldIdList))
                .stream().map(RoleFieldRel::getRoleId).distinct().toList();
        invalidateUserFieldPermCacheByRoleIds(roleIds);
    }

    private void delUserCache(Collection<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        List<CacheKey> keys = userIds.stream().map(UserFieldPermCacheKeyBuilder::build).toList();
        cacheOps.del(keys);
    }
}
