# 单点登录（OAuth2 模式） - 二次开发文档

> 面向读者：基于 MDP 进行二次开发的开发人员。阅读本文档后，您可以定位 OAuth2 链路的问题、新增 scope、调整令牌策略、理解各扩展点。

> ⚠️ 修改认证相关代码前，请注意安全影响：code 派发、client_secret 校验、redirect_uri 白名单均为安全关键路径，改动需评审。

## 1. 架构总览

MDP OAuth2 服务端基于 **sa-token OAuth2 定制版**（`mdp-base/md-sa-token` 下的 oauth2 模块）实现，代码集中在 `workbench-web` 的 `oauth2` 包：

```
                    ┌ MDP 后端 (workbench-web) ──────────────────────────────┐
                    │                                                        │
   web-workbench    │  OAuth2ServerController   授权页路由：getRedirectUri   │
   /oauth2/authorize│        │                    getConfirmInfo / confirm   │
   (授权+登录页) ──>│        │                                                    │
                    │  OAuth2ResourceController  后台资源：token / refresh    │
   第三方后台 ──────>│        │                    revoke / client_token      │
                    │        │                    userinfo                  │
                    │        │                                                    │
                    │  OAuth2DataLoaderImpl       Client注册信息从数据库读取     │
                    │  Oauth2DataResolver         参数读取（表单/Basic 头）      │
                    │  3个ScopeHandler            openid/unionid/日志扩展      │
                    └────────────────────────────────────────────────────────┘
```

与 sa-token 官方 OAuth2 的主要差异：官方的 `/oauth2/authorize` 页面式跳转被拆为**前端授权页（authorize.vue）+ 后端 JSON 接口（getRedirectUri）**，以适配前后端分离架构；客户端信息由数据库（`mdo_app`）动态加载，而非 yml 静态配置。

`mdp-base/md-sa-token/sa-token-oauth2-client` 是配套的 OAuth2 客户端工具包（`SaOauth2ClientTemplate#buildServerAuthorizeUrl`），供客户端系统引入使用。

## 2. 关键类索引

| 类 | 位置 | 职责 |
| --- | --- | --- |
| `OAuth2ServerController` | `workbench-web/.../oauth2/controller/OAuth2ServerController.java` | 授权页接口：getRedirectUri / getConfirmInfo / confirm + 定制配置 |
| `OAuth2ResourceController` | 同目录 `OAuth2ResourceController.java` | 第三方后台接口：token / refresh / revoke / client_token / userinfo |
| `OAuth2DataLoaderImpl` | `.../oauth2/dataloader/OAuth2DataLoaderImpl.java` | `SaOAuth2DataLoader` 实现：数据库加载 SaClientModel、查询 openid |
| `Oauth2DataResolver` | `.../oauth2/data/Oauth2DataResolver.java` | 读取 client_id/secret（请求参数或 Authorization Basic 头） |
| `CustomOidcScopeHandler` | `.../oauth2/handler/CustomOidcScopeHandler.java` | OIDC idToken 扩展字段（昵称/头像/邮箱/手机号） |
| `UnionIdScopeHandler` | 同目录 | access_token 额外字段追加 unionid |
| `LoginFinallyScopeHandler` | 同目录 | 最终处理器：授权日志落库（mdo_oauth_log） |
| 前端授权页 | `mdp-vben/apps/web-workbench/src/views/_core/oauth2/authorize.vue` | 登录 + 自动分流（未登录/需确认/直接授权） |
| 前端确认页 | 同目录 `confirm-scope.vue` | 勾选 scope、确认/拒绝授权 |
| `SaOauth2ClientTemplate` | `mdp-base/md-sa-token/sa-token-oauth2-client/...` | 客户端工具：构建授权地址 |

## 3. 授权页链路详解

### 3.1 前端 authorize.vue（状态分流）

`authorize.vue:199` `onMounted` 即调 `oauth2AutoLogin`，按 `getRedirectUri` 返回的 code 分流：

| 返回 code | 分流 |
| --- | --- |
| 401（NOT_TOKEN / INVALID_TOKEN） | 显示登录表单（账号密码/手机验证码/邮箱验证码），登录成功后重新走授权 |
| 411 | 用户尚未对该应用授权过且应用未开自动确认 → 显示确认授权页（confirm-scope.vue） |
| 0（成功） | `location.href = data` 直接重定向回第三方（已授权过 + 自动确认，免确认秒回） |

授权参数直接取自路由 query（`client_id`、`response_type`、`redirect_uri`、`scope`、`state`、`nonce`），通过 `Oauth2Api.getRedirectUri`（`web-workbench/src/api/common/oauth2.ts:14`，`responseReturn: 'raw'` 以便读取 401/411 状态码）提交后端。

### 3.2 后端 getRedirectUri（8 步校验）

`OAuth2ServerController.java:79`，入参 `RedirectUriDto`（`@JsonNaming` 驼峰转下划线，`response_type`/`redirect_uri`）：

1. `checkAuthorizeResponseType`：校验 response_type（code/token）——**两级开关**：平台全局 `SaOAuth2ServerConfig`（enableAuthorizationCode / enableImplicit）+ 应用 `oauth2AllowGrantTypes`，任一未开启分别抛 30141/30142；
2. `getLoginIdAsLong`：未登录会抛异常（前端转 401 显示登录表单）；
3. 构建 `RequestAuthModel`；
4. `userAuthorizeClientCheck`：用户级授权前置检查（策略点，默认放行）；
5. `checkRedirectUri`：校验 redirect_uri 在应用 `oauth2AllowRedirectUris` 白名单内；
6. `checkContractScope`：校验申请的 scope 该应用已签约；
7. `isNeedCarefulConfirm`：该用户尚未授权过这些 scope 且应用未开 `isAutoConfirm` → 返回 `R.fail(411, "请手动确认授权")`；
8. 按 response_type 生成 code（`generateCode`）或 token（`generateAccessToken`），构建重定向地址返回。

### 3.3 确认授权（confirm）

`confirm-scope.vue` 展示应用信息 + scope 勾选列表（`getConfirmInfo` 返回 `ConfirmInfoVo`：appVo、scopeList、user、scopes），用户确认后调 `POST /oauth2/confirm`：

- `saveGrantScope` 保存用户对应用的授权记录（此后再授权不再弹确认页）；
- `buildRedirectUri=true` 时按授权类型生成 code/token 并返回最终重定向地址，前端 `location.href` 跳回第三方；
- 用户拒绝时前端直接重定向 `redirect_uri?error=access_denied`（`confirm-scope.vue:76`）。

### 3.4 定制策略（configOAuth2Server）

`OAuth2ServerController.java:296` 注入 `SaOAuth2ServerConfig` 时定制：

- `confirmView` / `notLoginView`：直接抛"暂无此功能"（页面引导全部由前端接管）；
- `doLoginHandle`：接 `AuthService.login`（授权页的账号密码登录）。

## 4. 后台接口链路（第三方后台调用）

`OAuth2ResourceController`：

| 接口 | 位置 | 要点 |
| --- | --- | --- |
| `POST /oauth2/token` | `:85` | `SaOAuth2Strategy.instance.grantTypeAuth.apply(request)` 按框架内置处理器分发（authorization_code / password） |
| `POST /oauth2/refresh` | `:100` | 手工校验 grant_type、全局开关、应用开关、clientSecretAndScope、refresh_token 归属 client 一致性后 `refreshAccessToken` |
| `POST /oauth2/revoke` | `:148` | 校验后 `revokeAccessToken` |
| `POST /oauth2/client_token` | `:182` | 凭证式：两级开关校验 → `checkContractScope` → `checkClientSecret` → `generateClientToken` |
| `POST /oauth2/userinfo` | `:62` | `checkAccessTokenScope(accessToken, "userinfo")` 校验 scope → 按 loginId 查用户（**置空 password/salt 后返回**） |

`Oauth2DataResolver.readClientIdAndSecret`（`Oauth2DataResolver.java:24`）：client_id/client_secret 优先取请求参数，缺失时从 `Authorization: Basic Base64(id:secret)` 头解析，均无则抛 30191。

这些接口均在鉴权白名单（`application.yml` 的 `mdp.web.ignore.anyUser`，微服务版在网关 `inner-gateway-server.yml` 同样放行），匿名访问，安全性由 client_secret / access_token 自身保证。

## 5. 数据加载与数据模型

### 5.1 OAuth2DataLoaderImpl（应用注册信息数据库化）

`OAuth2DataLoaderImpl.java:46` `getClientModel(clientId)`：按 appKey 查 `mdo_app`，映射为 `SaClientModel`：

| mdo_app 字段 | SaClientModel | 说明 |
| --- | --- | --- |
| `id`（转字符串） | clientId | 注意：model 的 clientId 是**数据库 id**，与入参 appKey 不同 |
| `appSecret` | clientSecret | |
| `oauth2AllowRedirectUris` | allowRedirectUris | 逗号分隔转数组 |
| 应用已签约的 scope 列表 | contractScopes | 由 `oauthScopeFacade.listByAppId` 查关联关系 |
| `oauth2AllowGrantTypes` | allowGrantTypes | 逗号分隔转数组 |
| `oauth2NewRefresh` | isNewRefresh | `-1` 时取全局配置 `ConfigKey.Open.APP_NEW_REFRESH`（默认 true） |
| `oauth2AccessTokenTimeout` 等 | 各 token 有效期 | `-1` 时取全局配置 `ConfigKey.Open.APP_*_TIMEOUT` |
| `oauth2IsConfirm` | isAutoConfirm | **高危：true 时跳过用户确认页** |

`getOpenid`（`:114`）：按 appKey + userId 查 `mdo_oauth_openid`。应用被禁用（state=false）时抛"该应用已被封禁，无法授权认证"。

### 5.2 相关表

| 表 | 说明 |
| --- | --- |
| `mdo_oauth_scope` | scope 字典（code/name/intro/confirm_prompt/level：1-公开 2-特殊），内置 `userinfo`、`openid`、`unionid` |
| `mdo_oauth_openid` | 用户×应用的 openid（un_openid 唯一索引） |
| `mdo_oauth_unionid` | 主体（应用创建人）×用户的 unionid（un_unionid 唯一索引） |
| `mdo_oauth_log` | 授权记录：token、有效期、grant_type、scopes、openid/unionid |

### 5.3 Scope 扩展处理器

scope 处理器在 access_token 生成时追加额外字段，均实现 `SaOAuth2ScopeHandlerInterface`（按 `getHandlerScope` 匹配）：

- `UnionIdScopeHandler`：scope 含 `unionid` 时，按（应用创建人=主体，userId）查 `mdo_oauth_unionid`，把 unionid 放入 `at.extraData`；
- `CustomOidcScopeHandler`：扩展 OIDC 的 idToken，追加 uid/nickname/avatar/email/phone；
- `LoginFinallyScopeHandler`（**始终最后执行**，scope 为 `_FINALLY_WORK_SCOPE`）：把本次授权写入 `mdo_oauth_log`，并预留消息通知 TODO（`LoginFinallyScopeHandler.java:71`）。

## 6. 扩展点

| 需求 | 做法 |
| --- | --- |
| 新增 scope | `mdo_oauth_scope` 加记录 → 管理端为应用签约该 scope →（需要附加数据时）新建类实现 `SaOAuth2ScopeHandlerInterface` 并注册 `@Component` |
| 新增授权模式开关 | 平台级：`SaOAuth2ServerConfig`（enableXxx）；应用级：`mdo_app.oauth2_allow_grant_types` |
| 调整全局 token 有效期 | 系统配置（`ConfigKey.Open.APP_*_TIMEOUT`）；应用级覆盖在应用管理中设置（-1 表示跟随全局） |
| userinfo 返回更多字段 | `OAuth2ResourceController#getUserinfo` + `SsoUserVo`（注意脱敏） |
| 授权成功发通知 | `LoginFinallyScopeHandler.workAccessToken` 中的 TODO（msgFacade.sendMessage） |
| 客户端构建授权地址 | 引入 `mdp-base/md-sa-token/sa-token-oauth2-client`，用 `SaOauth2ClientTemplate#buildServerAuthorizeUrl` |

## 7. 问题定位

### 7.1 常见错误码

| 错误码 | 含义 |
| --- | --- |
| 30125 | 无效 response_type |
| 30126 | 无效 grant_type（或全局/应用未开放） |
| 30141 / 30142 | 系统 / 应用未开放此授权模式 |
| 30111 | 无效 refresh_token |
| 30122 | refresh_token 与 client_id 不一致 |
| 30191 | 未提供 client 信息（参数和 Basic 头均缺失） |
| 411 | 需要用户手动确认授权（正常业务流程，非错误） |

### 7.2 排查路径

1. **授权跳转失败**：`getRedirectUri` 8 步校验逐步核对——response_type 两级开关 → redirect_uri 白名单 → scope 签约 → 应用状态；
2. **换 token 失败**：code 一次性/过期、redirect_uri 与授权时不一致、client_secret 错误（30191/30103）；
3. **userinfo 失败**：scope 未含 `userinfo`、access_token 过期/已回收；
4. **openid/unionid 未返回**：确认 scope 申请了对应项、应用已签约该 scope、`mdo_oauth_openid`/`mdo_oauth_unionid` 有记录；
5. **对账**：`mdo_oauth_log` 有每次授权的完整记录（token、grant_type、scopes、openid/unionid）。

### 7.3 日志

所有 OAuth2 接口均标注 `@RequestLog`（如"OAuth2获取Token"），可在操作日志/审计中按模块名检索。`OAuth2DataLoaderImpl` 与 `UnionIdScopeHandler` 查询失败时会输出 `log.warn("查询openid失败/应用不存在")`。

## 8. 注意事项

- `SaClientModel.clientId` 存的是 `mdo_app.id`（数字字符串）而非 appKey，排查 token 数据（如 AccessTokenModel.clientId）时注意区分两个标识；
- `oauth2IsConfirm=true`（自动确认授权）是高危配置，前端确认页也会被跳过，二次开发时禁止默认开启；
- `userinfo` 接口已置空 password/salt，新增返回字段时保持同样谨慎，敏感字段一律不出网；
- `md-sa-token` 下的 oauth2 模块是定制副本，升级 sa-token 版本需手动合并官方改动；
- `LoginFinallyScopeHandler#getGrantType` 通过请求路径/参数推断授权类型，新增自定义端点时需同步更新该方法的判断逻辑。
