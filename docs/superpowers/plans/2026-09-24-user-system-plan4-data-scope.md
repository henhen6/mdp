# 用户体系重构 · 计划 4：数据权限 SPI 与权限集合控范围 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按 spec 第 4 节实现数据权限"五档 + 自定义实现"：角色可配置数据范围，`@DataPermission` 执行链按当前用户角色的数据范围自动过滤 SQL；角色管理的可分配范围（应用/菜单/数据权限档位）改由本组织性质的权限集合控制。

**Architecture:** 修复并完成 md-db-mybatis-flex 里已存在的若依风格半成品骨架（DataScope/DataPermissionAspect/DataPermissionDialect/DataPermissionFilter）——表名对齐本项目（mdc_org/tree_path）、补"本公司及以下"档、CUSTOM 档由"角色-部门表"改为 SPI Bean（角色存 Bean 名）；角色数据范围由 `DataPermissionFilter` 实现类用 MyBatis-Flex Row Db 跨模块查询（md-common-config 不依赖 console 实体）。可分配范围通过 3 个只读接口暴露（前端接线属计划 5）。

**Tech Stack:** Java 17 / Spring Boot 3 / MyBatis-Flex（Row Db）/ sa-token

## Global Constraints

- **禁止任何 git add / commit / push 操作**。任务完成检查点 = 编译通过 + 测试通过。
- 注释简体中文；最大行长度 100 字符；使用 Lombok；不引入新依赖。
- 本计划不删除任何文件（仅改造既有骨架类与新增文件）。
- 编译命令（仓库根目录 `/Users/tangyh/gitee/mdp`）：`mvn -q clean compile -DskipTests`
- 依赖方向铁律：`md-common-config`（DataPermissionFilterImpl 所在）**不可依赖 console 的 Role 实体**，跨表查询用 `com.mybatisflex.core.row.Db`（Row 查询，无需实体）。

## 关键背景（执行者必读）

- 既有骨架（mdp-base/md-db-mybatis-flex/.../datapermission/）：`DataScope` 枚举只有 ALL/DEPT_AND_CHILD/DEPT/SELF/CUSTOM（缺"本公司及以下"）；`DataPermissionDialect` 的 DEPT_AND_CHILD 用若依的 `find_in_set(ancestors)`（本项目用 `tree_path`）；CUSTOM 查不存在的 `sys_role_dept` 表；`DataPermissionAspect` 通过 ThreadLocal 传递注解。
- `DataPermissionFilterImpl`（md-common-config/.../interceptor/）：`getCurrentUser()` 只填了 userId/deptId，**roles 为空 → 骨架当前实际是空转**（唯一使用点 ConfigServiceImpl:226 演示性质）。
- `DataPermission` 注解默认值还是若依表名（sys_dept/sys_role_dept），全工程仅 ConfigServiceImpl 一处使用。
- `ContextUtil.getCurrentCompanyId()`（:333）与 `getCurrentDeptId()`（:378）均存在（md-core）。
- 多角色范围合并规则（本计划定稿，写入 javadoc）：**范围最大的一档生效**（全部 > 本公司及以下 > 本部门及以下 > 本部门 > 仅本人）；CUSTOM 为独立档——存在任一内置档角色时 CUSTOM 忽略；全部为 CUSTOM 角色时多个 handler 条件以 OR 合并。
- 可分配数据权限档位规则（spec 第 4 节）：权限集合角色的 data_scope 档位为 R，则可分配内置档 = 档位 ≤ R；CUSTOM 档仅当 R=全部 时可分配。
- 档位编码（与 DataScope 枚举 code 一致）：10-全部 20-本公司及以下 30-本部门及以下 40-本部门 50-仅本人 90-自定义实现。

---

### Task 1: mdc_role 数据权限字段 + DataScope 枚举对齐

**Files:**
- Modify: `mdp-base/md-db-mybatis-flex/.../datapermission/DataScope.java`（加 COMPANY_AND_CHILD + 编码 + BaseEnum）
- Modify: `mdp-apps/md-console/console-pojo/.../entity/permission/base/RoleBase.java`（加 2 字段）
- Modify: `mdp-apps/md-console/console-pojo/.../dto/permission/RoleDto.java`、`RoleTemploateDto.java`、`vo/permission/RoleVo.java`（加 2 字段）
- Modify: `docs/mdp.sql`（mdc_role DDL 加 2 列）
- Create: `docs/用户体系/plan4-migration.sql`
- Test: `mdp-base/md-db-mybatis-flex/src/test/java/top/mddata/base/mybatisflex/datapermission/DataScopeTest.java`

**Interfaces:**
- Produces：`DataScope.ALL("10")/COMPANY_AND_CHILD("20")/DEPT_AND_CHILD("30")/DEPT("40")/SELF("50")/CUSTOM("90")`（实现 `BaseEnum<String>`）；`RoleBase.getDataScope()/getDataScopeImpl()`。

- [ ] **Step 1: 写失败测试**

检查 `mdp-base/md-db-mybatis-flex/pom.xml` 是否有 junit-jupiter（test scope），没有则按 md-common-pojo 同款添加。

```java
package top.mddata.base.mybatisflex.datapermission;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 数据范围枚举基线测试：编码是跨环境契约（mdc_role.data_scope 存储值）。
 */
class DataScopeTest {

    @Test
    void 五档加自定义编码稳定() {
        assertEquals("10", DataScope.ALL.getCode());
        assertEquals("20", DataScope.COMPANY_AND_CHILD.getCode());
        assertEquals("30", DataScope.DEPT_AND_CHILD.getCode());
        assertEquals("40", DataScope.DEPT.getCode());
        assertEquals("50", DataScope.SELF.getCode());
        assertEquals("90", DataScope.CUSTOM.getCode());
    }
}
```

Run: `mvn -q test -pl mdp-base/md-db-mybatis-flex -Dtest=DataScopeTest` → 编译失败。

- [ ] **Step 2: 改造 DataScope 枚举**

```java
package top.mddata.base.mybatisflex.datapermission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 数据范围枚举。
 *
 * <p>存储于 mdc_role.data_scope；五档为系统内置实现，自定义实现（90）由开发人员
 * 编写 DataScopeCustomHandler 实现类并在角色上配置其 Spring Bean 名（mdc_role.data_scope_impl）。</p>
 */
@Getter
@AllArgsConstructor
public enum DataScope implements BaseEnum<String> {

    /**
     * 全部数据
     */
    ALL("10", "全部数据"),

    /**
     * 本公司及以下
     */
    COMPANY_AND_CHILD("20", "本公司及以下"),

    /**
     * 本部门及以下
     */
    DEPT_AND_CHILD("30", "本部门及以下"),

    /**
     * 本部门
     */
    DEPT("40", "本部门"),

    /**
     * 仅本人
     */
    SELF("50", "仅本人"),

    /**
     * 自定义实现（角色上配置 DataScopeCustomHandler 的 Bean 名）
     */
    CUSTOM("90", "自定义实现");

    /**
     * 档位编码（数据库存储用）
     */
    private final String code;

    /**
     * 档位描述
     */
    private final String desc;

    /**
     * 按编码取枚举（BaseEnum 无静态查找方法，各枚举自带）
     *
     * @param code 档位编码
     * @return 枚举，无匹配返回 null
     */
    public static DataScope getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (DataScope scope : values()) {
            if (scope.getCode().equals(code)) {
                return scope;
            }
        }
        return null;
    }
}
```

确认 `BaseEnum<String>` 的路径（`top.mddata.base.interfaces.BaseEnum`，与 OrgNatureEnum 同款）；若 md-db-mybatis-flex 未依赖 md-core，检查 pom 后按需处理（md-db-mybatis-flex 应已依赖 md-core，编译验证）。注意：**不要**使用 `DataScope.valueOf(...)`——Java 内置 valueOf 按枚举名匹配而非编码，本计划一律用 `getByCode`。

- [ ] **Step 3: RoleBase 加字段**

在 `orgNature` 字段之后插入：

```java
    /**
     * 数据范围
     * [10-全部 20-本公司及以下 30-本部门及以下 40-本部门 50-仅本人 90-自定义实现]
     */
    private String dataScope;

    /**
     * 自定义数据范围实现类（Spring Bean 名）
     * 仅 dataScope=90 时使用，实现 DataScopeCustomHandler 接口
     */
    private String dataScopeImpl;
```

RoleDto、RoleTemploateDto、RoleVo 各加同名字段（参照各自文件中 orgNature 字段的注解风格：`@Schema(description = "数据范围")` 等；DTO 不加 @NotEmpty——数据范围允许为空，为空视同 ALL）。

- [ ] **Step 4: mdp.sql DDL + 迁移脚本**

mdc_role 建表语句 `org_nature` 行之后插入：

```sql
  `data_scope` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '10' COMMENT '数据范围\n[10-全部 20-本公司及以下 30-本部门及以下 40-本部门 50-仅本人 90-自定义实现]',
  `data_scope_impl` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '自定义数据范围实现类（Spring Bean 名）',
```

创建 `docs/用户体系/plan4-migration.sql`：

```sql
-- ----------------------------------------------------------------------------
-- 用户体系重构·计划4 迁移脚本（存量环境专用）
-- 内容：mdc_role 增加数据范围字段（docs/用户体系/用户角色组织权限体系设计.md 第 4 节）。
-- ----------------------------------------------------------------------------

ALTER TABLE `mdc_role` ADD COLUMN `data_scope` char(2) DEFAULT '10' COMMENT '数据范围\n[10-全部 20-本公司及以下 30-本部门及以下 40-本部门 50-仅本人 90-自定义实现]' AFTER `org_nature`;
ALTER TABLE `mdc_role` ADD COLUMN `data_scope_impl` varchar(255) DEFAULT NULL COMMENT '自定义数据范围实现类（Spring Bean 名）' AFTER `data_scope`;
```

- [ ] **Step 5: 测试通过 + 编译检查点**

Run: `mvn -q test -pl mdp-base/md-db-mybatis-flex -Dtest=DataScopeTest` → PASS
Run: `mvn -q clean compile -DskipTests` → BUILD SUCCESS

---

### Task 2: SPI 接口 + 方言改造

**Files:**
- Create: `mdp-base/md-db-mybatis-flex/.../datapermission/DataScopeCustomHandler.java`
- Modify: `mdp-base/md-db-mybatis-flex/.../datapermission/DataPermissionCurrentUser.java`（加 companyId、CurrentUserRole 加 dataScopeImpl）
- Modify: `mdp-base/md-db-mybatis-flex/.../datapermission/DataPermission.java`（注解默认值对齐本项目）
- Modify: `mdp-base/md-db-mybatis-flex/.../datapermission/DataPermissionDialect.java`（五档重写 + max-scope 合并）
- Modify: `mdp-base/md-db-mybatis-flex/.../config/DataPermissionAutoConfiguration.java`（注入 handler Map）
- Test: `mdp-base/md-db-mybatis-flex/src/test/java/top/mddata/base/mybatisflex/datapermission/EffectiveRolesTest.java`

**Interfaces:**
- Produces：
  - `void DataScopeCustomHandler.apply(DataPermission dataPermission, DataPermissionCurrentUser currentUser, QueryWrapper queryWrapper)`（二次开发的扩展点）
  - `static List<DataPermissionCurrentUser.CurrentUserRole> DataPermissionDialect.selectEffectiveRoles(Collection<CurrentUserRole> roles)`（合并规则纯函数，包级可见改 public static 以便测试）

- [ ] **Step 1: 创建 DataScopeCustomHandler**

```java
package top.mddata.base.mybatisflex.datapermission;

import com.mybatisflex.core.query.QueryWrapper;

/**
 * 自定义数据范围处理器（SPI 扩展点）。
 *
 * <p>五档内置数据范围无法满足需求时，开发人员实现本接口并注册为 Spring Bean，
 * 在角色的 data_scope_impl 字段配置 Bean 名（data_scope=90 时生效）。
 * 实现类的职责：把该角色可见的数据范围条件追加到 queryWrapper。</p>
 */
public interface DataScopeCustomHandler {

    /**
     * 追加自定义数据范围条件
     *
     * @param dataPermission 数据权限注解（含表别名、部门列、用户列等配置）
     * @param currentUser    当前用户上下文（userId、companyId、deptId）
     * @param queryWrapper   查询条件（在其上追加过滤）
     */
    void apply(DataPermission dataPermission, DataPermissionCurrentUser currentUser, QueryWrapper queryWrapper);
}
```

- [ ] **Step 2: DataPermissionCurrentUser 扩展**

1. 类中 `deptId` 字段后增加：
```java
    /**
     * 公司 ID
     */
    private Long companyId;
```
（配套 getter/setter——该类是手写 getter/setter 风格，照着 deptId 补。）
2. `CurrentUserRole` 增加字段及 getter/setter：
```java
        /**
         * 自定义数据范围实现类（Spring Bean 名），仅 dataScope=CUSTOM 时有值
         */
        private String dataScopeImpl;
```
3. `CurrentUserRole` 构造器改为三参 `(Long roleId, DataScope dataScope, String dataScopeImpl)`，保留原两参构造委托三参（兼容）。

- [ ] **Step 3: DataPermission 注解默认值对齐**

- `deptTableAlias()` 默认值 `"sys_dept"` → `"mdc_org"`，javadoc 说明本项目组织表与 tree_path 机制。
- 删除 `roleDeptTableAlias()` 与 `roleId()` 两个属性（CUSTOM 已改为 SPI，不再需要角色-部门表；全工程唯一使用点 ConfigServiceImpl 用的是无参 `@DataPermission`，不受影响——修改前先 grep 确认无其他使用点）。

- [ ] **Step 4: DataPermissionDialect 重写（核心）**

构造器改为 `(DataPermissionFilter, Map<String, DataScopeCustomHandler>)`。`forSelectByQuery` 主流程：

```java
    @Override
    public String forSelectByQuery(QueryWrapper queryWrapper) {
        if (!dataPermissionFilter.isFilter()) {
            return super.buildSelectSql(queryWrapper);
        }
        DataPermission dataPermission = DataPermissionAspect.currentDataPermission();
        if (dataPermission == null) {
            return super.buildSelectSql(queryWrapper);
        }
        DataPermissionCurrentUser currentUser = dataPermissionFilter.getCurrentUser();
        List<DataPermissionCurrentUser.CurrentUserRole> effectiveRoles =
                selectEffectiveRoles(currentUser.getRoles());
        if (CollUtil.isEmpty(effectiveRoles)) {
            return super.buildSelectSql(queryWrapper);
        }
        // 任一角色为全部数据，直接放行
        boolean hasAll = effectiveRoles.stream()
                .anyMatch(role -> DataScope.ALL.equals(role.getDataScope()));
        if (hasAll) {
            return super.buildSelectSql(queryWrapper);
        }
        // 多个生效角色的条件以 OR 合并（全部为 CUSTOM 角色的场景）
        if (effectiveRoles.size() == 1) {
            applyScope(dataPermission, currentUser, effectiveRoles.get(0), queryWrapper);
        } else {
            queryWrapper.and(qw -> {
                effectiveRoles.forEach(role ->
                        qw.or(orWrapper -> {
                            applyScope(dataPermission, currentUser, role, orWrapper);
                            return orWrapper;
                        }));
                return qw;
            });
        }
        return super.buildSelectSql(queryWrapper);
    }

    /**
     * 多角色范围合并（纯函数）：
     * 存在内置档角色时，只保留范围最大档的角色（全部 > 公司及以下 > 部门及以下 > 部门 > 仅本人）；
     * 全部为 CUSTOM 角色时全部保留（调用方以 OR 合并各 handler 条件）。
     *
     * @param roles 当前用户的角色集合
     * @return 生效角色列表，空入参返回空列表
     */
    public static List<DataPermissionCurrentUser.CurrentUserRole> selectEffectiveRoles(
            Collection<DataPermissionCurrentUser.CurrentUserRole> roles) {
        if (CollUtil.isEmpty(roles)) {
            return List.of();
        }
        List<DataScope> builtInScopes = roles.stream().map(DataPermissionCurrentUser.CurrentUserRole::getDataScope)
                .filter(scope -> scope != null && !DataScope.CUSTOM.equals(scope)).toList();
        if (builtInScopes.isEmpty()) {
            return List.copyOf(roles);
        }
        DataScope maxScope = Collections.max(builtInScopes,
                Comparator.comparingInt(DataPermissionDialect::scopeRank));
        return roles.stream().filter(role -> maxScope.equals(role.getDataScope())).toList();
    }

    private static int scopeRank(DataScope scope) {
        return switch (scope) {
            case ALL -> 5;
            case COMPANY_AND_CHILD -> 4;
            case DEPT_AND_CHILD -> 3;
            case DEPT -> 2;
            case SELF -> 1;
            case CUSTOM -> 0;
        };
    }

    private void applyScope(DataPermission dataPermission, DataPermissionCurrentUser currentUser,
                            DataPermissionCurrentUser.CurrentUserRole role, QueryWrapper queryWrapper) {
        switch (role.getDataScope()) {
            case COMPANY_AND_CHILD ->
                    buildOrgTreeExpression(dataPermission, currentUser.getCompanyId(), queryWrapper);
            case DEPT_AND_CHILD -> buildOrgTreeExpression(dataPermission, currentUser.getDeptId(), queryWrapper);
            case DEPT -> queryWrapper.eq(buildColumn(dataPermission.tableAlias(), dataPermission.deptId()),
                    currentUser.getDeptId());
            case SELF -> queryWrapper.eq(buildColumn(dataPermission.tableAlias(), dataPermission.userId()),
                    currentUser.getUserId());
            case CUSTOM -> buildCustomExpression(dataPermission, currentUser, role, queryWrapper);
            default -> throw new IllegalArgumentException("暂不支持 [%s] 数据权限".formatted(role.getDataScope()));
        }
    }

    /**
     * 构建组织树范围表达式：目标表的部门列在指定组织子树内（mdc_org.tree_path 前缀匹配）
     */
    private void buildOrgTreeExpression(DataPermission dataPermission, Long rootOrgId, QueryWrapper queryWrapper) {
        QueryWrapper subQueryWrapper = QueryWrapper.create();
        subQueryWrapper.select(dataPermission.id()).from(dataPermission.deptTableAlias());
        subQueryWrapper.like("tree_path", "/" + rootOrgId + "/");
        queryWrapper.in(buildColumn(dataPermission.tableAlias(), dataPermission.deptId()), subQueryWrapper);
    }

    /**
     * 构建自定义数据范围表达式：按角色配置的 Bean 名找到 SPI 实现并委托
     */
    private void buildCustomExpression(DataPermission dataPermission, DataPermissionCurrentUser currentUser,
                                       DataPermissionCurrentUser.CurrentUserRole role, QueryWrapper queryWrapper) {
        DataScopeCustomHandler handler = customHandlers.get(role.getDataScopeImpl());
        ArgumentAssert.notNull(handler, "角色[{}]配置的自定义数据范围实现[{}]不存在",
                role.getRoleId(), role.getDataScopeImpl());
        handler.apply(dataPermission, currentUser, queryWrapper);
    }
```

注意：
- 删除旧的 `buildCustomExpression`（sys_role_dept 版）与 `buildDeptAndChildExpression`（find_in_set 版）。
- `ArgumentAssert` 在 md-core（`top.mddata.base.utils.ArgumentAssert`），md-db-mybatis-flex 应可用，编译验证；不可用则改抛 `IllegalArgumentException` 并在报告中说明。
- `customHandlers` 为 `Map<String, DataScopeCustomHandler>` 字段。
- `and/or` 的 lambda 写法若与项目 MyBatis-Flex 版本不兼容，参考计划 3 Task 4 的备选写法处理并在报告中注明。

- [ ] **Step 5: DataPermissionAutoConfiguration 适配**

阅读该配置类，把 `new DataPermissionDialect(dataPermissionFilter)` 改为注入 `Map<String, DataScopeCustomHandler>`（Spring 自动聚合所有实现 Bean）后传入：

```java
    @Bean
    public DataPermissionDialect dataPermissionDialect(DataPermissionFilter dataPermissionFilter,
                                                       Map<String, DataScopeCustomHandler> customHandlers) {
        return new DataPermissionDialect(dataPermissionFilter, customHandlers);
    }
```

（以该类的实际结构为准调整；若 Map 注入在自动配置中有歧义，用 `ObjectProvider<Map<String, DataScopeCustomHandler>>` 或 `ApplicationContext.getBeansOfType` 替代并在报告中说明。）

- [ ] **Step 6: 写测试 EffectiveRolesTest 并通过**

```java
package top.mddata.base.mybatisflex.datapermission;

import org.junit.jupiter.api.Test;
import top.mddata.base.mybatisflex.datapermission.DataPermissionCurrentUser.CurrentUserRole;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 多角色数据范围合并规则测试。
 */
class EffectiveRolesTest {

    private static CurrentUserRole role(Long id, DataScope scope) {
        return new CurrentUserRole(id, scope, null);
    }

    @Test
    void 空集合返回空() {
        assertTrue(DataPermissionDialect.selectEffectiveRoles(null).isEmpty());
        assertTrue(DataPermissionDialect.selectEffectiveRoles(Set.of()).isEmpty());
    }

    @Test
    void 内置档取范围最大档() {
        List<CurrentUserRole> result = DataPermissionDialect.selectEffectiveRoles(
                Set.of(role(1L, DataScope.SELF), role(2L, DataScope.DEPT_AND_CHILD)));
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getRoleId());
    }

    @Test
    void 内置档存在时自定义档被忽略() {
        List<CurrentUserRole> result = DataPermissionDialect.selectEffectiveRoles(
                Set.of(role(1L, DataScope.CUSTOM), role(2L, DataScope.SELF)));
        assertEquals(1, result.size());
        assertEquals(DataScope.SELF, result.get(0).getDataScope());
    }

    @Test
    void 全部为自定义档时全部保留() {
        List<CurrentUserRole> result = DataPermissionDialect.selectEffectiveRoles(
                Set.of(role(1L, DataScope.CUSTOM), role(2L, DataScope.CUSTOM)));
        assertEquals(2, result.size());
    }

    @Test
    void 全部数据档直接保留() {
        List<CurrentUserRole> result = DataPermissionDialect.selectEffectiveRoles(
                Set.of(role(1L, DataScope.SELF), role(2L, DataScope.ALL)));
        assertEquals(1, result.size());
        assertEquals(DataScope.ALL, result.get(0).getDataScope());
    }
}
```

Run: `mvn -q test -pl mdp-base/md-db-mybatis-flex -Dtest=EffectiveRolesTest,DataScopeTest` → 全 PASS
Run: `mvn -q clean compile -DskipTests` → BUILD SUCCESS（注意 ConfigServiceImpl 不受影响）

---

### Task 3: DataPermissionFilterImpl 填充角色上下文

**Files:**
- Modify: `mdp-apps/md-public/md-common-config/.../interceptor/DataPermissionFilterImpl.java`

**Interfaces:**
- Consumes：Task 2 的 `CurrentUserRole(roleId, dataScope, dataScopeImpl)`、`ContextUtil.getCurrentCompanyId()`

- [ ] **Step 1: 改造 getCurrentUser**

```java
package top.mddata.common.interceptor;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import org.springframework.stereotype.Component;
import top.mddata.base.mybatisflex.datapermission.DataPermissionCurrentUser;
import top.mddata.base.mybatisflex.datapermission.DataPermissionFilter;
import top.mddata.base.mybatisflex.datapermission.DataScope;
import top.mddata.base.util.ContextUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限数据实现类。
 *
 * <p>加载当前用户及其启用角色的数据范围（跨模块查询 mdc_role，
 * 用 MyBatis-Flex Row Db 避免依赖 console 实体）。</p>
 */
@Component
public class DataPermissionFilterImpl implements DataPermissionFilter {
    @Override
    public boolean isFilter() {
        return true;
    }

    @Override
    public DataPermissionCurrentUser getCurrentUser() {
        DataPermissionCurrentUser currentUser = new DataPermissionCurrentUser();
        currentUser.setUserId(ContextUtil.getUserId());
        currentUser.setDeptId(ContextUtil.getCurrentDeptId());
        currentUser.setCompanyId(ContextUtil.getCurrentCompanyId());
        currentUser.setRoles(findRoles(ContextUtil.getUserId()));
        return currentUser;
    }

    /**
     * 查询用户绑定的启用角色及其数据范围配置
     */
    private Set<DataPermissionCurrentUser.CurrentUserRole> findRoles(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        QueryWrapper wrapper = QueryWrapper.create()
                .select("r.id AS roleId", "r.data_scope AS dataScope", "r.data_scope_impl AS dataScopeImpl")
                .from("mdc_user_role_rel").as("ur")
                .innerJoin("mdc_role").as("r").on("ur.role_id = r.id")
                .where("ur.user_id = {0}", userId)
                .and("r.state = 1").and("r.deleted_at = 0");
        List<Row> rows = Db.selectListByQuery(wrapper);
        return rows.stream().map(row -> new DataPermissionCurrentUser.CurrentUserRole(
                row.getLong("roleId"),
                DataScope.getByCode(row.getString("dataScope")),
                row.getString("dataScopeImpl")))
                .collect(Collectors.toSet());
    }
}
```

注意：
- 用 `DataScope.getByCode(code)`（Task 1 已定义），禁止 `DataScope.valueOf`。
- Row 的取数 API（getLong/getString）以项目 MyBatis-Flex 版本为准；`{0}` 占位符是 MyBatis-Flex 的参数绑定写法，若版本不支持改 `.where("ur.user_id = ?", userId)`。
- `r.state = 1` 若与实际存储（bit(1)）比较有问题，改用 `.and("r.state = true")` 或按 MyBatis-Flex 对 Boolean 的处理，以运行验证为准并在报告中说明。
- 修改前先 grep `Db.selectListByQuery` 确认项目里已有 Row Db 用法先例；完全没有则用 `Db.selectListBySql` 手写等价 SQL 替代。

- [ ] **Step 2: 编译检查点**

Run: `mvn -q clean compile -DskipTests` → BUILD SUCCESS

---

### Task 4: 权限集合控范围（可分配范围接口）

**Files:**
- Modify: `mdp-apps/md-console/console-service/.../permission/RoleService.java` + `impl/RoleServiceImpl.java`（getPermSetRoleByCurrentUser）
- Modify: `mdp-apps/md-console/console-web/.../permission/RoleController.java`（3 个只读端点）
- Test: `mdp-apps/md-console/console-service/src/test/java/top/mddata/console/service/permission/AssignableDataScopeTest.java`

**Interfaces:**
- Produces（前端计划 5 接线的契约）：
  - `Role RoleService.getPermSetRoleOfCurrentOperator()` — 当前操作人顶级公司性质对应的权限集合角色（roleCategory=30），无则返回 null
  - `List<DataScope> RoleService.getAssignableDataScopes()` — 权限集合档位 ≤ R 的内置档 +（R=全部时含 CUSTOM）
  - `GET /permission/role/assignableDataScopes` → `R<List<Map<String, String>>>`（code/desc）
  - `GET /permission/role/assignableAppIds` → `R<List<Long>>`（权限集合角色已分配的应用 id）
  - `GET /permission/role/assignableResourceIds` → `R<Map<Long, Collection<Long>>>`（与 findResourceIdByRoleId 同结构）

- [ ] **Step 1: 写失败测试（可分配档位纯函数）**

在 RoleService 接口加静态纯函数：

```java
    /**
     * 计算可分配的数据范围档位：不超过权限集合档位的内置档；
     * 自定义实现档仅当权限集合为全部数据时可分配。
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
            // 权限集合自身是 CUSTOM：视为仅可分配比"全部"小的说法不成立，从严只允许仅本人
            return List.of(DataScope.SELF);
        }
        return new ArrayList<>(builtIn.subList(0, maxRank + 1));
    }
```

规则说明（写入 javadoc）：内置档按 全部>公司及以下>部门及以下>部门>仅本人 排序，可分配 = 档位 ≤ 权限集合档位（indexOf 越小档越大；permSetScope=COMPANY_AND_CHILD 时可分配 公司及以下/部门及以下/部门/仅本人，不可分配"全部"）。

测试 `AssignableDataScopeTest.java`：

```java
package top.mddata.console.service.permission;

import org.junit.jupiter.api.Test;
import top.mddata.base.mybatisflex.datapermission.DataScope;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 可分配数据范围档位规则测试（spec 第 4 节：权限集合控制能配到哪一档）。
 */
class AssignableDataScopeTest {

    @Test
    void 权限集合为全部时可分配所有档含自定义() {
        List<DataScope> result = RoleService.getAssignableDataScopes(DataScope.ALL);
        assertEquals(DataScope.values().length, result.size());
        assertTrue(result.contains(DataScope.CUSTOM));
    }

    @Test
    void 权限集合为本公司及以下时不可分配全部与自定义() {
        List<DataScope> result = RoleService.getAssignableDataScopes(DataScope.COMPANY_AND_CHILD);
        assertFalse(result.contains(DataScope.ALL));
        assertFalse(result.contains(DataScope.CUSTOM));
        assertTrue(result.contains(DataScope.COMPANY_AND_CHILD));
        assertTrue(result.contains(DataScope.SELF));
    }

    @Test
    void 权限集合为仅本人时只能分配仅本人() {
        assertEquals(List.of(DataScope.SELF), RoleService.getAssignableDataScopes(DataScope.SELF));
    }

    @Test
    void 权限集合为空视为全部() {
        assertEquals(DataScope.values().length, RoleService.getAssignableDataScopes(null).size());
    }
}
```

- [ ] **Step 2: RoleService 接口 + RoleServiceImpl 实现**

接口追加（含 Step 1 的静态方法）：

```java
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
```

实现（RoleServiceImpl，注入 `top.mddata.base.util.ContextUtil` 无注入——静态调用）：

```java
    @Override
    @Transactional(readOnly = true)
    public Role getPermSetRoleOfCurrentOperator() {
        Integer nature = ContextUtil.getCurrentTopCompanyNature();
        ArgumentAssert.notNull(nature, "当前用户的组织性质未知，无法确定可分配范围");
        return getOne(QueryWrapper.create()
                .eq(Role::getRoleCategory, RoleCategoryEnum.PERM_SET.getCode())
                .eq(Role::getOrgNature, nature).eq(Role::getState, true));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataScope> getAssignableDataScopes() {
        Role permSet = getPermSetRoleOfCurrentOperator();
        DataScope permSetScope = permSet == null ? null
                : DataScope.getByCode(permSet.getDataScope());
        return RoleService.getAssignableDataScopes(permSetScope);
    }
```

验证 `ContextUtil.getCurrentTopCompanyNature()` 存在（AuthServiceImpl 登录时写入了 TOP_COMPANY_NATURE；若 ContextUtil 无此 getter，改用 `StpUtil.getTokenSession().get(...)` 同款 key 读取并在报告中说明）。`DataScope.getByCode` 的注意事项同 Task 3。RoleServiceImpl 需要 import `DataScope`、`ContextUtil`；`RoleCategoryEnum` 已在用。

- [ ] **Step 3: RoleController 三个只读端点**

```java
    @GetMapping("/assignableDataScopes")
    @Operation(summary = "可分配的数据范围档位", description = "按当前操作人的权限集合计算")
    @RequestLog(value = "查询可分配的数据范围档位", logType = RequestLog.LogType.QUERY)
    public R<List<Map<String, String>>> assignableDataScopes() {
        List<Map<String, String>> list = superService.getAssignableDataScopes().stream()
                .map(scope -> Map.of("code", scope.getCode(), "name", scope.getDesc()))
                .toList();
        return R.success(list);
    }

    @GetMapping("/assignableAppIds")
    @Operation(summary = "可分配的应用id集合", description = "取当前操作人权限集合角色已分配的应用")
    @RequestLog(value = "查询可分配的应用id集合", logType = RequestLog.LogType.QUERY)
    public R<List<Long>> assignableAppIds() {
        Role permSet = superService.getPermSetRoleOfCurrentOperator();
        if (permSet == null) {
            return R.success(List.of());
        }
        List<Long> appIds = roleAppRelService.list(QueryWrapper.create()
                        .eq(RoleAppRel::getRoleId, permSet.getId()))
                .stream().map(RoleAppRel::getAppId).distinct().toList();
        return R.success(appIds);
    }

    @GetMapping("/assignableResourceIds")
    @Operation(summary = "可分配的资源id集合", description = "取当前操作人权限集合角色已分配的资源")
    @RequestLog(value = "查询可分配的资源id集合", logType = RequestLog.LogType.QUERY)
    public R<Map<Long, Collection<Long>>> assignableResourceIds() {
        Role permSet = superService.getPermSetRoleOfCurrentOperator();
        if (permSet == null) {
            return R.success(Map.of());
        }
        return R.success(roleResourceRelService.findResourceIdByRoleId(permSet.getId()));
    }
```

注意：`findResourceIdByRoleId`（RoleController:151 已暴露的同款服务方法）的真实签名/返回类型以 `RoleResourceRelService` 接口为准，照搬 RoleController:149-151 的既有调用方式；`RoleAppRel` 的实体路径与 RoleAppRelService 是否已在 RoleController 注入，以现状为准（未注入则注入或在 RoleServiceImpl 实现后调用，报告中说明选择）。

- [ ] **Step 4: 测试通过 + 编译检查点**

Run: `mvn -q test -pl mdp-apps/md-console/console-service -Dtest=AssignableDataScopeTest` → PASS
Run: `mvn -q clean compile -DskipTests` → BUILD SUCCESS

---

### Task 5: 全量验证

- [ ] **Step 1: 编译 + 测试 + checkstyle**

Run: `mvn -q clean compile -DskipTests` → BUILD SUCCESS
Run: `mvn test -pl mdp-base/md-db-mybatis-flex,mdp-apps/md-console/console-service,mdp-apps/md-public/md-common-pojo` → 全部 PASS（FileServiceImplTest 的 2 个失败为既有问题，不计）
Run: `mvn -q checkstyle:check -pl mdp-base/md-db-mybatis-flex,mdp-apps/md-console/console-service,mdp-apps/md-console/console-web,mdp-apps/md-public/md-common-config` → 退出码 0

- [ ] **Step 2: 人工冒烟（由用户执行）**

1. 角色管理编辑某角色：数据范围选"本部门及以下"保存。
2. 在有 `@DataPermission` 的查询（ConfigServiceImpl 演示方法）验证 SQL 过滤生效（Druid 监控 /druid/* 可看真实 SQL）。
3. 调 `GET /permission/role/assignableDataScopes` 验证返回档位符合当前操作人的权限集合。

---

## Self-Review 记录

- **Spec 覆盖**：§4 数据权限五档+自定义实现 → Task 1/2/3；§4 "可分配范围受权限集合控制"（应用/菜单/数据权限）→ Task 4；§8 改造点 6、7 覆盖。
- **占位符**：Task 2/3/4 各有一处"以现状/版本为准"的适配说明（lambda 写法、Row API、findResourceIdByRoleId 签名），均给出了明确的备选方案与报告要求，非遗漏。
- **类型一致性**：`DataScope` code 在 Task 1 定义、Task 3/4 消费；`CurrentUserRole` 三参构造在 Task 2 定义、Task 3 消费；`getAssignableDataScopes` 静态版在 Task 4 Step 1 定义、Step 2 实例方法委托。
- **已知留尾**：`@DataPermission` 目前仅 ConfigServiceImpl 一处使用，业务页面逐个点亮数据权限属后续工作（spec 未要求本计划全量铺开）；前端角色管理页接 assignable* 接口属计划 5。

## 终审决议（2026-09-24，项目所有者裁决）

1. **执行链开关**：`DataPermissionAutoConfiguration` 由 `mdp.database.flex.dataScope: true` 条件装配，当前 application.yml 未配置，整条链静默未启用。**决议：现在不开，计划 5 业务点亮时统一开启**；开启前必须逐表核对注解入参（deptId/userId/tableAlias），并先修正 ConfigServiceImpl 演示方法的列名（mdc_config 无 dept_id/create_user 列）。
2. **零角色语义**：已认证零角色用户（userId 非 null、roles 为空）调 `@DataPermission` 方法**默认拒绝**（恒假条件）；无登录上下文（worker/系统线程，userId 为 null）放行。已在终审修复中落地。
3. **写端越权缺口（计划 5 最高优先级）**：saveRoleResource/saveByDto/角色 save/update 目前不校验"可分配范围 ⊆ 权限集合"，恶意调用可越权分配应用/菜单/数据档位。计划 5 必须在三处写入口补后端校验。
