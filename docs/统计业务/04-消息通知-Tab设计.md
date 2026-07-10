# 消息通知 Tab 设计

## 定位
展示消息发送情况和通知分布

## 数据项

| 指标 | 数据来源 | 统计口径 | 展示形式 | 所属模块 |
|-----|---------|---------|---------|---------|
| 消息任务总数 | `mdc_msg_task` | 所有任务数 | 数字卡片 | md-console |
| 今日发送消息数 | `mdc_msg_task` | `send_time=今天` 且 `status=2`（执行成功） | 数字卡片 | md-console |
| 待执行消息数 | `mdc_msg_task` | `status=1`（待执行） | 数字卡片 | md-console |
| 消息类型分布 | `mdc_msg_task` | 按 `type` 分组（1-站内信 2-短信 3-邮件） | 饼图 | md-console |
| 消息分类分布 | `mdc_notice` | 按 `msg_category` 分组（1-待办 2-公告 3-预警） | 饼图 | md-console |
| 消息发送趋势 | `mdc_msg_task` | 最近7天每日成功发送数 | 折线图 | md-console |
| 消息模板使用排行 | `mdc_msg_template` + `mdc_msg_task` | 各模板被使用的次数 TOP10 | 排行榜 | md-console |

## 接口设计

### md-console 模块
- `GET /dashboard/message/overview` - 消息概览（任务总数、今日发送数、待执行数）
- `GET /dashboard/message/typeDistribution` - 消息类型分布
- `GET /dashboard/message/categoryDistribution` - 消息分类分布
- `GET /dashboard/message/trend?days=7|30` - 消息发送趋势
- `GET /dashboard/message/templateRank?limit=10` - 消息模板使用排行

## 数据实时性
准实时（15分钟缓存）
