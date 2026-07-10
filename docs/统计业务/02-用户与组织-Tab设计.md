# 用户与组织 Tab 设计

## 定位
展示用户增长和组织结构，支持排行榜展示

## 数据项

| 指标 | 数据来源 | 统计口径 | 展示形式 | 所属模块 |
|-----|---------|---------|---------|---------|
| 用户总数 | `mdc_user` | `deleted_at=0` 且 `state=1` | 数字卡片 | md-console |
| 单位数量 | `mdc_org` | `org_type='10'` 且 `deleted_at=0` 且 `state=1` | 数字卡片 | md-console |
| 部门数量 | `mdc_org` | `org_type='20'` 且 `deleted_at=0` 且 `state=1` | 数字卡片 | md-console |
| 角色数量 | `mdc_role` | `deleted_at=0` 且 `state=1` | 数字卡片 | md-console |
| 用户增长趋势 | `mdc_user` | 最近7天/30天每日新增用户数 | 折线图 | md-console |
| 部门用户排行 | `mdc_user_org_rel` + `mdc_org` | 各部门下的用户数量 TOP10 | 排行榜 | md-console |
| 角色用户排行 | `mdc_user_role_rel` + `mdc_role` | 各角色下的用户数量 TOP10 | 排行榜 | md-console |
| 用户状态分布 | `mdc_user` | 正常/禁用用户占比 | 饼图 | md-console |
| 用户类型分布 | `mdc_user` | 普通用户/管理员/开发者/运维占比 | 饼图 | md-console |

## 接口设计

### md-console 模块
- `GET /dashboard/user/overview` - 用户与组织概览（用户总数、单位数量、部门数量、角色数量）
- `GET /dashboard/user/trend?days=7|30` - 用户增长趋势
- `GET /dashboard/user/orgRank?limit=10` - 部门用户排行
- `GET /dashboard/user/roleRank?limit=10` - 角色用户排行
- `GET /dashboard/user/statusDistribution` - 用户状态分布
- `GET /dashboard/user/typeDistribution` - 用户类型分布

## 数据实时性
准实时（15分钟缓存）
