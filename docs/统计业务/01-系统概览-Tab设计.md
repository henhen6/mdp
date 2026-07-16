# 系统概览 Tab 设计

## 定位
首页核心指标，一屏展示系统全貌

## 数据项

| 指标 | 数据来源 | 统计口径 | 展示形式 | 所属模块 |
|-----|---------|---------|---------|---------|
| 用户总数 | `mdc_user` | `deleted_at=0` 且 `state=1` | 数字卡片 | md-console |
| 组织总数 | `mdc_org` | `deleted_at=0` 且 `state=1` | 数字卡片 | md-console |
| 应用总数 | `mdo_app` | `state=1`（正常状态） | 数字卡片 | md-open |
| 今日登录次数 | `mdw_login_log` | `login_date=今天` 且 `status=01` | 数字卡片 | md-workbench |
| 今日接口调用量 | `mdo_api_call_log` | `created_at=今天` | 数字卡片 | md-open |
| 待办通知数 | `mdc_notice` | `msg_category=1`（待办）且今日创建 | 数字卡片 | md-console |
| 公告通知数 | `mdc_notice` | `msg_category=2`（公告）且今日创建 | 数字卡片 | md-console |
| 预警通知数 | `mdc_notice` | `msg_category=3`（预警）且今日创建 | 数字卡片 | md-console |
| 文件总数 | `mdc_file` | `count(id)` | 数字卡片 | md-console |
| 文件总容量 | `mdc_file` | `sum(file_size)` | 数字卡片（带单位） | md-console |

## 接口设计

### 数据聚合方式
由于不允许跨模块直接查询数据库表，系统概览数据需要通过以下方式聚合：

**方案：前端聚合**
- 各模块提供自己的统计接口
- 前端分别调用，汇总展示

### 接口清单

#### md-console 模块
- `GET /dashboard/overview/console`
  - 返回：用户总数、组织总数、待办通知数、公告通知数、预警通知数、文件总数、文件总容量

#### md-open 模块
- `GET /dashboard/open/overview`
  - 返回：应用总数、今日接口调用量

#### md-workbench 模块
- `GET /dashboard/overview/workbench`
  - 返回：今日登录次数

## 数据实时性
准实时（5分钟缓存）
