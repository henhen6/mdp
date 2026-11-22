# 第三方登录平台扩展指南

本文说明如何在现有社交登录体系中新增一个 JustAuth 支持的平台。当前已接入微信开放平台、QQ、钉钉和 Gitee，四者共用登录、首次选择、个人中心绑定/解绑、二次验证和跨账号转移流程。

## 1. 以 Gitee 为例

Gitee 接入后的调用链如下：

```text
登录页 / 个人中心
        │ provider=gitee
        ▼
SocialProviderEnum.GITEE
        │
        ├─ code=gitee             前端路由与接口编码
        ├─ label=Gitee            登录按钮与绑定列表名称
        └─ authType=GITEE(11)     登录日志与会话认证类型
        │
        ▼
SocialProviderFactory ──读取──> mdp.social.gitee
        │
        ▼
JustAuthRequestFactory ──创建──> AuthGiteeRequest
        │
        ▼
JustAuthSocialProvider ──读取──> AuthUser.uuid 作为统一 openId
        │
        ▼
provider=GITEE + clientId + openId 查询或绑定用户身份
```

除身份字段差异外，Gitee 没有独立的登录、注册、绑定、解绑或转移 Service。新增平台也应优先复用这套流程，不为每个平台复制业务代码。

## 2. 后端必改清单

### 2.1 定义认证日志类型

文件：`mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/enumeration/AuthTypeEnum.java`

增加一个全表唯一、不可复用的编码。例如 Gitee 使用：

```java
GITEE("11", "Gitee登录");
```

该类型只用于登录日志和会话认证类型，不加入账号密码认证策略。发布前检查现有枚举和历史数据，禁止修改已有编码。

### 2.2 定义平台元数据

文件：`mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/enumeration/SocialProviderEnum.java`

```java
GITEE("gitee", "Gitee", AuthTypeEnum.GITEE);
```

三个字段的含义：

| 字段 | 约束 | 使用位置 |
| --- | --- | --- |
| `code` | 小写字母开头，只含小写字母、数字、连字符，最长 32 字符 | 前端 URL、接口参数 |
| `label` | 用户可识别的平台名称 | 登录按钮、个人中心绑定列表 |
| `authType` | 对应唯一的 `AuthTypeEnum` | 登录、首次选择、跨账号转移 |

登录、首次选择和转移服务直接调用 `provider.getAuthType()`。新增平台后不要再在这三个 Service 中增加平台 switch。

数据库 `mdc_user_social_auth.provider` 保存枚举名称，例如 `GITEE`，不是前端编码 `gitee`。

### 2.3 绑定配置属性

文件：`mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/config/SocialLoginProperties.java`

```java
private ProviderConfig gitee = new ProviderConfig();
```

属性名对应 `mdp.social.gitee`。通用配置字段：

```yaml
mdp:
  social:
    example:
      enabled: ${EXAMPLE_ENABLED:false}
      client-id: ${EXAMPLE_CLIENT_ID:}
      client-secret: ${EXAMPLE_CLIENT_SECRET:}
      redirect-uri: ${EXAMPLE_REDIRECT_URI:}
      http-timeout-millis: 10000
```

以上仅为结构示例。真实密钥通过环境变量或配置中心注入，禁止写入文档、代码、日志和 Git。钉钉专用的 `corpId`、微信的 `unionId` 不应无理由复制到其他平台。

### 2.4 关联平台与配置

文件：`mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/SocialProviderFactory.java`

在 `configOf` 增加显式分支：

```java
case GITEE -> properties.getGitee();
```

工厂启动时会校验所有启用平台的 `clientId/clientSecret/redirectUri`，配置不完整直接失败。保留显式 switch 是为了让编译器发现遗漏，不建议改成按字符串反射读取配置。

### 2.5 创建 JustAuth 请求

文件：`mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/JustAuthRequestFactory.java`

```java
case GITEE -> new AuthGiteeRequest(authConfig);
```

先确认当前 JustAuth 版本确实提供目标请求类及构造器，再编写映射。公共 `AuthConfig` 已统一设置凭证、回调地址、HTTP 超时和 `ignoreCheckState(true)`；后者不是跳过安全校验，而是因为 `SocialTicketService` 已独立生成并一次性消费服务端 `state`，不能在请求工厂中删除这层校验。

### 2.6 核对用户标识字段

文件：`mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/impl/JustAuthSocialProvider.java`

不能假设所有平台都返回 `AuthToken.openId`：

| 平台 | 稳定用户标识来源 |
| --- | --- |
| 微信开放平台、QQ、钉钉 | `AuthUser.token.openId` |
| Gitee | `AuthUser.uuid`，其值为 Gitee 用户 `id` |

Gitee 在 JustAuth 1.16.7 中只把用户 `id` 写入 `uuid`。当前实现仅对 Gitee 读取该字段，其他平台行为不变。

禁止写成“所有平台 `openId` 为空就回退 `uuid`”。QQ 的 `uuid` 可能是 `unionId`，通用回退会把跨应用标识误作应用身份。新平台应先通过 JustAuth 源码或字节码确认字段语义，再增加明确的平台分支和测试；若 `uuid` 只是用户名、邮箱或可修改字段，不得作为绑定标识。

统一身份键仍为：

```text
provider + clientId + openId
```

`unionId` 只是附加信息，不替代上述应用内身份键。

## 3. 前端必改清单

前端仓库：`mdp-vben`，应用：`apps/web-workbench`。

### 3.1 平台编码类型

文件：`apps/web-workbench/src/api/common/model/social.ts`

在 `SocialProvider` 联合类型中加入平台编码，例如 `'gitee'`。`SocialBinding.label` 由后端返回，前端不再维护平台中文名 Map。

### 3.2 登录图标

文件：`apps/web-workbench/src/views/_core/sso/social-login.vue`

在 `providerIcons: Record<SocialProvider, Component>` 中注册图标。Gitee 复用 `@vben/icons` 已有的 `SvgGiteeIcon`。如果新平台没有现成图标，应按 icons 模块现有方式添加，不在页面内嵌入第三方远程图片。

完整的 `Record` 能让 TypeScript 在新增平台但漏配图标时编译失败。

### 3.3 不需要修改的扩展点

以下位置新增平台时无需改动：

- `views/_core/sso/data/social.ts`：回调只校验编码格式，不再维护平台白名单。
- 首次未绑定二选一页面。
- 个人中心绑定、解绑、二次验证和跨账号转移逻辑。
- 个人中心平台名称：使用后端 `SocialBinding.label`。
- 统一回调路由 `/auth/social/callback/:provider`。

前端格式校验不是安全边界。伪造合法编码后，后端仍会通过 `SocialProviderEnum.fromCode`、平台启用状态和一次性票据校验拒绝请求。

## 4. 回调地址与数据库

统一回调地址：

```text
https://{工作台域名}/#/auth/social/callback/{provider}
```

使用 hash 路由时保留 `/#/`；使用 history 路由时按实际路由去掉 `#`。Gitee 示例路径为 `/auth/social/callback/gitee`。YML 中配置原始地址，不要手动编码为 `%23`；`JustAuthRequestFactory` 创建 Gitee 请求时会统一编码含 hash 的 `redirect_uri`，使授权链接与令牌交换使用一致的回调地址，避免后续参数被当成 fragment 或回调路径在令牌请求中丢失。Gitee 回跳可能把 `code/state` 放在 `#` 前，前端同时兼容 `route.query` 与 `location.search`，并在提交登录前清理两处授权参数。

第三方平台后台登记的回调地址必须与 `redirect-uri` 完全一致，包括协议、域名、端口、路径、`#` 和末尾斜杠，本地开发按平台规则配置。如果具体平台禁止回调地址包含 fragment，则应改用 history 路由或服务端桥接回调。

复用 `mdc_user_social_auth`，标准新增平台不需要 SQL 变更。`provider varchar(32)` 可保存 `GITEE`。唯一约束继续保证同一应用身份只能绑定一个用户，同一用户在同一平台只有一条有效绑定。

## 5. 测试清单

新增平台至少覆盖：

1. 枚举 `code/label/authType` 和 `fromCode` 双向解析。
2. 配置工厂返回正确的请求类型，启用列表包含新平台，未启用时拒绝。
3. 用户标识来自正确字段；缺失标识时快速失败。
4. 原平台身份字段回归，尤其 QQ 不得把 `uuid/unionId` 当成 `openId`。
5. 已绑定登录、未绑定进入 `PENDING`、个人中心绑定列表 `label`。
6. 前端接受新平台回调，拒绝路径穿越、缺失平台、缺失 `code/state` 等非法参数。
7. 前端类型检查、定向测试和生产构建。

自动化测试通过不代表真实平台验收通过。上线前必须使用真实应用完成：已绑定登录、首次绑定已有账号、首次创建账号、个人中心绑定/解绑、二次验证、跨账号转移、取消授权、重复/过期回调，并检查日志和浏览器存储不泄露凭证或令牌。

## 6. 推荐实施顺序

```text
确认 JustAuth 请求类与用户标识语义
                ↓
先写平台枚举、请求类型、身份字段失败测试
                ↓
补后端元数据、配置属性、两个工厂分支
                ↓
补前端联合类型和图标
                ↓
运行原平台与新平台回归测试
                ↓
核对回调地址并进行真实平台人工验收
```

如果新增平台需要非标准 OAuth 流程、额外配置或不同身份字段，应先扩展适配器边界并补测试，不要把平台特殊判断散落到登录、注册和个人中心 Service。
