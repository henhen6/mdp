# 微信、QQ、钉钉登录与账号绑定 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在工作台接入微信、QQ、钉钉官方授权登录，并完成首次登录二选一、个人中心绑定解绑及误注册后的安全转移。

**Architecture:** 后端以独立第三方身份表为唯一网站绑定数据源，通过 JustAuth 策略适配三个平台，复用现有账号认证与 Sa-Token 会话签发能力。Redis 票据隔离登录、注册、绑定、解绑和转移动作；前端使用统一回调页和个人中心第三方账号卡片。

**Tech Stack:** Java 17、Spring Boot 3、MyBatis-Flex、Sa-Token、JustAuth 1.16.7、Redis CacheOps、JUnit 5、Mockito、Vue 3、TypeScript、Pinia、Vitest、Vue Test Utils。

## Global Constraints

- 后端仓库：`/Users/tangyh/gitee/mdp`；前端仓库：`/Users/tangyh/gitee/mdp-vben`。
- 设计基线：`docs/superpowers/specs/2026-11-11-social-login-account-binding-design.md`。
- 禁止执行任何 `git add`、`git commit`、`git push`，用任务结束后的测试和代码审查代替提交节点。
- 禁止删除文件、目录或 Git 历史，不回退两个仓库中的现有未提交改动。
- `docs/sql/mdp.sql` 已有用户改动，只能追加本功能表结构，不得覆盖或回退。
- 不读取或输出配置文件中的中间件地址、密码及任何真实第三方密钥。
- 所有交互文案、错误说明和注释使用简体中文；代码标识符保持英文。
- 每个新增函数保持单一职责，超过 80 行时按业务步骤拆分。
- 新增 Mapper 必须标注 `@Repository`。
- 实体表字段放在 `entity/base/UserSocialAuthBase.java`，业务扩展放在 `entity/UserSocialAuth.java`。
- 不修改现有 `/anyUser/auth/login` 契约，不迁移 `wx_openid`、`dd_openid` 和微信小程序逻辑。
- 第三方网站身份统一取 JustAuth `AuthUser.token.openId`，禁止把 QQ 的 `uuid` 当作 OpenID。
- 社交注册的 `userSource` 保持现有平台用户语义，避免改变密码算法分支；第三方来源由 `mdc_user_social_auth.provider` 记录。
- 不在日志、URL 或前端响应中暴露 client-secret、访问令牌、OpenID、UnionID、密码和验证码。
- 每个任务严格先写失败测试，再写最小实现，最后运行测试并审查差异。

---

## 文件结构

### 后端公共模型与持久化

- `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/entity/base/UserSocialAuthBase.java`：表字段。
- `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/entity/UserSocialAuth.java`：表映射。
- `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/dto/UserSocialRegisterDto.java`：内部社交注册请求。
- `mdp-apps/md-public/md-common-dao/src/main/java/top/mddata/common/mapper/UserSocialAuthMapper.java`：身份查询及条件更新。
- `mdp-apps/md-public/md-cache-key/src/main/java/top/mddata/common/cache/workbench/Social*CacheKeyBuilder.java`：三类票据及消费标记键。
- `docs/sql/mdp.sql`：新增 `mdc_user_social_auth`。

### 工作台后端

- `workbench-pojo/.../enumeration/`：平台、动作、二次验证方式枚举。
- `workbench-pojo/.../dto/`：回调、注册、绑定、二次验证、解绑和转移入参。
- `workbench-pojo/.../vo/`：平台、绑定状态、登录结果和票据返回。
- `workbench-service/.../config/SocialLoginProperties.java`：第三方应用配置。
- `workbench-service/.../service/social/model/`：第三方身份及票据上下文。
- `workbench-service/.../service/social/`：票据、平台、登录、注册和账号管理接口。
- `workbench-service/.../service/social/impl/`：上述接口实现。
- `workbench-service/.../service/AccountAuthenticator.java`：复用登录策略的账号认证。
- `workbench-service/.../service/LoginSessionService.java`：已认证用户的会话签发能力。
- `workbench-web/.../controller/SocialAuthController.java`：公开登录接口。
- `workbench-web/.../controller/SocialOnboardingController.java`：首次登录二选一接口。
- `workbench-web/.../controller/ProfileSocialController.java`：个人中心第三方账号接口。

### 控制台注册事务

- `console-facade/console-api/.../UserFacade.java`：社交注册入口。
- `console-facade/console-boot-impl/.../UserFacadeImpl.java`：单体本地调用。
- `console-facade/console-cloud-impl/.../UserApi.java`：微服务内部调用。
- `console-facade/console-cloud-impl/.../UserFacadeImpl.java`：微服务适配及失败检查。
- `console-facade/console-cloud-impl/.../fallback/UserApiFallback.java`：超时回退。
- `console-web/.../inner/InnerUserController.java`：内部注册接口。
- `console-service/.../organization/UserService.java`：社交注册服务契约。
- `console-service/.../organization/impl/UserServiceImpl.java`：用户与绑定同事务写入。

### 前端

- `apps/web-workbench/src/api/common/model/social.ts`：公开登录类型。
- `apps/web-workbench/src/api/common/social.ts`：公开登录 API。
- `apps/web-workbench/src/api/workbench/user/security/social.ts`：个人中心 API。
- `apps/web-workbench/src/store/auth.ts`：统一登录完成逻辑。
- `apps/web-workbench/src/views/_core/sso/social-login.vue`：登录入口。
- `apps/web-workbench/src/views/_core/sso/social-callback.vue`：统一回调及首登选择。
- `apps/web-workbench/src/views/_core/sso/modules/`：绑定已有账号、注册组件。
- `apps/web-workbench/src/views/_core/sso/data/social.ts`：回调参数和短期上下文。
- `apps/web-workbench/src/views/workbench/user/security/modules/social*.vue`：账号卡片及操作弹窗。
- `apps/web-workbench/src/views/workbench/user/security/data/social.ts`：账号管理状态与动作。
- `apps/web-workbench/src/router/routes/core.ts`：回调路由。
- `apps/web-workbench/src/locales/langs/{zh-CN,en-US}/workbench/user/security.json`：账号卡片文案。

---

### Task 1: 第三方身份模型与持久化

**Files:**
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/enumeration/SocialProviderEnum.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/model/SocialIdentity.java`
- Create: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/entity/base/UserSocialAuthBase.java`
- Create: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/entity/UserSocialAuth.java`
- Create: `mdp-apps/md-public/md-common-dao/src/main/java/top/mddata/common/mapper/UserSocialAuthMapper.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/UserSocialAuthService.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/impl/UserSocialAuthServiceImpl.java`
- Modify: `docs/sql/mdp.sql`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/impl/UserSocialAuthServiceImplTest.java`

**Interfaces:**
- Produces: `SocialProviderEnum.{WECHAT_OPEN,QQ,DINGTALK}`，前端编码分别为 `wechat-open`、`qq`、`dingtalk`。
- Produces: `SocialIdentity(provider, clientId, openId, unionId, nickname)`。
- Produces: `UserSocialAuthService.bind/findByIdentity/findByUserAndProvider/listByUser/countByUser/unbind/transfer`。

- [ ] **Step 1: 编写持久化行为失败测试**

```java
@Test
void 第三方身份已绑定其他用户时拒绝重复绑定() {
    SocialIdentity identity = identity();
    UserSocialAuth existing = new UserSocialAuth().setId(9L).setUserId(2L);
    when(mapper.selectOneByQuery(any(QueryWrapper.class))).thenReturn(existing);

    assertThrows(BizException.class, () -> service.bind(1L, identity));
    verify(mapper, never()).insert(any(UserSocialAuth.class));
}

@Test
void 转移时源用户不匹配则快速失败() {
    when(mapper.transferUser(any(), any(), any(), any(), any())).thenReturn(0);

    assertThrows(BizException.class, () -> service.transfer(9L, 1L, 2L, 1L));
}

@Test
void 解绑必须同时匹配绑定记录和当前用户() {
    when(mapper.logicalDeleteByUser(any(), any(), any(), any(), any())).thenReturn(0);

    assertThrows(BizException.class, () -> service.unbind(9L, 1L));
}

private SocialIdentity identity() {
    return SocialIdentity.builder()
            .provider(SocialProviderEnum.QQ)
            .clientId("client-id")
            .openId("open-id")
            .unionId("union-id")
            .nickname("测试用户")
            .build();
}
```

- [ ] **Step 2: 运行测试并确认因新类型缺失而失败**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=UserSocialAuthServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 编译失败，提示 `UserSocialAuthService`、`SocialIdentity` 或实现类不存在。

- [ ] **Step 3: 添加平台枚举和内部身份模型**

```java
@Getter
@AllArgsConstructor
public enum SocialProviderEnum {
    WECHAT_OPEN("wechat-open", "微信"),
    QQ("qq", "QQ"),
    DINGTALK("dingtalk", "钉钉");

    @JsonValue
    private final String code;
    private final String label;

    @JsonCreator
    public static SocialProviderEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new BizException("不支持的第三方登录平台"));
    }
}
```

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SocialIdentity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private SocialProviderEnum provider;
    private String clientId;
    private String openId;
    private String unionId;
    private String nickname;
}
```

- [ ] **Step 4: 添加表结构、实体和条件更新 Mapper**

```sql
CREATE TABLE IF NOT EXISTS `mdc_user_social_auth` (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `provider` varchar(32) NOT NULL COMMENT '第三方平台',
  `client_id` varchar(128) NOT NULL COMMENT '第三方应用ID',
  `open_id` varchar(128) NOT NULL COMMENT '应用内用户标识',
  `union_id` varchar(128) DEFAULT NULL COMMENT '跨应用用户标识',
  `nickname` varchar(255) DEFAULT NULL COMMENT '第三方昵称',
  `bound_at` datetime(3) NOT NULL COMMENT '绑定时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_at` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  `deleted_by` bigint DEFAULT NULL COMMENT '删除人',
  `deleted_at` bigint NOT NULL DEFAULT '0' COMMENT '删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_social_identity` (`provider`, `client_id`, `open_id`, `deleted_at`),
  UNIQUE KEY `uk_user_provider` (`user_id`, `provider`, `deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户第三方身份绑定';
```

实体基类包含 `id` 以外的全部表字段，并继承 `SuperEntity<Long>`；`UserSocialAuth` 只添加：

```java
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(UserSocialAuthBase.TABLE_NAME)
public class UserSocialAuth extends UserSocialAuthBase {
}
```

Mapper 必须包含 `@Repository` 及以下条件更新：

```java
@Update("""
        UPDATE mdc_user_social_auth
           SET user_id = #{targetUserId}, bound_at = #{now},
               updated_at = #{now}, updated_by = #{operatorId}
         WHERE id = #{id} AND user_id = #{sourceUserId} AND deleted_at = 0
        """)
int transferUser(@Param("id") Long id, @Param("sourceUserId") Long sourceUserId,
                 @Param("targetUserId") Long targetUserId,
                 @Param("operatorId") Long operatorId, @Param("now") LocalDateTime now);

@Update("""
        UPDATE mdc_user_social_auth
           SET deleted_at = #{deletedAt}, deleted_by = #{operatorId},
               updated_at = #{now}, updated_by = #{operatorId}
         WHERE id = #{id} AND user_id = #{userId} AND deleted_at = 0
        """)
int logicalDeleteByUser(@Param("id") Long id, @Param("userId") Long userId,
                        @Param("operatorId") Long operatorId,
                        @Param("deletedAt") Long deletedAt, @Param("now") LocalDateTime now);
```

- [ ] **Step 5: 实现查询、绑定、计数、解绑和转移**

```java
@Override
public UserSocialAuth bind(Long userId, SocialIdentity identity) {
    findByIdentity(identity).ifPresent(item -> {
        throw new BizException("该第三方账号已绑定其他用户");
    });
    findByUserAndProvider(userId, identity.getProvider()).ifPresent(item -> {
        throw new BizException("当前账号已绑定该平台，请先解绑");
    });
    UserSocialAuth entity = new UserSocialAuth()
            .setUserId(userId)
            .setProvider(identity.getProvider().name())
            .setClientId(identity.getClientId())
            .setOpenId(identity.getOpenId())
            .setUnionId(identity.getUnionId())
            .setNickname(identity.getNickname())
            .setBoundAt(LocalDateTime.now());
    try {
        save(entity);
        return entity;
    } catch (DuplicateKeyException ex) {
        throw new BizException("第三方绑定关系已发生变化，请重新操作", ex);
    }
}

@Override
public void unbind(Long id, Long userId) {
    LocalDateTime now = LocalDateTime.now();
    int affected = mapper.logicalDeleteByUser(id, userId, userId,
            System.currentTimeMillis(), now);
    ArgumentAssert.isTrue(affected == 1, "绑定关系已发生变化，请刷新后重试");
}

@Override
public void transfer(Long id, Long sourceUserId, Long targetUserId, Long operatorId) {
    try {
        int affected = mapper.transferUser(id, sourceUserId, targetUserId,
                operatorId, LocalDateTime.now());
        ArgumentAssert.isTrue(affected == 1, "绑定关系已发生变化，请刷新后重试");
    } catch (DuplicateKeyException ex) {
        throw new BizException("目标账号已绑定该平台，或第三方身份发生冲突", ex);
    }
}
```

查询统一使用 `QueryWrapper` 的 `provider/clientId/openId` 或 `userId/provider` 条件；逻辑删除由 MyBatis-Flex 全局规则处理。`countByUser` 使用 `selectCountByQuery`，不得循环查表。

- [ ] **Step 6: 运行测试并审查数据层**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=UserSocialAuthServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: `UserSocialAuthServiceImplTest` 全部通过；Mapper 带 `@Repository`；未修改旧用户表和旧 OpenID 字段。

---

### Task 2: 抽取账号认证并复用会话签发

**Files:**
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/AccountAuthenticator.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/LoginSessionService.java`
- Modify: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/impl/AuthServiceImpl.java`
- Modify: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/enumeration/AuthTypeEnum.java`
- Modify: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/impl/AuthServiceImplFindOrgTest.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/AccountAuthenticatorTest.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/impl/AuthServiceImplLoginTest.java`

**Interfaces:**
- Produces: `User AccountAuthenticator.authenticate(LoginDto login)`。
- Produces: `void AccountAuthenticator.checkUserState(User user, AuthTypeEnum type, String deviceInfo)`。
- Produces: `LoginVo LoginSessionService.loginAuthenticated(User user, AuthTypeEnum type, String account, String deviceId, String deviceInfo, String deviceType)`。
- Consumes: 现有 `Map<String, LoginStrategy>` 和 `AuthServiceImpl.fillTokenSession`。

- [ ] **Step 1: 编写认证复用失败测试**

```java
@Test
void 社交认证类型不能走账号密码策略() {
    LoginDto login = new LoginDto();
    login.setAuthType(AuthTypeEnum.WECHAT_OPEN);

    assertThrows(BizException.class, () -> authenticator.authenticate(login));
    verifyNoInteractions(loginStrategy);
}

@Test
void 手机认证仍复用现有策略顺序() {
    LoginDto login = new LoginDto();
    login.setAuthType(AuthTypeEnum.PHONE);
    login.setUsername("13800000000");
    User user = new User().setId(1L);
    when(loginStrategy.getUser(login.getUsername())).thenReturn(user);

    assertSame(user, authenticator.authenticate(login));
    InOrder order = inOrder(loginStrategy);
    order.verify(loginStrategy).checkParam(login);
    order.verify(loginStrategy).getUser(login.getUsername());
    order.verify(loginStrategy).checkUserPassword(login, user);
    order.verify(loginStrategy).checkUserState(login, user);
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=AccountAuthenticatorTest,AuthServiceImplLoginTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 因 `AccountAuthenticator` 和 `LoginSessionService` 不存在而编译失败。

- [ ] **Step 3: 实现账号认证组件和社交日志类型**

```java
@Component
@RequiredArgsConstructor
public class AccountAuthenticator {
    private static final Set<AuthTypeEnum> SUPPORTED = EnumSet.of(
            AuthTypeEnum.CAPTCHA, AuthTypeEnum.USERNAME,
            AuthTypeEnum.PHONE, AuthTypeEnum.EMAIL);

    private final Map<String, LoginStrategy> loginStrategy;

    public User authenticate(LoginDto login) {
        ArgumentAssert.notNull(login, "登录参数不能为空");
        ArgumentAssert.isTrue(SUPPORTED.contains(login.getAuthType()), "不支持的账号验证方式");
        LoginStrategy strategy = loginStrategy.get(login.getAuthType().name());
        ArgumentAssert.notNull(strategy, "未配置对应的账号验证策略");
        strategy.checkParam(login);
        User user = strategy.getUser(login.getUsername());
        strategy.checkUserPassword(login, user);
        strategy.checkUserState(login, user);
        return user;
    }

    public void checkUserState(User user, AuthTypeEnum type, String deviceInfo) {
        ArgumentAssert.notNull(user, "账号不存在或已被删除");
        LoginDto context = new LoginDto();
        context.setAuthType(type);
        context.setUsername(user.getUsername());
        context.setDeviceInfo(deviceInfo);
        loginStrategy.get(AuthTypeEnum.USERNAME.name()).checkUserState(context, user);
    }
}
```

在 `AuthTypeEnum` 添加 `WECHAT_OPEN("08", "微信网站扫码登录")`、`QQ("09", "QQ登录")`、`DINGTALK("10", "钉钉扫码登录")`，并把 EMAIL 的过时描述修正为“邮箱验证码登录”。这些新类型只用于日志，不注册为账号密码策略。

- [ ] **Step 4: 暴露统一的已认证用户会话签发方法**

```java
public interface LoginSessionService {
    LoginVo loginAuthenticated(User user, AuthTypeEnum authType, String account,
                               String deviceId, String deviceInfo, String deviceType);
}
```

`AuthServiceImpl` 同时实现 `LoginSessionService`，普通登录修改为：

```java
@Override
public R<LoginVo> login(LoginDto login) {
    User user = accountAuthenticator.authenticate(login);
    return R.success(loginAuthenticated(user, login.getAuthType(), login.getUsername(),
            login.getDeviceId(), login.getDeviceInfo(), "PC"));
}
```

把原登录方法中 Sa-Token、TokenSession、LoginVo 和登录事件逻辑移动到接口方法，保留 `fillTokenSession` 及组织解析方法原位，避免无关搬迁：

```java
@Override
public LoginVo loginAuthenticated(User user, AuthTypeEnum authType, String account,
                                  String deviceId, String deviceInfo, String deviceType) {
    StpUtil.login(user.getId(), new SaLoginParameter()
            .setDeviceType(deviceType)
            .setDeviceId(StrUtil.isEmpty(deviceId) ? SaFoxUtil.getRandomString(32) : deviceId));
    fillTokenSession(user);

    JSONObject obj = new JSONObject();
    obj.put(USER_ID, user.getId());
    SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
    LoginVo loginVo = BeanUtil.toBean(tokenInfo, LoginVo.class);
    loginVo.setExpire(tokenInfo.getTokenTimeout());
    loginVo.setRefreshToken(SaTempUtil.createToken(obj.toString(),
            2 * saTokenConfig.getTimeout()));
    loginVo.setId(user.getId());

    LoginLogDto dto = LoginLogDto.success(authType, deviceInfo, account,
                    "登录成功", JSON.toJSONString(tokenInfo))
            .setAppKey(DefValConstants.WORKBENCH_APP_KEY)
            .setAppName(DefValConstants.WORKBENCH_APP_NAME);
    SpringUtil.publishEvent(new LoginEvent(dto));
    return loginVo;
}
```

移除 `AuthServiceImpl` 中不再直接使用的 `loginStrategy` 字段，注入 `AccountAuthenticator`；调整现有测试构造参数，但不改变 `loginByWxMa` 的逻辑。

- [ ] **Step 5: 运行认证回归测试**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=AccountAuthenticatorTest,AuthServiceImplLoginTest,AuthServiceImplFindOrgTest,WxMaAuthServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 新测试及现有组织解析、微信小程序测试全部通过；普通登录请求和返回字段不变。

---

### Task 3: Redis 一次性票据服务

**Files:**
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/enumeration/SocialActionEnum.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/enumeration/SocialReauthMethodEnum.java`
- Create: `mdp-apps/md-public/md-cache-key/src/main/java/top/mddata/common/cache/workbench/SocialStateCacheKeyBuilder.java`
- Create: `mdp-apps/md-public/md-cache-key/src/main/java/top/mddata/common/cache/workbench/SocialPendingCacheKeyBuilder.java`
- Create: `mdp-apps/md-public/md-cache-key/src/main/java/top/mddata/common/cache/workbench/SocialReauthCacheKeyBuilder.java`
- Modify: `mdp-apps/md-public/md-cache-key/src/main/java/top/mddata/common/cache/CacheKeyTable.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/model/SocialState.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/model/PendingSocialUser.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/model/SocialReauth.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/SocialTicketService.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/impl/SocialTicketServiceImpl.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/social/impl/SocialTicketServiceImplTest.java`

**Interfaces:**
- Produces: `createState/consumeState`、`createPending/getPending/consumePending/registerPendingFailure`、`createReauth/getReauth/consumeReauth`。
- `SocialState` 字段：`provider/action/userId/requestedAction/requestedProvider/reauthTicketId`。
- `SocialReauth` 字段：`userId/action/provider/method`。

- [ ] **Step 1: 编写票据过期、重放和用途隔离测试**

```java
@Test
void state只能消费一次() {
    SocialState state = SocialState.builder()
            .provider(SocialProviderEnum.QQ)
            .action(SocialActionEnum.LOGIN)
            .build();
    when(cacheOps.get(any(CacheKey.class))).thenReturn(new CacheResult<>("key", state));
    when(cacheOps.setIfAbsent(any(CacheKey.class), any())).thenReturn(true, false);

    assertSame(state, ticketService.consumeState("state-1",
            SocialProviderEnum.QQ, SocialActionEnum.LOGIN));
    assertThrows(BizException.class, () -> ticketService.consumeState("state-1",
            SocialProviderEnum.QQ, SocialActionEnum.LOGIN));
}

@Test
void 二次验证票据不能跨动作使用() {
    SocialReauth reauth = SocialReauth.builder().userId(1L)
            .action(SocialActionEnum.BIND)
            .provider(SocialProviderEnum.QQ)
            .method(SocialReauthMethodEnum.PASSWORD)
            .build();
    when(cacheOps.get(any(CacheKey.class))).thenReturn(new CacheResult<>("key", reauth));

    assertThrows(BizException.class, () -> ticketService.getReauth("ticket-1", 1L,
            SocialActionEnum.UNBIND, SocialProviderEnum.QQ));
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=SocialTicketServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 新票据类型和实现不存在导致编译失败。

- [ ] **Step 3: 实现缓存键和安全随机票据**

在 `CacheKeyTable.Workbench` 增加：

```java
String SOCIAL_STATE = "social.state";
String SOCIAL_PENDING = "social.pending";
String SOCIAL_REAUTH = "social.reauth";
```

三个 Builder 分别返回 10 分钟、10 分钟、5 分钟 `Duration`。`SocialState`、`PendingSocialUser`、`SocialReauth` 均实现 `Serializable`，并统一使用 `@Data`、`@Builder`、`@NoArgsConstructor`、`@AllArgsConstructor`、`@Accessors(chain = true)`，同时满足缓存反序列化和测试构造需要。消费键使用 `key("consumed", ticket)`，失败计数键使用 `key("failed", ticket)`。票据生成统一使用：

```java
private String newTicket() {
    byte[] bytes = new byte[32];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
}
```

`SECURE_RANDOM` 为 `private static final SecureRandom`，禁止使用固定 state 或普通时间戳。

- [ ] **Step 4: 实现用途校验和原子消费**

```java
@Override
public SocialState consumeState(String ticket, SocialProviderEnum provider,
                                SocialActionEnum action) {
    CacheKey key = stateBuilder.key(ticket);
    SocialState state = cacheOps.get(key).getValue();
    ArgumentAssert.notNull(state, "授权状态已失效，请重新发起");
    ArgumentAssert.isTrue(state.getProvider() == provider && state.getAction() == action,
            "授权状态与当前操作不匹配");
    ArgumentAssert.isTrue(cacheOps.setIfAbsent(stateBuilder.key("consumed", ticket), true),
            "授权状态已使用，请重新发起");
    return state;
}

@Override
public SocialReauth getReauth(String ticket, Long userId, SocialActionEnum action,
                              SocialProviderEnum provider) {
    SocialReauth reauth = cacheOps.get(reauthBuilder.key(ticket)).getValue();
    ArgumentAssert.notNull(reauth, "身份验证已过期，请重新验证");
    ArgumentAssert.isFalse(cacheOps.exists(reauthBuilder.key("consumed", ticket)),
            "身份验证票据已使用");
    ArgumentAssert.isTrue(reauth.getUserId().equals(userId)
                    && reauth.getAction() == action && reauth.getProvider() == provider,
            "身份验证票据与当前操作不匹配");
    return reauth;
}

@Override
public void consumeReauth(String ticket) {
    getCacheTicket(ticket);
    ArgumentAssert.isTrue(cacheOps.setIfAbsent(reauthBuilder.key("consumed", ticket), true),
            "身份验证票据已使用");
}
```

`getCacheTicket` 只负责读取并断言票据存在；`getPending` 同样检查消费标记。`registerPendingFailure` 使用 `CacheOps.incr`，达到 5 次时删除待选择票据和失败计数，不删除任何用户数据。

- [ ] **Step 5: 运行测试并检查无原生 Redis 操作**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=SocialTicketServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 所有票据测试通过；实现只依赖 `CacheOps`，没有 `get` 后 `del` 的伪一次性消费。

---

### Task 4: JustAuth 配置与三平台适配

**Files:**
- Modify: `mdp-apps/md-workbench/workbench-service/pom.xml`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/config/SocialLoginProperties.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/SocialProvider.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/JustAuthRequestFactory.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/SocialProviderFactory.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/impl/JustAuthSocialProvider.java`
- Create: `docs/social-login.md`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/social/impl/JustAuthSocialProviderTest.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/social/SocialProviderFactoryTest.java`

**Interfaces:**
- Produces: `String SocialProvider.authorize(String state)`。
- Produces: `SocialIdentity SocialProvider.exchange(String code, String state)`。
- Produces: `SocialProviderFactory.get(SocialProviderEnum)` 和 `enabledProviders()`。
- Consumes: Task 1 的 `SocialIdentity` 和平台枚举。

- [ ] **Step 1: 编写 OpenID 归一化失败测试**

```java
@Test
void QQ启用UnionId时仍以tokenOpenId作为应用身份() {
    AuthUser user = AuthUser.builder()
            .uuid("union-id")
            .nickname("测试用户")
            .token(AuthToken.builder().openId("open-id").unionId("union-id").build())
            .build();
    AuthResponse<AuthUser> response = AuthResponse.<AuthUser>builder()
            .code(AuthResponseStatus.SUCCESS.getCode()).data(user).build();
    when(authRequest.login(any(AuthCallback.class))).thenReturn(response);

    SocialIdentity identity = provider.exchange("code", "state");

    assertEquals("open-id", identity.getOpenId());
    assertEquals("union-id", identity.getUnionId());
    assertEquals("client-id", identity.getClientId());
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=JustAuthSocialProviderTest,SocialProviderFactoryTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: JustAuth 依赖或适配器类型缺失导致失败。

- [ ] **Step 3: 添加依赖和强类型配置**

```xml
<dependency>
    <groupId>me.zhyd.oauth</groupId>
    <artifactId>JustAuth</artifactId>
</dependency>
```

`SocialLoginProperties` 使用 `@Component`、`@ConfigurationProperties(prefix = "mdp.social")` 和 `@RefreshScope`，包含 `wechatOpen`、`qq`、`dingTalk` 三个 `ProviderConfig`。每个配置字段为：

```java
@Data
public static class ProviderConfig {
    private boolean enabled;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String corpId;
    private boolean unionId;
    private int httpTimeoutMillis = 10_000;

    public boolean isComplete() {
        return StrUtil.isNoneBlank(clientId, clientSecret, redirectUri);
    }
}
```

在 `docs/social-login.md` 只记录环境变量名称和回调格式，例如 `${WECHAT_OPEN_CLIENT_ID}`，不写真实值，不要求读取现有中间件配置。

- [ ] **Step 4: 实现 JustAuth 请求工厂**

```java
public AuthRequest create(SocialProviderEnum provider,
                          SocialLoginProperties.ProviderConfig config) {
    HttpConfig httpConfig = new HttpConfig();
    httpConfig.setTimeout(config.getHttpTimeoutMillis());
    AuthConfig authConfig = AuthConfig.builder()
            .clientId(config.getClientId())
            .clientSecret(config.getClientSecret())
            .redirectUri(config.getRedirectUri())
            .unionId(config.isUnionId())
            .dingTalkCorpId(config.getCorpId())
            .httpConfig(httpConfig)
            .ignoreCheckState(true)
            .build();
    return switch (provider) {
        case WECHAT_OPEN -> new AuthWeChatOpenRequest(authConfig);
        case QQ -> new AuthQqRequest(authConfig);
        case DINGTALK -> new AuthDingTalkV2Request(authConfig);
    };
}
```

`ignoreCheckState(true)` 仅因为 Task 3 已在服务端完成独立 state 校验；不得删除 Task 3 校验。钉钉固定使用 JustAuth 1.16.7 的 `AuthDingTalkV2Request`，对应新版授权和用户接口；`corpId` 按企业应用情况可选配置，不使用旧 `AuthDingTalkRequest`。

- [ ] **Step 5: 实现身份交换与平台工厂**

```java
@Override
public SocialIdentity exchange(String code, String state) {
    AuthResponse<AuthUser> response = authRequest.login(AuthCallback.builder()
            .code(code).state(state).build());
    if (!response.ok() || response.getData() == null) {
        log.error("第三方授权失败，provider={}，responseCode={}", provider, response.getCode());
        throw new BizException("第三方授权失败，请重新授权");
    }
    AuthUser user = response.getData();
    String openId = user.getToken() == null ? null : user.getToken().getOpenId();
    String unionId = user.getToken() == null ? null : user.getToken().getUnionId();
    ArgumentAssert.isTrue(StrUtil.isNotBlank(openId), "第三方未返回有效用户标识");
    return SocialIdentity.builder()
            .provider(provider)
            .clientId(config.getClientId())
            .openId(openId)
            .unionId(unionId)
            .nickname(StrUtil.blankToDefault(user.getNickname(), provider.getLabel()))
            .build();
}
```

`SocialProviderFactory` 构造时校验所有 `enabled=true` 的配置完整；`enabledProviders()` 只返回完整启用项；每次 `get()` 创建新的适配器和 JustAuth 请求对象，避免共享用户授权状态。

- [ ] **Step 6: 运行适配器测试**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=JustAuthSocialProviderTest,SocialProviderFactoryTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 微信、QQ、钉钉请求类型映射正确；QQ 测试证明 OpenID 与 UnionID 不混用；配置和日志测试不包含密钥。

---

### Task 5: 已绑定登录与未绑定待选择

**Files:**
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/dto/SocialCallbackDto.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/vo/SocialProviderVo.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/vo/SocialLoginResultVo.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/SocialLoginService.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/impl/SocialLoginServiceImpl.java`
- Create: `mdp-apps/md-workbench/workbench-web/src/main/java/top/mddata/workbench/controller/SocialAuthController.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/social/impl/SocialLoginServiceImplTest.java`

**Interfaces:**
- Consumes: Task 1 至 Task 4 的持久化、认证、票据和平台接口。
- Produces: `List<SocialProviderVo> providers()`、`String loginUrl(SocialProviderEnum)`、`SocialLoginResultVo login(SocialCallbackDto)`。
- `SocialLoginResultVo.status` 只使用 `SUCCESS` 或 `PENDING`。

- [ ] **Step 1: 编写已绑定和未绑定两条失败测试**

```java
@Test
void 已绑定身份直接登录原用户() {
    SocialIdentity identity = identity();
    UserSocialAuth binding = new UserSocialAuth().setUserId(7L);
    User user = new User().setId(7L).setUsername("existing");
    when(ticketService.consumeState(anyString(), any(), any())).thenReturn(loginState());
    when(providerFactory.get(SocialProviderEnum.QQ)).thenReturn(provider);
    when(provider.exchange("code", "state")).thenReturn(identity);
    when(userSocialAuthService.findByIdentity(identity)).thenReturn(Optional.of(binding));
    when(ssoUserService.getById(7L)).thenReturn(user);
    when(loginSessionService.loginAuthenticated(any(), any(), any(), any(), any(), any()))
            .thenReturn(new LoginVo());

    SocialLoginResultVo result = service.login(callback());

    assertEquals("SUCCESS", result.getStatus());
    verify(accountAuthenticator).checkUserState(eq(user), eq(AuthTypeEnum.QQ), any());
    verify(ticketService, never()).createPending(any());
}

@Test
void 未绑定身份只签发待选择票据不创建用户() {
    when(userSocialAuthService.findByIdentity(any())).thenReturn(Optional.empty());
    when(provider.exchange(any(), any())).thenReturn(identity());
    when(ticketService.createPending(any())).thenReturn("pending-ticket");

    SocialLoginResultVo result = service.login(callback());

    assertEquals("PENDING", result.getStatus());
    assertEquals("pending-ticket", result.getPendingTicket());
    verifyNoInteractions(loginSessionService);
}

private SocialIdentity identity() {
    return SocialIdentity.builder().provider(SocialProviderEnum.QQ)
            .clientId("client-id").openId("open-id").nickname("测试用户").build();
}

private SocialState loginState() {
    return SocialState.builder().provider(SocialProviderEnum.QQ)
            .action(SocialActionEnum.LOGIN).build();
}

private SocialCallbackDto callback() {
    SocialCallbackDto callback = new SocialCallbackDto();
    callback.setProvider(SocialProviderEnum.QQ);
    callback.setCode("code");
    callback.setState("state");
    return callback;
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=SocialLoginServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 登录服务和返回模型不存在导致编译失败。

- [ ] **Step 3: 实现登录编排**

```java
@Override
public SocialLoginResultVo login(SocialCallbackDto callback) {
    ticketService.consumeState(callback.getState(), callback.getProvider(),
            SocialActionEnum.LOGIN);
    SocialIdentity identity = providerFactory.get(callback.getProvider())
            .exchange(callback.getCode(), callback.getState());
    return userSocialAuthService.findByIdentity(identity)
            .map(binding -> loginBoundUser(binding, identity, callback))
            .orElseGet(() -> pendingUser(identity));
}

private SocialLoginResultVo loginBoundUser(UserSocialAuth binding, SocialIdentity identity,
                                           SocialCallbackDto callback) {
    User user = ssoUserService.getById(binding.getUserId());
    accountAuthenticator.checkUserState(user, authType(identity.getProvider()),
            callback.getDeviceInfo());
    LoginVo loginVo = loginSessionService.loginAuthenticated(user,
            authType(identity.getProvider()), user.getUsername(), callback.getDeviceId(),
            callback.getDeviceInfo(), "PC");
    return SocialLoginResultVo.success(loginVo);
}

private SocialLoginResultVo pendingUser(SocialIdentity identity) {
    String ticket = ticketService.createPending(identity);
    return SocialLoginResultVo.pending(ticket, identity.getProvider().getCode(),
            identity.getNickname());
}
```

`authType` 用 `switch` 显式映射三个平台，遇到未知平台直接抛出业务异常。`SocialLoginResultVo` 字段为 `status/login/pendingTicket/provider/nickname`，并使用全参构造器实现以下静态工厂，公开响应不包含身份三元组：

```java
public static SocialLoginResultVo success(LoginVo login) {
    return new SocialLoginResultVo("SUCCESS", login, null, null, null);
}

public static SocialLoginResultVo pending(String ticket, String provider, String nickname) {
    return new SocialLoginResultVo("PENDING", null, ticket, provider, nickname);
}
```

- [ ] **Step 4: 添加公开控制器和参数校验**

```java
@GetMapping("/providers")
public R<List<SocialProviderVo>> providers() {
    return R.success(socialLoginService.providers());
}

@GetMapping("/{provider}/loginUrl")
public R<String> loginUrl(@PathVariable String provider) {
    return R.success(socialLoginService.loginUrl(SocialProviderEnum.fromCode(provider)));
}

@PostMapping("/login")
@RequestLog(value = "第三方账号登录", logType = RequestLog.LogType.OTHER, request = false)
public R<SocialLoginResultVo> login(@Validated @RequestBody SocialCallbackDto callback) {
    return R.success(socialLoginService.login(callback));
}
```

类路径为 `/anyUser/social`。`SocialCallbackDto` 的 `provider/code/state` 必填，`deviceId/deviceInfo` 可选；授权码和 state 不写请求日志。

- [ ] **Step 5: 运行测试**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=SocialLoginServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 已绑定登录、禁用用户、未绑定待选择、错误平台和回调异常路径均通过；未绑定路径没有用户写入。

---

### Task 6: 绑定已有账号与社交注册事务

**Files:**
- Create: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/dto/UserSocialRegisterDto.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/dto/SocialBindExistingDto.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/dto/SocialRegisterDto.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/SocialOnboardingService.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/impl/SocialOnboardingServiceImpl.java`
- Create: `mdp-apps/md-workbench/workbench-web/src/main/java/top/mddata/workbench/controller/SocialOnboardingController.java`
- Modify: `mdp-apps/md-console/console-facade/console-api/src/main/java/top/mddata/console/facade/organization/UserFacade.java`
- Modify: `mdp-apps/md-console/console-facade/console-boot-impl/src/main/java/top/mddata/console/facade/impl/organization/UserFacadeImpl.java`
- Modify: `mdp-apps/md-console/console-facade/console-cloud-impl/src/main/java/top/mddata/console/facade/api/organization/UserApi.java`
- Modify: `mdp-apps/md-console/console-facade/console-cloud-impl/src/main/java/top/mddata/console/facade/impl/organization/UserFacadeImpl.java`
- Modify: `mdp-apps/md-console/console-facade/console-cloud-impl/src/main/java/top/mddata/console/facade/api/organization/fallback/UserApiFallback.java`
- Modify: `mdp-apps/md-console/console-web/src/main/java/top/mddata/console/controller/inner/InnerUserController.java`
- Modify: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/organization/UserService.java`
- Modify: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/organization/impl/UserServiceImpl.java`
- Modify: `mdp-apps/md-console/console-service/src/test/java/top/mddata/console/service/organization/impl/UserServiceImplContactTest.java`
- Test: `mdp-apps/md-console/console-service/src/test/java/top/mddata/console/service/organization/impl/UserServiceImplSocialRegisterTest.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/social/impl/SocialOnboardingServiceImplTest.java`

**Interfaces:**
- Produces: `LoginVo bindExisting(SocialBindExistingDto)` 和 `LoginVo register(SocialRegisterDto)`。
- Produces: `Long UserFacade.registerBySocial(UserSocialRegisterDto)`，单体和微服务都返回新用户 ID，供自动登录使用。
- Produces: `boolean UserService.registerBySocial(User user, UserSocialAuth socialAuth)`。

- [ ] **Step 1: 编写首次选择行为失败测试**

```java
@Test
void 已有账号凭据错误累计失败且不绑定() {
    when(accountAuthenticator.authenticate(any())).thenThrow(new BizException("验证失败"));

    assertThrows(BizException.class, () -> service.bindExisting(bindDto()));
    verify(ticketService).registerPendingFailure("pending-ticket");
    verify(userSocialAuthService, never()).bind(any(), any());
}

@Test
void 社交注册不强制联系方式且密码使用随机值() {
    SocialRegisterDto dto = registerDto();
    when(ticketService.getPending(dto.getPendingTicket()))
            .thenReturn(new PendingSocialUser(identity()));
    when(userFacade.registerBySocial(any())).thenReturn(99L);

    service.register(dto);

    ArgumentCaptor<UserSocialRegisterDto> captor =
            ArgumentCaptor.forClass(UserSocialRegisterDto.class);
    verify(userFacade).registerBySocial(captor.capture());
    User user = captor.getValue().getUser();
    assertNull(user.getPhone());
    assertNull(user.getEmail());
    assertNotNull(user.getPassword());
    assertNotEquals("123456", user.getPassword());
    assertFalse(user.getUsername().contains(identity().getOpenId()));
    assertEquals(99L, user.getId());
}

private SocialBindExistingDto bindDto() {
    SocialBindExistingDto dto = new SocialBindExistingDto();
    dto.setPendingTicket("pending-ticket");
    dto.setLogin(new LoginDto());
    return dto;
}

private SocialRegisterDto registerDto() {
    SocialRegisterDto dto = new SocialRegisterDto();
    dto.setPendingTicket("pending-ticket");
    dto.setName("测试用户");
    dto.setNature(OrgNatureEnum.HEAD_COMPANY.getCode());
    dto.setAgreement(true);
    return dto;
}

private SocialIdentity identity() {
    return SocialIdentity.builder().provider(SocialProviderEnum.QQ)
            .clientId("client-id").openId("open-id").nickname("测试用户").build();
}
```

控制台测试还必须验证：`userSocialAuthMapper.insert` 抛出异常时，不调用用户新增事件；`socialAuth.userId` 使用新生成的用户 ID。

- [ ] **Step 2: 运行两组测试并确认失败**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-console/console-service,mdp-apps/md-console/console-facade/console-boot-impl,mdp-apps/md-console/console-facade/console-cloud-impl,mdp-apps/md-workbench/workbench-service -am -Dtest=UserServiceImplSocialRegisterTest,SocialOnboardingServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 新注册 DTO、Facade 方法和服务不存在导致编译失败。

- [ ] **Step 3: 实现绑定已有账号**

```java
@Override
public LoginVo bindExisting(SocialBindExistingDto dto) {
    User target;
    try {
        target = accountAuthenticator.authenticate(dto.getLogin());
    } catch (BizException ex) {
        ticketService.registerPendingFailure(dto.getPendingTicket());
        throw ex;
    }
    PendingSocialUser pending = ticketService.getPending(dto.getPendingTicket());
    SocialIdentity identity = pending.getIdentity();
    ArgumentAssert.isTrue(userSocialAuthService
                    .findByUserAndProvider(target.getId(), identity.getProvider()).isEmpty(),
            "目标账号已绑定该平台，请先解绑");
    ArgumentAssert.isTrue(userSocialAuthService.findByIdentity(identity).isEmpty(),
            "该第三方账号已绑定其他用户");
    ticketService.consumePending(dto.getPendingTicket());
    userSocialAuthService.bind(target.getId(), identity);
    return loginSessionService.loginAuthenticated(target, authType(identity.getProvider()),
            target.getUsername(), dto.getLogin().getDeviceId(),
            dto.getLogin().getDeviceInfo(), "PC");
}
```

`SocialBindExistingDto` 包含非空 `pendingTicket` 和 `@Valid @NotNull LoginDto login`。只允许 Task 2 支持的账号认证类型。

- [ ] **Step 4: 实现控制台本地事务注册**

`UserSocialRegisterDto` 使用 `@Data`、`@NoArgsConstructor`、`@AllArgsConstructor`，明确包含 `User user` 和 `UserSocialAuth socialAuth`。在 `UserServiceImpl` 抽取私有 `registerUser(User user, String displayName)`，普通用户名注册传入用户名，社交注册传入用户填写姓名；私有方法负责现有初始化、人员编号、默认组织和默认角色，但不发布事件。

```java
@Override
@Transactional(rollbackFor = Exception.class)
public boolean registerBySocial(User user, UserSocialAuth socialAuth) {
    ArgumentAssert.isTrue(UserIdentityService.checkRegisterNature(user.getNature()),
            "不支持的注册身份：{}", user.getNature());
    ArgumentAssert.isFalse(checkUsername(user.getUsername(), null),
            "用户名：{}已经存在", user.getUsername());
    registerUser(user, user.getName());
    socialAuth.setUserId(user.getId());
    socialAuth.setBoundAt(LocalDateTime.now());
    userSocialAuthMapper.insert(socialAuth);
    publishUserAdded(user);
    return true;
}
```

普通 `registerByUsername` 也调用 `registerUser` 后发布事件，保持原有行为。`initSsoUser` 继续使用平台密码算法，不能把 `userSource` 改为社交平台编码。新增 final Mapper 构造参数后，更新现有测试构造器。

- [ ] **Step 5: 实现社交注册编排和远程失败检查**

```java
@Override
public LoginVo register(SocialRegisterDto dto) {
    ArgumentAssert.isTrue(Boolean.TRUE.equals(dto.getAgreement()), "请先阅读并同意用户协议");
    PendingSocialUser pending = ticketService.getPending(dto.getPendingTicket());
    SocialIdentity identity = pending.getIdentity();
    ticketService.consumePending(dto.getPendingTicket());

    User user = new User();
    user.setUsername("social_" + UUID.randomUUID().toString(true));
    user.setPassword(randomPassword());
    user.setName(StrUtil.blankToDefault(dto.getName(), identity.getNickname()));
    user.setNature(dto.getNature());

    UserSocialAuth binding = new UserSocialAuth()
            .setProvider(identity.getProvider().name())
            .setClientId(identity.getClientId())
            .setOpenId(identity.getOpenId())
            .setUnionId(identity.getUnionId())
            .setNickname(identity.getNickname());
    Long userId = userFacade.registerBySocial(new UserSocialRegisterDto(user, binding));
    ArgumentAssert.notNull(userId, "社交账号注册未返回用户ID");
    user.setId(userId);
    return loginSessionService.loginAuthenticated(user, authType(identity.getProvider()),
            user.getUsername(), dto.getDeviceId(), dto.getDeviceInfo(), "PC");
}
```

`randomPassword()` 使用 `SecureRandom` 生成 32 字节 URL-safe 随机串。单体 Facade 调用服务成功后返回 `user.getId()`；微服务 `UserApi` 和内部控制器统一使用 `R<Long>`，微服务 `UserFacadeImpl` 必须检查成功状态并返回新用户 ID：

```java
R<Long> result = userApi.registerBySocial(register);
ArgumentAssert.isTrue(result.getIsSuccess(), "社交账号注册失败：{}", result.getMsg());
return result.getData();
```

回退实现返回 `R.<Long>timeout()`，不得被当成成功。

- [ ] **Step 6: 添加首次选择控制器并运行测试**

在 `/anyUser/social` 下添加 `POST /bindExisting`、`POST /register`，请求日志关闭密码、票据等入参记录。运行：

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-console/console-service,mdp-apps/md-console/console-facade/console-boot-impl,mdp-apps/md-console/console-facade/console-cloud-impl,mdp-apps/md-workbench/workbench-service -am -Dtest=UserServiceImplSocialRegisterTest,SocialOnboardingServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 绑定已有账号和注册测试通过；注册事件在绑定插入后执行；单体和微服务接口签名一致。

---

### Task 7: 二次验证与个人中心绑定

**Files:**
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/dto/SocialReauthDto.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/dto/SocialReauthUrlDto.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/dto/SocialTicketDto.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/vo/SocialBindingVo.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/vo/SocialReauthTicketVo.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/SocialAccountService.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/impl/SocialAccountServiceImpl.java`
- Create: `mdp-apps/md-workbench/workbench-web/src/main/java/top/mddata/workbench/controller/ProfileSocialController.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/social/impl/SocialAccountServiceImplTest.java`

**Interfaces:**
- Produces: `list/reauth/reauthUrl/reauthByCallback/bindUrl/bind`。
- Consumes: `ContextUtil.getUserId()`、Task 3 的用途隔离票据、Task 4 的身份交换。

- [ ] **Step 1: 编写跨用户和第三方重新授权失败测试**

```java
@Test
void 账号凭据属于其他用户时不能签发当前用户票据() {
    User other = new User().setId(2L);
    when(accountAuthenticator.authenticate(any())).thenReturn(other);

    assertThrows(BizException.class, () -> service.reauth(1L, reauthDto()));
    verify(ticketService, never()).createReauth(any());
}

@Test
void 第三方重新授权身份必须等于当前已有绑定() {
    UserSocialAuth current = new UserSocialAuth().setUserId(1L)
            .setClientId("client-a").setOpenId("open-a");
    SocialIdentity scanned = identity().setClientId("client-b").setOpenId("open-b");
    when(userSocialAuthService.findByUserAndProvider(1L, SocialProviderEnum.QQ))
            .thenReturn(Optional.of(current));
    when(provider.exchange(any(), any())).thenReturn(scanned);

    assertThrows(BizException.class, () -> service.reauthByCallback(1L, callback()));
    verify(ticketService, never()).createReauth(any());
}

private SocialReauthDto reauthDto() {
    SocialReauthDto dto = new SocialReauthDto();
    dto.setAction(SocialActionEnum.BIND);
    dto.setProvider(SocialProviderEnum.QQ);
    dto.setLogin(new LoginDto());
    return dto;
}

private SocialIdentity identity() {
    return SocialIdentity.builder().provider(SocialProviderEnum.QQ)
            .clientId("client-a").openId("open-a").build();
}

private SocialCallbackDto callback() {
    SocialCallbackDto dto = new SocialCallbackDto();
    dto.setProvider(SocialProviderEnum.QQ);
    dto.setCode("code");
    dto.setState("state");
    return dto;
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=SocialAccountServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 个人中心账号服务和 DTO 不存在导致编译失败。

- [ ] **Step 3: 实现 MDP 凭据二次验证**

```java
@Override
public SocialReauthTicketVo reauth(Long currentUserId, SocialReauthDto dto) {
    User verified = accountAuthenticator.authenticate(dto.getLogin());
    ArgumentAssert.isTrue(currentUserId.equals(verified.getId()),
            "验证账号与当前登录账号不一致");
    SocialReauth reauth = SocialReauth.builder()
            .userId(currentUserId)
            .action(dto.getAction())
            .provider(dto.getProvider())
            .method(reauthMethod(dto.getLogin().getAuthType()))
            .build();
    return new SocialReauthTicketVo(ticketService.createReauth(reauth));
}
```

`reauthMethod` 将 `CAPTCHA/USERNAME` 映射为 `PASSWORD`，`PHONE` 映射为 `PHONE`，`EMAIL` 映射为 `EMAIL`，其他类型快速失败。

- [ ] **Step 4: 实现第三方重新授权**

生成授权地址前，必须确认所选平台已绑定当前用户。state 使用 `action=REAUTH`，保存 `userId/requestedAction/requestedProvider`。回调时：

```java
SocialState state = ticketService.consumeState(callback.getState(),
        callback.getProvider(), SocialActionEnum.REAUTH);
ArgumentAssert.isTrue(currentUserId.equals(state.getUserId()), "授权用户已变化，请重新操作");
SocialIdentity identity = providerFactory.get(callback.getProvider())
        .exchange(callback.getCode(), callback.getState());
UserSocialAuth binding = userSocialAuthService.findByUserAndProvider(
                currentUserId, callback.getProvider())
        .orElseThrow(() -> new BizException("当前账号未绑定该平台"));
ArgumentAssert.isTrue(Objects.equals(binding.getClientId(), identity.getClientId())
                && Objects.equals(binding.getOpenId(), identity.getOpenId()),
        "扫码身份与当前绑定账号不一致");
SocialReauth reauth = SocialReauth.builder()
        .userId(currentUserId)
        .action(state.getRequestedAction())
        .provider(state.getRequestedProvider())
        .method(SocialReauthMethodEnum.SOCIAL)
        .build();
return new SocialReauthTicketVo(ticketService.createReauth(reauth));
```

- [ ] **Step 5: 实现绑定地址和绑定完成**

`bindUrl` 使用 `getReauth(ticket, currentUserId, BIND, targetProvider)` 验证后，创建 `action=BIND` 的 state，并保存 `reauthTicketId`。`bind` 的处理顺序为：

```java
SocialState state = ticketService.consumeState(callback.getState(),
        callback.getProvider(), SocialActionEnum.BIND);
ArgumentAssert.isTrue(currentUserId.equals(state.getUserId()), "授权用户已变化，请重新操作");
ticketService.getReauth(state.getReauthTicketId(), currentUserId,
        SocialActionEnum.BIND, callback.getProvider());
SocialIdentity identity = providerFactory.get(callback.getProvider())
        .exchange(callback.getCode(), callback.getState());
ticketService.consumeReauth(state.getReauthTicketId());
userSocialAuthService.bind(currentUserId, identity);
```

`list` 批量查询当前用户全部绑定后在内存按平台分组，不得在平台循环中逐条查库；只返回 `provider/bound/nickname/boundAt`。

- [ ] **Step 6: 添加个人中心控制器并运行测试**

实现 `/profile/social/list`、`/reauth`、`/{provider}/reauthUrl`、`/reauthByCallback`、`/{provider}/bindUrl`、`/bind`。控制器从 `ContextUtil.getUserId()` 获取用户，不接受前端传用户 ID。运行：

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=SocialAccountServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 跨用户票据、错误动作、错误扫码身份和正常绑定测试通过；控制器无业务逻辑。

---

### Task 8: 解绑与最后登录方式保护

**Files:**
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/dto/SocialUnbindDto.java`
- Modify: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/SocialAccountService.java`
- Modify: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/impl/SocialAccountServiceImpl.java`
- Modify: `mdp-apps/md-workbench/workbench-web/src/main/java/top/mddata/workbench/controller/ProfileSocialController.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/social/impl/SocialAccountServiceImplTest.java`

**Interfaces:**
- Produces: `void unbind(Long currentUserId, SocialUnbindDto dto)`。
- Consumes: `SocialReauth.method` 和 `UserSocialAuthService.countByUser`。

- [ ] **Step 1: 编写最后登录方式失败测试**

```java
private final UserSocialAuth binding = new UserSocialAuth()
        .setId(9L).setUserId(1L).setProvider(SocialProviderEnum.QQ.name());

@BeforeEach
void mockBinding() {
    when(userSocialAuthService.findByUserAndProvider(1L, SocialProviderEnum.QQ))
            .thenReturn(Optional.of(binding));
}

@Test
void 第三方验证后解绑最后一个身份必须拒绝() {
    SocialReauth reauth = SocialReauth.builder().userId(1L)
            .action(SocialActionEnum.UNBIND)
            .provider(SocialProviderEnum.QQ)
            .method(SocialReauthMethodEnum.SOCIAL).build();
    when(ticketService.getReauth("ticket", 1L,
            SocialActionEnum.UNBIND, SocialProviderEnum.QQ)).thenReturn(reauth);
    when(userSocialAuthService.countByUser(1L)).thenReturn(1L);

    assertThrows(BizException.class, () -> service.unbind(1L, unbindDto()));
    verify(userSocialAuthService, never()).unbind(any(), any());
}

@Test
void 已验证密码时允许解绑最后一个第三方身份() {
    SocialReauth reauth = reauth(SocialReauthMethodEnum.PASSWORD);
    when(ticketService.getReauth(any(), any(), any(), any())).thenReturn(reauth);

    service.unbind(1L, unbindDto());

    verify(userSocialAuthService).unbind(binding.getId(), 1L);
}

private SocialReauth reauth(SocialReauthMethodEnum method) {
    return SocialReauth.builder().userId(1L)
            .action(SocialActionEnum.UNBIND)
            .provider(SocialProviderEnum.QQ)
            .method(method)
            .build();
}

private SocialUnbindDto unbindDto() {
    SocialUnbindDto dto = new SocialUnbindDto();
    dto.setProvider(SocialProviderEnum.QQ);
    dto.setReauthTicket("ticket");
    return dto;
}
```

- [ ] **Step 2: 运行新增测试并确认失败**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=SocialAccountServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: `unbind` 方法缺失或最后登录方式断言失败。

- [ ] **Step 3: 实现解绑保护**

```java
@Override
public void unbind(Long currentUserId, SocialUnbindDto dto) {
    SocialReauth reauth = ticketService.getReauth(dto.getReauthTicket(), currentUserId,
            SocialActionEnum.UNBIND, dto.getProvider());
    UserSocialAuth binding = userSocialAuthService.findByUserAndProvider(
                    currentUserId, dto.getProvider())
            .orElseThrow(() -> new BizException("当前账号未绑定该平台"));
    if (reauth.getMethod() == SocialReauthMethodEnum.SOCIAL) {
        ArgumentAssert.isTrue(userSocialAuthService.countByUser(currentUserId) > 1,
                "解绑后将无法登录，请先补充联系方式或绑定其他平台");
    }
    ticketService.consumeReauth(dto.getReauthTicket());
    userSocialAuthService.unbind(binding.getId(), currentUserId);
}
```

密码、手机、邮箱验证成功分别证明密码登录、短信登录或邮箱找回能力仍可用，不需要额外第三方数量。

- [ ] **Step 4: 添加接口并运行测试**

`POST /profile/social/unbind` 使用 `@RequestLog(value = "解绑第三方账号", logType = UPDATE, request = false)`。运行：

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=SocialAccountServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 四种验证方式、最后身份保护、票据重放和解绑成功路径全部通过。

---

### Task 9: 误注册后的跨账号安全转移

**Files:**
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/dto/SocialTransferDto.java`
- Modify: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/SocialAccountService.java`
- Modify: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/social/impl/SocialAccountServiceImpl.java`
- Modify: `mdp-apps/md-workbench/workbench-web/src/main/java/top/mddata/workbench/controller/ProfileSocialController.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/social/impl/SocialAccountServiceImplTest.java`

**Interfaces:**
- Produces: `LoginVo transfer(Long currentUserId, SocialTransferDto dto)`。
- Consumes: Task 1 条件转移、Task 2 目标账号验证和会话签发。

- [ ] **Step 1: 编写双方验证与不迁移数据测试**

```java
private final UserSocialAuth binding = new UserSocialAuth()
        .setId(9L).setUserId(1L).setProvider(SocialProviderEnum.QQ.name());

@BeforeEach
void mockTransferBinding() {
    when(userSocialAuthService.findByUserAndProvider(1L, SocialProviderEnum.QQ))
            .thenReturn(Optional.of(binding));
}

@Test
void 目标账号已绑定同平台时拒绝转移() {
    User target = new User().setId(2L).setState(true);
    when(accountAuthenticator.authenticate(any())).thenReturn(target);
    when(userSocialAuthService.findByUserAndProvider(2L, SocialProviderEnum.QQ))
            .thenReturn(Optional.of(new UserSocialAuth()));

    assertThrows(BizException.class, () -> service.transfer(1L, transferDto()));
    verify(userSocialAuthService, never()).transfer(any(), any(), any(), any());
}

@Test
void 转移只修改绑定并切换到目标账号() {
    when(accountAuthenticator.authenticate(any())).thenReturn(targetUser());

    service.transfer(1L, transferDto());

    verify(userSocialAuthService).transfer(binding.getId(), 1L, 2L, 1L);
    verify(ssoUserService, never()).updateById(any());
    verify(loginSessionService).loginAuthenticated(eq(targetUser()),
            eq(AuthTypeEnum.QQ), any(), any(), any(), eq("PC"));
}

private User targetUser() {
    return new User().setId(2L).setUsername("target").setState(true);
}

private SocialTransferDto transferDto() {
    SocialTransferDto dto = new SocialTransferDto();
    dto.setProvider(SocialProviderEnum.QQ);
    dto.setReauthTicket("ticket");
    dto.setTargetLogin(new LoginDto());
    return dto;
}
```

- [ ] **Step 2: 运行测试并确认失败**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=SocialAccountServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: `transfer` 方法缺失或交互断言失败。

- [ ] **Step 3: 实现转移编排**

```java
@Override
public LoginVo transfer(Long currentUserId, SocialTransferDto dto) {
    ticketService.getReauth(dto.getReauthTicket(), currentUserId,
            SocialActionEnum.TRANSFER, dto.getProvider());
    UserSocialAuth binding = userSocialAuthService.findByUserAndProvider(
                    currentUserId, dto.getProvider())
            .orElseThrow(() -> new BizException("当前账号未绑定该平台"));
    User target = accountAuthenticator.authenticate(dto.getTargetLogin());
    ArgumentAssert.isFalse(currentUserId.equals(target.getId()), "目标账号不能与当前账号相同");
    ArgumentAssert.isTrue(userSocialAuthService.findByUserAndProvider(
                    target.getId(), dto.getProvider()).isEmpty(),
            "目标账号已绑定该平台");

    ticketService.consumeReauth(dto.getReauthTicket());
    userSocialAuthService.transfer(binding.getId(), currentUserId, target.getId(), currentUserId);
    StpUtil.logout();
    return loginSessionService.loginAuthenticated(target, authType(dto.getProvider()),
            target.getUsername(), dto.getTargetLogin().getDeviceId(),
            dto.getTargetLogin().getDeviceInfo(), "PC");
}
```

不调用用户资料、组织、角色或消息服务；`SocialTransferDto` 不接收 `userId`，目标身份完全由凭据验证结果确定。

- [ ] **Step 4: 添加接口并运行测试**

添加 `POST /profile/social/transfer`，请求日志不记录双方密码、验证码和票据。运行：

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-workbench/workbench-service -am -Dtest=SocialAccountServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 双方验证、同账号拒绝、目标冲突、源绑定变化及成功切换测试通过。

---

### Task 10: 前端公开 API 与统一登录完成逻辑

**Files:**
- Create: `apps/web-workbench/src/api/common/model/social.ts`
- Create: `apps/web-workbench/src/api/common/social.ts`
- Modify: `apps/web-workbench/src/store/auth.ts`
- Create: `apps/web-workbench/src/views/_core/sso/data/social.ts`
- Test: `apps/web-workbench/src/views/_core/sso/data/social.test.ts`

**Interfaces:**
- Produces: `SocialApi.providers/loginUrl/login/bindExisting/register`。
- Produces: `authStore.completeLogin(result, onSuccess?)`。
- Produces: `readCallbackQuery/saveSocialIntent/takeSocialIntent/safeInternalPath`，以及复用 `useDeviceInfo` 的 `withDeviceInfo/deviceFields`。

- [ ] **Step 1: 编写回调参数和内部路径失败测试**

```ts
import { describe, expect, it } from 'vitest';
import { safeInternalPath } from './social';

describe('第三方登录上下文', () => {
  it('只允许站内绝对路径', () => {
    expect(safeInternalPath('/workbench/user/security')).toBe(
      '/workbench/user/security',
    );
    expect(safeInternalPath('https://evil.example')).toBe('/');
    expect(safeInternalPath('//evil.example')).toBe('/');
  });
});
```

- [ ] **Step 2: 运行测试并确认失败**

Run:

```bash
pnpm --dir /Users/tangyh/gitee/mdp-vben exec vitest run --dom apps/web-workbench/src/views/_core/sso/data/social.test.ts
```

Expected: `social.ts` 不存在导致测试失败。

- [ ] **Step 3: 实现公开类型和 API**

```ts
export type SocialProvider = 'dingtalk' | 'qq' | 'wechat-open';

export interface SocialCallbackDto {
  code: string;
  state: string;
  provider: SocialProvider;
  deviceId?: string;
  deviceInfo?: string;
}

export interface SocialLoginResult {
  status: 'PENDING' | 'SUCCESS';
  login?: AuthModel.LoginVO;
  nickname?: string;
  pendingTicket?: string;
  provider?: SocialProvider;
}

export interface SocialProviderInfo {
  code: SocialProvider;
  label: string;
}

export interface SocialBindExistingDto {
  pendingTicket: string;
  login: AuthModel.LoginDTO;
}

export interface SocialRegisterDto {
  agreement: boolean;
  deviceId?: string;
  deviceInfo?: string;
  name: string;
  nature: 1 | 90;
  pendingTicket: string;
}
```

API 路径严格对应 Task 5 和 Task 6，`PREFIX` 定义为 `ServicePrefixEnum.WORKBENCH`：

```ts
export namespace SocialApi {
  export const providers = () =>
    requestClient.get<SocialProviderInfo[]>(`${PREFIX}/anyUser/social/providers`);

  export const loginUrl = (provider: SocialProvider) =>
    requestClient.get<string>(`${PREFIX}/anyUser/social/${provider}/loginUrl`);

  export const login = (data: SocialCallbackDto) =>
    requestClient.post<SocialLoginResult>(`${PREFIX}/anyUser/social/login`, data);

  export const bindExisting = (data: SocialBindExistingDto) =>
    requestClient.post<AuthModel.LoginVO>(`${PREFIX}/anyUser/social/bindExisting`, data);

  export const register = (data: SocialRegisterDto) =>
    requestClient.post<AuthModel.LoginVO>(`${PREFIX}/anyUser/social/register`, data);
}
```

- [ ] **Step 4: 抽取统一登录完成逻辑**

```ts
async function completeLogin(
  result: AuthModel.LoginVO,
  onSuccess?: () => Promise<void> | void,
) {
  const { tokenValue, refreshToken, expire } = result;
  if (!tokenValue) {
    throw new Error('登录结果缺少访问令牌');
  }
  accessStore.setAccessToken(tokenValue);
  accessStore.setRefreshToken(refreshToken);
  accessStore.setExpiration(expire);
  const userInfo = await UserApi.getUserinfo();
  userStore.setUserInfo(userInfo);
  if (accessStore.loginExpired) {
    accessStore.setLoginExpired(false);
  } else if (onSuccess) {
    await onSuccess();
  } else {
    await router.push(userInfo.homePath || preferences.app.defaultHomePath);
  }
  if (userInfo.name) {
    notification.success({
      description: `${$t('authentication.loginSuccessDesc')}:${userInfo.name}`,
      duration: 3,
      title: $t('authentication.loginSuccess'),
    });
  }
  return userInfo;
}
```

`authLogin` 只负责补充设备信息、调用 `AuthApi.login` 和等待 `completeLogin`，保留原通知与 `loginLoading` 行为，并从 store 返回 `completeLogin`。

- [ ] **Step 5: 实现安全的短期上下文**

```ts
export function safeInternalPath(path: unknown): string {
  return typeof path === 'string' && path.startsWith('/') && !path.startsWith('//')
    ? path
    : '/';
}
```

`saveSocialIntent/takeSocialIntent` 使用 sessionStorage，类型仅允许 `LOGIN/BIND/REAUTH`，内容只包含动作、平台、返回路径和短票据；禁止保存密码、验证码和 `pendingTicket`。`take` 读取后立即删除对应键。

- [ ] **Step 6: 运行测试和类型检查**

Run:

```bash
pnpm --dir /Users/tangyh/gitee/mdp-vben exec vitest run --dom apps/web-workbench/src/views/_core/sso/data/social.test.ts
pnpm --dir /Users/tangyh/gitee/mdp-vben --filter @vben/web-workbench typecheck
```

Expected: 测试通过；类型检查无新增错误。

---

### Task 11: 登录入口、统一回调与首次选择页面

**Files:**
- Create: `apps/web-workbench/src/views/_core/sso/social-login.vue`
- Create: `apps/web-workbench/src/views/_core/sso/social-callback.vue`
- Create: `apps/web-workbench/src/views/_core/sso/modules/social-bind-existing.vue`
- Create: `apps/web-workbench/src/views/_core/sso/modules/social-register.vue`
- Modify: `apps/web-workbench/src/views/_core/sso/login.vue`
- Modify: `apps/web-workbench/src/views/_core/sso/data/login.tsx`
- Modify: `apps/web-workbench/src/router/routes/core.ts`
- Test: `apps/web-workbench/src/views/_core/sso/social-callback.test.ts`

**Interfaces:**
- Consumes: Task 10 API、短期上下文和 `completeLogin`。
- Produces: 路由 `/auth/social/callback/:provider`。

- [ ] **Step 1: 编写待选择页面失败测试**

```ts
const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    {
      path: '/auth/social/callback/:provider',
      component: SocialCallback,
    },
  ],
});

it('未绑定身份展示两个选择且不自动注册', async () => {
  vi.mocked(SocialApi.login).mockResolvedValue({
    status: 'PENDING',
    pendingTicket: 'pending-ticket',
    provider: 'qq',
    nickname: '测试用户',
  });

  await router.push('/auth/social/callback/qq?code=code&state=state');
  await router.isReady();
  const wrapper = mount(SocialCallback, { global: { plugins: [router] } });
  await flushPromises();

  expect(wrapper.text()).toContain('绑定已有账号');
  expect(wrapper.text()).toContain('创建新账号');
  expect(SocialApi.register).not.toHaveBeenCalled();
});
```

- [ ] **Step 2: 运行测试并确认失败**

Run:

```bash
pnpm --dir /Users/tangyh/gitee/mdp-vben exec vitest run --dom apps/web-workbench/src/views/_core/sso/social-callback.test.ts
```

Expected: 回调组件不存在导致失败。

- [ ] **Step 3: 接入登录入口并保存 SSO 上下文**

```vue
<SsoLogin
  v-if="state.showUsernameForm"
  ref="formRef"
  show-third-party-login
  :username-form-schema="usernameFormSchema"
  :phone-form-schema="phoneFormSchema"
  :email-form-schema="emailFormSchema"
  :loading="authStore.loginLoading"
  @phone-submit="handlePhoneSubmit"
  @username-submit="handleAccountLogin"
  @email-submit="handleEmailSubmit"
>
  <template #third-party-login>
    <SocialLogin :sso-param="state.ssoParam" />
  </template>
</SsoLogin>
```

`social-login.vue` 只渲染 `providers()` 返回的平台。点击处理器必须等待后端授权地址：

```ts
async function handleLogin(provider: SocialProvider) {
  saveSocialIntent({ kind: 'LOGIN', provider, ssoParam: props.ssoParam });
  const url = await SocialApi.loginUrl(provider);
  window.location.assign(url);
}
```

不修改公共组件中的 GitHub、Google 和旧钉钉示例，避免影响其他应用。

- [ ] **Step 4: 实现统一回调主流程**

```ts
async function handleCallback() {
  const callback = readCallbackQuery(route);
  const intent = takeSocialIntent() ?? {
    kind: 'LOGIN',
    provider: callback.provider,
  };
  await router.replace({ query: {} });
  const result = await SocialApi.login(withDeviceInfo(callback));
  if (result.status === 'SUCCESS' && result.login) {
    await authStore.completeLogin(result.login, () =>
      redirectAfterLogin(intent?.ssoParam),
    );
    return;
  }
  pending.value = result;
}
```

`readCallbackQuery` 验证平台、code 和 state；清理 URL 前先保存到内存。`redirectAfterLogin` 从现有 `handleSuccess` 抽取，普通登录和社交登录共用，继续使用后端 `getRedirectUrl` 完成 SSO，不直接信任前端外部地址。

- [ ] **Step 5: 实现两个首登组件**

`social-bind-existing.vue` 复用现有用户名、手机和邮箱验证码表单规则，提交：

```ts
const login = await SocialApi.bindExisting({ pendingTicket, login: values });
await authStore.completeLogin(login, onSuccess);
```

`social-register.vue` 的姓名默认为第三方昵称，`nature` 使用现有注册身份选项，协议复选框未选中时阻止提交：

```ts
const login = await SocialApi.register({
  pendingTicket,
  name: values.name,
  nature: values.nature,
  agreement: values.agreement === true,
  ...deviceFields(),
});
await authStore.completeLogin(login, onSuccess);
```

两个组件均把 `pendingTicket` 保存在父组件内存，不写 sessionStorage 或 URL。

- [ ] **Step 6: 添加回调路由并运行测试**

```ts
{
  name: 'SocialCallback',
  path: 'social/callback/:provider',
  component: () => import('#/views/_core/sso/social-callback.vue'),
  meta: { title: '第三方账号登录' },
},
```

将路由放在 `Authentication` 的 children 中。运行 Step 2 测试及工作台 typecheck。

Expected: 成功、待选择、失败、注册和绑定已有账号分支通过；URL 不保留 code/state。

---

### Task 12: 个人中心绑定、解绑和转移界面

**Files:**
- Create: `apps/web-workbench/src/api/workbench/user/security/social.ts`
- Create: `apps/web-workbench/src/views/workbench/user/security/data/social.ts`
- Create: `apps/web-workbench/src/views/workbench/user/security/modules/social.vue`
- Create: `apps/web-workbench/src/views/workbench/user/security/modules/social-reauth.vue`
- Create: `apps/web-workbench/src/views/workbench/user/security/modules/social-transfer.vue`
- Modify: `apps/web-workbench/src/views/workbench/user/security/index.vue`
- Modify: `apps/web-workbench/src/views/_core/sso/social-callback.vue`
- Modify: `apps/web-workbench/src/views/_core/sso/data/social.ts`
- Modify: `apps/web-workbench/src/locales/langs/zh-CN/workbench/user/security.json`
- Modify: `apps/web-workbench/src/locales/langs/en-US/workbench/user/security.json`
- Test: `apps/web-workbench/src/views/workbench/user/security/data/social.test.ts`

**Interfaces:**
- Consumes: Task 7 至 Task 9 的全部个人中心接口。
- Produces: `useSocialAccounts()` 的 `accounts/loading/errorMessage/load/startBind/startUnbind/startTransfer/confirmUnbind`。
- Consumes: `sessionStorage` 中短期 `reauthTicket`，不保存任何密码或验证码。

- [ ] **Step 1: 编写账号列表和操作结果失败测试**

```ts
it('绑定列表只发起一次批量查询', async () => {
  vi.mocked(SocialSecurityApi.list).mockResolvedValue([
    { provider: 'qq', bound: true, nickname: 'QQ用户', boundAt: '2026-11-11' },
    { provider: 'wechat-open', bound: false },
  ]);

  const { accounts, load } = useSocialAccounts();
  await load();

  expect(SocialSecurityApi.list).toHaveBeenCalledTimes(1);
  expect(accounts.value.find((item) => item.provider === 'qq')?.bound).toBe(true);
});

it('最后登录方式保护错误直接展示后端具体消息', async () => {
  vi.mocked(SocialSecurityApi.unbind).mockRejectedValue(
    new Error('解绑后将无法登录，请先补充联系方式或绑定其他平台'),
  );
  const { confirmUnbind, errorMessage } = useSocialAccounts();

  await expect(
    confirmUnbind({ provider: 'qq', reauthTicket: 'ticket' }),
  ).rejects.toThrow('解绑后将无法登录');

  expect(errorMessage.value).toBe(
    '解绑后将无法登录，请先补充联系方式或绑定其他平台',
  );
});
```

- [ ] **Step 2: 运行测试并确认失败**

Run:

```bash
pnpm --dir /Users/tangyh/gitee/mdp-vben exec vitest run --dom apps/web-workbench/src/views/workbench/user/security/data/social.test.ts
```

Expected: 账号管理 API 或 composable 不存在导致失败。

- [ ] **Step 3: 实现个人中心 API**

```ts
export namespace SocialSecurityApi {
  export const list = () => requestClient.get<SocialBinding[]>(`${MODULAR}/list`);
  export const reauth = (data: SocialReauthDto) =>
    requestClient.post<SocialReauthTicket>(`${MODULAR}/reauth`, data);
  export const reauthUrl = (provider: SocialProvider, data: SocialReauthUrlDto) =>
    requestClient.post<string>(`${MODULAR}/${provider}/reauthUrl`, data);
  export const reauthByCallback = (data: SocialCallbackDto) =>
    requestClient.post<SocialReauthTicket>(`${MODULAR}/reauthByCallback`, data);
  export const bindUrl = (provider: SocialProvider, reauthTicket: string) =>
    requestClient.post<string>(`${MODULAR}/${provider}/bindUrl`, { reauthTicket });
  export const bind = (data: SocialCallbackDto) =>
    requestClient.post<void>(`${MODULAR}/bind`, data);
  export const unbind = (data: SocialUnbindDto) =>
    requestClient.post<void>(`${MODULAR}/unbind`, data);
  export const transfer = (data: SocialTransferDto) =>
    requestClient.post<AuthModel.LoginVO>(`${MODULAR}/transfer`, data);
}
```

`MODULAR` 为 `${ServicePrefixEnum.WORKBENCH}/profile/social`。

- [ ] **Step 4: 实现二次验证和短期票据续接**

`social-reauth.vue` 提供密码、手机、邮箱和已绑定第三方四种方式。手机、邮箱验证码复用 `CaptchaApi` 和现有登录模板；提交后只保存后端返回的 `reauthTicket`。

选择第三方重新授权时：

```ts
saveSocialIntent({
  kind: 'REAUTH',
  provider,
  action,
  targetProvider,
  returnPath: safeInternalPath(route.fullPath),
});
window.location.assign(await SocialSecurityApi.reauthUrl(provider, {
  action,
  provider: targetProvider,
}));
```

回调页在调用 `SocialApi.login` 前，将非 `LOGIN` intent 交给以下函数并立即返回：

```ts
async function handleAccountCallback(
  callback: SocialCallbackDto,
  intent: AccountSocialIntent,
) {
  if (intent.kind === 'REAUTH') {
    const result = await SocialSecurityApi.reauthByCallback(callback);
    saveReauthResume({ ...intent, reauthTicket: result.reauthTicket });
    await router.replace(safeInternalPath(intent.returnPath));
    return;
  }
  if (intent.kind === 'BIND') {
    await SocialSecurityApi.bind(callback);
    await router.replace(safeInternalPath(intent.returnPath));
  }
}
```

`AccountSocialIntent` 是排除 `LOGIN` 后的判别联合类型，保证 `returnPath` 在 TypeScript 中必填。

安全设置页读取一次性 resume 上下文：BIND 继续获取目标平台授权地址，UNBIND 打开影响确认，TRANSFER 打开目标账号凭据表单。密码和验证码绝不进入该上下文。

- [ ] **Step 5: 实现账号卡片、解绑和转移**

卡片每行展示平台、绑定状态、昵称、绑定时间及操作。绑定入口先执行二次验证，再获取授权地址：

```ts
saveSocialIntent({
  kind: 'BIND',
  provider,
  returnPath: safeInternalPath(route.fullPath),
});
window.location.assign(await SocialSecurityApi.bindUrl(provider, reauthTicket));
```

解绑确认必须包含最后登录方式提示；`confirmUnbind` 捕获请求异常，将后端具体消息写入 `errorMessage` 并保留弹窗，不静默吞错：

```ts
async function confirmUnbind(data: SocialUnbindDto) {
  try {
    errorMessage.value = '';
    await SocialSecurityApi.unbind(data);
    await load();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '解绑失败，请重试';
    throw error;
  }
}
```

转移确认必须显示“仅转移第三方登录方式，不迁移组织、角色、消息和业务数据”。转移成功后调用 `authStore.completeLogin` 切换目标账号。

- [ ] **Step 6: 接入安全设置页和多语言并运行测试**

在 `security/index.vue` 第三列密码卡片下加入 `<SocialAccounts />`。中文和英文 JSON 均补齐 `social.title/desc/bind/unbind/transfer/bound/notBound/reauth` 文案。运行 Step 2 测试、回调测试和 typecheck。

Expected: 列表、绑定续接、解绑错误、转移确认及成功后状态刷新测试通过；敏感信息未写入 storage。

---

### Task 13: 全量回归、构建与真实平台验收准备

**Files:**
- Modify: `docs/social-login.md`
- Review: 本计划涉及的全部文件。

**Interfaces:**
- Consumes: Task 1 至 Task 12 的全部实现。
- Produces: 可复现的自动化验证结果和真实扫码验收清单。

- [ ] **Step 1: 运行后端完整相关测试**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-console/console-service,mdp-apps/md-console/console-facade/console-boot-impl,mdp-apps/md-console/console-facade/console-cloud-impl,mdp-apps/md-console/console-web,mdp-apps/md-workbench/workbench-service,mdp-apps/md-workbench/workbench-web -am test
```

Expected: 构建成功，所有新增测试和现有相关模块测试通过。若存在失败，记录具体测试和输出，不得跳过或宣称通过。

- [ ] **Step 2: 运行后端 Checkstyle**

Run:

```bash
mvn -f /Users/tangyh/gitee/mdp/pom.xml -pl mdp-apps/md-console/console-service,mdp-apps/md-console/console-facade/console-boot-impl,mdp-apps/md-console/console-facade/console-cloud-impl,mdp-apps/md-console/console-web,mdp-apps/md-workbench/workbench-service,mdp-apps/md-workbench/workbench-web -am checkstyle:check
```

Expected: 无新增规范错误；若仓库已有错误，区分本次引入与历史问题并如实报告。

- [ ] **Step 3: 运行前端测试、类型检查和构建**

Run:

```bash
pnpm --dir /Users/tangyh/gitee/mdp-vben exec vitest run --dom apps/web-workbench/src/views/_core/sso/data/social.test.ts apps/web-workbench/src/views/_core/sso/social-callback.test.ts apps/web-workbench/src/views/workbench/user/security/data/social.test.ts
pnpm --dir /Users/tangyh/gitee/mdp-vben --filter @vben/web-workbench typecheck
pnpm --dir /Users/tangyh/gitee/mdp-vben build:workbench
```

Expected: 三条命令均退出码为 0；输出中无新增 TypeScript 错误。

- [ ] **Step 4: 审查安全和兼容性清单**

逐项确认：

1. `AuthServiceImplLoginTest` 和 `WxMaAuthServiceImplTest` 均通过。
2. 旧用户表没有新增 QQ 字段，旧小程序字段没有迁移或改义。
3. QQ 使用 `token.openId`，不是 `uuid`。
4. 所有启用配置都有非空校验，日志不包含配置对象。
5. state、pendingTicket、reauthTicket 均有过期、用途校验和消费标记。
6. 个人中心用户 ID 全部来自服务端上下文。
7. 转移只更新绑定表，没有组织、角色和资料更新调用。
8. 前端 URL、sessionStorage 和组件日志中没有密码、验证码和 pendingTicket。
9. `docs/sql/mdp.sql` 只追加新表，没有覆盖原有修改。
10. 两个仓库均未执行 Git 暂存、提交或推送。

- [ ] **Step 5: 完善真实平台验收说明**

在 `docs/social-login.md` 为三个平台分别记录以下人工步骤，不填写真实凭证：

1. 配置应用凭证和精确回调地址后启动后端与工作台。
2. 验证已绑定登录、未绑定绑定已有账号、未绑定创建新账号。
3. 验证个人中心四种二次验证中当前环境可用的方式。
4. 验证绑定、解绑、最后登录方式保护和误注册转移。
5. 验证取消授权、错误回调、重复回调、过期票据和禁用用户。
6. 分别记录每个平台实际执行结果及无法执行的原因。

Expected: 没有真实凭证时明确标记“未执行真实第三方验收”，自动化结果与人工结果分开报告。
