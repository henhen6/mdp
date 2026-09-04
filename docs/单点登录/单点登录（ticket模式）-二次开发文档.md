# 单点登录（ticket 模式） - 二次开发文档

> 面向读者：基于 MDP 进行二次开发的开发人员。阅读本文档后，您可以定位单点登录链路的问题、新增内置应用、理解服务端/客户端的定制点。

> ⚠️ 修改认证相关代码前，请注意安全影响：ticket 派发、`pushS` 校验、IP 白名单、签名校验均为安全关键路径，改动需评审。

## 1. 架构总览

MDP 后端（`workbench-web` 模块）在 SSO 体系中**同时承担两个角色**：

- **SSO 服务端**：`sso` 包下的 `SsoServerController` + `CustomSaSsoServerTemplate`。客户端注册信息存在 `mdo_app` 表（由 `AppFacade` 读取），第三方应用在【应用管理】创建后即可接入，**无需改服务端代码**；
- **SSO 客户端**：`SsoClientController`。MDP 的 3 个前端（web-workbench / web-console / web-open）本身也是 SSO 客户端，通过自研的 `sa-token-sso-client-starter` 支持"一个后端服务 + 多个前端应用"。

```
                    ┌ MDP 后端 (workbench-web) ─────────────────────────────┐
                    │                                                        │
   web-workbench ──>│  SsoServerController（服务端：派发ticket/pushS/注销）    │<── 3个内置前端
   统一登录页        │  CustomSaSsoServerTemplate（从 mdo_app 动态读取client）  │    均为客户端
   (认证中心)        │  SsoClientController（客户端：getSsoAuthUrl/换token/pushC）│
                    │  SaTokenSsoConfigure（装配定制Template）                │<── 第三方客户端
                    └────────────────────────────────────────────────────────┘
```

底层依赖 `mdp-base/md-sa-token` 下的定制版 sa-token SSO 模块（`sa-token-sso-server`、`sa-token-sso-client`、`sa-token-sso-client-starter`），相对官方版本的主要差异：所有客户端方法均增加 `clientId` 参数以支持多 client 路由。

## 2. 关键类索引

| 类 | 位置 | 职责 |
| --- | --- | --- |
| `SsoServerController` | `mdp-apps/md-workbench/workbench-web/.../sso/controller/SsoServerController.java` | 服务端接口：getRedirectUrl / signout / pushS / logout |
| `SsoClientController` | 同目录 `SsoClientController.java` | 客户端接口：getSsoAuthUrl / doLoginByTicket / pushC / signout / logout |
| `CustomSaSsoServerTemplate` | `.../sso/config/CustomSaSsoServerTemplate.java` | 服务端定制：client 注册信息从数据库读取 |
| `SaTokenSsoConfigure` | `.../sso/config/SaTokenSsoConfigure.java` | 装配：用 CustomSaSsoServerTemplate 替换默认 Template |
| `AuthController` | `.../controller/AuthController.java` | 认证中心的实际登录接口（账号/手机/邮箱） |
| `SaSsoClientTemplate` | `mdp-base/md-sa-token/sa-token-sso-client/...` | 客户端模板：`getClientConfig(clientId)` 多 client 路由 |
| `SaSsoServerTemplate` | `mdp-base/md-sa-token/sa-token-sso-server/...` | 服务端模板：buildRedirectUrl / checkTicket / createTicketAndSave |
| `SaSsoClientBeanRegister` | `mdp-base/md-sa-token/sa-token-sso-client-starter/...` | 注册 `ssoClientsConfigMap`（sso-clients 多前端配置） |
| 前端统一登录页 | `mdp-vben/apps/web-workbench/src/views/_core/sso/login.vue` + `data/login.tsx` | 认证中心登录页（服务端前端） |
| 前端客户端登录页 | `mdp-vben/apps/web-console/src/views/_core/authentication/login.vue` | 客户端中转页（web-open 同构） |

## 3. 服务端链路详解

### 3.1 getRedirectUrl（登录后构建重定向地址）

`SsoServerController.java:58`，POST `/anyUser/sso/getRedirectUrl`，入参 `LoginRedirectUrlDto`（`workbench-pojo/.../dto/LoginRedirectUrlDto.java`）：

| 字段 | 说明 |
| --- | --- |
| `authType` | 登录方式（CAPTCHA/USERNAME/EMAIL/PHONE），用于登录日志 |
| `client` | 目标应用 appKey |
| `mode` | 模式：`simple`=模式一；空/其他=模式二、三 |
| `redirect` | 客户端回调地址（需在应用 `ssoAllowUrl` 白名单内） |
| `deviceInfo` | 设备信息，用于登录日志 |

处理流程：

1. `StpUtil.isLogin()` 未登录返回 401（前端据此显示登录表单）；
2. `appFacade.getAppByAppKey(client)` 查应用：不存在返回"无效的AppId"；`state=false` 返回"应用已被禁用"；
3. `appFacade.checkAppByUserId` 校验当前用户是否有该应用权限，无权限返回"暂无权限登入此应用"；
4. 按 `mode` 分流：
   - 模式一（`SaSsoConsts.MODE_SIMPLE`）：仅 `checkRedirectUrl` 校验后**原样返回 redirect**（同域共享 cookie，不下发 ticket）；
   - 模式二/三：`SaSsoServerUtil.buildRedirectUrl(client, redirect, loginId, tokenValue)` 派发 ticket 并返回 `redirect?ticket=xxx`；
5. 发布 `LoginEvent` 记录登录日志（userId、appKey、跳转地址、tokenInfo）。

`buildRedirectUrl` 内部（`SaSsoServerTemplate.java:372`）：先 `deleteTicket` 删除该用户旧 ticket（同一账号同 client 只保留最新 ticket）→ `createTicketAndSave` 创建新 ticket 存 Redis → `encodeBackParam` 对 redirect 中的 back 参数做 URL 编码（修复参数丢失 bug）。

### 3.2 pushS（接收客户端请求）

`SsoServerController.java:148`，GET `/anyUser/sso/pushS`。客户端后台在 `checkTicket` / 注销时按消息类型调用此接口。MDP 在官方逻辑外增加了**IP 白名单校验**：

1. `IpUtil.normalizeLoopback` 归一化回环地址（127.0.0.1/内网 IP）；
2. `appFacade.getAppByAppKey(client)` 取应用 `allowIp`，非空则逐条 `IpUtil.matchIp` 匹配，不匹配返回"IP 不在允许访问的白名单中"；
3. `SaSsoServerProcessor.getInstance().ssoPushS()` 处理消息（校验 ticket：`timestamp + sign` 签名校验后比对 Redis 中 ticket，返回 loginId、remainTokenTimeout）。

### 3.3 CustomSaSsoServerTemplate（客户端注册改为数据库驱动）

`CustomSaSsoServerTemplate.java` 重写两个方法：

- `getClient(clientSn)`（`CustomSaSsoServerTemplate.java:43`）：`appFacade.getAppByAppKey` 查 `mdo_app`，转换为 `SaSsoClientModel`；应用不存在抛"应用无效"，`state=false` 抛"该应用已被封禁"。字段映射：`appKey→client`、`appSecret→secretKey`、`ssoAllowUrl→allowUrl`、`ssoPush→isPush/isSlo`、`ssoPushUrl→pushUrl`；
- `getNeedPushClients()`（`CustomSaSsoServerTemplate.java:61`）：`appFacade.listNeedPushApp` 查询所有开启 `ssoPush` 的应用，全端注销时逐个推送。

由 `SaTokenSsoConfigure.java:21` 装配：`SaSsoServerProcessor.getInstance().setSsoServerTemplate(customSaSsoServerTemplate)`，同时将未登录视图设为抛异常（登录引导完全由前端控制）。

## 4. 客户端链路详解

### 4.1 三个内置前端的客户端接入

`SsoClientController`（`workbench-web/.../sso/controller/SsoClientController.java`）：

| 接口 | 位置 | 说明 |
| --- | --- | --- |
| GET `/anyUser/client/getSsoAuthUrl` | `:45` | 构建认证中心地址：`authUrl?client={clientId}&redirect={clientLoginUrl}`；`ssoUrl=true` 时直接返回认证地址（注销后重新登录用） |
| GET `/anyUser/client/doLoginByTicket` | `:93` | `checkTicket(clientId, ticket)` 校验 → `StpUtil.login(loginId, remainTokenTimeout, deviceId)` 生成本系统会话 → 返回 token |
| GET `/anyUser/client/pushC` | `:147` | 接收服务端推送（logoutCall 单点注销） |
| `/anyUser/client/signout` / `/logout` | `:110` / `:129` | 客户端发起全端退出 / 退出当前应用 |

与若依参考实现的差异：MDP 的方法均带 `clientId` 参数（如 `checkTicket(clientId, ticket)`、`ssoPushC(clientId)`），因为**一个后端要服务 3 个前端应用**，需按 clientId 路由到各自配置。

### 4.2 sso-clients 多前端增强（自研）

sa-token 官方一个后端只有一份 `sso-client` 配置。MDP 在 `mdp-base/md-sa-token/sa-token-sso-client-starter` 中扩展了 `sa-token.sso-clients` 配置：

```yaml
sa-token:
  sso-client:      # 全局默认（client: '*'）
    client: '*'
    serverUrl: http://localhost:23455
    authUrl: 'http://localhost:7700/#/auth/login'
    ...
  sso-clients:     # mdp 自研：每个内置前端一份配置
    web-workbench: { client: 'web-workbench', secretKey: '...', ... }
    web-console:   { client: 'web-console',   secretKey: '...', ... }
    web-open:      { client: 'web-open',        secretKey: '...', ... }
```

`SaSsoClientTemplate#getClientConfig(clientId)`（`sa-token-sso-client/.../SaSsoClientTemplate.java:253`）按 clientId 从 `SaSsoClientManager.getClientConfigMap()` 取对应配置，取不到回落全局 `sso-client` 配置。**内置应用只在 yml 中配置（3 个），第三方应用只在数据库（mdo_app）中注册**，两套来源在 `CustomSaSsoServerTemplate` / `getClientConfig` 中各管一边。

### 4.3 前端流程

**认证中心登录页**（`mdp-vben/apps/web-workbench/src/views/_core/sso/`）：

- `data/login.tsx:66` `useLogin()` 从路由 query 读取 `client`、`redirect`、`mode` 存入 `ssoParam`；
- `onMounted` 触发 `ssoAutoLogin`（`login.tsx:27`）：已携带 client+redirect 时直接调 `getRedirectUrl`——若当前浏览器在认证中心已有会话，**免输密码自动跳回客户端**；401（未登录）则显示登录表单，-10（应用禁用/无权限）显示错误文案；
- 登录成功后 `handleSuccess`（`login.tsx:51`）：有 client+redirect 调 `getRedirectUrl` 跳回客户端；无则进入工作台首页（普通登录场景）。

**客户端中转页**（以 web-console 为例，`apps/web-console/src/views/_core/authentication/login.vue:28`）：

```
onMounted → URL 有 ticket ? handleLoginByTicket(ticket) : goSsoAuthUrl()
  handleLoginByTicket：doLoginByTicket → accessStore.setAccessToken → 跳转 back 参数指定地址
    失败(30004/30005)：显示"重新登录"按钮 → gotoLogin 重新走 goSsoAuthUrl
  goSsoAuthUrl：getSsoAuthUrl(encodeURIComponent(location.href), VITE_GLOB_APP_KEY) → 重定向
```

`VITE_GLOB_APP_KEY` 即各前端的环境变量 appKey（web-workbench / web-console / web-open）。

**应用跳转入口**：工作台"我的应用"（`web-workbench/src/views/workbench/user/application/index.vue:74`）以 `mode: 'ticket'` 调 `getRedirectUrl`，携带应用的 `ssoAutoLoginUrl` 作为 redirect，新窗口打开第三方系统。

## 5. 数据模型（mdo_app）

SSO 相关字段（`open-pojo/.../vo/admin/AppVo.java`）：

| 字段 | 行号 | 用途 |
| --- | --- | --- |
| `appKey` / `appSecret` | - | 客户端标识 / 秘钥（getClient、签名校验） |
| `state` | - | 应用禁用后拒绝授权（getClient 抛异常） |
| `ssoAllowUrl` | `:161` | 允许重定向的 URL 白名单（checkRedirectUrl） |
| `ssoPush` / `ssoPushUrl` | `:148` / `:154` | 是否接收注销推送 / 推送地址 |
| `allowIp` | `:142` | 调用服务端接口的 IP 白名单（pushS 校验） |
| `ssoAutoLoginUrl` | `:123` | 我的应用跳转的自动登录地址 |

## 6. 扩展点

| 需求 | 做法 |
| --- | --- |
| 新增第三方系统接入 | 控制台【应用管理】创建应用（填 ssoAllowUrl、ssoPushUrl、allowIp 等），零代码 |
| 新增 MDP 内置前端应用 | `application.yml` 的 `sa-token.sso-clients` 追加一份配置（参考注释，仅内置应用需配置） |
| 调整 ticket 有效期 | `sa-token.sso-server.ticket-timeout`（默认 300 秒） |
| 切换模式三 | `sa-token.sso-client.is-http: true`（客户端后台通过 http 校验，替代同域重定向） |
| 调整登录页未登录行为 | `SaTokenSsoConfigure.java:25` 的 `setNotLoginView` |
| 登录日志 | 监听 `LoginEvent`（getRedirectUrl 成功时发布，含 tokenInfo 快照——注意其中含 token，日志落库需评估脱敏） |

## 7. 问题定位

### 7.1 日志关键字

| 日志 | 位置 | 含义 |
| --- | --- | --- |
| `重定向地址: {}` | `SsoServerController.getRedirectUrl` | ticket 派发成功，打印最终 redirect?ticket=xxx |
| `接收到客户端:[{}]， 应用:[{}] 的请求` | `SsoServerController.pushS` | 客户端后台调用（校验 ticket / 注销） |
| `客户端IP:[{}]不在白名单中` | `SsoServerController.pushS` | allowIp 校验失败 |
| `获取SSO服务端登录地址: ssoUrl={} clientLoginUrl={}, clientId={}` | `SsoClientController.getSsoAuthUrl` | 客户端构建认证地址 |
| `signout` / `pushC` 异常堆栈 | 两个 Controller 的 catch | 注销 / 推送处理失败 |

### 7.2 常见错误码

| 错误 | 含义 |
| --- | --- |
| 401 | 认证中心无会话（前端显示登录表单） |
| -10 | 操作异常：应用无效 / 已禁用 / 用户无权限（BizException） |
| 30004 | ticket 无效（不存在或已过期） |
| 30005 | 模式三下客户端调用 pushS 校验 ticket 失败 |

### 7.3 排查路径

1. **登录跳转失败**：确认 `redirect` 在 `mdo_app.sso_allow_url` 白名单内 → 看是否被 `checkAppByUserId` 权限拦截 → 查 401/-10 的具体 msg；
2. **ticket 校验失败**：确认 ticket 一次性（旧 ticket 已被删除）、`secret-key` 与 `mdo_app.app_secret` 一致、客户端出口 IP 在 `allow_ip` 白名单内、服务器时间偏差未超签名允许范围（timestamp 校验）；
3. **单点注销不生效**：确认 `ssoPush=true`、`ssoPushUrl` 可达（服务端 `getNeedPushClients` 只返回开启推送的应用）、客户端 `pushC` 未被鉴权拦截（anyUser 匿名路径）。

## 8. 注意事项

- `CustomSaSsoServerTemplate` 同时被服务端（`getClient`）与注销推送（`getNeedPushClients`）使用，修改字段映射时注意 `isPush` 与 `isSlo` 目前共用 `ssoPush` 字段——若两者需要独立控制需扩展数据模型；
- `mdp-base/md-sa-token` 下的 sa-token 模块是**定制副本**（非官方依赖），升级 sa-token 版本时需手动合并官方改动；
- `getRedirectUrl` 的 `LoginEvent` 携带完整 tokenInfo（含 token 值），排查日志时注意不要将 token 明文输出到外部渠道；
- OAuth2 单点登录（`workbench-web/.../oauth2/` 包 + `/oauth2/authorize` 前端页）是独立链路，与应用管理的 `oauth2AllowRedirectUris`、`oauth2AllowGrantTypes` 字段相关，见 OAuth2 独立文档。
