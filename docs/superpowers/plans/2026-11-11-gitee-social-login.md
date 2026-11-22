# Gitee 社交登录与扩展性优化实施计划

> **执行方式：** 按任务顺序遵循 TDD；每任务先写失败测试，再写最小实现并回归。禁止任何 Git 暂存、提交和推送。

**目标：** 接入 Gitee 完整社交登录闭环，收敛重复平台映射并形成扩展文档。

**架构：** 复用现有 JustAuth 适配器、一次性票据和个人中心服务；平台枚举携带认证类型，工厂保留显式分支，前端通过统一类型和图标映射获得编译期检查。

**技术栈：** Java 17、Spring Boot 3、JustAuth 1.16.7、JUnit 5、Vue 3、TypeScript、Vitest。

## 全局约束

- 不修改 `application.yml` 中用户已经配置的 Gitee 节点。
- 不新增数据库表、字段或第三方依赖。
- 微信、QQ、钉钉继续只使用 `AuthToken.openId`；只有 Gitee 使用 `AuthUser.uuid`。
- 不输出真实凭证、令牌或完整敏感回调参数。
- 不执行 `git add`、`git commit`、`git push`。

---

### 任务 1：后端平台元数据与 Gitee 请求

**文件：**
- 修改 `SocialProviderEnum.java`、`AuthTypeEnum.java`
- 修改 `SocialLoginProperties.java`、`SocialProviderFactory.java`、`JustAuthRequestFactory.java`
- 测试 `UserSocialAuthServiceImplTest.java`、`SocialProviderFactoryTest.java`

- [x] 增加失败断言：`gitee` 可反向解析为 `GITEE`，认证类型编码为 `11`，请求类型为 `AuthGiteeRequest`。
- [x] 运行 `mvn -pl mdp-apps/md-workbench/workbench-service -am -DskipTests compile`，确认新增枚举引用导致编译失败。
- [x] 增加 `GITEE("gitee", "Gitee", AuthTypeEnum.GITEE)` 和 `GITEE("11", "Gitee登录")`。
- [x] 为现有枚举值补充对应 `AuthTypeEnum`，三个业务服务改用 `getAuthType()`。
- [x] 增加 Gitee 配置属性和两个工厂显式分支，测试工厂补全第四平台配置并验证启用数量。
- [x] 运行后端社交登录定向测试，确认平台元数据、现有请求类型与认证日志回归通过。

### 任务 2：Gitee 身份提取与绑定列表标签

**文件：**
- 修改 `JustAuthSocialProvider.java`、`SocialBindingVo.java`、`SocialAccountServiceImpl.java`
- 测试 `JustAuthSocialProviderTest.java`、`SocialAccountServiceImplTest.java`

- [x] 增加失败测试：Gitee 从 `AuthUser.uuid` 得到 `openId`；QQ 只有 `uuid` 而没有 `token.openId` 时仍抛出异常。
- [x] 增加失败测试：绑定列表返回 QQ、微信等枚举的 `label`。
- [x] 实现按平台明确选择身份字段，禁止通用 UUID 回退。
- [x] `SocialBindingVo` 增加 `label`，服务统一从平台枚举填充。
- [x] 运行两个测试类及全部社交服务测试，确认异常与日志不泄露测试密钥。

### 任务 3：前端 Gitee 与重复扩展点收敛

**文件：**
- 修改 `apps/web-workbench/src/api/common/model/social.ts`
- 修改 `apps/web-workbench/src/views/_core/sso/data/social.ts`
- 修改 `apps/web-workbench/src/views/_core/sso/social-login.vue`
- 修改 `apps/web-workbench/src/views/workbench/user/security/modules/social.vue`
- 测试对应 `social.test.ts`、`social-callback.test.ts`

- [x] 增加失败测试：合法 `gitee` 回调被接受，非法平台编码被拒绝；绑定列表保留后端 `label`。
- [x] 平台联合类型加入 `gitee`，`SocialBinding` 增加可选 `label`。
- [x] 回调白名单替换为长度不超过 32 的小写字母、数字、连字符编码格式校验。
- [x] 登录图标改为完整 `Record<SocialProvider, Component>`，加入现有 `SvgGiteeIcon`。
- [x] 个人中心移除中文名映射，使用 `account.label`，旧响应回退平台编码。
- [x] 运行三个前端定向测试文件、工作台 `typecheck` 和生产构建。

### 任务 4：扩展文档与最终验证

**文件：**
- 新建 `docs/social-login-extension.md`
- 修改 `docs/social-login.md`

- [x] 记录新增平台必须修改的后端枚举、配置属性、两个工厂、身份字段差异和前端类型/图标。
- [x] 说明认证类型与名称已收敛，回调安全仍由后端负责，配置和 JustAuth 请求工厂保留编译期显式分支。
- [x] 更新 Gitee 配置键、统一回调地址、数据库复用和人工验收清单，不记录真实配置值。
- [x] 执行后端社交测试与 Checkstyle、前端定向测试、类型检查和生产构建。
- [x] 核对 YML 未被本次修改，明确真实 Gitee 授权未由自动化测试执行。
