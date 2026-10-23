# 开放平台推送管道重构（计划 1：md-open 推送域 + PowerJob 重试）

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 抽取公共推送管道（模板方法 + 适配器），事件推送与接口回调两套 Service 瘦身成薄壳；修复同步阻塞、事务内 HTTP、NPE、重试静默失败、SSRF 等缺陷；重试调度迁移 PowerJob。

**计划系列：** 本计划为三个计划之一（核心）。计划 2：网关日志异步化 + SDK 缺陷修复；计划 3：web-open 日志查询 + EventTypeCode 事件码修正 + 文档同步。三个计划相互独立，可依次执行。

**Architecture:** 设计文档见 `docs/superpowers/specs/2026-10-13-open-platform-refactor-design.md`。open-service/open-web 按业务域分包；`notify/pipeline` 包提供业务无关的投递管道；两个任务类型各以 View 适配器 + Executor 钩子接入；事务内只写库，投递一律 `afterCommit`。

**Tech Stack:** Java 17、Spring Boot 3、MyBatis-Flex、Hutool HttpRequest、fastjson2、md-sop-support 的 `com.gitee.sop.support.aes.MdpBizMsgCrypt`、PowerJob（worker-server 已接入）、JUnit 5 + Mockito（spring-boot-starter-test）。

## Global Constraints

- **禁止任何 git 操作**（用户全局规则），检查点以「编译/测试通过」代替提交。
- 单测使用 JUnit 5（`org.junit.jupiter.api.Test`），禁止 JUnit 4。
- 注释用简体中文，解释「为什么」；方法超 80 行拆分；错误消息必须具体。
- 表结构按需可改但**两体系表不得合并**；本计划仅新增索引（Task 13），无字段变更。
- 待删除文件（已随计划获批）：`mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/admin/processor/MultiThreadTaskProcessor.java`、`mdp-apps/md-server/worker-server/src/test/java/top/mddata/MultiThreadTaskProcessorTest.java`（Task 12 执行）。
- 行为变更须知（已含在设计中）：管理端「重新推送」由同步改为异步（状态刷新后可见）；`notify()` Facade 返回值语义从「投递完成后的 id」变为「任务入库后的 id」（唯一调用方 `OrgOpenServiceImpl.callNotify` 不使用返回值，已核实安全）。
- 设计文档 §7 的「Service 集成测试」在本计划中以「Mock Mapper 的执行器测试」实现，不引入 H2/MySQL 测试库（环境耦合，投入产出比低）。

## 关键既有事实（实施时不必再查）

- 实体字段：`NotifyInfo`/`EventPush` 均有 `notifyUrl, notifyEncryptionType, notifyToken, notifyEncodingAesKey, requestData, lastRequestTime, nextRequestTime, maxRequestCnt, requestCnt, execStatus, remark`，继承 `SuperEntity<Long>`（含 id/createdAt/createdBy/updatedAt/updatedBy），链式 setter（`@Accessors(chain = true)`）。
  - `NotifyInfo` 另有 `callLogId, appId, appKey, apiName, apiVersion`；`EventPush` 另有 `eventTriggerId, eventCode, appId, appKey`。
- `ExecStatusEnum`（String code）：`WAIT("0")`、`SUCCESS("1")`、`FAIL("2")`、`RETRY_OVER("3")`、`END("4")`。
- 日志实体字段：`NotifyInfoLog` 有 `notifyInfoId, requestData, responseData, requestTime, responseTime, errorMsg, execStatus`；`EventPushLog` 同构（外键为 `eventPushId`）。
- `MdpBizMsgCrypt`（md-sop-support）：构造 `new MdpBizMsgCrypt(token, encodingAesKey, appKey)`；`encryptMsg(plaintext, timestamp, nonce, encryptionMode)` 返回 `JSONObject`，结果签名经 `getSignature()/getMsgSignature()` 读取；静态方法 `generateTimestamp()/generateNonce()/generateToken()/generateEncodingAesKey()/appendUrlParams(baseUrl, signature, msgSignature, timestamp, nonce, encryptType)`；加密模式常量 `MODE_PLAINTEXT=0`。
- `OpenException` 构造器：`new OpenException(I18nMessage, Object...)`，项目惯例 `new OpenException(StoryMessageEnum.PARAM_VALIDATION, "具体消息")`。
- `ApiResponse.SUCCESS_CODE` 为成功码；成功判定 `JSON.parseObject(body, ApiResponse.class)` 且 code 相等。
- MyBatis-Flex：`BaseMapper.selectOneById(id)`、`insert(entity)`、`update(entity)`（按 id 忽略 null）；`QueryWrapper.create().select(...).where(...).and(...).orderBy(Xxx::getId, true).limit(n)`；`SuperServiceImpl.list/save/saveBatch/updateById/getById/updateChain` 可用。
- `NotifyProperties`（prefix `mdp.notify`）现有：`maxRetry=5, timeout=5000, timeLevel="2m,5m,30m,6h,12h", threads=8, threadTasks=50`。
- `NotifyInfoDto`：`callLogId, appId, appKey, apiName, apiVersion, charset, accessToken, clientIp, notifyUrl, bizParams(Map), remark`。
- `EventTriggerDto`：`eventCode, triggerAt, eventContent`。
- worker-server 现有 PowerJob 示例 `LogTestProcessor implements BasicProcessor`（`@Component`，`process(TaskContext)` 返回 `ProcessResult`）。
- Feign 现状：`NotifyInfoApi` POST `/admin/notifyInfo/notify`；`EventTriggerApi` POST `/admin/eventTrigger/save`（`@FeignClient(name = AppConstants.OPEN_SERVER)`）。
- open-service 目前**没有** src/test 目录和测试依赖，需新增 `spring-boot-starter-test`（test scope）。

## 目标包结构（Task 1 完成后）

```
open-service/src/main/java/top/mddata/open/service/
├── app/         AppService(AppKeysService/AppApplyService/AppGroupRelService) + impl/
├── api/         ApiService/ApiCallLogService/GroupApiRelService + impl/
├── doc/         DocGroupService/DocInfoService/DocContentService/HelpDocService + impl/
├── oauth/       OauthLogService/OauthOpenidService/OauthScopeService/OauthUnionidService/ScopeGroupService/GroupScopeRelService + impl/
├── notify/
│   ├── NotifyProperties.java            （从 service.admin.properties 迁入）
│   ├── callback/  NotifyInfoService/NotifyInfoLogService + impl/
│   ├── event/     EventPushService/EventPushLogService/EventTriggerService/EventSubscriptionService/EventTypeService + impl/
│   └── pipeline/  【Task 2-6 新建】NotifyTaskView/NotifyResult/RetryPolicy/NotifyUrlValidator/NotifySender/NotifyTaskExecutor/NotifyTaskDispatcher/NotifyConfiguration
├── convert/     （从 service.admin.convert 迁入，全域共享）
└── dashboard/   （不动）

open-web/src/main/java/top/mddata/open/controller/
├── app/    AppController/AppApplyController
├── api/    ApiController/ApiCallLogController/GroupApiRelController
├── doc/    DocGroupController/DocInfoController/HelpDocController
├── oauth/  OauthLogController/OauthOpenidController/OauthScopeController/OauthUnionidController/ScopeGroupController/GroupScopeRelController
├── notify/callback/  NotifyInfoController/NotifyInfoLogController
├── notify/event/     EventPushController/EventPushLogController/EventTriggerController/EventTypeController
├── inner/  【Task 10 新建】InnerNotifyController/InnerEventTriggerController/InnerAuthInterceptor/InnerWebMvcConfiguration
├── client/ （不动）
└── dashboard/ （不动）
```

**所有 URL 路径不变**（`@RequestMapping("/admin/xxx")` 原样保留，仅 Java 包迁移），前端零影响。

---

### Task 1: 包结构机械迁移（行为零变化）

**Files:**
- Move: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/admin/**` → 按「目标包结构」映射
- Move: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/admin/**` → 按「目标包结构」映射
- Modify: 全仓引用旧包名的 import（open-service、open-web、open-facade/open-boot-impl、md-server/worker-server）

**Interfaces:**
- Consumes: 无
- Produces: 后续所有任务基于新包名，如 `top.mddata.open.service.notify.callback.NotifyInfoService`、`top.mddata.open.service.notify.event.EventPushService`、`top.mddata.open.service.notify.NotifyProperties`

- [ ] **Step 1: 创建目标目录并移动 open-service 文件**

```bash
SVC=/Users/tangyh/gitee/mdp/mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service
mkdir -p $SVC/app/impl $SVC/api/impl $SVC/doc/impl $SVC/oauth/impl \
         $SVC/notify/callback/impl $SVC/notify/event/impl $SVC/notify/pipeline $SVC/convert

# app 域
for f in AppService AppKeysService AppApplyService AppGroupRelService; do mv $SVC/admin/$f.java $SVC/app/; done
for f in AppServiceImpl AppKeysServiceImpl AppApplyServiceImpl AppGroupRelServiceImpl; do mv $SVC/admin/impl/$f.java $SVC/app/impl/; done

# api 域
for f in ApiService ApiCallLogService GroupApiRelService; do mv $SVC/admin/$f.java $SVC/api/; done
for f in ApiServiceImpl ApiCallLogServiceImpl GroupApiRelServiceImpl; do mv $SVC/admin/impl/$f.java $SVC/api/impl/; done

# doc 域
for f in DocGroupService DocInfoService DocContentService HelpDocService; do mv $SVC/admin/$f.java $SVC/doc/; done
for f in DocGroupServiceImpl DocInfoServiceImpl DocContentServiceImpl HelpDocServiceImpl; do mv $SVC/admin/impl/$f.java $SVC/doc/impl/; done

# oauth 域
for f in OauthLogService OauthOpenidService OauthScopeService OauthUnionidService ScopeGroupService GroupScopeRelService; do mv $SVC/admin/$f.java $SVC/oauth/; done
for f in OauthLogServiceImpl OauthOpenidServiceImpl OauthScopeServiceImpl OauthUnionidServiceImpl ScopeGroupServiceImpl GroupScopeRelServiceImpl; do mv $SVC/admin/impl/$f.java $SVC/oauth/impl/; done

# notify.callback 域
for f in NotifyInfoService NotifyInfoLogService; do mv $SVC/admin/$f.java $SVC/notify/callback/; done
for f in NotifyInfoServiceImpl NotifyInfoLogServiceImpl; do mv $SVC/admin/impl/$f.java $SVC/notify/callback/impl/; done

# notify.event 域
for f in EventPushService EventPushLogService EventTriggerService EventSubscriptionService EventTypeService; do mv $SVC/admin/$f.java $SVC/notify/event/; done
for f in EventPushServiceImpl EventPushLogServiceImpl EventTriggerServiceImpl EventSubscriptionServiceImpl EventTypeServiceImpl; do mv $SVC/admin/impl/$f.java $SVC/notify/event/impl/; done

# NotifyProperties 与 convert
mv $SVC/admin/properties/NotifyProperties.java $SVC/notify/
mv $SVC/admin/convert/*.java $SVC/convert/
# processor/（MultiThreadTaskProcessor）本任务不动，Task 12 删除
```

- [ ] **Step 2: 移动 open-web 控制器**

```bash
CTL=/Users/tangyh/gitee/mdp/mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller
mkdir -p $CTL/app $CTL/api $CTL/doc $CTL/oauth $CTL/notify/callback $CTL/notify/event $CTL/inner

for f in AppController AppApplyController; do mv $CTL/admin/$f.java $CTL/app/; done
for f in ApiController ApiCallLogController GroupApiRelController; do mv $CTL/admin/$f.java $CTL/api/; done
for f in DocGroupController DocInfoController HelpDocController; do mv $CTL/admin/$f.java $CTL/doc/; done
for f in OauthLogController OauthOpenidController OauthScopeController OauthUnionidController ScopeGroupController GroupScopeRelController; do mv $CTL/admin/$f.java $CTL/oauth/; done
for f in NotifyInfoController NotifyInfoLogController; do mv $CTL/admin/$f.java $CTL/notify/callback/; done
for f in EventPushController EventPushLogController EventTriggerController EventTypeController; do mv $CTL/admin/$f.java $CTL/notify/event/; done
```

- [ ] **Step 3: 批量改写 package 声明与全仓 import**

对已移动文件，逐个将 `package top.mddata.open.service.admin;` / `...admin.impl;` / `package top.mddata.open.controller.admin;` 改为对应新包（可用 IDE 的 Move 重构自动完成 Step 1-3；若手工执行，用下面的 sed 脚本）。

```bash
# 对移动后的文件改 package 声明（示例：app 域，其余域同理替换目录名）
sed -i '' 's/^package top\.mddata\.open\.service\.admin;$/package top.mddata.open.service.app;/' $SVC/app/*.java
sed -i '' 's/^package top\.mddata\.open\.service\.admin\.impl;$/package top.mddata.open.service.app.impl;/' $SVC/app/impl/*.java
# ... 对 api/doc/oauth/notify.callback/notify.event 执行同样两条（包名段换成 api/doc/oauth/notify.callback/notify.event）
sed -i '' 's/^package top\.mddata\.open\.service\.admin\.properties;$/package top.mddata.open.service.notify;/' $SVC/notify/NotifyProperties.java
sed -i '' 's/^package top\.mddata\.open\.service\.admin\.convert;$/package top.mddata.open.service.convert;/' $SVC/convert/*.java
for d in app api doc oauth; do sed -i '' "s/^package top\.mddata\.open\.controller\.admin;$/package top.mddata.open.controller.$d;/" $CTL/$d/*.java; done
sed -i '' 's/^package top\.mddata\.open\.controller\.admin;$/package top.mddata.open.controller.notify.callback;/' $CTL/notify/callback/*.java
sed -i '' 's/^package top\.mddata\.open\.controller\.admin;$/package top.mddata.open.controller.notify.event;/' $CTL/notify/event/*.java

# 全仓 import 重写（mdp-apps 整个目录）
cd /Users/tangyh/gitee/mdp/mdp-apps
rewrite() { grep -rl --include="*.java" "$1" . | xargs sed -i '' "s/$1/$2/g"; }
rewrite 'top\.mddata\.open\.service\.admin\.AppKeysService'        'top.mddata.open.service.app.AppKeysService'
rewrite 'top\.mddata\.open\.service\.admin\.AppService'            'top.mddata.open.service.app.AppService'
rewrite 'top\.mddata\.open\.service\.admin\.AppApplyService'       'top.mddata.open.service.app.AppApplyService'
rewrite 'top\.mddata\.open\.service\.admin\.AppGroupRelService'    'top.mddata.open.service.app.AppGroupRelService'
rewrite 'top\.mddata\.open\.service\.admin\.ApiCallLogService'     'top.mddata.open.service.api.ApiCallLogService'
rewrite 'top\.mddata\.open\.service\.admin\.ApiService'            'top.mddata.open.service.api.ApiService'
rewrite 'top\.mddata\.open\.service\.admin\.GroupApiRelService'    'top.mddata.open.service.api.GroupApiRelService'
rewrite 'top\.mddata\.open\.service\.admin\.DocGroupService'       'top.mddata.open.service.doc.DocGroupService'
rewrite 'top\.mddata\.open\.service\.admin\.DocInfoService'        'top.mddata.open.service.doc.DocInfoService'
rewrite 'top\.mddata\.open\.service\.admin\.DocContentService'     'top.mddata.open.service.doc.DocContentService'
rewrite 'top\.mddata\.open\.service\.admin\.HelpDocService'        'top.mddata.open.service.doc.HelpDocService'
rewrite 'top\.mddata\.open\.service\.admin\.OauthLogService'       'top.mddata.open.service.oauth.OauthLogService'
rewrite 'top\.mddata\.open\.service\.admin\.OauthOpenidService'    'top.mddata.open.service.oauth.OauthOpenidService'
rewrite 'top\.mddata\.open\.service\.admin\.OauthScopeService'     'top.mddata.open.service.oauth.OauthScopeService'
rewrite 'top\.mddata\.open\.service\.admin\.OauthUnionidService'   'top.mddata.open.service.oauth.OauthUnionidService'
rewrite 'top\.mddata\.open\.service\.admin\.ScopeGroupService'     'top.mddata.open.service.oauth.ScopeGroupService'
rewrite 'top\.mddata\.open\.service\.admin\.GroupScopeRelService'  'top.mddata.open.service.oauth.GroupScopeRelService'
rewrite 'top\.mddata\.open\.service\.admin\.NotifyInfoLogService'  'top.mddata.open.service.notify.callback.NotifyInfoLogService'
rewrite 'top\.mddata\.open\.service\.admin\.NotifyInfoService'     'top.mddata.open.service.notify.callback.NotifyInfoService'
rewrite 'top\.mddata\.open\.service\.admin\.EventPushLogService'   'top.mddata.open.service.notify.event.EventPushLogService'
rewrite 'top\.mddata\.open\.service\.admin\.EventPushService'      'top.mddata.open.service.notify.event.EventPushService'
rewrite 'top\.mddata\.open\.service\.admin\.EventTriggerService'   'top.mddata.open.service.notify.event.EventTriggerService'
rewrite 'top\.mddata\.open\.service\.admin\.EventSubscriptionService' 'top.mddata.open.service.notify.event.EventSubscriptionService'
rewrite 'top\.mddata\.open\.service\.admin\.EventTypeService'      'top.mddata.open.service.notify.event.EventTypeService'
rewrite 'top\.mddata\.open\.service\.admin\.properties\.NotifyProperties' 'top.mddata.open.service.notify.NotifyProperties'
rewrite 'top\.mddata\.open\.service\.admin\.convert'               'top.mddata.open.service.convert'
```

注意：`rewrite` 必须按上表顺序执行（先长后短，如 `NotifyInfoLogService` 先于 `NotifyInfoService`，`AppKeysService` 先于 `AppService`，避免子串误伤）。执行后复查：`grep -rn "service.admin" --include="*.java" . | grep -v "service.admin.processor"`，应只剩 processor 包自身。

- [ ] **Step 4: 编译验证**

```bash
cd /Users/tangyh/gitee/mdp
mvn clean install -DskipTests -pl mdp-apps/md-open/open-service,mdp-apps/md-open/open-web,mdp-apps/md-open/open-facade/open-boot-impl,mdp-apps/md-open/open-facade/open-cloud-impl,mdp-apps/md-server/worker-server -am
```

预期：BUILD SUCCESS。若有漏网 import，按报错逐个修正。

---

### Task 2: NotifyProperties 扩展 + RetryPolicy（TDD）

**Files:**
- Modify: `mdp-apps/md-open/open-service/pom.xml`（新增测试依赖）
- Modify: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/NotifyProperties.java`
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/pipeline/RetryPolicy.java`
- Test: `mdp-apps/md-open/open-service/src/test/java/top/mddata/open/service/notify/pipeline/RetryPolicyTest.java`

**Interfaces:**
- Consumes: 无
- Produces: `new RetryPolicy(String timeLevel)`（非法配置构造即抛 `IllegalArgumentException`）；`LocalDateTime nextRequestTime(int requestCnt, LocalDateTime now)`（返回 null = 次数用尽）；`int getMaxRetry()`

- [ ] **Step 1: open-service/pom.xml 的 `<dependencies>` 末尾新增**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: NotifyProperties 修改**

删除 `threads`、`threadTasks` 两个字段（yml 中的残留 key 会被 Spring 忽略，不报错），新增：

```java
/** 投递线程池核心线程数 */
private Integer corePoolSize = 8;
/** 投递线程池最大线程数 */
private Integer maxPoolSize = 16;
/** 投递线程池队列容量（满后拒绝，任务留待重试扫描兜底） */
private Integer queueCapacity = 500;
```

- [ ] **Step 3: 写失败测试 `RetryPolicyTest`**

```java
package top.mddata.open.service.notify.pipeline;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryPolicyTest {

    @Test
    void 按梯度计算下次重试时间() {
        RetryPolicy policy = new RetryPolicy("2m,5m,30m,6h,12h");
        LocalDateTime now = LocalDateTime.of(2026, 10, 13, 10, 30, 45);
        // 时间被截断到分钟，再叠加梯度
        assertEquals(LocalDateTime.of(2026, 10, 13, 10, 32, 0), policy.nextRequestTime(1, now));
        assertEquals(LocalDateTime.of(2026, 10, 13, 10, 35, 0), policy.nextRequestTime(2, now));
        assertEquals(LocalDateTime.of(2026, 10, 13, 16, 30, 0), policy.nextRequestTime(4, now));
        assertEquals(LocalDateTime.of(2026, 10, 13, 22, 30, 0), policy.nextRequestTime(5, now));
    }

    @Test
    void 次数用尽返回null() {
        RetryPolicy policy = new RetryPolicy("2m,5m");
        // 梯度共 2 级：第 2 次失败后即用尽
        assertNull(policy.nextRequestTime(2, LocalDateTime.now()));
        assertNull(policy.nextRequestTime(3, LocalDateTime.now()));
        assertEquals(2, policy.getMaxRetry());
    }

    @Test
    void 非法配置启动即失败() {
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(""));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(null));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy("2x"));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy("m"));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy("abc m"));
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy("-5m"));
    }
}
```

- [ ] **Step 4: 跑测试确认失败**

```bash
mvn test -pl mdp-apps/md-open/open-service -Dtest=RetryPolicyTest
```

预期：编译失败（RetryPolicy 不存在）。

- [ ] **Step 5: 实现 `RetryPolicy`**

```java
package top.mddata.open.service.notify.pipeline;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 重试策略：构造时解析重试梯度配置，非法配置立即 fail fast（避免运行期配置错误被静默当成"重试用尽"）。
 * 配置格式示例："2m,5m,30m,6h,12h"，单位仅支持 m(分)/h(时)/d(天)。
 */
public class RetryPolicy {

    private final List<Duration> intervals;

    public RetryPolicy(String timeLevel) {
        this.intervals = parse(timeLevel);
    }

    static List<Duration> parse(String timeLevel) {
        if (timeLevel == null || timeLevel.isBlank()) {
            throw new IllegalArgumentException("重试间隔配置不能为空，格式示例：2m,5m,30m,6h,12h");
        }
        String[] parts = timeLevel.split(",");
        List<Duration> result = new ArrayList<>(parts.length);
        for (String part : parts) {
            result.add(parseOne(part.trim().toLowerCase(), part));
        }
        return List.copyOf(result);
    }

    private static Duration parseOne(String exp, String origin) {
        if (exp.length() < 2) {
            throw new IllegalArgumentException("重试间隔配置项非法：" + origin);
        }
        long value;
        try {
            value = Long.parseLong(exp.substring(0, exp.length() - 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("重试间隔配置项数值非法：" + origin, e);
        }
        if (value <= 0) {
            throw new IllegalArgumentException("重试间隔必须为正数：" + origin);
        }
        return switch (exp.charAt(exp.length() - 1)) {
            case 'm' -> Duration.ofMinutes(value);
            case 'h' -> Duration.ofHours(value);
            case 'd' -> Duration.ofDays(value);
            default -> throw new IllegalArgumentException("重试间隔单位仅支持 m/h/d：" + origin);
        };
    }

    /**
     * 计算下次重试时间。
     *
     * @param requestCnt 已投递次数（含本次）
     * @param now        当前时间
     * @return null 表示重试次数已用尽
     */
    public LocalDateTime nextRequestTime(int requestCnt, LocalDateTime now) {
        if (requestCnt >= intervals.size()) {
            return null;
        }
        return now.withSecond(0).withNano(0).plus(intervals.get(requestCnt - 1));
    }

    /** 最大投递次数 = 梯度级数 */
    public int getMaxRetry() {
        return intervals.size();
    }
}
```

- [ ] **Step 6: 跑测试确认通过**

```bash
cd /Users/tangyh/gitee/mdp && mvn test -pl mdp-apps/md-open/open-service -Dtest=RetryPolicyTest
```

预期：Tests run: 3, Failures: 0。

---

### Task 3: NotifyUrlValidator（TDD，SSRF 防护）

**Files:**
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/pipeline/NotifyUrlValidator.java`
- Test: `mdp-apps/md-open/open-service/src/test/java/top/mddata/open/service/notify/pipeline/NotifyUrlValidatorTest.java`

**Interfaces:**
- Consumes: `com.gitee.sop.support.exception.OpenException(String subCode, String subMsg)`
- Produces: `NotifyUrlValidator.validate(String notifyUrl)`，非法地址抛 `OpenException`，合法静默通过

- [ ] **Step 1: 写失败测试**

```java
package top.mddata.open.service.notify.pipeline;

import com.gitee.sop.support.exception.OpenException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotifyUrlValidatorTest {

    @Test
    void 合法地址通过() {
        assertDoesNotThrow(() -> NotifyUrlValidator.validate("https://open.example.com/notify"));
        assertDoesNotThrow(() -> NotifyUrlValidator.validate("http://8.8.8.8:8080/callback"));
    }

    @Test
    void 空地址与非法协议被拒绝() {
        assertThrows(OpenException.class, () -> NotifyUrlValidator.validate(null));
        assertThrows(OpenException.class, () -> NotifyUrlValidator.validate("  "));
        assertThrows(OpenException.class, () -> NotifyUrlValidator.validate("ftp://example.com/x"));
        assertThrows(OpenException.class, () -> NotifyUrlValidator.validate("not-a-url"));
    }

    @Test
    void 内网地址被拒绝() {
        assertThrows(OpenException.class, () -> NotifyUrlValidator.validate("http://127.0.0.1:8080/x"));
        assertThrows(OpenException.class, () -> NotifyUrlValidator.validate("http://localhost/x"));
        assertThrows(OpenException.class, () -> NotifyUrlValidator.validate("http://192.168.1.10/x"));
        assertThrows(OpenException.class, () -> NotifyUrlValidator.validate("http://10.0.0.5/x"));
        assertThrows(OpenException.class, () -> NotifyUrlValidator.validate("http://[::1]/x"));
        assertThrows(OpenException.class, () -> NotifyUrlValidator.validate("http://0.0.0.0/x"));
    }
}
```

- [ ] **Step 2: 跑测试确认编译失败**（类不存在）

```bash
cd /Users/tangyh/gitee/mdp && mvn test -pl mdp-apps/md-open/open-service -Dtest=NotifyUrlValidatorTest
```

- [ ] **Step 3: 实现**

```java
package top.mddata.open.service.notify.pipeline;

import com.gitee.sop.support.exception.OpenException;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * 回调地址安全校验：notifyUrl 由开发者自助配置，投递前必须防 SSRF。
 * 仅允许 http/https，且解析后的所有 IP 均不得为内网/回环/链路本地地址。
 */
public final class NotifyUrlValidator {

    private static final String ERR_CODE = "NOTIFY_URL_INVALID";

    private NotifyUrlValidator() {
    }

    public static void validate(String notifyUrl) {
        if (notifyUrl == null || notifyUrl.isBlank()) {
            throw new OpenException(ERR_CODE, "回调地址不能为空");
        }
        URI uri;
        try {
            uri = URI.create(notifyUrl.trim());
        } catch (IllegalArgumentException e) {
            throw new OpenException(ERR_CODE, "回调地址格式非法：" + notifyUrl);
        }
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new OpenException(ERR_CODE, "回调地址仅支持 http/https 协议：" + notifyUrl);
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new OpenException(ERR_CODE, "回调地址缺少主机名：" + notifyUrl);
        }
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new OpenException(ERR_CODE, "回调地址主机无法解析：" + host);
        }
        for (InetAddress address : addresses) {
            if (address.isLoopbackAddress() || address.isSiteLocalAddress()
                    || address.isAnyLocalAddress() || address.isLinkLocalAddress()
                    || address.isMulticastAddress()) {
                throw new OpenException(ERR_CODE, "回调地址不允许指向内网地址：" + host);
            }
        }
    }
}
```

- [ ] **Step 4: 跑测试确认通过**

```bash
cd /Users/tangyh/gitee/mdp && mvn test -pl mdp-apps/md-open/open-service -Dtest=NotifyUrlValidatorTest
```

预期：Tests run: 3, Failures: 0。（注意：测试依赖 DNS 解析 localhost/[::1]，离线环境 `8.8.8.8` 用例不依赖 DNS，可正常跑。）

---

### Task 4: NotifyResult + NotifyTaskView + NotifySender（TDD）

**Files:**
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/pipeline/NotifyResult.java`
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/pipeline/NotifyTaskView.java`
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/pipeline/NotifySender.java`
- Test: `mdp-apps/md-open/open-service/src/test/java/top/mddata/open/service/notify/pipeline/NotifySenderTest.java`

**Interfaces:**
- Consumes: `MdpBizMsgCrypt`、`NotifyProperties.getTimeout()`、`ApiResponse.SUCCESS_CODE`
- Produces:
  - `NotifyTaskView`（接口，方法见下）——Task 7/8 的两个适配器实现它
  - `NotifyResult.ok(...)` / `NotifyResult.fail(...)`，getter：`isSuccess(), getErrorMsg(), getRequestUrl(), getRequestBody(), getResponseBody(), getRequestTime(), getResponseTime()`
  - `new NotifySender(NotifyProperties)`；`NotifyResult send(NotifyTaskView task)`——任何失败归一为 `NotifyResult.fail`，**不向外抛异常**

- [ ] **Step 1: 创建 `NotifyTaskView` 接口**

```java
package top.mddata.open.service.notify.pipeline;

import java.time.LocalDateTime;

/**
 * 推送任务视图：公共管道只面向该接口编程，不感知具体任务表（mdo_notify_info / mdo_event_push）。
 * 两种任务实体各写一个适配器实现本接口。
 */
public interface NotifyTaskView {

    Long getId();

    String getNotifyUrl();

    String getAppKey();

    Integer getNotifyEncryptionType();

    String getNotifyToken();

    String getNotifyEncodingAesKey();

    Integer getRequestCnt();

    String getExecStatus();

    /** 组装业务报文（明文 JSON），两种任务的字段差异在此封装 */
    String buildPlaintext();

    /** 推进一次投递计数：requestCnt + 1，lastRequestTime = now */
    void markDelivering(LocalDateTime now);

    /** 更新执行状态与下次重试时间 */
    void markStatus(String execStatus, LocalDateTime nextRequestTime);
}
```

- [ ] **Step 2: 创建 `NotifyResult`**

```java
package top.mddata.open.service.notify.pipeline;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 一次投递的结果（不可变）。成功与失败的请求/响应报文都保留，供写日志使用。
 */
@Getter
public final class NotifyResult {

    private final boolean success;
    private final String errorMsg;
    private final String requestUrl;
    private final String requestBody;
    private final String responseBody;
    private final LocalDateTime requestTime;
    private final LocalDateTime responseTime;

    private NotifyResult(boolean success, String errorMsg, String requestUrl, String requestBody,
                         String responseBody, LocalDateTime requestTime, LocalDateTime responseTime) {
        this.success = success;
        this.errorMsg = errorMsg;
        this.requestUrl = requestUrl;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.requestTime = requestTime;
        this.responseTime = responseTime;
    }

    public static NotifyResult ok(String requestUrl, String requestBody, String responseBody,
                                  LocalDateTime requestTime, LocalDateTime responseTime) {
        return new NotifyResult(true, "", requestUrl, requestBody, responseBody, requestTime, responseTime);
    }

    public static NotifyResult fail(String errorMsg, String requestUrl, String requestBody, String responseBody,
                                    LocalDateTime requestTime, LocalDateTime responseTime) {
        return new NotifyResult(false, errorMsg, requestUrl, requestBody, responseBody, requestTime, responseTime);
    }
}
```

- [ ] **Step 3: 写失败测试 `NotifySenderTest`（用 JDK 自带 HttpServer，不引新依赖）**

```java
package top.mddata.open.service.notify.pipeline;

import com.gitee.sop.support.aes.MdpBizMsgCrypt;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.mddata.open.service.notify.NotifyProperties;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotifySenderTest {

    private HttpServer server;
    private NotifySender sender;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/ok", exchange -> respond(exchange, "{\"code\":0,\"msg\":\"\"}"));
        server.createContext("/bizFail", exchange -> respond(exchange, "{\"code\":1,\"msg\":\"no\"}"));
        server.createContext("/notJson", exchange -> respond(exchange, "not-json"));
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            respond(exchange, "{\"code\":0,\"msg\":\"\"}");
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        NotifyProperties properties = new NotifyProperties();
        properties.setTimeout(500);
        sender = new NotifySender(properties);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private void respond(com.sun.net.httpserver.HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private NotifyTaskView newTask(String url) {
        return new NotifyTaskView() {
            @Override public Long getId() { return 1L; }
            @Override public String getNotifyUrl() { return url; }
            @Override public String getAppKey() { return "testAppKey"; }
            @Override public Integer getNotifyEncryptionType() { return 0; }
            @Override public String getNotifyToken() { return MdpBizMsgCrypt.generateToken(); }
            @Override public String getNotifyEncodingAesKey() { return MdpBizMsgCrypt.generateEncodingAesKey(); }
            @Override public Integer getRequestCnt() { return 1; }
            @Override public String getExecStatus() { return "0"; }
            @Override public String buildPlaintext() { return "{\"type\":\"CALLBACK\"}"; }
            @Override public void markDelivering(LocalDateTime now) { }
            @Override public void markStatus(String execStatus, LocalDateTime nextRequestTime) { }
        };
    }

    @Test
    void 对方返回code0判定成功() {
        NotifyResult result = sender.send(newTask(baseUrl + "/ok"));
        assertTrue(result.isSuccess());
        assertEquals("{\"code\":0,\"msg\":\"\"}", result.getResponseBody());
    }

    @Test
    void 对方返回业务失败码判定失败() {
        NotifyResult result = sender.send(newTask(baseUrl + "/bizFail"));
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMsg().contains("no"));
    }

    @Test
    void 对方返回非JSON判定失败() {
        NotifyResult result = sender.send(newTask(baseUrl + "/notJson"));
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMsg().contains("格式"));
    }

    @Test
    void 超时判定失败且不抛异常() {
        NotifyResult result = sender.send(newTask(baseUrl + "/slow"));
        assertFalse(result.isSuccess());
    }
}
```

- [ ] **Step 4: 跑测试确认编译失败**（NotifySender 不存在）

- [ ] **Step 5: 实现 `NotifySender`**

```java
package top.mddata.open.service.notify.pipeline;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.gitee.sop.support.aes.AesException;
import com.gitee.sop.support.aes.MdpBizMsgCrypt;
import com.gitee.sop.support.message.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mddata.open.enumeration.admin.NotifyEncryptionTypeEnum;
import top.mddata.open.service.notify.NotifyProperties;

import java.time.LocalDateTime;

/**
 * 统一投递器：加签/加密 → HTTP POST → 结果判定。
 * 所有失败（加密失败、超时、连接失败、业务 code 非 0、响应非 JSON）归一为 NotifyResult.fail，不向外抛异常，
 * 由调用方（NotifyTaskExecutor）统一做状态流转，避免两份复制粘贴的 doNotify。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifySender {

    private final NotifyProperties notifyProperties;

    public NotifyResult send(NotifyTaskView task) {
        LocalDateTime requestTime = LocalDateTime.now();
        String requestUrl;
        String requestBody;
        try {
            int encryptionMode = task.getNotifyEncryptionType() != null
                    ? task.getNotifyEncryptionType() : NotifyEncryptionTypeEnum.PLAINTEXT.getCode();
            MdpBizMsgCrypt crypt = new MdpBizMsgCrypt(
                    task.getNotifyToken(), task.getNotifyEncodingAesKey(), task.getAppKey());
            String timestamp = MdpBizMsgCrypt.generateTimestamp();
            String nonce = MdpBizMsgCrypt.generateNonce();
            JSONObject body = crypt.encryptMsg(task.buildPlaintext(), timestamp, nonce, encryptionMode);
            requestBody = body.toJSONString();
            requestUrl = MdpBizMsgCrypt.appendUrlParams(task.getNotifyUrl(),
                    crypt.getSignature(), crypt.getMsgSignature(), timestamp, nonce,
                    encryptionMode == NotifyEncryptionTypeEnum.PLAINTEXT.getCode() ? null : "aes");
        } catch (AesException e) {
            return NotifyResult.fail("消息加密失败：" + e.getMessage(),
                    task.getNotifyUrl(), null, null, requestTime, LocalDateTime.now());
        }

        log.info("[推送] 发送请求, taskId={}, url={}", task.getId(), requestUrl);
        try (HttpResponse response = HttpRequest.post(requestUrl)
                .timeout(notifyProperties.getTimeout())
                .body(requestBody)
                .execute()) {
            String responseBody = response.body();
            return judge(requestUrl, requestBody, responseBody, requestTime);
        } catch (Exception e) {
            log.warn("[推送] 请求异常, taskId={}, url={}", task.getId(), requestUrl, e);
            return NotifyResult.fail("请求异常：" + e.getMessage(),
                    requestUrl, requestBody, null, requestTime, LocalDateTime.now());
        }
    }

    /** 成功判定：HTTP 200 且响应体能解析出 code=成功码 */
    private NotifyResult judge(String requestUrl, String requestBody, String responseBody, LocalDateTime requestTime) {
        LocalDateTime responseTime = LocalDateTime.now();
        try {
            ApiResponse r = JSON.parseObject(responseBody, ApiResponse.class);
            if (r != null && ApiResponse.SUCCESS_CODE.equals(r.getCode())) {
                return NotifyResult.ok(requestUrl, requestBody, responseBody, requestTime, responseTime);
            }
            return NotifyResult.fail("对方返回失败：" + responseBody,
                    requestUrl, requestBody, responseBody, requestTime, responseTime);
        } catch (JSONException e) {
            return NotifyResult.fail("返回值格式错误，请返回正确格式：{\"code\": 0, \"msg\": \"\"}",
                    requestUrl, requestBody, responseBody, requestTime, responseTime);
        }
    }
}
```

- [ ] **Step 6: 跑测试确认通过**

```bash
cd /Users/tangyh/gitee/mdp && mvn test -pl mdp-apps/md-open/open-service -Dtest=NotifySenderTest
```

预期：Tests run: 4, Failures: 0。

---

### Task 5: NotifyTaskExecutor 模板方法（TDD）

**Files:**
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/pipeline/NotifyTaskExecutor.java`
- Test: `mdp-apps/md-open/open-service/src/test/java/top/mddata/open/service/notify/pipeline/NotifyTaskExecutorTest.java`

**Interfaces:**
- Consumes: `NotifySender.send`、`RetryPolicy.nextRequestTime`、`ExecStatusEnum`（SUCCESS/FAIL/RETRY_OVER/END/WAIT）
- Produces: 抽象类 `NotifyTaskExecutor<T extends NotifyTaskView>`，公开 final 方法 `execute(Long taskId)`；钩子 `loadTask(Long)`、`persist(T)`、`appendLog(T, NotifyResult)`。Task 7/8 的两个执行器继承它。

- [ ] **Step 1: 写失败测试（内存 Fake + Mockito Mock Sender）**

```java
package top.mddata.open.service.notify.pipeline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.mddata.open.enumeration.admin.ExecStatusEnum;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotifyTaskExecutorTest {

    /** 内存任务：记录状态推进 */
    static class FakeTask implements NotifyTaskView {
        Long id = 1L;
        String execStatus = ExecStatusEnum.WAIT.getCode();
        int requestCnt = 0;
        LocalDateTime lastRequestTime;
        LocalDateTime nextRequestTime;

        @Override public Long getId() { return id; }
        @Override public String getNotifyUrl() { return "http://x"; }
        @Override public String getAppKey() { return "ak"; }
        @Override public Integer getNotifyEncryptionType() { return 0; }
        @Override public String getNotifyToken() { return "t"; }
        @Override public String getNotifyEncodingAesKey() { return "k"; }
        @Override public Integer getRequestCnt() { return requestCnt; }
        @Override public String getExecStatus() { return execStatus; }
        @Override public String buildPlaintext() { return "{}"; }
        @Override public void markDelivering(LocalDateTime now) { requestCnt++; lastRequestTime = now; }
        @Override public void markStatus(String status, LocalDateTime next) { execStatus = status; nextRequestTime = next; }
    }

    /** 内存执行器：loadTask 返回预置任务，persist/appendLog 记录调用 */
    static class FakeExecutor extends NotifyTaskExecutor<FakeTask> {
        FakeTask task;
        boolean persisted;
        NotifyResult loggedResult;
        String loggedStatus;

        FakeExecutor(NotifySender sender, FakeTask task) {
            super(sender, new RetryPolicy("2m,5m"));
            this.task = task;
        }

        @Override protected FakeTask loadTask(Long taskId) { return task; }
        @Override protected void persist(FakeTask t) { persisted = true; }
        @Override protected void appendLog(FakeTask t, NotifyResult result) {
            loggedResult = result;
            loggedStatus = t.getExecStatus();
        }
    }

    private NotifySender sender;

    @BeforeEach
    void setUp() {
        sender = mock(NotifySender.class);
    }

    @Test
    void 任务不存在直接跳过() {
        FakeExecutor executor = new FakeExecutor(sender, null);
        executor.execute(99L);
        verify(sender, never()).send(any());
    }

    @Test
    void 终态任务幂等跳过() {
        FakeTask task = new FakeTask();
        task.execStatus = ExecStatusEnum.RETRY_OVER.getCode();
        FakeExecutor executor = new FakeExecutor(sender, task);
        executor.execute(1L);
        verify(sender, never()).send(any());
    }

    @Test
    void 投递成功置SUCCESS并写日志() {
        when(sender.send(any())).thenReturn(NotifyResult.ok("u", "q", "r", LocalDateTime.now(), LocalDateTime.now()));
        FakeTask task = new FakeTask();
        FakeExecutor executor = new FakeExecutor(sender, task);
        executor.execute(1L);
        assertEquals(ExecStatusEnum.SUCCESS.getCode(), task.execStatus);
        assertEquals(1, task.requestCnt);
        assertTrue(executor.persisted);
        assertNotNull(executor.loggedResult);
    }

    @Test
    void 投递失败且次数未满置FAIL并算下次时间() {
        when(sender.send(any())).thenReturn(NotifyResult.fail("x", "u", "q", null, LocalDateTime.now(), LocalDateTime.now()));
        FakeTask task = new FakeTask();
        FakeExecutor executor = new FakeExecutor(sender, task);
        executor.execute(1L);
        assertEquals(ExecStatusEnum.FAIL.getCode(), task.execStatus);
        assertNotNull(task.nextRequestTime);
    }

    @Test
    void 投递失败且次数用尽置RETRY_OVER() {
        when(sender.send(any())).thenReturn(NotifyResult.fail("x", "u", "q", null, LocalDateTime.now(), LocalDateTime.now()));
        FakeTask task = new FakeTask();
        task.requestCnt = 1; // 梯度只有 2m,5m 两级，本次为第 2 次，失败即用尽
        FakeExecutor executor = new FakeExecutor(sender, task);
        executor.execute(1L);
        assertEquals(ExecStatusEnum.RETRY_OVER.getCode(), task.execStatus);
        assertNull(task.nextRequestTime);
    }
}
```

- [ ] **Step 2: 跑测试确认编译失败**（NotifyTaskExecutor 不存在）

- [ ] **Step 3: 实现模板方法**

```java
package top.mddata.open.service.notify.pipeline;

import lombok.extern.slf4j.Slf4j;
import top.mddata.open.enumeration.admin.ExecStatusEnum;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 投递生命周期模板：取任务 → 幂等检查 → 投递 → 状态流转 → 持久化 → 写日志。
 * 子类只需实现三个钩子（各自操作自己的任务表/日志表），管道逻辑全类唯一一份。
 */
@Slf4j
public abstract class NotifyTaskExecutor<T extends NotifyTaskView> {

    /** 终态：重复提交（手动重推连点、重试扫描与即时投递并发）不产生重复投递 */
    private static final Set<String> TERMINAL_STATUS = Set.of(
            ExecStatusEnum.SUCCESS.getCode(), ExecStatusEnum.RETRY_OVER.getCode(), ExecStatusEnum.END.getCode());

    private final NotifySender notifySender;
    private final RetryPolicy retryPolicy;

    protected NotifyTaskExecutor(NotifySender notifySender, RetryPolicy retryPolicy) {
        this.notifySender = notifySender;
        this.retryPolicy = retryPolicy;
    }

    public final void execute(Long taskId) {
        try {
            T task = loadTask(taskId);
            if (task == null) {
                log.warn("[推送] 任务不存在，跳过, taskId={}", taskId);
                return;
            }
            if (TERMINAL_STATUS.contains(task.getExecStatus())) {
                log.info("[推送] 任务已是终态，跳过, taskId={}, status={}", taskId, task.getExecStatus());
                return;
            }
            task.markDelivering(LocalDateTime.now());
            NotifyResult result = notifySender.send(task);
            if (result.isSuccess()) {
                task.markStatus(ExecStatusEnum.SUCCESS.getCode(), null);
            } else {
                LocalDateTime nextTime = retryPolicy.nextRequestTime(task.getRequestCnt(), LocalDateTime.now());
                task.markStatus(nextTime == null ? ExecStatusEnum.RETRY_OVER.getCode()
                        : ExecStatusEnum.FAIL.getCode(), nextTime);
            }
            persist(task);
            appendLog(task, result);
        } catch (Exception e) {
            // 兜底：线程池任务异常不外抛，任务保持原状态等下轮重试扫描
            log.error("[推送] 投递生命周期异常, taskId={}", taskId, e);
        }
    }

    protected abstract T loadTask(Long taskId);

    protected abstract void persist(T task);

    protected abstract void appendLog(T task, NotifyResult result);
}
```

- [ ] **Step 4: 跑测试确认通过**

```bash
mvn test -pl mdp-apps/md-open/open-service -Dtest=NotifyTaskExecutorTest
```

预期：Tests run: 5, Failures: 0。

---

### Task 6: NotifyTaskDispatcher + NotifyConfiguration

**Files:**
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/pipeline/NotifyTaskDispatcher.java`
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/pipeline/NotifyConfiguration.java`

**Interfaces:**
- Consumes: `NotifyProperties`（corePoolSize/maxPoolSize/queueCapacity/timeLevel）
- Produces:
  - Bean `RetryPolicy retryPolicy`（非法 timeLevel 启动即失败）
  - Bean `ThreadPoolExecutor notifyTaskThreadPool`（AbortPolicy）
  - `NotifyTaskDispatcher.submit(Long taskId, NotifyTaskExecutor<? extends NotifyTaskView> executor)`、`submitAll(Collection<Long>, NotifyTaskExecutor<? extends NotifyTaskView>)`——线程池拒绝时只记 warn，任务保持原状态由重试扫描兜底

- [ ] **Step 1: 创建 `NotifyTaskDispatcher`**

```java
package top.mddata.open.service.notify.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 推送任务调度入口：把「任务ID + 执行器」投递到统一线程池。
 * 拒绝策略为 AbortPolicy：线程池满时不阻塞调用线程（修旧版 CallerRuns 导致调度线程被 HTTP 阻塞的问题），
 * 任务保持原状态（WAIT/FAIL），由 PowerJob 重试扫描兜底，不丢任务。
 */
@Slf4j
@Component
public class NotifyTaskDispatcher {

    private final ThreadPoolExecutor threadPool;

    public NotifyTaskDispatcher(@Qualifier("notifyTaskThreadPool") ThreadPoolExecutor threadPool) {
        this.threadPool = threadPool;
    }

    public void submit(Long taskId, NotifyTaskExecutor<? extends NotifyTaskView> executor) {
        try {
            threadPool.execute(() -> executor.execute(taskId));
        } catch (RejectedExecutionException e) {
            log.warn("[推送] 线程池已满，任务留待重试扫描兜底, taskId={}", taskId);
        }
    }

    public void submitAll(Collection<Long> taskIds, NotifyTaskExecutor<? extends NotifyTaskView> executor) {
        taskIds.forEach(taskId -> submit(taskId, executor));
    }
}
```

（因 `@Qualifier` 必须显式构造，不使用 `@RequiredArgsConstructor`。）

- [ ] **Step 2: 创建 `NotifyConfiguration`**

```java
package top.mddata.open.service.notify.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.mddata.open.service.notify.NotifyProperties;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 推送域装配：配置属性、重试策略、投递线程池统一在此创建
 * （修旧版 @EnableConfigurationProperties 挂在 ServiceImpl 上的隐式初始化链问题）。
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(NotifyProperties.class)
public class NotifyConfiguration {

    /** timeLevel 非法时启动即失败，避免运行期被静默当成"重试用尽" */
    @Bean
    public RetryPolicy retryPolicy(NotifyProperties notifyProperties) {
        return new RetryPolicy(notifyProperties.getTimeLevel());
    }

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolExecutor notifyTaskThreadPool(NotifyProperties properties) {
        AtomicInteger seq = new AtomicInteger(1);
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, "notify-push-" + seq.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        };
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(properties.getQueueCapacity()),
                factory,
                new ThreadPoolExecutor.AbortPolicy());
        log.info("[推送] 投递线程池初始化, core={}, max={}, queue={}",
                properties.getCorePoolSize(), properties.getMaxPoolSize(), properties.getQueueCapacity());
        return pool;
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
cd /Users/tangyh/gitee/mdp && mvn compile -pl mdp-apps/md-open/open-service
```

预期：BUILD SUCCESS。

---

### Task 7: 接口回调薄壳改造（CALLBACK）

**Files:**
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/callback/NotifyInfoTaskView.java`
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/callback/NotifyInfoTaskExecutor.java`
- Modify: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/callback/NotifyInfoService.java`
- Modify: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/callback/impl/NotifyInfoServiceImpl.java`（整体重写，删除 doNotify/appendUrlParams/buildNextSendTime/retryTask 约 200 行）

**Interfaces:**
- Consumes: pipeline 包全部组件；`AppKeysService.getByAppId`；`NotifyInfoMapper`；`NotifyInfoLogMapper`
- Produces:
  - `NotifyInfoService.notify(NotifyInfoDto): R<Long>`——**改为异步**：事务内 insert（WAIT），afterCommit 投递，立即返回任务 id
  - `NotifyInfoService.push(Long id, String url): Boolean`——任务状态重置为 WAIT 后异步重推（保留旧版「重试结束任务可手动重推」能力）
  - `NotifyInfoService.retry(LocalDateTime now, int limit): void`——供 PowerJob 调用（签名从 `retry(LocalDateTime)` 变更）

- [ ] **Step 1: 创建 `NotifyInfoTaskView` 适配器**

```java
package top.mddata.open.service.notify.callback;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gitee.sop.support.aes.MdpBizMsgCrypt;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.mddata.open.dto.admin.NotifyInfoDto;
import top.mddata.open.entity.admin.NotifyInfo;
import top.mddata.open.enumeration.admin.EventTypeEnum;
import top.mddata.open.service.notify.pipeline.NotifyTaskView;

import java.time.LocalDateTime;

/**
 * 接口回调任务适配器：把 NotifyInfo 实体包装成管道视图，业务报文差异在此封装。
 */
@Getter
@RequiredArgsConstructor
public class NotifyInfoTaskView implements NotifyTaskView {

    private final NotifyInfo entity;

    @Override
    public Long getId() {
        return entity.getId();
    }

    @Override
    public String getNotifyUrl() {
        return entity.getNotifyUrl();
    }

    @Override
    public String getAppKey() {
        return entity.getAppKey();
    }

    @Override
    public Integer getNotifyEncryptionType() {
        return entity.getNotifyEncryptionType();
    }

    @Override
    public String getNotifyToken() {
        return entity.getNotifyToken();
    }

    @Override
    public String getNotifyEncodingAesKey() {
        return entity.getNotifyEncodingAesKey();
    }

    @Override
    public Integer getRequestCnt() {
        return entity.getRequestCnt();
    }

    @Override
    public String getExecStatus() {
        return entity.getExecStatus();
    }

    @Override
    public String buildPlaintext() {
        NotifyInfoDto request = JSON.parseObject(entity.getRequestData(), NotifyInfoDto.class);
        JSONObject bizData = new JSONObject();
        bizData.put("type", EventTypeEnum.CALLBACK.name());
        bizData.put("method", entity.getApiName());
        bizData.put("appKey", entity.getAppKey());
        bizData.put("version", entity.getApiVersion());
        bizData.put("timestamp", MdpBizMsgCrypt.generateTimestamp());
        bizData.put("callLogId", entity.getCallLogId());
        bizData.put("bizContent", request != null ? request.getBizParams() : null);
        return bizData.toJSONString();
    }

    @Override
    public void markDelivering(LocalDateTime now) {
        entity.setRequestCnt(entity.getRequestCnt() + 1);
        entity.setLastRequestTime(now);
    }

    @Override
    public void markStatus(String execStatus, LocalDateTime nextRequestTime) {
        entity.setExecStatus(execStatus);
        entity.setNextRequestTime(nextRequestTime);
    }
}
```

- [ ] **Step 2: 创建 `NotifyInfoTaskExecutor`**

```java
package top.mddata.open.service.notify.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mddata.open.entity.admin.NotifyInfo;
import top.mddata.open.entity.admin.NotifyInfoLog;
import top.mddata.open.mapper.admin.NotifyInfoLogMapper;
import top.mddata.open.mapper.admin.NotifyInfoMapper;
import top.mddata.open.service.notify.pipeline.NotifyResult;
import top.mddata.open.service.notify.pipeline.NotifySender;
import top.mddata.open.service.notify.pipeline.NotifyTaskExecutor;
import top.mddata.open.service.notify.pipeline.RetryPolicy;

/**
 * 接口回调执行器：只负责 mdo_notify_info / mdo_notify_info_log 两张表的钩子实现。
 */
@Slf4j
@Component
public class NotifyInfoTaskExecutor extends NotifyTaskExecutor<NotifyInfoTaskView> {

    private final NotifyInfoMapper notifyInfoMapper;
    private final NotifyInfoLogMapper notifyInfoLogMapper;

    public NotifyInfoTaskExecutor(NotifySender notifySender, RetryPolicy retryPolicy,
                                  NotifyInfoMapper notifyInfoMapper, NotifyInfoLogMapper notifyInfoLogMapper) {
        super(notifySender, retryPolicy);
        this.notifyInfoMapper = notifyInfoMapper;
        this.notifyInfoLogMapper = notifyInfoLogMapper;
    }

    @Override
    protected NotifyInfoTaskView loadTask(Long taskId) {
        NotifyInfo entity = notifyInfoMapper.selectOneById(taskId);
        return entity == null ? null : new NotifyInfoTaskView(entity);
    }

    @Override
    protected void persist(NotifyInfoTaskView task) {
        notifyInfoMapper.update(task.getEntity());
    }

    @Override
    protected void appendLog(NotifyInfoTaskView task, NotifyResult result) {
        try {
            NotifyInfoLog notifyLog = new NotifyInfoLog();
            notifyLog.setNotifyInfoId(task.getId());
            notifyLog.setRequestData(result.getRequestBody());
            notifyLog.setResponseData(result.getResponseBody());
            notifyLog.setRequestTime(result.getRequestTime());
            notifyLog.setResponseTime(result.getResponseTime());
            notifyLog.setErrorMsg(result.getErrorMsg());
            notifyLog.setExecStatus(task.getExecStatus());
            notifyInfoLogMapper.insert(notifyLog);
        } catch (Exception e) {
            // 日志写失败不冲掉已完成的投递与状态更新
            log.error("[回调] 写回调日志失败, notifyId={}", task.getId(), e);
        }
    }
}
```

- [ ] **Step 3: 重写 `NotifyInfoServiceImpl`（整体替换旧文件内容）**

```java
package top.mddata.open.service.notify.callback.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.gitee.sop.support.exception.OpenException;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import top.mddata.base.base.R;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.enumeration.StoryMessageEnum;
import top.mddata.open.dto.admin.NotifyInfoDto;
import top.mddata.open.entity.admin.AppKeys;
import top.mddata.open.entity.admin.NotifyInfo;
import top.mddata.open.enumeration.admin.ExecStatusEnum;
import top.mddata.open.mapper.admin.NotifyInfoMapper;
import top.mddata.open.service.app.AppKeysService;
import top.mddata.open.service.notify.callback.NotifyInfoService;
import top.mddata.open.service.notify.callback.NotifyInfoTaskExecutor;
import top.mddata.open.service.notify.pipeline.NotifyTaskDispatcher;
import top.mddata.open.service.notify.pipeline.NotifyUrlValidator;
import top.mddata.open.service.notify.pipeline.RetryPolicy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

/**
 * 回调任务 服务层实现。
 * 投递/重试/日志管道已下沉到 notify.pipeline，本类只负责任务的创建、状态查询与调度触发。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyInfoServiceImpl extends SuperServiceImpl<NotifyInfoMapper, NotifyInfo> implements NotifyInfoService {

    /** WAIT 状态超过该分钟数视为「投递丢失」（线程池拒绝/服务重启），由重试扫描兜底 */
    private static final int WAIT_LOST_MINUTES = 5;

    private final AppKeysService appKeysService;
    private final RetryPolicy retryPolicy;
    private final NotifyTaskDispatcher notifyTaskDispatcher;
    private final NotifyInfoTaskExecutor notifyInfoTaskExecutor;

    /**
     * 生成回调任务：事务内只写库，事务提交后再异步投递（修旧版在调用方线程同步发 HTTP 最长阻塞 5s 的问题）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Long> notify(NotifyInfoDto request) {
        NotifyInfo notifyInfo = buildRecord(request);
        NotifyUrlValidator.validate(notifyInfo.getNotifyUrl());
        save(notifyInfo);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notifyTaskDispatcher.submit(notifyInfo.getId(), notifyInfoTaskExecutor);
            }
        });
        return R.success(notifyInfo.getId());
    }

    /**
     * 手动重推：状态重置为 WAIT 后异步投递。
     * 重置是为了保留旧版「RETRY_OVER/已结束任务可手动重推」的能力（执行器对终态任务幂等跳过）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean push(Long id, String url) {
        if (StrUtil.isNotBlank(url)) {
            NotifyUrlValidator.validate(url);
        }
        NotifyInfo entity = new NotifyInfo();
        entity.setId(id).setExecStatus(ExecStatusEnum.WAIT.getCode());
        if (StrUtil.isNotBlank(url)) {
            entity.setNotifyUrl(url);
        }
        boolean updated = updateById(entity);
        if (!updated) {
            throw new OpenException(StoryMessageEnum.PARAM_VALIDATION, "回调任务不存在：" + id);
        }
        notifyTaskDispatcher.submit(id, notifyInfoTaskExecutor);
        return true;
    }

    @Override
    public Boolean end(Long id) {
        return updateChain().set(NotifyInfo::getExecStatus, ExecStatusEnum.END.getCode())
                .eq(NotifyInfo::getExecStatus, ExecStatusEnum.FAIL.getCode())
                .eq(NotifyInfo::getId, id).update();
    }

    /**
     * 重试扫描（PowerJob 定时调用）：分页取「到期 FAIL」+「丢失 WAIT」两类任务，提交线程池。
     */
    @Override
    public void retry(LocalDateTime now, int limit) {
        ArgumentAssert.notNull(now, "当前时间不能为空");
        LocalDateTime nextTime = now.withSecond(0).withNano(0);
        List<Long> failIds = list(QueryWrapper.create()
                        .select(NotifyInfo::getId)
                        .where(NotifyInfo::getExecStatus).eq(ExecStatusEnum.FAIL.getCode())
                        .and(NotifyInfo::getNextRequestTime).le(nextTime)
                        .orderBy(NotifyInfo::getId, true)
                        .limit(limit))
                .stream().map(NotifyInfo::getId).toList();
        List<Long> lostWaitIds = list(QueryWrapper.create()
                        .select(NotifyInfo::getId)
                        .where(NotifyInfo::getExecStatus).eq(ExecStatusEnum.WAIT.getCode())
                        .and(NotifyInfo::getCreatedAt).le(nextTime.minusMinutes(WAIT_LOST_MINUTES))
                        .orderBy(NotifyInfo::getId, true)
                        .limit(limit))
                .stream().map(NotifyInfo::getId).toList();
        List<Long> taskIds = Stream.concat(failIds.stream(), lostWaitIds.stream()).toList();
        if (taskIds.isEmpty()) {
            log.info("[{}]表无重试记录", NotifyInfo.TABLE_NAME);
            return;
        }
        log.info("[{}]重试扫描命中{}条任务", NotifyInfo.TABLE_NAME, taskIds.size());
        notifyTaskDispatcher.submitAll(taskIds, notifyInfoTaskExecutor);
    }

    private NotifyInfo buildRecord(NotifyInfoDto request) {
        NotifyInfo notifyInfo = new NotifyInfo();
        notifyInfo.setCallLogId(request.getCallLogId());
        notifyInfo.setAppId(request.getAppId());
        notifyInfo.setAppKey(request.getAppKey());
        notifyInfo.setApiName(request.getApiName());
        notifyInfo.setApiVersion(request.getApiVersion());
        notifyInfo.setNotifyUrl(request.getNotifyUrl());
        notifyInfo.setRequestData(JSON.toJSONString(request));
        notifyInfo.setRequestCnt(0);
        notifyInfo.setMaxRequestCnt(retryPolicy.getMaxRetry());
        notifyInfo.setExecStatus(ExecStatusEnum.WAIT.getCode());
        notifyInfo.setRemark(request.getRemark());

        // 从 AppKeys 冗余加密配置快照：已生成任务不受后续配置变更影响
        AppKeys appKeys = appKeysService.getByAppId(request.getAppId());
        if (appKeys != null) {
            notifyInfo.setNotifyEncryptionType(appKeys.getNotifyEncryptionType());
            notifyInfo.setNotifyToken(appKeys.getNotifyToken());
            notifyInfo.setNotifyEncodingAesKey(appKeys.getNotifyEncodingAesKey());
            if (StrUtil.isBlank(notifyInfo.getNotifyUrl())) {
                notifyInfo.setNotifyUrl(appKeys.getNotifyUrl());
            }
        }
        return notifyInfo;
    }
}
```

- [ ] **Step 4: 修改 `NotifyInfoService` 接口**

```java
package top.mddata.open.service.notify.callback;

import top.mddata.base.base.R;
import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.dto.admin.NotifyInfoDto;
import top.mddata.open.entity.admin.NotifyInfo;

import java.time.LocalDateTime;

/**
 * 回调任务 服务层接口。
 */
public interface NotifyInfoService extends SuperService<NotifyInfo> {

    /** 生成回调任务并异步投递，返回任务 id */
    R<Long> notify(NotifyInfoDto request);

    /** 手动重推（可换回调地址），状态重置后异步投递 */
    Boolean push(Long id, String url);

    /** 手动结束重试（仅 FAIL 状态可结束） */
    Boolean end(Long id);

    /** 重试扫描：取到期 FAIL 与丢失 WAIT 任务提交线程池（PowerJob 定时调用） */
    void retry(LocalDateTime now, int limit);
}
```

（若旧接口还有其他方法签名，以旧接口为准保留；`notifyImmediately` 删除——能力已由 `push` + 异步执行器覆盖，调用方只有旧 `push` 内部。）

- [ ] **Step 5: 编译验证**

```bash
mvn compile -pl mdp-apps/md-open/open-service,mdp-apps/md-open/open-web,mdp-apps/md-open/open-facade/open-boot-impl -am
```

预期：BUILD SUCCESS（open-web 的 NotifyInfoController、facade 的 NotifyAndEventPushFacadeImpl 无需改动——`notify` 签名未变）。

---

### Task 8: 事件推送薄壳改造（EVENT_PUSH）

**Files:**
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/event/EventPushTaskView.java`
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/event/EventPushTaskExecutor.java`
- Modify: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/event/EventPushService.java`
- Modify: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/event/impl/EventPushServiceImpl.java`（整体重写）
- Modify: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/event/impl/EventTriggerServiceImpl.java`（仅 import 修正，逻辑不动）

**Interfaces:**
- Consumes: pipeline 全部组件；`EventSubscriptionService.findByEventCode`（Task 9 迁移；本任务先用 `AppKeysService.findByEventCode`，Task 9 统一切换）；`EventPushMapper`、`EventPushLogMapper`
- Produces:
  - `EventPushService.saveByEventTrigger(String eventCode, Long triggerId, String requestData): void`——事务内 insert trigger 订阅方的任务，afterCommit 批量投递
  - `EventPushService.executeImmediately(Long id): Long`——状态重置 WAIT 后异步投递（修 NPE）
  - `EventPushService.retry(LocalDateTime now, int limit): void`

- [ ] **Step 1: 创建 `EventPushTaskView`**

```java
package top.mddata.open.service.notify.event;

import com.alibaba.fastjson2.JSONObject;
import com.gitee.sop.support.aes.MdpBizMsgCrypt;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.mddata.open.entity.admin.EventPush;
import top.mddata.open.enumeration.admin.EventTypeEnum;
import top.mddata.open.service.notify.pipeline.NotifyTaskView;

import java.time.LocalDateTime;

/**
 * 事件推送任务适配器：与回调的差异在业务报文（EVENT_PUSH + eventCode + eventTriggerId，无 version）。
 */
@Getter
@RequiredArgsConstructor
public class EventPushTaskView implements NotifyTaskView {

    private final EventPush entity;

    @Override
    public Long getId() {
        return entity.getId();
    }

    @Override
    public String getNotifyUrl() {
        return entity.getNotifyUrl();
    }

    @Override
    public String getAppKey() {
        return entity.getAppKey();
    }

    @Override
    public Integer getNotifyEncryptionType() {
        return entity.getNotifyEncryptionType();
    }

    @Override
    public String getNotifyToken() {
        return entity.getNotifyToken();
    }

    @Override
    public String getNotifyEncodingAesKey() {
        return entity.getNotifyEncodingAesKey();
    }

    @Override
    public Integer getRequestCnt() {
        return entity.getRequestCnt();
    }

    @Override
    public String getExecStatus() {
        return entity.getExecStatus();
    }

    @Override
    public String buildPlaintext() {
        JSONObject bizData = new JSONObject();
        bizData.put("type", EventTypeEnum.EVENT_PUSH.name());
        bizData.put("method", entity.getEventCode());
        bizData.put("appKey", entity.getAppKey());
        bizData.put("timestamp", MdpBizMsgCrypt.generateTimestamp());
        bizData.put("eventTriggerId", entity.getEventTriggerId());
        bizData.put("bizContent", entity.getRequestData());
        return bizData.toJSONString();
    }

    @Override
    public void markDelivering(LocalDateTime now) {
        entity.setRequestCnt(entity.getRequestCnt() + 1);
        entity.setLastRequestTime(now);
    }

    @Override
    public void markStatus(String execStatus, LocalDateTime nextRequestTime) {
        entity.setExecStatus(execStatus);
        entity.setNextRequestTime(nextRequestTime);
    }
}
```

- [ ] **Step 2: 创建 `EventPushTaskExecutor`**（与回调同构，操作 `EventPushMapper`/`EventPushLogMapper`，日志外键 `eventPushId`）

```java
package top.mddata.open.service.notify.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mddata.open.entity.admin.EventPush;
import top.mddata.open.entity.admin.EventPushLog;
import top.mddata.open.mapper.admin.EventPushLogMapper;
import top.mddata.open.mapper.admin.EventPushMapper;
import top.mddata.open.service.notify.pipeline.NotifyResult;
import top.mddata.open.service.notify.pipeline.NotifySender;
import top.mddata.open.service.notify.pipeline.NotifyTaskExecutor;
import top.mddata.open.service.notify.pipeline.RetryPolicy;

/**
 * 事件推送执行器：只负责 mdo_event_push / mdo_event_push_log 两张表的钩子实现。
 */
@Slf4j
@Component
public class EventPushTaskExecutor extends NotifyTaskExecutor<EventPushTaskView> {

    private final EventPushMapper eventPushMapper;
    private final EventPushLogMapper eventPushLogMapper;

    public EventPushTaskExecutor(NotifySender notifySender, RetryPolicy retryPolicy,
                                 EventPushMapper eventPushMapper, EventPushLogMapper eventPushLogMapper) {
        super(notifySender, retryPolicy);
        this.eventPushMapper = eventPushMapper;
        this.eventPushLogMapper = eventPushLogMapper;
    }

    @Override
    protected EventPushTaskView loadTask(Long taskId) {
        EventPush entity = eventPushMapper.selectOneById(taskId);
        return entity == null ? null : new EventPushTaskView(entity);
    }

    @Override
    protected void persist(EventPushTaskView task) {
        eventPushMapper.update(task.getEntity());
    }

    @Override
    protected void appendLog(EventPushTaskView task, NotifyResult result) {
        try {
            EventPushLog pushLog = new EventPushLog();
            pushLog.setEventPushId(task.getId());
            pushLog.setRequestData(result.getRequestBody());
            pushLog.setResponseData(result.getResponseBody());
            pushLog.setRequestTime(result.getRequestTime());
            pushLog.setResponseTime(result.getResponseTime());
            pushLog.setErrorMsg(result.getErrorMsg());
            pushLog.setExecStatus(task.getExecStatus());
            eventPushLogMapper.insert(pushLog);
        } catch (Exception e) {
            log.error("[事件推送] 写推送日志失败, eventPushId={}", task.getId(), e);
        }
    }
}
```

- [ ] **Step 3: 重写 `EventPushServiceImpl`（整体替换）**

```java
package top.mddata.open.service.notify.event.impl;

import com.gitee.sop.support.exception.OpenException;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.enumeration.StoryMessageEnum;
import top.mddata.open.entity.admin.EventPush;
import top.mddata.open.enumeration.admin.ExecStatusEnum;
import top.mddata.open.mapper.admin.EventPushMapper;
import top.mddata.open.service.app.AppKeysService;
import top.mddata.open.service.notify.event.EventPushService;
import top.mddata.open.service.notify.event.EventPushTaskExecutor;
import top.mddata.open.service.notify.pipeline.NotifyTaskDispatcher;
import top.mddata.open.service.notify.pipeline.RetryPolicy;
import top.mddata.open.vo.admin.AppKeysVo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

/**
 * 事件推送任务 服务层实现。
 * 投递/重试/日志管道已下沉到 notify.pipeline，本类只负责任务生成、状态查询与调度触发。
 *
 * @author henhen6
 * @since 2026-01-12 21:28:36
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventPushServiceImpl extends SuperServiceImpl<EventPushMapper, EventPush> implements EventPushService {

    /** WAIT 状态超过该分钟数视为「投递丢失」，由重试扫描兜底 */
    private static final int WAIT_LOST_MINUTES = 5;

    private final AppKeysService appKeysService;
    private final RetryPolicy retryPolicy;
    private final NotifyTaskDispatcher notifyTaskDispatcher;
    private final EventPushTaskExecutor eventPushTaskExecutor;

    /**
     * 按事件触发记录生成各订阅应用的推送任务：事务内只写库，提交后批量异步投递
     * （修旧版在事务内向线程池提交任务、长事务占连接的问题）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveByEventTrigger(String eventCode, Long triggerId, String requestData) {
        List<AppKeysVo> appKeysList = appKeysService.findByEventCode(eventCode);
        log.info("[事件触发] 事件类型：{}，订阅该事件的应用数量：{}", eventCode, appKeysList.size());
        if (appKeysList.isEmpty()) {
            return;
        }

        List<EventPush> eventPushList = appKeysList.stream().map(appKeys -> {
            EventPush eventPush = new EventPush();
            eventPush.setEventCode(eventCode);
            eventPush.setEventTriggerId(triggerId);
            eventPush.setAppId(appKeys.getAppId());
            eventPush.setAppKey(appKeys.getAppKey());
            eventPush.setNotifyUrl(appKeys.getNotifyUrl());
            // 冗余加密配置快照：已生成任务不受后续配置变更影响
            eventPush.setNotifyEncryptionType(appKeys.getNotifyEncryptionType());
            eventPush.setNotifyToken(appKeys.getNotifyToken());
            eventPush.setNotifyEncodingAesKey(appKeys.getNotifyEncodingAesKey());
            eventPush.setRequestData(requestData);
            eventPush.setMaxRequestCnt(retryPolicy.getMaxRetry());
            eventPush.setRequestCnt(0);
            eventPush.setExecStatus(ExecStatusEnum.WAIT.getCode());
            return eventPush;
        }).toList();

        saveBatch(eventPushList);
        List<Long> taskIds = eventPushList.stream().map(EventPush::getId).toList();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notifyTaskDispatcher.submitAll(taskIds, eventPushTaskExecutor);
            }
        });
    }

    @Override
    public Boolean end(Long id) {
        return updateChain().set(EventPush::getExecStatus, ExecStatusEnum.END.getCode())
                .eq(EventPush::getExecStatus, ExecStatusEnum.FAIL.getCode())
                .eq(EventPush::getId, id).update();
    }

    /**
     * 手动立即推送：状态重置 WAIT 后异步投递（修旧版 getById 不判空的 NPE，并保留终态任务重推能力）。
     */
    @Override
    public Long executeImmediately(Long id) {
        EventPush entity = new EventPush();
        entity.setId(id).setExecStatus(ExecStatusEnum.WAIT.getCode());
        boolean updated = updateById(entity);
        if (!updated) {
            throw new OpenException(StoryMessageEnum.PARAM_VALIDATION, "事件推送任务不存在：" + id);
        }
        notifyTaskDispatcher.submit(id, eventPushTaskExecutor);
        return id;
    }

    /**
     * 重试扫描（PowerJob 定时调用）：分页取「到期 FAIL」+「丢失 WAIT」两类任务，提交线程池。
     */
    @Override
    public void retry(LocalDateTime now, int limit) {
        ArgumentAssert.notNull(now, "当前时间不能为空");
        LocalDateTime nextTime = now.withSecond(0).withNano(0);
        List<Long> failIds = list(QueryWrapper.create()
                        .select(EventPush::getId)
                        .where(EventPush::getExecStatus).eq(ExecStatusEnum.FAIL.getCode())
                        .and(EventPush::getNextRequestTime).le(nextTime)
                        .orderBy(EventPush::getId, true)
                        .limit(limit))
                .stream().map(EventPush::getId).toList();
        List<Long> lostWaitIds = list(QueryWrapper.create()
                        .select(EventPush::getId)
                        .where(EventPush::getExecStatus).eq(ExecStatusEnum.WAIT.getCode())
                        .and(EventPush::getCreatedAt).le(nextTime.minusMinutes(WAIT_LOST_MINUTES))
                        .orderBy(EventPush::getId, true)
                        .limit(limit))
                .stream().map(EventPush::getId).toList();
        List<Long> taskIds = Stream.concat(failIds.stream(), lostWaitIds.stream()).toList();
        if (taskIds.isEmpty()) {
            log.info("[{}]表无重试记录", EventPush.TABLE_NAME);
            return;
        }
        log.info("[{}]重试扫描命中{}条任务", EventPush.TABLE_NAME, taskIds.size());
        notifyTaskDispatcher.submitAll(taskIds, eventPushTaskExecutor);
    }
}
```

- [ ] **Step 4: 修改 `EventPushService` 接口**

```java
package top.mddata.open.service.notify.event;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.entity.admin.EventPush;

import java.time.LocalDateTime;

/**
 * 事件推送任务 服务层接口。
 */
public interface EventPushService extends SuperService<EventPush> {

    /** 按事件触发生成订阅方推送任务，事务提交后批量异步投递 */
    void saveByEventTrigger(String eventCode, Long triggerId, String requestData);

    /** 手动结束重试（仅 FAIL 状态可结束） */
    Boolean end(Long id);

    /** 手动立即推送：状态重置后异步投递 */
    Long executeImmediately(Long id);

    /** 重试扫描（PowerJob 定时调用） */
    void retry(LocalDateTime now, int limit);
}
```

- [ ] **Step 5: `EventTriggerServiceImpl` 修正 import（逻辑不动）**

`EventPushService`/`EventTypeService` 的 import 已在 Task 1 批量改写；本步确认 `save` 方法的事务与调用链不变（`event_trigger` 与 `event_push` 同事务，`afterCommit` 在最外层事务提交后触发——`EventTriggerServiceImpl.save` 是事务入口，`saveByEventTrigger` 内注册的同步器随外层提交触发，语义正确）。

- [ ] **Step 6: 编译验证**

```bash
mvn compile -pl mdp-apps/md-open/open-service,mdp-apps/md-open/open-facade/open-boot-impl,mdp-apps/md-server/worker-server -am
```

预期：BUILD SUCCESS。worker-server 的 `DubboConfiguration` 会报 `retry(LocalDateTime)` 签名不存在——属于预期，Task 12 处理；若想本步先编译通过，可临时把 worker-server 从 -pl 列表去掉。

---

### Task 9: findByEventCode 迁移 + AppKeys 瘦身

**Files:**
- Modify: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/event/EventSubscriptionService.java`
- Modify: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/event/impl/EventSubscriptionServiceImpl.java`
- Modify: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/app/AppKeysService.java`（删除 findByEventCode）
- Modify: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/app/impl/AppKeysServiceImpl.java`（删除 findByEventCode）
- Modify: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/notify/event/impl/EventPushServiceImpl.java`（依赖切换）

**Interfaces:**
- Consumes: `AppKeysService.findByEventCode(String): List<AppKeysVo>`（现实现，三表 innerJoin）
- Produces: `EventSubscriptionService.findByEventCode(String eventCode): List<AppKeysVo>`——签名不变，实现原样迁移

- [ ] **Step 1: `EventSubscriptionService` 接口新增方法**

```java
/**
 * 查询订阅了指定事件、且通知开关打开、应用启用的应用密钥列表（含加密配置）。
 *
 * @param eventCode 事件编码
 * @return 订阅方密钥列表
 */
List<AppKeysVo> findByEventCode(String eventCode);
```

- [ ] **Step 2: 实现原样迁移到 `EventSubscriptionServiceImpl`**

从 `AppKeysServiceImpl.findByEventCode` 整体搬移（import 一并搬：`QueryMethods`、`App`、`AppKeys`、`EventSubscription`、`EventType`、`AppKeysVo`）：

```java
@Override
@Transactional(readOnly = true)
public List<AppKeysVo> findByEventCode(String eventCode) {
    Iterable<QueryColumn> queryColumns = QueryMethods.allColumns(AppKeys.class);
    QueryColumn appId = QueryMethods.column(App::getId).as("app_id");
    QueryColumn appName = QueryMethods.column(App::getName).as("app_name");
    QueryWrapper wrapper = QueryWrapper.create()
            .select(queryColumns)
            .select(App::getAppKey, App::getAppSecret)
            .select(appId, appName)
            .from(AppKeys.class)
            .innerJoin(App.class).on(App::getId, AppKeys::getAppId)
            .innerJoin(EventSubscription.class).on(EventSubscription::getAppId, App::getId)
            .innerJoin(EventType.class).on(EventType::getId, EventSubscription::getEventTypeId)
            .where(EventType::getCode).eq(eventCode)
            .eq(AppKeys::getNotifyState, true)
            .eq(App::getState, true);

    return listAs(wrapper, AppKeysVo.class);
}
```

- [ ] **Step 3: `EventPushServiceImpl` 依赖切换**

字段 `AppKeysService appKeysService` 改为 `EventSubscriptionService eventSubscriptionService`，`saveByEventTrigger` 中 `appKeysService.findByEventCode(eventCode)` 改为 `eventSubscriptionService.findByEventCode(eventCode)`，import 同步修改。

- [ ] **Step 4: 删除 `AppKeysService`/`AppKeysServiceImpl` 的 `findByEventCode`**

删除接口方法与实现方法，清理不再使用的 import（`QueryMethods`、`QueryColumn`、`App`、`EventSubscription`、`EventType` 中仅该方法使用的部分——`AppKeysVo` 仍被 getByAppKey/getKeys 使用，保留）。

- [ ] **Step 5: 全仓搜索确认无残留引用后编译**

```bash
cd /Users/tangyh/gitee/mdp/mdp-apps && grep -rn "appKeysService.findByEventCode" --include="*.java" . ; echo "应无输出"
mvn compile -pl mdp-apps/md-open/open-service -am
```

预期：grep 无输出；BUILD SUCCESS。

---

### Task 10: 隐藏端点迁移 /inner/ + 内部令牌保护

**Files:**
- Create: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/inner/InnerNotifyController.java`
- Create: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/inner/InnerEventTriggerController.java`
- Create: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/inner/InnerAuthInterceptor.java`
- Create: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/inner/InnerWebMvcConfiguration.java`
- Modify: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/notify/callback/NotifyInfoController.java`（删除 `notify` 方法及 `NotifyInfoDto` import）
- Modify: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/notify/event/EventTriggerController.java`（删除 `save` 方法及 `EventTriggerDto` import）
- Modify: `mdp-apps/md-open/open-facade/open-cloud-impl/src/main/java/top/mddata/open/facade/admin/api/NotifyInfoApi.java`（URL 改 `/inner/notifyInfo/notify`）
- Modify: `mdp-apps/md-open/open-facade/open-cloud-impl/src/main/java/top/mddata/open/facade/admin/api/EventTriggerApi.java`（URL 改 `/inner/eventTrigger/save`）
- Create: `mdp-apps/md-open/open-facade/open-cloud-impl/src/main/java/top/mddata/open/facade/admin/config/InnerTokenRequestInterceptor.java`

**Interfaces:**
- Consumes: `NotifyInfoService.notify`、`EventTriggerService.save`
- Produces: `/inner/notifyInfo/notify`、`/inner/eventTrigger/save` 两个受保护内部端点；请求头 `X-Inner-Token`（配置项 `mdp.inner.token`，来自 yml/nacos，不硬编码）

- [ ] **Step 1: 创建 `InnerAuthInterceptor`**

```java
package top.mddata.open.controller.inner;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 内部端点令牌校验：/inner/** 仅供平台内部服务经 Feign 回环调用，
 * 通过共享令牌头防止端点裸露在 HTTP 面上（旧版仅靠 @Operation(hidden) 隐藏文档，端点实际无保护）。
 */
@Slf4j
public class InnerAuthInterceptor implements HandlerInterceptor {

    public static final String INNER_TOKEN_HEADER = "X-Inner-Token";

    private final String innerToken;

    public InnerAuthInterceptor(String innerToken) {
        this.innerToken = innerToken;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (innerToken == null || innerToken.isBlank()) {
            // 未配置令牌时拒绝一切调用：宁可内部调用失败，也不让端点裸奔
            log.error("[内部接口] mdp.inner.token 未配置，拒绝访问 {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "内部接口未启用");
            return false;
        }
        if (!innerToken.equals(request.getHeader(INNER_TOKEN_HEADER))) {
            log.warn("[内部接口] 令牌校验失败, uri={}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "内部接口令牌无效");
            return false;
        }
        return true;
    }
}
```

- [ ] **Step 2: 创建 `InnerWebMvcConfiguration`**

```java
package top.mddata.open.controller.inner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 仅对 /inner/** 注册令牌拦截器，不影响其他端点的既有鉴权链。
 */
@Configuration
public class InnerWebMvcConfiguration implements WebMvcConfigurer {

    @Value("${mdp.inner.token:}")
    private String innerToken;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InnerAuthInterceptor(innerToken)).addPathPatterns("/inner/**");
    }
}
```

- [ ] **Step 3: 创建两个内部控制器**

```java
package top.mddata.open.controller.inner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.open.dto.admin.NotifyInfoDto;
import top.mddata.open.service.notify.callback.NotifyInfoService;

/**
 * 内部-接口回调入口：仅供平台内部服务（md-api 等）经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-接口回调")
@RequestMapping("/inner/notifyInfo")
@RequiredArgsConstructor
public class InnerNotifyController {

    private final NotifyInfoService notifyInfoService;

    @Operation(hidden = true)
    @PostMapping("/notify")
    public R<Long> notify(@Validated @RequestBody NotifyInfoDto request) {
        return notifyInfoService.notify(request);
    }
}
```

```java
package top.mddata.open.controller.inner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.open.dto.admin.EventTriggerDto;
import top.mddata.open.entity.admin.EventTrigger;
import top.mddata.open.service.notify.event.EventTriggerService;

/**
 * 内部-事件触发入口：仅供平台内部服务（console、workbench 等）经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-事件触发")
@RequestMapping("/inner/eventTrigger")
@RequiredArgsConstructor
public class InnerEventTriggerController {

    private final EventTriggerService eventTriggerService;

    @Operation(hidden = true)
    @PostMapping("/save")
    public R<EventTrigger> save(@Validated @RequestBody EventTriggerDto save) {
        return R.success(eventTriggerService.save(save));
    }
}
```

- [ ] **Step 4: 从 admin 控制器删除旧隐藏端点**

- `NotifyInfoController`：删除 `notify(@Validated @RequestBody NotifyInfoDto request)` 方法（含 javadoc 与注解），删除 `NotifyInfoDto` import。
- `EventTriggerController`：删除 `save(@Validated @RequestBody EventTriggerDto save)` 方法（含 javadoc 与注解），删除不再使用的 import。

- [ ] **Step 5: Feign 客户端改 URL + 注入令牌头**

`NotifyInfoApi`：`@PostMapping("/admin/notifyInfo/notify")` → `@PostMapping("/inner/notifyInfo/notify")`。
`EventTriggerApi`：`@PostMapping("/admin/eventTrigger/save")` → `@PostMapping("/inner/eventTrigger/save")`。

新建 `InnerTokenRequestInterceptor`：

```java
package top.mddata.open.facade.admin.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Feign 回环调用内部端点时自动携带内部令牌（与 open-web 的 InnerAuthInterceptor 配对）。
 */
@Component
public class InnerTokenRequestInterceptor implements RequestInterceptor {

    @Value("${mdp.inner.token:}")
    private String innerToken;

    @Override
    public void apply(RequestTemplate template) {
        if (template.url() != null && template.url().contains("/inner/")) {
            template.header("X-Inner-Token", innerToken);
        }
    }
}
```

- [ ] **Step 6: 配置项与编译验证**

在 `boot-server` 与 `open-server` 的 `application.yml`（及 nacos 对应 dataId）增加：

```yaml
mdp:
  inner:
    token: ${MDP_INNER_TOKEN:请替换为随机串}
```

```bash
mvn compile -pl mdp-apps/md-open/open-web,mdp-apps/md-open/open-facade/open-cloud-impl -am
```

预期：BUILD SUCCESS。部署动作记入完成报告：各环境 nacos 配置 `mdp.inner.token`（boot/cloud 两侧必须一致）。

---

### Task 11: 分页查询 Wrapper 构建下沉（消除约 20 处模板重复）

**Files:**
- Create: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/support/PageWrapperHelper.java`
- Modify: open-web 下 `app/api/doc/oauth/notify` 各域所有含 `page` 方法的 Controller（约 20 处，模式相同）

**Interfaces:**
- Consumes: `WrapperUtil.buildOperators/buildWrapperByExtra/buildWrapperByOrder`、`PageParams`
- Produces: `PageWrapperHelper.build(PageParams<?> params, Class<T> entityClass): QueryWrapper`

- [ ] **Step 1: 创建 `PageWrapperHelper`**

```java
package top.mddata.open.controller.support;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;

/**
 * 分页查询 Wrapper 构建：实体映射 + 操作符 + 扩展条件 + 排序，四步合一。
 * 消除各 Controller page 方法的模板代码重复。
 */
public final class PageWrapperHelper {

    private PageWrapperHelper() {
    }

    public static <T> QueryWrapper build(PageParams<?> params, Class<T> entityClass) {
        T entity = BeanUtil.toBean(params.getModel(), entityClass);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entityClass));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entityClass);
        WrapperUtil.buildWrapperByOrder(wrapper, params, entityClass);
        return wrapper;
    }
}
```

- [ ] **Step 2: 逐 Controller 改造 page 方法（修改前/后对照）**

修改前（以 `NotifyInfoController.page` 为例，其余 Controller 同模式）：

```java
Page<NotifyInfoVo> page = Page.of(params.getCurrent(), params.getSize());
NotifyInfo entity = BeanUtil.toBean(params.getModel(), NotifyInfo.class);
QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
superService.pageAs(page, wrapper, NotifyInfoVo.class);
return R.success(page);
```

修改后：

```java
Page<NotifyInfoVo> page = Page.of(params.getCurrent(), params.getSize());
superService.pageAs(page, PageWrapperHelper.build(params, NotifyInfo.class), NotifyInfoVo.class);
return R.success(page);
```

逐文件执行后清理不再使用的 `BeanUtil`/`QueryWrapper`/`WrapperUtil` import。`list` 方法中同模式的三行 Wrapper 构建同样替换。

- [ ] **Step 3: 编译 + 残留检查**

```bash
cd /Users/tangyh/gitee/mdp/mdp-apps/md-open/open-web
grep -rn "WrapperUtil.buildOperators" --include="*.java" src/main/java ; echo "应无输出"
mvn compile -pl mdp-apps/md-open/open-web -am
```

预期：grep 无输出（或仅剩特殊场景，需人工确认后保留并加注释）；BUILD SUCCESS。

---

### Task 12: worker-server PowerJob 重试处理器 + 清理

**Files:**
- Create: `mdp-apps/md-server/worker-server/src/main/java/top/mddata/worker/processors/NotifyInfoRetryProcessor.java`
- Create: `mdp-apps/md-server/worker-server/src/main/java/top/mddata/worker/processors/EventPushRetryProcessor.java`
- Modify: `mdp-apps/md-server/worker-server/src/main/java/top/mddata/worker/configuration/DubboConfiguration.java`
- Delete（计划已获批）: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/admin/processor/MultiThreadTaskProcessor.java`
- Delete（计划已获批）: `mdp-apps/md-server/worker-server/src/test/java/top/mddata/MultiThreadTaskProcessorTest.java`

**Interfaces:**
- Consumes: `NotifyInfoService.retry(LocalDateTime, int)`、`EventPushService.retry(LocalDateTime, int)`
- Produces: 两个 PowerJob `BasicProcessor` Bean，任务 cron 在 PowerJob 控制台配置（见 Step 4 部署说明）

- [ ] **Step 1: 创建两个重试处理器**

```java
package top.mddata.worker.processors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import top.mddata.open.service.notify.callback.NotifyInfoService;

import java.time.LocalDateTime;

/**
 * 接口回调重试扫描：替代原 DubboConfiguration 中的 @Scheduled（该任务曾被 dubbo.enabled 开关错误控制）。
 * PowerJob 单机任务模式，多实例部署天然只有一个节点执行，无需自实现分布式锁。
 * 建议 cron：0 0/10 * * * ?（沿用原 10 分钟周期）
 */
@Component
@RequiredArgsConstructor
public class NotifyInfoRetryProcessor implements BasicProcessor {

    /** 单次扫描批量上限，防止一次加载全量失败记录 */
    private static final int BATCH_LIMIT = 500;

    private final NotifyInfoService notifyInfoService;

    @Override
    public ProcessResult process(TaskContext context) {
        notifyInfoService.retry(LocalDateTime.now(), BATCH_LIMIT);
        return new ProcessResult(true, "接口回调重试扫描完成");
    }
}
```

```java
package top.mddata.worker.processors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import top.mddata.open.service.notify.event.EventPushService;

import java.time.LocalDateTime;

/**
 * 事件推送重试扫描。
 * 建议 cron：0 0/8 * * * ?（沿用原 8 分钟周期）
 */
@Component
@RequiredArgsConstructor
public class EventPushRetryProcessor implements BasicProcessor {

    private static final int BATCH_LIMIT = 500;

    private final EventPushService eventPushService;

    @Override
    public ProcessResult process(TaskContext context) {
        eventPushService.retry(LocalDateTime.now(), BATCH_LIMIT);
        return new ProcessResult(true, "事件推送重试扫描完成");
    }
}
```

- [ ] **Step 2: 清理 `DubboConfiguration`**

删除两个 `@Scheduled` 方法与两个 `@Autowired` Service 字段，类只保留 Dubbo 装配：

```java
package top.mddata.worker.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import static top.mddata.base.constant.Constants.UTIL_PACKAGE;

@ConditionalOnProperty(
        prefix = "dubbo",
        name = {"enabled"},
        havingValue = "true",
        matchIfMissing = true
)
@Configuration
@EnableDubbo(scanBasePackages = UTIL_PACKAGE)
@Slf4j
public class DubboConfiguration {

    public DubboConfiguration() {
        log.info("dubbo 已加载");
    }
}
```

- [ ] **Step 3: 删除旧线程池及其测试**

```bash
rm /Users/tangyh/gitee/mdp/mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/admin/processor/MultiThreadTaskProcessor.java
rmdir /Users/tangyh/gitee/mdp/mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/admin/processor
rm /Users/tangyh/gitee/mdp/mdp-apps/md-server/worker-server/src/test/java/top/mddata/MultiThreadTaskProcessorTest.java
# 删除后确认旧包无残留引用
cd /Users/tangyh/gitee/mdp/mdp-apps && grep -rn "MultiThreadTaskProcessor" --include="*.java" . ; echo "应无输出"
```

- [ ] **Step 4: 编译验证 + PowerJob 控制台配置说明**

```bash
mvn compile -pl mdp-apps/md-server/worker-server -am
```

预期：BUILD SUCCESS。

部署动作（非代码）：在 PowerJob 控制台新建两个单机任务，分别指向 `notifyInfoRetryProcessor`（cron `0 0/10 * * * ?`）与 `eventPushRetryProcessor`（cron `0 0/8 * * * ?`）。此项写入实施完成报告，提醒用户配置。

---

### Task 13: 重试扫描索引 DDL + 全量构建验证

**Files:**
- Create: `docs/sql/20261013_open_notify_retry_index.sql`

- [ ] **Step 1: 编写索引脚本**

```sql
-- 推送任务重试扫描索引：查询条件为 exec_status + next_request_time / created_at
ALTER TABLE mdo_notify_info ADD INDEX idx_exec_status_next_request_time (exec_status, next_request_time);
ALTER TABLE mdo_event_push ADD INDEX idx_exec_status_next_request_time (exec_status, next_request_time);
```

（执行方式：由用户在对应环境手工执行；本仓库不自动跑 DDL。）

- [ ] **Step 2: 全量测试与构建**

```bash
cd /Users/tangyh/gitee/mdp
mvn test -pl mdp-apps/md-open/open-service
mvn clean install -DskipTests -pl mdp-apps/md-open,mdp-apps/md-server/worker-server -am
mvn checkstyle:check -pl mdp-apps/md-open/open-service
```

预期：测试全绿；BUILD SUCCESS；checkstyle 无新增违规。

- [ ] **Step 3: 编写实施完成报告**

向用户汇报：改动清单、测试结果、遗留部署动作（PowerJob 控制台两个任务配置、索引 DDL 执行、nacos 中 `mdp.notify.threads`/`mdp.notify.threadTasks` 已失效可清理）。

---

## 自检清单（实施完成后核对）

- [ ] `NotifyInfoServiceImpl`/`EventPushServiceImpl` 中已无 `doNotify`/`appendUrlParams`/`buildNextSendTime` 任何一份
- [ ] 全仓 grep 无 `service.admin` 残留（除 git 历史）
- [ ] `notify()` 调用方线程不再发起 HTTP（看日志：Facade 返回耗时 < 50ms）
- [ ] 非法 `mdp.notify.time-level`（如 `2x`）启动即报错
- [ ] `DubboConfiguration` 无 `@Scheduled`；`dubbo.enabled=false` 时重试仍由 PowerJob 执行
- [ ] 内网 notifyUrl（如 `http://127.0.0.1`）在任务生成时被拒绝
