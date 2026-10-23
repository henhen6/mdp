# 开放平台重构（计划 2：网关日志异步化 + SDK 缺陷修复）

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** sop-gateway-server 调用日志异步化（日志异常永不冲掉业务响应）+ configguration 包名改正 + CommonsMultipartFile 验证式删除；mdp-sdk 线程安全/副作用/异常吞噬等缺陷修复。

**计划系列：** 计划 1（md-open 推送域，已确认）→ 本计划 → 计划 3（web-open + 事件码 + 文档）。本计划与计划 1 完全独立，可并行或任意顺序执行。

**Architecture:** 设计文档见 `docs/superpowers/specs/2026-10-13-open-platform-refactor-design.md` §5.2/§5.3。网关只动日志写入路径与包名，主链路（校验/拦截器/泛化调用）零改动；SDK 修复以 public API 最小变更为准（已获批准可改签名）。

**Tech Stack:** Spring Boot 3（@Async）、okhttp、fastjson2、JUnit（SDK 测试体系现为 JUnit 4，新增测试沿用现状，迁移另行评估）。

## Global Constraints

- **禁止任何 git 操作**，检查点以「编译/测试通过」代替提交。
- SDK 测试硬编码的真实 appSecret/私钥改为环境变量读取后，**用户需自行轮换已泄露密钥**（写入完成报告）。
- `signEnabled` 默认值**不改**（保持 false）。
- SDK 测试体系保持 JUnit 4 现状（与本模块既有测试一致），不引入 JUnit 5 混用。
- CommonsMultipartFile 删除是**验证式**的：基线测试证明上传链路不走 fileupload2 分支才删，否则保留加注释。

## 关键既有事实（实施时不必再查）

- 网关日志链路：`RouteServiceImpl.route` 的 finally（:126-130）`SpringUtil.publishEvent(new CallLogEvent(apiCallLog))` 无保护 → `CallLogListener.saveApiCallLog`（同步 @EventListener，直接 `apiCallLogMapper.insert`）。
- `configguration` 包仅含 `MybatisFlexConfiguration.java`、`SystemAutoConfiguration.java`，包名引用只在这两个文件自身（已全仓 grep 确认），重命名是自包含的。
- CommonsMultipartFile 拷贝类（`src/main/java/org/springframework/web/multipart/commons/CommonsMultipartFile.java`）**被引用**：`RequestUtil.getUploadInfo(HttpServletRequest)` :107 `new CommonsMultipartFile(fileItem)`。该方法有两个分支：`StandardMultipartHttpServletRequest` 分支（:127，Spring 标准 multipart 解析，不依赖拷贝类）和 fileupload2 分支（:89-125，依赖拷贝类 + commons-fileupload2）。运行时走哪条分支决定拷贝类是否可删。
- 开放接口写法：接口方法加 `@Open("demo.upload.more")`（`com.gitee.sop.support.annotation.Open`），实现类 `@DubboService @Service`；启动时 ApiRegisterRunner 自动注册进 api 表。文件上传参数类型 `com.gitee.sop.support.dto.FileData`（实现类 `CommonFileData`，字段 name/originalFilename/contentType/data），由网关 `buildFileParam` 从 multipart 注入。
- SDK 上传：`BaseParam.addFile(new UploadFile(表单名, 文件名, byte[]))` → okhttp multipart。
- SDK 下载：参数类须继承 `DownloadRequest<Req>`（实现 `DownloadAware`），调用 `client.download(api)` 返回 `FileResult`。**现状 bug**：`DemoFileDownloadApi` 错误继承 `BaseParam<DemoFileDownloadRequest, Object>`（走 parseResponse，下载永远走不通）。
- `OpenClient.parseResponse`（:222-243）：data 为 JSONArray 时 `(Resp) arr.toJavaList(...)`；否则强转 `(JSONObject) data`——data 为标量时 ClassCastException。
- `OpenClient.buildFileResult`（:193-207）：不检查 `response.isSuccessful()`，IOException 只 log "文件不存在" 返回半空结果。
- `SignUtil.getSignCheckContentV1`（:208）/`getSignCheckContentV2`（:236）：直接 `params.remove("sign")` 污染调用方 Map；`rsaCheckV2`（:298-304）`String.valueOf(params.get("sign"))` 在 sign 缺失时产生 "null" 字符串。
- SDK `MdpBizMsgCrypt.encryptMsg`（:75-109）：结果签名写入实例字段 `signature`/`msgSignature`（:89-90），经 getter 读取——共享实例并发加密会串值。构造校验 `encodingAesKey.length() != 43` 抛 AesException。
- `mdp-sdk-core/pom.xml` :41 有 `io.github.shalousun:common-util` 依赖，main 源码零引用（已 grep 确认）。
- `BaseTest`（mdp-simple-sdk/src/test/.../BaseTest.java:22-31）硬编码真实 appSecret 与完整 RSA 私钥；`NotifyTest`、`TokenTest`、`HttpPostTest` 同样硬编码。
- md-api 现有开放服务位于 `mdp-apps/md-api/api-service/src/main/java/top/mddata/api/`（接口）与 `.../api/impl/`（实现），dto 在 `top.mddata.api.dto`。**目前没有任何 demo.file 后端实现**（SDK 的 DemoFileUploadApi/DemoFileDownloadApi 悬空）。

---

### Task 1: md-api 新建 demo 文件上传/下载开放接口

**Files:**
- Create: `mdp-apps/md-api/api-service/src/main/java/top/mddata/api/DemoOpenService.java`
- Create: `mdp-apps/md-api/api-service/src/main/java/top/mddata/api/dto/DemoUploadResp.java`
- Create: `mdp-apps/md-api/api-service/src/main/java/top/mddata/api/impl/DemoOpenServiceImpl.java`

**Interfaces:**
- Consumes: `@Open` 注解机制、`FileData`/`CommonFileData`
- Produces: `demo.upload.more`（收文件返回文件名+大小）、`demo.download`（返回固定内容文件）两个开放接口，供 Task 2 SDK 基线调用

- [ ] **Step 1: 创建接口与响应 DTO**

```java
package top.mddata.api;

import com.gitee.sop.support.annotation.Open;
import com.gitee.sop.support.dto.FileData;

/**
 * 开放平台能力验证接口（demo）：用于验证经网关的文件上传/下载链路。
 *
 * @author henhen
 */
public interface DemoOpenService {

    /**
     * 文件上传（验证 multipart 经网关解析注入 FileData）。
     *
     * @param file 上传文件
     * @return 文件名与大小
     */
    @Open("demo.upload.more")
    DemoUploadResp uploadMore(FileData file);

    /**
     * 文件下载（验证 FileData 经网关还原为文件响应）。
     *
     * @return 固定内容的演示文件
     */
    @Open("demo.download")
    FileData download();
}
```

```java
package top.mddata.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * demo 文件上传响应。
 *
 * @author henhen
 */
@Accessors(chain = true)
@Data
public class DemoUploadResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 原始文件名 */
    private String fileName;

    /** 文件大小（字节） */
    private Integer size;
}
```

- [ ] **Step 2: 创建实现**

```java
package top.mddata.api.impl;

import com.gitee.sop.support.dto.CommonFileData;
import com.gitee.sop.support.dto.FileData;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import top.mddata.api.DemoOpenService;
import top.mddata.api.dto.DemoUploadResp;

import java.nio.charset.StandardCharsets;

/**
 * demo 接口实现：不做真实存储，仅验证网关文件链路通断。
 *
 * @author henhen
 */
@DubboService
@Service
@Slf4j
public class DemoOpenServiceImpl implements DemoOpenService {

    @Override
    public DemoUploadResp uploadMore(FileData file) {
        log.info("[demo] 收到上传文件, name={}, originalFilename={}, size={}",
                file.getName(), file.getOriginalFilename(), file.getData() == null ? 0 : file.getData().length);
        return new DemoUploadResp()
                .setFileName(file.getOriginalFilename())
                .setSize(file.getData() == null ? 0 : file.getData().length);
    }

    @Override
    public FileData download() {
        CommonFileData fileData = new CommonFileData();
        fileData.setName("demo");
        fileData.setOriginalFilename("demo.txt");
        fileData.setContentType("text/plain");
        fileData.setData("hello open platform".getBytes(StandardCharsets.UTF_8));
        return fileData;
    }
}
```

- [ ] **Step 3: 编译验证**

```bash
mvn compile -pl mdp-apps/md-api/api-service -am
```

预期：BUILD SUCCESS。接口注册在 md-api 启动时由 ApiRegisterRunner 自动完成，无需手工配 api 表。

---

### Task 2: SDK 上传/下载基线测试（CommonsMultipartFile 判定的前提）

**Files:**
- Modify: `mdp-sdk/mdp-simple-sdk/src/main/java/top/mddata/sdk/simple/api/demo/DemoFileDownloadApi.java`（改继承 DownloadRequest，修下载链路）
- Create: `mdp-sdk/mdp-simple-sdk/src/main/java/top/mddata/sdk/simple/response/demo/DemoUploadResp.java`
- Modify: `mdp-sdk/mdp-simple-sdk/src/main/java/top/mddata/sdk/simple/api/demo/DemoFileUploadApi.java`（响应泛型 UserResp → DemoUploadResp）
- Create: `mdp-sdk/mdp-simple-sdk/src/test/java/top/mddata/sdk/test/demo/FileLinkTest.java`

**Interfaces:**
- Consumes: Task 1 的两个开放接口；`UploadFile(表单名, 文件名, byte[])`；`client.download(api)`；`FileResult.getFileData()/getHeaders()`
- Produces: 可重复执行的上传/下载基线测试，作为 Task 5 删除判定与回归手段

- [ ] **Step 1: 修 `DemoFileDownloadApi` 继承（下载链路前提）**

```java
package top.mddata.sdk.simple.api.demo;

import top.mddata.sdk.core.param.DownloadRequest;
import top.mddata.sdk.simple.request.demo.DemoFileDownloadRequest;

/**
 * 下载文件。
 * 必须继承 DownloadRequest（实现 DownloadAware），OpenClient 才会走文件下载分支而非 JSON 解析分支。
 *
 * @author 六如
 */
public class DemoFileDownloadApi extends DownloadRequest<DemoFileDownloadRequest> {
    @Override
    protected String method() {
        return "demo.download";
    }
}
```

- [ ] **Step 2: 新建 SDK 侧响应类并修正上传 API 泛型**

```java
package top.mddata.sdk.simple.response.demo;

/**
 * demo 文件上传响应。
 *
 * @author henhen
 */
public class DemoUploadResp {
    private String fileName;
    private Integer size;

    public String getFileName() {
        return fileName;
    }

    public DemoUploadResp setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public DemoUploadResp setSize(Integer size) {
        this.size = size;
        return this;
    }
}
```

`DemoFileUploadApi` 改为 `extends BaseParam<DemoFileUploadRequest, DemoUploadResp>`（import 从 `response.user.UserResp` 换成 `response.demo.DemoUploadResp`，method 保持 `demo.upload.more`）。

- [ ] **Step 3: 编写基线测试 `FileLinkTest`（沿用本模块 JUnit 4 风格）**

```java
package top.mddata.sdk.test.demo;

import top.mddata.sdk.core.common.FileResult;
import top.mddata.sdk.core.common.Result;
import top.mddata.sdk.core.common.UploadFile;
import top.mddata.sdk.simple.api.demo.DemoFileDownloadApi;
import top.mddata.sdk.simple.api.demo.DemoFileUploadApi;
import top.mddata.sdk.simple.request.demo.DemoFileDownloadRequest;
import top.mddata.sdk.simple.request.demo.DemoFileUploadRequest;
import top.mddata.sdk.simple.response.demo.DemoUploadResp;
import top.mddata.sdk.test.BaseTest;

import java.nio.charset.StandardCharsets;

/**
 * 文件上传/下载链路基线测试。
 * 需先启动本地环境（网关 localhost:23456 + md-api + 注册中心），接口由 ApiRegisterRunner 自动注册。
 * 用途：验证 CommonsMultipartFile 拷贝类删除前后上传链路行为一致（Task 5 的判定依据）。
 *
 * @author henhen
 */
public class FileLinkTest extends BaseTest {

    /** 上传：multipart 经网关解析 → FileData 注入 → 返回文件名与大小 */
    public void testUpload() {
        getAccessToken();
        DemoFileUploadApi api = new DemoFileUploadApi();
        DemoFileUploadRequest request = new DemoFileUploadRequest()
                .setProductName("基线测试")
                .setAddTime("2026-10-13 12:00:00");
        api.setBizModel(request);
        byte[] content = "网关上传链路基线内容".getBytes(StandardCharsets.UTF_8);
        api.addFile(new UploadFile("file", "baseline.txt", content));

        Result<DemoUploadResp> result = client.execute(api);
        logResult(result);
        assertTrue("上传应成功", result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("baseline.txt", result.getData().getFileName());
        assertEquals(Integer.valueOf(content.length), result.getData().getSize());
    }

    /** 下载：FileData 经网关还原 → SDK 收文件流 */
    public void testDownload() {
        getAccessToken();
        DemoFileDownloadApi api = new DemoFileDownloadApi();
        api.setBizModel(new DemoFileDownloadRequest());

        FileResult fileResult = client.download(api);
        assertNotNull("下载应返回文件", fileResult);
        assertNotNull(fileResult.getFileData());
        String text = new String(fileResult.getFileData(), StandardCharsets.UTF_8);
        logger.info("下载内容: {}", text);
        assertEquals("hello open platform", text);
    }
}
```

（`DemoFileDownloadRequest` 已存在于 `request/demo/`，若其含必填字段按现状 set 即可；`logResult`/`getAccessToken` 来自 BaseTest。）

- [ ] **Step 4: 编译 + 启动环境手动执行**

```bash
mvn compile -pl mdp-sdk/mdp-simple-sdk -am
mvn test-compile -pl mdp-sdk/mdp-simple-sdk
```

然后启动 boot-server（含网关与 md-api）后执行 `FileLinkTest` 两个方法。
预期：上传返回 `baseline.txt` + 正确大小；下载返回 `hello open platform`。
**此结果同时回答 Task 5 的判定问题：上传链路当前可用。**

- [ ] **Step 5: 判定上传走哪个分支（Task 5 输入）**

临时在 `RequestUtil.getUploadInfo(HttpServletRequest)` 方法入口（:93 前）加一行：

```java
log.info("[分支判定] getUploadInfo 进入 fileupload2 分支, requestType={}", request.getClass().getName());
```

重跑 `testUpload`：
- 网关日志**没有**这行 → 走的是 `StandardMultipartHttpServletRequest` 分支，拷贝类运行时可删 → Task 5 执行删除；
- **有**这行 → fileupload2 分支是活的，拷贝类不可删 → Task 5 走「保留加注释」分支。
判定后删除该临时日志。

---

### Task 3: 网关调用日志异步化

**Files:**
- Create: `mdp-apps/md-gateway/sop-gateway-server/src/main/java/top/mddata/gateway/sop/configguration/CallLogAsyncConfiguration.java`（Task 4 再随包改名移动；若 Task 4 已先执行则直接放 configuration 包）
- Modify: `mdp-apps/md-gateway/sop-gateway-server/src/main/java/top/mddata/gateway/sop/event/CallLogListener.java`
- Modify: `mdp-apps/md-gateway/sop-gateway-server/src/main/java/top/mddata/gateway/sop/service/impl/RouteServiceImpl.java:126-130`

**Interfaces:**
- Consumes: `CallLogEvent`、`ApiCallLogMapper`
- Produces: Bean `apiCallLogExecutor`（有界队列，满则丢弃 + warn）；`CallLogListener.saveApiCallLog` 改为异步且异常自吞

- [ ] **Step 1: 创建异步配置**

```java
package top.mddata.gateway.sop.configguration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 接口调用日志异步写入配置。
 * 调用日志是旁观型数据：可丢、不可影响业务响应，故队列有界且满则丢弃。
 */
@Slf4j
@Configuration
@EnableAsync
public class CallLogAsyncConfiguration {

    @Bean("apiCallLogExecutor")
    public TaskExecutor apiCallLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("api-call-log-");
        executor.setRejectedExecutionHandler((runnable, pool) ->
                log.warn("调用日志写入队列已满，丢弃一条日志记录"));
        executor.initialize();
        return executor;
    }
}
```

- [ ] **Step 2: `CallLogListener` 改异步 + 异常自吞**

```java
package top.mddata.gateway.sop.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.mddata.open.entity.admin.ApiCallLog;
import top.mddata.open.mapper.admin.ApiCallLogMapper;

/**
 * 调用日志事件监听
 * @author henhen
 * @since 2026/1/12 23:29
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CallLogListener {
    private final ApiCallLogMapper apiCallLogMapper;

    /**
     * 异步落库：日志写失败只记 error，绝不影响业务响应（旧版同步 insert，DB 异常会冲掉响应）。
     */
    @Async("apiCallLogExecutor")
    @EventListener({CallLogEvent.class})
    public void saveApiCallLog(CallLogEvent event) {
        try {
            ApiCallLog source = (ApiCallLog) event.getSource();
            apiCallLogMapper.insert(source);
        } catch (Exception e) {
            log.error("保存接口调用日志失败", e);
        }
    }
}
```

- [ ] **Step 3: `RouteServiceImpl` finally 加保护**

修改前（:126-130）：

```java
} finally {
    if (apiCallLog != null) {
        SpringUtil.publishEvent(new CallLogEvent(apiCallLog));
    }
}
```

修改后：

```java
} finally {
    if (apiCallLog != null) {
        try {
            SpringUtil.publishEvent(new CallLogEvent(apiCallLog));
        } catch (Exception e) {
            // 双保险：即使事件发布失败（如异步队列拒绝策略变更），也不能影响业务响应
            log.error("发布调用日志事件失败, callLogId={}", apiCallLog.getId(), e);
        }
    }
}
```

- [ ] **Step 4: 编译验证**

```bash
mvn compile -pl mdp-apps/md-gateway/sop-gateway-server -am
```

预期：BUILD SUCCESS。回归验证（环境允许时）：关停 DB 或制造日志表写入失败，业务接口仍正常返回。

---

### Task 4: configguration 包名改正

**Files:**
- Move: `mdp-apps/md-gateway/sop-gateway-server/src/main/java/top/mddata/gateway/sop/configguration/`（`MybatisFlexConfiguration.java`、`SystemAutoConfiguration.java`、`CallLogAsyncConfiguration.java`）→ `configuration/`

- [ ] **Step 1: 移动并改写包声明**

```bash
CFG=/Users/tangyh/gitee/mdp/mdp-apps/md-gateway/sop-gateway-server/src/main/java/top/mddata/gateway/sop
mv $CFG/configguration $CFG/configuration
sed -i '' 's/^package top\.mddata\.gateway\.sop\.configguration;/package top.mddata.gateway.sop.configuration;/' $CFG/configuration/*.java
```

- [ ] **Step 2: 全仓复查引用**

```bash
grep -rn "configguration" --include="*.java" --include="*.imports" --include="*.factories" --include="*.yml" --include="*.properties" . ; echo "应无输出"
```

若有输出（如自动装配注册文件），同步修正。

- [ ] **Step 3: 编译验证**

```bash
mvn compile -pl mdp-apps/md-gateway/sop-gateway-server -am
```

预期：BUILD SUCCESS。

---

### Task 5: CommonsMultipartFile 验证式删除

**Files:**
- 判定依据: Task 2 Step 5 的分支判定结果
- Modify（删除分支时）: `mdp-apps/md-gateway/sop-gateway-server/src/main/java/top/mddata/gateway/sop/util/RequestUtil.java`
- Delete（删除分支且计划获批）: `mdp-apps/md-gateway/sop-gateway-server/src/main/java/org/springframework/web/multipart/commons/CommonsMultipartFile.java`
- Modify（删除分支时）: `mdp-apps/md-gateway/sop-gateway-server/pom.xml`（若 commons-fileupload2 依赖仅该分支使用）

- [ ] **Step 1: 分支 A —— 判定为「可删」（上传走 StandardMultipartHttpServletRequest 分支）**

1. `RequestUtil` 中删除 `getUploadInfo(HttpServletRequest)` 整个 fileupload2 方法（:89-125），并把 `getUploadInfo(request)` 调用点统一指向 `getUploadInfo(StandardMultipartHttpServletRequest)`：若调用处传入的是原始 `HttpServletRequest`，需确认网关全局有 multipart 解析（Spring Boot 默认开启），将调用处强转前先 `instanceof` 断言；无 multipart 内容时 `uploadContext` 为 null 即可。
2. 删除拷贝类 `org/springframework/web/multipart/commons/CommonsMultipartFile.java`（同时删除空的 `org/springframework/...` 目录树）。
3. `RequestUtil` 清理 import：`DiskFileItem`、`DiskFileItemFactory`、`JakartaServletFileUpload`、`CommonsMultipartFile`、`StandardCharsets`、`ArrayList`（若不再使用）。
4. `pom.xml` 移除 commons-fileupload2 依赖（先 `mvn dependency:analyze | grep fileupload` 确认无其他使用）。
5. 编译通过后**重跑 Task 2 的 `FileLinkTest.testUpload`**：上传仍返回正确文件名与大小 → 删除成立。

- [ ] **Step 2: 分支 B —— 判定为「保留」（fileupload2 分支是活的）**

不删任何代码，仅在拷贝类文件头加注释：

```java
/**
 * 【兼容性拷贝，勿删】Spring 6 已移除 org.springframework.web.multipart.commons 包，
 * 本网关的文件上传解析（RequestUtil#getUploadInfo 的 fileupload2 分支）仍依赖 commons-fileupload2 的
 * DiskFileItem → MultipartFile 适配，故从 Spring 5 拷贝本类并适配 fileupload2 API。
 * 后续若上传解析统一迁移到 StandardMultipartHttpServletRequest，本类可随 fileupload2 分支一并删除。
 */
```

- [ ] **Step 3: 结果记录**

无论走哪个分支，把判定过程与结论写入实施完成报告（含 Task 2 基线测试输出截图/文本）。

---

### Task 6: SDK MdpBizMsgCrypt 线程安全改造

**Files:**
- Create: `mdp-sdk/mdp-sdk-core/src/main/java/top/mddata/sdk/core/aes/EncryptResult.java`
- Modify: `mdp-sdk/mdp-sdk-core/src/main/java/top/mddata/sdk/core/aes/MdpBizMsgCrypt.java`
- Modify: 调用方（`mdp-simple-sdk/src/test/java/top/mddata/sdk/test/demo/NotifyTest.java` 等 grep 到的 encryptMsg/getSignature 调用点）

**Interfaces:**
- Consumes: 无
- Produces: `EncryptResult`（getBody()/getSignature()/getMsgSignature()）；`MdpBizMsgCrypt.encryptMsg(...)` 返回值从 `JSONObject` 变为 `EncryptResult`；**删除** `getSignature()`/`getMsgSignature()` 实例方法

- [ ] **Step 1: 创建 `EncryptResult`**

```java
package top.mddata.sdk.core.aes;

import com.alibaba.fastjson2.JSONObject;

/**
 * 加密结果：报文体 + 签名。
 * 替代旧版将签名写入 MdpBizMsgCrypt 实例字段的做法（共享实例并发加密时签名会串值）。
 *
 * @author henhen
 */
public class EncryptResult {

    private final JSONObject body;
    private final String signature;
    private final String msgSignature;

    public EncryptResult(JSONObject body, String signature, String msgSignature) {
        this.body = body;
        this.signature = signature;
        this.msgSignature = msgSignature;
    }

    public JSONObject getBody() {
        return body;
    }

    public String getSignature() {
        return signature;
    }

    public String getMsgSignature() {
        return msgSignature;
    }
}
```

- [ ] **Step 2: 改造 `encryptMsg`（修改前/后对照）**

修改前（:75-122 关键段）：

```java
public JSONObject encryptMsg(String plaintext, String timestamp, String nonce, int encryptionMode)
        throws AesException {
    ...
    this.signature = signature;
    this.msgSignature = msgSignature;
    ...
    return body;
}

/** 最近一次 encryptMsg 计算得到的 signature */
private String signature;
/** 最近一次 encryptMsg 计算得到的 msgSignature（明文模式为 null） */
private String msgSignature;

public String getSignature() { return signature; }
public String getMsgSignature() { return msgSignature; }
```

修改后：

```java
/**
 * 加密消息并构建请求体。
 * 签名随返回值返回（不再写实例字段，实例可安全共享给多线程使用）。
 *
 * @param plaintext      明文 JSON 字符串
 * @param timestamp      时间戳
 * @param nonce          随机字符串
 * @param encryptionMode 加密模式（{@link #MODE_PLAINTEXT} / {@link #MODE_COMPATIBLE} / {@link #MODE_ENCRYPTED}）
 * @return 报文体与签名
 * @throws AesException 加密失败时抛出
 */
public EncryptResult encryptMsg(String plaintext, String timestamp, String nonce, int encryptionMode)
        throws AesException {
    String encrypt;
    if (encryptionMode == MODE_PLAINTEXT) {
        encrypt = "";
    } else {
        encrypt = encrypt(plaintext);
    }

    // 计算签名
    String signature = SHA1.getSHA1(token, timestamp, nonce, "");
    String msgSignature = encryptionMode == MODE_PLAINTEXT
            ? null
            : SHA1.getSHA1(token, timestamp, nonce, encrypt);

    // 构建请求体
    JSONObject body = new JSONObject();
    if (encryptionMode == MODE_PLAINTEXT) {
        // 明文模式：直接发送明文
        body = JSON.parseObject(plaintext);
    } else if (encryptionMode == MODE_COMPATIBLE) {
        // 兼容模式：明文展开 + 密文 + appKey
        JSONObject plainData = JSON.parseObject(plaintext);
        body.putAll(plainData);
        body.put("encrypt", encrypt);
        body.put("appKey", appKey);
    } else {
        // 安全模式：仅密文 + appKey
        body.put("encrypt", encrypt);
        body.put("appKey", appKey);
    }
    return new EncryptResult(body, signature, msgSignature);
}
```

同时删除实例字段 `signature`/`msgSignature` 及 `getSignature()`/`getMsgSignature()` 两个方法。

- [ ] **Step 3: 修正调用方**

```bash
grep -rn "encryptMsg\|getSignature\|getMsgSignature" /Users/tangyh/gitee/mdp/mdp-sdk --include="*.java" | grep -v "md-sop-support"
```

逐处改为：

```java
EncryptResult encryptResult = crypt.encryptMsg(plaintext, timestamp, nonce, mode);
JSONObject body = encryptResult.getBody();
String signature = encryptResult.getSignature();
String msgSignature = encryptResult.getMsgSignature();
```

- [ ] **Step 4: 并发安全性测试（TDD，放 mdp-sdk-core/src/test）**

```java
package top.mddata.sdk.core.aes;

import com.alibaba.fastjson2.JSONObject;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 共享实例并发加密：每个线程的签名必须与自己线程的报文对应（旧版实例字段会串值）。
 */
public class MdpBizMsgCryptConcurrentTest {

    @Test
    public void testConcurrentEncrypt() throws Exception {
        MdpBizMsgCrypt crypt = new MdpBizMsgCrypt(
                MdpBizMsgCrypt.generateToken(),
                MdpBizMsgCrypt.generateEncodingAesKey(),
                "testAppKey");
        int threads = 16;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        Set<String> signatures = ConcurrentHashMap.newKeySet();
        Set<String> errors = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threads; i++) {
            final int index = i;
            pool.submit(() -> {
                try {
                    for (int j = 0; j < 50; j++) {
                        String plaintext = "{\"seq\":\"" + index + "-" + j + "\"}";
                        String timestamp = MdpBizMsgCrypt.generateTimestamp();
                        String nonce = MdpBizMsgCrypt.generateNonce();
                        EncryptResult result = crypt.encryptMsg(plaintext, timestamp, nonce,
                                MdpBizMsgCrypt.MODE_ENCRYPTED);
                        signatures.add(result.getSignature());
                        JSONObject body = result.getBody();
                        if (body == null || !body.containsKey("encrypt")) {
                            errors.add("报文缺 encrypt: " + index);
                        }
                    }
                } catch (Exception e) {
                    errors.add(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        pool.shutdown();
        assertTrue("并发执行有错误: " + errors, errors.isEmpty());
        assertEquals("每次加密的签名应唯一", threads * 50, signatures.size());
    }
}
```

注意：mdp-sdk-core 的 pom 若无 junit 依赖需补（test scope，与本模块一致的 JUnit 4）。每次加密的 nonce 不同 → 签名必然不同，签名唯一性断言即可证明实例字段不再串值。

- [ ] **Step 5: 编译 + 测试**

```bash
mvn test -pl mdp-sdk/mdp-sdk-core
```

预期：BUILD SUCCESS，并发测试通过。

---

### Task 7: SDK SignUtil 副作用与 rsaCheckV2 修复

**Files:**
- Modify: `mdp-sdk/mdp-sdk-core/src/main/java/top/mddata/sdk/core/sign/SignUtil.java`
- Test: `mdp-sdk/mdp-sdk-core/src/test/java/top/mddata/sdk/core/sign/SignUtilTest.java`

**Interfaces:**
- Consumes: 无
- Produces: `getSignCheckContentV1/V2` 不再修改调用方入参；`rsaCheckV2(Map, String, String, String)` 在 sign 缺失时返回 false

- [ ] **Step 1: 写失败测试**

```java
package top.mddata.sdk.core.sign;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SignUtilTest {

    @Test
    public void getSignCheckContent不修改调用方入参() {
        Map<String, String> params = new HashMap<>();
        params.put("b", "2");
        params.put("a", "1");
        params.put("sign", "xxx");
        params.put("sign_type", "RSA2");

        String content = SignUtil.getSignCheckContentV1(params);
        assertEquals("a=1&b=2", content);
        assertTrue("调用方入参的 sign 不应被删除", params.containsKey("sign"));
        assertTrue("调用方入参的 sign_type 不应被删除", params.containsKey("sign_type"));

        Map<String, Object> params2 = new HashMap<>();
        params2.put("b", "2");
        params2.put("a", 1);
        params2.put("sign", "xxx");
        String content2 = SignUtil.getSignCheckContentV2(params2);
        assertEquals("a=1&b=2", content2);
        assertTrue(params2.containsKey("sign"));
    }

    @Test
    public void rsaCheckV2签名缺失返回false() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("a", "1");
        assertFalse(SignUtil.rsaCheckV2(params, "anyPublicKey", "UTF-8", "RSA2"));
    }
}
```

- [ ] **Step 2: 跑测试确认失败**

预期：`getSignCheckContent不修改调用方入参` 断言失败（sign 被 remove）；`rsaCheckV2签名缺失返回false` 可能抛出异常或走到验签逻辑报错。

- [ ] **Step 3: 修复实现**

`getSignCheckContentV1`（:208-229）改为：

```java
public static String getSignCheckContentV1(Map<String, String> params) {
    if (params == null) {
        return null;
    }

    // 复制入参再剔除签名字段：不污染调用方的 Map（旧版直接 remove，调用方数据被篡改）
    Map<String, String> signParams = new HashMap<>(params);
    signParams.remove("sign");
    signParams.remove("sign_type");

    StringBuilder content = new StringBuilder();
    List<String> keys = new ArrayList<String>(signParams.keySet());
    Collections.sort(keys);

    for (int i = 0; i < keys.size(); i++) {
        String key = keys.get(i);
        if (signParams.get(key) != null) {
            String value = signParams.get(key);
            content.append(i == 0 ? "" : "&").append(key).append("=").append(value);
        }
    }

    return content.toString();
}
```

`getSignCheckContentV2`（:236-256）同样改为先 `Map<String, Object> signParams = new HashMap<>(params);` 再 `signParams.remove("sign");`，后续一律用 `signParams`。

`rsaCheckV2(Map, String, String, String)`（:298-304）改为：

```java
public static boolean rsaCheckV2(Map<String, ?> params, String publicKey,
                                 String charset, String signType) throws SopSignException {
    Object signObj = params.get("sign");
    if (signObj == null) {
        // sign 缺失直接判失败（旧版 String.valueOf(null) 产生 "null" 字符串进入验签）
        return false;
    }
    String sign = String.valueOf(signObj);
    String content = getSignCheckContentV2(params);

    return rsaCheck(content, sign, publicKey, charset, signType);
}
```

- [ ] **Step 4: 跑测试确认通过**

```bash
mvn test -pl mdp-sdk/mdp-sdk-core -Dtest=SignUtilTest
```

预期：Tests run: 2, Failures: 0。

---

### Task 8: SDK OpenClient 响应解析与下载修复

**Files:**
- Modify: `mdp-sdk/mdp-sdk-core/src/main/java/top/mddata/sdk/core/client/OpenClient.java`

**Interfaces:**
- Consumes: `SdkException(String)` / `SdkException(String, Throwable)`（已存在，:140 有用例）
- Produces: `parseResponse` 在 data 为标量时抛 SdkException（具体消息）；`buildFileResult` HTTP 非 2xx 抛 SdkException，IO 异常不再吞噬

- [ ] **Step 1: `parseResponse` data 类型校验（修改前/后对照）**

修改前（:236-240）：

```java
if (data instanceof JSONArray arr) {
    dataObj = (Resp) arr.toJavaList(param.getResponseClass());
} else {
    dataObj = ((JSONObject) data).toJavaObject(param.getResponseClass());
}
```

修改后：

```java
if (data instanceof JSONArray arr) {
    dataObj = (Resp) arr.toJavaList(param.getResponseClass());
} else if (data instanceof JSONObject obj) {
    dataObj = obj.toJavaObject(param.getResponseClass());
} else {
    // data 为标量（字符串/数字等）时直接报具体错误，不再延迟到调用方 CCE
    throw new SdkException("响应" + rootNodeName + "节点类型与预期不符, method=" + param.getMethod()
            + ", 实际类型=" + data.getClass().getSimpleName() + ", 值=" + data);
}
```

- [ ] **Step 2: `buildFileResult` fail fast（修改前/后对照）**

修改前（:193-207）：

```java
protected FileResult buildFileResult(Response response) {
    FileResult fileResult = new FileResult();
    Headers headers = response.headers();
    try {
        byte[] bytes = null;
        if (response.body() != null) {
            bytes = response.body().bytes();
        }
        fileResult.setFileData(bytes);
    } catch (IOException e) {
        log.error("文件不存在", e);
    }
    fileResult.setHeaders(headers);
    return fileResult;
}
```

修改后：

```java
protected FileResult buildFileResult(Response response) {
    if (!response.isSuccessful()) {
        // 非 2xx 直接失败（旧版会把错误页字节当文件内容返回）
        throw new SdkException("文件下载失败, HTTP状态码=" + response.code());
    }
    FileResult fileResult = new FileResult();
    fileResult.setHeaders(response.headers());
    try {
        byte[] bytes = null;
        if (response.body() != null) {
            bytes = response.body().bytes();
        }
        fileResult.setFileData(bytes);
    } catch (IOException e) {
        // fail fast：不再只记日志后返回半空结果
        throw new SdkException("读取下载文件内容失败", e);
    }
    return fileResult;
}
```

- [ ] **Step 3: 编译验证**

```bash
mvn compile -pl mdp-sdk/mdp-sdk-core
```

预期：BUILD SUCCESS。`SdkException(String, Throwable)` 构造若不存在，编译会报——届时改用 `new SdkException("读取下载文件内容失败：" + e.getMessage())`。

---

### Task 9: pom 清理 + 测试密钥环境变量化 + README 修正

**Files:**
- Modify: `mdp-sdk/mdp-sdk-core/pom.xml`（删除 common-util 依赖，:41 附近）
- Modify: `mdp-sdk/mdp-simple-sdk/src/test/java/top/mddata/sdk/test/BaseTest.java`
- Modify: `mdp-sdk/mdp-simple-sdk/src/test/java/top/mddata/sdk/test/demo/NotifyTest.java`、`token/TokenTest.java`、`demo/HttpPostTest.java`（grep 出的硬编码点）
- Modify: `mdp-sdk/README.md`、`mdp-sdk/mdp-simple-sdk/README.md`（fastjson → fastjson2 描述修正）

- [ ] **Step 1: 删除 common-util 依赖**

```bash
grep -rn "io.github.shalousun\|common-util" /Users/tangyh/gitee/mdp/mdp-sdk --include="*.java" ; echo "应无输出"
mvn dependency:analyze -pl mdp-sdk/mdp-sdk-core 2>&1 | grep -A2 "common-util"
```

确认零引用后删除 pom 中该 `<dependency>` 块，`mvn compile -pl mdp-sdk/mdp-sdk-core` 验证。

- [ ] **Step 2: 密钥改环境变量读取（BaseTest 示例，其余测试类同模式）**

修改前（:22-31）：

```java
String url = "http://localhost:23456/api";
protected String appKey = "ruoyi-vue-sso";
protected String appSecret = "0Tvtm2xrTWG7n2azkM4FPyHwS6c3NxjQjYKh";
String privateKeyIsv = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC/VS3h9F...（完整私钥）";
```

修改后：

```java
String url = System.getenv().getOrDefault("MDP_TEST_URL", "http://localhost:23456/api");
protected String appKey = System.getenv().getOrDefault("MDP_TEST_APP_KEY", "ruoyi-vue-sso");
/** 应用秘钥，用于获取 accessToken。从环境变量读取，禁止提交真实值 */
protected String appSecret = System.getenv().getOrDefault("MDP_TEST_APP_SECRET", "请配置环境变量MDP_TEST_APP_SECRET");
/** 开发者私钥。从环境变量读取，禁止提交真实值 */
String privateKeyIsv = System.getenv().getOrDefault("MDP_TEST_PRIVATE_KEY", "请配置环境变量MDP_TEST_PRIVATE_KEY");
```

对 `NotifyTest`、`TokenTest`、`HttpPostTest` 中的硬编码密钥/私钥/token/aesKey 做同样处理（逐一 grep `appSecret\|privateKey\|MIIE\|encodingAesKey\s*=\s*"` 定位）。

- [ ] **Step 3: README 修正**

`mdp-sdk/README.md:7` 与 `mdp-sdk/mdp-simple-sdk/README.md:12-13` 中 fastjson 相关描述改为 fastjson2（依赖实际为 `com.alibaba.fastjson2:fastjson2`）。

- [ ] **Step 4: 全量验证**

```bash
cd /Users/tangyh/gitee/mdp/mdp-sdk && mvn clean install -DskipTests
mvn compile -pl mdp-apps/md-gateway/sop-gateway-server -am
```

预期：全部 BUILD SUCCESS。

- [ ] **Step 5: 编写实施完成报告**

汇报：改动清单、Task 5 判定结论、Task 2 基线测试结果、**提醒用户轮换已入库的 appSecret 与 RSA 私钥**（NotifyTest/BaseTest 中曾硬编码真实值）。
