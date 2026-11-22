# 双因素认证（2FA）设计方案

- 日期：2026-11-16
- 状态：待评审
- 范围：mdp-apps（workbench 登录链路 + console 配置）+ mdp-vben（登录页 + 安全设置页）

## 1. 背景与目标

MDP 现有登录链路（`AuthServiceImpl.login`）在账号密码验证通过后直接颁发 token。本方案为其增加第二因素验证（短信/邮箱 OTP），并支持管理员全局策略与用户自主开通。

目标：

1. 全局策略三档（关闭 / 允许自选 / 强制全员），管理员可在控制台"系统配置"中切换
2. 用户在工作台"安全设置 → 双因素认证"中开通、关闭、更换验证渠道
3. 密码类登录触发二次验证，通过后才颁发 token
4. 满足等保三级"两种及以上鉴别技术"的可落地形态

### 等保三级依据

GB/T 22239-2019 8.1.4.1 c)：应采用两种或两种以上鉴别技术，其中一种鉴别技术至少应使用密码技术来实现。全局策略切到"强制全员"即满足要求。短信/邮箱 OTP 属于"所拥有之物"（持有因素），叠加密码（所知因素）构成双因素。

## 2. 关键决策记录

| 决策点 | 结论 | 理由 |
|---|---|---|
| 第二因素形式 | 短信/邮箱 OTP | 复用现有 `VerificationCodeService` 发送通道，零门槛落地 |
| 配置模式 | 全局策略 + 用户自开通 | 等保三级用"强制全员"档满足；平时"允许自选"不扰民 |
| 触发范围 | 仅密码类登录（CAPTCHA / USERNAME） | 手机/邮箱验证码登录、指纹、第三方登录本身已是持有因素，同因素叠加无安全增益 |
| 强制模式落地 | 登录时实时判定（FORCE 短路 shouldTrigger，无需批量开通） | 不依赖用户开通记录与异步任务，管理员切换策略即时生效 |
| 全局策略存储 | `mdc_config` 系统配置表 | 控制台已有"系统配置"页（`/system/config`），经 `ConfigFacade` 跨模块读取 |
| 登录返回结构 | `LoginVo` 增加字段 | 接口不变，前端向后兼容（未触发时新字段为 null） |
| 验证渠道 | 用户可选 PHONE / EMAIL / BOTH | BOTH = 手机或邮箱均可接收，登录时默认手机优先，可切换 |

## 3. 总体流程

### 3.1 密码登录（触发 2FA 时）

```
┌─────────┐  1.账号+密码+图形验证码        ┌───────────┐
│ 前端登录页│ ────────────────────────────→ │ POST /login │
│         │ ←─ 未触发：LoginVo(status=TOKEN)│ (现有接口)  │
│         │ ←─ 触发：LoginVo(status=TWO_FACTOR,│         │
│  2FA界面 │ twoFactorTicket,开通渠道,脱敏目标) └───────────┘
│         │
│         │  2a. POST /anyUser/auth/twoFactor/send   → 发送 OTP
│         │  2b. POST /anyUser/auth/twoFactor/verify → 验证通过
│         │ ←─ LoginVo(status=TOKEN, tokenValue, ...) 颁发 token
└─────────┘
```

关键规则：

- 第一步密码验证通过后**不创建任何 sa-token 会话**，仅签发一次性 2FA ticket 存 Redis
- ticket 绑定 userId + authType + deviceId，5 分钟有效，一次性使用，验证失败累计 5 次即作废
- 第二步验证通过后删除 ticket，走现有 `loginAuthenticated` 正常颁发 token，登录日志照常记录
- `twoFactorChannel` 回传用户**开通渠道**（PHONE/EMAIL/BOTH，票据签发时固定）：BOTH 时登录投递按绑定自适应（手机优先、无手机取邮箱），前端可凭 BOTH 展示“换一种方式”经 send 接口切到另一渠道；实际投递渠道只保存在票据内，不回传前端
- 会话颁发前必须凭票据中的 userId 重新加载用户并复核存在性与启用状态，防止票据有效期内账号被删除或禁用

### 3.2 触发判定矩阵

| 全局策略 | 用户已开通 | 密码类登录行为 |
|---|---|---|
| OFF | 任意 | 不触发，直接颁发 token |
| OPTIONAL | 是 | 触发 2FA |
| OPTIONAL | 否 | 不触发 |
| FORCE | 是 | 触发 2FA（渠道以 `TWO_FACTOR_FORCE_CHANNEL` 配置为准，不读开通记录） |
| FORCE | 否（未绑定配置指定渠道） | 严格拦截：PHONE 档提示"系统指定验证渠道为手机，请先绑定手机号，请联系管理员"，EMAIL 档对称，BOTH 档提示"请联系管理员"；不自动降级到另一渠道，不颁发 token |
| FORCE | 否（但已绑定配置指定渠道） | 视同触发（登录实时判定，不依赖开通记录） |

PHONE / EMAIL / TICKET / OAUTH2 / WXMA 等非密码类登录一律不触发。

## 4. 数据模型

### 4.1 全局策略配置（`mdc_config`）

| 字段 | 值 |
|---|---|
| config_group | `SECURITY` |
| uniq_key | `TWO_FACTOR_STRATEGY` |
| name | 双因素认证全局策略 |
| value | `OFF` / `OPTIONAL` / `FORCE`（默认 `OFF`） |
| data_type | 1（字符串） |

FORCE 档位的验证渠道由同组第二个配置项决定：

| 字段 | 值 |
|---|---|
| config_group | `SECURITY` |
| uniq_key | `TWO_FACTOR_FORCE_CHANNEL` |
| name | 双因素认证强制渠道 |
| value | `PHONE` / `EMAIL` / `BOTH`（默认 `BOTH`）；配置缺失、读取异常或值非法时回退 `BOTH` |
| data_type | 1（字符串） |

读取通道：workbench 侧经 `ConfigFacade` 读取并做本地短缓存（60 秒），两个配置项各自独立缓存，避免每次登录查库。

### 4.2 新表 `mdw_user_two_factor_auth`

> 表名遵循 workbench 域 `mdw_` 前缀规范（与 `mdw_login_log` 一致）。

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT PK | 雪花ID |
| user_id | BIGINT NOT NULL | 唯一索引 `uk_user_id` |
| enabled | TINYINT NOT NULL | 0-关闭 1-开通 |
| channel | VARCHAR(16) NOT NULL | PHONE / EMAIL / BOTH |
| created_at | DATETIME | 强制字段 |
| created_by | BIGINT | 强制字段 |
| updated_at | DATETIME | 可选字段 |
| updated_by | BIGINT | 可选字段 |

一个用户一条记录（uk_user_id 唯一），不侵入 User 表。实体遵循项目规范拆分 `UserTwoFactorAuthBase`（表字段）与 `UserTwoFactorAuth`（业务方法）。

### 4.3 SQL 变更文件

新增 `docs/sql/20261116-security-two-factor-table.sql`：

1. 建表 `mdw_user_two_factor_auth`
2. 插入 `mdc_config` 配置项 `TWO_FACTOR_STRATEGY`（默认 `OFF`）

## 5. 后端设计（mdp-apps / md-workbench + console）

### 5.1 模块归属

- 实体/DAO：`workbench-pojo`（`entity/base/UserTwoFactorAuthBase`、`entity/UserTwoFactorAuth`、dto、vo、枚举）+ `workbench-dao`（`UserTwoFactorAuthMapper`）
- 服务：`workbench-service` 新增 `TwoFactorAuthService` / `TwoFactorTicketService`；二次验证通过后的会话编排在 `AuthService#verifyTwoFactorLogin`（票据消费、账号复核、颁发会话同处一处）
- 控制器：`workbench-web` 新增 `TwoFactorAuthController`，同一控制器承载匿名段（`/anyUser/auth/twoFactor/*`）与登录段（`/profile/twoFactor/*`）
- 策略读取：`workbench-service` 封装 `TwoFactorStrategyResolver`，内部走 `ConfigFacade` + 本地缓存

### 5.2 接口清单

匿名接口（路径含 `/anyUser/`）：

| 接口 | 入参 | 出参 | 说明 |
|---|---|---|---|
| `POST /anyUser/auth/twoFactor/send` | `TwoFactorSendDto{ticket, channel?}` | `R<String>`（返回脱敏目标，换渠道后前端展示用） | 校验 ticket → 按 channel（默认用户开通渠道，BOTH 默认手机）生成 6 位 OTP 并发送 |
| `POST /anyUser/auth/twoFactor/verify` | `TwoFactorVerifyDto{ticket, code}` | `R<LoginVo>` | 校验 OTP → 删除 ticket → `loginAuthenticated` 颁发 token |

登录接口（需登录，工作台安全设置）：

| 接口 | 入参 | 出参 | 说明 |
|---|---|---|---|
| `GET /profile/twoFactor/status` | 无 | `R<TwoFactorStatusVo{enabled, channel, strategy, maskedTarget}>` | 安全设置页展示 |
| `POST /profile/twoFactor/sendEnableCode` | `TwoFactorChannelDto{channel}` | `R<String>`（返回脱敏接收目标） | 开通/换渠道前，向目标渠道发送验证码（60s 间隔） |
| `POST /profile/twoFactor/enable` | `TwoFactorEnableDto{channel, code}` | `R<Boolean>` | 验证 OTP → 写入开通（ON DUPLICATE 更新） |
| `POST /profile/twoFactor/disable` | `TwoFactorDisableDto{code}` | `R<Boolean>` | 验证当前渠道 OTP → 关闭。FORCE 策略下拒绝（fail fast 抛业务异常） |
| `POST /profile/twoFactor/changeChannel` | `TwoFactorEnableDto{channel, code}` | `R<Boolean>` | 验证新渠道 OTP → 更新 channel |

`status` 接口三态语义：

- OFF：`enabled=false`，channel/maskedTarget 为 null；策略短路，不查用户与开通记录，前端按功能关闭展示；
- FORCE：`enabled=true`，忽略用户开通记录，channel 直接回显 `TWO_FACTOR_FORCE_CHANNEL` 配置值（PHONE / EMAIL / BOTH，不按绑定自适应）；maskedTarget 按该渠道解析脱敏目标（BOTH 手机优先无手机取邮箱），用户未绑定配置指定渠道时 maskedTarget=null，前端已有兜底，登录时再由签票环节严格拦截；
- OPTIONAL：读用户开通记录，已开通则 `enabled=true` 并回显记录 channel 与对应脱敏目标；未开通 `enabled=false`，channel/maskedTarget 为 null。

### 5.3 LoginVo 变更

`LoginVo`（`workbench-pojo`）增加字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| status | String | `TOKEN`（默认）/ `TWO_FACTOR` |
| twoFactorTicket | String | 一次性票据，仅 status=TWO_FACTOR 时非空 |
| twoFactorChannel | String | 用户开通的验证渠道 PHONE / EMAIL / BOTH（票据签发时确定）；BOTH 时实际投递渠道默认手机、可经 send 接口切换，投递渠道不回传 |
| twoFactorMaskedTarget | String | 脱敏接收目标，如 `138****5678`、`a***@x.com` |

未触发 2FA 的所有既有登录路径（含第三方、ticket、WXMA）status 赋 `TOKEN` 或留空，前端以非 `TWO_FACTOR` 判定为直接登录成功，兼容零改动。

### 5.4 核心服务与缓存键

- `TwoFactorAuthService`：触发判定、开通/关闭/换渠道；FORCE 下登录与 status 均按强制渠道配置（`TWO_FACTOR_FORCE_CHANNEL`）判定，不维护批量开通状态
- `TwoFactorTicketService`：ticket 签发/校验/作废（Redis，key 前缀 `workbench:two_factor:ticket:`，值含 userId、authType、deviceId、channel；TTL 300s；失败计数 key `:fail` 同 TTL）
- OTP 缓存键：独立场景 `LOGIN_TWO_FACTOR` / `TWO_FACTOR_ENABLE`（`md-cache-key` 模块新增 CacheKeyBuilder），与图形验证码、换绑验证码场景完全隔离，杜绝跨场景用码；TTL 300s、发送间隔 60s、校验失败 5 次作废
- 事件：复用 `LoginEvent`，`LoginLogDto` 增加登录事件类型（二步验证通过 / 失败），审计可追溯

### 5.5 FORCE 实时判定说明

FORCE 不做批量开通，原事件链路（console 发布 `TwoFactorForceEnableEvent` → workbench `TwoFactorForceEnableListener` 异步写库，含 `batchEnableForForce`）已整体废弃：

- `shouldTrigger` 对 FORCE 短路恒 true；`startTwoFactor` 不读用户开通记录表，openedChannel 直接取 `TwoFactorStrategyResolver.resolveForceChannel()`（即 `TWO_FACTOR_FORCE_CHANNEL` 配置，默认 BOTH），`status` 的 FORCE 回显同口径；
- 配置值决定 FORCE 用户的开通渠道：`PHONE` 档仅按手机投递、`EMAIL` 档仅按邮箱投递，用户未绑定指定渠道时**严格拦截登录、不自动降级**到另一渠道，文案具体化为"系统指定验证渠道为手机，请先绑定手机号，请联系管理员"（EMAIL 对称）；`BOTH` 为默认档，openedChannel=BOTH、首次投递手机、可经 send 接口切换邮箱，两渠道皆无时以"双因素认证渠道未绑定手机或邮箱，请联系管理员"拦截；
- 强制渠道配置做 60 秒本地缓存（与策略缓存相互独立），配置缺失、读取异常或值非法时回退 BOTH；管理员切换策略/渠道即时生效（缓存窗口最长 60 秒），不存在批量任务遗漏用户；未绑定配置指定渠道的用户一律不颁发 token。

## 6. 前端设计（mdp-vben / web-workbench）

### 6.1 登录页两步切换（`views/_core/sso/`）

- `data/account.ts`：`handleAccountLogin` 响应 `status === 'TWO_FACTOR'` 时，切换到 2FA 第二步状态（复用 `useLogin` 的 state 机制）
- 新增 `two-factor-login.vue`：6 位验证码输入 + 脱敏目标展示 + 60s 倒计时重发按钮 + "换一种方式"（BOTH 时切换 PHONE/EMAIL 后重调 send）+ 验证失败计数提示；verify 成功后复用 `authStore` 既有登录成功逻辑
- "换一种方式"入口的唯一触发条件是登录第一步响应 `twoFactorChannel=BOTH`（canSwitch 只认 BOTH）：OPTIONAL 用户开通 BOTH 与 FORCE 配置为 BOTH 且两渠道齐的用户均返回 BOTH；FORCE 配置为 PHONE/EMAIL 时 openedChannel 恒为单渠道，不展示入口；点击后带 `channel` 调 send 接口，后端按用户实时绑定解析目标（窗口期解绑/换绑以后端结果为准），返回的新脱敏目标用于刷新展示
- 非 `TWO_FACTOR` 响应走现有逻辑，零改动

### 6.2 安全设置页实装（`views/workbench/user/security/modules/two-factor.vue`）

替换现占位（按钮 disabled、"敬请期待"），三态卡片：

- 未开通：说明文案 + "开通"按钮 → 弹窗选渠道（手机 / 邮箱 / 手机或邮箱均可）→ 发送并输入验证码 → 确认开通
- 已开通：展示渠道与脱敏目标 + "关闭" / "更换渠道"按钮（同样走验证码确认）；FORCE 策略时"关闭"置灰并提示"管理员已强制启用"
- status 接口返回 `strategy` 用于展示当前全局策略说明

### 6.3 控制台

无新增页面：`mdc_config` 的 `TWO_FACTOR_STRATEGY` 在既有"系统配置"页维护，仅需在 SQL 中预插配置项。配置页如需下拉枚举（OFF/OPTIONAL/FORCE），按该页现有参数值编辑方式落地。

## 7. 安全细节与错误处理

- fail fast：ticket 无效/过期返回明确业务异常，前端引导回第一步重新登录，不留半登录态
- OTP 与其他验证码场景隔离（独立 CacheKey），校验时比较 Redis 值后立即删除，防重放
- 发送接口防滥用：60s 发送间隔 + 行为验证码可后续叠加（第一版先做间隔限制）
- 脱敏输出：所有对外返回的手机/邮箱均脱敏，登录页不回显完整号码，防账号枚举
- 日志：不输出验证码明文与 ticket 全值（仅前 8 位），遵守"注释和日志不输出密钥 token"约定
- 修改认证相关代码：**上线前需评审安全影响**（sa-token 会话创建时机变化）

## 8. 测试策略（TDD）

- 后端单测：
  - `TwoFactorAuthService` 触发判定矩阵（3 策略 × 开通状态 × 登录方式，对齐 3.2 表格逐格断言）
  - ticket 生命周期：签发、一次性消费、5 次失败作废、过期
  - OTP 场景隔离：LOGIN_TWO_FACTOR 验证码不能通过 TWO_FACTOR_ENABLE 场景校验
  - FORCE 实时判定：openedChannel 取 `resolveForceChannel()` 配置——BOTH+两渠道齐 opened=BOTH（响应 twoFactorChannel=BOTH）、PHONE 档有手机按手机投递/缺手机（即使有邮箱）严格拦截并报"系统指定验证渠道为手机…"、EMAIL 档对称、BOTH 档双空拦截；Resolver 补缓存命中、facade 异常与非法值回退 BOTH 用例；登录第二步换渠道按实时绑定解析目标（窗口期解绑报"该渠道未绑定接收目标"、换绑发往新目标），默认发送沿用票据快照不查库
- 前端 vitest：
  - 登录页 status 分支切换（对齐 `social-callback.test.ts` 风格）
  - two-factor.vue 三态渲染与 FORCE 置灰逻辑
- 最低覆盖率遵循项目 80% 要求

## 9. 边界情况清单

| 场景 | 行为 |
|---|---|
| 用户手机邮箱都未绑定且 OPTIONAL | 不影响登录，无法开通（前端提示先绑定） |
| FORCE 下用户未绑定配置指定渠道（含两渠道皆无） | 严格拦截登录、不自动降级：PHONE 档提示"系统指定验证渠道为手机，请先绑定手机号，请联系管理员"，EMAIL 档对称，BOTH 档提示"双因素认证渠道未绑定手机或邮箱，请联系管理员"；status 返回 channel=配置值、maskedTarget=null |
| FORCE 下用户关闭 | 后端拒绝（业务异常），前端按钮置灰双保险 |
| 用户开通后手机解绑/失效 | 登录发送 OTP 失败时提示改用另一渠道（BOTH）或联系管理员 |
| 2FA ticket 有效期内重复 /login | 每次密码验证通过签发新 ticket，旧 ticket 立即作废（同用户仅一个有效 ticket） |
| verify 与 send 并发 | ticket 消费原子化（Redis 删除即消费），删除成功才放行 |
| 老版本前端 | 收到 TWO_FACTOR 响应时无 token 无法进入系统（服务端未颁发会话），不产生安全窗口 |
