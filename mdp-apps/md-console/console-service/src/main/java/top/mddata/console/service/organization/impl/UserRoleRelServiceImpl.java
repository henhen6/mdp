package top.mddata.console.service.organization.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.interfaces.echo.EchoService;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.console.permission.UserFieldPermCacheKeyBuilder;
import top.mddata.common.cache.console.permission.UserResourceApiCacheKeyBuilder;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.UserOrgRel;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.enumeration.organization.UserIdentityEnum;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.common.mapper.UserRoleRelMapper;
import top.mddata.console.dto.organization.UserRoleRelDto;
import top.mddata.console.dto.organization.UserRoleSaveDto;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.mapper.permission.RoleMapper;
import top.mddata.console.service.organization.OrgService;
import top.mddata.console.service.organization.SystemProtectService;
import top.mddata.console.service.organization.UserIdentityService;
import top.mddata.console.service.organization.UserOrgRelService;
import top.mddata.console.service.organization.UserRoleRelService;
import top.mddata.console.vo.organization.AssignableRoleVo;
import top.mddata.console.vo.permission.RoleVo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色关联 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 15:50:00
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserRoleRelServiceImpl extends SuperServiceImpl<UserRoleRelMapper, UserRoleRel> implements UserRoleRelService {

    private final SystemProtectService systemProtectService;
    private final RoleMapper roleMapper;
    private final UserOrgRelService userOrgRelService;
    private final OrgService orgService;
    private final UserIdentityService userIdentityService;
    private final EchoService echoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByRoleIds(Collection<? extends Serializable> roleIdList) {
        if (CollUtil.isEmpty(roleIdList)) {
            return;
        }
        List<Long> userIds = list(QueryWrapper.create().in(UserRoleRel::getRoleId, roleIdList))
                .stream()
                .map(UserRoleRel::getUserId)
                .distinct()
                .toList();
        super.remove(QueryWrapper.create().in(UserRoleRel::getRoleId, roleIdList));
        // 角色级联删除关联，失效受影响用户的接口放行集缓存
        invalidateUserResourceApiCache(userIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveByDto(UserRoleRelDto dto) {
        checkRoleUserNatureMatch(dto.getRoleId(), dto.getUserIdList());

        List<UserRoleRel> existList = list(QueryWrapper.create().eq(UserRoleRel::getRoleId, dto.getRoleId()));
        List<Long> existUserIdList = existList.stream().map(UserRoleRel::getUserId).distinct().toList();

        List<UserRoleRel> saveList = dto.getUserIdList().stream()
                // 已存在的不添加
                .filter(userId -> !existUserIdList.contains(userId))
                .map(userId -> {
                    UserRoleRel rel = new UserRoleRel();
                    rel.setRoleId(dto.getRoleId());
                    rel.setUserId(userId);
                    return rel;
                })
                .collect(Collectors.toList());

        boolean result = super.saveBatch(saveList);
        // 角色绑定变更，失效对应用户的接口放行集缓存
        invalidateUserResourceApiCache(dto.getUserIdList());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(UserRoleRelDto dto) {
        systemProtectService.checkUsersNotProtected(dto.getUserIdList(), "解绑角色");

        Boolean result = super.remove(
                QueryWrapper.create().eq(UserRoleRel::getRoleId, dto.getRoleId())
                        .in(UserRoleRel::getUserId, dto.getUserIdList()));
        // 角色解绑变更，失效对应用户的接口放行集缓存
        invalidateUserResourceApiCache(dto.getUserIdList());
        return result;
    }

    /**
     * 失效指定用户的接口放行集缓存B。
     */
    private void invalidateUserResourceApiCache(Collection<Long> userIdList) {
        if (CollUtil.isEmpty(userIdList)) {
            return;
        }
        List<CacheKey> keys = new ArrayList<>(userIdList.stream()
                .map(UserResourceApiCacheKeyBuilder::build)
                .toList());
        // 用户角色变更同样影响其字段受限集
        userIdList.stream().map(UserFieldPermCacheKeyBuilder::build).forEach(keys::add);
        cacheOps.del(keys);
    }

    @Override
    @Transactional(readOnly = true)
    public void checkRoleUserNatureMatch(Long roleId, Collection<Long> userIds) {
        ArgumentAssert.notNull(roleId, "角色id不能为空");
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        Role role = roleMapper.selectOneByQuery(QueryWrapper.create().eq(Role::getId, roleId));
        ArgumentAssert.notNull(role, "角色[{}]不存在", roleId);
        ArgumentAssert.notNull(role.getOrgNature(), "角色[{}]未配置组织性质，无法执行绑定校验",
                role.getName());

        for (Long userId : userIds) {
            List<UserOrgRel> relList = userOrgRelService.list(
                    QueryWrapper.create().eq(UserOrgRel::getUserId, userId));
            ArgumentAssert.isFalse(CollUtil.isEmpty(relList),
                    "绑定失败：用户[{}]不属于任何组织，无法判定其身份", userId);
            for (UserOrgRel rel : relList) {
                Org org = orgService.getByIdCache(rel.getOrgId());
                ArgumentAssert.isTrue(org != null && role.getOrgNature().equals(org.getNature()),
                        "绑定失败：用户[{}]所属组织性质与角色[{}]的组织性质不一致", userId, role.getName());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findRoleIdByUserId(Long userId) {
        ArgumentAssert.notNull(userId, "用户id不能为空");
        List<Long> roleIds = listUserRoleIds(userId);
        if (CollUtil.isEmpty(roleIds)) {
            return List.of();
        }
        return filterAssignableRoles(roleIds).stream().map(Role::getId).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AssignableRoleVo listAssignableRoles() {
        List<RoleVo> roleList = roleMapper.selectListByQueryAs(
                assignableScopeWrapper(QueryWrapper.create()).orderBy(Role::getCreatedAt, false),
                RoleVo.class);
        // 回显角色分类/组织性质字典显示名，前端分组标题与徽章依赖 echoMap
        echoService.action(roleList);
        return AssignableRoleVo.builder()
                .operationsAdmin(isOperationsAdmin())
                .roleList(roleList)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveUserRole(UserRoleSaveDto dto) {
        ArgumentAssert.notNull(dto.getUserId(), "用户id不能为空");
        systemProtectService.checkUsersNotProtected(List.of(dto.getUserId()), "分配角色");

        List<Long> newRoleIds = dto.getRoleIdList() == null ? List.of()
                : dto.getRoleIdList().stream().distinct().toList();
        if (CollUtil.isNotEmpty(newRoleIds)) {
            // 范围校验 + 组织性质校验
            checkAssignableAndNatureMatch(dto.getUserId(), newRoleIds);
        }

        // 只删除可分配范围内的旧关联，保留模板角色等其他类别绑定
        List<Long> oldRoleIds = filterAssignableRoles(listUserRoleIds(dto.getUserId()))
                .stream().map(Role::getId).toList();
        if (CollUtil.isNotEmpty(oldRoleIds)) {
            super.remove(QueryWrapper.create()
                    .eq(UserRoleRel::getUserId, dto.getUserId())
                    .in(UserRoleRel::getRoleId, oldRoleIds));
        }

        // 走 mapper 直插而非 saveBatch，便于单测 mock（单用户角色数量小，逐条插入可接受）
        for (Long roleId : newRoleIds) {
            UserRoleRel rel = new UserRoleRel();
            rel.setUserId(dto.getUserId());
            rel.setRoleId(roleId);
            mapper.insert(rel);
        }

        // 用户角色变更，失效接口放行集与字段受限集缓存
        invalidateUserResourceApiCache(List.of(dto.getUserId()));
        return true;
    }

    /**
     * 查询用户当前全部角色关联的角色ID。
     */
    private List<Long> listUserRoleIds(Long userId) {
        return list(QueryWrapper.create().eq(UserRoleRel::getUserId, userId))
                .stream().map(UserRoleRel::getRoleId).distinct().toList();
    }

    /**
     * 构造可分配角色范围条件（按当前操作者身份分流）：
     * 运营人员：非模板 + 普通/管理员角色，不限组织性质；
     * 其他身份：非模板 + 普通角色 + 当前公司组织性质（与 RoleController.list 对齐）。
     * 回显、保存删除范围、提交校验、列表端点四处共用，保证口径一致。
     */
    private QueryWrapper assignableScopeWrapper(QueryWrapper wrapper) {
        if (isOperationsAdmin()) {
            wrapper.in(Role::getRoleCategory, RoleCategoryEnum.NORMAL_ROLE.getCode(),
                    RoleCategoryEnum.ADMIN_ROLE.getCode());
        } else {
            wrapper.eq(Role::getTemplateRole, false);
            // 公司性质缺失时 mybatis-flex 会静默忽略 eq 条件导致范围失控，必须 fail fast
            Integer currentNature = ContextUtil.getCurrentCompanyNature();
            ArgumentAssert.notNull(currentNature, "当前公司性质缺失，无法确定可分配角色范围");
            wrapper.eq(Role::getRoleCategory, RoleCategoryEnum.NORMAL_ROLE.getCode())
                    .eq(Role::getOrgNature, currentNature);
        }
        return wrapper;
    }

    /**
     * 运营人员判定：绑定了性质99管理员角色（与平台身份体系一致）。
     */
    private boolean isOperationsAdmin() {
        return UserIdentityEnum.OPERATIONS_ADMIN == userIdentityService.getIdentity(ContextUtil.getUserId());
    }

    /**
     * 在指定角色中过滤出当前操作者可分配的角色（范围见 {@link #assignableScopeWrapper}）。
     */
    private List<Role> filterAssignableRoles(Collection<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return List.of();
        }
        return roleMapper.selectListByQuery(
                assignableScopeWrapper(QueryWrapper.create().in(Role::getId, roleIds)));
    }

    /**
     * 校验提交的角色均为可分配角色，且角色性质与用户每个组织的性质都一致（ALL 语义，防身份漂移）。
     * 规则同 {@link #checkRoleUserNatureMatch}：用户挂在不同性质组织下时，任何角色都无法绑定。
     */
    private void checkAssignableAndNatureMatch(Long userId, Collection<Long> roleIds) {
        List<Role> assignableRoles = filterAssignableRoles(roleIds);
        ArgumentAssert.isTrue(assignableRoles.size() == roleIds.size(),
                "分配失败：存在不可分配的角色（模板角色、权限集合、超出当前身份可授范围或角色不存在）");

        List<UserOrgRel> relList = userOrgRelService.list(
                QueryWrapper.create().eq(UserOrgRel::getUserId, userId));
        ArgumentAssert.isFalse(CollUtil.isEmpty(relList),
                "绑定失败：用户[{}]不属于任何组织，无法判定其身份", userId);

        // 外层角色、内层组织，逐一断言每个组织的性质都等于角色性质
        for (Role role : assignableRoles) {
            ArgumentAssert.notNull(role.getOrgNature(), "角色[{}]未配置组织性质，无法执行绑定校验",
                    role.getName());
            for (UserOrgRel rel : relList) {
                Org org = orgService.getByIdCache(rel.getOrgId());
                ArgumentAssert.isTrue(org != null && role.getOrgNature().equals(org.getNature()),
                        "绑定失败：用户[{}]所属组织性质与角色[{}]的组织性质不一致", userId, role.getName());
            }
        }
    }
}
