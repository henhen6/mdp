# 密码过期、临期提醒与找回策略设计

- 日期：2026-11-23
- 状态：已确认
- 后端仓库：`mdp`
- 前端仓库：`mdp-vben`（`apps/web-workbench`）

## 1. 背景与现状

`mdc_user.pw_expire_time` 已存在于 `UserBase`，当前系统已经具备以下能力：

- `PASSWORD_EXPIRE_TIME` 配置密码有效期，初始化值为 `3M`。
- `PASSWORD_EXPIRE_NOT_ALLOW_LOGIN` 配置密码过期后是否禁止登录，初始化值为 `false`。
- 新增用户、注册、个人修改密码、管理员重置密码、邮箱令牌找回和手机/邮箱验证码找回时，均会重算 `pwExpireTime`。
- 用户名密码登录已存在过期拦截，但判断发生在密码比对之前，提示文案仍引导“修改密码”，与禁止登录后的实际可执行路径不一致。
- `/console/anyone/getUserinfo` 已返回 `pwExpireTime`，前端已有“安全设置 → 修改密码”页面及修改成功后的用户信息刷新逻辑。

本期不新增用户表字段，不重复建设密码修改或找回页面，重点补齐安全的认证门禁、临期状态计算和前后端提醒闭环。

## 2. 已确认决策

| 决策点 | 结论 |
|---|---|
| 禁止登录的认证范围 | 仅 USERNAME/CAPTCHA 密码认证；手机、邮箱验证码、社交、指纹和微信小程序登录不被密码过期直接拦截 |
| 有效期配置变更 | 不追溯现有用户，只影响新用户及此后修改/重置密码的用户 |
| 临期阈值 | 新增系统配置，默认提前 7 个自然日，`0` 表示关闭临期提醒 |
| 过期后的找回路径 | 禁止密码登录时，引导用户通过现有“忘记密码”流程重置 |
| 允许过期登录时 | 允许进入系统，但每次登录显示已过期提醒，引导立即修改密码 |
| 前端范围 | 后端与 `mdp-vben/apps/web-workbench` 同步修改 |

## 3. 配置与错误码

### 3.1 系统配置

| 配置键 | 默认值 | 含义 |
|---|---|---|
| `PASSWORD_EXPIRE_TIME` | `3M` | 密码有效期；空值表示永不过期，沿用现有时间单位 |
| `PASSWORD_EXPIRE_NOT_ALLOW_LOGIN` | `false` | `true` 时过期密码不能用于密码登录；`false` 时仍可登录但显示提醒 |
| `PASSWORD_EXPIRE_REMIND_DAYS` | `7` | 提前提醒的自然日数；`0` 关闭临期提醒，负数及非整数视为非法配置 |

新配置加入 `ConfigKey.Workbench`，初始化到 `mdc_config` 的 `system` 分组，`data_type=2`。同时提供全量 `mdp.sql` 初始化数据和面向存量库的增量 SQL，不修改已有两个密码配置的历史值。

### 3.2 专用业务错误

在 `ExceptionCode` 中新增：

```text
PASSWORD_EXPIRED(-17, "密码已过期，请通过忘记密码重置后再登录")
```

`-17` 紧接现有幂等错误码 `-14` 至 `-16`，前端只根据错误码识别，不解析异常文案。

## 4. 密码状态模型

### 4.1 状态定义

使用四种展示状态：

| 状态 | 判定 |
|---|---|
| `NEVER` | `pwExpireTime` 为 `null`，或值为 `LocalDateTime.MAX` 所代表的永不过期时间 |
| `EXPIRED` | 非永不过期，且 `now >= pwExpireTime` |
| `EXPIRING` | 未过期，提醒阈值大于 0，且过期日期距离当前日期不超过阈值 |
| `NORMAL` | 其他情况 |

所有后端判断在一次计算中使用同一个 `now`，避免跨边界时重复调用 `LocalDateTime.now()` 得到不一致结果。

### 4.2 剩余天数

- 按服务器当前日期与过期日期计算自然日差，不按客户端时间，也不简单用 24 小时整除。
- `NORMAL`、`EXPIRING` 返回实际剩余自然日数；同一天内尚未过期时返回 `0`，前端显示“今天过期”。
- `EXPIRED` 返回 `0`，具体状态由 `passwordExpireStatus` 表达。
- `NEVER` 返回 `null`。

### 4.3 纯计算组件

在 `md-common-pojo` 增加无数据库、无缓存依赖的密码过期计算器，输入当前时间、过期时间和提醒阈值，输出状态及剩余天数。workbench 登录门禁与 console 用户信息组装复用同一套边界规则，避免两端对“等于过期时间”“永不过期”“今天过期”的判断不一致。

> 模块归属说明（2026-11-23 修订）：计算器初版误置于 `md-common-config`，该模块是仅限 server 层引入的运行时装配模块（AutoConfiguration/Interceptor 等），service 层不得依赖；已迁至 `md-common-pojo`，各 service/web 层经既有传递链（如 console-service → console-pojo → md-common-pojo）可见，与 `ConfigKey` 同款使用方式。

## 5. 后端设计

### 5.1 密码登录门禁

调整 `UsernameLoginStrategyImpl.checkUserPassword` 的顺序：

```text
用户存在性检查
  → 账号锁定检查
  → 密码摘要比对
  → 密码正确后读取过期禁止登录配置
  → 过期且禁止登录：记录失败登录事件并抛出 PASSWORD_EXPIRED
  → 返回认证成功
```

安全约束：

- 密码错误时不得读取或泄露密码过期状态，仍返回统一的“用户名或密码错误”。
- 过期日志只在密码已经验证通过后记录，避免未认证请求制造“密码已过期”的可信审计事件。
- 禁止登录只影响密码认证，不把该判断下沉到通用 `loginAuthenticated`，避免误伤社交、指纹、手机/邮箱验证码等独立认证方式。
- 双因素认证在第一步密码验证时已经执行过期门禁；第二步 OTP 验证保留现有用户存在性和启用状态复核，不重复签发或绕过密码校验。
- 禁止登录配置通过新增的 `ConfigFacade#getBooleanRequired` 严格读取：cloud 实现对空响应或失败响应记录 key、响应 code/msg 并抛出异常，调用点记录具体错误并终止登录；不得把 Facade 降级结果当成 `false` 放行。现有 `getBoolean` 的其他消费者保持原行为。

### 5.2 过期时间更新策略

以下场景在密码写入成功的同一事务中设置：

```text
pwExpireTime = 密码变更时间 + 当前 PASSWORD_EXPIRE_TIME
```

覆盖：

- 控制台新增用户；
- 用户名、手机、邮箱及社交注册初始化；
- 个人中心修改密码；
- 管理员重置密码；
- 邮箱令牌找回密码；
- 手机/邮箱验证码找回密码。

以下操作不重算：修改用户名、姓名、手机、邮箱、组织、岗位、角色、头像和最近登录组织。

### 5.2.1 续期豁免保护（2026-11-23 变更）

密码变更场景（管理员重置、个人修改、邮箱令牌找回、手机验证码找回）更新 `pwExpireTime` 前，若用户现有 `pwExpireTime` 为 `null`，视为该用户豁免密码过期策略，**不覆盖该字段**；有值（含 `LocalDateTime.MAX`）才按当前 `PASSWORD_EXPIRE_TIME` 重算。

- 豁免依据：`null` 在状态模型中表示永不过期（§4.1），此类用户多为手动豁免（如内置账号），重置密码不得把豁免覆盖成有期限。
- `LocalDateTime.MAX` 不豁免（用户裁决）：历史版本在配置为空时曾写入 `MAX` 值，这类用户重置密码后重算为 `now + 周期`。
- 实现方式：豁免判断收口在 `PasswordExpiryCalculator.renewExpireTime(oldExpireTime, period)`——`oldExpireTime` 为 `null` 时返回 `null`；否则返回 `DateUtils.conversionDateTime(now, period)`（含配置为空时的 `MAX` 语义）。调用点以返回值为准：非 `null` 才调用 setter（`UpdateEntity` 未 set 的字段不会进 UPDATE 语句，不得 `setPwExpireTime(null)`）。返回 `null` 当且仅当豁免，调用点不做二次 null 判断。`PasswordExpiryCalculator` 位于 `md-common-pojo`，console-service 与 workbench-service 经既有传递链可见，无需新增依赖（`md-common-config` 仅限 server 层引入，service 层不得依赖）。
- 控制台新增用户（`saveBefore`）与注册/SSO 初始化（`initSsoUser`）不适用此保护：新实体无旧值，仍按配置计算。

修改 `PASSWORD_EXPIRE_TIME` 不批量更新历史数据。需要让某用户按新周期续期时，由管理员重置密码或用户修改密码完成；本期不提供单独或批量强制立即过期操作。

### 5.3 当前用户信息返回值

复用 `ResourceController#getUserinfo` 和 `UserinfoVo`，保留已有 `pwExpireTime`，新增：

| 字段 | 类型 | 说明 |
|---|---|---|
| `passwordExpireStatus` | `String` | `NEVER/NORMAL/EXPIRING/EXPIRED` |
| `passwordExpireRemainDays` | `Long` | 剩余自然日数；永不过期为 `null` |
| `passwordExpireRemindDays` | `Integer` | 当前生效的提醒阈值，便于前端展示和测试 |

ResourceController 通过现有 ConfigService 读取阈值并调用统一计算器。提醒配置读取失败时记录错误，阈值降级为 `0`，不影响已登录用户获取资料；认证禁止配置不采用这一降级规则。

## 6. 前端设计（mdp-vben）

### 6.1 密码过期禁止登录

在 `views/_core/sso/data/account.tsx` 的登录异常处理中识别 `-17`：

- 显示明确弹窗：“密码已过期，请通过忘记密码重置后再登录”。
- 主按钮为“忘记密码”，点击后进入项目现有忘记密码页面，不新增找回页面。
- 次按钮为“取消”，关闭弹窗并停留登录页。
- 全局请求层仅对 URL 以 `/anyUser/auth/login` 结尾的 `-17` 响应抑制重复 toast；指纹密码复核等其他接口仍显示后端具体错误，不扩大静默范围。
- 不写 Token、不创建半登录态；保留现有验证码刷新和其他异常处理。

### 6.2 登录成功后的提醒

扩展 workbench 的用户信息类型，并在 `authStore.completeLogin` 获取用户信息后执行密码状态提醒：

- `NEVER`、`NORMAL`：不提示。
- `EXPIRING`：每次成功登录最多弹出一次，显示过期日期和剩余天数；剩余 `0` 天时显示“今天过期”。
- `EXPIRED`：显示“密码已过期，请立即修改”，不因 `PASSWORD_EXPIRE_REMIND_DAYS=0` 而隐藏。
- 弹窗按钮为“立即修改”和“稍后修改”。“稍后修改”仅关闭本次弹窗，不写永久忽略标记，不阻断首页跳转。
- `completeLogin` 在原站内/SSO 跳转前等待用户选择：“立即修改”取消原 SSO/onSuccess 跳转并进入现有安全设置密码页签；“稍后修改”或关闭弹窗才继续原跳转，避免整页卸载导致提醒一闪而过。
- 所有调用 `completeLogin` 的认证方式共享提醒逻辑，密码、验证码、社交和指纹登录行为一致。

提醒只负责交互，不参与路由放行和权限判断，不能替代后端密码登录门禁。

### 6.3 安全设置页

在现有密码模块增加状态区域：

- 永不过期：不显示临期警告，可展示“密码长期有效”。
- 正常：展示密码过期时间。
- 临期：展示警告色提示、过期时间和剩余天数。
- 已过期：展示错误色提示“密码已过期，请立即修改”。

保留现有 `SecurityApi.updatePassword` 和 `password.tsx` 的提交逻辑。修改成功后继续调用 `authStore.fetchUserInfo()`，由服务端最新状态关闭临期或过期提示。

### 6.4 多语言

所有新增按钮、状态、弹窗标题和内容补充 `zh-CN`、`en-US` 文案；日期使用项目现有日期格式化能力，不在模板中拼接中英文句子。

## 7. 数据库与升级

- `mdc_user` 不新增字段，继续使用 `pw_expire_time`。
- `mdc_config` 新增 `PASSWORD_EXPIRE_REMIND_DAYS` 初始化数据。
- 全量初始化更新 `docs/sql/mdp.sql`。
- 新增 `docs/sql/20261123-password-expiry-policy.sql`，为存量环境提供可重复执行的配置初始化语句。
- 不批量更新现有用户的 `pw_expire_time`，避免管理员修改周期后出现不可预期的集中过期或续期。

## 8. 测试与验收

### 8.1 后端行为测试

- 过期时间等于当前时间时判定过期。
- `null` 和永不过期时间不拦截、不进入临期状态。
- 阈值内、阈值边界、阈值外、今天过期、提醒关闭分别返回正确状态。
- `PASSWORD_EXPIRE_NOT_ALLOW_LOGIN=false` 时过期密码可继续认证。
- 配置为 `true` 时过期密码返回 `-17`，且不创建会话或双因素票据。
- 错误密码即使账号已过期，仍返回统一密码错误，不返回 `-17`。
- 禁止配置读取失败时不静默放行。
- 手机、邮箱、社交、指纹登录不被密码过期直接拦截。
- 新增、个人修改、管理员重置和两类找回密码均按当前配置续期。
- 现有 `pwExpireTime` 为 `null` 的用户，四类密码变更场景均不覆盖该字段（豁免）；为 `LocalDateTime.MAX` 的用户重算为有期限。
- 新增用户与注册初始化仍按配置设置过期时间，不受豁免保护。
- 修改有效期配置后，既有用户过期时间不被批量改变。

### 8.2 前端测试

- 密码状态与剩余天数到提醒文案的映射，包括今天过期和永不过期。
- `-17` 弹窗及忘记密码跳转，不调用 `completeLogin`。
- 每次登录提醒一次，“稍后修改”不永久屏蔽。
- `EXPIRING` 与 `EXPIRED` 的立即修改入口进入正确安全设置页签。
- 修改密码刷新用户信息后提醒状态更新。
- 中英文关键文案齐全，类型检查和定向测试通过。

### 8.3 人工验收

1. 新建用户并验证初始过期时间。
2. 将测试用户过期时间调整到临期范围，分别验证密码、验证码、社交和指纹登录后的提醒。
3. 开启禁止登录，验证过期密码被拦截并可进入忘记密码；重置后可正常登录且过期时间续期。
4. 关闭禁止登录，验证过期密码仍可登录，但每次登录出现已过期提醒。
5. 修改有效期配置，确认老用户时间不变、新修改密码用户使用新周期。
6. 将提醒阈值设为 `0`，确认临期弹窗关闭、已过期允许登录的提醒仍保留。

## 9. 范围边界

本期不包含：

- 新增 `password_changed_at` 或批量追溯重算历史密码周期。
- 定时任务、邮件或短信主动推送临期通知。
- 过期后强制只允许访问修改密码页。
- 修改密码后的全端会话注销策略。
- 新的密码复杂度、历史密码重复校验规则。
