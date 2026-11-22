# 幂等注解重复请求策略设计

## 背景

当前 `@Idempotent` 接口在首次请求成功后，再次使用相同 `X-Idempotency-Key` 和相同请求体时，一律重放首次成功响应。部分接口希望改为返回重复请求错误，因此需要按接口配置该行为。

## 目标

- 保留裸 `@Idempotent` 的现有重放语义，确保向后兼容。
- 允许接口显式配置首次成功后的重复请求直接报错。
- 不改变处理中、请求指纹冲突、令牌无效等既有语义。
- 不修改 Redis Lua 脚本和仓储接口。

## 注解 API

新增 `DuplicateStrategy` 枚举：

- `REPLAY`：重放首次成功响应，作为默认值。
- `REJECT`：首次成功后，相同 Key 的重复请求返回错误。

`Idempotent` 增加属性：

```java
DuplicateStrategy onDuplicate() default DuplicateStrategy.REPLAY;
```

使用示例：

```java
@Idempotent

@Idempotent(onDuplicate = DuplicateStrategy.REJECT)
```

## 处理流程

`IdempotencyInterceptor` 读取方法上的 `@Idempotent` 后，仍调用现有 `IdempotencyRepository.acquire` 获取状态。仅在 `REPLAY` 分支增加策略判断：

| 首次请求状态 | 注解配置 | 第二次相同 Key、相同请求体的结果 |
| --- | --- | --- |
| 已成功 | 默认 `REPLAY` | 返回第一次的响应 |
| 已成功 | `REJECT` | 返回 `-14`，提示“请求已处理，请勿重复提交” |
| 处理中 | 任意配置 | 返回 `-14`，提示“相同请求正在处理中” |
| 请求体不一致 | 任意配置 | 返回 `-15` |
| Key 无效或过期 | 任意配置 | 返回 `-16` |

仓储继续负责原子状态判定与缓存响应存取，拦截器负责接口策略和 HTTP 响应分发，避免将注解语义下沉到 Redis 脚本。

## 修改范围

1. `mdp-base/md-annotation/src/main/java/top/mddata/base/annotation/idempotent/Idempotent.java`：新增 `onDuplicate` 属性。
2. `mdp-base/md-annotation/src/main/java/top/mddata/base/annotation/idempotent/DuplicateStrategy.java`：新增策略枚举。
3. `mdp-base/md-idempotent-starter/src/main/java/top/mddata/base/idempotent/interceptor/IdempotencyInterceptor.java`：在 `REPLAY` 分支应用策略。
4. `mdp-base/md-idempotent-starter/src/test/java/top/mddata/base/idempotent/interceptor/IdempotencyInterceptorTest.java`：补充行为测试。

## 测试与验收

按 TDD 先增加失败测试，再实现功能：

- 裸 `@Idempotent` 继续重放首次成功响应。
- 配置 `REJECT` 后，首次成功的重复请求返回 `-14`，不进入 Controller。
- 配置 `REJECT` 时，处理中、请求冲突、令牌无效的行为不变。
- 运行幂等模块全量测试及 Checkstyle，均通过后方可交付。
