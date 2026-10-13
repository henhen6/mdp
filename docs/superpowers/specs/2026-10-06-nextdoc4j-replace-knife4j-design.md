# NextDoc4j 替换 knife4j 设计文档

日期：2026-10-06
状态：已确认

## 1. 背景与目标

将项目中所有 knife4j 在线文档替换为 NextDoc4j（1.4.1）：

1. 单体版 boot-server：能单独访问在线文档
2. 微服务版 inner-gateway-server、open-server、console-server、workbench-server：各业务服务能单独访问文档，同时能通过 inner-gateway-server 聚合访问

## 2. 现状梳理

| 位置 | 内容 |
|---|---|
| `mdp-parent/pom.xml` | knife4j 4.5.0 BOM、springdoc 2.8.5、swagger-models/annotations 2.2.28 |
| `mdp-base/md-openapi3-starter` | 依赖 `knife4j-openapi3-jakarta-spring-boot-starter`；`SwaggerAutoConfiguration`（OpenAPI 信息 + 全局参数 + knife4j customizer）、`MyKnife4jOpenApiCustomizer`（knife4j issue#865 补丁）、`SwaggerWebMvcConfigurer`（doc.html 资源映射）。经 `md-all-boot` 传递到所有服务 |
| `mdp-base/md-annotation` | 声明 `knife4j-core` 依赖但源码未使用 |
| `boot-server`（单体） | 本地 application.yml：`mdp.swagger.*` 文档信息 + `knife4j:` 块 + springdoc 3 个分组（workbench/console/open） |
| `worker-server` | 本地 application.yml 同样有 `mdp.swagger.*` + `knife4j:` 块 |
| 微服务（console/workbench/open-server） | knife4j/springdoc 配置在 Nacos：common.yml 通用配置 + 各 server yml 的 group-configs |
| `inner-gateway-server` | 依赖 `knife4j-openapi3-ui` + 自定义 `OpenApi3Controller` 聚合各服务 `/v3/api-docs/swagger-config`（增强版，可聚合所有分组）+ `GateController` `/gate/doc.html` 跳转；context-path 为 `/api`；路由 workbench/console/open 均 `StripPrefix=1`，且开启 `discovery.locator.enabled: true` |

关键事实：

- 业务代码 **0 处** 使用 knife4j 注解（`@ApiSupport`、`@ApiOperationSupport`），替换只涉及 starter、pom、配置
- NextDoc4j 默认入口同为 `/doc.html`，现有 sa-token/XSS 白名单基本不用动
- NextDoc4j 需要宿主自行引入 springdoc UI 依赖（WebMvc/WebFlux 按栈二选一），推荐 springdoc 2.8.17

## 3. 方案决策

- Nacos 配置交付方式：**在 docs 下产出修改后的完整配置文本，用户自行导入 Nacos**
- starter 改造力度：**中度清理**——模块名 `md-openapi3-starter` 不变，类名 `Swagger*` → `NextDoc4j*`，配置前缀 `mdp.swagger` → `mdp.nextdoc4j`，包名 `top.mddata.base.openapi3` 不变
- springdoc 版本：2.8.5 → 2.8.17（NextDoc4j 官方推荐）
- 网关聚合：删除自定义 `OpenApi3Controller`，使用 `nextdoc4j-gateway-spring-boot-starter` 自动聚合

## 4. 详细设计

### 4.1 依赖管理（mdp-parent/pom.xml）

- 移除 `knife4j-dependencies` BOM 及属性 `knife4j.version`、`knife4j-swagger-models-v3.version`
- `springdoc.version`：2.8.5 → 2.8.17（swagger-annotations-jakarta / swagger-models-jakarta 版本改由 springdoc BOM 管理）
- 新增 `top.nextdoc4j:nextdoc4j-bom:1.4.1`（import scope）

### 4.2 md-openapi3-starter 改造

pom：
- 移除 `knife4j-openapi3-jakarta-spring-boot-starter`
- 新增 `top.nextdoc4j:nextdoc4j-spring-boot-starter`、`org.springdoc:springdoc-openapi-starter-webmvc-ui`

代码：

| 现状 | 改后 |
|---|---|
| `properties/SwaggerProperties`（前缀 `mdp.swagger`） | `properties/NextDoc4jProperties`（前缀 `mdp.nextdoc4j`），字段保持不变（title/description/version/license/contact/globalOperationParameters 等） |
| `SwaggerAutoConfiguration` | `NextDoc4jAutoConfiguration`：保留 `customOpenApi()`（OpenAPI 信息）、`customize2()`（全局参数）两个 Bean；删除 knife4j customizer Bean 和空的 `orderGlobalOpenApiCustomizer`；`@ConditionalOnProperty` 改为 `prefix = "nextdoc4j", name = "enabled", havingValue = "true", matchIfMissing = true` |
| `MyKnife4jOpenApiCustomizer` | 删除（knife4j 专属补丁，NextDoc4j 不需要） |
| `SwaggerWebMvcConfigurer` | 删除（NextDoc4j starter 自带 doc.html 资源映射） |
| `META-INF/spring/...AutoConfiguration.imports` | 更新为 `top.mddata.base.openapi3.NextDoc4jAutoConfiguration` |

### 4.3 md-annotation

- 移除 `knife4j-core` 依赖（源码未使用，纯清理）；`swagger-annotations-jakarta`、`swagger-models-jakarta` 保留

### 4.4 boot-server（单体）

`src/main/resources/application.yml`：

- `mdp.swagger.*` → `mdp.nextdoc4j.*`（字段内容不变）
- 删除 `knife4j:` 配置块，替换为：

```yaml
nextdoc4j:
  enabled: true
```

- springdoc 配置（含 3 个 group-configs）保留不动
- 访问地址：`http://localhost:23455/doc.html`

### 4.5 worker-server

`application.yml` 做与 boot-server 相同的清理（`mdp.swagger` → `mdp.nextdoc4j`、删 `knife4j:` 块、加 `nextdoc4j.enabled: true`）。

### 4.6 inner-gateway-server（聚合）

pom：
- 移除 `knife4j-openapi3-ui`
- 新增 `top.nextdoc4j:nextdoc4j-gateway-spring-boot-starter`、`org.springdoc:springdoc-openapi-starter-webflux-ui`

代码：
- 删除 `top.mddata.gateway.inner.config.OpenApi3Controller`（自定义聚合逻辑由 starter 的 auto-discovery 替代）
- `GateController` 保留（`/gate/doc.html` 跳转目标 `/doc.html` 仍然有效，类中还有其他接口）
- `GatewayServerApplication` 启动日志文案 "knife4j 聚合文档" → "NextDoc4j 聚合文档"

聚合访问地址：`http://<gateway-host>:23450/api/doc.html`

### 4.7 Nacos 配置变更（产出完整文本，用户自行导入）

输出位置：`docs/nacos-nextdoc4j/` 目录，按文件存放修改后完整内容。

**common.yml**：
- 删除 `knife4j:` 配置块
- 新增 `nextdoc4j.enabled: true`
- 如存在 `mdp.swagger` 段则改为 `mdp.nextdoc4j`
- springdoc 基础配置保留

**inner-gateway-server.yml**：新增

```yaml
nextdoc4j:
  enabled: true
  gateway:
    auto-discovery: true
    # 读取各服务的 /v3/api-docs/swagger-config，聚合所有分组（替代原自定义 Controller）
    service-swagger-config-enabled: true
    # 网关开启了 discovery.locator，必须限定路由，否则 worker-server 等无关服务也会被聚合
    include-patterns:
      - workbench
      - console
      - open
    name-mappings:
      workbench: 工作台
      console: 控制台
      open: 开发者平台
```

**console-server.yml / workbench-server.yml / open-server.yml**：
- springdoc group-configs 保留不动
- 如存在 `mdp.swagger` 引用则同步改为 `mdp.nextdoc4j`

### 4.8 白名单核对

NextDoc4j 官方要求放行：`/doc.html`、`/webjars/**`、`/v3/api-docs/**`、`/swagger-resources/**`、`/favicon.ico`。

逐一核对并补缺：

- `md-common-pojo` `IgnoreProperties`：已有 `/**/api-docs/**`、`/**/api-docs-ext/**`、`/**/webjars/**`，核对 `/doc.html`、`/favicon.ico`
- `md-common-config` `BasicConfigurer`：已有 `/doc.html**`、`/webjars/**`、`/v2/api-docs` 等，核对 `/v3/api-docs/**`
- `md-xss-starter` `XssProperties`：已有 `/**/doc.html`、`/**/swagger-ui.html`、`/webjars/**`

### 4.9 错误处理

- 依赖替换后若 springdoc 版本冲突，以 `mvn dependency:tree` 确认 2.8.17 生效
- 网关聚合若未出现某服务，先检查该服务 `/v3/api-docs` 是否可直连，再核对 `include-patterns` 与路由 id 匹配

## 5. 验证

1. 编译：`mvn clean install -DskipTests`（mdp-base → mdp-apps 相关模块），并运行 checkstyle
2. 运行时验证（用户本地启动确认）：
   - boot-server：`http://localhost:23455/doc.html` 可访问，3 个分组可切换
   - console/workbench/open-server：各自 `/doc.html` 可访问
   - 网关：`http://localhost:23450/api/doc.html` 聚合显示工作台/控制台/开发者平台 3 个服务

## 6. 明确不做

- 不重命名 `md-openapi3-starter` 模块和 `top.mddata.base.openapi3` 包
- 不删除 `GateController`
- 不修改 Nacos（只产出配置文本）
- 不做 git 提交
- 不更新 docs 下的 nacos_config_export zip 二进制文件
