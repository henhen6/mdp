# MDP (Master Data Platform) - 主数据平台后端 后端工程

## 项目概述

mdp-apps 是MDP的后端工程。项目采用模块化架构，通过启动不同的server层，项目支持单体架构和微服务架构，包含多个不同用途的应用程序：

- **md-workbench**: 用户工作台（统一门户）
- **md-console**: 管理后台（管理员后台）
- **md-open**: 开发者平台（第三方开发者接入中心）
- **md-api**: 外部 API 服务（对外提供接口）

## 构建和开发命令

### 构建项目

```bash
# 构建整个项目
mvn clean install

# 构建时跳过测试
mvn clean install -DskipTests
```

### 测试

```bash
# 运行所有测试
mvn test
```

### 代码质量

```bash
# 运行 checkstyle（在 md-build 中定义）
mvn checkstyle:check

# 生成依赖树
mvn dependency:tree
```

## 架构概述

### 模块结构

项目采用 Maven 多模块结构组织：

```
mdp-apps/
├── md-build/          # 构建配置和版本管理
├── md-public/         # 共享模块（common-dao、common-pojo 等）
├── md-workbench/      # 用户工作台应用
├── md-console/        # 管理控制台应用
├── md-open/           # 开发者平台
├── md-api/            # 外部 API 服务
├── md-gateway/        # 网关服务
└── md-server/         # 主应用服务器
    ├── boot-server/   # 单体应用启动器
    └── worker-server/ # 后台任务处理器
```

### 分层架构

每个服务模块遵循标准分层结构：

```
md-xxx/
├── xxx-pojo/          # 实体类（Entity、VO、DTO、Query）
├── xxx-dao/           # 数据访问层（Mapper、XML）
├── xxx-service/       # 业务逻辑层（Service 接口与实现）
├── xxx-controller/    # 控制器层
├── xxx-facade/        # 远程调用接口（Feign）
│   ├── xxx-api/       # 接口定义
│   └── xxx-boot-impl/ # 单体实现
│   └── xxx-cloud-impl/ # 微服务实现
└── xxx-server/        # 启动入口
```

注意：

- Entity、VO、DTO、Query 均根据表名截取掉前缀后生成，如`ua_user`表，则生成 `User`、`UserDto`、`UserVo`。
- VO、DTO、Query等模型，请勿使用全大写结尾，可使用驼峰命名，如 `UserDto`、`UserVo`。
- Entity请勿添加后缀，可使用驼峰命名，如 `User`。
- Entity 会生成两个类，自动生成的 getter、setter 字段等都在 Base 类里，而开发者可以在 Entity 中添加自己的业务代码。
  如： User、UserBase

### md-public 模块划分

md-public 是跟业务相关的公共模块。

- `md-common-dao`: 公共数据访问层
- `md-common-pojo`: 公共数据实体类
- `md-common-config`: 公共配置
- `md-enumeration-scanning`: 枚举扫描
- `md-cache-key`: 缓存键管理

### 响应格式

统一使用 `top.mddata.base.base.R<T>` 包装响应：

```java
R.success(data)          // 成功
R.

fail("错误消息")        // 失败
```

### 实体基类

- `BaseEntity<T>` - 包含 id、createdAt、createdBy
- `SuperEntity<T>` - 继承 BaseEntity，增加 updatedAt、updatedBy
- `TreeEntity<T>` - 树形结构实体

### Mapper 继承

所有 Mapper 继承 `top.mddata.base.mvcflex.mapper.SuperMapper<T>`，且**必须加 `@Repository` 注解**：

```java

@Repository
public interface XxxMapper extends SuperMapper<Xxx> {
}
```

> Mapper 的注册机制是 Spring 组件扫描 + `@Repository`（不是 `@MapperScan`）。
> 漏加时编译正常，但启动失败：`Parameter N of constructor in XxxServiceImpl
> required a bean of type 'XxxMapper' that could not be found`。

### Service 继承

```java
public interface XxxService extends SuperService<Xxx> {
}

public class XxxServiceImpl extends SuperServiceImpl<XxxMapper, Xxx> implements XxxService {
}
```

### Controller 继承

```java

@RestController
public class XxxController extends SuperController<XxxService, Xxx> {
}
```

### 关键技术

- **核心框架**: Spring Boot 3.x with Java 17
- **数据库**: MyBatis-Flex with MySQL 8.0+ / DM database
- **身份认证**: Sa-Token with SSO support
- **缓存**: Redis with Sa-Token integration
- **文档**: Knife4j (enhanced Swagger)
- **文件存储**: X-File-Storage (supports local, MinIO, Aliyun OSS)

### 单体版架构

系统采用单体架构时，包含1个入口点：

1. **BootServerApplication** (`md-server/boot-server`): 服务所有模块的主应用程序

### 微服务版架构

系统采用微服务架构时，包含多个入口点：

1. **ApiServerApplication** (`md-api/api-server`): 用于第三方 SDK 集成的外部 API 服务
2. **ConsoleServerApplication** (`md-server/console-server`): 后台管理控制台
3. **OpenServerApplication** (`md-open/open-server`): 开发者平台
4. **WorkerServerApplication** (`md-server/worker-server`): 后台任务处理器
5. **GatewayServerApplication** (`md-gateway/gateway-server`): 网关服务

### 通用模式

- **SSO 配置**: 后端同时作为三个前端应用程序的 SSO 服务器和客户端
- **数据权限**: 通过注解实现细粒度的数据权限过滤
- **审计日志**: 包含 IP 地理定位的全面操作日志
- **文件上传**: 使用 @XFile 注解的统一文件存储

## 配置

### 基于配置文件的配置

项目使用 Maven 配置文件针对不同环境：

- `dev`（默认）：本地开发，使用文件存储
- `test`：测试环境，使用 MinIO 存储
- `prod`：生产环境，使用 MinIO 存储

### 关键配置文件

- `application.yml`: 带配置文件的主配置
- 数据库和 Redis 配置在 `application.yml` 中
- Sa-Token SSO 多客户端配置
- 不同存储平台的 X-File-Storage 配置

### 开发环境设置

默认测试账户：

- ops_admin/admin（运维管理员）
- open_admin/admin（开发者管理员）
- admin/admin（企业管理管理员）

## 通用开发模式

### 控制器层

- 使用 `@Echo` 进行自动响应包装
-

### 业务层

- 使用 `@DataPermission` 实现数据权限过滤

### 数据库操作

- 使用 MyBatis-Flex 和 `BaseMapper<T>` 进行 CRUD 操作
- 逻辑删除配置为 `deleted_at` 时间戳

## 测试

### 测试结构

- 使用 Spring Boot 测试切片注解（`@SpringBootTest`）
- 必要时使用 Mockito 模拟外部依赖

### 测试配置文件

- 使用 `test` 配置文件进行具有外部依赖的集成测试
- 在 `src/test/resources` 中配置特定于测试的属性

## 重要说明

### 性能

- 使用 MyBatis-Flex 批量操作进行批量插入
- 为经常访问的数据实现 Redis 缓存
- 使用异步线程池进行后台操作

### 数据库

- 尽可能写标准SQL，通知支持 MySQL、Oracle、DM（达梦数据库）
- 使用 Druid 进行连接池
- SQL 监控在 `/druid/*` 可用
- 使用时间戳列进行逻辑删除