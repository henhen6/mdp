# 开放平台 Tab 设计

## 定位
展示开放平台应用、API、OAuth授权、事件推送等数据

## 数据项

### 一、应用与API统计

| 指标 | 数据来源 | 统计口径 | 展示形式 | 所属模块 |
|-----|---------|---------|---------|---------|
| 应用总数 | `mdo_app` | `state=1`（正常状态） | 数字卡片 | md-open |
| 自建应用数 | `mdo_app` | `type='10'` 且 `state=1` | 数字卡片 | md-open |
| 第三方应用数 | `mdo_app` | `type='20'` 且 `state=1` | 数字卡片 | md-open |
| API总数 | `mdo_api` | `state=1`（启用） | 数字卡片 | md-open |
| 今日API调用量 | `mdo_api_call_log` | `created_at=今天` | 数字卡片 | md-open |
| 今日调用失败数 | `mdo_api_call_log` | `created_at=今天` 且 `exec_status=2` | 数字卡片 | md-open |
| API调用趋势 | `mdo_api_call_log` | 最近7天每日调用量 | 折线图 | md-open |
| 应用调用排行 | `mdo_api_call_log` + `mdo_app` | 各应用调用次数 TOP10 | 排行榜 | md-open |
| API调用排行 | `mdo_api_call_log` + `mdo_api` | 各API调用次数 TOP10 | 排行榜 | md-open |
| OAuth授权统计 | `mdo_oauth_log` | 按 `grant_type` 分组统计 | 饼图 | md-open |
| 应用待审批数 | `mdo_app_apply` | `audit_status=1`（待审批） | 数字卡片 | md-open |

### 二、事件触发统计（基于 mdo_event_type + mdo_event_trigger）

| 指标 | 数据来源 | 统计口径 | 展示形式 | 所属模块 |
|-----|---------|---------|---------|---------|
| 事件类型触发统计 | `mdo_event_type` + `mdo_event_trigger` | 按 `event_code` 分组统计触发次数 | 柱状图 | md-open |
| 事件触发趋势 | `mdo_event_trigger` | 按天统计触发次数（支持日期区间查询） | 折线图 | md-open |
| 事件触发排行 | `mdo_event_type` + `mdo_event_trigger` | 按事件类型统计触发次数 TOP10 | 排行榜 | md-open |

### 三、事件推送统计（基于 mdo_event_type + mdo_event_trigger + mdo_event_push + mdo_event_push_log）

| 指标 | 数据来源 | 统计口径 | 展示形式 | 所属模块 |
|-----|---------|---------|---------|---------|
| 事件应用推送统计 | `mdo_event_type` + `mdo_event_trigger` + `mdo_event_push` | 按事件类型+应用分组，统计触发次数和推送次数 | 堆叠柱状图 | md-open |
| 事件推送趋势 | `mdo_event_trigger` + `mdo_event_push` + `mdo_event_push_log` | 按天统计触发次数、推送请求次数（支持日期区间查询） | 双轴折线图 | md-open |

**说明**：
- 触发次数：`mdo_event_trigger` 表中按天统计的记录数
- 推送请求次数：`mdo_event_push_log` 表中按天统计的记录数
- 关联关系：`mdo_event_push.event_trigger_id` → `mdo_event_trigger.id`

## 接口设计

### md-open 模块

#### 应用与API相关
- `GET /dashboard/open/overview` - 开放平台概览（应用总数、自建应用数、第三方应用数、API总数、今日调用量、今日失败数、待审批数）
- `GET /dashboard/open/callTrend?days=7|30` - API调用趋势
- `GET /dashboard/open/appRank?limit=10` - 应用调用排行
- `GET /dashboard/open/apiRank?limit=10` - API调用排行
- `GET /dashboard/open/oauthDistribution` - OAuth授权统计

#### 事件触发相关
- `GET /dashboard/open/event/trigger/statistics` - 事件类型触发统计（按事件类型分组统计触发次数）
- `GET /dashboard/open/event/trigger/trend?startDate=&endDate=` - 事件触发趋势（按天统计，支持日期区间）
- `GET /dashboard/open/event/trigger/rank?limit=10` - 事件触发排行榜 TOP10

#### 事件推送相关
- `GET /dashboard/open/event/push/statistics` - 事件应用推送统计（按事件类型+应用分组，统计触发次数和推送次数）
- `GET /dashboard/open/event/push/trend?startDate=&endDate=` - 事件推送趋势（按天统计触发次数和推送请求次数，支持日期区间）

## 数据实时性
实时（1分钟缓存）
