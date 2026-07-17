# 消息通知 Tab 设计

## 定位
展示消息发送情况和通知分布

## 数据项

| 指标 | 数据来源 | 统计口径 | 展示形式 | 所属模块 |
|-----|---------|---------|---------|---------|
| 消息任务总数 | `mdc_msg_task` | 所有任务数 | 数字卡片 | md-console |
| 今日发送消息数 | `mdc_msg_task` | `send_time=今天` 且 `status=2`（执行成功） | 数字卡片 | md-console |
| 待执行消息数 | `mdc_msg_task` | `status=1`（待执行） | 数字卡片 | md-console |
| 草稿消息数 | `mdc_msg_task` | `status=0`（草稿） | 数字卡片 | md-console |
| 执行成功消息数 | `mdc_msg_task` | `status=2`（执行成功） | 数字卡片 | md-console |
| 执行失败消息数 | `mdc_msg_task` | `status=3`（执行失败） | 数字卡片 | md-console |
| 消息类型分布 | `mdc_msg_task` | 按 `type` 分组（1-站内信 2-短信 3-邮件） | 饼图 | md-console |
| 消息分类分布 | `mdc_msg_task` | 按 `msg_category` 分组（1-待办 2-公告 3-预警），增加 `type=1` 且 `status=2` 条件 | 饼图 | md-console |
| 消息发送趋势 | `mdc_msg_task` | 最近7天每日消息总数，支持任意日期区间，开始时间从0点算，截止时间到23:59:59，支持按消息类型筛选 | 折线图 | md-console |
| 消息模板使用排行 | `mdc_msg_template` + `mdc_msg_task` | 各模板被使用的次数 TOP10，`mdc_msg_template.state=1` | 排行榜 | md-console |

## 接口设计

### md-console 模块
- `GET /dashboard/message/overview` - 消息概览（任务总数、今日发送数、待执行数、草稿数、成功数、失败数）
- `GET /dashboard/message/typeDistribution` - 消息类型分布
- `GET /dashboard/message/categoryDistribution` - 消息分类分布（本地查询 `mdc_msg_task`，条件 `type=1 AND status=2`）
- `GET /dashboard/message/trend` - 消息发送趋势（参数 `startDate`、`endDate`、`type`，日期区间必传，默认近7天，支持多类型）
- `GET /dashboard/message/templateRank?limit=10` - 消息模板使用排行

### 消息发送趋势接口详细设计
- 请求方式：`GET`
- 请求参数：
  | 参数名 | 类型 | 必填 | 说明 |
  |-------|------|-----|------|
  | startDate | String | 是 | 开始日期，格式 yyyy-MM-dd |
  | endDate | String | 是 | 结束日期，格式 yyyy-MM-dd |
  | type | Integer | 否 | 消息类型（1-站内信 2-短信 3-邮件），不传则查所有类型 |
- 响应数据：折线图同时展示4条线（站内信、短信、邮件、总计），日期区间不能为空
- SQL 条件：`send_time` 开始时间从 `00:00:00` 开始，截止时间到 `23:59:59`

### 消息分类分布接口详细设计
- 数据来源：`mdc_msg_task` 表
- 筛选条件：`type = 1 AND status = 2`
- 分组字段：`msg_category`（1-待办 2-公告 3-预警）

### 消息模板使用排行接口详细设计
- 增加条件：`mdc_msg_template.state = 1`

## 数据实时性
准实时（15分钟缓存）
