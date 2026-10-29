# 编码规则（单号生成器）· 设计方案

> 日期：2026-10-28
> 状态：需求已确认（头脑风暴 6 项决策），待用户审阅

## 一、需求背景

控制台「系统管理」新增**编码规则**配置功能：管理员维护各类单号的编码规则（编码标识唯一，一规则多明细，明细可拖拽排序）；系统内部各组件（含 md-api 层）通过统一接口按编码标识**单个/批量**生成单号。
硬性要求：**大并发、集群部署安全、单号绝不重复**、不加方法级锁。

## 二、已确认决策（头脑风暴结论）

| # | 决策点 | 结论 |
|---|--------|------|
| 1 | 流水号并发策略 | **Redis INCR 原子递增**（key 含周期标签 + TTL），DB 流水表做水位冷备恢复，恢复时跳跃前进（允许跳号、绝不重复） |
| 2 | 变量类别语义 | 变量类型两种：**映射**（配置 key→value 映射表，传 key 显示 value）、**替换**（传啥显示啥） |
| 3 | 规则类别 | 9 类：常量、日期、变量、流水号、随机码、校验位、GUID/雪花ID、组织编码、父单号截取 |
| 4 | 调用范围 | 内部 facade 调用（boot 本地直调 / cloud 走 /inner Feign），**不对第三方开发者开放** |
| 5 | 批量语义 | 三种接口都要：`generate`（单个）、`generateBatch`（同参 N 个）、`generateMulti`（逐参 N 个） |
| 6 | 组织编码承载 | `mdc_org` 新增 `code` 字段（唯一索引），组织管理页面同步加编码录入 |

## 三、数据库设计

表归属 console 库（`mdc_` 前缀）。三张新表实体均继承 `SuperEntity<Long>`（含 id、created_at、created_by、updated_at、updated_by 五字段，DDL 已逐表写全）。

### 3.1 mdc_code_rule（编码规则主表）

```sql
CREATE TABLE `mdc_code_rule` (
  `id` bigint NOT NULL COMMENT '主键',
  `code` varchar(64) NOT NULL COMMENT '编码标识（调用方按此生成）',
  `name` varchar(128) NOT NULL COMMENT '规则名称',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `state` bit(1) DEFAULT b'1' COMMENT '状态\n[1-启用 0-禁用]',
  `created_at` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `created_by` bigint DEFAULT '0' COMMENT '创建人ID',
  `updated_at` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) COMMENT='编码规则';
```

### 3.2 mdc_code_rule_item（规则明细表）

明细的差异化配置用 JSON 列承载（项目已有 `InterfaceConfigBase.config_json` 同类先例）：

```sql
CREATE TABLE `mdc_code_rule_item` (
  `id` bigint NOT NULL COMMENT '主键',
  `rule_id` bigint NOT NULL COMMENT '所属编码规则',
  `rule_type` tinyint NOT NULL COMMENT '规则类别\n[10-常量 20-日期 30-变量 40-流水号 50-随机码 60-校验位 70-GUID/雪花 80-组织编码 90-父单号截取]',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序（生成时按此顺序拼接）',
  `config_json` text COMMENT '类别差异化配置（JSON）',
  `state` bit(1) DEFAULT b'1' COMMENT '状态\n[1-启用 0-禁用]',
  `created_at` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `created_by` bigint DEFAULT '0' COMMENT '创建人ID',
  `updated_at` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人ID',
  PRIMARY KEY (`id`),
  KEY `idx_rule_id` (`rule_id`)
) COMMENT='编码规则明细';
```

### 3.3 mdc_code_sn（流水号水位表，Redis 冷备）

```sql
CREATE TABLE `mdc_code_sn` (
  `id` bigint NOT NULL COMMENT '主键',
  `rule_item_id` bigint NOT NULL COMMENT '规则明细ID',
  `period_key` varchar(32) NOT NULL COMMENT '周期标签（归零维度，如 202610/20261028/2026W43；不归零时为 FOREVER）',
  `current_value` bigint NOT NULL DEFAULT '0' COMMENT '当前水位（Redis 每跨 1000 步异步回写一次）',
  `created_at` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `created_by` bigint DEFAULT '0' COMMENT '创建人ID',
  `updated_at` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后修改时间',
  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_period` (`rule_item_id`, `period_key`)
) COMMENT='编码规则流水号水位';
```

### 3.4 mdc_org 加列

```sql
ALTER TABLE `mdc_org` ADD COLUMN `code` varchar(64) DEFAULT NULL COMMENT '组织编码' AFTER `short_name`;
-- 唯一索引（允许 NULL 多值，MySQL 唯一索引天然支持）
ALTER TABLE `mdc_org` ADD UNIQUE KEY `uk_code` (`code`);
```

## 四、规则明细配置模型（config_json 各类别内容）

> JSON 字段统一 camelCase（与项目 `InterfaceConfigBase.config_json` 等既有 JSON 配置先例一致）；后端对应每个类别一个 `XxxConfig` DTO 承载反序列化与校验。

| 类别 | config_json 字段 | 说明 |
|------|-----------------|------|
| 10-常量 | `{ "value": "SO-" }` | 固定前缀/后缀/中间值（按 sort 位置决定角色） |
| 20-日期 | `{ "pattern": "yyyyMMdd" }` | Java DateTimeFormatter 占位符，保存时校验合法性，生成时实时取值 |
| 30-变量 | `{ "varName": "orderType", "varType": "MAPPING\|REPLACE", "mappings": {"1": "电子产品"} }` | 映射：调用方传 key 显示 value，key 无映射时显示 key 本身兜底；替换：传啥显示啥 |
| 40-流水号 | `{ "initValue": 1, "step": 1, "length": 4, "padChar": "0", "resetRule": "NEVER\|YEAR\|MONTH\|WEEK\|DAY", "overflowStrategy": "ERROR\|GROW", "cycleOffset": 0, "offsetUnit": "HOUR\|DAY" }` | 未超长度按 padChar 补位（默认 "0"）；超长策略：ERROR 报错 / GROW 超长递增，默认 ERROR；周期偏移见 5.2.1 |
| 50-随机码 | `{ "length": 6, "charset": "DIGIT\|UPPER\|LOWER\|DIGIT_UPPER..." }` | 字符集可组合 |
| 60-校验位 | `{ "algorithm": "LUHN\|LUHN_MOD_N" }` | 基于已生成前文计算追加。LUHN 仅适用纯数字前文；含字母自动用 LUHN_MOD_N |
| 70-GUID/雪花 | `{ "type": "GUID\|SNOWFLAKE", "length": 8 }` | length>0 时截取前 N 位；雪花复用 md-db-uid 的 UidGenerator |
| 80-组织编码 | `{ "source": "DEPT\|COMPANY" }` | 取当前上下文用户的部门编码/机构（公司）编码（mdc_org.code） |
| 90-父单号截取 | `{ "varName": "parentCode", "cutType": "INDEX\|SEPARATOR", "startIndex": 2, "length": 8, "separator": "-", "splitPosition": 1 }` | 从调用方 map 取父单号截取复用。INDEX=按下标截取（startIndex 起，length 为空截到末尾，前缀定长场景）；SEPARATOR=取第 splitPosition 个分隔符之后的全部内容，**splitPosition 支持负数（从后往前数，-1=最后一个分隔符）**（前缀不定长场景，见下例） |

**90-父单号截取示例**（前缀不定长，按分隔符取后续内容）：

```
配置：{ "varName": "parentCode", "cutType": "SEPARATOR", "separator": "-", "splitPosition": 1 }
传入：parentCode = "总公司-2026-HW0002"（"总公司"长度不固定）
结果：取第 1 个 "-" 之后的全部 → "2026-HW0002"

splitPosition 支持负数（从后往前数）：
splitPosition = -1 → 取最后一个 "-" 之后 → "HW0002"
splitPosition = -2 → 取倒数第 2 个 "-" 之后 → "2026-HW0002"
```

**字段兜底决策（fail fast，宁报错不出错单号；映射除外）**：
- 变量/父单号：调用方 map 缺 key → 报错"缺少变量[xxx]"
- 父单号截取：INDEX 模式 startIndex 超出父单号长度 → 报错；SEPARATOR 模式 splitPosition=0 → 报错（无意义）；splitPosition 绝对值超过分隔符总数 → 报错
- 变量-映射模式：传了映射表外的 key → **不报错，直接显示 key 本身兜底**（如传 `3` 而映射表只有 1/2，则单号中显示 `3`）
- 组织编码：上下文无登录用户或组织未配置编码 → 报错
- 规则禁用/无启用明细 → 报错

## 五、生成引擎设计

### 5.1 流水线

```
generate(ruleCode, params):
  1. 加载规则+启用明细（Redis 缓存，key=mdc:codeRule:{ruleCode}，规则+明细整体 JSON 序列化存储，
     TTL 1h + 配置变更时主动删除缓存键失效；不用本地缓存——集群多实例下本地缓存失效不一致）
  2. 按 sort 排序，逐段执行对应 RuleItemGenerator.generate(context) → 拼接
  3. 校验位（若有）最后计算（基于已生成前文）
  4. 返回单号
```

缓存读写走 md-cache-starter 的 CacheOps 封装（含序列化与空值防穿透），缓存 key 遵循项目 CacheKeyBuilder 模式。

每个类别一个 `RuleItemGenerator` 实现（策略模式，`rule_type` → 实现类注册），新增类别只加一个实现类。

### 5.2 流水号并发核心（无锁、集群安全、不重复）

```
Redis key = mdp:sn:{ruleItemId}:{periodKey}
Lua 原子脚本（一次性完成 初始化判断+初始化+递增+设TTL）：
  if not exists(key):
      dbValue = 读 mdc_code_sn 水位（无记录则为 initValue - step）
      init = max(initValue - step, dbValue + 1000)  -- Redis 冷启动恢复时水位跳跃 +1000
      set(key, init)                                -- 首次创建从初始值起步（initValue - step，下一步 INCR 正好得到 initValue）
  v = INCRBY key step                               -- 原子返回本次递增值
  首次创建时补 EXPIRE = 周期 TTL（日=2天、周=9天、月=32天、年=367天，NEVER 不过期）
  
水位回写：INCR 结果每跨 1000 的倍数时，异步 UPDATE mdc_code_sn（不每次写，减少 DB 压力）
恢复语义：Redis 数据丢失时，从 DB 水位 +1000 起步 → 允许跳号，绝不重复
```

- **归零**：periodKey 按 resetRule 计算（YEAR=yyyy、MONTH=yyyyMM、WEEK=yyyy+'W'+ww、DAY=yyyyMMdd、NEVER=FOREVER），新周期天然新 key 从 initValue 重新开始

#### 5.2.1 周期偏移（cycleOffset / offsetUnit）

**用途**：周期切换点默认按自然时间（如按天归零 = 零点切换），但很多业务的"业务日/财务月"不是自然边界（零售仓储的业务日 = 当日 08:00 ~ 次日 07:59:59；财务月 = 每月 5 号起）。`cycleOffset` 对周期 key 的时间做一次性平移解决此类场景，**配置极简，一套逻辑兼容自然周期与偏移周期**。

```
参与周期计算的基准时间 = 生成时间 - cycleOffset（按 offsetUnit 换算，HOUR/DAY）
再用基准时间按 resetRule 格式化生成 periodKey
```

演算（`resetRule=DAY`、`cycleOffset=8`、`offsetUnit=HOUR`，即业务日早 8 点切换）：

```
生成时间 2026-10-28 01:30 → 基准 2026-10-27 17:30 → periodKey=20261027（归上一业务日）
生成时间 2026-10-28 08:01 → 基准 2026-10-28 00:01 → periodKey=20261028（切新周期）
```

**边界规则（必须遵守）**：
1. 偏移**只作用于 periodKey 生成**，不修改日期明细（20-日期）的取值——单号上打印的日期仍是原始生成时间，流水归属偏移后的周期桶，二者互不干扰
2. `resetRule=NEVER` 时偏移无效，前端页面将偏移字段置灰不可编辑
3. 只做一次时间平移，不支持跨多周期的复杂计算
4. `cycleOffset=0` 即原生自然周期行为（默认值，向后兼容）
5. **高危操作**：偏移配置改动后，历史已生成单号的周期归属不迁移；规则变更操作必须落 `@RequestLog` 审计日志
6. 单位字段 `offsetUnit` 独立配置（HOUR/DAY），页面对用户友好，不采用秒
- **批量**：`generateBatch/generateMulti` 中流水号段用 `INCRBY count` 一次原子分配连续区间，再逐个拼装
- **超长**：补位后长度超限 → ERROR 报错 / GROW 原样输出（按 overflowStrategy）

### 5.3 单号唯一性论证

唯一性由流水号段保证：同一规则内含流水号明细时，Redis INCR/INCRBY 原子递增 + 周期 key 隔离 + 水位跳跃恢复 ⇒ 任何时刻任何节点不会发出重复流水号。**建议规则中必须包含流水号或 GUID/雪花明细**（纯常量+日期+变量的规则无法防重），保存规则时给出校验提示（警告而非强制）。

## 六、接口设计

### 6.1 Service 接口（console-service）

```java
public interface CodeRuleService extends SuperService<CodeRule> {
    /** 生成单个单号 */
    String generate(String ruleCode, Map<String, String> params);
    /** 同参批量生成（流水号 INCRBY count 连续分配） */
    List<String> generateBatch(String ruleCode, Map<String, String> params, int count);
    /** 逐参批量生成（每组参数一个单号，流水号一次原子分配连续区间） */
    List<String> generateMulti(String ruleCode, List<Map<String, String>> paramsList);
    /** 规则/明细变更后删除 Redis 缓存键（配置页保存/删除/排序时调用） */
    void evictCache(Long ruleId);
}
```

入参上限校验：count / paramsList.size ≤ 1000（防滥用）。

### 6.2 Facade（内部服务调用）

- `console-facade/console-api`：`CodeRuleFacade`（同上三个方法）
- `console-boot-impl`：本地直调 CodeRuleService
- `console-cloud-impl`：Feign → `/inner/codeRule/generate|generateBatch|generateMulti`
- console-web：`InnerCodeRuleController`（`/inner/codeRule/**`，走既有 InnerAuthInterceptor 保护）

## 七、前端设计（web-console）

### 7.1 菜单

挂在「系统管理」（id=662697699833204736，code=console:system）下：

- 菜单：`console:system:codeRule` / 编码规则 / path=`/system/codeRule` / component=`/console/system/codeRule/index`
- 按钮：新增/编辑/删除/详情
- 接口权限：/system/codeRule/page|save|update|delete|getById + /system/codeRuleItem/listByRuleId|saveAll + /system/codeRule/testGenerate

### 7.2 页面

**编码规则列表页**（index.vue）：vxe-table（编码标识、名称、备注、状态、更新时间）+ 操作列【编辑】【明细配置】【删除】【试生成】

**明细配置**（useVbenDrawer 抽屉）：
- vxe-table 展示明细列表，**行拖拽排序**（参照 `interfaceConfig/data/config-json.tsx` 的 `rowDragConfig` 既有用法）
- 行内编辑：规则类别下拉 → 按类别动态渲染配置表单（常量/日期/变量/流水号/随机码/校验位/GUID/组织编码/父单号截取各自的表单字段）
- 变量类别子表单：变量类型（映射/替换）+ 变量名 + 映射模式时的 key→value 映射表编辑
- 父单号截取子表单：截取方式切换（按下标/按分隔符）——下标模式显示起始下标+截取长度（长度可空=截到末尾）；分隔符模式显示分隔符+第几个分隔符
- 流水号子表单：初始值、递增值、长度、补位字符（默认 0）、归零规则（不归零/按年/月/周/日）、超长策略（报错/超长递增）、周期偏移量+单位（小时/天，归零规则为"不归零"时置灰）
- 保存时整组提交（saveAll 全量替换该规则明细）

**试生成**（useVbenModal）：变量参数 key-value 动态行编辑 + 生成数量 → 调 `/system/codeRule/testGenerate` 展示结果

### 7.3 组织管理页面

组织新增/编辑表单加「组织编码」字段（唯一性后端校验），列表加编码列。

## 八、测试与验收

- `CodeRuleGeneratorTest`：9 种类别各自的生成逻辑单测（mock 上下文）
- `SnGeneratorConcurrencyTest`：流水号并发测试（多线程 100 线程 × 100 次，断言无重复、连续递增、补位正确、归零切换、超长策略两分支）
- 水位恢复测试：模拟 Redis 清空后从 DB 水位 +1000 恢复
- 前端构建验证 + 手工验收：配置规则 → 试生成 → facade 调用方（workbench 等）实际生成

## 九、工作量清单

| 端 | 内容 | 文件数（约） |
|----|------|------------|
| SQL | 3 张新表 + mdc_org 加列 + 菜单/接口权限 INSERT | 2 段脚本 |
| 后端 | 实体/Mapper/Service/生成器×9/Facade 三端/Inner Controller | ~30 |
| web-console | 规则列表页 + 明细抽屉 + 试生成弹窗 + 组织管理加编码 + api/权限点/国际化 | ~15 |
