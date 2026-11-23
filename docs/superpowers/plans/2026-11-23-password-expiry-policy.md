# 密码过期与临期提醒实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在不新增用户表字段的前提下，完善密码过期登录门禁、忘记密码引导、登录后临期提醒及安全设置状态展示。

**Architecture:** 后端以 `pwExpireTime` 为唯一过期时间数据源，通过纯计算器统一状态边界；密码验证成功后才执行过期门禁。console 在现有用户信息接口中附加密码状态，workbench 前端复用 `completeLogin` 和现有修改密码、忘记密码页面完成提醒闭环。

**Tech Stack:** Java 17、Spring Boot、JUnit 5、Mockito、MyBatis-Flex、Vue 3、TypeScript、Pinia、antdv-next、Vitest。

## Global Constraints

- 设计依据：`docs/superpowers/specs/2026-11-23-password-expiry-policy-design.md`。
- 仅 USERNAME/CAPTCHA 密码认证受过期禁止登录限制，其他认证方式只显示登录后提醒。
- `PASSWORD_EXPIRE_TIME` 修改不追溯现有用户。
- 新增 `PASSWORD_EXPIRE_REMIND_DAYS=7`，`0` 关闭临期提醒，已过期允许登录的提醒不受其影响。
- 密码过期专用错误码固定为 `-17`，前端不得解析异常文案判断。
- 错误处理必须记录具体日志；禁止静默吞异常，禁止在日志中记录密码、验证码、Token。
- 不新增 `password_changed_at`，不引入定时通知、强制修改密码路由守卫或会话注销改造。
- 所有新增界面文案同时补充 `zh-CN` 和 `en-US`。
- 禁止执行 `git add`、`git commit`、`git push`；每个任务以测试和代码审查作为检查点。

---

### Task 1: 后端统一密码状态计算器

**Files:**
- Create: `mdp-apps/md-public/md-common-config/src/main/java/top/mddata/common/password/PasswordExpiryCalculator.java`
- Test: `mdp-apps/md-public/md-common-config/src/test/java/top/mddata/common/password/PasswordExpiryCalculatorTest.java`

**Interfaces:**
- Consumes: `LocalDateTime now`、`LocalDateTime expireTime`、`int remindDays`。
- Produces: `PasswordExpiryCalculator.evaluate(...)`，返回 `Result(Status status, Long remainDays)`；状态为 `NEVER/NORMAL/EXPIRING/EXPIRED`。

- [ ] **Step 1: 编写失败的边界测试**

```java
package top.mddata.common.password;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static top.mddata.common.password.PasswordExpiryCalculator.Status;

class PasswordExpiryCalculatorTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 23, 10, 0);

    @Test
    void shouldReturnNeverWhenExpireTimeIsNullOrMax() {
        assertNull(PasswordExpiryCalculator.evaluate(NOW, null, 7).remainDays());
        assertEquals(Status.NEVER,
                PasswordExpiryCalculator.evaluate(NOW, LocalDateTime.MAX, 7).status());
    }

    @Test
    void shouldExpireAtExactDeadline() {
        var result = PasswordExpiryCalculator.evaluate(NOW, NOW, 7);
        assertEquals(Status.EXPIRED, result.status());
        assertEquals(0L, result.remainDays());
    }

    @Test
    void shouldCalculateCalendarDaysAndThreshold() {
        var today = PasswordExpiryCalculator.evaluate(NOW, NOW.plusHours(2), 7);
        assertEquals(Status.EXPIRING, today.status());
        assertEquals(0L, today.remainDays());

        var boundary = PasswordExpiryCalculator.evaluate(NOW, NOW.plusDays(7), 7);
        assertEquals(Status.EXPIRING, boundary.status());
        assertEquals(7L, boundary.remainDays());

        assertEquals(Status.NORMAL,
                PasswordExpiryCalculator.evaluate(NOW, NOW.plusDays(8), 7).status());
        assertEquals(Status.NORMAL,
                PasswordExpiryCalculator.evaluate(NOW, NOW.plusDays(1), 0).status());
    }

    @Test
    void shouldRejectNegativeReminderDays() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordExpiryCalculator.evaluate(NOW, NOW.plusDays(1), -1));
    }
}
```

- [ ] **Step 2: 运行测试并确认编译失败**

Run:

```bash
mvn -pl mdp-apps/md-public/md-common-config -am \
  -Dtest=PasswordExpiryCalculatorTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: FAIL，提示 `PasswordExpiryCalculator` 不存在。

- [ ] **Step 3: 实现纯计算器**

```java
package top.mddata.common.password;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** 密码过期状态纯计算，不读取配置和数据库。 */
public final class PasswordExpiryCalculator {

    private PasswordExpiryCalculator() {
    }

    public enum Status {
        NEVER, NORMAL, EXPIRING, EXPIRED
    }

    public record Result(Status status, Long remainDays) {
    }

    public static Result evaluate(LocalDateTime now, LocalDateTime expireTime, int remindDays) {
        if (remindDays < 0) {
            throw new IllegalArgumentException("密码临期提醒天数不能小于0");
        }
        if (expireTime == null || LocalDateTime.MAX.equals(expireTime)) {
            return new Result(Status.NEVER, null);
        }
        if (!now.isBefore(expireTime)) {
            return new Result(Status.EXPIRED, 0L);
        }
        long days = ChronoUnit.DAYS.between(now.toLocalDate(), expireTime.toLocalDate());
        Status status = remindDays > 0 && days <= remindDays ? Status.EXPIRING : Status.NORMAL;
        return new Result(status, days);
    }
}
```

- [ ] **Step 4: 重跑测试并确认通过**

Run: 同 Step 2。

Expected: `PasswordExpiryCalculatorTest` 全部 PASS，构建退出码为 0。

---

### Task 2: 密码验证后执行过期登录门禁

**Files:**
- Modify: `mdp-base/md-core/src/main/java/top/mddata/base/exception/code/ExceptionCode.java:104-107`
- Modify: `mdp-apps/md-console/console-facade/console-api/src/main/java/top/mddata/console/facade/system/ConfigFacade.java`
- Modify: `mdp-apps/md-console/console-facade/console-boot-impl/src/main/java/top/mddata/console/facade/impl/system/ConfigFacadeImpl.java`
- Modify: `mdp-apps/md-console/console-facade/console-cloud-impl/src/main/java/top/mddata/console/facade/impl/system/ConfigFacadeImpl.java`
- Modify: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/handler/impl/UsernameLoginStrategyImpl.java:53-117`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/handler/impl/UsernameLoginStrategyImplTest.java`
- Test: `mdp-apps/md-console/console-facade/console-cloud-impl/src/test/java/top/mddata/console/facade/impl/system/ConfigFacadeImplRequiredBooleanTest.java`
- Modify: `mdp-apps/md-console/console-facade/console-cloud-impl/pom.xml`（仅补充测试依赖）

**Interfaces:**
- Consumes: `ConfigFacade.getBoolean(...)`、`User.pwExpireTime`。
- Produces: `ExceptionCode.PASSWORD_EXPIRED`，错误码 `-17`；密码错误时仍只返回原密码错误异常。

- [ ] **Step 1: 编写登录门禁失败测试**

使用 `@ExtendWith(MockitoExtension.class)` 和 `@InjectMocks UsernameLoginStrategyImpl`。构造平台用户时设置随机盐、`UserSourceEnum.PLATFORM`、`pwErrorNum=0`，密码使用 `SecureUtil.sha256("correct" + salt)`；`systemProperties.getVerifyPassword()` 固定返回 `true`。测试方法必须包含：

```java
@Test
void shouldNotRevealExpiryWhenPasswordIsWrong() {
    User user = passwordUser();
    user.setPwExpireTime(LocalDateTime.now().minusDays(1));
    LoginDto login = login("wrong");

    try (MockedStatic<SpringUtil> mocked = Mockito.mockStatic(SpringUtil.class)) {
        BizException error = assertThrows(BizException.class,
                () -> strategy.checkUserPassword(login, user));
        assertNotEquals(-17, error.getCode());
        verify(configFacade, never()).getBooleanRequired(anyString(), anyBoolean());
    }
}

@Test
void shouldRejectExpiredPasswordAfterPasswordVerified() {
    User user = passwordUser();
    user.setPwExpireTime(LocalDateTime.now().minusSeconds(1));
    when(configFacade.getBooleanRequired(ConfigKey.Workbench.PASSWORD_EXPIRE_NOT_ALLOW_LOGIN, false))
            .thenReturn(true);

    try (MockedStatic<SpringUtil> mocked = Mockito.mockStatic(SpringUtil.class)) {
        BizException error = assertThrows(BizException.class,
                () -> strategy.checkUserPassword(login("correct"), user));
        assertEquals(-17, error.getCode());
        assertEquals("密码已过期，请通过忘记密码重置后再登录", error.getMessage());
    }
}

@Test
void shouldAllowExpiredPasswordWhenSwitchIsOff() {
    User user = passwordUser();
    user.setPwExpireTime(LocalDateTime.now().minusSeconds(1));
    when(configFacade.getBooleanRequired(ConfigKey.Workbench.PASSWORD_EXPIRE_NOT_ALLOW_LOGIN, false))
            .thenReturn(false);
    assertDoesNotThrow(() -> strategy.checkUserPassword(login("correct"), user));
}
```

测试类的 `passwordUser()`、`login(String password)` 辅助方法只组装上述字段，不启动 Spring 容器。

- [ ] **Step 2: 运行测试确认旧实现不符合预期**

Run:

```bash
mvn -pl mdp-apps/md-workbench/workbench-service -am \
  -Dtest=UsernameLoginStrategyImplTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 错误密码用例因提前读取过期配置而失败，过期用例因错误码不是 `-17` 而失败。

- [ ] **Step 3: 添加错误码并移动过期判断**

在 `ExceptionCode` 的 `-16` 后添加：

```java
PASSWORD_EXPIRED(-17, "密码已过期，请通过忘记密码重置后再登录"),
```

删除 `UsernameLoginStrategyImpl` 当前密码比对前的第 64～74 行，在密码摘要比对成功后添加：

```java
boolean notAllowLogin = isPasswordExpireNotAllowLogin();
LocalDateTime now = LocalDateTime.now();
if (Boolean.TRUE.equals(notAllowLogin)
        && user.getPwExpireTime() != null
        && !now.isBefore(user.getPwExpireTime())) {
    String msg = ExceptionCode.PASSWORD_EXPIRED.getMsg();
    LoginLogDto dto = LoginLogDto.failByCheck(
                    login.getAuthType(), login.getDeviceInfo(), user.getUsername(), msg)
            .setAppKey(DefValConstants.WORKBENCH_APP_KEY)
            .setAppName(DefValConstants.WORKBENCH_APP_NAME);
    SpringUtil.publishEvent(new LoginEvent(dto));
    throw BizException.wrap(ExceptionCode.PASSWORD_EXPIRED);
}
```

严格配置读取封装为：

```java
private boolean isPasswordExpireNotAllowLogin() {
    try {
        return Boolean.TRUE.equals(configFacade.getBooleanRequired(
                ConfigKey.Workbench.PASSWORD_EXPIRE_NOT_ALLOW_LOGIN, false));
    } catch (Exception e) {
        log.error("[密码策略] 读取过期登录配置失败，拒绝本次登录", e);
        throw new BizException("读取密码安全策略失败，请稍后重试", e);
    }
}
```

`ConfigFacade` 新增 `getBooleanRequired`；boot 直接委托，cloud 对空响应或失败响应记录 key、响应 code/msg 并抛出异常，其他 `getBoolean` 调用保持原行为。

保留现有账号锁定和密码错误顺序，不修改其他登录策略。

- [ ] **Step 4: 重跑登录策略测试**

Run: 同 Step 2。

Expected: 3 个门禁用例及该测试类其他用例全部 PASS。

---

### Task 3: 用户信息返回密码状态并初始化提醒配置

**Files:**
- Modify: `mdp-apps/md-public/md-common-pojo/src/main/java/top/mddata/common/constant/ConfigKey.java:55-60`
- Modify: `mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/vo/permission/UserinfoVo.java:96-106`
- Modify: `mdp-apps/md-console/console-web/src/main/java/top/mddata/console/controller/ResourceController.java:35-92`
- Modify: `mdp-apps/md-console/console-web/pom.xml`（引入已有 md-common-config 模块以供编译）
- Test: `mdp-apps/md-console/console-web/src/test/java/top/mddata/console/controller/ResourceControllerPasswordExpiryTest.java`
- Modify: `docs/sql/mdp.sql`
- Create: `docs/sql/20261123-password-expiry-policy.sql`

**Interfaces:**
- Consumes: Task 1 的 `PasswordExpiryCalculator.Result`、`ConfigService.getInteger`。
- Produces: `UserinfoVo.passwordExpireStatus`、`passwordExpireRemainDays`、`passwordExpireRemindDays`。

- [ ] **Step 1: 编写用户信息状态测试**

使用 Mockito mock `UserService` 和 `ConfigService`，mock 静态 `StpUtil.getLoginIdAsLong()` 返回 `1L`，使用 `@InjectMocks ResourceController`。用户 `pwExpireTime=NOW.plusDays(3)`，配置返回 `7`，断言：

```java
assertEquals("EXPIRING", response.getData().getPasswordExpireStatus());
assertEquals(3L, response.getData().getPasswordExpireRemainDays());
assertEquals(7, response.getData().getPasswordExpireRemindDays());
```

再增加配置读取抛出异常用例，断言接口仍返回用户数据、提醒阈值降级为 `0`，并验证 mock logger 所在类没有向外抛出异常。

- [ ] **Step 2: 运行测试确认新字段不存在**

Run:

```bash
mvn -pl mdp-apps/md-console/console-web -am \
  -Dtest=ResourceControllerPasswordExpiryTest -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: FAIL，提示新增 getter 不存在。

- [ ] **Step 3: 添加配置键和 VO 字段**

在 `ConfigKey.Workbench` 添加：

```java
/** 密码临期提醒自然日数，0表示关闭 */
String PASSWORD_EXPIRE_REMIND_DAYS = "PASSWORD_EXPIRE_REMIND_DAYS";
```

在 `UserinfoVo` 添加：

```java
@Schema(description = "密码状态 NEVER/NORMAL/EXPIRING/EXPIRED")
private String passwordExpireStatus;

@Schema(description = "密码剩余有效自然日数，永不过期为null")
private Long passwordExpireRemainDays;

@Schema(description = "密码临期提醒阈值")
private Integer passwordExpireRemindDays;
```

- [ ] **Step 4: 在用户信息接口组装状态**

在 `ResourceController` 注入 `ConfigService`，将 `getUserinfo` 改为：

```java
User user = userService.getByIdCache(StpUtil.getLoginIdAsLong());
UserinfoVo vo = BeanUtil.toBean(user, UserinfoVo.class);
int remindDays = loadPasswordExpireRemindDays();
PasswordExpiryCalculator.Result result = PasswordExpiryCalculator.evaluate(
        LocalDateTime.now(), user.getPwExpireTime(), remindDays);
vo.setPasswordExpireStatus(result.status().name());
vo.setPasswordExpireRemainDays(result.remainDays());
vo.setPasswordExpireRemindDays(remindDays);
return R.success(vo);
```

新增私有方法：

```java
private int loadPasswordExpireRemindDays() {
    try {
        Integer days = configService.getInteger(
                ConfigKey.Workbench.PASSWORD_EXPIRE_REMIND_DAYS, 7);
        if (days == null || days < 0) {
            log.error("[密码策略] 临期提醒天数非法，value={}，降级为0", days);
            return 0;
        }
        return days;
    } catch (Exception e) {
        log.error("[密码策略] 读取临期提醒天数失败，降级为0", e);
        return 0;
    }
}
```

- [ ] **Step 5: 添加全量和增量配置 SQL**

增量脚本插入以下字段和值，其他审计字段沿用当前表默认值；执行前以 `mdc_config` 实际非空字段核对列清单：

```sql
INSERT INTO `mdc_config`
(`id`, `config_group`, `uniq_key`, `name`, `value`, `data_type`, `state`, `remark`,
 `created_at`, `created_by`, `deleted_at`)
SELECT 2026112300000000001, 'system', 'PASSWORD_EXPIRE_REMIND_DAYS',
       '密码临期提醒天数', '7', '2', b'1', '提前提醒的自然日数，0表示关闭临期提醒',
       CURRENT_TIMESTAMP(3), 0, 0
WHERE NOT EXISTS (
    SELECT 1 FROM `mdc_config` WHERE `uniq_key` = 'PASSWORD_EXPIRE_REMIND_DAYS'
);
```

在 `docs/sql/mdp.sql` 的其他密码策略配置后追加相同配置，时间使用 Dump 固定时间，不修改既有配置值。

- [ ] **Step 6: 重跑测试并检查 SQL**

Run: 同 Step 2；随后执行：

```bash
/usr/bin/grep -n 'PASSWORD_EXPIRE_REMIND_DAYS' \
  docs/sql/mdp.sql docs/sql/20261123-password-expiry-policy.sql
```

Expected: 测试 PASS；两个文件各包含一条配置定义。

---

### Task 4: 前端密码状态类型与提醒函数

**Files:**
- Modify: `packages/types/src/user.ts:4-12`
- Create: `apps/web-workbench/src/store/password-expiry.ts`
- Test: `apps/web-workbench/src/store/password-expiry.test.ts`

**Interfaces:**
- Consumes: Task 3 的四个用户信息字段。
- Produces: `isPasswordExpiredError(error)`、`isPasswordExpiredLoginError(error)`、`showPasswordExpiredLoginError(onForget)`、`showPasswordExpiryNotice(userInfo): Promise<'edit' | 'later' | null>`。

- [ ] **Step 1: 编写提醒函数失败测试**

mock `antdv-next` 的 `Modal.confirm` 和 `#/locales`，至少覆盖：

```ts
it('普通和永不过期状态不弹窗', () => {
  expect(showPasswordExpiryNotice(user('NORMAL'), vi.fn())).toBe(false);
  expect(showPasswordExpiryNotice(user('NEVER'), vi.fn())).toBe(false);
  expect(Modal.confirm).not.toHaveBeenCalled();
});

it('临期状态显示修改入口', () => {
  expect(showPasswordExpiryNotice(user('EXPIRING', 0), vi.fn())).toBe(true);
  expect(Modal.confirm).toHaveBeenCalledOnce();
});

it('已过期且阈值为0仍显示提醒', () => {
  expect(showPasswordExpiryNotice(user('EXPIRED', 0, 0), vi.fn())).toBe(true);
});

it('只根据-17识别密码过期异常', () => {
  expect(isPasswordExpiredError({ response: { data: { code: -17 } } })).toBe(true);
  expect(isPasswordExpiredError({ response: { data: { code: -12 } } })).toBe(false);
});
```

`user(...)` 辅助函数返回包含 `pwExpireTime`、`passwordExpireStatus`、`passwordExpireRemainDays`、`passwordExpireRemindDays` 的测试对象。

- [ ] **Step 2: 运行测试确认模块不存在**

Run:

```bash
pnpm vitest run apps/web-workbench/src/store/password-expiry.test.ts
```

Expected: FAIL，提示无法加载 `password-expiry`。

- [ ] **Step 3: 扩展 UserInfo 并实现提醒函数**

在 `UserInfo` 添加可选字段：

```ts
pwExpireTime?: string;
passwordExpireStatus?: 'NEVER' | 'NORMAL' | 'EXPIRING' | 'EXPIRED';
passwordExpireRemainDays?: number;
passwordExpireRemindDays?: number;
```

实现 `password-expiry.ts`：

```ts
import type { UserInfo } from '@vben/types';

import { Modal } from 'antdv-next';

import { $t } from '#/locales';

export function isPasswordExpiredError(error: unknown): boolean {
  const value = error as { response?: { data?: { code?: number } } };
  return value?.response?.data?.code === -17;
}

export function showPasswordExpiredLoginError(onForget: () => void): void {
  Modal.confirm({
    title: $t('authentication.passwordExpiry.expiredTitle'),
    content: $t('authentication.passwordExpiry.resetRequired'),
    okText: $t('authentication.passwordExpiry.forgetPassword'),
    cancelText: $t('common.cancel'),
    onOk: onForget,
  });
}

export function showPasswordExpiryNotice(
  userInfo: UserInfo,
  onEdit: () => void,
): boolean {
  const status = userInfo.passwordExpireStatus;
  if (status !== 'EXPIRED' && status !== 'EXPIRING') {
    return false;
  }
  const expired = status === 'EXPIRED';
  const content = expired
    ? $t('authentication.passwordExpiry.expiredContent')
    : userInfo.passwordExpireRemainDays === 0
      ? $t('authentication.passwordExpiry.expireToday', {
          date: userInfo.pwExpireTime,
        })
      : $t('authentication.passwordExpiry.expiringContent', {
          date: userInfo.pwExpireTime,
          days: userInfo.passwordExpireRemainDays,
        });
  Modal.confirm({
    title: $t('authentication.passwordExpiry.reminderTitle'),
    content,
    okText: $t('authentication.passwordExpiry.updateNow'),
    cancelText: $t('authentication.passwordExpiry.updateLater'),
    onOk: onEdit,
  });
  return true;
}
```

- [ ] **Step 4: 重跑前端测试**

Run: 同 Step 2。

Expected: 所有提醒函数用例 PASS。

---

### Task 5: 接入登录异常、登录成功提醒和安全页路由

**Files:**
- Modify: `apps/web-workbench/src/views/_core/sso/data/account.tsx:127-134`
- Modify: `apps/web-workbench/src/api/request.ts`（仅对 -17 抑制重复全局错误提示）
- Modify: `apps/web-workbench/src/store/auth.ts:58-89`
- Modify: `apps/web-workbench/src/views/workbench/user/security/index.vue:21-37`
- Test: `apps/web-workbench/src/store/password-expiry.test.ts`
- Test: `apps/web-workbench/src/views/workbench/user/security/index.test.ts`

**Interfaces:**
- Consumes: Task 4 的三个函数。
- Produces: 忘记密码路径 `/auth/forget-password`；修改密码路径 `/workbench/user/security?tab=password`。

- [ ] **Step 1: 添加路由与异常分支测试**

在现有安全设置 `index.test.ts` 增加 query `tab=email` 时初始页签为 `email`、非法值回退 `password` 的测试。在 `password-expiry.test.ts` 验证 `showPasswordExpiredLoginError` 的主按钮回调会调用传入的 `onForget`，且不会调用登录成功逻辑。

- [ ] **Step 2: 运行定向测试**

Run:

```bash
pnpm vitest run \
  apps/web-workbench/src/store/password-expiry.test.ts \
  apps/web-workbench/src/views/workbench/user/security/index.test.ts
```

Expected: `tab=email` 用例因当前固定使用 `password` 而失败，非法值回退用例通过。

- [ ] **Step 3: 接入密码过期登录异常**

在 `account.tsx` 引入 `router` 和 Task 4 函数，在 `catch` 最前面添加：

```ts
if (isPasswordExpiredError(error)) {
  showPasswordExpiredLoginError(() => {
    void router.push('/auth/forget-password');
  });
  return;
}
```

保留 `-12` 验证码刷新逻辑，并确保该分支位于 `finally` 前且不会调用 `completeLogin`。

- [ ] **Step 4: 在 completeLogin 接入统一提醒**

在原 loginExpired/onSuccess/defaultHome 跳转之前等待选择：

```ts
const choice = await showPasswordExpiryNotice(userInfo);
if (choice === 'edit') {
  accessStore.setLoginExpired(false);
  await router.push({
    path: '/workbench/user/security',
    query: { tab: 'password' },
  });
} else {
  // 执行原有 loginExpired/onSuccess/defaultHome 跳转分支
}
```

`later/null` 继续原跳转，`edit` 不调用原 onSuccess；该调用不得放在 `if (userInfo.name)` 内，每次 `completeLogin` 只调用一次。

- [ ] **Step 5: 支持安全设置页签 query**

使用 `useRoute` 和白名单初始化：

```ts
const route = useRoute();
const tabKeys = new Set([
  'password', 'username', 'phone', 'email', 'twoFactor', 'fingerprint', 'social',
]);
const requestedTab = String(route.query.tab || 'password');
const activeKey = ref(tabKeys.has(requestedTab) ? requestedTab : 'password');
```

不监听后续 query 变化，不修改现有社交授权回跳逻辑。

- [ ] **Step 6: 重跑定向测试**

Run: 同 Step 2。

Expected: 密码提醒及安全页签测试全部 PASS。

---

### Task 6: 安全设置密码卡片展示状态及多语言

**Files:**
- Create: `apps/web-workbench/src/views/workbench/user/security/data/password-status.ts`
- Test: `apps/web-workbench/src/views/workbench/user/security/data/password-status.test.ts`
- Modify: `apps/web-workbench/src/views/workbench/user/security/modules/password.vue`
- Modify: `packages/locales/src/langs/zh-CN/authentication.json`
- Modify: `packages/locales/src/langs/en-US/authentication.json`
- Modify: `packages/locales/src/langs/zh-CN/workbench/user/security.json`
- Modify: `packages/locales/src/langs/en-US/workbench/user/security.json`

**Interfaces:**
- Consumes: `useUserStore().userInfo.passwordExpireStatus`、`pwExpireTime` 和 Task 4 的状态模型。
- Produces: `passwordStatusText(userInfo): { key: string; params?: Record<string, string | number> }`，以及密码卡片状态区域。

- [ ] **Step 1: 添加密码状态展示测试**

在 `password-status.test.ts` 中断言新增纯展示函数分别映射：

```ts
expect(passwordStatusText(user('NORMAL')).key).toBe('expireTime');
expect(passwordStatusText(user('EXPIRING', 3))).toMatchObject({
  key: 'remainingDays',
  params: { days: 3 },
});
expect(passwordStatusText(user('EXPIRED')).key).toBe('expired');
expect(passwordStatusText(user('NEVER')).key).toBe('neverExpire');
```

运行 `pnpm vitest run apps/web-workbench/src/views/workbench/user/security/data/password-status.test.ts`，预期因模块不存在而失败。

- [ ] **Step 2: 实现状态文本函数和密码卡片区域**

新增 `password-status.ts`：

```ts
import type { UserInfo } from '@vben/types';

export interface PasswordStatusText {
  key: string;
  params?: Record<string, number | string>;
}

export function passwordStatusText(userInfo: UserInfo): PasswordStatusText {
  const date = userInfo.pwExpireTime || '';
  switch (userInfo.passwordExpireStatus) {
    case 'EXPIRED': {
      return { key: 'expired' };
    }
    case 'EXPIRING': {
      return userInfo.passwordExpireRemainDays === 0
        ? { key: 'expireToday', params: { date } }
        : {
            key: 'remainingDays',
            params: { days: userInfo.passwordExpireRemainDays || 0, date },
          };
    }
    case 'NORMAL': {
      return { key: 'expireTime', params: { date } };
    }
    default: {
      return { key: 'neverExpire' };
    }
  }
}
```

在 `password.vue` 通过 `workbench.user.security.password.` 前缀拼接 `statusText.key`，调用 `$t` 并传入 `statusText.params`；同时引入 `Alert`、`computed`、`useUserStore`：

```ts
const userStore = useUserStore();
const userInfo = computed(() => userStore.userInfo as UserInfo);
const statusText = computed(() => passwordStatusText(userInfo.value));
const alertType = computed(() =>
  userInfo.value?.passwordExpireStatus === 'EXPIRED' ? 'error' : 'warning',
);
```

在 `<Form />` 前展示状态 Alert：`EXPIRING/EXPIRED` 使用警告或错误样式；`NORMAL` 显示过期时间；`NEVER` 显示长期有效。不得修改现有表单字段、提交接口及 `fetchUserInfo()` 调用。

- [ ] **Step 3: 添加中英文案**

`authentication.json` 增加 `passwordExpiry` 节点，至少包含：

```json
{
  "expiredTitle": "密码已过期",
  "resetRequired": "密码已过期，请通过忘记密码重置后再登录",
  "forgetPassword": "忘记密码",
  "reminderTitle": "密码有效期提醒",
  "expiredContent": "密码已过期，请立即修改",
  "expireToday": "密码将于今天过期，过期时间：{date}",
  "expiringContent": "密码将于 {date} 过期，还剩 {days} 天，请及时修改",
  "updateNow": "立即修改",
  "updateLater": "稍后修改"
}
```

英文节点使用相同 key；`workbench/user/security.json` 增加密码卡片的 `expireTime`、`expireToday`、`remainingDays`、`expired`、`neverExpire` 文案。日期格式沿用接口返回值，不在模板拼接句子。

- [ ] **Step 4: 运行前端定向测试与类型检查**

Run:

```bash
pnpm vitest run \
  apps/web-workbench/src/store/password-expiry.test.ts \
  apps/web-workbench/src/views/workbench/user/security
pnpm --filter @vben/web-workbench typecheck
```

Expected: 定向测试 PASS，TypeScript 无新增错误。

---

### Task 7: 前后端联合回归与交付检查

**Files:**
- Review: 本计划涉及的全部文件。
- Verify: `docs/sql/mdp.sql`、`docs/sql/20261123-password-expiry-policy.sql`。

**Interfaces:**
- Consumes: Task 1～6 的全部交付物。
- Produces: 可人工验收的前后端密码过期闭环。

- [ ] **Step 1: 运行后端定向测试**

```bash
mvn -pl mdp-apps/md-public/md-common-config,mdp-apps/md-console/console-facade/console-cloud-impl,mdp-apps/md-workbench/workbench-service,mdp-apps/md-console/console-web -am \
  -Dtest=PasswordExpiryCalculatorTest,UsernameLoginStrategyImplTest,ConfigFacadeImplRequiredBooleanTest,ResourceControllerPasswordExpiryTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Expected: 三个测试类全部 PASS。

- [ ] **Step 2: 运行后端代码规范检查**

```bash
mvn -pl mdp-apps/md-public/md-common-config,mdp-apps/md-workbench/workbench-service,mdp-apps/md-console/console-web,mdp-apps/md-console/console-facade/console-cloud-impl \
  checkstyle:check
```

Expected: 退出码 0；若仅出现本次未触碰的历史问题，记录完整输出并单独说明，不顺手重构。

- [ ] **Step 3: 运行前端测试、类型检查和生产构建**

```bash
pnpm vitest run \
  apps/web-workbench/src/store/password-expiry.test.ts \
  apps/web-workbench/src/views/workbench/user/security
pnpm --filter @vben/web-workbench typecheck
pnpm --filter @vben/web-workbench build:boot
```

Expected: 测试 PASS、类型检查通过、生产构建成功。

- [ ] **Step 4: 静态核对需求覆盖**

```bash
/usr/bin/grep -R -n 'PASSWORD_EXPIRE_REMIND_DAYS\|PASSWORD_EXPIRED' \
  mdp-apps mdp-base docs/sql
/usr/bin/grep -R -n 'passwordExpireStatus\|showPasswordExpiryNotice\|isPasswordExpiredError' \
  apps/web-workbench/src packages/types/src/user.ts
```

Expected: 配置、后端状态、登录门禁、前端异常和提醒入口均有对应代码；不存在写死的提醒天数替代配置读取。

- [ ] **Step 5: 人工验收并如实记录**

按设计文档第 8.3 节逐项验证密码、验证码、社交和指纹登录；没有真实第三方或 WebAuthn 环境时明确标记“未执行”，不得将自动化测试描述为真实认证验收。
