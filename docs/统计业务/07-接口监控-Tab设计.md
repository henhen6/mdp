# 接口监控 Tab 设计

## 定位
展示接口调用监控、成功率和请求日志分析

## 数据项

| 指标 | 数据来源 | 统计口径 | 展示形式 | 所属模块 |
|-----|---------|---------|---------|---------|
| 接口总数 | `mdc_interface_config` | 所有接口数 | 数字卡片 | md-console |
| 今日调用总次数 | `mdc_interface_stat` | `sum(success_count + fail_count)` 且 `last_exec_at=今天` | 数字卡片 | md-console |
| 今日成功次数 | `mdc_interface_stat` | `sum(success_count)` 且 `last_exec_at=今天` | 数字卡片 | md-console |
| 今日失败次数 | `mdc_interface_stat` | `sum(fail_count)` 且 `last_exec_at=今天` | 数字卡片 | md-console |
| 接口成功率 | `mdc_interface_stat` | `success_count / (success_count + fail_count) * 100` | 仪表盘 | md-console |
| 接口调用排行 | `mdc_interface_stat` | 按调用总次数排序 TOP10 | 排行榜 | md-console |
| 接口失败排行 | `mdc_interface_stat` | 按失败次数排序 TOP10 | 排行榜 | md-console |
| 请求日志类型分布 | `mdc_request_log` | 按 `log_type` 分组（1-查询 2-新增 3-修改 4-删除 9-其他） | 饼图 | md-console |
| 请求地域分布 | `mdc_request_log` | 按省份统计请求次数 | 地图 | md-console |
| 请求耗时分布 | `mdc_request_log` | 按 `consuming_time` 区间分组统计 | 柱状图 | md-console |
| 异常请求统计 | `mdc_request_log` | `abnormal=1` 的数量 | 数字卡片 | md-console |

## 接口设计

### md-console 模块
- `GET /dashboard/monitor/overview` - 接口监控概览（接口总数、今日调用总次数、成功次数、失败次数、异常请求数）
- `GET /dashboard/monitor/successRate` - 接口成功率（仪表盘数据）
- `GET /dashboard/monitor/callRank?limit=10` - 接口调用排行
- `GET /dashboard/monitor/failRank?limit=10` - 接口失败排行
- `GET /dashboard/monitor/logTypeDistribution` - 请求日志类型分布
- `GET /dashboard/monitor/regionDistribution` - 请求地域分布
- `GET /dashboard/monitor/consumingTimeDistribution` - 请求耗时分布

## 数据实时性
实时（1分钟缓存）
