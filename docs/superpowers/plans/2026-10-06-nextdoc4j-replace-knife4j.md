# NextDoc4j 替换 knife4j 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将项目全部 knife4j 在线文档替换为 NextDoc4j 1.4.1，单体 boot-server 可独立访问，微服务各 server 可独立访问且 inner-gateway-server 可聚合访问。

**Architecture:** 业务服务（WebMvc）经 `md-openapi3-starter` 引入 `nextdoc4j-spring-boot-starter` + `springdoc-openapi-starter-webmvc-ui`；网关（WebFlux）引入 `nextdoc4j-gateway-spring-boot-starter` + `springdoc-openapi-starter-webflux-ui`，用 auto-discovery 替代自定义聚合 Controller。springdoc 升级到 2.8.17，swagger-core 对齐 2.2.47。

**Tech Stack:** NextDoc4j 1.4.1、springdoc 2.8.17、swagger-core 2.2.47、Spring Boot 3.x、Spring Cloud Gateway（WebFlux）。

**Spec:** `docs/superpowers/specs/2026-10-06-nextdoc4j-replace-knife4j-design.md`

## Global Constraints

- **禁止任何 git add / git commit / git push 操作**（用户全局约定，覆盖技能默认的 commit 步骤）
- NextDoc4j 版本固定 **1.4.1**；springdoc 版本固定 **2.8.17**；swagger-core 版本固定 **2.2.47**
- 模块名 `md-openapi3-starter`、包名 `top.mddata.base.openapi3` 保持不变
- 配置前缀 `mdp.swagger` → `mdp.nextdoc4j`；类名 `Swagger*` → `NextDoc4j*`
- 文档入口路径保持 `/doc.html` 不变（白名单无需改动）
- 白名单核对结论（spec 4.8，规划阶段已核对）：NextDoc4j 需放行的 `/doc.html`、`/webjars/**`、`/v3/api-docs/**`、`/swagger-resources/**`、`/favicon.ico` 已被 `IgnoreProperties.baseUri` 的 `/**/{p}.html`、`/**/{p}.ico`、`/**/api-docs/**`、`/**/swagger-resources/**`、`/**/webjars/**` 全部覆盖；`BasicConfigurer`（`/*.html`、`/webjars/**`）与 `XssProperties`（`/**/doc.html`、`/webjars/**`）同理，**三处白名单均无需修改**
- 代码注释、提交信息、交互文案使用简体中文
- 构建验证命令在仓库根目录执行；checkstyle 不可跳过
- 本任务为依赖/配置替换，无新增业务逻辑，验证手段为编译 + checkstyle + 残留扫描，不适用 TDD

---

### Task 1: mdp-parent 依赖管理改造

**Files:**
- Modify: `mdp-parent/pom.xml`（properties 段约 42-48 行；dependencyManagement 段约 183-213、245-255 行）

**Interfaces:**
- Produces: 版本属性 `nextdoc4j.version=1.4.1`、`springdoc.version=2.8.17`、`swagger-core.version=2.2.47`；dependencyManagement 中新增 `top.nextdoc4j:nextdoc4j-spring-boot-starter` 与 `top.nextdoc4j:nextdoc4j-gateway-spring-boot-starter` 两条托管（后续任务 2、5 直接引用、不写版本号）

- [ ] **Step 1: 修改 properties 段**

修改前（mdp-parent/pom.xml 约 43-46 行）：

```xml
        <!-- 文档相关 -->
        <springdoc.version>2.8.5</springdoc.version>
        <knife4j-swagger-models-v3.version>2.2.28</knife4j-swagger-models-v3.version>
        <knife4j.version>4.5.0</knife4j.version>
```

修改后：

```xml
        <!-- 文档相关 -->
        <springdoc.version>2.8.17</springdoc.version>
        <swagger-core.version>2.2.47</swagger-core.version>
        <nextdoc4j.version>1.4.1</nextdoc4j.version>
```

- [ ] **Step 2: 删除 knife4j BOM，新增 NextDoc4j 托管依赖**

修改前（约 207-213 行）：

```xml
            <dependency>
                <groupId>com.github.xiaoymin</groupId>
                <artifactId>knife4j-dependencies</artifactId>
                <version>${knife4j.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
```

修改后（原位替换为以下两条托管依赖）：

```xml
            <dependency>
                <groupId>top.nextdoc4j</groupId>
                <artifactId>nextdoc4j-spring-boot-starter</artifactId>
                <version>${nextdoc4j.version}</version>
            </dependency>
            <dependency>
                <groupId>top.nextdoc4j</groupId>
                <artifactId>nextdoc4j-gateway-spring-boot-starter</artifactId>
                <version>${nextdoc4j.version}</version>
            </dependency>
```

注意：约 183-190 行的 `org.springdoc:springdoc-openapi`（pom import）保留不动，仅版本随 `${springdoc.version}` 升到 2.8.17；其上方注释「以上几个配置的顺序不能错」保留。

- [ ] **Step 3: 修正 swagger-annotations/models 的版本引用**

修改前（约 245-255 行）：

```xml
            <!-- Swagger文档 -->
            <dependency>
                <groupId>io.swagger.core.v3</groupId>
                <artifactId>swagger-annotations-jakarta</artifactId>
                <version>${knife4j-swagger-models-v3.version}</version>
            </dependency>
            <dependency>
                <groupId>io.swagger.core.v3</groupId>
                <artifactId>swagger-models-jakarta</artifactId>
                <version>${knife4j-swagger-models-v3.version}</version>
            </dependency>
```

修改后（仅版本属性名和值变化，与 springdoc 2.8.17 对齐）：

```xml
            <!-- Swagger文档 -->
            <dependency>
                <groupId>io.swagger.core.v3</groupId>
                <artifactId>swagger-annotations-jakarta</artifactId>
                <version>${swagger-core.version}</version>
            </dependency>
            <dependency>
                <groupId>io.swagger.core.v3</groupId>
                <artifactId>swagger-models-jakarta</artifactId>
                <version>${swagger-core.version}</version>
            </dependency>
```

- [ ] **Step 4: 验证父 pom 可被解析**

Run:

```bash
cd /Users/tangyh/gitee/mdp/mdp-parent && mvn validate -q
```

Expected: BUILD SUCCESS，无 "Could not resolve dependencies" 错误

---

### Task 2: md-openapi3-starter 改造（核心）

**Files:**
- Modify: `mdp-base/md-openapi3-starter/pom.xml`
- Create: `mdp-base/md-openapi3-starter/src/main/java/top/mddata/base/openapi3/properties/NextDoc4jProperties.java`
- Create: `mdp-base/md-openapi3-starter/src/main/java/top/mddata/base/openapi3/NextDoc4jAutoConfiguration.java`
- Delete: `mdp-base/md-openapi3-starter/src/main/java/top/mddata/base/openapi3/properties/SwaggerProperties.java`
- Delete: `mdp-base/md-openapi3-starter/src/main/java/top/mddata/base/openapi3/SwaggerAutoConfiguration.java`
- Delete: `mdp-base/md-openapi3-starter/src/main/java/top/mddata/base/openapi3/MyKnife4jOpenApiCustomizer.java`
- Delete: `mdp-base/md-openapi3-starter/src/main/java/top/mddata/base/openapi3/SwaggerWebMvcConfigurer.java`
- Modify: `mdp-base/md-openapi3-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

**Interfaces:**
- Consumes: Task 1 的 `nextdoc4j-spring-boot-starter` 托管版本、`springdoc.version=2.8.17`
- Produces:
  - `top.mddata.base.openapi3.properties.NextDoc4jProperties`，前缀 `mdp.nextdoc4j`，字段：`title/description/version/license/licenseUrl/termsOfServiceUrl/contact(name,url,email)/globalOperationParameters(name,description,modelRef,parameterType,required,defaultValue,allowEmptyValue)`（与旧 SwaggerProperties 字段完全一致）
  - `top.mddata.base.openapi3.NextDoc4jAutoConfiguration`，Bean：`customOpenApi()`、`globalOperationCustomizer()`
  - 后续 Task 4 的 yml 依赖 `mdp.nextdoc4j.*` 前缀生效

- [ ] **Step 1: 替换 pom 依赖**

修改前（mdp-base/md-openapi3-starter/pom.xml 约 36-39 行）：

```xml
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
        </dependency>
```

修改后：

```xml
        <dependency>
            <groupId>top.nextdoc4j</groupId>
            <artifactId>nextdoc4j-spring-boot-starter</artifactId>
        </dependency>
        <!-- NextDoc4j 要求宿主自行引入 springdoc UI（WebMvc 栈） -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        </dependency>
```

同时把模块 description 从「swagger文档模块」改为「NextDoc4j在线文档模块」。

- [ ] **Step 2: 创建 NextDoc4jProperties**

新建 `mdp-base/md-openapi3-starter/src/main/java/top/mddata/base/openapi3/properties/NextDoc4jProperties.java`，完整内容：

```java
package top.mddata.base.openapi3.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.mddata.base.constant.Constants;

import java.util.List;

import static top.mddata.base.openapi3.properties.NextDoc4jProperties.PREFIX;

/**
 * NextDoc4j 在线文档属性配置
 * 必须配置 prefix ，才能有提示
 *
 * @author henhen6
 * @since 2018/11/18 9:17
 */
@Data
@ConfigurationProperties(prefix = PREFIX)
public class NextDoc4jProperties {
    public static final String PREFIX = Constants.PROJECT_PREFIX + ".nextdoc4j";

    /**
     * 标题
     **/
    private String title = "在线文档";
    /**
     * 描述
     **/
    private String description = "md-cloud 在线文档";
    /**
     * 版本
     **/
    private String version = "1.0";
    /**
     * 许可证
     **/
    private String license = "";
    /**
     * 许可证URL
     **/
    private String licenseUrl = "";
    /**
     * 服务条款URL
     **/
    private String termsOfServiceUrl = "";

    private Contact contact = new Contact();

    /**
     * 全局参数配置
     **/
    private List<GlobalOperationParameter> globalOperationParameters;

    @Setter
    @Getter
    public static class GlobalOperationParameter {
        /**
         * 参数名
         **/
        private String name;

        /**
         * 描述信息
         **/
        private String description = "全局参数";

        /**
         * 指定参数类型
         **/
        private String modelRef = "String";

        /**
         * 参数放在哪个地方:header,query,path,body.form
         **/
        private String parameterType = "header";

        /**
         * 参数是否必须传
         **/
        private Boolean required = false;
        /**
         * 默认值
         */
        private String defaultValue = "";
        /**
         * 允许为空
         */
        private Boolean allowEmptyValue = true;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Contact {
        /**
         * 联系人
         **/
        private String name = "";
        /**
         * 联系人url
         **/
        private String url = "";
        /**
         * 联系人email
         **/
        private String email = "";
    }

}
```

- [ ] **Step 3: 创建 NextDoc4jAutoConfiguration**

新建 `mdp-base/md-openapi3-starter/src/main/java/top/mddata/base/openapi3/NextDoc4jAutoConfiguration.java`，完整内容：

```java
package top.mddata.base.openapi3;

import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import top.mddata.base.openapi3.properties.NextDoc4jProperties;

import java.util.List;

/**
 * NextDoc4j 在线文档自动配置
 *
 * @author henhen6
 * @since 2018/11/18 9:22
 */
@ConditionalOnProperty(prefix = "nextdoc4j", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(NextDoc4jProperties.class)
public class NextDoc4jAutoConfiguration {
    private final NextDoc4jProperties nextDoc4jProperties;

    public NextDoc4jAutoConfiguration(NextDoc4jProperties nextDoc4jProperties) {
        this.nextDoc4jProperties = nextDoc4jProperties;
    }

    /**
     * 为所有接口追加全局参数（如 Token 请求头）
     *
     * @return 全局 Operation 定制器
     */
    @Bean
    public GlobalOperationCustomizer globalOperationCustomizer() {
        return (operation, handlerMethod) -> {
            List<NextDoc4jProperties.GlobalOperationParameter> globalOperationParameters =
                    nextDoc4jProperties.getGlobalOperationParameters();

            if (CollUtil.isEmpty(globalOperationParameters)) {
                return operation;
            }
            for (NextDoc4jProperties.GlobalOperationParameter globalOperationParameter : globalOperationParameters) {
                operation = operation.addParametersItem(
                        new Parameter()
                                .in(globalOperationParameter.getParameterType())
                                .required(globalOperationParameter.getRequired())
                                .schema(new StringSchema())
                                .example(globalOperationParameter.getDefaultValue())
                                .description(globalOperationParameter.getDescription())
                                .allowEmptyValue(globalOperationParameter.getAllowEmptyValue())
                                .name(globalOperationParameter.getName())
                );
            }
            return operation;
        };
    }

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title(nextDoc4jProperties.getTitle())
                        .description(nextDoc4jProperties.getDescription())
                        .version(nextDoc4jProperties.getVersion())
                        .termsOfService(nextDoc4jProperties.getTermsOfServiceUrl())
                        .contact(new Contact().name(nextDoc4jProperties.getContact().getName())
                                .url(nextDoc4jProperties.getContact().getUrl())
                                .email(nextDoc4jProperties.getContact().getEmail()))
                        .license(new License().name(nextDoc4jProperties.getLicense())
                                .url(nextDoc4jProperties.getLicenseUrl())));
    }

}
```

- [ ] **Step 4: 删除旧类并更新自动装配注册**

删除以下 4 个文件（用 Bash `rm`）：

```bash
rm mdp-base/md-openapi3-starter/src/main/java/top/mddata/base/openapi3/properties/SwaggerProperties.java
rm mdp-base/md-openapi3-starter/src/main/java/top/mddata/base/openapi3/SwaggerAutoConfiguration.java
rm mdp-base/md-openapi3-starter/src/main/java/top/mddata/base/openapi3/MyKnife4jOpenApiCustomizer.java
rm mdp-base/md-openapi3-starter/src/main/java/top/mddata/base/openapi3/SwaggerWebMvcConfigurer.java
```

修改 `mdp-base/md-openapi3-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`，修改前：

```text
top.mddata.base.openapi3.SwaggerAutoConfiguration
```

修改后：

```text
top.mddata.base.openapi3.NextDoc4jAutoConfiguration
```

- [ ] **Step 5: 编译并执行 checkstyle**

Run:

```bash
cd /Users/tangyh/gitee/mdp/mdp-base && mvn -pl md-openapi3-starter -am clean install -DskipTests
```

Expected: BUILD SUCCESS（含 checkstyle 检查通过），target 中生成 md-openapi3-starter jar

---

### Task 3: md-annotation 移除 knife4j-core 依赖

**Files:**
- Modify: `mdp-base/md-annotation/pom.xml`（约 17-20 行）

**Interfaces:**
- Consumes: Task 1 的 swagger-core 托管版本
- Produces: md-annotation 不再传递 knife4j-core；`swagger-annotations-jakarta`、`swagger-models-jakarta` 保留（业务代码大量使用 `@Tag`、`@Operation` 注解）

- [ ] **Step 1: 删除 knife4j-core 依赖块**

修改前：

```xml
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-core</artifactId>
        </dependency>
```

修改后：整块删除（`swagger-annotations-jakarta`、`swagger-models-jakarta` 两个依赖保留不动）。

- [ ] **Step 2: 编译验证**

Run:

```bash
cd /Users/tangyh/gitee/mdp/mdp-base && mvn -pl md-annotation -am clean install -DskipTests
```

Expected: BUILD SUCCESS

---

### Task 4: boot-server 与 worker-server 本地 yml 改造

**Files:**
- Modify: `mdp-apps/md-server/boot-server/src/main/resources/application.yml`（约 121-137、273-282 行）
- Modify: `mdp-apps/md-server/worker-server/src/main/resources/application.yml`（约 119-138、270-279 行）

**Interfaces:**
- Consumes: Task 2 的 `NextDoc4jProperties`（前缀 `mdp.nextdoc4j`）
- Produces: 两个 server 的 `mdp.nextdoc4j.*` 配置；springdoc 分组配置保持不变

- [ ] **Step 1: boot-server —— 配置前缀改名**

`mdp-apps/md-server/boot-server/src/main/resources/application.yml` 约 121 行，修改前：

```yaml
  swagger:
    license: Powered By henhen6
```

修改后（仅键名 `swagger` → `nextdoc4j`，其余内容不动）：

```yaml
  nextdoc4j:
    license: Powered By henhen6
```

注意：此处是 `mdp:` 节点下的子键，缩进为 2 空格，只改 `swagger:` 这一个键名。

- [ ] **Step 2: boot-server —— knife4j 块替换为 nextdoc4j 开关**

约 273-282 行，修改前：

```yaml
# knife4j 文档通用配置 详情看: Knife4jProperties
knife4j:
  enable: true
  setting:
    enableReloadCacheParameter: true
    enableVersion: true
    enableDynamicParameter: true
    enableFooter: false
    enableFooterCustom: true
    footerCustomContent: Apache License 2.0 | Copyright  2024 [mddata](https://mddata.top)
```

修改后：

```yaml
# NextDoc4j 在线文档总开关
nextdoc4j:
  enabled: true
```

其后的 `springdoc:` 块（含 workbench/console/open 三个 group-configs）保持不动。

- [ ] **Step 3: worker-server —— 同样两处修改**

`mdp-apps/md-server/worker-server/src/main/resources/application.yml`：

1. 约 119 行 `  swagger:`（`mdp:` 子键）→ `  nextdoc4j:`（内容与 boot-server 相同，含 defaultValue: "test" 注释段，全部保留）
2. 约 270-279 行的 `knife4j:` 块（内容与 boot-server 相同）替换为：

```yaml
# NextDoc4j 在线文档总开关
nextdoc4j:
  enabled: true
```

其后的 `springdoc:` 块（含 api 分组）保持不动。

- [ ] **Step 4: 编译验证**

Run:

```bash
cd /Users/tangyh/gitee/mdp/mdp-apps && mvn -pl md-server/boot-server,md-server/worker-server -am clean install -DskipTests
```

Expected: BUILD SUCCESS

---

### Task 5: inner-gateway-server 聚合改造

**Files:**
- Modify: `mdp-apps/md-gateway/inner-gateway-server/pom.xml`（约 119-122 行）
- Delete: `mdp-apps/md-gateway/inner-gateway-server/src/main/java/top/mddata/gateway/inner/config/OpenApi3Controller.java`
- Modify: `mdp-apps/md-gateway/inner-gateway-server/src/main/java/top/mddata/gateway/inner/GatewayServerApplication.java:42`

**Interfaces:**
- Consumes: Task 1 的 `nextdoc4j-gateway-spring-boot-starter` 托管版本、`springdoc.version=2.8.17`
- Produces: 网关聚合能力由 starter auto-discovery 提供（Task 6 的 Nacos 配置开启）；`GateController` 保留不动（`/gate/doc.html` 跳转目标仍然有效，类中还有其他接口）

- [ ] **Step 1: 替换 pom 依赖**

修改前（约 119-122 行）：

```xml
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-ui</artifactId>
        </dependency>
```

修改后：

```xml
        <dependency>
            <groupId>top.nextdoc4j</groupId>
            <artifactId>nextdoc4j-gateway-spring-boot-starter</artifactId>
        </dependency>
        <!-- NextDoc4j 要求宿主自行引入 springdoc UI（WebFlux 栈） -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
        </dependency>
```

- [ ] **Step 2: 删除自定义聚合 Controller**

```bash
rm mdp-apps/md-gateway/inner-gateway-server/src/main/java/top/mddata/gateway/inner/config/OpenApi3Controller.java
```

- [ ] **Step 3: 修改启动日志文案**

`GatewayServerApplication.java` 约 42 行，修改前：

```java
                knife4j 聚合文档: http://{}:{}{}/doc.html
```

修改后：

```java
                NextDoc4j 聚合文档: http://{}:{}{}/doc.html
```

- [ ] **Step 4: 编译验证**

Run:

```bash
cd /Users/tangyh/gitee/mdp/mdp-apps && mvn -pl md-gateway/inner-gateway-server -am clean install -DskipTests
```

Expected: BUILD SUCCESS

---

### Task 6: 产出 Nacos 配置修改文本

**Files:**
- Create: `docs/nacos-nextdoc4j/common.yml`
- Create: `docs/nacos-nextdoc4j/inner-gateway-server.yml`
- Create: `docs/nacos-nextdoc4j/README.md`

**Interfaces:**
- Consumes: `docs/nacos_config_export_20261005165133.zip`（Nacos 现有配置导出）
- Produces: 用户导入 Nacos 的 2 份完整配置 + 1 份导入说明。console-server.yml / workbench-server.yml / open-server.yml 无 knife4j 和 `mdp.swagger` 引用（springdoc 分组配置保留即可），无需变更

- [ ] **Step 1: 导出原始配置为文本**

```bash
cd /Users/tangyh/gitee/mdp
unzip -p docs/nacos_config_export_20261005165133.zip DEFAULT_GROUP/common.yml > docs/nacos-nextdoc4j/common.yml
unzip -p docs/nacos_config_export_20261005165133.zip DEFAULT_GROUP/inner-gateway-server.yml > docs/nacos-nextdoc4j/inner-gateway-server.yml
```

- [ ] **Step 2: 修改 common.yml**

对 `docs/nacos-nextdoc4j/common.yml` 做 3 处修改：

1. `mdp:` 节点下的 `  swagger:` 键（约 81 行）改名为 `  nextdoc4j:`，其下内容（license/licenseUrl/termsOfServiceUrl/contact/global-operation-parameters）不动
2. 删除整个 knife4j 块（约 167-176 行）：

```yaml
# knife4j 文档通用配置 详情看: Knife4jProperties
knife4j:
  enable: true
  setting:
    enableReloadCacheParameter: true
    enableVersion: true
    enableDynamicParameter: true
    enableFooter: false
    enableFooterCustom: true
    footerCustomContent: Apache License 2.0 | Copyright  2024 [mddata](https://mddata.top)
```

替换为：

```yaml
# NextDoc4j 在线文档总开关
nextdoc4j:
  enabled: true
```

3. `springdoc:` 块（default-flat-param-object、swagger-ui、api-docs）保持不动

- [ ] **Step 3: 修改 inner-gateway-server.yml**

在 `docs/nacos-nextdoc4j/inner-gateway-server.yml` 的 `server:` 节点之前（文件末尾）追加：

```yaml
# NextDoc4j 网关聚合文档配置
nextdoc4j:
  enabled: true
  gateway:
    auto-discovery: true
    # 读取各服务的 /v3/api-docs/swagger-config，聚合服务的全部分组
    service-swagger-config-enabled: true
    # 网关开启了 discovery.locator，必须按路由 id 限定，避免聚合 wsMsg、worker-server 等无关路由
    include-patterns:
      - workbench
      - console
      - open
    name-mappings:
      workbench: 工作台
      console: 控制台
      open: 开发者平台
```

注意：`server:` 节点（port: 23450、servlet.context-path: /api）在文件末尾，聚合配置应插入到 `server:` 节点**之前**，保持原有节点完整。

- [ ] **Step 4: 编写导入说明 README**

新建 `docs/nacos-nextdoc4j/README.md`：

```markdown
# NextDoc4j Nacos 配置导入说明

本目录为 knife4j → NextDoc4j 替换后的 Nacos 配置完整文本，基于
`docs/nacos_config_export_20261005165133.zip` 修改生成。

## 变更清单

### common.yml（所有微服务共享）
1. `mdp.swagger` → `mdp.nextdoc4j`（内容不变）
2. 删除 `knife4j:` 配置块，新增 `nextdoc4j.enabled: true`
3. springdoc 配置不变

### inner-gateway-server.yml（网关）
- 新增 `nextdoc4j.gateway` 聚合配置：auto-discovery、聚合各服务全部分组、
  仅包含 workbench/console/open 三个路由、服务中文名映射

### 无需变更
- console-server.yml / workbench-server.yml / open-server.yml：
  仅有 springdoc group-configs，无 knife4j 引用，保持现状

## 导入方式

在 Nacos 控制台分别编辑 `common.yml`、`inner-gateway-server.yml`
（DEFAULT_GROUP），用本目录对应文件内容整体替换后发布。

## 验证

1. 各业务服务单独访问：`http://<host>:<port>/doc.html`
   （console: 23451 / open: 23452 / workbench: 23453）
2. 网关聚合访问：`http://<host>:23450/api/doc.html`，
   顶部服务选择器应出现「工作台 / 控制台 / 开发者平台」
```

---

### Task 7: 残留扫描与全量验证

**Files:**
- 无新增/修改（纯验证）

- [ ] **Step 1: knife4j 残留扫描**

Run:

```bash
cd /Users/tangyh/gitee/mdp
grep -rn "knife4j\|xiaoymin" --include="*.java" --include="*.xml" --include="*.yml" mdp-base mdp-apps mdp-parent | grep -v target | grep -v flattened
```

Expected: 无输出（或仅剩 docs 目录历史文档，可接受）。注意 `.flattened-pom.xml` 是构建产物，重新构建后会自动更新，无需手工修改

- [ ] **Step 2: Swagger 旧类引用扫描**

Run:

```bash
cd /Users/tangyh/gitee/mdp
grep -rn "SwaggerProperties\|SwaggerAutoConfiguration\|SwaggerWebMvcConfigurer\|MyKnife4jOpenApiCustomizer\|mdp.swagger" --include="*.java" --include="*.yml" mdp-base mdp-apps | grep -v target
```

Expected: 无输出

- [ ] **Step 3: 确认 springdoc/swagger 版本生效**

Run:

```bash
cd /Users/tangyh/gitee/mdp/mdp-base && mvn -pl md-openapi3-starter dependency:tree | grep -E "springdoc|swagger|nextdoc4j"
```

Expected:
- `org.springdoc:springdoc-openapi-starter-webmvc-ui:jar:2.8.17`
- `top.nextdoc4j:nextdoc4j-spring-boot-starter:jar:1.4.1`
- `io.swagger.core.v3:swagger-core-jakarta:jar:2.2.47`
- 无 `com.github.xiaoymin` 任何条目

- [ ] **Step 4: 全量编译（含 checkstyle）**

Run:

```bash
cd /Users/tangyh/gitee/mdp/mdp-base && mvn clean install -DskipTests
cd /Users/tangyh/gitee/mdp/mdp-apps && mvn clean install -DskipTests
```

Expected: 两次均 BUILD SUCCESS

- [ ] **Step 5: 运行时验证（用户手动执行）**

以下步骤需要 Nacos/MySQL/Redis 环境，由用户本地验证：

1. 单体：启动 boot-server，访问 `http://localhost:23455/doc.html`，确认 3 个分组（工作台/控制台/开发者平台）可切换、接口可调试
2. 微服务：导入 Task 6 的 Nacos 配置并发布，依次启动 console/workbench/open-server，分别访问 `http://localhost:23451/doc.html`、`http://localhost:23453/doc.html`、`http://localhost:23452/doc.html`
3. 网关：启动 inner-gateway-server，访问 `http://localhost:23450/api/doc.html`，确认聚合出现 3 个服务且接口可调试
