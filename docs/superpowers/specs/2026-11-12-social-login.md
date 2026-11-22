# 第三方登录配置说明

支持微信开放平台、QQ、钉钉和 Gitee。配置前缀为 `mdp.social`，密钥通过环境变量或配置中心注入，不写入文档和代码仓库。新增平台的开发步骤见 [第三方登录平台扩展指南](./social-login-extension.md)。

## 配置项

每个平台（`wechatOpen` / `qq` / `dingTalk` / `gitee`）包含以下通用字段：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `enabled` | 是 | 是否启用该平台 |
| `client-id` | 是 | 第三方应用 ID |
| `client-secret` | 是 | 第三方应用密钥 |
| `redirect-uri` | 是 | 授权回调地址 |
| `corp-id` | 否 | 钉钉指定企业组织时按需配置，配置后授权范围包含 `corpid` |
| `union-id` | 否 | 是否申请跨应用 unionId |
| `http-timeout-millis` | 否 | HTTP 请求超时，默认 10000 毫秒 |

## 环境变量

通过环境变量占位符注入，部署时在各环境单独配置；本文档不记录任何真实值。各平台是否默认启用以当前部署配置为准。

完整 YML 结构位于 `mdp-apps/md-server/boot-server/src/main/resources/application.yml` 的 `mdp.social` 节点。MySQL 8.0+ 增量建表脚本位于 `docs/sql/2026-11-11-social-login.sql`，可重复执行；接入 Gitee 不需要新增表或字段。

### 微信开放平台

- `${WECHAT_OPEN_ENABLED}`：是否启用
- `${WECHAT_OPEN_CLIENT_ID}`
- `${WECHAT_OPEN_CLIENT_SECRET}`
- `${WECHAT_OPEN_REDIRECT_URI}`：微信开放平台登记的完整回调地址
- `${WECHAT_OPEN_UNION_ID}`：是否申请 unionId

### QQ

- `${QQ_ENABLED}`：是否启用
- `${QQ_CLIENT_ID}`
- `${QQ_CLIENT_SECRET}`
- `${QQ_REDIRECT_URI}`：QQ 互联登记的完整回调地址

### 钉钉

- `${DINGTALK_ENABLED}`：是否启用
- `${DINGTALK_CLIENT_ID}`
- `${DINGTALK_CLIENT_SECRET}`
- `${DINGTALK_REDIRECT_URI}`：钉钉应用登记的完整回调地址
- `${DINGTALK_CORP_ID}`（指定企业组织时按需配置）

配置 `corp-id` 后，后端自动使用 `openid,corpid` 授权范围，以获取用户在授权过程中选择的组织 ID；未配置时保持默认 `openid`。这里的“组织信息”仅指 `corpId`，不会读取企业名称、部门、成员或通讯录，也不因此新增企业通讯录权限诉求。

### Gitee

- `${GITEE_ENABLED}`：是否启用
- `${GITEE_CLIENT_ID}`
- `${GITEE_CLIENT_SECRET}`
- `${GITEE_REDIRECT_URI}`：Gitee 应用登记的完整回调地址

Gitee 使用 JustAuth `AuthGiteeRequest`，其用户 `id` 由 JustAuth 写入 `AuthUser.uuid`，后端将该值保存为统一身份模型的 `openId`。其他平台仍读取 `AuthToken.openId`，不做通用 UUID 回退。

## 回调地址格式

hash 路由模式下：

- 微信开放平台：`https://{工作台域名}/#/auth/social/callback/wechat-open`
- QQ：`https://{工作台域名}/#/auth/social/callback/qq`
- 钉钉：`https://{工作台域名}/#/auth/social/callback/dingtalk`
- Gitee：`https://{工作台域名}/#/auth/social/callback/gitee`

history 路由模式下去掉地址中的 `#`。YML 中保留原始回调地址，不要手动把 `#` 写成 `%23`；后端创建第三方请求时会统一编码含 hash 的 `redirect_uri`，使授权链接与令牌交换使用一致的回调地址，避免后续参数被当成 fragment 或回调路径在令牌请求中丢失。Gitee 回跳可能把 `code/state` 放在 `#` 前，前端同时兼容 `route.query` 与 `location.search`，并在提交登录前清理两处授权参数。

回调地址指向工作台统一回调页，必须与第三方开放平台登记的地址完全一致，包括协议、域名、端口、路径、`#` 和末尾斜杠。生产环境仅允许 `https`，本地开发按各平台规则配置。如果具体平台禁止回调地址包含 fragment，则应改用 history 路由或服务端桥接回调。

## 安全要求

1. 平台标记 `enabled=true` 但 `client-id`、`client-secret`、`redirect-uri` 任一缺失时，应用启动直接失败，不得带病运行。
2. 密钥只能通过环境变量或配置中心加密注入，禁止提交到 Git、打印到日志或写入接口响应。
3. 授权 state 由服务端生成并通过缓存一次性校验，第三方回调必须携带有效 state，防止 CSRF。
4. 前端回调只校验平台编码格式，不支持的平台、未启用平台、伪造或过期票据均由后端拒绝，前端白名单不是安全边界。
5. 钉钉固定使用新版授权接口（JustAuth `AuthDingTalkV2Request`），不得回退旧版接口。
6. 身份唯一键为 `provider + clientId + openId`。QQ 的 `AuthUser.uuid` 可能表示 unionId，禁止将其作为通用 openId 回退。

## 真实平台人工验收

### 验收步骤

每个平台均按以下顺序执行，并分别记录结果：

1. 在开放平台配置应用凭证和上述精确回调地址，通过环境变量注入后端，启动后端与工作台。
2. 在登录页验证已绑定身份直接登录；未绑定身份必须停留在待选择页，分别验证“绑定已有账号”和“创建新账号”，不得自动注册。
3. 在工作台安全设置中验证当前环境可用的二次验证方式：账号密码、手机验证码、邮箱验证码、已绑定第三方重新授权。
4. 验证个人中心绑定、解绑、最后登录方式保护，以及误注册后的跨账号安全转移。转移只能更换第三方身份所属账号，不得迁移组织、角色、消息和业务数据。
5. 验证取消授权、缺少或错误回调参数、重复回调、过期票据和禁用用户，均应明确失败且不能签发新会话。
6. 检查浏览器地址、会话存储和前后端日志，不得出现密码、验证码、访问令牌或第三方应用密钥。

### 本次执行结果

本次仅完成自动化测试、类型检查和生产构建，未发起真实第三方平台授权，因此以下项目均为“未执行真实第三方验收”。自动化结果不能替代真实扫码和授权回调验收。

| 平台 | 已绑定登录与首次选择 | 个人中心绑定/解绑/转移 | 异常与过期场景 | 结果说明 |
| --- | --- | --- | --- | --- |
| 微信开放平台 | 未执行 | 未执行 | 未执行 | 需在已配置真实微信开放平台应用的环境中验收 |
| QQ | 未执行 | 未执行 | 未执行 | 需在已配置真实 QQ 互联应用的环境中验收 |
| 钉钉 | 未执行 | 未执行 | 未执行 | 需在已配置真实钉钉应用及回调白名单的环境中验收 |
| Gitee | 未执行 | 未执行 | 未执行 | 需在已配置真实 Gitee 应用及精确回调地址的环境中验收 |
