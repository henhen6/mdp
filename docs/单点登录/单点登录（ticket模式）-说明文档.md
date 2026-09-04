# 单点登录（ticket 模式） - 说明文档

> 面向读者：需要接入 MDP 统一认证的第三方系统开发者（如若依、自研系统）。阅读本文档后，您可以了解 MDP 单点登录的整体流程、需要在您的系统实现哪些接口和页面、如何配置对接参数。

MDP 平台支持两种单点登录方式：

| 方式 | 核心机制 | 适用场景 |
| --- | --- | --- |
| **ticket 模式**（本文档） | 登录成功后 MDP 派发一次性 `ticket`，客户端凭 `ticket` 换取用户 id，生成本系统会话 | 前后端分离的 Web 系统 |
| OAuth2 模式（另见独立文档） | 标准授权码模式 | 需要 OAuth2 生态兼容的系统 |

## 1. 角色说明

MDP 单点登录基于 Sa-Token SSO 模块（模式二/模式三）实现：

- **SSO 服务端（认证中心）**：MDP 平台。统一登录页由 `web-workbench` 前端承担（`mdp-vben/apps/web-workbench`），后端接口在 `workbench-web` 模块（`SsoServerController`）。
- **SSO 客户端**：您的系统。需要实现 3 个后端接口 + 1 个前端中转页。

```
                 ┌ MDP 平台（认证中心）─────────────────────────────┐
                 │  web-workbench 统一登录页  workbench 后端         │
                 │  (登录表单/派发ticket)      (SsoServerController) │
                 └───────────────△──────────────△────────────────┘
                                 │ ticket         │ pushS：校验ticket
                 ┌───────────────┴───────────────┴────────────────┐
                 │              您的系统（SSO 客户端）               │
                 │   前端中转页  +  客户端后端(SsoClientController) │
                 └────────────────────────────────────────────────┘
```

## 2. 登录流程总览（模式二/三）

```
您的系统前端            您的系统后端              MDP 统一登录页            MDP 服务端后端
    │                      │                       │                        │
    │ ①访问页面，无会话      │                       │                        │
    │ ②getSsoAuthUrl ────> │                       │                        │
    │ <── 认证中心地址 ───── │                       │                        │
    │ ③浏览器重定向 ──────────────────────────────> │                        │
    │                      │        ④已有MDP会话？ │                        │
    │                      │          是│  否：显示登录表单，登录成功          │
    │ ⑤getRedirectUrl ──────────────────────────────────────────────────> │
    │ <──── 返回 redirect?ticket=xxx（ticket 一次性，默认 5 分钟有效）────── │
    │ <─────────────────────│                       │                        │
    │ ⑥浏览器带 ticket 重定向回您的登录页             │                        │
    │ ⑦doLoginByTicket ──> │ ⑧checkTicket(后台调用 pushS) ────────────────> │
    │ <── 本系统 token ──── │ <── userId（loginId）── │                        │
    │ ⑨保存 token，跳回原页面 │                      │                        │
```

关键点：

- **ticket 是一次性的**：默认 5 分钟有效（服务端配置 `sa-token.sso-server.ticket-timeout=300`），且使用一次后即销毁；同一账号重复登录时服务端会先删除旧 ticket 再派发新 ticket；
- **MDP 不会把用户密码告诉您**，您拿到的是 `userId`（MDP 用户 id），由您自行映射/创建本系统的账号；
- **登录方式**（账号密码 / 手机验证码 / 邮箱验证码）由认证中心的登录页决定，您的系统无需关心。

## 3. 接入前准备

### 3.1 在 MDP 应用管理中创建应用

联系 MDP 管理员（或在开发者平台自助）创建应用后，您将获得并配置以下信息：

| 参数 | 来源 | 说明 |
| --- | --- | --- |
| `appKey`（应用ID） | 系统生成 | 客户端唯一标识，即下文配置中的 `client` |
| `appSecret`（应用秘钥） | 系统生成 | 客户端与 MDP 后端通信时接口调用秘钥 |
| `ssoAllowUrl` | 您配置 | **允许授权的重定向地址白名单**，多个用逗号分隔。`redirect` 参数必须在白名单内，否则拒绝重定向 |
| `ssoPushUrl` | 您配置 | 您系统接收 MDP 推送消息的后端地址（如 `http://your-domain/anyUser/client/pushC`），用于单点注销 |
| `ssoPush` | 您配置 | 是否接收消息推送。需要单点注销请开启 |
| `allowIp` | 您配置 | 允许调用 MDP 服务端接口的 IP 白名单。内置应用填内网 IP，第三方应用填您的服务器**外网 IP** |

> 安全提示：`appSecret` 请保存在您自己的配置文件或配置中心中妥善保管，防止泄露。

## 4. 您的系统需要实现的内容

以下代码参考自若依的接入实现（`RuoYi-Vue/ruoyi-admin/.../SsoClientController.java`、`ruoyi-ui/src/views/login_sso.vue`）。

### 4.1 后端：3 个接口

#### 接口一：获取认证中心登录地址

```
GET /anyUser/client/getSsoAuthUrl?clientLoginUrl={当前页面地址}
```

检测到用户未登录（无本系统 token，URL 上也没有 ticket）时，前端先调用此接口，然后重定向到返回的地址。

```java
@GetMapping("/anyUser/client/getSsoAuthUrl")
public AjaxResult getSsoAuthUrl(String clientLoginUrl) {
    // 拼接：认证中心登录页 + client + redirect=clientLoginUrl
    String serverAuthUrl = SaSsoClientUtil.buildServerAuthUrl(clientLoginUrl, "");
    return AjaxResult.success("操作成功", serverAuthUrl);
}
```

#### 接口二：根据 ticket 登录（核心）

```
GET /anyUser/client/doLoginByTicket?ticket={ticket}
```

浏览器从认证中心带 ticket 重定向回来后，前端调用此接口换取本系统的 token：

```java
@GetMapping("/anyUser/client/doLoginByTicket")
public AjaxResult doLoginByTicket(String ticket) {
    // 校验 ticket（内部会通过 http 请求调用 MDP 服务端的 pushS 接口完成校验）
    // 校验失败会直接抛异常（错误码 30004：ticket 无效；30005：校验失败）
    SaCheckTicketResult ctr = SaSsoClientProcessor.instance.checkTicket(ticket);

    // 校验成功：ctr.loginId 即 MDP 的用户id，根据它查询/创建本系统用户，生成本系统会话
    String token = sysLoginService.login(Convert.toLong(ctr.loginId()));
    return AjaxResult.success("操作成功", token);
}
```

`SaCheckTicketResult` 主要字段：

| 字段 | 说明 |
| --- | --- |
| `loginId` | MDP 用户 id（即您换取到的用户标识） |
| `remainTokenTimeout` | MDP 会话剩余有效时间（秒），可用于同步设置您本系统会话时长 |
| `clientId` | 客户端标识（即 appKey） |

#### 接口三：接收服务端推送（单点注销必需）

```
GET /anyUser/client/pushC
```

MDP 服务端执行"全端退出"时，会向所有开启了 `ssoPush` 的应用推送注销消息，您的系统在此接口中销毁本系统会话：

```java
@GetMapping("/anyUser/client/pushC")
public Object push() {
    try {
        // 消息类型：logoutCall（单点注销回调），框架内部自动处理
        return SaSsoClientProcessor.instance.ssoPushC();
    } catch (Exception e) {
        return SaResult.error(e.getMessage());
    }
}
```

> 若您的系统集成了 Spring Security 等安全框架，注销回调还需同步调整登出处理器（若依即因集成了 Spring Security 而改造了 `LogoutSuccessHandlerImpl`）。

### 4.2 后端：sa-token sso-client 配置

在您的 `application.yml` 中：

```yaml
sa-token:
  sso-client:
    ### 以下信息为【应用管理】创建应用后系统生成的值 ###
    client: your-app-key           # 应用ID（即 appKey）
    secret-key: your-app-secret    # 应用秘钥（即 appSecret）

    ### 以下信息根据您的部署环境修改 ###
    # MDP 单体版：boot-server 访问地址
    server-url: http://localhost:23455
    # MDP 微服务版：inner-gateway-server 访问地址 + workbench 前缀
    # server-url: http://localhost:23450/api/workbench
    # MDP 统一登录页地址（web-workbench 前端）
    authUrl: 'http://localhost:7700/#/auth/login'

    ### 以下为 MDP 服务端固定路径，请勿修改 ###
    signoutUrl: '/anyUser/sso/signout'
    pushUrl: '/anyUser/sso/pushS'
    # 是否使用模式三
    is-http: true
```

说明：

- `is-http: true` 即**模式三**：客户端后端通过 http 请求向 MDP 服务端校验 ticket、注销会话（推荐，前后端分离架构适用）；
- `secret-key` 用于客户端 ↔ 服务端后台接口调用的签名校验（timestamp + sign），防止伪造；
- `server-url` 必须是您的后端服务**可以访问到**的地址（内网互通即可）。

### 4.3 前端：登录中转页

新建一个中转页（如 `login_sso.vue`），逻辑只有两步：**有 ticket 就登录，没 ticket 就跳认证中心**。

```
访问中转页
   │
   ├─ URL 上有 ticket ──> 调 doLoginByTicket ──> 成功：保存 token，跳转 back 参数指定的原页面
   │                                        └─ 失败（30004/30005）：显示"重新登录"按钮
   └─ URL 上无 ticket ──> 调 getSsoAuthUrl(当前页面地址) ──> 重定向到 MDP 统一登录页
```

参考实现要点（完整代码见 `RuoYi-Vue/ruoyi-ui/src/views/login_sso.vue`）：

```javascript
created() {
  // 有 ticket 走登录，无 ticket 跳认证中心
  (this.ticket ? this.handleLoginByTicket(this.ticket) : this.goSsoAuthUrl());
},
methods: {
  handleLoginByTicket(ticket) {
    doLoginByTicket(ticket).then((res) => {
      // 保存 token 到本系统，然后跳回 back 指定的原页面
      this.$store.dispatch("Login2", res.data).then(() => {
        location.href = decodeURIComponent(this.back);
      });
    }).catch((error) => {
      // 30004：ticket 无效；30005：ticket 校验失败 —— 提示重新登录
      if ([30004, 30005].includes(error?.data?.code)) {
        this.ticketError = true;   // 页面显示"重新登录"按钮，点击后重新走 goSsoAuthUrl
      }
    });
  },
  goSsoAuthUrl() {
    // clientLoginUrl 传当前完整地址（含 back 参数），登录成功后认证中心会重定向回来
    getSsoAuthUrl(location.href).then((res) => {
      location.href = res.data;
    });
  },
}
```

配套调整：

- 将本系统所有"检测到未登录"的地方重定向到该中转页，并携带 `back` 参数（登录成功后要回到的地址）；
- 建议在路由守卫中：无 token 且无 ticket 时跳转中转页。

## 5. 单点注销

| 操作 | 行为 |
| --- | --- |
| 退出当前应用 | 只销毁本系统会话（客户端本地 `logout`） |
| **全端退出（单点注销）** | MDP 服务端遍历所有开启 `ssoPush` 的应用，逐个向其 `ssoPushUrl`（即您的 `pushC` 接口）推送 `logoutCall` 消息，各应用销毁本地会话 |

要支持单点注销，需要确保：

1. 应用配置中 `ssoPush` 已开启、`ssoPushUrl` 已正确指向您的 `pushC` 接口；
2. `pushC` 接口外网可访问（MDP 服务端主动发起 HTTP 调用）；
3. 集成了自有安全框架的系统需同步改造登出逻辑。

## 6. 从工作台跳转到您的系统（自动登录）

若您的系统配置了**自动登录地址**（`ssoAutoLoginUrl`），用户在 MDP 工作台"我的应用"点击您的应用时，MDP 会直接派发 ticket 并打开：

```
https://your-domain/your-login-path?ticket=xxx
```

您的中转页按 4.3 节流程处理该 ticket 即可，无需额外开发。

## 7. 常见问题

**Q1：登录后提示 ticket 无效（30004/30005）？**
- ticket 一次性且默认 5 分钟有效，请确认没有重复使用（例如页面刷新导致同一 ticket 提交两次）；
- 确认 `secret-key` 与平台登记的 `appSecret` 一致——客户端调用服务端校验接口需要签名；
- 出现该错误时引导用户走"重新登录"，重新获取新 ticket。

**Q2：重定向被拒绝 / 提示 redirect 不合法？**
`redirect` 地址必须在应用配置的 `ssoAllowUrl` 白名单内（支持逗号分隔多个）。请检查域名、端口、路径是否完全匹配。

**Q3：调 MDP 服务端接口提示 IP 不在白名单？**
检查应用配置的 `allowIp`。第三方应用应配置您服务器的**外网出口 IP**；与 MDP 部署在同一内网的系统才填内网 IP。

**Q4：单点注销没有生效？**
- 确认 `ssoPush` 开启、`ssoPushUrl` 配置正确且 MDP 服务端可访问；
- 确认您的 `pushC` 接口没有被您系统的鉴权拦截器拦截（该接口必须匿名可访问，即路径含 `anyUser`）；
- 查看您系统是否因集成 Spring Security 等框架，需要在登出处理器中补充销毁逻辑。

**Q5：ticket 换登录成功后，再次访问又要求登录？**
您本系统的会话时长设置过短或未持久化 token。建议用 `SaCheckTicketResult.remainTokenTimeout`（MDP 会话剩余时间）来设置您本系统会话的有效期，保持两者同步过期。

**Q6：如何区分不同登录方式、记录登录日志？**
认证中心在登录和跳转时会记录登录日志（含 appKey、跳转地址等），您可以在 MDP 控制台查询。
