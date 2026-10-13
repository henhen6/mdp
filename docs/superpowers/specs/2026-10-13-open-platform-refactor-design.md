# 开放平台重构设计文档

- 日期：2026-10-13
- 状态：待用户最终确认（确认后才允许修改代码）
- 范围：`mdp-apps/md-open`、`mdp-apps/md-gateway/sop-gateway-server`、`mdp-apps/md-server/worker-server`、`mdp-sdk`、`mdp-vben/apps/web-open`、`mdp-docs`（文档同步）

## 1. 背景与目标

开放平台（md-open）的事件推送（EVENT_PUSH）与接口回调（CALLBACK）两套体系存在约 200 行复制粘贴的投递/重试/日志代码，且与业务 CRUD 混杂在同一包下；存在同步阻塞、事务内发 HTTP、SSRF 等缺陷。

**目标**：

1. 抽取公共推送管道组件，推送域与业务 CRUD 代码分离（方案 A：领域分包）。
2. 修复摸底发现的全部确认 bug。
3. 网关调用日志异步化。
4. SDK 缺陷修复。
5. 开发者中心（web-open）新增回调/推送/调用日志自助查询。
6. **不做**：事件推送与接口回调两套体系的表合并（表结构/字段可按需调整，但两体系表必须保持独立）、console 管理端页面收敛（保持独立页面）、open-service 对 console-api 的依赖解耦（仅标记为已知问题）、任务表敏感信息加密存储（标记为遗留风险）。

## 2. 已确认决策表

| # | 决策点 | 结论 |
|---|--------|------|
| 1 | 重构范围 | md-open 回调管道 + 网关日志异步化 + SDK 修复 + 前端（仅 web-open 新增日志查询页） |
| 2 | 表结构 | 可按需调整表结构与字段设计（含加索引，如重试扫描用到的 (exec_status, next_request_time)），DDL 变更脚本随实施提供，无需数据迁移（生产无历史包袱）；**红线：事件推送与接口回调两套表不得合并** |
| 3 | 接口兼容性 | 内外部接口均可改（Facade 签名、API 路径、SDK public API） |
| 4 | 异步投递与重试调度 | 统一线程池投递 + PowerJob 调度重试（排除 RocketMQ：全工程零使用，且开源版延迟级别最大 2h，覆盖不了 6h/12h 梯度） |
| 5 | 体系合并 | 两套体系不合并，表不动，抽公共管道组件，两个 Service 变薄壳 |
| 6 | 结构方案 | 方案 A：open-service/open-web 内领域分包；open-pojo/open-dao 包名不动（避免牵连网关 import） |
| 7 | console-api 依赖 | 本次完全不动，设计文档标记为已知问题 |
| 8 | 敏感信息明文冗余 | 本次不改存储方式（风险面与 mdo_app_keys 同级，不扩大），标记遗留风险 |
| 9 | CommonsMultipartFile（网关拷贝的 Spring 类） | 验证式删除：先建立上传/下载基线测试 → 删除 → 重跑测试，不受影响才保留删除，否则还原并加注释 |
| 10 | SDK signEnabled 默认值 | 不改（保持 false） |
| 11 | console 管理端 6 个雷同页面 | 不收敛，保持独立代码 |
| 12 | 流程 | 本设计文档经用户确认后，再编写实施计划，之后才允许修改代码 |

## 3. 现状摘要（摸底结论）

### 3.1 两条推送链路

```
接口回调 CALLBACK                          事件推送 EVENT_PUSH
md-api 业务接口执行完                        console/workbench 组织、用户变更
  → NotifyAndEventPushFacade                 → NotifyAndEventPushFacade
    .apiCallNotify()                            .eventPush()
  → NotifyInfoService.notify()               → EventTriggerService.save()
    【同步 HTTP，阻塞请求线程最长 5s】             → EventPushService.saveByEventTrigger()
  → mdo_notify_info                              【查订阅 → 批量生成任务 → 线程池】
    → mdo_notify_info_log                    → mdo_event_push
  worker @Scheduled 每 10 分钟重试                → mdo_event_push_log
                                             worker @Scheduled 每 8 分钟重试
```

- 两条链路的 `doNotify` / `appendUrlParams` / `buildNextSendTime` 几乎逐字复制，仅业务字段差 3 个。
- 推送协议：HTTP POST，URL 带 signature/timestamp/nonce（+安全模式 msgSignature/encryptType），三种加密模式（明文/兼容/安全），AES-256-CBC；成功判定：HTTP 200 且响应体 `{"code":0}`；重试梯度 `2m,5m,30m,6h,12h`，最多 5 次，用尽置 RETRY_OVER，管理端可手动重推（push）/结束（end）。
- 幂等键：CALLBACK 用 callLogId，EVENT_PUSH 用 eventTriggerId。

### 3.2 网关（sop-gateway-server）

基于 SOP 框架改造的协议网关（Servlet + Dubbo 泛化调用，非 Spring Cloud Gateway）：参数解析 → ApiValidator 十项校验 → 拦截器链（CallLogRouteInterceptor 生成调用日志、TokenValidateInterceptor）→ Dubbo 泛化调用 md-api → 结果包装。`CallLogListener` **同步** insert 调用日志（在 finally 中发布事件，日志异常会冲掉业务响应）。网关直连 open-dao/open-pojo（现状承认的耦合，本次不动）。网关本身不做回调推送，仅透传 notifyUrl。

### 3.3 SDK（mdp-sdk）

`mdp-sdk-core`（OpenClient/签名/MdpBizMsgCrypt 加解密）+ `mdp-simple-sdk`（业务接口封装）。已知缺陷：`MdpBizMsgCrypt` 线程不安全、`SignUtil` 验签修改调用方入参、`parseResponse` 泛型强转风险、下载失败吞异常、测试硬编码真实密钥。

## 4. md-open 核心设计：推送域

### 4.1 包结构（open-service）

```
top.mddata.open.service/
├── notify/                          # 【推送域】与业务 CRUD 彻底分开
│   ├── pipeline/                    # 公共管道（不感知任务类型，业务无关）
│   │   ├── NotifyTaskDispatcher     # 统一线程池：接收任务ID → 提交执行
│   │   ├── NotifyTaskExecutor       # 模板方法抽象类：投递生命周期骨架
│   │   ├── NotifySender             # 加签/AES加密/HTTP POST/5s超时/结果判定
│   │   ├── RetryPolicy              # 重试梯度解析、nextRequestTime 计算
│   │   ├── NotifyUrlValidator       # SSRF 防护：仅 http/https + 内网地址拦截
│   │   └── NotifyTaskView           # 任务视图接口（适配两种任务实体）
│   ├── callback/                    # 接口回调 CALLBACK（薄壳）
│   │   ├── NotifyInfoService(Impl)      # 只管 mdo_notify_info 的状态与钩子
│   │   └── NotifyInfoLogService(Impl)
│   └── event/                       # 事件推送 EVENT_PUSH（薄壳）
│       ├── EventPushService(Impl)
│       ├── EventPushLogService(Impl)
│       ├── EventTriggerService(Impl)
│       ├── EventSubscriptionService(Impl)   # findByEventCode 从 AppKeys 迁入
│       └── EventTypeService(Impl)
├── app/      # 应用域：AppService、AppKeysService（瘦身，只剩密钥 CRUD + RSA 生成）
├── api/      # 接口域
├── doc/      # 文档域
└── oauth/    # 授权域
```

- `open-web` 的 controller 同步按域分包。
- `open-pojo` / `open-dao` 包名不动（网关直连 open-dao，包名一动网关 20+ 处 import 全要改；实体只是数据载体，留在原包不影响结构目标）。

### 4.2 管道核心：模板方法 + 适配器

两种任务实体字段几乎一致，适配为统一视图，管道只面向视图编程：

```java
public interface NotifyTaskView {
    Long getId();
    String getNotifyUrl();
    String getAppKey();
    String getMethod();
    String getType();            // CALLBACK / EVENT_PUSH
    String getBizContent();      // 各自组装的业务报文
    Integer getRequestCount();   // 已重试次数
}
```

模板方法定骨架，两个 Service 各实现 3 个钩子：

```java
public abstract class NotifyTaskExecutor<T extends NotifyTaskView> {

    public final void execute(Long taskId) {
        T task = loadTask(taskId);                       // 钩子1：取任务
        if (task == null) return;                        // 判空，修 NPE
        if (是终态(task)) return;                        // 幂等：重复提交不产生重复推送

        NotifyResult result = sender.send(task);         // 公共投递（加密/加签/HTTP）

        if (result.isSuccess())      onSuccess(task);    // 钩子2：状态流转（各自表）
        else if (还能重试(task))     onRetryableFail(task, result);   // 算 nextRequestTime
        else                         onRetryOver(task, result);

        saveLog(task, result);                           // 钩子3：写日志（各自日志表）
    }

    protected abstract T loadTask(Long id);
    protected abstract void onSuccess(T task);           // onRetryableFail / onRetryOver 同理
    protected abstract void saveLog(T task, NotifyResult result);
}
```

`NotifyInfoServiceImpl` / `EventPushServiceImpl` 继承它，从"各写 200 行管道"瘦身成"3 个钩子 + 各自表操作"。

**任务状态机**：`PENDING → SUCCESS / FAIL(算 nextRequestTime) → … → RETRY_OVER`，另有手动结束终态。终态任务不再投递（幂等保证）。

### 4.3 关键时序：事务内只写库，投递一律 afterCommit

**接口回调**（修复同步阻塞）：

```
md-api 业务线程                     md-open
    │  Facade.apiCallNotify()         │
    └──────────────────────────────▶  │ @Transactional 仅 insert mdo_notify_info（PENDING）
                                      │ afterCommit → dispatcher.submit(taskId)
    ◀────────── 立即返回 ────────────  │
                                    线程池：executor.execute(taskId) → HTTP 投递 → 状态+日志
```

**事件推送**（修复事务内提交线程池任务）：

```
业务方变更 → Facade.eventPush()
  → EventTriggerService.save()：
      @Transactional { insert event_trigger; 查订阅; batch insert event_push(PENDING) }
      afterCommit → 批量任务ID 提交 dispatcher
```

**统一规则**：事务内只写库；所有 HTTP 投递动作都在事务提交之后（`TransactionSynchronization.afterCommit`）。

### 4.4 线程池与配置

- 新建 `NotifyConfiguration` 配置类：`@EnableConfigurationProperties(NotifyProperties)` 从 `NotifyInfoServiceImpl` 挪到此处；线程池定义为 Bean（`destroyMethod = "shutdown"`），取代 `MultiThreadTaskProcessor`。
- 线程池参数全部来自 `NotifyProperties`（core/max/queue）。
- 拒绝策略改 **AbortPolicy**：捕获 `RejectedExecutionException` 后任务保持 PENDING，等下轮重试扫描兜底——调度线程不再被 HTTP 阻塞。
- `NotifyUrlValidator` 在任务生成时校验 notifyUrl：仅允许 http/https，解析 host 拦截内网 IP / localhost（SSRF 防护）；`appendUrlParams` 手拼 URL 改为 URLEncoder 编码。

### 4.5 PowerJob 重试（worker-server）

- 删除 `worker-server/configuration/DubboConfiguration.java` 中的两个裸 `@Scheduled`（8 分钟事件重推、10 分钟回调重试）；该类若只剩 Dubbo 配置则保留 Dubbo 部分并改名，或按剩余职责处理。重试不再受 `dubbo.enabled` 开关影响。
- 新增两个 PowerJob 处理器：`EventPushRetryProcessor`、`NotifyInfoRetryProcessor`（cron 沿用 8/10 分钟，可配）。
- PowerJob 单机任务模式：多实例部署天然只有一个节点执行，无需自实现分布式锁。
- 扫描逻辑改分页批量（每批 500，替代全量 loadAll），取出的任务 ID 提交 dispatcher 线程池执行。

## 5. Bug 修复清单

### 5.1 md-open

| # | 问题 | 修复方式 | 落点 |
|---|------|---------|------|
| 1 | 接口回调同步阻塞请求线程 5s | insert 即返回，afterCommit 异步投递（见 4.3） | NotifyInfoServiceImpl.notify |
| 2 | 事务内发 HTTP / 提交线程池 | 事务只写库，投递统一 afterCommit | EventPushServiceImpl |
| 3 | executeImmediately / notifyImmediately NPE | 模板方法 loadTask 判空 + 终态幂等检查 | NotifyTaskExecutor.execute |
| 4 | 重试间隔解析失败被静默当成 RETRY_OVER | RetryPolicy 启动时解析配置，非法配置启动即 fail fast 抛异常；运行期不再解析字符串 | RetryPolicy |
| 5 | 重试任务挂在 DubboConfiguration + dubbo.enabled 开关 | 删除 @Scheduled，迁移 PowerJob（见 4.5） | worker-server |
| 6 | 重试无锁、全量扫描 | PowerJob 单机模式 + 分页批量 | 两个 RetryProcessor |
| 7 | 线程池拒绝策略阻塞调度线程、参数与配置脱节 | AbortPolicy + 拒绝后留 PENDING；参数全部走 NotifyProperties | NotifyTaskDispatcher |
| 8 | SSRF：notifyUrl 无校验、URL 手拼不 encode | NotifyUrlValidator + URLEncoder | NotifyUrlValidator / NotifySender |
| 9 | token/aesKey 明文冗余任务表 | 不改存储，标记遗留风险 | — |
| 10 | workbench 6 处全发 Console.USER_EDIT 事件码 | EventTypeCode.Workbench 命名空间补充事件码，6 处调用点改正（SsoUserServiceImpl） | md-common-pojo + md-workbench |
| 11 | notify / eventTrigger/save 隐藏端点无内部保护 | 迁移到 /inner/ 前缀，鉴权配置禁止外网访问；cloud-impl Feign 回环 URL 同步更新 | open-web + open-cloud-impl |
| 12 | Controller 手建 QueryWrapper 重复约 20 处 | 查询条件构建下沉到 Query 对象或公共方法，Controller 只传参 | open-web |
| 13 | @EnableConfigurationProperties 挂在 ServiceImpl | 挪到 NotifyConfiguration | open-service |

### 5.2 网关（sop-gateway-server）

| # | 改动 | 说明 |
|---|------|------|
| 1 | 调用日志异步化 | CallLogListener 改提交独立单线程池（有界队列，拒绝时丢弃 + warn）；RouteServiceImpl.finally 中 publishEvent 包 try-catch。双保险：日志异常永远冲不掉业务响应 |
| 2 | 包名改正 | configguration → configuration，import 机械修正 |
| 3 | CommonsMultipartFile 验证式删除 | 先建立上传/下载基线测试（SDK 已有 DemoFileUploadApi/DemoFileDownloadApi，必要时补齐后端演示接口与 SDK 调用测试）→ 删除该类 → 重跑测试，上传下载不受影响才保留删除，否则还原并加注释 |
| 4 | 不动 | 校验链、拦截器链、Dubbo 泛化调用、与 open-dao 的直连依赖 |

### 5.3 SDK（mdp-sdk）

| # | 修复 | 方式 |
|---|------|------|
| 1 | MdpBizMsgCrypt 线程不安全 | encryptMsg 的 signature/msgSignature 从实例字段改为封装进返回值对象（如 EncryptResult）；public API 签名变更已获批准 |
| 2 | SignUtil.getSignCheckContentV1/V2 副作用 | 方法内 new TreeMap<>(params) 后再 remove sign，不动调用方入参 |
| 3 | parseResponse 泛型强转 | data 为数组时校验 responseClass 是否 List 兼容，不匹配抛 SdkException（具体消息），不再延迟到调用方 CCE |
| 4 | 下载失败吞异常 | 服务端返回 JSON 错误体时解析为 SdkException 抛出；IO 异常不再只 log 后返回半空结果 |
| 5 | rsaCheckV2 的 "null" 字符串 | sign 为 null 时直接返回 false |
| 6 | 测试硬编码真实密钥 | 测试类密钥/私钥改从环境变量读取，仓库内不留真实值；README 的 fastjson 描述修正。**注意：已入库密钥建议用户轮换** |
| 7 | 无用依赖 common-util | 从 mdp-sdk-core pom 移除（先验证无传递依赖使用） |
| 8 | signEnabled 默认值 | 【明确不改】保持 false |

### 5.4 错误处理统一约定

- 管道内：HTTP 超时/连接失败/响应 code≠0 归一为 NotifyResult.fail(原因)，不向线程池外抛；saveLog 自身异常 try-catch 记 error 日志，绝不冲掉状态更新。
- Service 边界：业务校验失败抛具体异常（有意义的消息），禁止 catch Exception → R.fail(e.getMessage()) 泛化。
- 线程池任务：每个任务 try-catch 兜底，异常记日志 + 任务置 FAIL 进入重试梯度，不静默吞掉。

## 6. 前端设计

### 6.1 管理端 console

【明确不做】6 个回调/日志页面保持独立代码，不抽取公共组件。

### 6.2 开发者中心 web-open：新增日志自助查询

后端 open-web 新增 client 端只读接口（登录态 + 强制按当前用户的 appId 过滤，开发者只能看自己应用的数据）：

```
/open/client/notifyInfo/page、getById      # 我的接口回调记录
/open/client/eventPush/page、getById       # 我的事件推送记录
/open/client/apiCallLog/page、getById      # 我的接口调用日志
```

前端 web-open/src/views/open/client/ 新增 3 个页面（回调记录 / 推送记录 / 调用日志），菜单挂在开发者中心"我的应用"下。页面结构参照 console 端对应页面，组件各自独立实现。

## 7. 测试策略（TDD）

| 层次 | 内容 |
|------|------|
| 管道组件单测（先写） | RetryPolicy（梯度解析、非法配置 fail fast、次数边界）；NotifyTaskExecutor 模板方法（成功/可重试失败/重试用尽/任务不存在/终态幂等 5 条路径，Mock Sender）；NotifyUrlValidator（内网 IP、非法协议、正常地址） |
| Service 集成测试 | notify/eventPush 两条链路：insert → afterCommit 投递（Mock HTTP）→ 状态与日志断言 |
| 网关 | 日志写库抛异常时业务响应仍正常返回；上传/下载基线测试（兼作 CommonsMultipartFile 删除验证） |
| SDK | MdpBizMsgCrypt 并发加解密测试；SignUtil 入参不被修改测试；上传/下载调用测试 |
| 约束 | JUnit 5（org.junit.jupiter.api.Test），关键路径覆盖 |

## 8. 实施顺序

```
① 管道组件 + 单测（RetryPolicy/Sender/Executor/Validator/Dispatcher）
② 两个 Service 薄壳改造 + 集成测试（同步阻塞、事务修复随之完成）
③ AppKeys 瘦身 + findByEventCode 迁移 + 隐藏端点 /inner/ 保护
④ worker-server：PowerJob 重试处理器，删 @Scheduled
⑤ 网关日志异步化 + 包名改正 + CommonsMultipartFile 验证式删除
⑥ SDK 缺陷修复 + 测试
⑦ web-open 日志查询（后端 client API → 前端 3 个页面）
⑧ EventTypeCode 事件码修正（md-common-pojo + workbench 调用点）
⑨ 全量构建 + 文档同步（mdp-docs：http→https 未同步处、事件清单补充；
   md-open README 的重试描述"3 次/5m,30m,6h"与代码实际"5 次/2m,5m,30m,6h,12h"不一致，一并修正）
```

## 9. 已知问题与遗留风险（本次不处理）

| 项 | 说明 |
|----|------|
| open-service 依赖 console-api | AppServiceImpl import console 的 Role/RoleAppRel/FileFacade，彻底解耦需 console 侧接口下沉 common，单独立项 |
| 任务表明文冗余 notifyToken/aesKey | 风险面与 mdo_app_keys 表同级，本次不扩大也不收敛；如需加密存储另立专项 |
| 网关直连 open-dao | 协议网关与 md-open 的 DAO 级耦合为现状架构，本次不动 |
| 网关限流/防重缺失 | api-standards.md 声称的 Sentinel 限流、nonce 防重未实现，不在本次范围 |
| SDK 测试硬编码密钥已入库 | 代码改为环境变量读取后，已泄露密钥需用户自行轮换 |
