# 登录与安全 Tab 设计

## 定位
展示登录行为分析和安全监控，重点展示地域分布地图

## 数据项

| 指标 | 数据来源 | 统计口径 | 展示形式 | 所属模块 |
|-----|---------|---------|---------|---------|
| 今日登录次数 | `mdw_login_log` | `login_date=今天` 且 `status=01` | 数字卡片 | md-workbench |
| 今日登录失败次数 | `mdw_login_log` | `login_date=今天` 且 `status=02` | 数字卡片 | md-workbench |
| 登录地域分布 | `mdw_login_log` | 按省份统计登录次数（需过滤空值） | 地图 | md-workbench |
| 登录省份排行 | `mdw_login_log` | 按省份统计登录次数 TOP10 | 排行榜 | md-workbench |
| 登录IP排行 | `mdw_login_log` | 按登录IP统计次数 TOP10 | 排行榜 | md-workbench |
| 姓名登录排行 | `mdw_login_log` | 按 `name` 统计登录次数 TOP10 | 排行榜 | md-workbench |
| 浏览器分布 | `mdw_login_log` | 按 `browser_name` 分组统计 | 饼图 | md-workbench |
| 操作系统分布 | `mdw_login_log` | 按 `os` 分组统计 | 饼图 | md-workbench |
| 登录方式统计 | `mdw_login_log` | 按 `auth_type` 分组统计 | 饼图 | md-workbench |
| 登录渠道统计 | `mdw_login_log` | 按 `login_channel` 分组统计 | 饼图 | md-workbench |
| 事件类型统计 | `mdw_login_log` | 按 `event_type` 分组统计（01-登录 02-退出 03-注销 04-切换 05-扮演） | 饼图 | md-workbench |
| 每日登录统计 | `mdw_login_log` | 按天统计每天的登录总次数和总人次（去重用户数） | 折线图 | md-workbench |
| 活跃用户排行 | `mdw_login_log` | 最近7天登录次数 TOP10 | 排行榜 | md-workbench |
| 登录时段分布 | `mdw_login_log` | 按小时统计登录次数 | 热力图/柱状图 | md-workbench |

## 接口设计

### md-workbench 模块
- `GET /dashboard/login/overview` - 登录概览（今日登录次数、失败次数）
- `GET /dashboard/login/regionDistribution` - 登录地域分布（返回省份+数量，用于地图）
- `GET /dashboard/login/provinceRank?limit=10` - 登录省份排行 TOP10
- `GET /dashboard/login/ipRank?limit=10` - 登录IP排行 TOP10
- `GET /dashboard/login/nameRank?limit=10` - 姓名登录排行 TOP10
- `GET /dashboard/login/browserDistribution` - 浏览器分布统计
- `GET /dashboard/login/osDistribution` - 操作系统分布统计
- `GET /dashboard/login/authTypeDistribution` - 登录方式统计
- `GET /dashboard/login/channelDistribution` - 登录渠道统计
- `GET /dashboard/login/eventTypeDistribution` - 事件类型统计
- `GET /dashboard/login/dailyStatistics?days=7|30` - 每日登录统计（返回每天的登录次数和登录人次）
- `GET /dashboard/login/activeUserRank?limit=10` - 活跃用户排行
- `GET /dashboard/login/hourlyDistribution?date=today` - 登录时段分布

## 数据实时性
实时（1分钟缓存）
