# md-idempotent-starter · 幂等组件

基于「一次性令牌 + 请求指纹 + 结果缓存」的接口幂等保护组件，防止表单重复提交、网络超时重试导致的业务数据重复创建。

> 适用于：新增订单、提交审批、支付下单等**写操作**接口。
> 不适用于：发送验证码等响应含一次性数据的接口，以及天然幂等的查询类请求。

---

## 一、快速使用

### 1. 后端：接口标注 `@Idempotent`

```java
@PostMapping("/save")
@Idempotent                       // ← 声明本接口需要令牌幂等保护
@Operation(summary = "新增", description = "保存用户")
public R<Long> save(@Validated @RequestBody UserDto dto) {
    return R.success(superService.saveDto(dto).getId());
}
```

仅此一步。组件其余部分（令牌签发端点、过滤器、拦截器、响应缓存）由自动配置装配。

### 2. 前端：先取令牌，再携带令牌提交

```ts
// 1. 签发一次性令牌（端点：GET /{服务前缀}/anyone/idempotent/token）
const token = await requestClient.get<string>(`${prefix}/anyone/idempotent/token`);

// 2. 业务请求携带请求头 X-Idempotency-Key: {token}
await requestClient.post(`${prefix}/organization/user/save`, dto, {
  headers: { 'X-Idempotency-Key': token },
});
```

推荐封装执行器（同一令牌自动复用、失败按错误码决定是否换新令牌），参照
`apps/web-console/src/views/console/organization/user/data/form.tsx` 中的
`createIdempotencyExecutor` 用法（packages/effects/request/src/idempotency.ts 提供）。

### 3. 交互约定

| 场景 | 表现 |
|------|------|
| 令牌已签发未使用，用户重复点击 | 第二次提交内容指纹一致 → **重放第一次的成功响应** |
| 同一令牌并发双击（首次仍在处理） | 返回 -14「相同请求正在处理中，请勿重复提交」 |
| 首次业务成功后，用户修改了表单再提交（同令牌） | 返回 -15「请求内容与首次请求不一致」 |
| 令牌过期 / 已被使用后重新提交 / 身份不匹配 | 返回 -16「幂等令牌无效或已过期」 |
| 前端未携带 `X-Idempotency-Key` | 返回 -16「幂等令牌缺失」 |
| 请求体非 JSON（如 multipart 文件上传） | 返回 -6「幂等请求仅支持受控 JSON 请求」 |

业务失败（`R.code != 0`，如唯一键冲突）时令牌立即释放，调用方可修正参数后**换新令牌重新提交**。

---

## 二、组件构成

```
请求 → IdempotencyRequestFilter（Servlet Filter，order 最高+30，最先执行）
         │  命中条件：携带 X-Idempotency-Key 且 Content-Type 为 JSON
         │  动作：缓存请求体（上限 mdp.idempotent.max-request-body，默认 1MB）
         ▼
     CachedBodyRequestWrapper（body 可重复读）
         ▼
IdempotencyInterceptor（MVC 拦截器，order=-10，先于业务拦截器）
         │  ① 校验令牌存在与身份匹配
         │  ② 计算请求指纹（SHA-256：method + URI + query + body）
         │  ③ repository.acquire 按令牌状态分发（见状态机）
         ▼
Controller 执行 → IdempotencyResponseAdvice（LOWEST_PRECEDENCE）
         │  成功（R.code=0）   → complete：缓存响应，供后续重放
         │  明确业务失败       → release：释放令牌，允许换新令牌重交
         │  非 R/不确定结果   → 保持 PROCESSING，绝不自动释放
         ▼
IdempotencyTokenController：GET /anyone/idempotent/token 签发一次性令牌
```

| 组件 | 职责 |
|------|------|
| `IdempotencyRequestFilter` | 仅对携带幂等头的 JSON 请求缓存请求体；其余零包装放行 |
| `CachedBodyRequestWrapper` | body 可重复读包装（含大小上限校验，超限拒绝） |
| `IdempotencyInterceptor` | 令牌校验、指纹比对、状态分发 |
| `IdempotencyResponseAdvice` | 成功响应缓存 / 业务失败释放 / 不确定结果保守处理 |
| `IdempotencyTokenController` / `IdempotencyTokenService` | 令牌签发（一次性，绑定签发身份） |
| `RedisIdempotencyRepository` | 令牌状态与响应缓存的 Redis 仓储 |

> ⚠️ **包装顺序注意**：幂等 Filter 的 order 为最高优先级+30（先于其他 Filter），
> 后续包装型 Filter（如 XSS 的 `XssRequestWrapper`）会把缓存体包在里层。
> 拦截器因此使用 `WebUtils.getNativeRequest(request, CachedBodyRequestWrapper.class)`
> **沿包装链查找**而非直接 `instanceof`——自定义 Filter 时请保持这一约定。

---

## 三、设计原理

### 1. 状态机

令牌生命周期由 `repository.acquire` 按当前状态返回 `Acquisition.outcome`：

```
签发（UNUSED, TTL=unusedTtl 10min）
  → 首次携带令牌的请求：acquire → ACQUIRED（CAS 抢占，指纹绑定）
      → 处理中：PROCESSING（TTL=processingTtl 10min）
          → 成功响应：COMPLETE（TTL=successTtl 24h）→ 重试重放（REPLAY）
          → 明确业务失败：RELEASE → 令牌删除，可换新令牌重新签发
          → 非 R/未知异常：保持 PROCESSING，由运维介入（禁止自动释放）
  → 重复/过期/指纹不符：REPLAY / INVALID / CONFLICT
```

### 2. 请求指纹

`RequestFingerprint.sha256(request, cachedBody)`，摘要内容依次为：
**请求方法 + 请求 URI + QueryString + 请求体**。
同一令牌下，指纹用于识别「内容一致的重试」与「内容不同的误用」——前者重放，后者冲突。

### 3. 身份绑定

令牌签发时绑定签发身份（`user:{userId}` 或 `app:{appId}`），acquire 时校验当前身份与签发身份一致，
防止令牌被同应用内其他用户/应用冒用。

### 4. 为什么重试返回第一次的结果，而不是报「请求重复」？

幂等的核心场景是**超时重试**：第一次请求可能已在服务端成功（数据已创建），只是响应丢失。
若重试返回「重复」错误，调用方无法判断首次结果——对新增/下单/支付类场景会诱导换令牌重新提交，制造真正的重复数据。
返回首次响应让客户端状态确定性地收敛。这也是 Stripe / AWS 等幂等 API 的业界标准做法。

**并发与完成后重试采用不同策略**：

| 同令牌第 2 次调用的时机 | 返回 | 说明 |
|------------------------|------|------|
| 首次处理中 | -14 报「处理中」 | 并发双击需要明确拒绝 |
| 首次已成功 | ♻️ 重放首次响应 | 超时重试拿到确定结果 |
| 首次明确业务失败 | 令牌已释放，报「令牌无效」 | 失败不留可重放结果，引导换新令牌 |

### 5. 不确定结果的保守策略

Controller 抛出非业务异常（DB 超时、下游不可用等）时，**结果未知**：
既不能重放（可能没执行），也不能释放（可能已执行）。令牌保持 PROCESSING 直至 TTL 过期，
期间重试返回 -14。该状态需运维按 instance/日志人工介入——宁可挂起，不可重放或重交。

---

## 四、配置项

| 配置键 | 默认值 | 说明 |
|--------|--------|------|
| `mdp.idempotent.enabled` | `true` | 是否启用幂等组件 |
| `mdp.idempotent.key-prefix` | `mdp:idempotent:` | Redis key 前缀 |
| `mdp.idempotent.unused-ttl` | 10m | 已签发未使用令牌的存活时间 |
| `mdp.idempotent.processing-ttl` | 10m | 处理中状态的存活时间 |
| `mdp.idempotent.success-ttl` | 24h | 成功响应缓存的存活时间（重放窗口） |
| `mdp.idempotent.max-request-body` | 1MB | 允许幂等保护的最大请求体 |

---

## 五、边界与注意事项

1. **仅支持 JSON 请求**：multipart（文件上传混表单）、urlencoded 请求无法缓存请求体，
   标注 `@Idempotent` 的接口请保持 `@RequestBody` JSON 提交；文件请走独立上传接口
   （先上传文件拿 fileId，再 JSON 提交表单）。非 JSON 请求返回 -6。
2. **响应可重放是前提**：接口响应应满足「重放不产生误导」。发送验证码这类每次结果不同的接口不要加 `@Idempotent`（其防重走频次限制）。
3. **成功响应缓存 24h**：重试窗口超过 success-ttl 后，同令牌重试返回 -16；如需更长重放窗口调整 success-ttl。
4. **失败路径**：Controller 抛出 `BizException` 之外的非受检异常时令牌保持 PROCESSING（结果未知），
   由运维按日志介入——组件绝不自动释放，防止结果未知时被重放或重交。
5. **与限流的区别**：幂等防「同一意图重复生效」，限流防「请求频率超限」——两者互补，不要混用。
6. **身份绑定**：令牌与签发身份绑定（登录用户 / 第三方应用），匿名请求（无登录、无 appId）无法签发；
   令牌不可跨身份使用。
7. **GET 查询无需加**：幂等保护面向写操作；GET 的天然幂等性由 HTTP 语义保证。
8. **依赖**：Redis（令牌状态仓储）必须可用；`CachePlusOps` 缺失时幂等保护启动失败，禁止静默关闭。

---

## 六、错误码速查

| code | 常量含义 | 典型原因 | 前端处理建议 |
|------|---------|---------|-------------|
| -6 | 请求未经受控 JSON 包装 | Content-Type 非 JSON，或请求未经过幂等 Filter | 改用 JSON 提交 |
| -14 | 相同请求正在处理中 | 同令牌并发提交 | 稍后查询结果，不要立即重试 |
| -15 | 请求内容与首次请求不一致 | 同令牌但请求体被修改 | 换新令牌后重新提交 |
| -16 | 令牌缺失/无效/过期/身份不匹配 | 未取令牌、超过 unused-ttl、跨身份使用 | 重新签发令牌后提交 |

---

## 七、模块测试

```bash
mvn test -pl mdp-base/md-idempotent-starter
```

覆盖：拦截器状态机全分支、双层包装查找、指纹一致性、仓储并发抢占、响应 Advice 成功/失败/非 R 分支、
自动配置条件。测试中 Redis 交互使用嵌入式模拟，不依赖外部环境。
