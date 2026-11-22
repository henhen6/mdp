# 指纹登录功能设计

- 日期：2026-11-13
- 状态：已确认
- 后端仓库：mdp（本仓库）
- 前端仓库：mdp-vben（`apps/web-workbench`）
- 参考实现：uni-auth（`~/workspace/pj/uni-auth/ua-server/ua-center`，同作者已验证的 WebAuthn 实现）

## 1. 背景与目标

在 MDP 工作台实现基于 WebAuthn 的指纹免密登录，替换安全设置页的"人脸认证"占位模块。

需求场景与 WebAuthn resident key（可发现凭证）模式的对应关系：

| 需求 | 实现机制 |
|---|---|
| 一个用户可在不同电脑绑定指纹 | 一个 userId 对应多条 credential 记录 |
| 同一台电脑可给不同账号绑定指纹 | 每条凭证独立公钥对；登录时不带 username 发起认证，由系统认证器弹出账号选择器（显示姓名+用户名） |
| 绑定/解绑指纹 | 凭证表 CRUD（逻辑删除） |
| 指纹登录 | WebAuthn assertion 校验通过后走现有登录签发链路 |

## 2. 总体流程

```
绑定流程（安全设置页）                       登录流程（登录页）
┌─────────────────────┐                  ┌──────────────────────┐
│ 1. 验证登录密码       │                  │ 1. GET /anyUser/     │
│    → 一次性票据(5min) │                  │    fingerprint/      │
│ 2. POST registration │                  │    login/options     │
│    /options（消费票据）│                  │    → 挑战（无 username）│
│ 3. 前端 navigator.   │                  │ 2. 前端 @simplewebauthn│
│    credentials.create│                  │    /browser 的       │
│    → 系统指纹采集     │                  │    startAuthentication│
│ 4. POST registration │                  │    → 系统弹出账号选择器 │
│    /verify 保存公钥   │                  │    （姓名+用户名）     │
└─────────────────────┘                  │ 3. POST login/verify │
                                         │    → 校验签名 → 签发token│
                                         └──────────────────────┘
```

## 3. 数据库

新表 `mdw_fingerprint_credential`（`mdw_` 前缀 = workbench）：

```sql
CREATE TABLE `mdw_fingerprint_credential` (
  `id`            bigint       NOT NULL COMMENT '主键（雪花ID）',
  `user_id`       bigint       NOT NULL COMMENT '用户ID',
  `credential_id` varchar(512) NOT NULL COMMENT 'WebAuthn凭证ID(base64url)',
  `public_key`    text         NOT NULL COMMENT 'COSE公钥(base64)',
  `sign_count`    bigint       NOT NULL DEFAULT '0' COMMENT '签名计数器(防克隆)',
  `aaguid`        varchar(36)  DEFAULT NULL COMMENT '认证器AAGUID',
  `device_id`     varchar(64)  DEFAULT NULL COMMENT '前端localStorage设备ID',
  `device_name`   varchar(64)  DEFAULT NULL COMMENT '设备名称',
  `platform`      varchar(32)  DEFAULT NULL COMMENT '平台 win32/darwin/linux',
  `user_agent`    varchar(500) DEFAULT NULL COMMENT '登记时User-Agent',
  `registered_at` datetime     DEFAULT NULL COMMENT '登记时间',
  `last_used_at`  datetime     DEFAULT NULL COMMENT '最后使用时间',
  `last_login_ip` varchar(64)  DEFAULT NULL COMMENT '最后登录IP',
  `created_at`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by`    bigint       DEFAULT NULL,
  `updated_at`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updated_by`    bigint       DEFAULT NULL,
  `deleted_at`    bigint       NOT NULL DEFAULT '0' COMMENT '删除标识',
  `deleted_by`    bigint       DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_credential_id` (`credential_id`, `deleted_at`),
  KEY `idx_user_id` (`user_id`)
) COMMENT='指纹登录凭证';
```

- 唯一键含 `deleted_at`：解绑后同一设备可重新绑定。
- SQL 落 `docs/sql/20261113-security-fingerprint-resource-api.sql`，含接口权限资源注册（格式参考 `docs/sql/20261112-security-social-resource-api.sql`）。

## 4. 后端设计（md-workbench）

### 4.1 新增文件

| 模块 | 文件 |
|---|---|
| workbench-pojo | `entity/base/FingerprintCredentialBase`（仅表字段，契约同 uni-auth `FingerprintCredentialBase`）、`entity/FingerprintCredential` |
| workbench-pojo | DTO：`FingerprintRegisterVerifyDto`（challengeId、responseJson、deviceId、deviceName、platform）、`FingerprintRenameDto`（name） |
| workbench-pojo | VO：`FingerprintRegisterOptionsVo`（challengeId、optionsJson）、`FingerprintLoginOptionsVo`（challengeId、optionsJson）、`FingerprintCredentialVo`（id、deviceName、platform、aaguid、registeredAt、lastUsedAt、lastLoginIp） |
| md-cache-key | `FingerprintChallengeCacheKeyBuilder`、`FingerprintPwdVerifiedCacheKeyBuilder`（均 TTL 5 分钟） |
| workbench-service | `config/FingerprintProperties`（`mdp.fingerprint.*`）、`config/FingerprintWebAuthnConfig`（RelyingParty bean） |
| workbench-service | `service/impl/FingerprintCredentialRepositoryImpl`（Yubico `CredentialRepository` 适配，凭 `SsoUserService.getByUsername/getById` 反查） |
| workbench-service | `util/FingerprintUserHandles`（userHandle = userId 字符串的 UTF-8 字节） |
| workbench-service | `service/FingerprintService` + `impl/FingerprintServiceImpl` |
| workbench-web | `controller/FingerprintController`（`/fingerprint/**`，需登录态）、`controller/FingerprintLoginController`（`/anyUser/fingerprint/**`，免登录） |

### 4.2 修改文件

- `AuthTypeEnum`：新增 `FINGERPRINT("12", "指纹登录")`。
- workbench-service `pom.xml`：新增 `com.yubico:webauthn-server-core`。

### 4.3 FingerprintService 接口（8 个方法）

| 方法 | 说明 |
|---|---|
| `checkPassword(password)` | 构造 `LoginDto(authType=USERNAME)` 复用 `UsernameLoginStrategyImpl.checkUserPassword`（与账号密码登录完全一致的校验与错误处理），通过后发一次性票据（5 分钟） |
| `registrationOptions()` | 消费一次性票据 → `relyingParty.startRegistration`（`residentKey=REQUIRED`、`userVerification=REQUIRED`，自动 excludeCredentials）→ 挑战存 Redis，返回 `{challengeId, optionsJson}` |
| `verifyRegistration(dto)` | 校验 WebAuthn 注册响应 → 保存凭证（credentialId、公钥、signCount、aaguid、设备信息） |
| `listMine()` | 当前用户凭证列表 |
| `rename(id, dto)` | 改设备名（校验归属） |
| `delete(id)` | 逻辑删除（校验归属） |
| `loginOptions()` | 免登录，`relyingParty.startAssertion`（不带 username，UV REQUIRED）→ 挑战（完整 AssertionRequest JSON）存 Redis |
| `loginVerify(dto)` | 免登录，恢复 AssertionRequest → `finishAssertion` → 按 userHandle 还原 userId → 校验用户可用性 → 复用 `LoginSessionService.loginAuthenticated(user, FINGERPRINT, username, deviceId, deviceInfo, "PC")` 签发 token；更新 signCount/lastUsedAt/lastLoginIp |

### 4.4 配置

`mdp.fingerprint.*`：
- `enabled`：是否启用（默认 false）
- `rpId`：RP ID，必须等于登录页部署域名（或其父域）
- `rpName`：RP 显示名称
- `origins`：允许的前端 origin 集合（严格匹配）
- `maxCredentialsPerUser`：单用户最大凭证数（默认 10）

`enabled=true` 时启动期强制校验 rpId/origins 非空；未启用时用占位值保证 RelyingParty bean 可创建。配置项具体值由用户在环境配置中填写，不落仓库。

## 5. 前端设计（mdp-vben / apps/web-workbench）

### 5.1 安全设置页改造（mdp-vben 为 Tabs 结构）

- `views/workbench/user/security/index.vue` 的 `tabItems`（key: password/username/phone/email/twoFactor/face/social）中，`face` tab 改为 `fingerprint` tab
- `data/face.tsx` → 重命名 `data/fingerprint.tsx`，从占位改造为指纹模块数据逻辑：
  - 凭证列表（设备名/平台/登记时间/最后使用时间）
  - 改名、解绑（确认区，仿 `social.vue` 的列表+行内按钮+Alert+确认区模式）
  - 「添加指纹」流程：密码验证弹窗 → `startRegistration({ optionsJSON })` → 系统指纹采集 → 提交 verify
  - `browserSupportsWebAuthn()` 检测不支持时显示降级提示
- `modules/face.vue` → 重命名 `modules/fingerprint.vue`，Card 结构沿用（`antdv-next`）
- 文案：`locales/langs/{zh-CN,en-US}/workbench/user/security.json` 的 `face` 节点改为 `fingerprint` 节点（"指纹登录"）

### 5.2 登录页入口

- `views/_core/sso/login.vue`：表单下方加「指纹登录」按钮 → `startAuthentication({ optionsJSON })`（系统弹出账号选择器）→ `login/verify` → 复用现有登录成功处理（存 token、跳转）
- 不支持 WebAuthn 或后端 `enabled=false` 时隐藏按钮

### 5.3 API 与依赖

- 依赖：`@simplewebauthn/browser`（web-workbench）
- API 封装：`api/workbench/user/security/` 下按现有 `SecurityConfig + SecurityApi` 模式新增指纹接口（含免登录的登录接口，走登录页请求客户端）

## 6. 安全设计

- ⚠️ 本功能涉及认证链路：新增免登录接口 `/anyUser/fingerprint/login/**`，仅暴露获取挑战与验证两个动作，不返回任何用户信息
- 挑战（含完整 AssertionRequest）存 Redis，取出即删（一次性消费，防重放），TTL 5 分钟
- 密码验证票据一次性消费
- 签名计数器（signCount）下降视为克隆告警，断言失败
- RP ID 与 origins 严格匹配，WebAuthn 层面拒绝跨站
- 越权操作（改/删他人凭证）走归属校验
- 用户禁用状态在登录签发前校验

## 7. 错误处理（fail fast）

所有失败场景抛具体 `BizException`，不吞异常：
- 密码错误 / 票据缺失："请先验证登录密码"
- 挑战过期："录入请求已过期，请重新录入" / "登录请求已过期，请重新验证"
- 凭证超限："指纹数量已达上限，请先删除部分设备"
- 重复录入/校验失败："指纹录入失败：该设备可能已录入过指纹，或验证未通过，请重试"
- 登录失败："指纹验证失败"；用户禁用："您已被禁用，请联系管理员开通账号！"
- 越权："无权操作他人指纹凭证"

## 8. 测试（TDD）

后端 `FingerprintServiceImplTest` 覆盖：
- 密码票据缺失时 registrationOptions 拒绝
- 挑战过期/一次性消费后二次使用拒绝
- 越权 rename/delete 拒绝
- 凭证数超限拒绝
- loginVerify 断言失败 / 用户不存在 / 用户禁用路径

前端跟随项目现状，不做新增自动化测试。

## 9. 验收标准

1. 同一用户在 A、B 两台电脑分别绑定指纹，均可登录
2. 同一台电脑为账号甲、乙分别绑定指纹，登录时系统账号选择器展示"姓名（用户名）"可区分选择
3. 解绑后无法再用该指纹登录；解绑后可重新绑定
4. 未验证密码直接调用 registration/options 被拒绝
5. 挑战重放被拒绝
6. 登录日志记录认证方式为"指纹登录"
