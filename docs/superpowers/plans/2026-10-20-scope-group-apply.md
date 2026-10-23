# 权限组申请与审核 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 开发者在 web-open 为应用申请/查看权限组，管理员在 web-console 审核申请，审核通过写入 mdo_app_group_rel 生效。

**Architecture:** 新建 `mdo_app_scope_apply` 申请表（一单一组），后端 md-open 模块双 Controller（`/client/*` 开发者端、`/admin/*` 审核端）完全参照 AppApply 模式；前端 web-open 应用详情新增「权限」tab（三区），web-console 新增审核菜单页。

**Tech Stack:** Java 17 + Spring Boot 3 + MyBatis-Flex + Lombok + JUnit5；Vue3 + vben（vxe-table、useVbenModal、useVbenVxeGrid）

**Spec:** `docs/superpowers/specs/2026-10-20-scope-group-apply-design.md`

## Global Constraints

- **禁止任何 git add / git commit / git push 操作**（项目硬性规定，任务中的验证步骤替代 commit 步骤）
- 实体分层：`entity/base/XxxBase.java` 只放表字段；业务字段放 `entity/Xxx.java`
- Mapper 接口必须加 `@Repository` 注解（注册靠组件扫描，非 @MapperScan）
- Service 继承模式：`XxxService extends SuperService<Xxx>`、`XxxServiceImpl extends SuperServiceImpl<XxxMapper, Xxx> implements XxxService`
- 统一返回 `top.mddata.base.base.R<T>`；分页入参 `PageParams<XxxQuery>`、出参 `R<Page<XxxVo>>`
- 审核状态复用现有枚举 `top.mddata.common.enumeration.AuditStatusEnum`（PENDING=1 待审批、APPROVED=2 通过、REJECTED=99 退回；本期不使用 INITIALIZED=0）
- 所有弹窗/抽屉必须使用 `useVbenModal` / `useVbenDrawer`（参照 `web-console/src/views/open/admin/appApply/modules/wrapper.vue`）
- 注释、错误消息、文案全部使用简体中文；错误处理 fail fast，用 `ArgumentAssert`
- TDD：Service 层先写测试再写实现；测试用 `org.junit.jupiter.api.Test`
- `docs/superpowers/` 被 gitignore，计划/设计文档执行前提醒用户备份

---

### Task 1: SQL 脚本（mdp.sql + 可执行脚本）

**Files:**
- Modify: `docs/mdp.sql`（mdo_scope_group 定义 + 末尾追加新表）
- Create: `docs/sql/2026-10-20-scope-group-apply.sql`

- [ ] **Step 1: 创建可执行 SQL 脚本**

创建 `docs/sql/2026-10-20-scope-group-apply.sql`：

```sql
-- 权限组申请与审核 - 数据库变更（2026-10-20）

-- 1. mdo_scope_group 新增简介字段
ALTER TABLE `mdo_scope_group`
  ADD COLUMN `intro` varchar(512) DEFAULT NULL COMMENT '简介' AFTER `name`;

-- 2. 新建权限组申请表
CREATE TABLE `mdo_app_scope_apply` (
  `id` bigint NOT NULL COMMENT '主键',
  `app_id` bigint NOT NULL COMMENT '所属应用',
  `group_id` bigint NOT NULL COMMENT '申请权限组',
  `apply_reason` varchar(512) NOT NULL COMMENT '申请理由',
  `audit_status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '审核状态\n[1-待审批 2-通过 99-退回]',
  `audit_at` datetime(3) DEFAULT NULL COMMENT '审核时间',
  `review_comments` varchar(255) DEFAULT NULL COMMENT '审核意见',
  `created_at` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `created_by` bigint DEFAULT '0' COMMENT '创建人ID',
  `updated_at` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_app_id` (`app_id`),
  KEY `idx_group_status` (`group_id`, `audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='权限组申请';
```

- [ ] **Step 2: 同步修改 docs/mdp.sql**

在 `docs/mdp.sql` 中：
1. `mdo_scope_group` 的 CREATE TABLE（约 3593 行）`name` 字段后追加：
```sql
  `intro` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '简介',
```
2. 文件末尾追加 `mdo_app_scope_apply` 的完整 CREATE TABLE（内容与 Step 1 一致，列加 CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci，与库中其他表风格一致）。

- [ ] **Step 3: 验证**

目检两处 SQL 语法正确；提醒用户在开发库执行 `docs/sql/2026-10-20-scope-group-apply.sql`。

---

### Task 2: ScopeGroup 实体加 intro 字段

**Files:**
- Modify: `mdp-apps/md-open/open-pojo/src/main/java/top/mddata/open/entity/admin/base/ScopeGroupBase.java`
- Modify: `mdp-apps/md-open/open-pojo/src/main/java/top/mddata/open/vo/admin/ScopeGroupVo.java`
- Modify: `mdp-apps/md-open/open-pojo/src/main/java/top/mddata/open/dto/admin/ScopeGroupDto.java`

- [ ] **Step 1: ScopeGroupBase 加字段**

在 `ScopeGroupBase.java` 的 `name` 字段后追加：

```java
    /**
     * 简介
     */
    private String intro;
```

- [ ] **Step 2: ScopeGroupVo / ScopeGroupDto 同步**

两个文件同样在 `name` 字段后追加（保持各文件原有注解风格，Vo 带 `@Schema(description = "简介")`）：

```java
    /**
     * 简介
     */
    private String intro;
```

- [ ] **Step 3: 编译验证**

Run: `mvn -q compile -pl mdp-apps/md-open/open-pojo -DskipTests`
Expected: BUILD SUCCESS（退出码 0）

---

### Task 3: AppScopeApply 实体层（Base/Entity/Dto/Vo/Query/Mapper）

**Files:**
- Create: `mdp-apps/md-open/open-pojo/src/main/java/top/mddata/open/entity/admin/base/AppScopeApplyBase.java`
- Create: `mdp-apps/md-open/open-pojo/src/main/java/top/mddata/open/entity/admin/AppScopeApply.java`
- Create: `mdp-apps/md-open/open-pojo/src/main/java/top/mddata/open/dto/admin/AppScopeApplyDto.java`
- Create: `mdp-apps/md-open/open-pojo/src/main/java/top/mddata/open/dto/admin/AppScopeApplyReviewDto.java`
- Create: `mdp-apps/md-open/open-pojo/src/main/java/top/mddata/open/vo/admin/AppScopeApplyVo.java`
- Create: `mdp-apps/md-open/open-pojo/src/main/java/top/mddata/open/query/admin/AppScopeApplyQuery.java`
- Create: `mdp-apps/md-open/open-dao/src/main/java/top/mddata/open/mapper/admin/AppScopeApplyMapper.java`

**Interfaces:**
- Produces:
  - `AppScopeApply`（实体，字段：id、appId、groupId、applyReason、auditStatus、auditAt、reviewComments + SuperEntity 审计字段）
  - `AppScopeApplyDto`（apply 入参：`@NotNull Long appId`、`@NotEmpty List<Long> groupIds`、`@NotBlank @Size(max=512) String applyReason`）
  - `AppScopeApplyReviewDto`（review 入参：`@NotNull Long id`、`@NotNull Boolean approve`、`@Size(max=255) String reviewComments`）
  - `AppScopeApplyVo`（含回显字段 `appName`、`groupName`）
  - `AppScopeApplyMapper extends SuperMapper<AppScopeApply>`

- [ ] **Step 1: 创建 AppScopeApplyBase**

```java
package top.mddata.open.entity.admin.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限组申请实体类。
 *
 * @author henhen6
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AppScopeApplyBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_app_scope_apply";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属应用
     */
    private Long appId;

    /**
     * 申请权限组
     */
    private Long groupId;

    /**
     * 申请理由
     */
    private String applyReason;

    /**
     * 审核状态
     * [1-待审批 2-通过 99-退回]
     */
    private Integer auditStatus;

    /**
     * 审核时间
     */
    private LocalDateTime auditAt;

    /**
     * 审核意见
     */
    private String reviewComments;

}
```

- [ ] **Step 2: 创建 AppScopeApply 实体**

```java
package top.mddata.open.entity.admin;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.open.entity.admin.base.AppScopeApplyBase;

import java.io.Serial;

/**
 * 权限组申请实体类。
 *
 * @author henhen6
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Table(AppScopeApplyBase.TABLE_NAME)
public class AppScopeApply extends AppScopeApplyBase {
    @Serial
    private static final long serialVersionUID = 1L;
}
```

- [ ] **Step 3: 创建 DTO（apply + review）**

`AppScopeApplyDto.java`：

```java
package top.mddata.open.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 权限组申请 入参。
 *
 * @author henhen6
 */
@Data
@Accessors(chain = true)
@Schema(description = "权限组申请")
public class AppScopeApplyDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属应用
     */
    @NotNull(message = "应用不能为空")
    @Schema(description = "所属应用")
    private Long appId;

    /**
     * 申请的权限组（一单一组，批量提交时后端循环生成多张申请单）
     */
    @NotEmpty(message = "请至少勾选一个权限组")
    @Schema(description = "申请权限组")
    private List<Long> groupIds;

    /**
     * 申请理由
     */
    @NotBlank(message = "请填写申请理由")
    @Size(max = 512, message = "申请理由长度不能超过{max}")
    @Schema(description = "申请理由")
    private String applyReason;
}
```

`AppScopeApplyReviewDto.java`：

```java
package top.mddata.open.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 权限组申请 审核入参。
 *
 * @author henhen6
 */
@Data
@Accessors(chain = true)
@Schema(description = "权限组申请审核")
public class AppScopeApplyReviewDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 申请单ID
     */
    @NotNull(message = "id不能为空")
    @Schema(description = "申请单ID")
    private Long id;

    /**
     * 是否通过
     */
    @NotNull(message = "审核结果不能为空")
    @Schema(description = "是否通过")
    private Boolean approve;

    /**
     * 审核意见（退回时必填，Service 层校验）
     */
    @Size(max = 255, message = "审核意见长度不能超过{max}")
    @Schema(description = "审核意见")
    private String reviewComments;
}
```

- [ ] **Step 4: 创建 Vo 和 Query**

`AppScopeApplyVo.java`：

```java
package top.mddata.open.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限组申请 出参。
 *
 * @author henhen6
 */
@Data
@Accessors(chain = true)
@Schema(description = "权限组申请")
public class AppScopeApplyVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "所属应用")
    private Long appId;

    @Schema(description = "申请权限组")
    private Long groupId;

    @Schema(description = "申请理由")
    private String applyReason;

    @Schema(description = "审核状态[1-待审批 2-通过 99-退回]")
    private Integer auditStatus;

    @Schema(description = "审核时间")
    private LocalDateTime auditAt;

    @Schema(description = "审核意见")
    private String reviewComments;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "创建人ID")
    private Long createdBy;

    // ===== 回显字段（非表字段，Service/Controller 层组装） =====

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "权限组名称")
    private String groupName;
}
```

`AppScopeApplyQuery.java`：

```java
package top.mddata.open.query.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限组申请 查询入参。
 *
 * @author henhen6
 */
@Data
@Accessors(chain = true)
@Schema(description = "权限组申请查询")
public class AppScopeApplyQuery implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "所属应用")
    private Long appId;

    @Schema(description = "申请权限组")
    private Long groupId;

    @Schema(description = "审核状态[1-待审批 2-通过 99-退回]")
    private Integer auditStatus;

    @Schema(description = "创建时间-起")
    private LocalDateTime startCreatedAt;

    @Schema(description = "创建时间-止")
    private LocalDateTime endCreatedAt;
}
```

- [ ] **Step 5: 创建 Mapper（必须 @Repository）**

```java
package top.mddata.open.mapper.admin;

import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.open.entity.admin.AppScopeApply;

/**
 * 权限组申请 Mapper。
 *
 * @author henhen6
 */
@Repository
public interface AppScopeApplyMapper extends SuperMapper<AppScopeApply> {
}
```

- [ ] **Step 6: 编译验证**

Run: `mvn -q compile -pl mdp-apps/md-open/open-pojo,mdp-apps/md-open/open-dao -DskipTests`
Expected: 退出码 0

---

### Task 4: AppScopeApplyService.apply（TDD）

**Files:**
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/app/AppScopeApplyService.java`
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/app/impl/AppScopeApplyServiceImpl.java`
- Test: `mdp-apps/md-open/open-server/src/test/java/top/mddata/open/service/app/AppScopeApplyServiceImplTest.java`

**Interfaces:**
- Consumes: `AppScopeApplyDto`（Task 3）、`AuditStatusEnum.PENDING`、`AppService.count()`、`ScopeGroupService.getById()`、`AppGroupRelService`
- Produces:
  - `Integer apply(AppScopeApplyDto dto)` — 返回成功生成的申请单数量；校验失败抛 `BizException`（经 `ArgumentAssert`）
  - `Boolean review(AppScopeApplyReviewDto dto)`（Task 5 实现，接口本任务一并声明）

- [ ] **Step 1: 写失败测试**

先查现有测试基类（如 `mdp-apps/md-open/open-server/src/test/java/top/mddata/open/service/dashboard/DashboardOpenServiceTest.java`）确认 `@SpringBootTest` 模式后，创建 `AppScopeApplyServiceImplTest.java`：

```java
package top.mddata.open.service.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.common.enumeration.AuditStatusEnum;
import top.mddata.open.dto.admin.AppScopeApplyDto;
import top.mddata.open.entity.admin.AppScopeApply;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 权限组申请 Service 测试。
 */
@SpringBootTest
class AppScopeApplyServiceImplTest {

    @Resource
    private AppScopeApplyService appScopeApplyService;

    @Test
    void apply_应用不存在时报错() {
        AppScopeApplyDto dto = new AppScopeApplyDto()
                .setAppId(-1L)
                .setGroupIds(List.of(1L))
                .setApplyReason("测试申请");
        Exception e = assertThrows(Exception.class, () -> appScopeApplyService.apply(dto));
        assertTrue(e.getMessage().contains("应用"));
    }

    @Test
    void apply_权限组不存在时报错() {
        // 前置：准备存在但无权限组的场景（使用测试库中存在的 appId，或 mock）
        AppScopeApplyDto dto = new AppScopeApplyDto()
                .setAppId(1L)
                .setGroupIds(List.of(-1L))
                .setApplyReason("测试申请");
        Exception e = assertThrows(Exception.class, () -> appScopeApplyService.apply(dto));
        assertTrue(e.getMessage().contains("权限组"));
    }

    @Test
    void apply_成功后生成待审批申请单() {
        // 前置：测试库存在 appId=1 的应用、存在启用的权限组且该应用未拥有
        // 数据准备按测试库实际情况调整（可先查询一个可用 groupId）
        AppScopeApplyDto dto = new AppScopeApplyDto()
                .setAppId(1L)
                .setGroupIds(List.of(124315423202196024L))
                .setApplyReason("单元测试申请");
        Integer count = appScopeApplyService.apply(dto);
        assertEquals(1, count);

        AppScopeApply saved = appScopeApplyService.getOne(
                new com.mybatisflex.core.query.QueryWrapper()
                        .eq(AppScopeApply::getAppId, 1L)
                        .eq(AppScopeApply::getGroupId, 124315423202196024L)
                        .eq(AppScopeApply::getAuditStatus, AuditStatusEnum.PENDING.getCode()));
        assertEquals("单元测试申请", saved.getApplyReason());
    }

    @Test
    void apply_在审单存在时禁止重复申请() {
        // 依赖上一个测试留下的在审单
        AppScopeApplyDto dto = new AppScopeApplyDto()
                .setAppId(1L)
                .setGroupIds(List.of(124315423202196024L))
                .setApplyReason("重复申请");
        Exception e = assertThrows(Exception.class, () -> appScopeApplyService.apply(dto));
        assertTrue(e.getMessage().contains("待审批"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn test -pl mdp-apps/md-open/open-server -Dtest=AppScopeApplyServiceImplTest`
Expected: 编译失败（AppScopeApplyService 不存在）

- [ ] **Step 3: 创建 Service 接口**

```java
package top.mddata.open.service.app;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.dto.admin.AppScopeApplyDto;
import top.mddata.open.dto.admin.AppScopeApplyReviewDto;
import top.mddata.open.entity.admin.AppScopeApply;

/**
 * 权限组申请 服务层。
 *
 * @author henhen6
 */
public interface AppScopeApplyService extends SuperService<AppScopeApply> {

    /**
     * 批量申请权限组（一单一组，循环生成多张待审批申请单）。
     * 校验：应用归属当前用户、组存在且启用、未拥有、无在审单。
     *
     * @param dto 申请参数
     * @return 成功生成的申请单数量
     */
    Integer apply(AppScopeApplyDto dto);

    /**
     * 审核申请单。通过：写入 mdo_app_group_rel（幂等）；退回：审核意见必填。
     *
     * @param dto 审核参数
     * @return 是否成功
     */
    Boolean review(AppScopeApplyReviewDto dto);
}
```

- [ ] **Step 4: 创建 Service 实现**

```java
package top.mddata.open.service.app.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.enumeration.AuditStatusEnum;
import top.mddata.open.dto.admin.AppScopeApplyDto;
import top.mddata.open.dto.admin.AppScopeApplyReviewDto;
import top.mddata.open.entity.admin.App;
import top.mddata.open.entity.admin.AppGroupRel;
import top.mddata.open.entity.admin.AppScopeApply;
import top.mddata.open.entity.admin.ScopeGroup;
import top.mddata.open.mapper.admin.AppScopeApplyMapper;
import top.mddata.open.service.app.AppGroupRelService;
import top.mddata.open.service.app.AppScopeApplyService;
import top.mddata.open.service.app.AppService;
import top.mddata.open.service.oauth.ScopeGroupService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限组申请 服务层实现。
 *
 * @author henhen6
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppScopeApplyServiceImpl extends SuperServiceImpl<AppScopeApplyMapper, AppScopeApply>
        implements AppScopeApplyService {

    private final AppService appService;
    private final ScopeGroupService scopeGroupService;
    private final AppGroupRelService appGroupRelService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer apply(AppScopeApplyDto dto) {
        // 归属校验：只能为本人创建的应用申请
        long appCount = appService.count(QueryWrapper.create()
                .eq(App::getId, dto.getAppId())
                .eq(App::getCreatedBy, ContextUtil.getUserId()));
        ArgumentAssert.isTrue(appCount > 0, "应用不存在或无权限操作");

        List<AppScopeApply> applyList = new ArrayList<>();
        for (Long groupId : dto.getGroupIds()) {
            ScopeGroup group = scopeGroupService.getById(groupId);
            ArgumentAssert.notNull(group, "权限组[{}]不存在", groupId);
            ArgumentAssert.isTrue(Boolean.TRUE.equals(group.getState()), "权限组[{}]已停用", group.getName());

            long owned = appGroupRelService.count(QueryWrapper.create()
                    .eq(AppGroupRel::getAppId, dto.getAppId())
                    .eq(AppGroupRel::getGroupId, groupId));
            ArgumentAssert.isTrue(owned == 0, "已拥有权限组[{}]，请勿重复申请", group.getName());

            long pending = count(QueryWrapper.create()
                    .eq(AppScopeApply::getAppId, dto.getAppId())
                    .eq(AppScopeApply::getGroupId, groupId)
                    .eq(AppScopeApply::getAuditStatus, AuditStatusEnum.PENDING.getCode()));
            ArgumentAssert.isTrue(pending == 0, "权限组[{}]存在待审批的申请，请勿重复提交", group.getName());

            applyList.add(AppScopeApply.builder()
                    .appId(dto.getAppId())
                    .groupId(groupId)
                    .applyReason(dto.getApplyReason())
                    .auditStatus(AuditStatusEnum.PENDING.getCode())
                    .build());
        }
        saveBatch(applyList);
        return applyList.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean review(AppScopeApplyReviewDto dto) {
        AppScopeApply apply = getById(dto.getId());
        ArgumentAssert.notNull(apply, "申请单不存在");
        ArgumentAssert.isTrue(AuditStatusEnum.PENDING.eq(apply.getAuditStatus()), "申请单状态已变更，请刷新后重试");

        if (dto.getApprove()) {
            // 幂等：已存在授权关系则跳过写入，防止并发/重复审核产生重复数据
            long exists = appGroupRelService.count(QueryWrapper.create()
                    .eq(AppGroupRel::getAppId, apply.getAppId())
                    .eq(AppGroupRel::getGroupId, apply.getGroupId()));
            if (exists == 0) {
                appGroupRelService.save(AppGroupRel.builder()
                        .appId(apply.getAppId())
                        .groupId(apply.getGroupId())
                        .build());
            }
            updateAudit(apply.getId(), AuditStatusEnum.APPROVED, dto.getReviewComments());
        } else {
            ArgumentAssert.notBlank(dto.getReviewComments(), "退回时必须填写审核意见");
            updateAudit(apply.getId(), AuditStatusEnum.REJECTED, dto.getReviewComments());
        }
        return true;
    }

    private void updateAudit(Long id, AuditStatusEnum status, String reviewComments) {
        AppScopeApply update = new AppScopeApply();
        update.setId(id);
        update.setAuditStatus(status.getCode());
        update.setReviewComments(reviewComments);
        update.setAuditAt(LocalDateTime.now());
        updateById(update);
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `mvn test -pl mdp-apps/md-open/open-server -Dtest=AppScopeApplyServiceImplTest`
Expected: 4 个测试 PASS（测试数据 id 按测试库实际情况调整后）

- [ ] **Step 6: 检查 AppGroupRelService 是否存在**

Run: `rtk proxy grep -rn "interface AppGroupRelService" mdp-apps/md-open --include="*.java"`
Expected: 存在；若不存在则按标准模式补建（接口 + Impl + Mapper，参照 Task 3 模式，复用现有 `AppGroupRel` 实体）

---

### Task 5: review 审核测试补齐（TDD 续）

**Files:**
- Test: `mdp-apps/md-open/open-server/src/test/java/top/mddata/open/service/app/AppScopeApplyServiceImplTest.java`

**Interfaces:**
- Consumes: `AppScopeApplyService.review`（Task 4 已实现，本任务补测试）

- [ ] **Step 1: 追加 review 测试**

在测试类中追加：

```java
    @Test
    void review_退回时审核意见必填() {
        // 前置：存在待审批单（沿用 apply 测试数据）
        AppScopeApply pending = appScopeApplyService.getOne(
                new com.mybatisflex.core.query.QueryWrapper()
                        .eq(AppScopeApply::getAuditStatus, AuditStatusEnum.PENDING.getCode())
                        .last("limit 1"));
        var dto = new top.mddata.open.dto.admin.AppScopeApplyReviewDto()
                .setId(pending.getId())
                .setApprove(false)
                .setReviewComments(null);
        Exception e = assertThrows(Exception.class, () -> appScopeApplyService.review(dto));
        assertTrue(e.getMessage().contains("审核意见"));
    }

    @Test
    void review_通过后写入授权关系且幂等() {
        AppScopeApply pending = appScopeApplyService.getOne(
                new com.mybatisflex.core.query.QueryWrapper()
                        .eq(AppScopeApply::getAuditStatus, AuditStatusEnum.PENDING.getCode())
                        .last("limit 1"));
        var dto = new top.mddata.open.dto.admin.AppScopeApplyReviewDto()
                .setId(pending.getId())
                .setApprove(true);
        assertTrue(appScopeApplyService.review(dto));

        // 重复审核（状态已变更）应报错
        Exception e = assertThrows(Exception.class, () -> appScopeApplyService.review(dto));
        assertTrue(e.getMessage().contains("状态已变更"));
    }
```

- [ ] **Step 2: 运行全部测试**

Run: `mvn test -pl mdp-apps/md-open/open-server -Dtest=AppScopeApplyServiceImplTest`
Expected: 全部 PASS

---

### Task 6: ScopeGroupService 扩展（可申请/已拥有/组详情，TDD）

**Files:**
- Modify: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/oauth/ScopeGroupService.java`
- Modify: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/oauth/impl/ScopeGroupServiceImpl.java`
- Create: `mdp-apps/md-open/open-pojo/src/main/java/top/mddata/open/vo/admin/ScopeGroupDetailVo.java`
- Test: `mdp-apps/md-open/open-server/src/test/java/top/mddata/open/service/oauth/ScopeGroupServiceImplTest.java`

**Interfaces:**
- Consumes: `AppScopeApply`（Task 3）、`AppGroupRel`、`GroupApiRel`、`GroupScopeRel`、`Api`、`OauthScope` 实体
- Produces:
  - `List<ScopeGroupVo> listAppliable(Long appId)` — 启用且未拥有且无在审单
  - `List<ScopeGroupVo> listOwned(Long appId)` — 已生效权限组
  - `ScopeGroupDetailVo getGroupDetail(Long groupId)` — 组 + `apis: List<ApiVo>` + `scopes: List<OauthScopeVo>`

- [ ] **Step 1: 创建 ScopeGroupDetailVo**

```java
package top.mddata.open.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 权限组详情（含组内开放接口与 oauth2 权限）。
 *
 * @author henhen6
 */
@Data
@Accessors(chain = true)
@Schema(description = "权限组详情")
public class ScopeGroupDetailVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "简介")
    private String intro;

    @Schema(description = "排序")
    private Long weight;

    @Schema(description = "组内开放接口")
    private List<ApiVo> apis;

    @Schema(description = "组内oauth2权限")
    private List<OauthScopeVo> scopes;
}
```

- [ ] **Step 2: 写失败测试**

创建 `ScopeGroupServiceImplTest.java`：

```java
package top.mddata.open.service.oauth;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.open.vo.admin.ScopeGroupDetailVo;
import top.mddata.open.vo.admin.ScopeGroupVo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 权限组 Service 扩展方法测试。
 */
@SpringBootTest
class ScopeGroupServiceImplTest {

    @Resource
    private ScopeGroupService scopeGroupService;

    @Test
    void listAppliable_排除已拥有和在审的组() {
        // 测试库存在 appId=1 拥有组 695047019844641792（见 mdo_app_group_rel 种子数据）
        List<ScopeGroupVo> list = scopeGroupService.listAppliable(1L);
        assertNotNull(list);
        assertTrue(list.stream().noneMatch(g -> g.getId().equals(695047019844641792L)));
        assertTrue(list.stream().allMatch(g -> Boolean.TRUE.equals(g.getState())));
    }

    @Test
    void listOwned_返回已生效的组() {
        List<ScopeGroupVo> list = scopeGroupService.listOwned(1L);
        assertNotNull(list);
        assertTrue(list.stream().anyMatch(g -> g.getId().equals(695047019844641792L)));
    }

    @Test
    void getGroupDetail_返回组内接口和权限() {
        ScopeGroupDetailVo detail = scopeGroupService.getGroupDetail(124315423202196024L);
        assertNotNull(detail);
        assertNotNull(detail.getName());
        assertNotNull(detail.getApis());
        assertNotNull(detail.getScopes());
    }
}
```

- [ ] **Step 3: 运行测试确认失败**

Run: `mvn test -pl mdp-apps/md-open/open-server -Dtest=ScopeGroupServiceImplTest`
Expected: 编译失败（方法不存在）

- [ ] **Step 4: ScopeGroupService 接口追加**

```java
    /**
     * 查询应用可申请的权限组（启用 且 未拥有 且 无待审批申请单）
     *
     * @param appId 应用ID
     * @return 可申请权限组
     */
    List<ScopeGroupVo> listAppliable(Long appId);

    /**
     * 查询应用已拥有的权限组
     *
     * @param appId 应用ID
     * @return 已拥有权限组
     */
    List<ScopeGroupVo> listOwned(Long appId);

    /**
     * 查询权限组详情（含组内开放接口与 oauth2 权限）
     *
     * @param groupId 权限组ID
     * @return 组详情
     */
    ScopeGroupDetailVo getGroupDetail(Long groupId);
```

- [ ] **Step 5: ScopeGroupServiceImpl 实现追加**

```java
    @Override
    public List<ScopeGroupVo> listAppliable(Long appId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select(ScopeGroup.class)
                .from(ScopeGroup.class)
                .where(ScopeGroup::getState).eq(true)
                // 排除已拥有
                .and("not exists (select 1 from mdo_app_group_rel rel where rel.group_id = mdo_scope_group.id and rel.app_id = ?)", appId)
                // 排除在审申请单
                .and("not exists (select 1 from mdo_app_scope_apply apply where apply.group_id = mdo_scope_group.id and apply.app_id = ? and apply.audit_status = ?)",
                        appId, AuditStatusEnum.PENDING.getCode())
                .orderBy(ScopeGroup::getWeight, true);
        return listAs(wrapper, ScopeGroupVo.class);
    }

    @Override
    public List<ScopeGroupVo> listOwned(Long appId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select(ScopeGroup.class)
                .from(ScopeGroup.class)
                .innerJoin(AppGroupRel.class).on(AppGroupRel::getGroupId, ScopeGroup::getId)
                .where(AppGroupRel::getAppId).eq(appId)
                .orderBy(ScopeGroup::getWeight, true);
        return listAs(wrapper, ScopeGroupVo.class);
    }

    @Override
    public ScopeGroupDetailVo getGroupDetail(Long groupId) {
        ScopeGroup group = getById(groupId);
        ArgumentAssert.notNull(group, "权限组不存在");
        ScopeGroupDetailVo detail = BeanUtil.toBean(group, ScopeGroupDetailVo.class);

        // 组内开放接口（mdo_group_api_rel -> mdo_api）
        QueryWrapper apiWrapper = QueryWrapper.create()
                .select(Api.class)
                .from(Api.class)
                .innerJoin(GroupApiRel.class).on(GroupApiRel::getApiId, Api::getId)
                .where(GroupApiRel::getGroupId).eq(groupId);
        detail.setApis(listAs(apiWrapper, ApiVo.class));

        // 组内 oauth2 权限（mdo_group_scope_rel -> mdo_oauth_scope）
        QueryWrapper scopeWrapper = QueryWrapper.create()
                .select(OauthScope.class)
                .from(OauthScope.class)
                .innerJoin(GroupScopeRel.class).on(GroupScopeRel::getScopeId, OauthScope::getId)
                .where(GroupScopeRel::getGroupId).eq(groupId);
        detail.setScopes(listAs(scopeWrapper, OauthScopeVo.class));
        return detail;
    }
```

注意：实现类的 import 需补充 `cn.hutool.core.bean.BeanUtil`、`top.mddata.base.utils.ArgumentAssert`、`top.mddata.common.enumeration.AuditStatusEnum`、各实体与 Vo。
若 `listAs` 不支持 join 后转 Vo（实体类与查询主体不一致），改用 `scopeGroupService.getMapper().selectListByQueryAs(wrapper, ScopeGroupVo.class)` 或在 `ScopeGroupMapper` 增加自定义方法。

- [ ] **Step 6: 运行测试确认通过**

Run: `mvn test -pl mdp-apps/md-open/open-server -Dtest=ScopeGroupServiceImplTest`
Expected: 3 个测试 PASS

---

### Task 7: 接口层（3 个 Controller + 1 个扩展）

**Files:**
- Create: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/client/ClientScopeGroupController.java`
- Create: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/client/ClientAppScopeApplyController.java`
- Create: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/app/AppScopeApplyController.java`
- Modify: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/oauth/ScopeGroupController.java`（+ getGroupDetail）

**Interfaces:**
- Consumes: `AppScopeApplyService.apply/review`、`ScopeGroupService.listAppliable/listOwned/getGroupDetail`（Task 4、6）

- [ ] **Step 1: ClientScopeGroupController（开发者端-权限组）**

```java
package top.mddata.open.controller.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.open.service.oauth.ScopeGroupService;
import top.mddata.open.vo.admin.ScopeGroupDetailVo;
import top.mddata.open.vo.admin.ScopeGroupVo;

import java.util.List;

/**
 * 权限组 控制层（开发者端）。
 *
 * @author henhen6
 */
@RestController
@Validated
@Tag(name = "权限组-开发者端")
@RequestMapping("/client/scopeGroup")
@RequiredArgsConstructor
public class ClientScopeGroupController {

    private final ScopeGroupService scopeGroupService;

    /**
     * 查询应用可申请的权限组
     *
     * @param appId 应用ID
     * @return 可申请权限组
     */
    @GetMapping("/listAppliable")
    @Operation(summary = "查询可申请权限组", description = "启用且未拥有且无在审申请")
    public R<List<ScopeGroupVo>> listAppliable(@RequestParam Long appId) {
        return R.success(scopeGroupService.listAppliable(appId));
    }

    /**
     * 查询应用已拥有的权限组
     *
     * @param appId 应用ID
     * @return 已拥有权限组
     */
    @GetMapping("/listOwned")
    @Operation(summary = "查询已拥有权限组", description = "查询应用已拥有的权限组")
    public R<List<ScopeGroupVo>> listOwned(@RequestParam Long appId) {
        return R.success(scopeGroupService.listOwned(appId));
    }

    /**
     * 查询权限组详情（组内开放接口与 oauth2 权限）
     *
     * @param id 权限组ID
     * @return 组详情
     */
    @GetMapping("/getGroupDetail")
    @Operation(summary = "查询权限组详情", description = "含组内开放接口与oauth2权限")
    public R<ScopeGroupDetailVo> getGroupDetail(@RequestParam Long id) {
        return R.success(scopeGroupService.getGroupDetail(id));
    }
}
```

- [ ] **Step 2: ClientAppScopeApplyController（开发者端-申请）**

```java
package top.mddata.open.controller.client;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.base.util.ContextUtil;
import top.mddata.open.dto.admin.AppScopeApplyDto;
import top.mddata.open.entity.admin.AppScopeApply;
import top.mddata.open.query.admin.AppScopeApplyQuery;
import top.mddata.open.service.app.AppScopeApplyService;
import top.mddata.open.vo.admin.AppScopeApplyVo;

/**
 * 权限组申请 控制层（开发者端）。
 *
 * @author henhen6
 */
@RestController
@Validated
@Tag(name = "权限组申请-开发者端")
@RequestMapping("/client/appScopeApply")
@RequiredArgsConstructor
public class ClientAppScopeApplyController {

    private final AppScopeApplyService appScopeApplyService;

    /**
     * 批量申请权限组
     *
     * @param dto 申请参数
     * @return 生成的申请单数量
     */
    @PostMapping("/apply")
    @Operation(summary = "申请权限组", description = "一单一组，批量提交")
    @RequestLog(value = "申请权限组", logType = RequestLog.LogType.ADD)
    public R<Integer> apply(@Validated @RequestBody AppScopeApplyDto dto) {
        return R.success(appScopeApplyService.apply(dto));
    }

    /**
     * 分页查询我的申请记录（仅本人创建的应用）
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询申请记录", description = "仅本人名下应用")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<AppScopeApplyVo>> page(@RequestBody @Validated PageParams<AppScopeApplyQuery> params) {
        Page<AppScopeApplyVo> page = Page.of(params.getCurrent(), params.getSize());
        AppScopeApply entity = BeanUtil.toBean(params.getModel(), AppScopeApply.class);
        // 归属过滤：只能看本人创建的申请单
        entity.setCreatedBy(ContextUtil.getUserId());
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        appScopeApplyService.pageAs(page, wrapper, AppScopeApplyVo.class);
        return R.success(page);
    }
}
```

注意：申请单由本人提交，`createdBy` 即申请人，用 createdBy 过滤即可（与 ClientAppApplyController 模式一致）。
申请记录的 `groupName` 回显：pageAs 后在 Controller 中按 groupId 批量查 ScopeGroup 名称填充（用 `@Echo` 或手工 map，参照现有回显模式，简单起见手工组装）。

- [ ] **Step 3: AppScopeApplyController（console 审核端）**

```java
package top.mddata.open.controller.app;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.open.dto.admin.AppScopeApplyReviewDto;
import top.mddata.open.entity.admin.AppScopeApply;
import top.mddata.open.query.admin.AppScopeApplyQuery;
import top.mddata.open.service.app.AppScopeApplyService;
import top.mddata.open.vo.admin.AppScopeApplyVo;

/**
 * 权限组申请审核 控制层（控制台）。
 *
 * @author henhen6
 */
@RestController
@Validated
@Tag(name = "权限组申请审核")
@RequestMapping("/admin/appScopeApply")
@RequiredArgsConstructor
public class AppScopeApplyController extends SuperController<AppScopeApplyService, AppScopeApply> {

    /**
     * 分页查询权限组申请
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询权限组申请")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<AppScopeApplyVo>> page(@RequestBody @Validated PageParams<AppScopeApplyQuery> params) {
        Page<AppScopeApplyVo> page = Page.of(params.getCurrent(), params.getSize());
        AppScopeApply entity = BeanUtil.toBean(params.getModel(), AppScopeApply.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, AppScopeApplyVo.class);
        // appName/groupName 回显：批量按 appId/groupId 查名称填充
        return R.success(page);
    }

    /**
     * 根据主键查询申请单
     *
     * @param id 主键
     * @return 申请单
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键查询申请单")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<AppScopeApplyVo> getById(@RequestParam Long id) {
        AppScopeApply entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, AppScopeApplyVo.class));
    }

    /**
     * 审核
     *
     * @param dto 审核参数
     * @return 是否成功
     */
    @PostMapping("/review")
    @Operation(summary = "审核", description = "审核权限组申请")
    @RequestLog(value = "审核权限组申请", logType = RequestLog.LogType.UPDATE, request = false)
    public R<Boolean> review(@Validated @RequestBody AppScopeApplyReviewDto dto) {
        return R.success(superService.review(dto));
    }
}
```

- [ ] **Step 4: ScopeGroupController 追加 getGroupDetail（console 审核端详情）**

在现有 `ScopeGroupController`（`/admin/scopeGroup`）追加：

```java
    /**
     * 查询权限组详情（组内开放接口与 oauth2 权限）
     *
     * @param id 权限组ID
     * @return 组详情
     */
    @GetMapping("/getGroupDetail")
    @Operation(summary = "查询权限组详情", description = "含组内开放接口与oauth2权限")
    @RequestLog(value = "'查询权限组详情:' + #id", logType = RequestLog.LogType.QUERY)
    public R<ScopeGroupDetailVo> getGroupDetail(@RequestParam Long id) {
        return R.success(superService.getGroupDetail(id));
    }
```

- [ ] **Step 5: 编译 + checkstyle 验证**

Run: `mvn -q compile -pl mdp-apps/md-open/open-web -DskipTests`
Expected: 退出码 0（checkstyle 无 UnusedImports 等错误）

---

### Task 8: 菜单与接口权限 SQL

**Files:**
- Modify: `docs/sql/2026-10-20-scope-group-apply.sql`（追加）
- Modify: `docs/mdp.sql`（mdc_resource_menu / mdc_resource_api INSERT 区追加）

- [ ] **Step 1: 追加菜单 INSERT**

参照 `mdp.sql:1197` 的 `open:admin:appApply` 菜单（parent_id=`693083240537805901`，path 前缀 `/manage/`），生成（雪花 id 执行时替换为实际值，或用固定新 id）：

```sql
-- 3. 菜单：权限申请审核（挂在应用审核同级，parent_id 参照 open:admin:appApply）
INSERT INTO `mdc_resource_menu` (`id`, `app_id`, `code`, `name`, `menu_type`, `remarks`, `path`, `component`, `redirect`, `state`, `data_scope_state`, `tree_path`, `parent_id`, `weight`, `meta`, `created_by`, `created_at`)
VALUES (<新雪花id>, 2, 'open:admin:appScopeApply', '权限申请审核', '20', '', '/manage/appScopeApply', '/open/admin/appScopeApply/index', '', b'1', b'0', '/693083240537805824/693083240537805901/<新雪花id>/', 693083240537805901, 30, '{"icon":"ant-design:safety-certificate","keepAlive":true,"title":"权限申请审核"}', 680083598598475778, NOW(3));

-- 4. 接口权限（resource_id 关联菜单id，审核操作建议单独建按钮权限，参照 appApply 的 review 按钮）
INSERT INTO `mdc_resource_api` (`id`, `resource_id`, `uri`, `request_method`, `name`, `controller`, `application_name`, `is_input`, `created_by`, `created_at`) VALUES
(<新id>, <菜单id>, '/admin/appScopeApply/page', 'POST', '分页查询', 'AppScopeApplyController', 'boot-server', b'0', 680083598598475778, NOW(3)),
(<新id>, <菜单id>, '/admin/appScopeApply/getById', 'GET', '单体查询', 'AppScopeApplyController', 'boot-server', b'0', 680083598598475778, NOW(3)),
(<新id>, <按钮id>, '/admin/appScopeApply/review', 'POST', '审核', 'AppScopeApplyController', 'boot-server', b'0', 680083598598475778, NOW(3)),
(<新id>, <菜单id>, '/admin/scopeGroup/getGroupDetail', 'GET', '权限组详情', 'ScopeGroupController', 'boot-server', b'0', 680083598598475778, NOW(3)),
(<新id>, <菜单id>, '/client/scopeGroup/listAppliable', 'GET', '可申请权限组', 'ClientScopeGroupController', 'boot-server', b'1', 680083598598475778, NOW(3)),
(<新id>, <菜单id>, '/client/scopeGroup/listOwned', 'GET', '已拥有权限组', 'ClientScopeGroupController', 'boot-server', b'1', 680083598598475778, NOW(3)),
(<新id>, <菜单id>, '/client/scopeGroup/getGroupDetail', 'GET', '权限组详情', 'ClientScopeGroupController', 'boot-server', b'1', 680083598598475778, NOW(3)),
(<新id>, <菜单id>, '/client/appScopeApply/apply', 'POST', '申请权限组', 'ClientAppScopeApplyController', 'boot-server', b'1', 680083598598475778, NOW(3)),
(<新id>, <菜单id>, '/client/appScopeApply/page', 'POST', '申请记录', 'ClientAppScopeApplyController', 'boot-server', b'1', 680083598598475778, NOW(3));
```

注意：`/client/*` 接口 `is_input=b'1'`（参照现有 client 接口惯例，若现有数据不同则以库中为准）；`review` 的按钮权限需先 INSERT 一条 `mdc_resource_menu`（menu_type='30' 按钮，code='open:admin:appScopeApply:review'），参照 appApply 的按钮数据。
雪花 id 由执行者用任意 bigint 唯一值替换（如 7 开头 18 位）。

- [ ] **Step 2: 同步 mdp.sql**

将上述 INSERT 追加到 `docs/mdp.sql` 对应表的 INSERT 区域。

---

### Task 9: web-console 审核页（api + 页面 + 弹窗）

**Files:**
- Create: `mdp-vben/apps/web-console/src/api/open/admin/appScopeApply/api.ts`
- Create: `mdp-vben/apps/web-console/src/api/open/admin/appScopeApply/model.d.ts`
- Create: `mdp-vben/apps/web-console/src/constants/perm/open/admin/appScopeApply.ts`
- Create: `mdp-vben/apps/web-console/src/views/open/admin/appScopeApply/index.vue`
- Create: `mdp-vben/apps/web-console/src/views/open/admin/appScopeApply/data/index.tsx`
- Create: `mdp-vben/apps/web-console/src/views/open/admin/appScopeApply/data/review.tsx`
- Create: `mdp-vben/apps/web-console/src/views/open/admin/appScopeApply/modules/wrapper.vue`
- Create: `mdp-vben/apps/web-console/src/views/open/admin/appScopeApply/modules/review.vue`
- Create: `mdp-vben/apps/web-console/src/views/open/admin/appScopeApply/modules/ScopeGroupDetailModal.vue`

**Interfaces:**
- Consumes: 后端 `/admin/appScopeApply/page|review|getById`、`/admin/scopeGroup/getGroupDetail`（Task 7）

- [ ] **Step 1: api.ts + model.d.ts**

`api.ts`（模式照抄 appApply/api.ts）：

```typescript
import type { AppScopeApplyType } from './model';

import type { PageParams, PageResult } from '#/api';

import { RequestEnum, ServicePrefixEnum } from '@vben/constants';

import { requestClient } from '#/api/request';

const SERVICE_PREFIX = ServicePrefixEnum.OPEN;
const MODULAR = `${SERVICE_PREFIX}/admin/appScopeApply`;

export namespace AppScopeApplyApi {
  /** 分页查询 */
  export const page = (
    params: PageParams<Partial<AppScopeApplyType.AppScopeApplyQuery>>,
  ) =>
    requestClient.post<PageResult<AppScopeApplyType.AppScopeApplyVo>>(
      `${MODULAR}/page`,
      params,
    );

  /** 审核 */
  export const review = (
    params: Partial<AppScopeApplyType.AppScopeApplyReviewDto>,
  ) => requestClient.post<boolean>(`${MODULAR}/review`, params);
}

export namespace ScopeGroupApi {
  /** 权限组详情（组内接口与oauth2权限） */
  export const getGroupDetail = (id: string) =>
    requestClient.get<AppScopeApplyType.ScopeGroupDetailVo>(
      `${SERVICE_PREFIX}/admin/scopeGroup/getGroupDetail`,
      { params: { id } },
    );
}
```

`model.d.ts`：

```typescript
export namespace AppScopeApplyType {
  export interface AppScopeApplyVo {
    id: string;
    appId: string;
    groupId: string;
    applyReason: string;
    auditStatus: number;
    auditAt: string;
    reviewComments: string;
    createdAt: string;
    createdBy: string;
    appName: string;
    groupName: string;
  }

  export interface AppScopeApplyQuery {
    appId?: string;
    groupId?: string;
    auditStatus?: number;
  }

  export interface AppScopeApplyReviewDto {
    id: string;
    approve: boolean;
    reviewComments?: string;
  }

  export interface ScopeGroupDetailVo {
    id: string;
    name: string;
    intro: string;
    weight: number;
    apis: {
      id: string;
      apiName: string;
      description: string;
      apiVersion: string;
    }[];
    scopes: {
      id: string;
      code: string;
      name: string;
      icon: string;
      intro: string;
    }[];
  }
}
```

- [ ] **Step 2: 权限点**

`constants/perm/open/admin/appScopeApply.ts`：

```typescript
export const appScopeApply = {
  review: 'open:admin:appScopeApply:review',
  view: 'open:admin:appScopeApply:view',
};
```

- [ ] **Step 3: 审核页 data/index.tsx**

照抄 `appApply/data/index.tsx` 模式，调整点：
- 搜索表单：appId（Input）、auditStatus（ApiDict，DictUniqKey.common.auditStatus）、createdAt（RangePicker）
- 列：appName、groupName、applyReason、auditStatus（CellDict tag）、createdAt、auditAt、reviewComments
- 操作列：review（show: `values.auditStatus === AuditStatusEnum.PENDING`，auth: permCode.review）、detail（查看组详情，调 `ScopeGroupDetailModal`）
- `handleReview` 打开 wrapper 弹窗；`handleDetail` 打开组详情弹窗

- [ ] **Step 4: 审核弹窗（wrapper.vue + review.vue + data/review.tsx）**

照抄 `appApply/modules/wrapper.vue` + `review.vue` 模式（useVbenModal）。
`data/review.tsx` 的表单 schema：

```typescript
[
  {
    component: 'RadioGroup',
    fieldName: 'approve',
    label: '审核结果',
    defaultValue: true,
    componentProps: {
      options: [
        { label: '通过', value: true },
        { label: '退回', value: false },
      ],
    },
  },
  {
    component: 'Textarea',
    fieldName: 'reviewComments',
    label: '审核意见',
    // 退回时必填：提交前在 handleSubmit 中校验 approve === false 时非空
    componentProps: { maxlength: 255, showCount: true },
  },
]
```

`handleSubmit` 中：退回且 reviewComments 为空 → `createMessage.warning('退回时必须填写审核意见')` 中断；否则调 `AppScopeApplyApi.review` → 成功后 `modalApi.close()` + emit('success')。

- [ ] **Step 5: 组详情弹窗 ScopeGroupDetailModal.vue**

useVbenModal 封装，`onOpened` 时 `ScopeGroupApi.getGroupDetail(modalApi.getData().groupId)`：

```vue
<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Card, CardMeta } from 'antdv-next';

import { ScopeGroupApi } from '#/api/open/admin/appScopeApply/api';

const detail = ref<any>({ apis: [], scopes: [] });
const loading = ref(false);

const [Modal, modalApi] = useVbenModal({
  async onOpened() {
    loading.value = true;
    try {
      detail.value = await ScopeGroupApi.getGroupDetail(
        modalApi.getData().groupId,
      );
    } finally {
      loading.value = false;
    }
  },
  showConfirmButton: false,
  cancelText: '关闭',
});

defineExpose({ modalApi });
</script>
<template>
  <Modal :title="`权限组详情 - ${detail.name ?? ''}`" class="w-[70%]">
    <a-spin :spinning="loading">
      <p class="mb-2 text-gray-500">{{ detail.intro }}</p>
      <h4 class="mb-2 font-bold">开放接口</h4>
      <vxe-table :data="detail.apis" border show-overflow>
        <vxe-column field="apiName" title="接口名" />
        <vxe-column field="description" title="接口描述" />
        <vxe-column field="apiVersion" title="版本" width="80" />
      </vxe-table>
      <h4 class="my-2 font-bold">Oauth2权限</h4>
      <div class="flex flex-wrap gap-2">
        <Card v-for="scope in detail.scopes" :key="scope.id" class="w-[240px]">
          <CardMeta :title="scope.name" :description="scope.intro">
            <template #avatar>
              <img v-if="scope.icon" :src="scope.icon" class="h-8 w-8" alt="" />
            </template>
          </CardMeta>
          <p class="mt-1 text-xs text-gray-400">{{ scope.code }}</p>
        </Card>
      </div>
    </a-spin>
  </Modal>
</template>
```

注：vxe-table 原生标签需确认项目注册方式（参照项目中已有 vxe-table 原生用法的页面；若统一走 useVbenVxeGrid，则改为 Grid 配置方式）。

- [ ] **Step 6: index.vue**

照抄 `appApply/index.vue`，模板中加 `<Wrapper ref="formWrapperRef" @success="handleRefresh" />` 和 `<ScopeGroupDetailModal ref="detailModalRef" />`。

- [ ] **Step 7: 前端构建验证**

Run: `cd mdp-vben && pnpm --filter web-console build`（或项目惯用的 lint/typecheck 命令）
Expected: 构建成功无类型错误

---

### Task 10: web-open 开发者端「权限」tab

**Files:**
- Create: `mdp-vben/apps/web-open/src/api/open/client/scopeGroup/api.ts` + `model.d.ts`
- Create: `mdp-vben/apps/web-open/src/api/open/client/appScopeApply/api.ts` + `model.d.ts`
- Create: `mdp-vben/apps/web-open/src/views/open/client/app/modules/scope.vue`
- Create: `mdp-vben/apps/web-open/src/views/open/client/app/modules/ScopeGroupCard.vue`
- Create: `mdp-vben/apps/web-open/src/views/open/client/app/modules/ScopeGroupDetailModal.vue`
- Modify: `mdp-vben/apps/web-open/src/views/open/client/app/modules/view.vue:28-49`（settingList 注册）

**Interfaces:**
- Consumes: `/client/scopeGroup/listAppliable|listOwned|getGroupDetail`、`/client/appScopeApply/apply|page`（Task 7）

- [ ] **Step 1: api×2 + model**

`scopeGroup/api.ts`：

```typescript
import type { ScopeGroupType } from './model';

import { ServicePrefixEnum } from '@vben/constants';

import { requestClient } from '#/api/request';

const MODULAR = `${ServicePrefixEnum.OPEN}/client/scopeGroup`;

export namespace ScopeGroupApi {
  /** 可申请权限组 */
  export const listAppliable = (appId: string) =>
    requestClient.get<ScopeGroupType.ScopeGroupVo[]>(`${MODULAR}/listAppliable`, {
      params: { appId },
    });

  /** 已拥有权限组 */
  export const listOwned = (appId: string) =>
    requestClient.get<ScopeGroupType.ScopeGroupVo[]>(`${MODULAR}/listOwned`, {
      params: { appId },
    });

  /** 权限组详情 */
  export const getGroupDetail = (id: string) =>
    requestClient.get<ScopeGroupType.ScopeGroupDetailVo>(
      `${MODULAR}/getGroupDetail`,
      { params: { id } },
    );
}
```

`scopeGroup/model.d.ts`：

```typescript
export namespace ScopeGroupType {
  export interface ScopeGroupVo {
    id: string;
    name: string;
    intro: string;
    weight: number;
    state: boolean;
  }

  export interface ScopeGroupDetailVo extends ScopeGroupVo {
    apis: { id: string; apiName: string; description: string; apiVersion: string }[];
    scopes: { id: string; code: string; name: string; icon: string; intro: string }[];
  }
}
```

`appScopeApply/api.ts`：

```typescript
import type { AppScopeApplyType } from './model';

import type { PageParams, PageResult } from '#/api';

import { ServicePrefixEnum } from '@vben/constants';

import { requestClient } from '#/api/request';

const MODULAR = `${ServicePrefixEnum.OPEN}/client/appScopeApply`;

export namespace AppScopeApplyApi {
  /** 批量申请权限组 */
  export const apply = (params: AppScopeApplyType.AppScopeApplyDto) =>
    requestClient.post<number>(`${MODULAR}/apply`, params);

  /** 我的申请记录 */
  export const page = (
    params: PageParams<Partial<AppScopeApplyType.AppScopeApplyQuery>>,
  ) =>
    requestClient.post<PageResult<AppScopeApplyType.AppScopeApplyVo>>(
      `${MODULAR}/page`,
      params,
    );
}
```

`appScopeApply/model.d.ts`：

```typescript
export namespace AppScopeApplyType {
  export interface AppScopeApplyDto {
    appId: string;
    groupIds: string[];
    applyReason: string;
  }

  export interface AppScopeApplyQuery {
    groupId?: string;
    auditStatus?: number;
  }

  export interface AppScopeApplyVo {
    id: string;
    appId: string;
    groupId: string;
    applyReason: string;
    auditStatus: number;
    auditAt: string;
    reviewComments: string;
    createdAt: string;
    groupName: string;
  }
}
```

- [ ] **Step 2: ScopeGroupCard.vue（卡片组件）**

```vue
<script lang="ts" setup>
import type { ScopeGroupType } from '#/api/open/client/scopeGroup/model';

defineProps<{
  group: ScopeGroupType.ScopeGroupVo;
  selectable?: boolean;
  checked?: boolean;
}>();

const emit = defineEmits<{
  (e: 'detail', id: string): void;
  (e: 'toggle', id: string): void;
}>();
</script>
<template>
  <Card
    class="w-[260px] cursor-pointer"
    :class="{ 'border-primary': checked }"
    @click="selectable && emit('toggle', group.id)"
  >
    <div class="flex items-start justify-between">
      <Checkbox v-if="selectable" :checked="checked" @click.stop />
      <span v-else class="text-success">已生效</span>
      <Button size="small" type="link" @click.stop="emit('detail', group.id)">
        详情
      </Button>
    </div>
    <h4 class="mt-1 font-bold">{{ group.name }}</h4>
    <p class="mt-1 line-clamp-2 text-xs text-gray-500">{{ group.intro }}</p>
  </Card>
</template>
<script lang="ts" setup>
import { Button, Card, Checkbox } from 'antdv-next';
</script>
```

注：script 合并为单个 setup 块（此处为展示拆分），import 置顶。

- [ ] **Step 3: ScopeGroupDetailModal.vue**

与 Task 9 Step 5 结构相同（web-open 端独立实现，api 用 `#/api/open/client/scopeGroup/api` 的 `ScopeGroupApi.getGroupDetail`），同样 useVbenModal + vxe-table（接口）+ Card（oauth2权限）。

- [ ] **Step 4: scope.vue（三区容器）**

核心逻辑：

```vue
<script lang="ts" setup>
import type { ScopeGroupType } from '#/api/open/client/scopeGroup/model';

import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { useVbenVxeGrid } from '@vben/components/adapter';
import { useMessage } from '@vben/components/hooks';

import { Button, Textarea } from 'antdv-next';

import { AppScopeApplyApi } from '#/api/open/client/appScopeApply/api';
import { ScopeGroupApi } from '#/api/open/client/scopeGroup/api';

import ScopeGroupCard from './ScopeGroupCard.vue';
import ScopeGroupDetailModal from './ScopeGroupDetailModal.vue';

const router = useRouter();
const appId = router.currentRoute.value.params.id as string;

const appliableList = ref<ScopeGroupType.ScopeGroupVo[]>([]);
const ownedList = ref<ScopeGroupType.ScopeGroupVo[]>([]);
const checkedIds = ref<Set<string>>(new Set());
const applyReason = ref('');
const submitting = ref(false);
const detailModalRef = ref();
const { createMessage } = useMessage();

async function loadData() {
  [appliableList.value, ownedList.value] = await Promise.all([
    ScopeGroupApi.listAppliable(appId),
    ScopeGroupApi.listOwned(appId),
  ]);
}

function toggle(id: string) {
  checkedIds.value.has(id)
    ? checkedIds.value.delete(id)
    : checkedIds.value.add(id);
}

function openDetail(id: string) {
  detailModalRef.value?.modalApi.setData({ groupId: id }).open();
}

async function handleSubmit() {
  if (checkedIds.value.size === 0) {
    createMessage.warning('请至少勾选一个权限组');
    return;
  }
  if (!applyReason.value.trim()) {
    createMessage.warning('请填写申请理由');
    return;
  }
  submitting.value = true;
  try {
    const count = await AppScopeApplyApi.apply({
      appId,
      groupIds: [...checkedIds.value],
      applyReason: applyReason.value,
    });
    createMessage.success(`已提交 ${count} 条申请，请等待审核`);
    checkedIds.value.clear();
    applyReason.value = '';
    await loadData();
    gridApi.query();
  } finally {
    submitting.value = false;
  }
}

// 申请记录 Grid：列 groupName、auditStatus(CellDict)、reviewComments、createdAt、auditAt
const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: [
      { field: 'groupName', title: '权限组' },
      {
        field: 'auditStatus',
        title: '审核状态',
        cellRender: { name: 'CellDict', attrs: { type: 'common.auditStatus', mode: 'tag' } },
      },
      { field: 'reviewComments', title: '审核意见' },
      { field: 'createdAt', title: '申请时间', width: 160 },
      { field: 'auditAt', title: '审核时间', width: 160 },
    ],
    proxyConfig: {
      ajax: {
        query: async ({ page }) =>
          await AppScopeApplyApi.page({
            model: {},
            size: page.pageSize,
            current: page.currentPage,
          }),
      },
    },
  },
});

onMounted(loadData);
</script>

<template>
  <div>
    <h4 class="mb-2 font-bold">可申请权限组</h4>
    <div class="flex flex-wrap gap-2">
      <ScopeGroupCard
        v-for="g in appliableList"
        :key="g.id"
        :group="g"
        selectable
        :checked="checkedIds.has(g.id)"
        @toggle="toggle"
        @detail="openDetail"
      />
      <a-empty v-if="appliableList.length === 0" description="暂无可申请的权限组" />
    </div>
    <div class="mt-3 flex items-start gap-2">
      <Textarea
        v-model:value="applyReason"
        :maxlength="512"
        show-count
        placeholder="请填写申请理由（必填）"
        class="w-[400px]"
      />
      <Button type="primary" :loading="submitting" @click="handleSubmit">
        提交申请
      </Button>
    </div>

    <h4 class="mb-2 mt-6 font-bold">已有权限</h4>
    <div class="flex flex-wrap gap-2">
      <ScopeGroupCard v-for="g in ownedList" :key="g.id" :group="g" @detail="openDetail" />
      <a-empty v-if="ownedList.length === 0" description="暂无权限" />
    </div>

    <h4 class="mb-2 mt-6 font-bold">申请记录</h4>
    <Grid />

    <ScopeGroupDetailModal ref="detailModalRef" />
  </div>
</template>
```

注：`common.auditStatus` 字典 key 以 web-open 项目 `DictUniqKey` 实际定义为准（参照 web-console 的 `DictUniqKey.common.auditStatus`）。

- [ ] **Step 5: view.vue 注册 tab**

`settingList` 追加：

```typescript
  {
    key: '5',
    name: '权限',
    component: Scope,
  },
```

并 `import Scope from './scope.vue';`。

- [ ] **Step 6: 前端构建验证**

Run: `cd mdp-vben && pnpm --filter web-open build`
Expected: 构建成功无类型错误

---

### Task 11: 端到端验证

**Files:** 无新增（验证任务）

- [ ] **Step 1: 后端全量编译 + 测试**

Run: `mvn -q compile -pl mdp-apps/md-open/open-pojo,mdp-apps/md-open/open-dao,mdp-apps/md-open/open-service,mdp-apps/md-open/open-web,mdp-apps/md-open/open-server -DskipTests`
Run: `mvn test -pl mdp-apps/md-open/open-server -Dtest='AppScopeApplyServiceImplTest,ScopeGroupServiceImplTest'`
Expected: 编译退出码 0；测试全部 PASS

- [ ] **Step 2: checkstyle**

Run: `mvn -q checkstyle:check -pl mdp-apps/md-open/open-pojo,mdp-apps/md-open/open-dao,mdp-apps/md-open/open-service,mdp-apps/md-open/open-web`
Expected: 退出码 0

- [ ] **Step 3: 提醒用户执行 SQL**

提醒用户在开发库执行 `docs/sql/2026-10-20-scope-group-apply.sql`（含 Task 8 的菜单/接口权限 INSERT，雪花 id 替换后）。

- [ ] **Step 4: 手工验收清单（启动后用户自验）**

1. web-open：应用详情 → 权限 tab → 勾选 2 组 + 理由 → 提交 → 可申请区该 2 组消失、申请记录出现 2 条待审批
2. 重复提交同组 → 报错"存在待审批的申请"
3. web-console：权限申请审核菜单 → 退回 1 条（不填意见 → 前端拦截；填意见 → 成功）→ web-open 申请记录显示退回+意见 → 该组回到可申请区
4. web-console：通过另 1 条 → web-open 已有权限区出现该组 → 再次申请该组在可申请区不可见
5. 详情弹窗：两端的【详情】均可看到组内接口表格和 oauth2 权限卡片

---

## Self-Review 记录

- **Spec 覆盖**：spec 决策 1-10 全覆盖（Task 1-3→模型；4-5→申请/审核；6→组查询；7→接口；8→菜单；9→console；10→open；弹窗规范→Task 9/10 全程 useVbenModal）
- **类型一致性**：`AppScopeApplyDto(appId, groupIds, applyReason)`、`AppScopeApplyReviewDto(id, approve, reviewComments)`、`listAppliable/listOwned/getGroupDetail` 签名在 Task 3/4/6/7/9/10 间一致
- **已知风险点**：Task 6 Step 5 的 `listAs` join 转 Vo 若不支持需切换 mapper 自定义查询（实现时验证）；前端原生 `vxe-table` 标签注册方式以实现时项目现状为准
