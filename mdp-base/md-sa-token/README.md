# md-sa-token 简介

基于 Sa-Token 源码（v1.45.0）进行改造的版本，并不是重复造轮子，主要是官方 sa-token 无法满足本项目需求，特此改造。

> ⚠️ 本模块是 sa-token 官方源码的**定制副本**（非官方依赖），升级 sa-token 版本时需手动合并官方改动，合并时注意保留下文列出的定制点。

## 1. 模块总览

```
md-sa-token
├── sa-token-sso-core              SSO 公共内核（服务端/客户端共用）
├── sa-token-sso-server            SSO 服务端（认证中心）
├── sa-token-sso-server-starter    SSO 服务端 Spring Boot 自动装配
├── sa-token-sso-client            SSO 客户端（被接入方）
├── sa-token-sso-client-starter    SSO 客户端 Spring Boot 自动装配（含多客户端增强）
├── sa-token-oauth2-client         OAuth2 客户端工具（不依赖 sa-token 其余模块）
└── sa-token-oauth2-client-starter OAuth2 客户端 Spring Boot 自动装配
```

依赖关系：

```
sa-token-sso-core ──> sa-token-core、sa-token-sign（官方 jar）
        ├──> sa-token-sso-server ──> sa-token-sso-server-starter
        └──> sa-token-sso-client ──> sa-token-sso-client-starter
sa-token-oauth2-client（仅依赖 sa-token-core）──> sa-token-oauth2-client-starter
```

> 注意：OAuth2 **服务端**不在本模块内，`workbench-web` 直接使用官方 `cn.dev33:sa-token-oauth2`（配合 `OAuth2DataLoaderImpl` 从数据库加载 client 信息）。本模块只改造了 OAuth2 的**客户端**部分。

## 2. 各子模块说明

### 2.1 sa-token-sso-core —— SSO 公共内核

SSO 服务端与客户端共用的底层模型，无 Spring 依赖：

| 内容 | 说明 |
| --- | --- |
| `SaSsoTemplate` | 模板基类（消息分发 `handleMessage`、参数名/API 名、StpLogic 持有） |
| `SaSsoMessage` / `SaSsoMessageHolder` | 消息模型与消息处理器容器（checkTicket、signout、logoutCall 等消息类型路由） |
| `SaCheckTicketResult` / `TicketModel` | ticket 校验结果、ticket 数据模型 |
| `SaSsoClientModel` | 服务端视角的"客户端注册信息"（client、secretKey、allowUrl、pushUrl 等） |
| `SaSsoServerStrategy` / `SaSsoClientStrategy` | 双端可替换策略（sendRequest、doLoginHandle、notLoginView 等 function） |
| `ParamName` / `ApiName` / `SaSsoConsts` | 参数名、接口路径、常量 |
| `SaSsoErrorCode` / `SaSsoException` | 错误码（30001~30008）与异常 |

### 2.2 sa-token-sso-server —— SSO 服务端

认证中心实现，核心类：

- `SaSsoServerManager`：服务端配置总控（`sa-token.sso-server` 前缀），含 `is-check-sign=false` 的安全警告输出；
- `SaSsoServerProcessor`：服务端请求处理器（ssoAuth 登录地址、ssoSignout、ssoPushS 消息推送路由）；
- `SaSsoServerTemplate`：服务端模板（buildRedirectUrl 派发 ticket、createTicketAndSave、checkRedirectUrl、消息推送 handle）；
- 消息处理器：`SaSsoMessageCheckTicketHandle`（校验 ticket）、`SaSsoMessageSignoutHandle`（单点注销，遍历通知所有 client）。

### 2.3 sa-token-sso-server-starter —— SSO 服务端自动装配

Spring Boot 入口，通过 `META-INF/spring/...AutoConfiguration.imports` 注册：

- `SaSsoServerBeanRegister`：注册 `SaSsoServerConfig`（绑定 `sa-token.sso-server`）与 `SaSsoServerTemplate`；
- `SaSsoServerBeanInject`：将 Spring Bean 注入 `SaSsoServerManager` / `SaSsoServerProcessor` 静态容器。

### 2.4 sa-token-sso-client —— SSO 客户端（含核心改造）

被接入方实现。**本模块是改造重点**：所有核心方法均增加了 `clientId` 参数，支持一个后端服务同时作为多个 SSO 客户端：

| 类 | 核心方法（均带 clientId） | 说明 |
| --- | --- | --- |
| `SaSsoClientManager` | `getClientConfig()` / `getClientConfigMap()` | 配置总控：默认配置 + 多客户端配置 Map（定制） |
| `SaSsoClientTemplate` | `buildServerAuthUrl(clientId, ...)`、`buildCheckTicketMessage`、`buildSignoutMessage`、`pushMessage(clientId, ...)`、`getClientConfig(clientId)` | 按 clientId 路由配置；secretKey 优先级：SSO 配置 > sign 模块全局（定制） |
| `SaSsoClientProcessor` | `checkTicket(clientId, ticket)`、`ssoLogout(clientId)`、`ssoPushC(clientId)`、`dister(clientId)` | 请求处理器（定制） |

### 2.5 sa-token-sso-client-starter —— SSO 客户端自动装配（多客户端增强）

- `SaSsoClientBeanRegister`：注册默认配置 Bean（`sa-token.sso-client`）与 **`ssoClientsConfigMap`**（绑定 `sa-token.sso-clients`，定制新增）；
- `SaSsoClientBeanInject`：`@PostConstruct` 中按固定顺序将两份配置注入 `SaSsoClientManager`，并将 Template 注入 Processor。

### 2.6 sa-token-oauth2-client —— OAuth2 客户端工具

供接入 MDP OAuth2 的第三方客户端使用，**仅依赖 sa-token-core**，最小化引入：

| 类 | 说明 |
| --- | --- |
| `Oauth2ClientConfig` | 客户端配置（clientId、clientSecret、serverUrl 及 authorize/token/refresh/revoke/userinfo/client_token 六个端点地址拼接） |
| `SaOauth2ClientTemplate` | `buildServerAuthorizeUrl`：构建授权地址（response_type=code + client_id + redirect_uri + scope + state） |
| `SaOauth2ClientProcessor` / `SaOauth2ClientManager` | 处理器与配置总控 |
| `ParamName` | OAuth2 参数名常量 |

### 2.7 sa-token-oauth2-client-starter —— OAuth2 客户端自动装配

注册 `Oauth2ClientConfig`（绑定 `sa-token.oauth2-client`）与 `SaOauth2ClientTemplate`。

## 3. 与原生 sa-token 的区别

| # | 差异点 | 原生 sa-token | 本定制版 |
| --- | --- | --- | --- |
| 0 | 代码风格 | 官方风格 | 全量改造为本项目编码规范（checkstyle） |
| 1 | 模块拆分 | `sa-token-sso` 单模块同时含服务端/客户端代码 | 拆分为 sso-core / sso-server / sso-client 三层，客户端系统可最小化引入 |
| 2 | 多客户端支持 | 一个后端只有一份 sso-client 配置，**不支持 1 个服务端对多个客户端**（官方认为多余） | 客户端核心方法全部增加 `clientId` 参数 + `sa-token.sso-clients` 配置 Map，按 clientId 路由 |
| 3 | starter 拆分 | 自动装配与核心代码耦合 | 独立的 `-starter` 模块，非 Spring 环境可不引入 |
| 4 | oauth2-client 独立 | oauth2 模块整体依赖较重 | 拆出轻量 oauth2-client（仅依赖 sa-token-core），供客户端最小化引入 |
| 5 | secretKey 优先级 | sign 模块全局配置 | SSO 配置的 secretKey 优先于 sign 模块全局配置（`SaSsoClientTemplate#getSignTemplate`） |
| 6 | 安全警告 | 无 | 配置 `is-check-sign=false` 时启动即输出 error 级警告（跳过签名校验仅限本地调试） |

**多客户端改造的业务背景**（对应关系）：

- 1 个单点登录服务端：`boot-server`（单体）或 `workbench-server`（微服务）；
- 多个单点登录客户端：`web-workbench`、`web-console`、`web-open` 三个前端应用共用同一个后端。

原生 sa-token 中一个后端只能绑定一个 client 标识，无法区分"当前请求来自哪个前端应用"，本改造通过 `clientId` 参数 + `sso-clients` 配置解决（详见应用层 `SsoClientController` 与 `CustomSaSsoServerTemplate`，客户端注册信息从 `mdo_app` 表动态加载）。

## 4. 使用场景

### 4.1 服务端（认证中心）

MDP 平台自身作为认证中心时使用。引入：

```xml
<dependency>
    <groupId>top.mddata.base</groupId>
    <artifactId>sa-token-sso-server-starter</artifactId>
</dependency>
```

配置前缀 `sa-token.sso-server`（ticket 有效期、全局秘钥等）。客户端注册信息通过重写 `SaSsoServerTemplate#getClient` 从数据库读取（参考应用层 `CustomSaSsoServerTemplate`）。

### 4.2 客户端（被接入方）

- **MDP 内部前端应用**（web-workbench 等）：引入 `sa-token-sso-client-starter`，在 `sa-token.sso-clients` 下为每个前端应用配置一份 client 信息；
- **第三方系统（前后端分离，如若依）**：引入 `sa-token-sso-client-starter`，只需配置 `sa-token.sso-client`（单客户端），配合 `getSsoAuthUrl` / `doLoginByTicket` / `pushC` 三个接口接入；
- **不想引入 sa-token 生态的客户端**：无需引入本模块，参照《单点登录（ticket 模式）- 说明文档》直接实现三个 HTTP 接口即可（本模块 client 端本质就是这几个 HTTP 调用的封装）。

### 4.3 OAuth2 客户端

需要以 OAuth2 协议接入 MDP 的第三方系统引入 `sa-token-oauth2-client(-starter)`，用 `SaOauth2ClientTemplate#buildServerAuthorizeUrl` 构建授权跳转地址。OAuth2 服务端使用官方 `cn.dev33:sa-token-oauth2`，不在本模块范围。

### 4.4 典型配置示例

```yaml
sa-token:
  sso-server:                # 服务端：ticket 派发与校验
    ticket-timeout: 300
    is-http: false
    allow-anon-client: false
    secret-key: 全局秘钥
  sso-client:                # 客户端：默认（单客户端/兜底）配置
    client: '*'
    secret-key: 应用秘钥
    server-url: http://认证中心地址
    auth-url: 'http://认证中心前端登录页'
    is-http: true            # 模式三：后台 http 校验 ticket
  sso-clients:               # 客户端：多客户端配置（定制功能）
    web-workbench: { client: 'web-workbench', secret-key: '...', ... }
    web-console:   { client: 'web-console',   secret-key: '...', ... }
    web-open:      { client: 'web-open',      secret-key: '...', ... }
```

## 5. 升级 sa-token 版本注意事项

1. 本模块为源码副本，官方发新版后需手动 diff 合并（重点：`SaSsoClientProcessor` / `SaSsoClientTemplate` 的 `clientId` 参数是定制签名，方法签名与官方不一致，不能直接覆盖）；
2. 合并后逐个核验第 3 节列出的定制点是否保留；
3. 客户端/服务端的 Bean 注入顺序由 starter 的 `@PostConstruct` 保证，调整 starter 时注意"先默认配置、后多客户端配置"的顺序；
4. 版本由 `mdp-parent` 的 `<sa-token.version>` 统一管理（当前 1.45.0）。

## 6. 相关文档

- 《单点登录（ticket 模式）- 说明文档 / 二次开发文档》：ticket 模式完整链路；
- 《单点登录（OAuth2 模式）- 说明文档 / 二次开发文档》：OAuth2 链路（服务端为官方 oauth2 模块 + MDP 应用层扩展）。
