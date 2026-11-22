# 幂等重复请求策略实现计划

> **供执行代理使用：** 必须使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans，按任务逐项执行并用复选框跟踪。

**目标：** 为 `@Idempotent` 增加按接口配置的重复请求策略，使首次成功后的相同请求可选择重放首次响应或返回重复请求错误。

**架构：** 保留现有 Redis 状态机、Lua 脚本和 `IdempotencyRepository` 接口不变。注解模块提供 `DuplicateStrategy`，MVC 拦截器在现有 `REPLAY` 分支读取策略并决定重放响应或写出 `-14` 错误。

**技术栈：** Java 17、Spring MVC、JUnit 5、Mockito、Maven、Checkstyle。

## 全局约束

- 裸 `@Idempotent` 默认使用 `REPLAY`，保持现有调用方行为不变。
- 仅改变首次成功后的重复请求行为；`PROCESSING`、`CONFLICT`、`INVALID` 语义不变。
- `REJECT` 使用业务错误码 `-14`，消息为“请求已处理，请勿重复提交”。
- 不引入 `message()`、接口级 TTL、SpEL Key 或 Service 层 AOP 幂等。
- 不修改 Redis Lua 脚本和 `IdempotencyRepository`。
- 按 TDD 先写失败测试，再写最小实现。
- 禁止执行 `git add`、`git commit`、`git push` 等 Git 写操作。

---

### Task 1: 注解策略与拦截器分发

**文件：**
- 新建：`mdp-base/md-annotation/src/main/java/top/mddata/base/annotation/idempotent/DuplicateStrategy.java`
- 修改：`mdp-base/md-annotation/src/main/java/top/mddata/base/annotation/idempotent/Idempotent.java`
- 修改：`mdp-base/md-idempotent-starter/src/main/java/top/mddata/base/idempotent/interceptor/IdempotencyInterceptor.java:33-40,89-113`
- 测试：`mdp-base/md-idempotent-starter/src/test/java/top/mddata/base/idempotent/interceptor/IdempotencyInterceptorTest.java`

**接口：**
- 消费：现有 `IdempotencyRepository.acquire(String token, String subject, String fingerprint)` 返回的 `Acquisition` 与 `Outcome.REPLAY`。
- 产出：`DuplicateStrategy { REPLAY, REJECT }`、`Idempotent.onDuplicate()`，返回类型为 `DuplicateStrategy`，默认值为 `REPLAY`。

- [x] **步骤 1：先增加策略行为测试**

在 `IdempotencyInterceptorTest` 中导入 `DuplicateStrategy`，增加 `rejectDuplicateHandler` 并在 `setUp()` 中通过 `Fixture.class.getDeclaredMethod("rejectDuplicate")` 初始化。`Fixture` 增加：

```java
@Idempotent(onDuplicate = DuplicateStrategy.REJECT)
public void rejectDuplicate() {
}
```

增加首次成功后的拒绝测试：

```java
@Test
void successReplayRejectsWhenConfigured() throws Exception {
    ContextUtil.setUserId(7L);
    when(repository.acquire(anyString(), anyString(), anyString()))
            .thenReturn(new Acquisition(Outcome.REPLAY, null,
                    new IdempotencyRepository.CachedResponse(
                            200, MediaType.APPLICATION_JSON_VALUE, "{\"code\":0,\"data\":1001}")));
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertFalse(interceptor.preHandle(
            wrappedRequest("token-1", "{}"), response, rejectDuplicateHandler));
    assertEquals(DUPLICATE_CODE, responseCodeOf(response).get("code").asInt());
    assertEquals("请求已处理，请勿重复提交", responseCodeOf(response).get("msg").asText());
}
```

增加非重放状态不受策略影响的参数化测试：

```java
@ParameterizedTest
@EnumSource(value = Outcome.class, names = {"PROCESSING", "CONFLICT", "INVALID"})
void nonReplayOutcomesIgnoreDuplicateStrategy(Outcome outcome) throws Exception {
    ContextUtil.setUserId(7L);
    when(repository.acquire(anyString(), anyString(), anyString()))
            .thenReturn(new Acquisition(outcome, null, null));
    MockHttpServletResponse response = new MockHttpServletResponse();

    assertFalse(interceptor.preHandle(
            wrappedRequest("token-1", "{}"), response, rejectDuplicateHandler));
    assertEquals(expectedCode(outcome), responseCodeOf(response).get("code").asInt());
}
```

将测试中的 `PROCESSING_CODE` 重命名为 `DUPLICATE_CODE`，并让 `expectedCode` 支持处理中状态：

```java
private static int expectedCode(Outcome outcome) {
    return switch (outcome) {
        case PROCESSING -> DUPLICATE_CODE;
        case CONFLICT -> CONFLICT_CODE;
        case INVALID -> INVALID_CODE;
        default -> throw new IllegalArgumentException("非预期的拒绝结果: " + outcome);
    };
}
```

保留现有 `successReplayWritesFirstBody` 不变，用它验证裸 `@Idempotent` 默认重放。

- [x] **步骤 2：运行定向测试并确认因缺少策略 API 而失败**

运行：

```bash
mvn -pl mdp-base/md-idempotent-starter -am \
  -Dtest=IdempotencyInterceptorTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

预期：`testCompile` 失败，提示找不到 `DuplicateStrategy` 或 `onDuplicate()`；此时不能进入实现测试通过的判断。

- [x] **步骤 3：新增策略枚举**

新建 `DuplicateStrategy.java`：

```java
package top.mddata.base.annotation.idempotent;

/** 首次请求成功后，重复请求的处理策略。 */
public enum DuplicateStrategy {
    /** 返回首次成功响应。 */
    REPLAY,
    /** 返回重复请求错误。 */
    REJECT
}
```

- [x] **步骤 4：为注解增加默认重放策略**

在 `Idempotent` 中增加：

```java
/**
 * 首次请求成功后的重复请求处理策略。
 *
 * @return 重复请求处理策略
 */
DuplicateStrategy onDuplicate() default DuplicateStrategy.REPLAY;
```

该属性与枚举同包，无需额外 import。

- [x] **步骤 5：在拦截器 REPLAY 分支应用策略**

将 `IdempotencyInterceptor` 的 `PROCESSING_CODE` 重命名为 `DUPLICATE_CODE`，值保持 `-14`，处理中分支同步使用该名称。导入 `DuplicateStrategy`，将 `REPLAY` 分支改为：

```java
case REPLAY -> {
    if (idempotent.onDuplicate() == DuplicateStrategy.REJECT) {
        responseWriter.writeError(response, DUPLICATE_CODE, "请求已处理，请勿重复提交");
    } else {
        responseWriter.replay(response, acquisition.response());
    }
    yield false;
}
```

其余分支、仓储调用和请求属性写入逻辑保持不变。

- [x] **步骤 6：运行定向测试并确认全部通过**

运行：

```bash
mvn -pl mdp-base/md-idempotent-starter -am \
  -Dtest=IdempotencyInterceptorTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

预期：`IdempotencyInterceptorTest` 全部通过，`Failures: 0, Errors: 0`，Reactor 为 `BUILD SUCCESS`。

- [x] **步骤 7：运行幂等模块及上游模块全量测试**

运行：

```bash
mvn -pl mdp-base/md-idempotent-starter -am test
```

预期：包括 `IdempotencyInterceptorTest`、`IdempotencyFieldPermIntegrationTest` 在内的所有已执行测试均为 `Failures: 0, Errors: 0`，最终 `BUILD SUCCESS`。若存在失败，必须记录具体测试和输出，不得宣称完成。

- [x] **步骤 8：执行代码规范检查**

运行：

```bash
mvn -pl mdp-base/md-idempotent-starter -am checkstyle:check
```

预期：所有模块均为 0 个 Checkstyle 违规，最终 `BUILD SUCCESS`。

- [x] **步骤 9：核对最终差异**

运行：

```bash
rtk proxy git diff -- \
  mdp-base/md-annotation/src/main/java/top/mddata/base/annotation/idempotent/Idempotent.java \
  mdp-base/md-annotation/src/main/java/top/mddata/base/annotation/idempotent/DuplicateStrategy.java \
  mdp-base/md-idempotent-starter/src/main/java/top/mddata/base/idempotent/interceptor/IdempotencyInterceptor.java \
  mdp-base/md-idempotent-starter/src/test/java/top/mddata/base/idempotent/interceptor/IdempotencyInterceptorTest.java
```

预期：差异仅包含默认策略 API、拒绝分支及对应测试，不包含仓储、Lua、TTL、提示语定制或无关重构。不执行任何 Git 提交。
