# 用户体系重构 · 计划 3：可见性、硬保护与绑定校验 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按 spec 第 5、7 节与第 4 节绑定校验规则，实现：①查询可见性过滤（组织/用户/岗位/角色用户按操作者身份过滤）②运营者与内置根公司的硬保护（service 层统一拦截）③角色用户绑定的"公司性质 = 角色性质"校验。

**Architecture:** 两个基础组件先行——`SystemProtectService`（硬保护判定：是否运营者用户/受保护角色/内置组织）与 `OrgVisibilityService`（当前操作人身份 → 可见组织树根节点），所有拦截与过滤点复用这两个组件，规则不集中散落。过滤一律在查询组装处（Controller/Service 的 QueryWrapper 构建点）追加条件，写入口拦截在各 Service 的既有钩子/方法首行 fail fast。

**Tech Stack:** Java 17 / Spring Boot 3 / MyBatis-Flex / sa-token（ContextUtil.getUserId）

## Global Constraints

- **禁止任何 git add / commit / push 操作**。任务完成检查点 = 编译通过 + 测试通过。
- 注释简体中文；最大行长度 100 字符；使用 Lombok；不引入新依赖。
- 本计划不删除任何文件。
- 编译命令（仓库根目录 `/Users/tangyh/gitee/mdp`）：`mvn -q clean compile -DskipTests`
- 计划 1/2 已产出可直接使用：`UserIdentityService.getIdentity/mapIdentity` + `UserIdentityEnum`（4 身份）、`BuiltInOrgId`（4 个内置组织 ID）、`RoleCode.OPERATIONS_ADMIN` 等 8 常量、`OrgNatureEnum`。
- 当前操作人获取惯例：`top.mddata.base.util.ContextUtil.getUserId()`。
- 错误处理：fail fast，用 `top.mddata.base.utils.ArgumentAssert` 抛出带具体信息的异常，严禁吞异常。

## spec 规则摘录（实现依据，逐条来自设计文档）

**第 7 节可见性**：
| 页面 | 运营者 | 开发者管理员 | 开发者 | 普通用户 |
|---|---|---|---|---|
| 组织管理 | 全部三棵树 | 仅开发者平台子树 | 无入口 | 仅总公司子树 |
| 用户管理 | 全部用户 | 全体开发者（开发者平台树） | 仅自己 | 总公司树内用户（不含运营者、开发者） |
| 角色管理-角色用户 | 全部用户 | 全体开发者 | 无入口 | 全部普通用户 |

**第 5 节硬保护**：
- 持有「运营管理员」角色（code=`OPERATIONS_ADMIN`）的用户：不可被删除、禁用、解绑角色；除本人外不可被重置密码。
- 「运营管理员」角色本身：不可被删除、禁用、减少授权（授权改动整体禁止，含模板入口）。
- 内置组织（3 根公司 + 默认部门）：不可删除、不可移动；根公司不可被改为非根（parentId 必须保持 null）、不可修改组织性质。

**第 4 节绑定校验**：角色用户绑定时，用户所属顶级公司的性质必须等于角色的 orgNature（防身份漂移）。

**第 8 节岗位**：岗位查询按可见树过滤；写入口校验 orgId ∈ 可见树（写入口校验放计划内 Task 5，查询过滤 Task 4）。

## 关键实现事实（已勘察，执行者不必重新调查）

- `OrgController.tree`（console-web）:145 有明确 TODO 注释描述本计划的可见性规则；tree/page/list 均在 Controller 直接组装 QueryWrapper。
- `UserController.pageByRoleId`（角色用户 Tab）:189 用 exists/notExists 子查询按 roleId 过滤，不过滤身份。
- `PositionController.page/list` 直接由 Controller 组装 wrapper，无任何过滤。
- 写入口现状：`UserServiceImpl.removeByIds(:313)`、`updateBefore(:168)`、`resetPassword(:254)`、`unlock(:243)`；`RoleServiceImpl.removeByIds`（先删 rel 再删角色）、`saveBefore/updateBefore`；`RoleTemplateServiceImpl` 有独立 saveBefore/updateBefore/delete 入口（运营管理员角色 template_role=true，模板页也能删改它，**必须覆盖**）；`RoleResourceRelServiceImpl.saveRoleResource(:73)`；`RoleAppRelService.saveByDto/delete`；`UserRoleRelServiceImpl.saveByDto`（增）/`delete`（解绑）。
- `UserRoleRelDto` 结构：roleId + userIdList。
- sa-token session 中已有 `TOP_COMPANY_NATURE`（AuthServiceImpl 登录时写入），但可见性判定统一走 `UserIdentityService.getIdentity`（角色为准），不读 session，避免双口径。

---

### Task 1: SystemProtectService（硬保护判定组件）

**Files:**
- Create: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/organization/SystemProtectService.java`
- Create: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/organization/impl/SystemProtectServiceImpl.java`
- Test: `mdp-apps/md-console/console-service/src/test/java/top/mddata/console/service/organization/BuiltInOrgCheckTest.java`

**Interfaces:**
- Produces（Task 2/3/5 全部拦截点复用）：
  - `boolean SystemProtectService.isOperationsAdminUser(Long userId)`（持有启用状态 OPERATIONS_ADMIN 角色）
  - `void SystemProtectService.checkUsersNotProtected(Collection<Long> userIds, String action)` — 任一用户是运营者则抛出 `"{}失败：用户[{}]是运营者，受系统保护"`
  - `void SystemProtectService.checkRoleNotProtected(Long roleId, String action)` — roleId 是 OPERATIONS_ADMIN 角色则抛出 `"{}失败：角色[运营管理员]受系统保护"`
  - `void SystemProtectService.checkOrgNotBuiltIn(Collection<Long> orgIds, String action)` — 任一 id 属于内置组织（BuiltInOrgId 4 个值）则抛出 `"{}失败：组织[{}]是系统内置组织，受系统保护"`
  - `static boolean SystemProtectService.isBuiltInOrg(Long orgId)`（纯函数）

- [ ] **Step 1: 写失败测试**

```java
package top.mddata.console.service.organization;

import org.junit.jupiter.api.Test;
import top.mddata.common.constant.BuiltInOrgId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 内置组织判定测试：3 根公司 + 默认部门受保护，其余组织不拦截。
 */
class BuiltInOrgCheckTest {

    @Test
    void 内置组织全部命中() {
        assertTrue(SystemProtectService.isBuiltInOrg(BuiltInOrgId.OPERATIONS_CENTER));
        assertTrue(SystemProtectService.isBuiltInOrg(BuiltInOrgId.DEVELOPER_PLATFORM));
        assertTrue(SystemProtectService.isBuiltInOrg(BuiltInOrgId.HEAD_COMPANY));
        assertTrue(SystemProtectService.isBuiltInOrg(BuiltInOrgId.DEFAULT_DEPT));
    }

    @Test
    void 普通组织与空值不命中() {
        assertFalse(SystemProtectService.isBuiltInOrg(123456L));
        assertFalse(SystemProtectService.isBuiltInOrg(null));
    }
}
```

Run: `mvn -q test -pl mdp-apps/md-console/console-service -Dtest=BuiltInOrgCheckTest` → 编译失败（类不存在）。

- [ ] **Step 2: 创建 SystemProtectService 接口**

```java
package top.mddata.console.service.organization;

import top.mddata.common.constant.BuiltInOrgId;

import java.util.Collection;
import java.util.Set;

/**
 * 系统硬保护 服务层。
 *
 * <p>保护规则见 docs/用户体系/用户角色组织权限体系设计.md 第 5 节：
 * 运营者（持有运营管理员角色的用户）、运营管理员角色本身、内置组织，
 * 在各写入口统一拦截，前端按钮隐藏仅体验优化，真正的保护在这里。</p>
 */
public interface SystemProtectService {

    /**
     * 用户是否运营者（持有启用状态的运营管理员角色）
     *
     * @param userId 用户id
     * @return true=运营者
     */
    boolean isOperationsAdminUser(Long userId);

    /**
     * 校验用户列表不含运营者，含则 fail fast
     *
     * @param userIds 待操作用户id
     * @param action  操作描述（用于错误消息，如"删除用户"）
     */
    void checkUsersNotProtected(Collection<Long> userIds, String action);

    /**
     * 校验角色不是运营管理员角色，是则 fail fast
     *
     * @param roleId 角色id
     * @param action 操作描述
     */
    void checkRoleNotProtected(Long roleId, String action);

    /**
     * 校验组织列表不含内置组织，含则 fail fast
     *
     * @param orgIds 待操作组织id
     * @param action 操作描述
     */
    void checkOrgNotBuiltIn(Collection<Long> orgIds, String action);

    /**
     * 是否系统内置组织（3 根公司 + 默认部门）
     *
     * @param orgId 组织id
     * @return true=内置组织
     */
    static boolean isBuiltInOrg(Long orgId) {
        if (orgId == null) {
            return false;
        }
        return Set.of(BuiltInOrgId.OPERATIONS_CENTER, BuiltInOrgId.DEVELOPER_PLATFORM,
                BuiltInOrgId.HEAD_COMPANY, BuiltInOrgId.DEFAULT_DEPT).contains(orgId);
    }
}
```

- [ ] **Step 3: 创建 SystemProtectServiceImpl**

依赖（均已存在）：`UserIdentityService`（计划 2）、`top.mddata.console.service.permission.RoleService`（`getOne(QueryWrapper)` 可用）、`top.mddata.base.utils.ArgumentAssert`。

```java
package top.mddata.console.service.organization.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.constant.RoleCode;
import top.mddata.common.enumeration.organization.UserIdentityEnum;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.service.organization.SystemProtectService;
import top.mddata.console.service.organization.UserIdentityService;
import top.mddata.console.service.permission.RoleService;

import java.util.Collection;

/**
 * 系统硬保护 服务层实现。
 */
@Service
@RequiredArgsConstructor
public class SystemProtectServiceImpl implements SystemProtectService {
    private final UserIdentityService userIdentityService;
    private final RoleService roleService;

    @Override
    @Transactional(readOnly = true)
    public boolean isOperationsAdminUser(Long userId) {
        if (userId == null) {
            return false;
        }
        return UserIdentityEnum.OPERATIONS_ADMIN == userIdentityService.getIdentity(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public void checkUsersNotProtected(Collection<Long> userIds, String action) {
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        userIdentityService.mapIdentity(userIds).forEach((userId, identity) ->
                ArgumentAssert.isFalse(UserIdentityEnum.OPERATIONS_ADMIN == identity,
                        "{}失败：用户[{}]是运营者，受系统保护", action, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public void checkRoleNotProtected(Long roleId, String action) {
        if (roleId == null) {
            return;
        }
        Role role = roleService.getOne(QueryWrapper.create().eq(Role::getId, roleId));
        ArgumentAssert.isFalse(role != null && RoleCode.OPERATIONS_ADMIN.equals(role.getCode()),
                "{}失败：角色[运营管理员]受系统保护", action);
    }

    @Override
    public void checkOrgNotBuiltIn(Collection<Long> orgIds, String action) {
        if (CollUtil.isEmpty(orgIds)) {
            return;
        }
        orgIds.forEach(orgId -> ArgumentAssert.isFalse(SystemProtectService.isBuiltInOrg(orgId),
                "{}失败：组织[{}]是系统内置组织，受系统保护", action, orgId));
    }
}
```

- [ ] **Step 4: 测试通过 + 编译检查点**

Run: `mvn -q test -pl mdp-apps/md-console/console-service -Dtest=BuiltInOrgCheckTest` → PASS
Run: `mvn -q clean compile -DskipTests` → BUILD SUCCESS

---

### Task 2: 用户域硬保护

**Files:**
- Modify: `mdp-apps/md-console/console-service/.../organization/impl/UserServiceImpl.java`（removeByIds、updateBefore、resetPassword）

**Interfaces:**
- Consumes: `SystemProtectService`（Task 1）

- [ ] **Step 1: UserServiceImpl 注入 SystemProtectService 并加 3 处拦截**

1. 新增字段 `private final SystemProtectService systemProtectService;`
2. `removeByIds` 方法体**第一行**插入：
   ```java
   systemProtectService.checkUsersNotProtected(
           idList.stream().map(id -> Long.valueOf(String.valueOf(id))).toList(), "删除用户");
   ```
3. `updateBefore` 中 `super.updateBefore(updateDto)` 之后插入（仅禁用场景拦截，其他字段修改放行）：
   ```java
   User updateEntity = (User) updateDto;
   if (Boolean.FALSE.equals(updateEntity.getState())) {
       systemProtectService.checkUsersNotProtected(List.of(updateEntity.getId()), "禁用用户");
   }
   ```
   注意：先看 updateDto 实际类型（UserUpdateDto 转 User 的时机），若 super.updateBefore 返回的 sysUser 才有 id/state，则用 sysUser 的 getId()/getState() 判断，在报告中说明采用哪种。
4. `resetPassword` 方法体第一行插入（本人修改自己密码的入口在 workbench updatePassword，此接口是管理员重置他人密码）：
   ```java
   systemProtectService.checkUsersNotProtected(List.of(data.getId()), "重置密码");
   ```

unlock 不拦截（spec 未列；运营者账号不会被锁定，因为其不可被禁用）。

- [ ] **Step 2: 编译检查点**

Run: `mvn -q clean compile -DskipTests` → BUILD SUCCESS

---

### Task 3: 角色域硬保护 + 角色用户绑定/解绑校验

**Files:**
- Modify: `mdp-apps/md-console/console-service/.../permission/impl/RoleServiceImpl.java`（removeByIds、updateBefore）
- Modify: `mdp-apps/md-console/console-service/.../permission/impl/RoleTemplateServiceImpl.java`（删除/更新入口）
- Modify: `mdp-apps/md-console/console-service/.../permission/impl/RoleResourceRelServiceImpl.java:73`（saveRoleResource）
- Modify: `mdp-apps/md-console/console-service/.../permission/impl/RoleAppRelServiceImpl.java`（saveByDto、delete）
- Modify: `mdp-apps/md-console/console-service/.../organization/impl/UserRoleRelServiceImpl.java`（saveByDto、delete）

**Interfaces:**
- Consumes: `SystemProtectService`（Task 1）、`UserIdentityService`（计划 2）
- Produces: `void UserRoleRelService.checkRoleUserNatureMatch(Long roleId, Collection<Long> userIds)`（绑定性质校验，供 saveByDto 与后续计划复用）

> **执行期架构修正（2026-09-24，控制器决策）**：按简报原文注入 Service 会产生构造器循环依赖（RoleServiceImpl → SystemProtectServiceImpl → UserIdentityServiceImpl → RoleServiceImpl 等 3 个环）。修正：SystemProtectServiceImpl 改为只依赖 `RoleMapper`/`UserRoleRelMapper`（直接查 OPERATIONS_ADMIN 角色持有关系，与身份判定在 spec §5 数据契约下等价）；checkRoleUserNatureMatch 的角色查询同样用 RoleMapper。不使用 @Lazy。

- [ ] **Step 1: RoleServiceImpl 拦截（2 处）**

1. 注入 `SystemProtectService`。
2. `removeByIds` 第一行：`idList.forEach(id -> systemProtectService.checkRoleNotProtected(Long.valueOf(String.valueOf(id)), "删除角色"));`
3. `updateBefore` 中 entity 构建后：若 `entity.getId()` 对应角色 code 是 OPERATIONS_ADMIN 且 `Boolean.FALSE.equals(entity.getState())`，抛 `"禁用角色失败：角色[运营管理员]受系统保护"`（用 roleService 自身查库取旧 code 判断，避免依赖前端传 code）。

- [ ] **Step 2: RoleTemplateServiceImpl 拦截**

阅读该类的删除/更新入口（delete/removeByIds、updateBefore），与 Step 1 相同的 2 条规则各加一份（运营管理员角色 template_role=true，模板页同样能触达它）。注入 `SystemProtectService`（console-service 同模块直接注入）。

- [ ] **Step 3: 授权入口拦截（3 处，"减少授权"整体禁止）**

1. `RoleResourceRelServiceImpl.saveRoleResource` 第一行：`systemProtectService.checkRoleNotProtected(dto.getRoleId(), "修改角色功能权限");`
2. `RoleAppRelServiceImpl.saveByDto` 第一行：`systemProtectService.checkRoleNotProtected(dto.getRoleId(), "修改角色应用权限");`
3. `RoleAppRelServiceImpl.delete` 第一行：`systemProtectService.checkRoleNotProtected(dto.getRoleId(), "修改角色应用权限");`

（spec 第 5 节：运营管理员角色不可减少授权。其已拥有全量权限，授权改动整体禁止是最简且安全的解释。）

- [ ] **Step 4: UserRoleRelServiceImpl 绑定校验 + 解绑保护**

1. 注入 `SystemProtectService`、`RoleService`、`top.mddata.console.service.organization.UserOrgRelService`、`top.mddata.console.service.organization.OrgService`。
2. `UserRoleRelService` 接口追加方法：

```java
    /**
     * 校验角色用户绑定：用户所属树的组织性质必须等于角色的组织性质（防身份漂移）。
     * 用户无任何组织关系、或角色不存在时 fail fast。
     *
     * @param roleId  角色id
     * @param userIds 待绑定用户id
     */
    void checkRoleUserNatureMatch(Long roleId, Collection<Long> userIds);
```

3. 实现（放 UserRoleRelServiceImpl）：

```java
    @Override
    @Transactional(readOnly = true)
    public void checkRoleUserNatureMatch(Long roleId, Collection<Long> userIds) {
        ArgumentAssert.notNull(roleId, "角色id不能为空");
        if (CollUtil.isEmpty(userIds)) {
            return;
        }
        Role role = roleService.getOne(QueryWrapper.create().eq(Role::getId, roleId));
        ArgumentAssert.notNull(role, "角色[{}]不存在", roleId);

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
```

说明：用户的每条组织关系都须与角色性质一致（跨树用户任何一条不匹配都拒绝，从严防漂移）。`UserOrgRel`、`Org` 的 import 按实际补充。

4. `saveByDto` 第一行插入：
   ```java
   checkRoleUserNatureMatch(dto.getRoleId(), dto.getUserIdList());
   ```
5. `delete`（解绑）第一行插入：
   ```java
   systemProtectService.checkUsersNotProtected(dto.getUserIdList(), "解绑角色");
   ```

- [ ] **Step 5: 编译检查点**

Run: `mvn -q clean compile -DskipTests` → BUILD SUCCESS

---

### Task 4: 查询可见性过滤

**Files:**
- Create: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/organization/OrgVisibilityService.java`
- Create: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/organization/impl/OrgVisibilityServiceImpl.java`
- Modify: `mdp-apps/md-console/console-web/.../organization/OrgController.java`（tree、page、list）
- Modify: `mdp-apps/md-console/console-web/.../organization/UserController.java`（page、list、pageByRoleId）
- Modify: `mdp-apps/md-console/console-web/.../organization/PositionController.java`（page、list）
- Test: `mdp-apps/md-console/console-service/src/test/java/top/mddata/console/service/organization/VisibleRootOrgTest.java`

**Interfaces:**
- Produces:
  - `List<Long> OrgVisibilityService.currentVisibleRootOrgIds()` — 当前操作人可见的组织树根节点；**返回 null 表示不限制（运营者）**；空列表表示什么都不可见（开发者）
  - `UserIdentityEnum OrgVisibilityService.currentIdentity()`
  - `static List<Long> OrgVisibilityService.visibleRootOrgIds(UserIdentityEnum identity)`（纯函数，运营者→null 的语义用"调用方特判"表达：纯函数只映射 开发者管理员→[开发者平台]、普通用户→[总公司]、开发者→[]、运营者→null）

- [ ] **Step 1: 写失败测试**

```java
package top.mddata.console.service.organization;

import org.junit.jupiter.api.Test;
import top.mddata.common.constant.BuiltInOrgId;
import top.mddata.common.enumeration.organization.UserIdentityEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 可见组织树根节点映射测试：规则见 spec 第 7 节。
 */
class VisibleRootOrgTest {

    @Test
    void 运营者不限制返回null() {
        assertNull(OrgVisibilityService.visibleRootOrgIds(UserIdentityEnum.OPERATIONS_ADMIN));
    }

    @Test
    void 开发者管理员仅开发者平台() {
        assertEquals(List.of(BuiltInOrgId.DEVELOPER_PLATFORM),
                OrgVisibilityService.visibleRootOrgIds(UserIdentityEnum.DEVELOPER_ADMIN));
    }

    @Test
    void 普通用户仅总公司() {
        assertEquals(List.of(BuiltInOrgId.HEAD_COMPANY),
                OrgVisibilityService.visibleRootOrgIds(UserIdentityEnum.USER));
    }

    @Test
    void 开发者什么都不可见() {
        assertTrue(OrgVisibilityService.visibleRootOrgIds(UserIdentityEnum.DEVELOPER).isEmpty());
    }
}
```

- [ ] **Step 2: 创建 OrgVisibilityService**

```java
package top.mddata.console.service.organization;

import top.mddata.common.constant.BuiltInOrgId;
import top.mddata.common.enumeration.organization.UserIdentityEnum;

import java.util.List;

/**
 * 组织可见性 服务层。
 *
 * <p>可见性规则见 docs/用户体系/用户角色组织权限体系设计.md 第 7 节：
 * 运营者可见全部；开发者管理员仅开发者平台子树；普通用户仅总公司子树；开发者不可见。</p>
 */
public interface OrgVisibilityService {

    /**
     * 当前操作人身份
     *
     * @return 用户身份
     */
    UserIdentityEnum currentIdentity();

    /**
     * 当前操作人可见的组织树根节点id列表。
     *
     * @return null=不限制（运营者）；空列表=什么都不可见（开发者）；其余=可见根节点
     */
    List<Long> currentVisibleRootOrgIds();

    /**
     * 身份到可见根节点的映射（纯函数）
     *
     * @param identity 用户身份
     * @return null=不限制；空列表=不可见；其余=可见根节点
     */
    static List<Long> visibleRootOrgIds(UserIdentityEnum identity) {
        if (identity == null) {
            return List.of();
        }
        return switch (identity) {
            case OPERATIONS_ADMIN -> null;
            case DEVELOPER_ADMIN -> List.of(BuiltInOrgId.DEVELOPER_PLATFORM);
            case USER -> List.of(BuiltInOrgId.HEAD_COMPANY);
            case DEVELOPER -> List.of();
        };
    }
}
```

实现类：

```java
package top.mddata.console.service.organization.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.util.ContextUtil;
import top.mddata.common.enumeration.organization.UserIdentityEnum;
import top.mddata.console.service.organization.OrgVisibilityService;
import top.mddata.console.service.organization.UserIdentityService;

import java.util.List;

/**
 * 组织可见性 服务层实现。
 */
@Service
@RequiredArgsConstructor
public class OrgVisibilityServiceImpl implements OrgVisibilityService {
    private final UserIdentityService userIdentityService;

    @Override
    public UserIdentityEnum currentIdentity() {
        return userIdentityService.getIdentity(ContextUtil.getUserId());
    }

    @Override
    public List<Long> currentVisibleRootOrgIds() {
        return OrgVisibilityService.visibleRootOrgIds(currentIdentity());
    }
}
```

- [ ] **Step 3: OrgController 三处过滤（tree/page/list）**

注入 `OrgVisibilityService`（console-web 已能注入 console-service 的 bean）。三个方法在组装 wrapper 后、查询前统一追加（抽一个私有方法）：

```java
    /**
     * 追加组织可见性过滤：null=不限制；空列表=查不到任何数据；其余按树路径过滤
     */
    private void appendVisibilityFilter(QueryWrapper wrapper) {
        List<Long> rootIds = orgVisibilityService.currentVisibleRootOrgIds();
        if (rootIds == null) {
            return;
        }
        if (rootIds.isEmpty()) {
            wrapper.where("1 = 0");
            return;
        }
        wrapper.and(qw -> {
            for (int i = 0; i < rootIds.size(); i++) {
                qw.or(Org::getTreePath).like("/" + rootIds.get(i) + "/");
            }
            return qw;
        });
    }
```

说明：根公司自身 tree_path 为 `/{id}/`，后代为 `/{id}/...`，`like('/{id}/')` 同时覆盖根与子孙。若 `wrapper.and(...)` 的 lambda 写法与项目 MyBatis-Flex 版本不兼容，改为循环 `wrapper.or(Org::getTreePath).like(...)` 并在报告中注明（注意多条件时的括号语义，务必用 and 包裹 or 组）。

- [ ] **Step 4: UserController 三处过滤（page/list/pageByRoleId）**

注入 `OrgVisibilityService`。

1. page/list：在现有 wrapper 上追加"用户必须属于可见树内的组织"（exists 子查询）：

```java
    /**
     * 追加用户可见性过滤：只显示可见组织树内的用户；
     * 非运营者再排除运营者账号（spec 第 7 节：普通用户看不到运营者和开发者——
     * 开发者不在总公司树内已天然排除，这里只需显式排除运营者）。
     */
    private void appendUserVisibilityFilter(QueryWrapper wrapper) {
        List<Long> rootIds = orgVisibilityService.currentVisibleRootOrgIds();
        if (rootIds == null) {
            return;
        }
        if (rootIds.isEmpty()) {
            // 开发者无用户管理入口，兜底只能看自己
            wrapper.eq(User::getId, ContextUtil.getUserId());
            return;
        }
        wrapper.and(qw -> {
            for (Long rootId : rootIds) {
                QueryWrapper sub = QueryWrapper.create().select("1").from(UserOrgRel.class)
                        .innerJoin(Org.class).on(UserRoleRel::getOrgId, Org::getId)
                        .where(UserOrgRel::getUserId).eq(User::getId)
                        .and(Org::getTreePath).like("/" + rootId + "/");
                qw.or(QueryMethods.exists(sub));
            }
            return qw;
        });
    }
```

注意：上面 innerJoin 的 on 条件应为 `UserOrgRel::getOrgId, Org::getId`（仔细核对，UserRoleRel 是笔误）；`UserOrgRel`、`Org`、`QueryMethods`、`ContextUtil` 的 import 按 UserController 现状补充（`UserRoleRel`、`QueryMethods` 已在 pageByRoleId 用过）。

2. pageByRoleId（角色用户 Tab）：角色性质决定候选树——先按 roleId 查角色的 orgNature（注入 `RoleService` 或 `SystemProtectService` 不适用，直接注入 `top.mddata.console.service.permission.RoleService`），把"可见根节点"改为"角色 orgNature 对应的内置根节点"（用 `OrgVisibilityService.visibleRootOrgIds` 不可直接复用，另写私有映射：1→[总公司]、90→[开发者平台]、99→[运营中心]，即 `BuiltInOrgId` 三个根公司，注意**不含默认部门**——默认部门在总公司树内，like 总公司路径已覆盖）；然后复用第 1 点的 exists 过滤逻辑追加到 wrapper。运营者操作时不额外排除任何人；非运营者操作时无需额外排除运营者（运营者在运营中心树，不会被总公司/开发者平台路径命中）。

3. list：与 page 相同的过滤。

- [ ] **Step 5: PositionController 两处过滤（page/list）**

注入 `OrgVisibilityService`。岗位通过 `org_id` 挂在组织上，过滤岗位所属组织在可见树内（exists 子查询，Org 表 tree_path like），空列表→`1 = 0`，null→不限制：

```java
    /**
     * 追加岗位可见性过滤：岗位所属组织必须在可见树内
     */
    private void appendPositionVisibilityFilter(QueryWrapper wrapper) {
        List<Long> rootIds = orgVisibilityService.currentVisibleRootOrgIds();
        if (rootIds == null) {
            return;
        }
        if (rootIds.isEmpty()) {
            wrapper.where("1 = 0");
            return;
        }
        wrapper.and(qw -> {
            for (Long rootId : rootIds) {
                QueryWrapper sub = QueryWrapper.create().select("1").from(Org.class)
                        .where(Org::getId).eq(Position::getOrgId)
                        .and(Org::getTreePath).like("/" + rootId + "/");
                qw.or(QueryMethods.exists(sub));
            }
            return qw;
        });
    }
```

- [ ] **Step 6: 测试通过 + 编译检查点**

Run: `mvn -q test -pl mdp-apps/md-console/console-service -Dtest=VisibleRootOrgTest,BuiltInOrgCheckTest` → PASS
Run: `mvn -q clean compile -DskipTests` → BUILD SUCCESS

---

### Task 5: 岗位写入口校验 + 全量验证

**Files:**
- Modify: `mdp-apps/md-console/console-service/.../organization/impl/PositionServiceImpl.java`（saveBefore/updateBefore）
- Modify: `mdp-apps/md-console/console-service/.../organization/impl/OrgServiceImpl.java`（removeByIds、move、updateBefore 内置组织保护）

**Interfaces:**
- Consumes: `SystemProtectService`（Task 1）、`OrgVisibilityService`（Task 4）

- [ ] **Step 1: OrgServiceImpl 内置组织保护（3 处）**

注入 `SystemProtectService`。
1. `removeByIds` 第一行：`systemProtectService.checkOrgNotBuiltIn(idList.stream().map(id -> Long.valueOf(String.valueOf(id))).toList(), "删除组织");`
2. `move` 第一行：`systemProtectService.checkOrgNotBuiltIn(List.of(sourceId), "移动组织");`
3. `updateBefore` 中：若 `org.getId()` 是内置组织，追加两条校验——`ArgumentAssert.isTrue(MyTreeUtil.isRoot(org.getParentId()), "修改组织失败：内置组织[{}]必须保持根节点", org.getId());`（默认部门不是根节点，其父必须保持为总公司：`ArgumentAssert.equals(BuiltInOrgId.HEAD_COMPANY, org.getParentId(), ...)`——注意区分：3 根公司要求 parentId 为根，默认部门要求 parentId=总公司）。组织性质已由计划 1 终审修复保证根节点不被 update 改写，无需重复校验。

- [ ] **Step 2: PositionServiceImpl 写入口校验（saveBefore/updateBefore）**

注入 `OrgVisibilityService`、`OrgService`。两个钩子各加：

```java
        Long orgId = entity.getOrgId();
        if (orgId != null) {
            List<Long> rootIds = orgVisibilityService.currentVisibleRootOrgIds();
            if (rootIds != null) {
                Org org = orgService.getByIdCache(orgId);
                ArgumentAssert.notNull(org, "所属组织[{}]不存在", orgId);
                boolean visible = rootIds.stream().anyMatch(rootId ->
                        org.getTreePath() != null && org.getTreePath().startsWith("/" + rootId + "/"));
                ArgumentAssert.isTrue(visible, "无权在组织[{}]下维护岗位", org.getName());
            }
        }
```

- [ ] **Step 3: 全量验证**

Run: `mvn -q clean compile -DskipTests` → BUILD SUCCESS
Run: `mvn test -pl mdp-apps/md-console/console-service,mdp-apps/md-public/md-common-pojo` → 全部 PASS
Run: `mvn -q checkstyle:check -pl mdp-apps/md-console/console-service,mdp-apps/md-console/console-web` → 退出码 0

- [ ] **Step 4: 人工冒烟（由用户执行）**

1. `ops_admin/admin` 登录：组织管理可见三棵树；用户管理可见全部用户。
2. `admin/admin`（总公司管理员）登录：组织管理仅总公司子树；用户管理看不到 ops_admin/open_admin；岗位管理仅总公司树岗位。
3. 尝试用 admin 删除 ops_admin → 报"受系统保护"；删除角色"运营管理员"→ 报"受系统保护"；删除"总公司"→ 报"系统内置组织"。
4. 角色管理给性质 1 的角色绑定开发者账号 → 报"组织性质不一致"。

---

## Self-Review 记录

- **Spec 覆盖**：§7 可见性 → Task 4；§5 硬保护 → Task 1/2/3/5；§4 绑定校验 → Task 3 Step 4；§8 岗位 → Task 4 Step 5 + Task 5 Step 2。spec 改造点 8、9、10 全覆盖。
- **占位符**：Task 4 Step 4 标注了一处笔误修正（UserRoleRel→UserOrgRel），执行时以正文说明为准，非遗漏。
- **类型一致性**：`SystemProtectService` 四个方法在 Task 1 定义、Task 2/3/5 消费；`OrgVisibilityService` 两个方法 + 纯函数在 Task 4 定义、Task 5 Step 2 消费。
- **已知留尾**：开发者直接调 console 接口的兜底是"只看自己"（Spec 第 7 节"仅自己"），菜单级拦截由前端 + 应用权限保证；用户管理"普通用户不含开发者"由树隔离天然保证，无显式排除逻辑。
