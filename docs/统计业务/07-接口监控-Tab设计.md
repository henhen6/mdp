# 接口监控 Tab 设计

## 定位
展示接口调用监控、成功率和请求日志分析

## 表说明
- `mdc_request_log` - Controller层接口的请求日志
- `mdc_interface_config` - 接口配置表（短信、邮件、站内信、微信模版消息等）
- `mdc_interface_stat` - 接口配置对应统计表（总成功数、总失败数）
- `mdc_interface_log` - 接口执行日志表（每次执行记录，status: 1-初始化 2-成功 3-失败）

## 模块区分（重要）
接口监控和请求日志是两类不同的统计数据，使用独立的 Service 和 Controller：

| 分类 | 数据来源 | Service | Controller |
|-----|---------|---------|-----------|
| 接口监控 | mdc_interface_log / mdc_interface_stat / mdc_interface_config | DashboardMonitorService | DashboardMonitorController |
| 请求日志 | mdc_request_log | DashboardRequestLogService | DashboardRequestLogController |

## 数据项

### 接口监控
| 指标 | 数据来源 | 统计口径 | 展示形式 |
|-----|---------|---------|---------|
| 接口总数 | `mdc_interface_config` | 所有接口数 | 数字卡片 |
| 今日调用总次数 | `mdc_interface_log` | `count(status IN (2,3))` 且 `exec_start_time=今天` | 数字卡片 |
| 今日成功次数 | `mdc_interface_log` | `count(status=2)` 且 `exec_start_time=今天` | 数字卡片 |
| 今日失败次数 | `mdc_interface_log` | `count(status=3)` 且 `exec_start_time=今天` | 数字卡片 |
| 总调用次数 | `mdc_interface_log` | `count(status IN (2,3))` 全量 | 数字卡片 |
| 总成功次数 | `mdc_interface_log` | `count(status=2)` 全量 | 数字卡片 |
| 总失败次数 | `mdc_interface_log` | `count(status=3)` 全量 | 数字卡片 |
| 接口成功率 | `mdc_interface_log` | `count(status=2) / count(status IN (2,3)) * 100` 今日 | 仪表盘 |
| 接口调用排行 | `mdc_interface_stat` | 按调用总次数排序 TOP10 | 排行榜 |
| 接口失败排行 | `mdc_interface_stat` | 按失败次数排序 TOP10 | 排行榜 |

### 请求日志
| 指标 | 数据来源 | 统计口径 | 展示形式 |
|-----|---------|---------|---------|
| 请求日志类型分布 | `mdc_request_log` | 按 `log_type` 分组（1-查询 2-新增 3-修改 4-删除 9-其他） | 饼图 |
| 请求地域分布 | `mdc_request_log` | 按省份统计请求次数 | 地图 |
| 请求耗时分布 | `mdc_request_log` | 按 `consuming_time` 区间分组统计 | 柱状图 |
| 异常请求统计 | `mdc_request_log` | `abnormal=1` 的数量 | 数字卡片 |

## 接口设计

### md-console 模块 - 接口监控
- `GET /dashboard/monitor/overview` - 接口监控概览（接口总数、今日/总调用次数、今日/总成功次数、今日/总失败次数）
- `GET /dashboard/monitor/successRate` - 接口成功率（仪表盘数据，统计 mdc_interface_log）
- `GET /dashboard/monitor/callRank?limit=10` - 接口调用排行
- `GET /dashboard/monitor/failRank?limit=10` - 接口失败排行

### md-console 模块 - 请求日志
- `GET /dashboard/requestLog/logTypeDistribution` - 请求日志类型分布
- `GET /dashboard/requestLog/regionDistribution` - 请求地域分布
- `GET /dashboard/requestLog/consumingTimeDistribution` - 请求耗时分布
- `GET /dashboard/requestLog/abnormalCount` - 异常请求数量

## 数据实时性
实时（1分钟缓存）

## 变更记录
### 2026-07-17
1. 拆分接口监控和请求日志为独立的 Service/Controller
2. `getOverview` 今日调用统计改为统计 `mdc_interface_log` 表（替代 mdc_interface_stat）
3. `getOverview` 新增总调用次数、总成功次数、总失败次数字段（统计 mdc_interface_log 全量）
4. `getSuccessRate` 改为统计 `mdc_interface_log` 表（替代 mdc_interface_stat）
5. `mdc_interface_log.status` 字段说明：1-初始化 2-成功 3-失败
