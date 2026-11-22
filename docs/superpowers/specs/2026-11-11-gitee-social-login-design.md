# Gitee 社交登录与平台扩展性优化设计

## 目标

在现有微信开放平台、QQ、钉钉网页登录基础上接入 Gitee，并覆盖首次未绑定选择、个人中心绑定、解绑、二次验证和跨账号安全转移。优化新增平台时重复且容易漏改的映射，保留现有认证流程和安全边界。

## 范围与约束

- 使用现有 JustAuth 1.16.7，不新增依赖。
- 用户已配置的 `mdp.social.gitee` YML 不修改；代码通过 Spring Boot relaxed binding 读取现有字段。
- 复用 `mdc_user_social_auth`，数据库 `provider` 保存 `GITEE`，不新增表或字段。
- Gitee 使用新的认证日志编码 `11`，现有 `01` 至 `10` 不变。
- 保持普通登录、SSO、微信小程序以及现有三个网页平台行为不变。
- 不引入动态 SPI、反射构造或自动扫描请求类。
- 不执行任何 Git 暂存、提交或推送。

## 后端设计

### 平台元数据

`SocialProviderEnum` 增加 `GITEE("gitee", "Gitee", AuthTypeEnum.GITEE)`。枚举同时持有 `code`、`label` 和 `authType`，登录、首次选择、跨账号转移统一调用 `provider.getAuthType()`，删除三份内容相同的 switch。

`AuthTypeEnum` 增加 `GITEE("11", "Gitee登录")`，仅用于认证日志和会话认证类型，不注册为账号密码认证策略。

### 配置与请求工厂

`SocialLoginProperties` 增加 `ProviderConfig gitee`，对应现有 `mdp.social.gitee` 节点。`SocialProviderFactory.configOf` 和 `JustAuthRequestFactory.create` 各增加一个显式分支，Gitee 请求类型为 `AuthGiteeRequest`。

保留这两个显式分支：新增平台时编译器会提示遗漏，且构造逻辑直观。配置完整性仍在工厂初始化时 fail fast。

### 身份标识

JustAuth 的 Gitee 实现把用户 `id` 写入 `AuthUser.uuid`，其 `AuthToken` 不包含 `openId`。因此身份提取按平台明确区分：

- 微信、QQ、钉钉：继续只读取 `AuthToken.openId`。
- Gitee：读取 `AuthUser.uuid`。
- 任一平台未取得有效标识：抛出“第三方未返回有效用户标识”。

不做通用的 `openId` 为空即回退 `uuid`，因为 QQ 的 `uuid` 可能表示 `unionId`，通用回退会改变身份语义并造成跨应用误绑定风险。`clientId` 仍参与身份唯一键，第三方令牌和密钥不进入日志、异常或响应。

### 绑定列表

`SocialBindingVo` 增加 `label`，由 `SocialProviderEnum.label` 填充。新增字段向后兼容，不删除或改名已有字段。前端不再自行维护平台中文名称。

## 前端设计

- `SocialProvider` 增加 `'gitee'`。
- 登录图标改为 `Record<SocialProvider, Component>` 映射，复用已有 `SvgGiteeIcon`。新增平台缺少图标时由 TypeScript 编译检查提示。
- 回调路由不再维护平台白名单，只校验编码格式 `^[a-z][a-z0-9-]{0,31}$` 以及 `code/state` 是否存在。真正的平台合法性、启用状态、一次性 `state` 和票据用途仍由后端校验。
- 个人中心使用接口返回的 `label`，以平台编码作为旧响应的显示回退。

## 数据流

Gitee 与现有平台完全复用以下流程：

1. 后端创建带平台、动作和用户上下文的一次性 `state`，JustAuth 生成官方授权地址。
2. 统一回调页清理 URL 中的 `code/state`，后端消费 `state` 并交换第三方身份。
3. 已绑定身份直接登录；未绑定身份签发短期 `pendingTicket`，由用户选择绑定已有账号或创建新账号。
4. 个人中心敏感操作继续要求二次验证；跨账号转移只更新第三方身份所属用户。

## 错误处理

- 启用但配置不完整：启动失败。
- 未启用或不支持的平台：后端拒绝，不依赖前端白名单保障安全。
- 授权失败或用户标识缺失：抛出具体业务异常，不签发会话或票据。
- 非法回调编码、缺少 `code/state`：前端快速失败；伪造合法格式但不受支持的平台由后端拒绝。

## 测试与验收

- 后端单测覆盖 Gitee 编码、认证类型、请求类型、启用列表、`uuid` 身份提取和绑定列表 `label`。
- 保留并执行 QQ 缺少 `openId` 必须失败的回归测试。
- 前端单测覆盖 Gitee 回调、非法编码拒绝、后端 `label` 透传。
- 执行后端定向测试和 Checkstyle、前端定向测试、类型检查和生产构建。
- 自动化测试不访问真实 Gitee；真实授权、首次选择、绑定、解绑和转移必须在配置有效应用的环境中人工验收，并如实记录结果。
