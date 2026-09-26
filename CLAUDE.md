# MDP (Master Data Platform) - 主数据平台后端工程

## 项目简介

MDP (Master Data Platform) 是一个基于 Java 17、SpringBoot、Vue3 的中后台快速开发平台，包含三个主要应用：
- **工作台** (md-workbench): 用户统一门户
- **控制台** (md-console): 管理员后台管理
- **开发者平台** (md-open): 第三方开发者接入中心
- **接口服务** (md-api): 对外提供接口供第三方通过SDK调用

MDP 是一个Monorepo项目，包含三个主要模块，分别位于mdp-base、mdp-apps、mdp-sdk目录下。
- mdp-base: 核心工具库 和 starter，封装业务无关的功能或工具
- mdp-apps: 业务模块
- mdp-sdk: 提供给第三方的SDK，所有的接口均来自 @mdp-apps/md-api/api-service 模块

### 核心特性
1. **权限管理**: 应用权限、菜单权限、按钮权限、数据权限、字段权限
2. **单点登录**: 支持SSO、OAuth2等多种协议
3. **数据同步**: 支持钉钉等三方数据同步
4. **代码生成**: 自主开发的代码生成器
5. **监控集成**: Druid监控、日志记录、操作审计

## 核心架构

- @docs/architecture.md
- @docs/api-standards.md
- @docs/database-schema.md


## 数据库配置

### 配置文件
主配置文件位于 `mdp-apps/md-server/boot-server/src/main/resources/application.yml`

### 数据库要求
- MySQL 8.0+ 或 达梦数据库
- Redis (端口 16379)

## 开发规范

### 代码风格
- 使用 Lombok 减少样板代码
- 遵循 Spring Boot 最佳实践
- 集成 MyBatis-Flex 进行数据库操作
- 最大行长度：100 字符

### 命名规范
- **文件**：kebab-case（`user-controller.ta`）
- **类**：PascalCase（`UserService`）
- **函数 / 变量**：camelCase（`getUserById`）
- **常量**：UPPER_SNAKE_CASE（`API_BASE_URL`）
- **数据库表**：snake_case（`user_accounts`）

### Git 工作流
- 禁止任何提交和推送

### 测试要求
- 最低 80% 代码覆盖率
- 所有关键路径都必须有测试
- 单元测试使用 org.junit.jupiter.api.Test

## 已知问题与解决方案
- PostgreSQL 连接池在高峰期限制为 20
