# 权限组申请与审核 · 设计方案

> 日期：2026-10-20
> 状态：已确认（需求决策 5 项 + 补充 3 项 + 弹窗规范 1 项）

## 一、需求背景

开发者在开发者中心（web-open）为自有应用**申请权限组**、**查看已有权限**；
管理员在控制台（web-console）**审核**开发者发起的申请。
权限组（`mdo_scope_group`）是开放接口（`mdo_api`）与 oauth2 权限（`mdo_oauth_scope`）的打包单元，
应用获得权限组后即拥有组内全部接口/权限的调用资格。

## 二、已确认决策

| # | 决策点 | 结论 |
|---|--------|------|
| 1 | 数据模型 | 新建申请表 `mdo_app_scope_apply`（一单一组），审核通过写入 `mdo_app_group_rel` |
| 2 | 申请粒度 | 前端可勾选多组一次提交，后端循环生成多张申请单，审核逐组通过/退回 |
| 3 | 状态机 | `audit_status [1-待审批 2-通过 99-退回]`，无草稿态，提交即待审批 |
| 4 | 申请约束 | 已生效（mdo_app_group_rel 有记录）或在审（有待审批单）禁止重复申请；退回后可重新申请（新单） |
| 5 | OauthScope | 结构不动；ScopeGroup 新增 `intro` 简介字段 |
| 6 | 开发者页面 | 应用详情新增「权限」tab，三区：可申请（卡片多选）、已有权限（卡片）、申请记录（表格） |
| 7 | 申请理由 | **必填**，审核员判断依据 |
| 8 | 审核通知 | 本期不做站内信通知（YAGNI），开发者看申请记录 |
| 9 | console 审核端 | 需要组详情弹窗（审核员看组内容再决定放行） |
| 10 | 弹窗规范 | 所有弹窗/抽屉统一使用框架封装的 `useVbenModal` / `useVbenDrawer`（参照 web-open `reset-keys.vue`、`appApply/wrapper.vue`） |

## 三、数据库设计

### 3.1 mdo_scope_group 加字段

```sql
ALTER TABLE `mdo_scope_group`
  ADD COLUMN `intro` varchar(512) DEFAULT NULL COMMENT '简介' AFTER `name`;
```

### 3.2 新建 mdo_app_scope_apply

```sql
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

### 3.3 菜单与接口权限（mdc_resource_menu / mdc_resource_api）

参照 `open:admin:appApply`（应用审核，parent_id=693083240537805901）新增：

- 菜单：`open:admin:appScopeApply` / 权限申请审核 / path=`/manage/appScopeApply` / component=`/open/admin/appScopeApply/index`
- 接口权限：`/admin/appScopeApply/page`、`/admin/appScopeApply/review`、`/admin/appScopeApply/getById`、`/admin/scopeGroup/getGroupDetail`、
  `/client/scopeGroup/listAppliable`、`/client/scopeGroup/listOwned`、`/client/scopeGroup/getGroupDetail`、`/client/appScopeApply/apply`、`/client/appScopeApply/page`

## 四、后端设计（md-open 模块）

### 4.1 实体层（open-pojo）

- `ScopeGroupBase`：+ `intro` 字段；同步 `ScopeGroupVo/Dto`
- 新建 `AppScopeApply` / `AppScopeApplyBase`（对应 `mdo_app_scope_apply`）
- 新建 `AppScopeApplyDto`（apply 入参：appId、groupIds、applyReason；review 入参：id、pass、reviewComments）
- 新建 `AppScopeApplyVo`（含 appName、groupName 回显字段）、`AppScopeApplyQuery`
- 新建 `ScopeGroupDetailVo`：组信息 + `apis: List<ApiVo>` + `scopes: List<OauthScopeVo>`（详情弹窗用）
- 新建枚举 `ScopeApplyAuditStatusEnum`（1-待审批 2-通过 99-退回），参照现有审核状态枚举模式

### 4.2 服务层（open-service）

`AppScopeApplyService`：
- `apply(appId, groupIds, applyReason)`
  - 归属校验：appId 必须属于当前登录开发者（参照 `listMyApp` 模式）
  - 逐组校验（fail fast，报具体组名）：组存在且 `state=启用` → 未拥有（mdo_app_group_rel 无记录）→ 无在审单
  - 批量插入申请单（batch）
- `review(id, pass, reviewComments)`
  - 仅 `待审批` 可审（否则报"申请单状态已变更"）
  - 通过：`audit_status=2` + 写 `mdo_app_group_rel`（先查重，幂等；uk_appid_groupid 兜底），reviewComments 选填
  - 退回：`audit_status=99` + reviewComments **必填**
- `pageMine(query)`：开发者查自己名下应用的申请记录（join app 过滤归属）

`ScopeGroupService` 扩展：
- `listAppliable(appId)`：`state=启用` 且未拥有且无在审单（left join 排除）
- `listOwned(appId)`：join `mdo_app_group_rel` 查已生效组
- `getGroupDetail(groupId)`：组 + 开放接口（`mdo_group_api_rel`→`mdo_api`）+ oauth2 权限（`mdo_group_scope_rel`→`mdo_oauth_scope`）

### 4.3 接口层（双 Controller，完全参照 AppApply 模式）

| Controller | 前缀 | 端点 | 用途 |
|-----------|------|------|------|
| `ClientScopeGroupController` | `/client/scopeGroup` | `GET /listAppliable`、`GET /listOwned`、`GET /getGroupDetail` | 开发者端 |
| `ClientAppScopeApplyController` | `/client/appScopeApply` | `POST /apply`、`POST /page` | 开发者端（page 仅自己应用） |
| `AppScopeApplyController` | `/admin/appScopeApply` | `POST /page`、`POST /review`、`GET /getById` | console 审核端 |
| `ScopeGroupController`（已有，扩展） | `/admin/scopeGroup` | `GET /getGroupDetail` | console 审核端详情 |

## 五、前端设计

### 5.1 web-console（审核端）

- `api/open/admin/appScopeApply/`（api.ts + model.d.ts，`ServicePrefixEnum.OPEN`）
- `views/open/admin/appScopeApply/index.vue`
  - vxe-table 列表：应用名称、权限组、申请理由、申请人、状态、申请时间、审核时间
  - 操作列（仅待审批行可用）：【通过】【退回】
  - 退回：`useVbenModal` 弹窗，审核意见必填
  - 行内【详情】：`useVbenModal` 组详情弹窗（复用下方组件）
- `views/open/admin/appScopeApply/modules/ScopeGroupDetailModal.vue`
  - 开放接口：vxe-table（apiName、description、apiVersion）
  - oauth2 权限：卡片（icon、name、code、intro）
- 权限点：`constants/perm/open/admin/appScopeApply.ts`

### 5.2 web-open（开发者端）

- `view.vue` 的 `settingList` 追加 `{ key: '5', name: '权限', component: Scope }`
- 新建 `views/open/client/app/modules/scope.vue`（三区容器）
- 新建 `ScopeGroupCard.vue`（卡片，可申请/已有两区复用）
- 新建 `ScopeGroupDetailModal.vue`（`useVbenModal`，结构同 console 端；两端各自实现不共享）
- `api/open/client/scopeGroup/` + `api/open/client/appScopeApply/`

页面结构：

```
┌─ 可申请权限组 ────────────────────┐
│ ☑┌──────┐ ☐┌──────┐ ☐┌──────┐  │
│  │名称A  │  │名称B  │  │名称C  │  │
│  │intro │  │intro │  │intro │  │
│  │[详情] │  │[详情] │  │[详情] │  │
│  └──────┘  └──────┘  └──────┘  │
│ 申请理由[textarea 必填] [提交申请]  │
├─ 已有权限 ───────────────────────┤
│ ┌──────┐ ┌──────┐（卡片，不可选）  │
├─ 申请记录 ───────────────────────┤
│ vxe-table: 组名|状态|审核意见|申请/审核时间 │
└─────────────────────────────────┘
```

## 六、关键流程

```
开发者勾选N组+理由 → apply → 归属/重复校验 → 生成N张待审批单
console审核列表 ← page
  ├─ 通过 → audit_status=2 → 写 mdo_app_group_rel(查重) → 应用即时获得权限
  └─ 退回 → audit_status=99 + 审核意见 → 开发者申请记录可见 → 可重新申请
```

## 七、安全与校验

- 开发者端所有接口强制 appId 归属校验（当前登录用户名下应用）
- 审核端接口走 console 权限点 + `mdc_resource_api` 接口权限
- 组 `state=启用` 才可出现在可申请列表与详情中
- `mdo_app_group_rel` 唯一索引 `uk_appid_groupid` 兜底防并发重复授权

## 八、测试要求

- `AppScopeApplyServiceImplTest`：apply 校验分支（组禁用/已拥有/在审/非本人应用）、review 通过幂等（重复审核、并发写 rel）、review 退回必填意见
- `ScopeGroupServiceImplTest`：listAppliable 三种排除场景、getGroupDetail 组装
- 覆盖率 ≥ 80%，junit5（org.junit.jupiter.api.Test）

## 九、工作量清单

| 端 | 内容 | 文件数（约） |
|----|------|------------|
| SQL | scope_group 加字段 + 建申请表 + 菜单/接口权限 INSERT | 4 段脚本 |
| 后端 | 实体/Base/Dto/Vo/Query、枚举、Mapper、Service×2、Controller×3 | ~20 |
| web-console | api + 审核页 + 详情弹窗 + 权限点 | ~7 |
| web-open | scope.vue + 卡片/弹窗 + api×2 + view.vue 注册 | ~9 |
