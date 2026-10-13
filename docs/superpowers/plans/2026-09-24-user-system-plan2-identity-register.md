# 用户体系重构 · 计划 2：身份判定与注册流程 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按 spec 第 1、6 节实现"身份由角色决定"的判定组件（含批量判定，供计划 3 可见性过滤使用），并把注册流程从"字典+配置间接链"改为"按注册身份直接绑定内置角色与内置组织"，移除 `BUILT_IN_DEVELOPER` 配置链。

**Architecture:** 身份判定规则收敛为一个纯函数（console 侧，可脱离 DB 单测），服务层提供单个/批量判定；注册时 `UserBase.nature`（计划 1 已加的临时载体，本计划定稿语义）直接决定绑定哪个内置组织与内置角色，组织 ID 取自计划 1 的 `BuiltInOrgId` 常量。

**Tech Stack:** Java 17 / Spring Boot 3 / MyBatis-Flex / Lombok

## Global Constraints

- **禁止任何 git add / commit / push 操作**。任务完成检查点 = 编译通过 + 测试通过。
- 所有注释、错误信息用简体中文；代码标识符用英文；最大行长度 100 字符；不引入新依赖。
- 删除文件仅限计划明确列出者；本计划不删除任何文件（仅删除常量与配置行）。
- 编译命令（仓库根目录 `/Users/tangyh/gitee/mdp`）：`mvn -q clean compile -DskipTests`
- spec 身份判定规则（第 1 节）：绑定性质 99 管理员角色 → 运营者；绑定性质 90 管理员角色 → 开发者管理员；绑定性质 90 其他角色 → 开发者；其余 → 普通用户。
- 计划 1 已产出可直接使用的产物：`OrgNatureEnum.HEAD_COMPANY(1)/DEVELOPER(90)/OPERATIONS(99)`、`RoleCode` 8 常量、`BuiltInOrgId.DEFAULT_DEPT/DEVELOPER_PLATFORM`、`UserBase.nature`（`@Column(ignore=true)` 临时载体）。
- 范围外（本计划不做）：用户管理/组织管理可见性过滤（计划 3）、数据权限（计划 4）、`记录.md` 中的多线增长趋势（未列入 spec，不做）。

## 关键背景（执行者必读）

- 注册链路：`workbench AuthServiceImpl.registerBy{Email,Phone,Username}` 已将 `RegisterVO.nature`（`@NotNull`）拷贝到 `User.nature` 载体 → `UserFacade.registerBy*` → `console UserServiceImpl.registerBy*`（3 个方法均执行 `save(user)` → `saveDefOrg(user)` → `saveDefRole(user)`）。
- `UserServiceImpl.saveDefOrg` 现状（计划 1 后）：仅当 `OrgNatureEnum.DEVELOPER.eq(user.getNature())` 时读 `ConfigKey.Console.BUILT_IN_DEVELOPER` 配置绑组织，普通用户不绑任何组织。
- `UserServiceImpl.saveDefRole` 现状（计划 1 后）：查用户组织关系 → 组织性质 90 则绑 `DEFAULT_DEVELOPER`，否则 `DEFAULT_USER`。
- spec 第 6 节目标：选「用户」→ 绑「普通用户」+ 总公司默认部门（`BuiltInOrgId.DEFAULT_DEPT`）；选「开发者」→ 绑「开发者」+ 开发者平台（`BuiltInOrgId.DEVELOPER_PLATFORM`）。注册不开放运营身份。
- `ConfigKey.Console.BUILT_IN_DEVELOPER` 全工程仅 2 处引用：`ConfigKey.java:26`（声明）与 `UserServiceImpl.java:362`（使用）。
- `UserServiceImpl` 中 `configService` 字段在 `initSsoUser`（密码过期时间）仍在用，**保留字段**，只删 BUILT_IN_DEVELOPER 相关代码。
- 实体依赖方向：`md-common-pojo`（User/OrgNatureEnum/RoleCode）← `console-pojo`（Role）← `console-service`。因此身份判定纯函数只能放 console 侧（要引用 Role）。

---

### Task 1: UserIdentityEnum + 身份判定组件

**Files:**
- Create: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/enumeration/organization/UserIdentityEnum.java`
- Create: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/organization/UserIdentityService.java`
- Create: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/organization/impl/UserIdentityServiceImpl.java`
- Test: `mdp-apps/md-console/console-service/src/test/java/top/mddata/console/service/organization/UserIdentityResolveTest.java`

**Interfaces:**
- Produces（计划 3 可见性过滤的直接依赖）：
  - `UserIdentityEnum.OPERATIONS_ADMIN / DEVELOPER_ADMIN / DEVELOPER / USER`
  - `UserIdentityEnum UserIdentityService.getIdentity(Long userId)`
  - `Map<Long, UserIdentityEnum> UserIdentityService.mapIdentity(Collection<Long> userIds)`
  - `static UserIdentityEnum UserIdentityService.resolve(Collection<Role> roles)`（纯函数，null/空集合 → USER）

- [ ] **Step 1: 写失败测试**

检查 `mdp-apps/md-console/console-service/pom.xml` 是否已有 `org.junit.jupiter:junit-jupiter`（test scope）。没有则按 md-common-pojo 的同款方式添加（不加 version；若编译报找不到包再加 `<version>5.10.2</version>`）。

创建 `UserIdentityResolveTest.java`：

```java
package top.mddata.console.service.organization;

import org.junit.jupiter.api.Test;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.organization.UserIdentityEnum;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.console.entity.permission.Role;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 身份判定规则测试：规则见 docs/用户体系/用户角色组织权限体系设计.md 第 1 节。
 */
class UserIdentityResolveTest {

    private static Role role(String category, Integer nature) {
        return new Role().setRoleCategory(category).setOrgNature(nature);
    }

    @Test
    void 绑定运营管理员角色判定为运营者() {
        assertEquals(UserIdentityEnum.OPERATIONS_ADMIN, UserIdentityService.resolve(
                List.of(role(RoleCategoryEnum.ADMIN_ROLE.getCode(), OrgNatureEnum.OPERATIONS.getCode()))));
    }

    @Test
    void 绑定开发者管理员角色判定为开发者管理员() {
        assertEquals(UserIdentityEnum.DEVELOPER_ADMIN, UserIdentityService.resolve(
                List.of(role(RoleCategoryEnum.ADMIN_ROLE.getCode(), OrgNatureEnum.DEVELOPER.getCode()))));
    }

    @Test
    void 绑定开发者普通角色判定为开发者() {
        assertEquals(UserIdentityEnum.DEVELOPER, UserIdentityService.resolve(
                List.of(role(RoleCategoryEnum.NORMAL_ROLE.getCode(), OrgNatureEnum.DEVELOPER.getCode()))));
    }

    @Test
    void 只绑定总公司角色判定为普通用户() {
        assertEquals(UserIdentityEnum.USER, UserIdentityService.resolve(
                List.of(role(RoleCategoryEnum.ADMIN_ROLE.getCode(), OrgNatureEnum.HEAD_COMPANY.getCode()))));
    }

    @Test
    void 多角色时按最高身份判定() {
        assertEquals(UserIdentityEnum.OPERATIONS_ADMIN, UserIdentityService.resolve(
                List.of(role(RoleCategoryEnum.NORMAL_ROLE.getCode(), OrgNatureEnum.HEAD_COMPANY.getCode()),
                        role(RoleCategoryEnum.ADMIN_ROLE.getCode(), OrgNatureEnum.OPERATIONS.getCode()))));
    }

    @Test
    void 无角色或空集合判定为普通用户() {
        assertEquals(UserIdentityEnum.USER, UserIdentityService.resolve(List.of()));
        assertEquals(UserIdentityEnum.USER, UserIdentityService.resolve(null));
    }
}
```

注：若 `Role` 的 `setRoleCategory/setOrgNature` 链式调用编译失败（`@Accessors(chain = true)` 应已开启），改用普通 set 写法构造。

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q test -pl mdp-apps/md-console/console-service -Dtest=UserIdentityResolveTest`
Expected: 编译失败（`UserIdentityEnum`、`UserIdentityService` 不存在）。

- [ ] **Step 3: 创建 UserIdentityEnum**

```java
package top.mddata.common.enumeration.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户身份。
 *
 * <p>身份由用户绑定的角色决定，判定规则见 UserIdentityService.resolve，
 * 设计文档：docs/用户体系/用户角色组织权限体系设计.md 第 1 节。</p>
 */
@Getter
@AllArgsConstructor
@Schema(description = "用户身份-枚举")
public enum UserIdentityEnum {
    /**
     * 运营者（绑定性质99-运营 的管理员角色）
     */
    OPERATIONS_ADMIN("运营者"),
    /**
     * 开发者管理员（绑定性质90-开发者 的管理员角色）
     */
    DEVELOPER_ADMIN("开发者管理员"),
    /**
     * 开发者（绑定性质90-开发者 的其他角色）
     */
    DEVELOPER("开发者"),
    /**
     * 普通用户（其余情况）
     */
    USER("普通用户");

    /**
     * 身份描述
     */
    private final String desc;
}
```

- [ ] **Step 4: 创建 UserIdentityService（含静态纯函数 resolve）**

```java
package top.mddata.console.service.organization;

import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.organization.UserIdentityEnum;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.console.entity.permission.Role;

import java.util.Collection;
import java.util.Map;

/**
 * 用户身份 服务层。
 *
 * <p>身份由用户绑定的角色决定（docs/用户体系/用户角色组织权限体系设计.md 第 1 节）。</p>
 */
public interface UserIdentityService {

    /**
     * 判定用户身份
     *
     * @param userId 用户id
     * @return 用户身份，用户不存在或无角色时返回 普通用户
     */
    UserIdentityEnum getIdentity(Long userId);

    /**
     * 批量判定用户身份（列表页用，避免 N+1）
     *
     * @param userIds 用户id集合
     * @return key=用户id，value=身份；入参中的每个 id 都会有值
     */
    Map<Long, UserIdentityEnum> mapIdentity(Collection<Long> userIds);

    /**
     * 身份判定规则（纯函数）：
     * 有性质99管理员角色 → 运营者；有性质90管理员角色 → 开发者管理员；
     * 有性质90其他角色 → 开发者；其余 → 普通用户。
     *
     * @param roles 用户绑定的角色集合
     * @return 用户身份
     */
    static UserIdentityEnum resolve(Collection<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return UserIdentityEnum.USER;
        }
        boolean hasOperationsAdmin = roles.stream().anyMatch(role ->
                RoleCategoryEnum.ADMIN_ROLE.getCode().equals(role.getRoleCategory())
                        && OrgNatureEnum.OPERATIONS.eq(role.getOrgNature()));
        if (hasOperationsAdmin) {
            return UserIdentityEnum.OPERATIONS_ADMIN;
        }
        boolean hasDeveloperAdmin = roles.stream().anyMatch(role ->
                RoleCategoryEnum.ADMIN_ROLE.getCode().equals(role.getRoleCategory())
                        && OrgNatureEnum.DEVELOPER.eq(role.getOrgNature()));
        if (hasDeveloperAdmin) {
            return UserIdentityEnum.DEVELOPER_ADMIN;
        }
        boolean hasDeveloper = roles.stream().anyMatch(role -> OrgNatureEnum.DEVELOPER.eq(role.getOrgNature()));
        return hasDeveloper ? UserIdentityEnum.DEVELOPER : UserIdentityEnum.USER;
    }
}
```

- [ ] **Step 5: 创建 UserIdentityServiceImpl**

参考依赖（均已存在）：`top.mddata.console.service.permission.RoleService`（继承 SuperService，可用 `list(QueryWrapper)`）、`top.mddata.common.entity.UserRoleRel`。

**实现要点**：项目惯例的 join 查询（参考 `RoleServiceImpl.findUserRoleCodes`）返回 Role 列表会丢失 userId 归属，无法支撑批量分组，因此 `listUserRoles` 分两步查询（关系表 + 角色表，避免手写 SQL、保持类型安全）。完整实现：

```java
package top.mddata.console.service.organization.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.enumeration.organization.UserIdentityEnum;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.service.organization.UserIdentityService;
import top.mddata.console.service.organization.UserRoleRelService;
import top.mddata.console.service.permission.RoleService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户身份 服务层实现。
 */
@Service
@RequiredArgsConstructor
public class UserIdentityServiceImpl implements UserIdentityService {
    private final RoleService roleService;
    private final UserRoleRelService userRoleRelService;

    @Override
    @Transactional(readOnly = true)
    public UserIdentityEnum getIdentity(Long userId) {
        if (userId == null) {
            return UserIdentityEnum.USER;
        }
        return resolve(listUserRoles(List.of(userId)).getOrDefault(userId, Collections.emptyList()));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, UserIdentityEnum> mapIdentity(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, List<Role>> roleMap = listUserRoles(userIds.stream().distinct().toList());
        // 入参中的每个 id 都必须有值，无角色的按普通用户处理
        return userIds.stream().collect(Collectors.toMap(Function.identity(),
                userId -> resolve(roleMap.getOrDefault(userId, Collections.emptyList())), (a, b) -> a));
    }

    /**
     * 按用户id分组查询其绑定的启用角色
     */
    private Map<Long, List<Role>> listUserRoles(List<Long> userIds) {
        // 1. 查关系：userId -> roleId
        QueryWrapper relQuery = QueryWrapper.create().select().from(UserRoleRel.class)
                .where(UserRoleRel::getUserId).in(userIds);
        List<UserRoleRel> relList = userRoleRelService.list(relQuery);
        if (relList.isEmpty()) {
            return Collections.emptyMap();
        }

        // 2. 查启用状态的角色
        List<Long> roleIds = relList.stream().map(UserRoleRel::getRoleId).distinct().toList();
        List<Role> roles = roleService.list(QueryWrapper.create()
                .where(Role::getId).in(roleIds).and(Role::getState).eq(true));
        Map<Long, Role> roleById = roles.stream()
                .collect(Collectors.toMap(Role::getId, Function.identity()));

        // 3. 按 userId 分组
        Map<Long, List<Role>> result = new HashMap<>();
        for (UserRoleRel rel : relList) {
            Role role = roleById.get(rel.getRoleId());
            if (role != null) {
                result.computeIfAbsent(rel.getUserId(), k -> new ArrayList<>()).add(role);
            }
        }
        return result;
    }
}
```

- [ ] **Step 6: 运行测试确认通过**

Run: `mvn -q test -pl mdp-apps/md-console/console-service -Dtest=UserIdentityResolveTest`
Expected: 6/6 PASS

- [ ] **Step 7: 检查点——编译**

Run: `mvn -q clean compile -DskipTests`
Expected: BUILD SUCCESS

---

### Task 2: 注册流程改造

**Files:**
- Modify: `mdp-apps/md-console/console-service/.../organization/impl/UserServiceImpl.java`（saveDefOrg、saveDefRole、3 个 registerBy* 方法）
- Modify: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/constant/ConfigKey.java:26`
- Test: `mdp-apps/md-console/console-service/src/test/java/top/mddata/console/service/organization/RegisterNatureCheckTest.java`

**Interfaces:**
- Consumes: `BuiltInOrgId.DEFAULT_DEPT/DEVELOPER_PLATFORM`、`OrgNatureEnum`、`RoleCode.DEFAULT_USER/DEFAULT_DEVELOPER`（计划 1）
- Produces: 注册行为契约 = spec 第 6 节：选用户 → DEFAULT_USER + 默认部门；选开发者 → DEFAULT_DEVELOPER + 开发者平台；nature 仅允许 1/90。

- [ ] **Step 1: 写失败测试（nature 白名单校验的纯函数部分）**

注册身份校验单独抽成可测的静态方法（放在 UserServiceImpl 里没法脱离 Spring 测，抽到 `UserIdentityService` 接口作为第二个静态方法）：

在 `UserIdentityService` 接口中追加：

```java
    /**
     * 校验注册身份是否合法：仅允许 1-总公司（普通用户）、90-开发者。
     * 运营身份不开放注册。
     *
     * @param nature 注册入参的组织性质
     * @return true=允许注册
     */
    static boolean checkRegisterNature(Integer nature) {
        return OrgNatureEnum.HEAD_COMPANY.eq(nature) || OrgNatureEnum.DEVELOPER.eq(nature);
    }
```

新建 `RegisterNatureCheckTest.java`：

```java
package top.mddata.console.service.organization;

import org.junit.jupiter.api.Test;
import top.mddata.common.enumeration.organization.OrgNatureEnum;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 注册身份白名单测试：运营身份(99)不开放注册，非法值快速失败。
 */
class RegisterNatureCheckTest {

    @Test
    void 总公司和开发者允许注册() {
        assertTrue(UserIdentityService.checkRegisterNature(OrgNatureEnum.HEAD_COMPANY.getCode()));
        assertTrue(UserIdentityService.checkRegisterNature(OrgNatureEnum.DEVELOPER.getCode()));
    }

    @Test
    void 运营身份与非法值禁止注册() {
        assertFalse(UserIdentityService.checkRegisterNature(OrgNatureEnum.OPERATIONS.getCode()));
        assertFalse(UserIdentityService.checkRegisterNature(null));
        assertFalse(UserIdentityService.checkRegisterNature(2));
    }
}
```

Run: `mvn -q test -pl mdp-apps/md-console/console-service -Dtest=RegisterNatureCheckTest`
Expected: 编译失败（方法不存在）→ 在接口追加方法后 PASS。

- [ ] **Step 2: UserServiceImpl 改造（4 处）**

1. `saveDefOrg` 整体替换为：

```java
    private void saveDefOrg(User user) {
        // 开发者落入开发者平台公司，普通用户落入总公司下的默认部门（spec 第 6 节）
        Long orgId = OrgNatureEnum.DEVELOPER.eq(user.getNature())
                ? BuiltInOrgId.DEVELOPER_PLATFORM : BuiltInOrgId.DEFAULT_DEPT;

        UserOrgRel userOrgRel = new UserOrgRel();
        userOrgRel.setUserId(user.getId());
        userOrgRel.setOrgId(orgId);
        userOrgRelService.save(userOrgRel);
    }
```

2. `saveDefRole` 整体替换为（直接用 nature 载体判定，不再查库）：

```java
    private void saveDefRole(User user) {
        String code = OrgNatureEnum.DEVELOPER.eq(user.getNature())
                ? RoleCode.DEFAULT_DEVELOPER : RoleCode.DEFAULT_USER;
        roleService.joinTheRole(code, user.getId());
    }
```

3. 3 个注册方法（registerByEmail/registerByPhone/registerByUsername）在方法体**第一行**（参数校验之前）插入：

```java
        ArgumentAssert.isTrue(UserIdentityService.checkRegisterNature(user.getNature()),
                "不支持的注册身份：{}", user.getNature());
```

import 新增 `top.mddata.console.service.organization.UserIdentityService`（静态调用，无需注入）。

4. 清理不再使用的引用：删除 `ConfigKey` import（若仅 BUILT_IN_DEVELOPER 使用——先 grep 确认 `ConfigKey.` 在该文件是否只剩这一处；`initSsoUser` 里用的是 `ConfigKey.Workbench.PASSWORD_EXPIRE_TIME`，若存在则保留 import 只删 Console 引用）。删除 `BuiltInOrgId`、`UserIdentityService`、`RoleCode`、`OrgNatureEnum` 中缺失的 import 补齐（编译器会提示）。

- [ ] **Step 3: ConfigKey 删除 BUILT_IN_DEVELOPER 常量**

`ConfigKey.java:26` 删除 `String BUILT_IN_DEVELOPER = "BUILT_IN_DEVELOPER";` 一行（含注释）。删除后验证：

Run: `grep -rn "BUILT_IN_DEVELOPER" --include="*.java" mdp-apps mdp-base mdp-sdk | grep -v "/target/"`
Expected: 0 匹配。

- [ ] **Step 4: 检查点——测试 + 编译**

Run: `mvn -q test -pl mdp-apps/md-console/console-service -Dtest=RegisterNatureCheckTest,UserIdentityResolveTest`
Expected: 全部 PASS

Run: `mvn -q clean compile -DskipTests`
Expected: BUILD SUCCESS

---

### Task 3: mdp.sql 与迁移脚本清理 + 全量验证

**Files:**
- Modify: `docs/mdp.sql`（删除 BUILT_IN_DEVELOPER 配置行）
- Create: `docs/用户体系/plan2-migration.sql`

**Interfaces:**
- Produces: 配置基线与代码一致（BUILT_IN_DEVELOPER 彻底移除）。

- [ ] **Step 1: mdp.sql 删除配置行**

`grep -n "BUILT_IN_DEVELOPER" docs/mdp.sql` 定位（当前在 mdc_config Records 块，约 :51），删除该行。删除后：

Run: `grep -n "BUILT_IN_DEVELOPER\|REG_BIND_ROLE" docs/mdp.sql`
Expected: 0 匹配。

- [ ] **Step 2: 编写 plan2-migration.sql**

创建 `docs/用户体系/plan2-migration.sql`：

```sql
-- ----------------------------------------------------------------------------
-- 用户体系重构·计划2 迁移脚本（存量环境专用）
--
-- 适用：已执行过 plan1-migration.sql 的存量环境。全新环境直接执行 docs/mdp.sql。
-- 内容：移除"内置开发者公司"配置（注册流程改为直接绑定内置组织，见
-- docs/用户体系/用户角色组织权限体系设计.md 第 6 节）。
-- ----------------------------------------------------------------------------

DELETE FROM `mdc_config` WHERE `uniq_key` = 'BUILT_IN_DEVELOPER';
```

- [ ] **Step 3: 全量验证**

Run: `mvn -q clean compile -DskipTests` → BUILD SUCCESS
Run: `mvn test -pl mdp-apps/md-console/console-service,mdp-apps/md-public/md-common-pojo` → 全部 PASS（含计划 1 的 3 个测试，共 3+6+3=12 个用例）
Run: `mvn -q checkstyle:check -pl mdp-apps/md-console/console-service,mdp-apps/md-public/md-common-pojo` → 退出码 0

- [ ] **Step 4: 人工冒烟（由用户执行）**

1. 开发库执行 `docs/用户体系/plan2-migration.sql`（若库是旧的）或用新 `mdp.sql` 重建。
2. 注册页分别以「用户」「开发者」身份注册账号。
3. 数据库核对：新用户在 `mdc_user_org_rel` 中分别挂 `687106879444787204`（默认部门）/ `687106879444787202`（开发者平台）；`mdc_user_role_rel` 分别绑 `DEFAULT_USER` / `DEFAULT_DEVELOPER` 角色的 id。
4. 尝试用 `nature=99` 调注册接口，应返回"不支持的注册身份"。

---

## Self-Review 记录

- **Spec 覆盖**：spec §1 身份判定 → Task 1；§6 注册流程 → Task 2/3；§8 改造点 5（注册流程改造）完整覆盖。统计接口改造（改造点 1 尾句）已由计划 1 的 countByNature 完成，本计划无遗留。
- **占位符**：无 TBD/TODO；所有代码片段为完整可粘贴实现。
- **类型一致性**：`UserIdentityService.resolve(Collection<Role>)` 在 Step 4 定义、测试与 Step 1 一致；`checkRegisterNature(Integer)` 在 Task 2 Step 1 定义、Step 2 消费。
- **已知留尾**：`UserBase.nature` 临时载体的最终形态（是否保留为非持久化字段）在计划 3 可见性实现后评估；多线增长趋势未列入 spec，不做。
