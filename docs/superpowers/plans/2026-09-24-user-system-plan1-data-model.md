# 用户体系重构 · 计划 1：数据模型与内置数据 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按《docs/用户体系/用户角色组织权限体系设计.md》第 8 节改造点 1-4，完成表结构改造（`mdc_user` 删 `user_type`、`mdc_role` 删 `role_type`、`mdc_org` 加 `nature`、废弃 `mdc_org_nature`）与内置数据基线（3 根公司 + 默认部门 + 8 内置角色），保证全工程编译通过。

**Architecture:** 组织性质从独立表 `mdc_org_nature` 迁移为 `mdc_org.nature` 字段（部门冗余同步、读取时取顶级公司）；身份相关字段（userType/roleType）连代码带数据整体下线；内置组织/角色 ID 在 `docs/mdp.sql` 中已存在且跨环境稳定，Java 侧用常量对齐。

**Tech Stack:** Java 17 / Spring Boot 3 / MyBatis-Flex / Lombok / MySQL 8（建库脚本 `docs/mdp.sql`，无 Flyway）

## Global Constraints

- **禁止任何 git add / commit / push 操作**（用户全局规范）。任务完成检查点 = 编译通过 + 测试通过，不写提交步骤。
- 所有交互、注释、错误信息使用简体中文；代码标识符用英文。
- 最大行长度 100 字符；使用 Lombok；不引入新依赖。
- 禁止读取/修改 yml 中的中间件连接与密码配置。
- 删除文件属于本计划已确认范围（spec 第 8 节改造点 1-3 明确"废弃 OrgNature 实体、OrgNatureService、UserTypeEnum"），除此之外不得删除任何文件。
- 编译命令（仓库根目录 `/Users/tangyh/gitee/mdp` 执行）：`mvn -q clean compile -DskipTests`
- 测试命令：`mvn -q test -pl mdp-apps/md-public/md-common-pojo`

## 关键背景（执行者必读）

- `docs/mdp.sql` 是唯一建库脚本，包含 DDL + 内置数据 INSERT，本计划直接改它。
- 已存在的内置数据（跨环境固定 ID，禁止改动 ID 本身）：
  - 公司：`687106879444787201` 内置运维公司（性质 99）、`687106879444787202` 内置开发者（性质 90）、`687106879444787203` 内置总公司（性质 1）
  - 角色（code 即稳定标识）：`OPERATIONS_ADMIN_COLL`(30,99)、`OPERATIONS_ADMIN`(20,99)、`ADMIN`(20,1)、`DEVELOPER_ADMIN`(20,90)、`DEFAULT_DEVELOPER`(10,90)、`DEFAULT_USER`(10,1)、`ADMIN_COLL`(30,1)、`DEVELOPER_ADMIN_COLL`(30,90) —— 正好是 spec 要求的 8 个内置角色，只需改名称、不需新增
- `RoleCode.SUPER_ADMIN` 常量在 Java 中无任何引用（已确认），且 SQL 中不存在该 code 的角色，本计划将其移除并按 SQL 实际 code 补齐常量。
- spec 决策：角色 `orgNature` 存性质编码不存 orgId；身份由角色决定（计划 2 实现判定组件，本计划只拆 userType）。

---

### Task 1: OrgNatureEnum 改名 + OrgBase 增加 nature 字段

**Files:**
- Modify: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/enumeration/organization/OrgNatureEnum.java`
- Modify: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/entity/base/OrgBase.java`
- Modify: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/permission/impl/RoleServiceImpl.java:80,90`
- Test: `mdp-apps/md-public/md-common-pojo/src/test/java/top/mddata/common/enumeration/organization/OrgNatureEnumTest.java`

**Interfaces:**
- Produces: `OrgNatureEnum.HEAD_COMPANY(1,"总公司")`（替代 `DEFAULT`）、`OrgNatureEnum.DEVELOPER(90,"开发者")`、`OrgNatureEnum.OPERATIONS(99,"运营")`；`OrgBase.getNature()/setNature(Integer)`。后续所有任务依赖这两个产物。

- [ ] **Step 1: 写失败测试**

检查 `mdp-apps/md-public/md-common-pojo/pom.xml` 是否已有 `org.junit.jupiter:junit-jupiter`（test scope）。没有则在 `<dependencies>` 中添加（版本由父工程管理，不写 version；若编译报找不到包，再加 `<version>5.10.2</version>`）：

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

创建 `OrgNatureEnumTest.java`：

```java
package top.mddata.common.enumeration.organization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 组织性质枚举基线测试：编码与名称是跨环境契约，改动必须显式感知。
 */
class OrgNatureEnumTest {

    @Test
    void 内置三个组织性质且编码稳定() {
        assertEquals(3, OrgNatureEnum.values().length);
        assertEquals(1, OrgNatureEnum.HEAD_COMPANY.getCode());
        assertEquals("总公司", OrgNatureEnum.HEAD_COMPANY.getDesc());
        assertEquals(90, OrgNatureEnum.DEVELOPER.getCode());
        assertEquals("开发者", OrgNatureEnum.DEVELOPER.getDesc());
        assertEquals(99, OrgNatureEnum.OPERATIONS.getCode());
        assertEquals("运营", OrgNatureEnum.OPERATIONS.getDesc());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q test -pl mdp-apps/md-public/md-common-pojo -Dtest=OrgNatureEnumTest`
Expected: 编译失败，`HEAD_COMPANY` 不存在。

- [ ] **Step 3: 修改 OrgNatureEnum**

`DEFAULT(1, "默认")` 改为 `HEAD_COMPANY(1, "总公司")`；`OPERATIONS(99, "运维")` 改为 `OPERATIONS(99, "运营")`；`DEVELOPER` 不动。类注释同步改为 `[1-总公司 90-开发者 99-运营]`。

- [ ] **Step 4: OrgBase 增加 nature 字段**

在 `orgType` 字段之后（`OrgBase.java:43` 之后）插入：

```java
    /**
     * 组织性质
     * [1-总公司 90-开发者 99-运营]
     * 一个组织有且只有一个性质；部门冗余同步所属公司的性质，读取时以顶级公司为准
     */
    private Integer nature;
```

- [ ] **Step 5: 修复 RoleServiceImpl 引用**

`RoleServiceImpl.java:80` 与 `:90` 两处 `OrgNatureEnum.DEFAULT.getCode()` 改为 `OrgNatureEnum.HEAD_COMPANY.getCode()`。

- [ ] **Step 6: 运行测试确认通过**

Run: `mvn -q test -pl mdp-apps/md-public/md-common-pojo -Dtest=OrgNatureEnumTest`
Expected: PASS

- [ ] **Step 7: 检查点——编译**

Run: `mvn -q clean compile -DskipTests`
Expected: 除 `OrgNatureEnum.DEFAULT` 的其他引用点（OrgServiceImpl、SsoUserServiceImpl、OrgOpenServiceImpl，Task 3 处理）报错外无新错误。若有其他文件引用 `DEFAULT`，一并改为 `HEAD_COMPANY`。

---

### Task 2: mdp.sql 表结构改造

**Files:**
- Modify: `docs/mdp.sql`（mdc_org DDL :827-844、mdc_org_nature 块 :869-893、mdc_role DDL :1359-1378 及 INSERT :1384-1391、mdc_user DDL :2041 起及 INSERT :2080 起）

**Interfaces:**
- Produces: 新表结构 = 后续所有计划的存储基线：`mdc_org.nature`、`mdc_user` 无 `user_type`、`mdc_role` 无 `role_type`、无 `mdc_org_nature` 表。

- [ ] **Step 1: mdc_org DDL 加 nature**

在 `mdc_org` 建表语句的 `org_type` 行之后插入：

```sql
  `nature` int DEFAULT '1' COMMENT '组织性质\n[1-总公司 90-开发者 99-运营]',
```

- [ ] **Step 2: 删除 mdc_org_nature 整块**

删除 `-- Table structure for mdc_org_nature` 注释行起、到其 `Records` 的 `COMMIT;` 止的完整块（当前 :869-893），包括 `DROP TABLE`、`CREATE TABLE` 与全部 7 条 INSERT。

- [ ] **Step 3: mdc_role DDL 删 role_type，同步 8 条 INSERT**

删除建表语句中的行：

```sql
  `role_type` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '10' COMMENT '角色类型\n[10-功能角色 20-桌面角色 30-数据角色]',
```

8 条 `mdc_role` INSERT 逐条修改：列清单中删除 `` `role_type` ``，VALUES 中删除其对应值（每条都是第 5 个值 `'10'`，位于 `role_category` 值之后）。示例（其余 7 条同法）：

修改前：
```sql
INSERT INTO `mdc_role` (`id`, `code`, `name`, `role_category`, `role_type`, `org_nature`, ...) VALUES (30589663320895490, 'OPERATIONS_ADMIN', '运维管理员', '20', '10', 99, ...);
```
修改后：
```sql
INSERT INTO `mdc_role` (`id`, `code`, `name`, `role_category`, `org_nature`, ...) VALUES (30589663320895490, 'OPERATIONS_ADMIN', '运维管理员', '20', 99, ...);
```

同时把 `mdc_role` DDL 中 `org_nature` 的注释改为 `组织性质\n[1-总公司 90-开发者 99-运营]`。

- [ ] **Step 4: mdc_user DDL 删 user_type，同步全部 INSERT**

删除建表语句中的行：

```sql
  `user_type` int DEFAULT '1' COMMENT '人员类型\r\n[1-普通用户 2-管理员 90-开发者 99-运维管理员]',
```

所有 `mdc_user` INSERT（:2080 起，约 8-9 条）逐条修改：列清单删除 `` `user_type` ``，VALUES 删除同位置对应值。

- [ ] **Step 5: 检查点——SQL 语法人工核对**

Run: `grep -n "user_type\|role_type\|mdc_org_nature" docs/mdp.sql`
Expected: 0 匹配（无任何残留）。

---

### Task 3: OrgNature 代码体系下线

**Files:**
- Delete: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/entity/OrgNature.java`
- Delete: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/entity/base/OrgNatureBase.java`
- Delete: `mdp-apps/md-public/md-common-dao/src/main/java/top/mddata/common/mapper/OrgNatureMapper.java`
- Delete: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/organization/OrgNatureService.java`
- Delete: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/organization/impl/OrgNatureServiceImpl.java`
- Delete: `mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/dto/organization/OrgNatureDto.java`
- Delete: `mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/query/organization/OrgNatureQuery.java`
- Delete: `mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/vo/organization/OrgNatureVo.java`
- Modify: `mdp-apps/md-console/console-service/.../organization/impl/OrgServiceImpl.java`
- Modify: `mdp-apps/md-workbench/workbench-service/.../service/SsoUserService.java:138`
- Modify: `mdp-apps/md-workbench/workbench-service/.../service/impl/SsoUserServiceImpl.java:304-317`
- Modify: `mdp-apps/md-workbench/workbench-service/.../service/impl/AuthServiceImpl.java`（findOrg 内两处"查询单位/顶级单位的组织性质"）
- Modify: `mdp-apps/md-api/api-service/src/main/java/top/mddata/api/impl/OrgOpenServiceImpl.java:48,76-79`

**Interfaces:**
- Consumes: `OrgBase.getNature()`（Task 1）、`OrgNatureEnum.HEAD_COMPANY/OPERATIONS`（Task 1）
- Produces: `Integer SsoUserService.getOrgNatureByOrgId(Long id)`（返回类型由 `OrgNature` 改为 `Integer`，可能为 null）。计划 2/3 依赖此签名。

- [ ] **Step 1: OrgServiceImpl 改造**

1. 删除字段 `private final OrgNatureService orgNatureService;` 及 `OrgNature`、`OrgNatureService` 的 import。
2. `saveAfter`（:191 起）删除 OrgNature 创建块（方法开头 5 行），保留事件推送。
3. `removeByIds` 中删除 `orgNatureService.remove(...)` 一行及上方"// 删除组织性质"注释。
4. 新增 `saveBefore`（当前不存在，参考同类 `RoleServiceImpl.saveBefore` 的钩子签名）：

```java
    @Override
    protected Org saveBefore(Object save) {
        Org entity = super.saveBefore(save);
        Org parent = entity.getParentId() != null ? getById(entity.getParentId()) : null;
        // 部门冗余同步所属公司性质；根节点默认总公司性质
        entity.setNature(parent != null ? parent.getNature() : OrgNatureEnum.HEAD_COMPANY.getCode());
        return entity;
    }
```

5. `updateBefore` 中 `fill(org, parent);` 之后追加一行：

```java
        org.setNature(parent != null ? parent.getNature() : OrgNatureEnum.HEAD_COMPANY.getCode());
```

- [ ] **Step 2: SsoUserService 接口改签名**

`SsoUserService.java:134-138` 的 `getOrgNatureByOrgId` 改为：

```java
    /**
     * 根据组织id查询组织性质
     * @param id 组织id
     * @return 组织性质（1-总公司 90-开发者 99-运营），组织不存在时返回 null
     */
    Integer getOrgNatureByOrgId(Long id);
```

删除 `OrgNature` import。

- [ ] **Step 3: SsoUserServiceImpl 改造**

`getTopCompanyIsAdminById` 与 `getOrgNatureByOrgId`（:304-317）整体替换为：

```java
    @Override
    @Transactional(readOnly = true)
    public boolean getTopCompanyIsAdminById(Long id) {
        Org org = getOrgByIdCache(id);
        return org != null && OrgNatureEnum.OPERATIONS.eq(org.getNature());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getOrgNatureByOrgId(Long id) {
        Org org = getOrgByIdCache(id);
        return org != null ? org.getNature() : null;
    }
```

删除 `orgNatureMapper` 字段及 `OrgNatureMapper`、`OrgNature` import。

- [ ] **Step 4: AuthServiceImpl 两处调用点适配**

`findOrg` 中"查询单位的组织性质"块改为：

```java
        // 查询单位的组织性质
        if (defaultCompany != null) {
            currentCompanyNature = ssoUserService.getOrgNatureByOrgId(defaultCompany.getId());
        }
```

"查询顶级单位的组织性质"块改为：

```java
        // 查询顶级单位的组织性质
        if (rootCompany != null) {
            currentTopCompanyNature = ssoUserService.getOrgNatureByOrgId(rootCompany.getId());
        }
```

删除 `OrgNature` import。

- [ ] **Step 5: OrgOpenServiceImpl 改造**

1. 删除字段 `private final OrgNatureMapper orgNatureMapper;` 及 `OrgNature`、`OrgNatureMapper` import（保留 `OrgNatureEnum` import）。
2. `save(entity);` 之前插入性质赋值，并删除其后 4 行 OrgNature insert 块：

```java
        entity.setNature(parent != null ? parent.getNature() : OrgNatureEnum.HEAD_COMPANY.getCode());

        save(entity);
```

- [ ] **Step 6: 删除 OrgNature 全部文件**

删除 Files 列出的 8 个文件。同时删除编译产物残留引用检查：

Run: `grep -rn "OrgNature\b\|OrgNatureMapper\|OrgNatureService" --include="*.java" mdp-apps mdp-base mdp-sdk | grep -v "OrgNatureEnum" | grep -v "/target/"`
Expected: 0 匹配。有匹配则继续修复。

- [ ] **Step 7: 检查点——编译**

Run: `mvn -q clean compile -DskipTests`
Expected: 通过（允许 Task 4/5 尚未处理的 userType/roleType 报错之外无其他错误；若报错仅剩这些，继续）。

---

### Task 4: mdc_user.userType 代码下线

**Files:**
- Modify: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/entity/base/UserBase.java:126-130`
- Delete: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/enumeration/organization/UserTypeEnum.java`
- Modify: `mdp-apps/md-api/api-service/src/main/java/top/mddata/api/impl/UserOpenServiceImpl.java:96,112`
- Modify: `mdp-apps/md-console/console-service/.../organization/impl/UserServiceImpl.java:347-359`
- Modify: `mdp-apps/md-public/md-common-dao/src/main/java/top/mddata/common/mapper/UserMapper.java:104-117`
- Modify: `mdp-apps/md-console/console-service/.../dashboard/impl/DashboardUserServiceImpl.java:13,146,151,179-190`
- Modify: `mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/vo/permission/UserinfoVo.java:27`

**Interfaces:**
- Consumes: `OrgNatureEnum`（Task 1）、`OrgService.getByIdCache`（已有）
- Produces: `UserMapper.countByNature()` 供仪表盘使用；`UserServiceImpl.saveDefRole` 新逻辑 = 计划 2 注册流程改造的基础。

- [ ] **Step 1: UserBase 删字段、UserinfoVo 删字段、删除 UserTypeEnum**

1. `UserBase.java` 删除 `userType` 字段（含注释，:126-130）。
2. `UserinfoVo.java:27` 删除 `private Integer userType;`。
3. 删除 `UserTypeEnum.java`。

- [ ] **Step 2: UserOpenServiceImpl 去 userType**

删除 :96 与 :112 两行 `user.setUserType(UserTypeEnum.USER.getCode());` 及 `UserTypeEnum` import。

- [ ] **Step 3: UserServiceImpl.saveDefRole 重写**

现有逻辑（按 userType 查字典 `REG_BIND_ROLE`）整体替换为按用户组织性质判定：

```java
    private void saveDefRole(User user) {
        String code = RoleCode.DEFAULT_USER;

        UserOrgRel rel = userOrgRelService.getOne(
                QueryWrapper.create().eq(UserOrgRel::getUserId, user.getId()));
        if (rel != null) {
            Org org = orgService.getByIdCache(rel.getOrgId());
            if (org != null && OrgNatureEnum.DEVELOPER.eq(org.getNature())) {
                code = RoleCode.DEFAULT_DEVELOPER;
            }
        }

        roleService.joinTheRole(code, user.getId());
    }
```

配套改动：
1. `UserServiceImpl` 新增注入 `private final OrgService orgService;`（OrgService 不依赖 UserService，无循环依赖）。
2. 删除不再使用的 `dictItemService.getDictItemByUniqKey(...)` 逻辑。执行 `grep -n "dictItemService" mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/organization/impl/UserServiceImpl.java`，若仅剩 saveDefRole 一处使用，则连字段 `private final DictItemService dictItemService;` 与 import 一并删除；若还有其他使用点则保留字段。
3. 删除 import：`EchoDictType`、`DictItem`、`UserTypeEnum`；新增 import：`Org`、`OrgNatureEnum`（`UserOrgRel`、`QueryWrapper` 已有）。

- [ ] **Step 4: UserMapper.countByType 改为 countByNature**

`UserMapper.java:104-117` 的 `countByType` 方法整体替换为：

```java
    /**
     * 按组织性质统计用户数。
     *
     * <p>手写 SQL，已手动过滤 deleted_at = 0。用户属于多个组织时按组织分别计数。</p>
     *
     * @return 组织性质分布，key=nature(1-总公司 90-开发者 99-运营)、count
     */
    @Select({
            """
            SELECT
                o.nature AS code,
                COUNT(*) AS count
              FROM mdc_user u
              JOIN mdc_user_org_rel r ON r.user_id = u.id
              JOIN mdc_org o ON o.id = r.org_id
             WHERE u.deleted_at = 0
               AND o.deleted_at = 0
               AND o.nature IS NOT NULL
             GROUP BY o.nature
            """
    })
    List<Map<String, Object>> countByNature();
```

- [ ] **Step 5: DashboardUserServiceImpl 适配**

1. `getTypeDistribution()`（方法名与接口保持不变，避免牵动 Controller）内 `userMapper.countByType()` 改为 `userMapper.countByNature()`，:151 的 `convertUserType(...)` 改为 `convertNature(...)`。
2. `convertUserType` 私有方法整体替换为：

```java
    private String convertNature(Long code) {
        if (code == null) {
            return null;
        }
        for (OrgNatureEnum enumVal : OrgNatureEnum.values()) {
            if (enumVal.getCode().equals(code.intValue())) {
                return enumVal.getDesc();
            }
        }
        return String.valueOf(code);
    }
```

3. import `UserTypeEnum` 换成 `OrgNatureEnum`。

- [ ] **Step 6: 检查点——零残留 + 编译**

Run: `grep -rn "userType\|UserTypeEnum\|getUserType\|setUserType\|countByType" --include="*.java" mdp-apps mdp-base mdp-sdk | grep -v "/target/"`
Expected: 0 匹配（前端仓库不在本工程内，另行处理）。

Run: `mvn -q clean compile -DskipTests`
Expected: 通过（除 Task 5 的 roleType 报错外无其他错误）。

---

### Task 5: mdc_role.roleType 代码下线

**Files:**
- Modify: `mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/entity/permission/base/RoleBase.java:50-54`
- Delete: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/enumeration/permission/RoleTypeEnum.java`
- Modify: `mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/dto/permission/RoleDto.java`
- Modify: `mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/dto/permission/RoleTemploateDto.java`
- Modify: `mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/vo/permission/RoleVo.java`
- Modify: `mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/query/permission/RoleQuery.java`

**Interfaces:**
- Produces: `mdc_role` 实体模型 = spec 第 3 节定稿字段（code/name/roleCategory/orgNature/templateRole/state/remarks）。前端表单中的 roleType 字段需在前端仓库另行删除（本计划不含前端）。

- [ ] **Step 1: 逐文件删除 roleType**

1. `RoleBase.java` 删除 `roleType` 字段（含注释，:50-54）。
2. `RoleDto.java`、`RoleTemploateDto.java`、`RoleVo.java`、`RoleQuery.java` 各自删除 `roleType` 字段及 `RoleTypeEnum` 相关 import（这些文件中 roleType 均只有字段声明，无逻辑）。
3. 删除 `RoleTypeEnum.java`。

- [ ] **Step 2: 检查点——零残留 + 全量编译**

Run: `grep -rn "roleType\|RoleTypeEnum\|getRoleType\|setRoleType" --include="*.java" mdp-apps mdp-base mdp-sdk | grep -v "/target/"`
Expected: 0 匹配。

Run: `mvn -q clean compile -DskipTests`
Expected: **全工程编译通过**。这是计划 1 的第一个完整编译绿点。

---

### Task 6: RoleCode 对齐 + BuiltInOrgId 常量 + mdp.sql 内置数据

**Files:**
- Modify: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/constant/RoleCode.java`
- Create: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/constant/BuiltInOrgId.java`
- Modify: `docs/mdp.sql`（mdc_org 内置 3 公司行、新增默认部门行、mdc_role 8 行改名、删除 REG_BIND_ROLE 字典行）
- Test: `mdp-apps/md-public/md-common-pojo/src/test/java/top/mddata/common/constant/BuiltInDataConstantTest.java`

**Interfaces:**
- Produces: `BuiltInOrgId.OPERATIONS_CENTER / DEVELOPER_PLATFORM / HEAD_COMPANY / DEFAULT_DEPT`（计划 2 注册流程直接用）；`RoleCode.OPERATIONS_ADMIN`（计划 3 硬保护用）等 8 个角色编码常量。

- [ ] **Step 1: 写失败测试**

创建 `BuiltInDataConstantTest.java`：

```java
package top.mddata.common.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 内置数据常量基线测试：常量值必须与 docs/mdp.sql 中的内置数据一致。
 */
class BuiltInDataConstantTest {

    @Test
    void 内置组织ID与建库脚本一致() {
        assertEquals(687106879444787201L, BuiltInOrgId.OPERATIONS_CENTER);
        assertEquals(687106879444787202L, BuiltInOrgId.DEVELOPER_PLATFORM);
        assertEquals(687106879444787203L, BuiltInOrgId.HEAD_COMPANY);
        assertEquals(687106879444787204L, BuiltInOrgId.DEFAULT_DEPT);
    }

    @Test
    void 内置角色编码与建库脚本一致() {
        assertEquals("OPERATIONS_ADMIN", RoleCode.OPERATIONS_ADMIN);
        assertEquals("OPERATIONS_ADMIN_COLL", RoleCode.OPERATIONS_ADMIN_COLL);
        assertEquals("ADMIN", RoleCode.ADMIN);
        assertEquals("ADMIN_COLL", RoleCode.ADMIN_COLL);
        assertEquals("DEVELOPER_ADMIN", RoleCode.DEVELOPER_ADMIN);
        assertEquals("DEVELOPER_ADMIN_COLL", RoleCode.DEVELOPER_ADMIN_COLL);
        assertEquals("DEFAULT_DEVELOPER", RoleCode.DEFAULT_DEVELOPER);
        assertEquals("DEFAULT_USER", RoleCode.DEFAULT_USER);
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q test -pl mdp-apps/md-public/md-common-pojo -Dtest=BuiltInDataConstantTest`
Expected: 编译失败（`BuiltInOrgId`、`RoleCode.OPERATIONS_ADMIN` 不存在）。

- [ ] **Step 3: 创建 BuiltInOrgId**

```java
package top.mddata.common.constant;

/**
 * 系统内置组织ID。
 *
 * <p>取值与 docs/mdp.sql 中的内置数据一一对应，属于跨环境契约，禁止修改。</p>
 */
public interface BuiltInOrgId {
    /**
     * 运营中心（组织性质=99 运营）
     */
    long OPERATIONS_CENTER = 687106879444787201L;

    /**
     * 开发者平台（组织性质=90 开发者）
     */
    long DEVELOPER_PLATFORM = 687106879444787202L;

    /**
     * 总公司（组织性质=1 总公司）
     */
    long HEAD_COMPANY = 687106879444787203L;

    /**
     * 总公司下的默认部门（普通用户注册落入）
     */
    long DEFAULT_DEPT = 687106879444787204L;
}
```

- [ ] **Step 4: 重写 RoleCode**

整体替换为（删除无引用且 SQL 中不存在的 `SUPER_ADMIN`，按 SQL 实际 code 补齐 8 个常量）：

```java
package top.mddata.common.constant;

/**
 * 系统内置角色编码。
 *
 * <p>取值与 docs/mdp.sql 中 mdc_role 的 code 一一对应，属于跨环境契约，禁止修改。</p>
 */
public interface RoleCode {
    /**
     * [管理员角色] 运营管理员：系统最高权限，受硬保护
     */
    String OPERATIONS_ADMIN = "OPERATIONS_ADMIN";

    /**
     * [权限集合] 运营管理员权限集
     */
    String OPERATIONS_ADMIN_COLL = "OPERATIONS_ADMIN_COLL";

    /**
     * [管理员角色] 总公司管理员
     */
    String ADMIN = "ADMIN";

    /**
     * [权限集合] 总公司管理员权限集
     */
    String ADMIN_COLL = "ADMIN_COLL";

    /**
     * [管理员角色] 开发者管理员
     */
    String DEVELOPER_ADMIN = "DEVELOPER_ADMIN";

    /**
     * [权限集合] 开发者管理员权限集
     */
    String DEVELOPER_ADMIN_COLL = "DEVELOPER_ADMIN_COLL";

    /**
     * [普通角色] 开发者：新注册开发者默认绑定
     */
    String DEFAULT_DEVELOPER = "DEFAULT_DEVELOPER";

    /**
     * [普通角色] 普通用户：新注册用户默认绑定
     */
    String DEFAULT_USER = "DEFAULT_USER";
}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `mvn -q test -pl mdp-apps/md-public/md-common-pojo -Dtest=BuiltInDataConstantTest`
Expected: PASS

- [ ] **Step 6: mdp.sql 内置公司改名 + 补 nature**

修改 3 条内置公司 INSERT（:861-863）：名称 `内置运维公司`→`运营中心`、`内置开发者`→`开发者平台`、`内置总公司`→`总公司`；列清单末尾（`deleted_at` 前按实际列序）加入 `` `nature` `` 并填值：运营中心 `99`、开发者平台 `90`、总公司 `1`。其余 mdc_org 行不加 nature 列，走 DDL 默认值 `1`。

- [ ] **Step 7: mdp.sql 新增默认部门**

在 mdc_org Records 块 `COMMIT;` 前追加：

```sql
INSERT INTO `mdc_org` (`id`, `name`, `org_type`, `short_name`, `parent_id`, `tree_path`, `weight`, `state`, `remarks`, `created_at`, `created_by`, `updated_at`, `updated_by`, `deleted_by`, `deleted_at`) VALUES (687106879444787204, '默认部门', '20', NULL, 687106879444787203, '/687106879444787203/687106879444787204/', 1, b'1', '系统内置部门，请勿删除', '2026-09-24 00:00:00.000', 11, '2026-09-24 00:00:00.000', 11, NULL, 0);
```

- [ ] **Step 8: mdp.sql 内置角色改名**

8 条 mdc_role INSERT 中改 3 处名称：`'运维管理员权限集'`→`'运营管理员权限集'`、`'运维管理员'`→`'运营管理员'`（OPERATIONS_ADMIN 行）、`'新注册用户'`→`'普通用户'`（DEFAULT_USER 行）。code 一律不动。

- [ ] **Step 9: mdp.sql 删除 REG_BIND_ROLE 字典**

删除包含 `REG_BIND_ROLE` 的字典类型行及其全部字典项行（:92 起的 mdc_dict Records 块内，`grep -n "REG_BIND_ROLE" docs/mdp.sql` 定位）。同时检查 Java 侧 `EchoDictType.Workbench.REG_BIND_ROLE` 常量：Task 4 已删除其唯一使用点，执行 `grep -rn "REG_BIND_ROLE" --include="*.java" mdp-apps | grep -v "/target/"`，若只剩常量声明本身则删除该常量。

注意：`BUILT_IN_DEVELOPER` 配置行**本计划保留**——`UserServiceImpl.saveDefOrg` 仍在使用，由计划 2 移除。

---

### Task 7: 全量验证

**Files:** 无新增修改，纯验证。

- [ ] **Step 1: 全量编译**

Run: `mvn -q clean compile -DskipTests`（仓库根目录）
Expected: BUILD SUCCESS

- [ ] **Step 2: 单测**

Run: `mvn -q test -pl mdp-apps/md-public/md-common-pojo`
Expected: OrgNatureEnumTest、BuiltInDataConstantTest 全部 PASS

- [ ] **Step 3: Checkstyle**

Run: `mvn -q checkstyle:check -pl mdp-apps/md-public/md-common-pojo,mdp-apps/md-console/console-service,mdp-apps/md-workbench/workbench-service,mdp-apps/md-api/api-service`
Expected: 无新增违规（若历史遗留违规已存在，不属本计划范围）

- [ ] **Step 4: SQL 最终核对**

Run: `grep -c "INSERT INTO \`mdc_role\`" docs/mdp.sql` → Expected: 8
Run: `grep -n "默认部门\|运营中心\|开发者平台\|总公司" docs/mdp.sql | head -12` → 人工核对内置公司与部门名称正确
Run: `grep -n "user_type\|role_type\|mdc_org_nature\|REG_BIND_ROLE" docs/mdp.sql` → Expected: 0 匹配

- [ ] **Step 5: 人工冒烟（由用户执行，涉及本地数据库，计划执行者不代做）**

1. 用改造后的 `docs/mdp.sql` 重建本地开发库。
2. 启动 `BootServerApplication`，用 `ops_admin/admin` 登录控制台。
3. 验证：组织管理可见三家公司与默认部门；角色管理列表 8 个内置角色名称正确；用户管理列表正常；仪表盘"用户类型分布"卡片按组织性质展示。

---

## Self-Review 记录

- **Spec 覆盖**：spec 第 8 节改造点 1-4 全部覆盖（1→Task 4、2→Task 5、3→Task 1-3、4→Task 2/6）。改造点 5-10 属计划 2/3/4，不在本计划。
- **占位符**：无 TBD/TODO；所有代码片段为完整可粘贴实现。
- **类型一致性**：`getOrgNatureByOrgId` 返回 `Integer`（Task 3 定义，AuthServiceImpl 两处按此消费）；`OrgNatureEnum.HEAD_COMPANY` 在 Task 1 定义、Task 3/4 消费；`BuiltInOrgId`/`RoleCode` 新常量在 Task 6 定义、计划 2/3 消费。
- **已知留尾**（显式说明，非遗漏）：`BUILT_IN_DEVELOPER` 配置、`saveDefOrg` 现有逻辑、前端 roleType/userType 字段，均由后续计划处理。
