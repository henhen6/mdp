# 文件存储 Tab 设计

## 定位
展示文件存储使用情况和文件分布

## 数据项

| 指标 | 数据来源 | 统计口径 | 展示形式 | 所属模块 |
|-----|---------|---------|---------|---------|
| 文件总数 | `mdc_file` | `count(id)` | 数字卡片 | md-console |
| 文件总容量 | `mdc_file` | `sum(file_size)` | 数字卡片（带单位） | md-console |
| 本月新增文件数 | `mdc_file` | `created_at` 在本月 | 数字卡片 | md-console |
| 本月新增容量 | `mdc_file` | 本月 `sum(file_size)` | 数字卡片（带单位） | md-console |
| 临时文件数量 | `mdc_file` | `object_type = 'temp'` | 数字卡片 | md-console |
| 临时文件容量 | `mdc_file` | `object_type = 'temp'` 的 `sum(file_size)` | 数字卡片（带单位） | md-console |
| 文件类型分布 | `mdc_file` | 按 `file_type` 分组统计（0-目录 1-图片 2-文档 3-视频 4-音频 99-其他） | 饼图 | md-console |
| 业务类型分布 | `mdc_file` | 按 `object_type` 分组统计 | 饼图 | md-console |
| 存储平台分布 | `mdc_file` | 按 `platform` 分组统计 | 饼图 | md-console |
| 文件大小分布 | `mdc_file` | 按文件大小区间分组统计 | 柱状图 | md-console |
| 文件增长趋势 | `mdc_file` | 支持任意日期区间，默认近7天，开始时间从0点，结束时间到23:59:59 | 折线图 | md-console |

## 接口设计

### md-console 模块
- `GET /dashboard/file/overview` - 文件概览（文件总数、总容量、本月新增文件数、本月新增容量、临时文件数量、临时文件容量）
- `GET /dashboard/file/typeDistribution` - 文件类型分布（按 file_type 统计）
- `GET /dashboard/file/objectTypeDistribution` - 业务类型分布（按 object_type 统计）
- `GET /dashboard/file/platformDistribution` - 存储平台分布
- `GET /dashboard/file/sizeDistribution` - 文件大小分布
- `GET /dashboard/file/trend` - 文件增长趋势（参数 `startDate`、`endDate`，日期区间必传，默认近7天）

### 文件增长趋势接口详细设计
- 请求方式：`GET`
- 请求参数：
  | 参数名 | 类型 | 必填 | 说明 |
  |-------|------|-----|------|
  | startDate | String | 是 | 开始日期，格式 yyyy-MM-dd |
  | endDate | String | 是 | 结束日期，格式 yyyy-MM-dd |
- 响应数据：折线图数据，日期区间不能为空
- SQL 条件：`created_at` 开始时间从 `00:00:00` 开始，截止时间到 `23:59:59`

## 数据实时性
准实时（15分钟缓存）
