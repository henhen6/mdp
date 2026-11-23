# 密码过期续期豁免保护 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 密码变更场景（管理员重置、个人修改、邮箱找回、手机找回）更新 `pwExpireTime` 时，旧值为 `null` 的用户豁免不覆盖，有值（含 `MAX`）才重算。

**Architecture:** 豁免判断收口在 `md-common-config` 的 `PasswordExpiryCalculator.renewExpireTime(oldExpireTime, period)`（返回 `null` 当且仅当豁免），4 个调用点以返回值为准、非 `null` 才调 setter（`UpdateEntity` 未 set 的字段不进 UPDATE 语句，严禁 `setPwExpireTime(null)`）。console-service 与 workbench-service 各新增 `md-common-config` 依赖。

**Tech Stack:** Java 17、MyBatis-Flex（UpdateEntity）、JUnit 5、Mockito（openMocks/spy 既有模式）

**规格:** `docs/superpowers/specs/2026-11-23-password-expiry-policy-design.md` §5.2.1 / §8.1（已确认，用户已裁决 null 豁免、MAX 不豁免）

## Global Constraints

- 禁止任何 git add/commit/push/stash；成果保留工作区。
- 仅保护旧值为 `null` 的用户；`LocalDateTime.MAX` 不豁免，重算为有期限（用户裁决 2026-11-23）。
- 控制台新增用户（`UserServiceImpl.saveBefore:192`）与注册/SSO 初始化（`UserServiceImpl.initSsoUser:632`）**不改动**，仍按配置计算。
- 豁免时调用点跳过 setter 调用，不得写 `setPwExpireTime(null)`。
- 配置读取时机保持现状（豁免分支也读配置，最小语义变更）。
- 最大行宽 100 字符；注释解释"为什么"；错误 fail fast 不吞异常。
- 测试用 org.junit.jupiter.api.Test + Mockito；console 模块沿用 openMocks 模式（无 mockito-junit-jupiter），workbench SsoUserServiceImplTest 沿用 spy 模式。
- `SsoUserServiceImplTest` 存在与本次无关的既有失败用例（updatePhone 等，committed red），甄别标准：**新增用例必须全绿，既有失败不处理、不扩大**。

---

### Task 1: renewExpireTime 策略方法与模块依赖

**Files:**
- Modify: `mdp-apps/md-public/md-common-config/src/main/java/top/mddata/common/password/PasswordExpiryCalculator.java`
- Test: `mdp-apps/md-public/md-common-config/src/test/java/top/mddata/common/password/PasswordExpiryCalculatorTest.java`
- Modify: `mdp-apps/md-console/console-service/pom.xml`
- Modify: `mdp-apps/md-workbench/workbench-service/pom.xml`

**Interfaces:**
- Consumes: `top.mddata.base.utils.DateUtils.conversionDateTime(LocalDateTime, String)`（md-common-config 经 md-common-pojo 传递依赖 md-util，无需新增依赖）。
- Produces: `public static LocalDateTime renewExpireTime(LocalDateTime oldExpireTime, String period)` —— `oldExpireTime == null` 返回 `null`；否则返回 `DateUtils.conversionDateTime(LocalDateTime.now(), period)`（含空串 period → `LocalDateTime.MAX` 的既有语义）。Task 2/3 按此签名调用。

- [ ] **Step 1: 写失败测试**（在 `PasswordExpiryCalculatorTest` 追加，import 补 `assertNull`、`assertTrue`、`assertNotNull`、`java.time.LocalDateTime` 已有）

```java
@Test
void renewExpireTime_旧值为null_返回null表示豁免() {
    assertNull(PasswordExpiryCalculator.renewExpireTime(null, "3M"));
}

@Test
void renewExpireTime_旧值有值_按周期重算() {
    LocalDateTime old = LocalDateTime.now().plusDays(1);
    LocalDateTime renewed = PasswordExpiryCalculator.renewExpireTime(old, "3M");
    assertNotNull(renewed);
    assertTrue(renewed.isAfter(old));
}

@Test
void renewExpireTime_MAX不豁免_重算为有期限() {
    LocalDateTime renewed = PasswordExpiryCalculator.renewExpireTime(LocalDateTime.MAX, "3M");
    assertNotNull(renewed);
    assertTrue(renewed.isBefore(LocalDateTime.MAX));
}

@Test
void renewExpireTime_配置为空_重算为MAX永不过期() {
    LocalDateTime old = LocalDateTime.now().plusDays(1);
    assertEquals(LocalDateTime.MAX, PasswordExpiryCalculator.renewExpireTime(old, ""));
}
```

- [ ] **Step 2: 验证失败**

Run: `mvn -pl mdp-apps/md-public/md-common-config -am -Dtest=PasswordExpiryCalculatorTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: FAIL，`Cannot resolve method 'renewExpireTime'`（编译失败亦视为 RED）

- [ ] **Step 3: 最小实现**（`PasswordExpiryCalculator` 追加；import 加 `top.mddata.base.utils.DateUtils`）

```java
/**
 * 密码变更场景的过期时间续期策略（规格 §5.2.1）：现有值为 null 表示该用户豁免
 * 密码过期，返回 null 且调用点必须跳过 setPwExpireTime（UpdateEntity 未 set 的
 * 字段不进 UPDATE 语句）；有值（含 MAX）才按当前周期重算。
 */
public static LocalDateTime renewExpireTime(LocalDateTime oldExpireTime, String period) {
    if (oldExpireTime == null) {
        return null;
    }
    return DateUtils.conversionDateTime(LocalDateTime.now(), period);
}
```

- [ ] **Step 4: 验证通过 + checkstyle**

Run: `mvn -pl mdp-apps/md-public/md-common-config -am -Dtest=PasswordExpiryCalculatorTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: PASS（既有 4 + 新增 4 = 8 用例）
Run: `mvn -pl mdp-apps/md-public/md-common-config checkstyle:check`
Expected: 退出码 0

- [ ] **Step 5: 两个 pom 加依赖**（dependency 段插入位置跟随各 pom 既有相邻依赖；无 commit）

`mdp-apps/md-console/console-service/pom.xml` 与 `mdp-apps/md-workbench/workbench-service/pom.xml` 各加：

```xml
<dependency>
    <groupId>top.mddata</groupId>
    <artifactId>md-common-config</artifactId>
    <version>${revision}</version>
</dependency>
```

验证：`mvn -pl mdp-apps/md-console/console-service,mdp-apps/md-workbench/workbench-service -am compile -q`
Expected: BUILD SUCCESS

---

### Task 2: workbench 三个调用点接入

**Files:**
- Modify: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/user/impl/SsoUserServiceImpl.java:484,541,570` 附近
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/user/impl/SsoUserServiceImplTest.java`

**Interfaces:**
- Consumes: Task 1 的 `PasswordExpiryCalculator.renewExpireTime(LocalDateTime, String)` 与 workbench-service 的 md-common-config 依赖。
- Produces: 无新接口；三处行为变更：豁免用户重置密码后 `pwExpireTime` 保持 `null`。

- [ ] **Step 1: 写失败测试**（`SsoUserServiceImplTest` 追加；该类为 spy 模式，`currentUser()`/`injectSuperCacheOps` 复用；import 补 `ArgumentCaptor` 已有、`assertNull`、`assertNotNull`、`LocalDateTime`）

```java
private User oldUser(LocalDateTime pwExpireTime) {
    User user = currentUser();
    user.setPwExpireTime(pwExpireTime);
    return user;
}

@Test
void updatePassword_旧值null_不覆盖过期时间() {
    doReturn(oldUser(null)).when(service).getById(USER_ID);
    doReturn(true).when(service).updateById(any(User.class));
    ProfilePasswordDto dto = new ProfilePasswordDto();
    dto.setOldPassword(PASSWORD);
    dto.setPassword("new-password");
    dto.setConfirmPassword("new-password");
    service.updatePassword(dto);
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(service).updateById(captor.capture());
    assertNull(captor.getValue().getPwExpireTime());
}

@Test
void updatePassword_旧值有值_重算过期时间() {
    doReturn(oldUser(LocalDateTime.now().plusDays(1))).when(service).getById(USER_ID);
    doReturn(true).when(service).updateById(any(User.class));
    when(configFacade.getString(anyString(), anyString())).thenReturn("3M");
    ProfilePasswordDto dto = new ProfilePasswordDto();
    dto.setOldPassword(PASSWORD);
    dto.setPassword("new-password");
    dto.setConfirmPassword("new-password");
    service.updatePassword(dto);
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(service).updateById(captor.capture());
    assertNotNull(captor.getValue().getPwExpireTime());
    assertTrue(captor.getValue().getPwExpireTime().isAfter(LocalDateTime.now()));
}

@Test
void resetPwByEmail_旧值null_不覆盖过期时间() {
    doReturn(oldUser(null)).when(service).getByEmail("a@b.com");
    doReturn(true).when(service).updateById(any(User.class));
    service.resetPwByEmail("a@b.com", "new-password");
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(service).updateById(captor.capture());
    assertNull(captor.getValue().getPwExpireTime());
}

@Test
void resetPwByEmail_旧值有值_重算过期时间() {
    doReturn(oldUser(LocalDateTime.now().plusDays(1))).when(service).getByEmail("a@b.com");
    doReturn(true).when(service).updateById(any(User.class));
    when(configFacade.getString(anyString(), anyString())).thenReturn("3M");
    service.resetPwByEmail("a@b.com", "new-password");
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(service).updateById(captor.capture());
    assertNotNull(captor.getValue().getPwExpireTime());
}

@Test
void resetPwByPhone_旧值null_不覆盖过期时间() {
    doReturn(oldUser(null)).when(service).getByPhone("13800000000");
    doReturn(true).when(service).updateById(any(User.class));
    service.resetPwByPhone("13800000000", "new-password");
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(service).updateById(captor.capture());
    assertNull(captor.getValue().getPwExpireTime());
}

@Test
void resetPwByPhone_旧值有值_重算过期时间() {
    doReturn(oldUser(LocalDateTime.now().plusDays(1))).when(service).getByPhone("13800000000");
    doReturn(true).when(service).updateById(any(User.class));
    when(configFacade.getString(anyString(), anyString())).thenReturn("3M");
    service.resetPwByPhone("13800000000", "new-password");
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(service).updateById(captor.capture());
    assertNotNull(captor.getValue().getPwExpireTime());
}
```

注意：若既有测试未 stub `configFacade.getString` 时 updatePassword 用例缺省返回 null 导致 NPE，则在豁免用例同样补 `when(configFacade.getString(anyString(), anyString())).thenReturn("3M")`（豁免分支不使用返回值，但读取不能抛异常）。

- [ ] **Step 2: 验证失败**

Run: `mvn -pl mdp-apps/md-workbench/workbench-service -am -Dtest=SsoUserServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: 新增 6 用例中"不覆盖"用例 FAIL（现实现无条件覆盖，captured 值非 null）；"重算"用例可能当轮通过（刻画既有行为）；既有失败用例（updatePhone 等）保持既有状态不处理。

- [ ] **Step 3: 最小实现**（三处同型替换，以 updatePassword :484 为例；另两处把 `current` 换成 `oldUser`；import 加 `top.mddata.common.password.PasswordExpiryCalculator`，**移除** `DateUtils` import——该类 4 处 DateUtils 引用中 3 处被替换后仅剩 import 行）

```java
// 修改前
String expireTime = configFacade.getString(ConfigKey.Workbench.PASSWORD_EXPIRE_TIME, "3M");
user.setPwExpireTime(DateUtils.conversionDateTime(LocalDateTime.now(), expireTime));

// 修改后
String expireTime = configFacade.getString(ConfigKey.Workbench.PASSWORD_EXPIRE_TIME, "3M");
LocalDateTime renewedExpireTime =
        PasswordExpiryCalculator.renewExpireTime(current.getPwExpireTime(), expireTime);
if (renewedExpireTime != null) {
    user.setPwExpireTime(renewedExpireTime);
}
```

`resetPwByEmail`（:541）与 `resetPwByPhone`（:570）用 `oldUser.getPwExpireTime()` 替换，其余逐行不动。

- [ ] **Step 4: 验证通过 + 模块回归 + checkstyle**

Run: `mvn -pl mdp-apps/md-workbench/workbench-service -am -Dtest=SsoUserServiceImplTest,UsernameLoginStrategyImplTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: 新增 6 用例全绿；UsernameLoginStrategyImplTest 6/6 回归不受影响；既有失败用例数与改前一致。
Run: `mvn -pl mdp-apps/md-workbench/workbench-service checkstyle:check`
Expected: 退出码 0

---

### Task 3: console resetPassword 接入

**Files:**
- Modify: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/organization/impl/UserServiceImpl.java:383-403`（resetPassword 方法）
- Test: `mdp-apps/md-console/console-service/src/test/java/top/mddata/console/service/organization/impl/UserServiceImplPasswordExpiryTest.java`（新建）

**Interfaces:**
- Consumes: Task 1 的 `renewExpireTime` 与 console-service 的 md-common-config 依赖。
- Produces: 管理员重置密码对豁免用户不覆盖 `pwExpireTime`；方法新增一次 `getById` 读取旧值。

- [ ] **Step 1: 写失败测试**（新建文件，模式复制自 `UserServiceImplAccountOpTest`：openMocks + @InjectMocks + ReflectionTestUtils 注入 mapper/cacheOps）

```java
package top.mddata.console.service.organization.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.common.entity.User;
import top.mddata.common.mapper.UserMapper;
import top.mddata.common.properties.SystemProperties;
import top.mddata.console.dto.organization.UserResetPasswordDto;
import top.mddata.console.service.organization.UserOrgRelService;
import top.mddata.console.service.organization.UserRoleRelService;
import top.mddata.console.service.organization.accountop.AccountOperation;
import top.mddata.console.service.organization.accountop.AccountOperationGuard;
import top.mddata.console.service.system.ConfigService;
import top.mddata.console.service.system.FileService;
import top.mddata.open.facade.admin.NotifyAndEventPushFacade;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserServiceImpl 管理员重置密码的续期豁免测试（规格 §5.2.1）：
 * 旧 pwExpireTime 为 null 的用户不覆盖，有值才重算。
 */
class UserServiceImplPasswordExpiryTest {

    private static final Long USER_ID = 20L;

    private AutoCloseable mocks;
    @Mock
    private UserMapper mapper;
    @Mock
    private CacheOps cacheOps;
    @Mock
    private AccountOperationGuard accountOperationGuard;
    @Mock
    private ConfigService configService;
    @Mock
    private SystemProperties systemProperties;
    @Mock
    private NotifyAndEventPushFacade notifyAndEventPushFacade;
    @Mock
    private FileService fileService;
    @Mock
    private UserOrgRelService userOrgRelService;
    @Mock
    private UserRoleRelService userRoleRelService;
    @Mock
    private top.mddata.console.service.organization.SystemProtectService systemProtectService;
    @Mock
    private top.mddata.common.mapper.UserSocialAuthMapper userSocialAuthMapper;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userService, "mapper", mapper);
        ReflectionTestUtils.setField(userService, "cacheOps", cacheOps);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    private UserResetPasswordDto resetDto() {
        UserResetPasswordDto data = new UserResetPasswordDto();
        data.setId(USER_ID);
        data.setPassword("new-password");
        return data;
    }

    @Test
    void resetPassword_旧值null_不覆盖过期时间() {
        User oldUser = new User();
        oldUser.setId(USER_ID);
        oldUser.setPwExpireTime(null);
        when(mapper.selectOneById(USER_ID)).thenReturn(oldUser);
        when(configService.getString(anyString(), anyString())).thenReturn("3M");
        userService.resetPassword(resetDto());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(mapper).update(captor.capture(), anyBoolean());
        assertNull(captor.getValue().getPwExpireTime());
    }

    @Test
    void resetPassword_旧值有值_重算过期时间() {
        User oldUser = new User();
        oldUser.setId(USER_ID);
        oldUser.setPwExpireTime(LocalDateTime.now().plusDays(1));
        when(mapper.selectOneById(USER_ID)).thenReturn(oldUser);
        when(configService.getString(anyString(), anyString())).thenReturn("3M");
        userService.resetPassword(resetDto());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(mapper).update(captor.capture(), anyBoolean());
        assertNotNull(captor.getValue().getPwExpireTime());
        assertTrue(captor.getValue().getPwExpireTime().isAfter(LocalDateTime.now()));
    }
}
```

注意：`@InjectMocks` 构造注入与 `ReflectionTestUtils` 补注父类 mapper/cacheOps 的组合沿用 `UserServiceImplAccountOpTest` 既有模式；若 `getById` 缓存路径（`getByIdCache`）需要额外字段，按该类既有测试同款方式补注，并在报告说明。

- [ ] **Step 2: 验证失败**

Run: `mvn -pl mdp-apps/md-console/console-service -am -Dtest=UserServiceImplPasswordExpiryTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: FAIL——现实现未查旧值且无条件覆盖（"不覆盖"用例 captured 非 null；若因 `getById` 未发生导致 stub 未命中，同属 RED，按实现缺失甄别）。

- [ ] **Step 3: 最小实现**（resetPassword；import 加 `top.mddata.base.utils.ArgumentAssert`（该包路径与 SsoUserServiceImpl 一致）与 `top.mddata.common.password.PasswordExpiryCalculator`）

```java
// 修改前
accountOperationGuard.check(data.getId(), AccountOperation.RESET_PASSWORD);
User user = UpdateEntity.of(User.class, data.getId());
user.setPwErrorLastTime(null);
user.setPwErrorNum(0);
String password;
String salt = RandomUtil.randomString(20);
if (data.getDefPassword() != null && data.getDefPassword()) {
    password = SecureUtil.sha256(systemProperties.getDefPwd() + salt);
} else {
    password = SecureUtil.sha256(data.getPassword() + salt);
}
user.setSalt(salt);
user.setPassword(password);

String expireTime = configService.getString(ConfigKey.Workbench.PASSWORD_EXPIRE_TIME, "3M");
user.setPwExpireTime(DateUtils.conversionDateTime(LocalDateTime.now(), expireTime));

// 修改后（guard 校验后补读旧值；续期改为豁免判定）
accountOperationGuard.check(data.getId(), AccountOperation.RESET_PASSWORD);
User oldUser = getById(data.getId());
ArgumentAssert.notNull(oldUser, "用户不存在");
User user = UpdateEntity.of(User.class, data.getId());
user.setPwErrorLastTime(null);
user.setPwErrorNum(0);
String password;
String salt = RandomUtil.randomString(20);
if (data.getDefPassword() != null && data.getDefPassword()) {
    password = SecureUtil.sha256(systemProperties.getDefPwd() + salt);
} else {
    password = SecureUtil.sha256(data.getPassword() + salt);
}
user.setSalt(salt);
user.setPassword(password);

String expireTime = configService.getString(ConfigKey.Workbench.PASSWORD_EXPIRE_TIME, "3M");
LocalDateTime renewedExpireTime =
        PasswordExpiryCalculator.renewExpireTime(oldUser.getPwExpireTime(), expireTime);
if (renewedExpireTime != null) {
    user.setPwExpireTime(renewedExpireTime);
}
```

`DateUtils` import 保留（`saveBefore:192`、`initSsoUser:632` 仍在使用）。

- [ ] **Step 4: 验证通过 + 既有测试回归 + checkstyle**

Run: `mvn -pl mdp-apps/md-console/console-service -am -Dtest=UserServiceImplPasswordExpiryTest,UserServiceImplAccountOpTest,UserServiceImplContactTest -Dsurefire.failIfNoSpecifiedTests=false test`
Expected: 新增 2 用例全绿；AccountOpTest/ContactTest 与改前一致（含既有失败甄别，不扩大）。
Run: `mvn -pl mdp-apps/md-console/console-service checkstyle:check`
Expected: 退出码 0

---

### Task 4: 联合回归与文档同步

**Files:**
- Verify: 全部上述文件；`docs/superpowers/specs/2026-11-23-password-expiry-policy-design.md`（已更新，无需改动）

- [ ] **Step 1: 后端定向回归**

Run:
```bash
mvn -pl mdp-apps/md-public/md-common-config,mdp-apps/md-console/console-service,mdp-apps/md-workbench/workbench-service -am \
  -Dtest=PasswordExpiryCalculatorTest,SsoUserServiceImplTest,UserServiceImplPasswordExpiryTest,UserServiceImplAccountOpTest,UsernameLoginStrategyImplTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```
Expected: PasswordExpiryCalculatorTest 8/8、新增豁免用例 8/8 全绿；既有失败用例数与改前基线一致（updatePhone 等既有失败不处理）。

- [ ] **Step 2: checkstyle 全覆盖**

Run: `mvn -pl mdp-apps/md-public/md-common-config,mdp-apps/md-console/console-service,mdp-apps/md-workbench/workbench-service checkstyle:check`
Expected: 3 模块退出码 0。

- [ ] **Step 3: 静态核对豁免语义**

```bash
grep -n "renewExpireTime" \
  mdp-apps/md-public/md-common-config/src/main/java/top/mddata/common/password/PasswordExpiryCalculator.java \
  mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/organization/impl/UserServiceImpl.java \
  mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/user/impl/SsoUserServiceImpl.java
grep -n "setPwExpireTime" \
  mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/organization/impl/UserServiceImpl.java \
  mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/user/impl/SsoUserServiceImpl.java
```
Expected: renewExpireTime 1 定义 + 4 调用；setPwExpireTime 仅剩 4 处（均在 `renewedExpireTime != null` 分支内）+ saveBefore/initSsoUser 2 处不受保护点；无 `setPwExpireTime(null)` 出现。

- [ ] **Step 4: 台账与交付记录**

在 `.superpowers/sdd/progress.md` 密码过期段落追加豁免保护变更记录；输出人工验证提示（豁免用户重置密码后登录不再触发过期提醒/拦截，可用 dev 环境抽查）。
