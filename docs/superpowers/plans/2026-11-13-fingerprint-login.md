# 指纹登录功能实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 MDP 工作台实现 WebAuthn 指纹绑定/解绑/免密登录，前端改造 mdp-vben 安全设置页与登录页。

**Architecture:** 后端移植 uni-auth 已验证的 Yubico webauthn-server-core 模式到 md-workbench（resident key 模式，userHandle = userId 字符串 UTF-8 字节），登录签发复用 `LoginSessionService.loginAuthenticated`；前端用 `@simplewebauthn/browser`，token 存储复用 `authStore.completeLogin`。

**Tech Stack:** Java 17 / SpringBoot 3.5 / MyBatis-Flex / com.yubico:webauthn-server-core:2.8.0 / Vue3 + TS + antdv-next + @simplewebauthn/browser ^13

**参考实现（只读，禁止修改）：**
- 后端蓝本：`~/workspace/pj/uni-auth/ua-server/ua-center/`（FingerprintServiceImpl、FingerprintCredentialRepositoryImpl、FingerprintWebAuthnConfig、FingerprintProperties、FingerprintUserHandles、FingerprintCredentialBase、Controller ×2、DTO/VO）
- 测试蓝本：`~/workspace/pj/uni-auth/ua-server/ua-center/center-service/src/test/java/cc/uniauth/center/service/impl/FingerprintServiceImplTest.java`

**设计文档：** `docs/superpowers/specs/2026-11-13-fingerprint-login-design.md`

## Global Constraints

- **禁止任何 git add/commit/push**（用户全局规则）。所有任务不含 commit 步骤。
- 后端注释、错误消息、日志全部简体中文；代码标识符英文。
- 全部错误处理 fail fast，抛 `BizException`（`top.mddata.base.exception.BizException`），错误消息具体。
- 表名 `mdw_fingerprint_credential`；类名 `FingerprintCredential`；包 `top.mddata.workbench.*`。
- 缓存 Key 风格：无静态工厂，`new XxxCacheKeyBuilder().key(id)`（参照 `SocialStateCacheKeyBuilder`）；TTL 均 5 分钟。
- Mapper 必须加 `@Repository`（靠组件扫描注册）。
- 实体拆分：`entity/base/FingerprintCredentialBase.java` 只放表字段；`entity/FingerprintCredential.java` 加 `@Table(TABLE_NAME)`。
- 免登录 Controller 路径 `/anyUser/fingerprint/**`（自动放行，无需改配置）；登录态 Controller 路径 `/profile/fingerprint/**`。
- 请求方式遵循 `docs/api-standards.md`：新增/提交/修改/删除一律 POST；简单查询 GET。
- Controller 注解组合：`@RestController + @RequestMapping + @AllArgsConstructor + @Tag`，方法 `@Operation + @XxxMapping (+ @RequestLog)`，返回 `R<T>`。
- DTO/VO：`@Data + @Schema`，校验用 `jakarta.validation.constraints.*` 且必带中文 message。
- 单元测试：纯 JUnit 5 + Mockito，无 @SpringBootTest，构造注入，测试方法名用中文（参照 `SocialLoginServiceImplTest`）。
- 测试命令（在 `/Users/tangyh/gitee/mdp` 下）：
  `mvn test -pl mdp-apps/md-workbench/workbench-service -am -Dtest=<ClassName> -DfailIfNoTests=false`
- 前端仓库 `/Users/tangyh/gitee/mdp-vben`，UI 库 `antdv-next`，请求统一 `requestClient`（`#/api/request`），登录成功统一 `authStore.completeLogin(LoginVO, onSuccess)`。
- 前端注释/文案简体中文，走 i18n（zh-CN 与 en-US 两个 locale 文件都要改）。
- 前端验证命令（在 `/Users/tangyh/gitee/mdp-vben` 下）：`pnpm install`（新增依赖后）、`pnpm check:type`（若无此脚本用 `pnpm -F @vben/web-workbench run typecheck`，实际以 package.json scripts 为准）、`pnpm test --run apps/web-workbench`（vitest）。

---

### Task 1: 依赖管理与基础配置（mdp 后端）

**Files:**
- Modify: `mdp-parent/pom.xml`（properties 区 + dependencyManagement L165 起）
- Modify: `mdp-apps/md-workbench/workbench-service/pom.xml`
- Modify: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/enumeration/AuthTypeEnum.java:68`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/config/FingerprintProperties.java`

**Interfaces:**
- Produces: `FingerprintProperties`（`getEnabled()/getRpId()/getRpName()/getOrigins()/getMaxCredentialsPerUser()`），`AuthTypeEnum.FINGERPRINT`（code "12"，desc "指纹登录"），yubico 依赖对所有 app 模块可用（无版本号引用）。

- [ ] **Step 1: mdp-parent 声明 webauthn-server-core 版本**

在 `mdp-parent/pom.xml` 的 `<properties>` 区加（与其他版本 property 并列）：

```xml
<webauthn-server-core.version>2.8.0</webauthn-server-core.version>
```

在 `<dependencyManagement><dependencies>` 中（按现有条目风格，groupId/artifactId/版本引用 property）加：

```xml
<dependency>
    <groupId>com.yubico</groupId>
    <artifactId>webauthn-server-core</artifactId>
    <version>${webauthn-server-core.version}</version>
</dependency>
```

- [ ] **Step 2: workbench-service pom 引用依赖**

在 `mdp-apps/md-workbench/workbench-service/pom.xml` 的 `<dependencies>` 中（无版本号，与 `JustAuth` 条目并列）：

```xml
<dependency>
    <groupId>com.yubico</groupId>
    <artifactId>webauthn-server-core</artifactId>
</dependency>
```

- [ ] **Step 3: AuthTypeEnum 新增枚举值**

在 `AuthTypeEnum.java` 的 `GITEE("11", "Gitee登录");` 前插入：

```java
/**
 * 指纹免密登录。仅用于登录日志与登录编排，不注册账号密码策略
 */
FINGERPRINT("12", "指纹登录");
```

注意：把原来 `GITEE("11", "Gitee登录");` 行尾的分号改为逗号。

- [ ] **Step 4: 创建 FingerprintProperties**

```java
package top.mddata.workbench.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 指纹登录配置
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Data
@Component
@ConfigurationProperties(prefix = "mdp.fingerprint")
public class FingerprintProperties {
    /** 是否启用指纹登录 */
    private Boolean enabled = false;
    /** RP ID：必须等于登录页部署域名（或其父域），启用时必填 */
    private String rpId;
    /** RP 显示名称 */
    private String rpName = "MDP主数据平台";
    /** 允许的前端 origin，严格匹配 */
    private Set<String> origins = new HashSet<>();
    /** 单用户最大凭证数 */
    private Integer maxCredentialsPerUser = 10;
}
```

- [ ] **Step 5: 编译验证**

Run: `cd /Users/tangyh/gitee/mdp && mvn compile -pl mdp-apps/md-workbench/workbench-service -am -q`
Expected: BUILD SUCCESS（yubico 依赖可解析，枚举编译通过）

---

### Task 2: 数据层与建表 SQL（mdp 后端）

**Files:**
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/entity/base/FingerprintCredentialBase.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/entity/FingerprintCredential.java`
- Create: `mdp-apps/md-workbench/workbench-dao/src/main/java/top/mddata/workbench/mapper/FingerprintCredentialMapper.java`
- Create: `docs/sql/20261113-security-fingerprint-table.sql`

**Interfaces:**
- Produces: 实体 `FingerprintCredential`（字段见下）、`FingerprintCredentialMapper extends SuperMapper<FingerprintCredential>`、`FingerprintCredential.TABLE_NAME`。后续 Service/Repository 依赖此实体。

- [ ] **Step 1: 创建 FingerprintCredentialBase（表字段契约）**

风格参照 `mdp-apps/md-public/md-common-pojo/.../entity/base/UserSocialAuthBase.java`（`@Data @Builder @Accessors(chain=true) @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(callSuper=true)`，继承 `SuperEntity<Long>`，含 `TABLE_NAME` 常量与 `@Serial serialVersionUID`）：

```java
package top.mddata.workbench.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 指纹登录凭证实体类。
 *
 * @author tangyh
 * @since 2026-11-13
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FingerprintCredentialBase extends SuperEntity<Long> implements Serializable {
    /**
     * 表名称
     */
    public static final String TABLE_NAME = "mdw_fingerprint_credential";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * WebAuthn凭证ID(base64url)，全局唯一
     */
    private String credentialId;

    /**
     * COSE公钥(base64)
     */
    private String publicKey;

    /**
     * 签名计数器(防克隆)
     */
    private Long signCount;

    /**
     * 认证器AAGUID
     */
    private String aaguid;

    /**
     * 前端localStorage设备ID，仅辅助展示与统计
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 平台 win32/darwin/linux
     */
    private String platform;

    /**
     * 登记时User-Agent
     */
    private String userAgent;

    /**
     * 登记时间
     */
    private LocalDateTime registeredAt;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedAt;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 删除用户
     */
    private Long deletedBy;

    /**
     * 删除标识
     */
    private Long deletedAt;
}
```

- [ ] **Step 2: 创建 FingerprintCredential**

```java
package top.mddata.workbench.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.workbench.entity.base.FingerprintCredentialBase;

/**
 * 指纹登录凭证
 *
 * @author tangyh
 * @since 2026-11-13
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(FingerprintCredentialBase.TABLE_NAME)
public class FingerprintCredential extends FingerprintCredentialBase {
}
```

- [ ] **Step 3: 创建 Mapper**

```java
package top.mddata.workbench.mapper;

import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.workbench.entity.FingerprintCredential;

/**
 * 指纹登录凭证 Mapper
 *
 * @author tangyh
 * @since 2026-11-13
 */
@Repository
public interface FingerprintCredentialMapper extends SuperMapper<FingerprintCredential> {
}
```

- [ ] **Step 4: 建表 SQL**

写入 `docs/sql/20261113-security-fingerprint-table.sql`（内容与设计文档第 3 节完全一致，外层加 `START TRANSACTION; ... COMMIT;`，字段注释中文）：

```sql
-- 指纹登录凭证表
START TRANSACTION;

CREATE TABLE IF NOT EXISTS `mdw_fingerprint_credential` (
  `id`            bigint       NOT NULL COMMENT '主键（雪花ID）',
  `user_id`       bigint       NOT NULL COMMENT '用户ID',
  `credential_id` varchar(512) NOT NULL COMMENT 'WebAuthn凭证ID(base64url)',
  `public_key`    text         NOT NULL COMMENT 'COSE公钥(base64)',
  `sign_count`    bigint       NOT NULL DEFAULT '0' COMMENT '签名计数器(防克隆)',
  `aaguid`        varchar(36)  DEFAULT NULL COMMENT '认证器AAGUID',
  `device_id`     varchar(64)  DEFAULT NULL COMMENT '前端localStorage设备ID',
  `device_name`   varchar(64)  DEFAULT NULL COMMENT '设备名称',
  `platform`      varchar(32)  DEFAULT NULL COMMENT '平台 win32/darwin/linux',
  `user_agent`    varchar(500) DEFAULT NULL COMMENT '登记时User-Agent',
  `registered_at` datetime     DEFAULT NULL COMMENT '登记时间',
  `last_used_at`  datetime     DEFAULT NULL COMMENT '最后使用时间',
  `last_login_ip` varchar(64)  DEFAULT NULL COMMENT '最后登录IP',
  `created_at`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by`    bigint       DEFAULT NULL COMMENT '创建人',
  `updated_at`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by`    bigint       DEFAULT NULL COMMENT '更新人',
  `deleted_at`    bigint       NOT NULL DEFAULT '0' COMMENT '删除标识',
  `deleted_by`    bigint       DEFAULT NULL COMMENT '删除用户',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_credential_id` (`credential_id`, `deleted_at`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指纹登录凭证';

COMMIT;
```

- [ ] **Step 5: 编译验证**

Run: `cd /Users/tangyh/gitee/mdp && mvn compile -pl mdp-apps/md-workbench/workbench-service -am -q`
Expected: BUILD SUCCESS

---

### Task 3: 缓存 KeyBuilder（mdp 后端）

**Files:**
- Modify: `mdp-apps/md-public/md-cache-key/src/main/java/top/mddata/common/cache/CacheKeyTable.java:66`（Workbench 接口）
- Create: `mdp-apps/md-public/md-cache-key/src/main/java/top/mddata/common/cache/workbench/FingerprintChallengeCacheKeyBuilder.java`
- Create: `mdp-apps/md-public/md-cache-key/src/main/java/top/mddata/common/cache/workbench/FingerprintPwdVerifiedCacheKeyBuilder.java`
- Test: `mdp-apps/md-public/md-cache-key/src/test/java/top/mddata/common/cache/workbench/FingerprintCacheKeyBuilderTest.java`

**Interfaces:**
- Produces: `new FingerprintChallengeCacheKeyBuilder().key(challengeId)` → `CacheKey`（expire 5 分钟）；`new FingerprintPwdVerifiedCacheKeyBuilder().key(String.valueOf(userId))` → `CacheKey`（expire 5 分钟）。

- [ ] **Step 1: CacheKeyTable.Workbench 加常量**

在 `CacheKeyTable` 的 `interface Workbench` 内（`SOCIAL_REAUTH` 行后）加：

```java
String FINGERPRINT_CHALLENGE = "fingerprint.challenge";
String FINGERPRINT_PWD_VERIFIED = "fingerprint.pwdVerified";
```

- [ ] **Step 2: 写失败测试**

```java
package top.mddata.common.cache.workbench;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 指纹登录缓存 Key 测试
 *
 * @author tangyh
 * @since 2026/11/13
 */
class FingerprintCacheKeyBuilderTest {

    @Test
    @DisplayName("挑战Key包含挑战ID且5分钟过期")
    void 挑战Key包含挑战ID且5分钟过期() {
        var cacheKey = new FingerprintChallengeCacheKeyBuilder().key("abc123");
        assertNotNull(cacheKey);
        assertEquals(Duration.ofMinutes(5), cacheKey.getExpire());
        org.assertj.core.api.Assertions.assertThat(cacheKey.getKey()).contains("fingerprint.challenge").contains("abc123");
    }

    @Test
    @DisplayName("密码票据Key包含userId且5分钟过期")
    void 密码票据Key包含userId且5分钟过期() {
        var cacheKey = new FingerprintPwdVerifiedCacheKeyBuilder().key("12345");
        assertNotNull(cacheKey);
        assertEquals(Duration.ofMinutes(5), cacheKey.getExpire());
        org.assertj.core.api.Assertions.assertThat(cacheKey.getKey()).contains("fingerprint.pwdVerified").contains("12345");
    }
}
```

注意：若 md-cache-key 测试依赖中无 assertj，则把断言改为 `assertTrue(cacheKey.getKey().contains("fingerprint.challenge"))`（`org.junit.jupiter.api.Assertions.assertTrue`）。先查 `md-cache-key/pom.xml` 的 test 依赖再定。

- [ ] **Step 3: 运行确认失败**

Run: `cd /Users/tangyh/gitee/mdp && mvn test -pl mdp-apps/md-public/md-cache-key -am -Dtest=FingerprintCacheKeyBuilderTest -DfailIfNoTests=false`
Expected: 编译失败（类不存在）

- [ ] **Step 4: 实现两个 KeyBuilder**

参照同目录 `SocialStateCacheKeyBuilder.java`（实现 `CacheKeyBuilder`，仅覆盖 `getTable()` 与 `getExpire()`）：

```java
package top.mddata.common.cache.workbench;

import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 指纹登录挑战 KEY（一次性消费，5 分钟过期）
 *
 * @author tangyh
 * @date 2026-11-13
 */
public class FingerprintChallengeCacheKeyBuilder implements CacheKeyBuilder {

    @Override
    public String getTable() {
        return CacheKeyTable.Workbench.FINGERPRINT_CHALLENGE;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofMinutes(5);
    }
}
```

```java
package top.mddata.common.cache.workbench;

import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 指纹录入前密码验证票据 KEY（一次性消费，5 分钟过期）
 *
 * @author tangyh
 * @date 2026-11-13
 */
public class FingerprintPwdVerifiedCacheKeyBuilder implements CacheKeyBuilder {

    @Override
    public String getTable() {
        return CacheKeyTable.Workbench.FINGERPRINT_PWD_VERIFIED;
    }

    @Override
    public Duration getExpire() {
        return Duration.ofMinutes(5);
    }
}
```

- [ ] **Step 5: 运行确认通过**

Run: 同 Step 3
Expected: PASS（2 个测试）

---

### Task 4: UserHandles、CredentialRepository 适配器与 RelyingParty 装配（mdp 后端）

**Files:**
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/util/FingerprintUserHandles.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/impl/FingerprintCredentialRepositoryImpl.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/config/FingerprintWebAuthnConfig.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/util/FingerprintUserHandlesTest.java`

**Interfaces:**
- Consumes: `FingerprintCredentialMapper`（Task 2）、`SsoUserService.getById(Long)/getByUsername(String)` 返回 `top.mddata.common.entity.User`。
- Produces: Spring bean `RelyingParty`（供 Task 5-7 的 Service 注入）；`FingerprintUserHandles.userHandleOf(Long): ByteArray` / `userIdOf(ByteArray): Long`；`FingerprintCredentialRepositoryImpl.getByCredentialId(String): FingerprintCredential` / `listByUserId(Long): List<FingerprintCredential>`（供 Service 复用）。

- [ ] **Step 1: 写 FingerprintUserHandles 失败测试**

```java
package top.mddata.workbench.util;

import com.yubico.webauthn.data.ByteArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * userHandle 编解码测试
 *
 * @author tangyh
 * @since 2026/11/13
 */
class FingerprintUserHandlesTest {

    @Test
    @DisplayName("userId编码后可无损解码")
    void userId编码后可无损解码() {
        Long userId = 1234567890123456789L;
        ByteArray handle = FingerprintUserHandles.userHandleOf(userId);
        assertEquals(userId, FingerprintUserHandles.userIdOf(handle));
    }

    @Test
    @DisplayName("不同userId生成不同userHandle")
    void 不同userId生成不同userHandle() {
        var h1 = FingerprintUserHandles.userHandleOf(1L);
        var h2 = FingerprintUserHandles.userHandleOf(2L);
        assertEquals(false, h1.equals(h2));
    }
}
```

- [ ] **Step 2: 运行确认失败**

Run: `cd /Users/tangyh/gitee/mdp && mvn test -pl mdp-apps/md-workbench/workbench-service -am -Dtest=FingerprintUserHandlesTest -DfailIfNoTests=false`
Expected: 编译失败（类不存在）

- [ ] **Step 3: 实现 FingerprintUserHandles**

从 uni-auth `center-service/.../util/FingerprintUserHandles.java` 原样移植，仅改包名为 `top.mddata.workbench.util`（逻辑逐字相同：`userHandleOf` = userId 字符串 UTF-8 字节，`userIdOf` 反向解析，私有构造器）。

- [ ] **Step 4: 运行确认通过**

Run: 同 Step 2
Expected: PASS

- [ ] **Step 5: 实现 FingerprintCredentialRepositoryImpl**

从 uni-auth `center-service/.../impl/FingerprintCredentialRepositoryImpl.java` 原样移植，按以下差异调整：
- 包名：`top.mddata.workbench.service.impl`
- 实体/Mapper import：`top.mddata.workbench.entity.FingerprintCredential`、`top.mddata.workbench.mapper.FingerprintCredentialMapper`
- 用户服务：`cc.uniauth.center.service.SsoUserService` → `top.mddata.workbench.service.SsoUserService`；`SysUser` → `top.mddata.common.entity.User`
- 保留 `@Repository`、`@RequiredArgsConstructor`、`getByCredentialId`/`listByUserId` 两个公共方法与 Yubico `CredentialRepository` 的全部 5 个接口方法实现（逻辑逐字相同）

- [ ] **Step 6: 实现 FingerprintWebAuthnConfig**

从 uni-auth `center-service/.../config/FingerprintWebAuthnConfig.java` 原样移植，按以下差异调整：
- 包名：`top.mddata.workbench.config`
- `FingerprintProperties` import 为 `top.mddata.workbench.config.FingerprintProperties`（本仓库 Task 1 已建）
- 占位 origin 改为本仓库前端开发端口：`http://localhost:5173`
- 错误消息中的配置前缀改为 `mdp.fingerprint.rp-id` / `mdp.fingerprint.origins`
- 其余逻辑逐字相同（enabled=true 时启动期校验 rpId/origins 非空；未启用时占位值保证 bean 可创建）

- [ ] **Step 7: 编译验证**

Run: `cd /Users/tangyh/gitee/mdp && mvn compile -pl mdp-apps/md-workbench/workbench-service -am -q`
Expected: BUILD SUCCESS

---

### Task 5: Service 绑定侧（checkPassword / registrationOptions / verifyRegistration）（mdp 后端，TDD）

**Files:**
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/dto/FingerprintRegisterVerifyDto.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/vo/FingerprintRegisterOptionsVo.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/vo/FingerprintCredentialVo.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/FingerprintService.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/impl/FingerprintServiceImpl.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/impl/FingerprintServiceImplTest.java`

**Interfaces:**
- Consumes: Task 2 实体/Mapper、Task 3 KeyBuilder、Task 4 `RelyingParty` + `FingerprintUserHandles` + `FingerprintCredentialRepositoryImpl`、`Map<String, LoginStrategy> loginStrategy`（Spring Map 注入，`get("USERNAME")` 为 `UsernameLoginStrategyImpl`）、`SsoUserService`、`ContextUtil.getUserId()`（`top.mddata.base.util.ContextUtil`）、`CacheOps`（`top.mddata.base.cache.repository.CacheOps`）。
- Produces（Task 6/7/8 依赖）:
  - `FingerprintService` 接口（本任务先声明全部 8 个方法，6/7 只补实现）
  - `FingerprintRegisterOptionsVo{challengeId, optionsJson}`、`FingerprintCredentialVo{id, deviceName, platform, aaguid, registeredAt, lastUsedAt, lastLoginIp}`、`FingerprintRegisterVerifyDto{challengeId, responseJson, deviceId, deviceName, platform}`

- [ ] **Step 1: 创建 DTO/VO 与 Service 接口**

`FingerprintRegisterVerifyDto`（风格参照 `SocialCallbackDto`）：

```java
package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 指纹录入验证入参
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Data
@Schema(title = "FingerprintRegisterVerifyDto", description = "指纹录入验证入参")
public class FingerprintRegisterVerifyDto {

    @Schema(description = "录入挑战ID")
    @NotBlank(message = "挑战ID不能为空")
    private String challengeId;

    @Schema(description = "WebAuthn注册响应JSON")
    @NotBlank(message = "注册响应不能为空")
    private String responseJson;

    @Schema(description = "前端localStorage设备ID")
    private String deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "平台 win32/darwin/linux")
    private String platform;
}
```

`FingerprintRegisterOptionsVo`：

```java
package top.mddata.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 指纹录入挑战出参
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Data
@Schema(title = "FingerprintRegisterOptionsVo", description = "指纹录入挑战出参")
public class FingerprintRegisterOptionsVo {

    @Schema(description = "挑战ID，verify 时原样回传")
    private String challengeId;

    @Schema(description = "WebAuthn PublicKeyCredentialCreationOptions JSON")
    private String optionsJson;
}
```

`FingerprintCredentialVo`：

```java
package top.mddata.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 指纹凭证出参
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Data
@Schema(title = "FingerprintCredentialVo", description = "指纹凭证出参")
public class FingerprintCredentialVo {

    @Schema(description = "凭证ID")
    private Long id;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "平台")
    private String platform;

    @Schema(description = "认证器AAGUID")
    private String aaguid;

    @Schema(description = "登记时间")
    private LocalDateTime registeredAt;

    @Schema(description = "最后使用时间")
    private LocalDateTime lastUsedAt;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;
}
```

`FingerprintService` 接口（全部 8 个方法一次声明，6/7 任务只补实现；`FingerprintLoginVerifyDto`/`FingerprintRenameDto`/`FingerprintLoginOptionsVo` 在 Task 6/7 创建前，本文件先用注释标注"Task 6/7 补充 import"，**直接在 Task 5 就把 Task 6/7 的三个 pojo 一并创建**——见 Step 6，避免编译不过）：

```java
package top.mddata.workbench.service;

import top.mddata.base.base.R;
import top.mddata.workbench.dto.FingerprintRegisterVerifyDto;
import top.mddata.workbench.dto.FingerprintRenameDto;
import top.mddata.workbench.dto.FingerprintLoginVerifyDto;
import top.mddata.workbench.vo.FingerprintCredentialVo;
import top.mddata.workbench.vo.FingerprintLoginOptionsVo;
import top.mddata.workbench.vo.FingerprintRegisterOptionsVo;
import top.mddata.workbench.vo.LoginVo;

import java.util.List;

/**
 * 指纹登录服务
 *
 * @author tangyh
 * @since 2026/11/13
 */
public interface FingerprintService {

    /**
     * 录入前验证当前登录密码，通过后发一次性票据（5分钟）
     */
    R<Boolean> checkPassword(String password);

    /**
     * 获取录入挑战（消费一次性票据）
     */
    FingerprintRegisterOptionsVo registrationOptions();

    /**
     * 校验录入结果并保存凭证
     */
    FingerprintCredentialVo verifyRegistration(FingerprintRegisterVerifyDto dto);

    /**
     * 我的指纹凭证列表
     */
    List<FingerprintCredentialVo> listMine();

    /**
     * 修改设备名称
     */
    Boolean rename(Long id, FingerprintRenameDto dto);

    /**
     * 删除指纹凭证（逻辑删除）
     */
    Boolean delete(Long id);

    /**
     * 获取指纹登录挑战（免登录）
     */
    FingerprintLoginOptionsVo loginOptions();

    /**
     * 指纹登录验证（免登录，校验通过后签发 token）
     */
    R<LoginVo> loginVerify(FingerprintLoginVerifyDto dto);
}
```

- [ ] **Step 2: 写失败测试（绑定侧三个方法）**

新建 `FingerprintServiceImplTest.java`（风格参照 uni-auth 测试 + `SocialLoginServiceImplTest`：纯 Mockito、构造注入、中文方法名）。核心 mock 组装：

```java
package top.mddata.workbench.service.impl;

import com.yubico.webauthn.RelyingParty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.BizException;
import top.mddata.workbench.config.FingerprintProperties;
import top.mddata.workbench.handler.LoginStrategy;
import top.mddata.workbench.mapper.FingerprintCredentialMapper;
import top.mddata.workbench.service.LoginSessionService;
import top.mddata.workbench.service.SsoUserService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 指纹登录服务测试
 *
 * @author tangyh
 * @since 2026/11/13
 */
class FingerprintServiceImplTest {

    private FingerprintCredentialMapper mapper;
    private CacheOps cacheOps;
    private RelyingParty relyingParty;
    private FingerprintProperties properties;
    private SsoUserService ssoUserService;
    private LoginSessionService loginSessionService;
    private Map<String, LoginStrategy> loginStrategy;
    private FingerprintServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = mock(FingerprintCredentialMapper.class);
        cacheOps = mock(CacheOps.class);
        relyingParty = mock(RelyingParty.class);
        properties = new FingerprintProperties();
        ssoUserService = mock(SsoUserService.class);
        loginSessionService = mock(LoginSessionService.class);
        loginStrategy = Map.of();
        service = new FingerprintServiceImpl(relyingParty, properties, ssoUserService,
                loginSessionService, loginStrategy, mapper, cacheOps);
    }

    // 以下测试方法与断言在后续 Step 中逐个补充
}
```

注意构造签名：`FingerprintServiceImpl` 同时继承 `SuperServiceImpl<FingerprintCredentialMapper, FingerprintCredential>` 与 `@RequiredArgsConstructor`。若父类已有 `mapper`/`cacheOps` 字段导致同名字段遮蔽编译冲突，则子类**不重复声明**这两个字段、不把它们放进构造器，改用父类保护字段；以 `mdp-base/md-mvc-flex` 中 `SuperServiceImpl` 实际字段为准（编码前先 Read 该文件确认）。测试构造器随之调整（mock 的 mapper/cacheOps 通过父类字段注入可用反射或构造器保留——以确认结果为准，优先构造器注入）。

补充测试方法（加到类中）：

```java
    @Test
    @DisplayName("未验证密码直接获取录入挑战被拒绝")
    void 未验证密码直接获取录入挑战被拒绝() {
        Mockito.mockStatic(ContextUtil.class).when(ContextUtil::getUserId).thenReturn(1L);
        when(cacheOps.get(any())).thenReturn(null);
        assertThrows(BizException.class, () -> service.registrationOptions());
    }
```

（`ContextUtil.getUserId` 为静态方法，用 `mockStatic`；若 `ContextUtil` 不可 mock 则改为在 service 中新增包级可覆写的 `currentUserId()` 保护方法并 spy 覆盖——以可编译可测为准，优先 mockStatic。）

```java
    @Test
    @DisplayName("密码校验通过后发放一次性票据")
    void 密码校验通过后发放一次性票据() {
        Mockito.mockStatic(ContextUtil.class).when(ContextUtil::getUserId).thenReturn(1L);
        User user = new User().setId(1L).setUsername("admin").setPassword("<sha256>").setSalt("salt");
        when(ssoUserService.getById(1L)).thenReturn(user);
        UsernameLoginStrategyImpl usernameStrategy = mock(UsernameLoginStrategyImpl.class);
        when(loginStrategy.get("USERNAME")).thenReturn(usernameStrategy);
        service.checkPassword("pwd");
        verify(cacheOps).set(any(), eq("1"));
    }
```

（`User` 的 setter 为链式（`@Accessors(chain=true)`）；测试中密码哈希值构造：`SecureUtil.sha256("pwd" + user.getSalt())` 后 set 进 user，确保 mock 校验通过。`UsernameLoginStrategyImpl` mock 时 `checkUserPassword` 为 void 方法默认不做任何事即通过。）

```java
    @Test
    @DisplayName("凭证数量达到上限时录入被拒绝")
    void 凭证数量达到上限时录入被拒绝() {
        // registrationOptions 通过（票据存在 + startRegistration mock 返回 options）后
        // verifyRegistration 时 mapper.selectCountByQuery 返回 maxCredentialsPerUser(10)
        // 断言 assertThrows(BizException.class, () -> service.verifyRegistration(dto))
    }
```

（实现细节：`relyingParty.startRegistration(any())` 需 mock 返回 `PublicKeyCredentialCreationOptions`——构造真实对象较繁琐，可用 `PublicKeyCredentialCreationOptions.builder()` 以最小参数构建（rp/user/challenge/pubKeyCredParams 必填）。若构建困难，把"数量上限"检查移到 verifyRegistration 开头且在 mock startRegistration 前触发不了的场景绕开——最终以测试可跑为准，必要时允许把上限校验从 verifyRegistration 前置到 challenge 解码后，保证纯单元可测。）

- [ ] **Step 3: 运行确认失败**

Run: `cd /Users/tangyh/gitee/mdp && mvn test -pl mdp-apps/md-workbench/workbench-service -am -Dtest=FingerprintServiceImplTest -DfailIfNoTests=false`
Expected: 编译失败（FingerprintServiceImpl 不存在）

- [ ] **Step 4: 实现 Service 绑定侧**

创建 `FingerprintServiceImpl`，移植 uni-auth `FingerprintServiceImpl` 的以下方法（逐字逻辑，按差异调整 import）：
- 类骨架：`@Slf4j @Service @RequiredArgsConstructor`，`extends SuperServiceImpl<FingerprintCredentialMapper, FingerprintCredential> implements FingerprintService`
- 构造注入：`RelyingParty relyingParty, FingerprintProperties fingerprintProperties, SsoUserService ssoUserService, LoginSessionService loginSessionService, Map<String, LoginStrategy> loginStrategy`（+ mapper/cacheOps 处理见 Step 2 注）
- `checkPassword(String password)`：差异点——`authService` 换成不存在，不需要；策略获取 `loginStrategy.get(LoginType.USERNAME.name())` → `loginStrategy.get("USERNAME")`；票据 key 用 `new FingerprintPwdVerifiedCacheKeyBuilder().key(String.valueOf(userId))`（import `top.mddata.common.cache.workbench.FingerprintPwdVerifiedCacheKeyBuilder`）；`LoginDto` 用 `top.mddata.workbench.dto.LoginDto` 且 `loginDto.setAuthType(AuthTypeEnum.USERNAME)`
- `registrationOptions()`：逐字移植（票据一次性消费 → 用户校验 → `startRegistration`（residentKey REQUIRED + UV REQUIRED）→ 挑战保存 → optionsJson）
- `verifyRegistration(dto)`：逐字移植（挑战一次性消费 → 上限校验 → 重建请求 → `finishRegistration` → 落库返回 VO）
- 私有辅助一并移植：`saveChallenge`/`getAndDeleteChallenge`（key 改用 `new FingerprintChallengeCacheKeyBuilder().key(challengeId)`）、`aaguidToString`、`currentUa`/`currentIp`/`currentRequest`、`descriptors`、`buildUserIdentity`（`user.getName()`：MDP User 的姓名字段是 `name`）、`toVo(FingerprintCredential)`、`listRecordsByUserId`、`getByCredentialId`
- Task 6/7 的方法（`listMine/rename/delete/loginOptions/loginVerify` + `publishLoginFail`/`checkUserEnabled`/`checkOwner`）本任务先以 `throw new UnsupportedOperationException("Task 6/7 实现");` 占位实现（**这是编译需要，不是功能占位**，Task 6/7 会替换）

- [ ] **Step 5: 运行确认通过**

Run: 同 Step 3
Expected: PASS（绑定侧测试全绿）

- [ ] **Step 6: 补齐 Task 6/7 所需 pojo（避免 Task 5 编译不过）**

`FingerprintRenameDto`（`workbench-pojo/.../dto/`）：

```java
package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 指纹凭证改名入参
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Data
@Schema(title = "FingerprintRenameDto", description = "指纹凭证改名入参")
public class FingerprintRenameDto {

    @Schema(description = "凭证ID")
    @NotNull(message = "凭证ID不能为空")
    private Long id;

    @Schema(description = "设备名称")
    @NotBlank(message = "设备名称不能为空")
    private String name;
}
```

`FingerprintLoginVerifyDto`（`workbench-pojo/.../dto/`）：

```java
package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 指纹登录验证入参
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Data
@Schema(title = "FingerprintLoginVerifyDto", description = "指纹登录验证入参")
public class FingerprintLoginVerifyDto {

    @Schema(description = "登录挑战ID")
    @NotBlank(message = "挑战ID不能为空")
    private String challengeId;

    @Schema(description = "WebAuthn认证响应JSON")
    @NotBlank(message = "认证响应不能为空")
    private String responseJson;

    @Schema(description = "此次登录的客户端设备id")
    private String deviceId;

    @Schema(description = "设备信息")
    private String deviceInfo;
}
```

`FingerprintLoginOptionsVo`（`workbench-pojo/.../vo/`，结构与 RegisterOptionsVo 相同，注释改"指纹登录挑战出参"）：

```java
package top.mddata.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 指纹登录挑战出参
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Data
@Schema(title = "FingerprintLoginOptionsVo", description = "指纹登录挑战出参")
public class FingerprintLoginOptionsVo {

    @Schema(description = "挑战ID，verify 时原样回传")
    private String challengeId;

    @Schema(description = "WebAuthn PublicKeyCredentialRequestOptions JSON")
    private String optionsJson;
}
```

---

### Task 6: Service 管理侧（listMine / rename / delete）（mdp 后端，TDD）

**Files:**
- Modify: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/impl/FingerprintServiceImpl.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/impl/FingerprintServiceImplTest.java`

**Interfaces:**
- Consumes: Task 5 的 `FingerprintServiceImpl` 骨架、`ContextUtil.getUserId()`、`getByCredentialId/listRecordsByUserId` 私有方法。
- Produces: `listMine(): List<FingerprintCredentialVo>`、`rename(Long, FingerprintRenameDto): Boolean`、`delete(Long): Boolean`（越权抛 BizException"无权操作他人指纹凭证"）。

- [ ] **Step 1: 写失败测试**

追加到 `FingerprintServiceImplTest`：

```java
    @Test
    @DisplayName("删除他人指纹凭证被拒绝")
    void 删除他人指纹凭证被拒绝() {
        try (var mocked = Mockito.mockStatic(ContextUtil.class)) {
            mocked.when(ContextUtil::getUserId).thenReturn(2L);
            FingerprintCredential record = new FingerprintCredential().setId(100L).setUserId(1L);
            when(mapper.selectOneById(100L)).thenReturn(record);
            BizException ex = assertThrows(BizException.class, () -> service.delete(100L));
            assertEquals("无权操作他人指纹凭证", ex.getMessage());
        }
    }

    @Test
    @DisplayName("删除自己的指纹凭证走逻辑删除")
    void 删除自己的指纹凭证走逻辑删除() {
        try (var mocked = Mockito.mockStatic(ContextUtil.class)) {
            mocked.when(ContextUtil::getUserId).thenReturn(1L);
            FingerprintCredential record = new FingerprintCredential().setId(100L).setUserId(1L);
            when(mapper.selectOneById(100L)).thenReturn(record);
            when(mapper.deleteById(100L)).thenReturn(1);
            Boolean result = service.delete(100L);
            assertEquals(Boolean.TRUE, result);
        }
    }

    @Test
    @DisplayName("改名校验归属后更新设备名")
    void 改名校验归属后更新设备名() {
        try (var mocked = Mockito.mockStatic(ContextUtil.class)) {
            mocked.when(ContextUtil::getUserId).thenReturn(1L);
            FingerprintCredential record = new FingerprintCredential().setId(100L).setUserId(1L).setDeviceName("旧名");
            when(mapper.selectOneById(100L)).thenReturn(record);
            when(mapper.update(any(FingerprintCredential.class))).thenReturn(1);
            Boolean result = service.rename(100L, new FingerprintRenameDto().setId(100L).setName("新名"));
            assertEquals(Boolean.TRUE, result);
        }
    }
```

注意：`SuperServiceImpl` 的删除可能是 `removeById`（内部走 LogicDeleteManager）；先 Read `mdp-base/md-mvc-flex/.../SuperServiceImpl.java` 确认可用方法名，mock 对应的 mapper 方法（`deleteById` 或父类封装）。以实际父类 API 为准调整 mock 与断言。

- [ ] **Step 2: 运行确认失败**

Run: `mvn test -pl mdp-apps/md-workbench/workbench-service -am -Dtest=FingerprintServiceImplTest -DfailIfNoTests=false`
Expected: FAIL（UnsupportedOperationException）

- [ ] **Step 3: 实现三个方法**

替换 Task 5 的占位实现，逻辑从 uni-auth 移植：
- `listMine()`：`toVo(listRecordsByUserId(ContextUtil.getUserId()))`
- `rename(Long id, FingerprintRenameDto dto)`：查记录 → null 抛"指纹凭证不存在" → `checkOwner` → `record.setDeviceName(dto.getName())` → `updateById(record)`
- `delete(Long id)`：查记录 → null 抛"指纹凭证不存在" → `checkOwner` → `removeById(id)`（父类逻辑删除）
- `checkOwner(record)`：userId 与 `ContextUtil.getUserId()` 不等抛 `BizException("无权操作他人指纹凭证")`

- [ ] **Step 4: 运行确认通过**

Run: 同 Step 2
Expected: PASS

---

### Task 7: Service 登录侧（loginOptions / loginVerify）（mdp 后端，TDD）

**Files:**
- Modify: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/impl/FingerprintServiceImpl.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/impl/FingerprintServiceImplTest.java`

**Interfaces:**
- Consumes: `RelyingParty.startAssertion/finishAssertion`、`AssertionRequest.fromJson/toJson`、`LoginSessionService.loginAuthenticated(user, AuthTypeEnum.FINGERPRINT, username, deviceId, deviceInfo, "PC")`（返回 `LoginVo`）、`LoginEvent`（`top.mddata.workbench.event.LoginEvent`）+ `LoginLogDto.failByCheck(authType, deviceInfo, account, statusReason)`。
- Produces: `loginOptions(): FingerprintLoginOptionsVo`、`loginVerify(dto): R<LoginVo>`。

- [ ] **Step 1: 写失败测试**

```java
    @Test
    @DisplayName("登录挑战过期时验证被拒绝")
    void 登录挑战过期时验证被拒绝() {
        FingerprintLoginVerifyDto dto = new FingerprintLoginVerifyDto();
        dto.setChallengeId("gone");
        when(cacheOps.get(any())).thenReturn(null);
        BizException ex = assertThrows(BizException.class, () -> service.loginVerify(dto));
        assertEquals("登录请求已过期，请重新验证", ex.getMessage());
    }
```

（`finishAssertion` 失败路径依赖真实 AssertionRequest JSON，纯单元测试成本高——以"挑战过期"与"结果非成功抛指纹验证失败"两条为主；成功路径由验收阶段手工联调覆盖，在测试类中加注释说明。）

- [ ] **Step 2: 运行确认失败**

Run: `mvn test -pl mdp-apps/md-workbench/workbench-service -am -Dtest=FingerprintServiceImplTest -DfailIfNoTests=false`
Expected: FAIL

- [ ] **Step 3: 实现两个方法**

从 uni-auth 移植，差异点：
- `loginOptions()` 逐字相同（`startAssertion` UV REQUIRED → 挑战存 AssertionRequest JSON）
- `loginVerify(dto)`：完成登录改为 `LoginVo loginVo = loginSessionService.loginAuthenticated(user, AuthTypeEnum.FINGERPRINT, user.getUsername(), dto.getDeviceId(), dto.getDeviceInfo(), "PC"); return R.success(loginVo);`
- `publishLoginFail(result)`：`LoginEvent` 构造改用 MDP 的 `LoginLogDto.failByCheck(AuthTypeEnum.FINGERPRINT, null, account, "指纹验证失败")` → `new LoginEvent(dto)`（`SpringUtil.publishEvent`，异常吞掉只 debug 日志，与 uni-auth 一致）
- `checkUserEnabled(user)`：MDP User 的 `state` 字段语义 `[0-禁用 1-正常]`，判断改为 `if (user.getState() != null && !user.getState())` 时抛"您已被禁用，请联系管理员开通账号！"并发布失败事件（先 Read `UsernameLoginStrategyImpl.checkUserState` 对齐判断写法与事件构造，保持一致）

- [ ] **Step 4: 运行确认通过**

Run: 同 Step 2
Expected: PASS（全部测试）

---

### Task 8: Controller 与资源 API SQL（mdp 后端）

**Files:**
- Create: `mdp-apps/md-workbench/workbench-web/src/main/java/top/mddata/workbench/controller/FingerprintController.java`
- Create: `mdp-apps/md-workbench/workbench-web/src/main/java/top/mddata/workbench/controller/FingerprintLoginController.java`
- Create: `docs/sql/20261113-security-fingerprint-resource-api.sql`

**Interfaces:**
- Consumes: Task 5-7 的 `FingerprintService` 全部方法。
- Produces: HTTP 接口：
  - 登录态（需登录+接口权限）：`POST /profile/fingerprint/checkPassword`、`POST /profile/fingerprint/registration/options`、`POST /profile/fingerprint/registration/verify`、`GET /profile/fingerprint/list`、`POST /profile/fingerprint/rename`、`POST /profile/fingerprint/delete`
  - 免登录（/anyUser 前缀自动放行）：`GET /anyUser/fingerprint/login/options`、`POST /anyUser/fingerprint/login/verify`

- [ ] **Step 1: FingerprintController**

从 uni-auth `center-controller/.../FingerprintController.java` 移植，差异点：
- `@RequestMapping("/profile/fingerprint")`
- 请求方式按 api-standards：`rename` 改 `@PostMapping("/rename")`（入参 `FingerprintRenameDto`，不再用 PathVariable）；`delete` 改 `@PostMapping("/delete")`（入参 `top.mddata.common.dto.IdDto`，先 Read 该类确认字段名 `id`）；`registration/verify` 入参去掉 PathVariable 无影响
- `CheckPasswordDto` 内部类保留（`@Data`，`@NotBlank(message = "密码不能为空") password`）
- `@WebLog` → MDP 用 `@RequestLog(value = "录入指纹", logType = RequestLog.LogType.ADD)`（delete 用 `RequestLog.LogType.DELETE`；import `top.mddata.base.annotation.log.RequestLog`）
- 其余（`@Tag`、`@Operation`、返回 `R<T>`）同 uni-auth

- [ ] **Step 2: FingerprintLoginController**

从 uni-auth 移植，差异点：包名 `top.mddata.workbench.controller`、`@RequestMapping("/anyUser/fingerprint")`、`@Tag(name = "指纹登录", description = "登录页指纹免密登录")`、返回类型 `R<FingerprintLoginOptionsVo>` / `R<LoginVo>`（`LoginVo` 为 `top.mddata.workbench.vo.LoginVo`）、`loginVerify` 返回 `R.success(...)`（uni-auth 中直接 return service 的 R，MDP 保持一致）。

- [ ] **Step 3: 资源 API 注册 SQL**

写入 `docs/sql/20261113-security-fingerprint-resource-api.sql`。格式逐段参照 `docs/sql/20261112-security-social-resource-api.sql`（临时表 + 固定雪花 ID + 从 `mdc_resource_menu` 查 `workbench:user:security` 菜单 + `NOT EXISTS` 防重 + 可重复执行）。注册以下 6 条（`application_name='workbench'`，controller 列填对应类简单名）：

| uri | request_method | name |
|---|---|---|
| /workbench/profile/fingerprint/checkPassword | POST | 指纹录入前验证密码 |
| /workbench/profile/fingerprint/registration/options | POST | 获取指纹录入挑战 |
| /workbench/profile/fingerprint/registration/verify | POST | 指纹录入验证 |
| /workbench/profile/fingerprint/list | GET | 我的指纹凭证列表 |
| /workbench/profile/fingerprint/rename | POST | 指纹凭证改名 |
| /workbench/profile/fingerprint/delete | POST | 删除指纹凭证 |

（固定 ID 从 150200000000000001 起递增，避免与 social 的 1501000000000000xx 冲突。）

- [ ] **Step 4: 编译 + 全量测试验证**

Run: `cd /Users/tangyh/gitee/mdp && mvn test -pl mdp-apps/md-workbench/workbench-service -am -Dtest=Fingerprint*Test -DfailIfNoTests=false`
Expected: PASS

---

### Task 9: 前端 API 层与类型（mdp-vben）

**Files:**
- Create: `apps/web-workbench/src/api/common/fingerprint.ts`
- Create: `apps/web-workbench/src/api/workbench/user/security/fingerprint.ts`
- Modify: `apps/web-workbench/src/api/common/index.ts`（加 `export * from './fingerprint';`）
- Modify: `apps/web-workbench/src/api/common/model.d.ts` 或新建 `apps/web-workbench/src/api/common/model/fingerprint.d.ts`（按 `model/social` 的实际组织方式存放；登录返回复用 `AuthModel.LoginVO`）

**Interfaces:**
- Produces:
  - `FingerprintApi.loginOptions(): Promise<{ challengeId: string; optionsJson: string }>`、`FingerprintApi.loginVerify(data): Promise<AuthModel.LoginVO>`（免登录，前缀 `/workbench/anyUser/fingerprint`）
  - `SecurityFingerprintApi.list(): Promise<FingerprintCredentialVo[]>`、`checkPassword(password): Promise<boolean>`、`registrationOptions(): Promise<{ challengeId: string; optionsJson: string }>`、`registrationVerify(data): Promise<FingerprintCredentialVo>`、`rename(data: { id: number; name: string }): Promise<boolean>`、`remove(id: number): Promise<boolean>`（登录态，前缀 `/workbench/profile/fingerprint`）

- [ ] **Step 1: 类型定义**

参照 `api/common/model/social` 的组织方式创建指纹类型（`FingerprintLoginOptions`、`FingerprintRegisterOptions`、`FingerprintCredentialVo`、`FingerprintRegisterVerifyDto`、`FingerprintLoginVerifyDto`），字段与后端 VO/DTO 一一对应（camelCase）。

- [ ] **Step 2: 免登录 API**

`api/common/fingerprint.ts`（格式参照 `api/common/social.ts` 的 namespace 风格）：

```ts
import type { AuthModel } from '#/api';

import type { FingerprintModel } from './model/fingerprint';

import { ServicePrefixEnum } from '@vben/constants';
import { requestClient } from '#/api/request';

const MODULAR = `${ServicePrefixEnum.WORKBENCH}/anyUser/fingerprint`;

export namespace FingerprintApi {
  /** 获取指纹登录挑战（免登录） */
  export const loginOptions = () =>
    requestClient.get<FingerprintModel.FingerprintLoginOptions>(
      `${MODULAR}/login/options`,
    );

  /** 指纹登录验证（免登录），成功返回登录信息 */
  export const loginVerify = (
    data: FingerprintModel.FingerprintLoginVerifyDto,
  ) => requestClient.post<AuthModel.LoginVO>(`${MODULAR}/login/verify`, data);
}
```

（import 路径以 `model/social` 实际写法为准校正。）

- [ ] **Step 3: 登录态 API**

`api/workbench/user/security/fingerprint.ts`（格式参照同目录 `social.ts`）：

```ts
import type { FingerprintModel } from '#/api/common/model/fingerprint';

import { ServicePrefixEnum } from '@vben/constants';
import { requestClient } from '#/api/request';

const MODULAR = `${ServicePrefixEnum.WORKBENCH}/profile/fingerprint`;

export namespace SecurityFingerprintApi {
  /** 我的指纹凭证列表 */
  export const list = () =>
    requestClient.get<FingerprintModel.FingerprintCredentialVo[]>(
      `${MODULAR}/list`,
    );

  /** 录入前验证当前密码 */
  export const checkPassword = (password: string) =>
    requestClient.post<boolean>(`${MODULAR}/checkPassword`, { password });

  /** 获取录入挑战 */
  export const registrationOptions = () =>
    requestClient.post<FingerprintModel.FingerprintRegisterOptions>(
      `${MODULAR}/registration/options`,
    );

  /** 验证录入结果并保存 */
  export const registrationVerify = (
    data: FingerprintModel.FingerprintRegisterVerifyDto,
  ) =>
    requestClient.post<FingerprintModel.FingerprintCredentialVo>(
      `${MODULAR}/registration/verify`,
      data,
    );

  /** 改设备名 */
  export const rename = (data: { id: number; name: string }) =>
    requestClient.post<boolean>(`${MODULAR}/rename`, data);

  /** 删除指纹凭证（逻辑删除） */
  export const remove = (id: number) =>
    requestClient.post<boolean>(`${MODULAR}/delete`, { id });
}
```

- [ ] **Step 4: 类型检查**

Run: `cd /Users/tangyh/gitee/mdp-vben && pnpm -F @vben/web-workbench run typecheck`（若无该脚本，用 package.json scripts 中的等价命令）
Expected: 无新增类型错误

---

### Task 10: 安全设置指纹 Tab（mdp-vben）

**Files:**
- Modify: `apps/web-workbench/src/locales/langs/zh-CN/workbench/user/security.json`（`face` 节点 → `fingerprint`）
- Modify: `apps/web-workbench/src/locales/langs/en-US/workbench/user/security.json`（同上）
- Create: `apps/web-workbench/src/views/workbench/user/security/data/fingerprint.ts`
- Create: `apps/web-workbench/src/views/workbench/user/security/modules/fingerprint.vue`
- Delete: `apps/web-workbench/src/views/workbench/user/security/data/face.tsx`
- Delete: `apps/web-workbench/src/views/workbench/user/security/modules/face.vue`
- Modify: `apps/web-workbench/src/views/workbench/user/security/index.vue`（tabItems face → fingerprint）
- Modify: 涉及 face 的测试文件（先 `grep -rn "face" apps/web-workbench/src/views/workbench/user/security` 找出全部引用）

**Interfaces:**
- Consumes: Task 9 的 `SecurityFingerprintApi`、`@simplewebauthn/browser` 的 `browserSupportsWebAuthn/startRegistration`、`useDeviceInfo`（deviceId/deviceInfo 来源，参照 `#/store/auth.ts` L40-41 的用法）。
- Produces: 组件 `Fingerprint`（default export，供 index.vue 的 tabItems 渲染）。

- [ ] **Step 1: i18n 文案**

zh-CN `security.json`：把 `"face": { "title": "人脸认证", "desc": "开启后，登录可以人脸登录" }` 改为：

```json
"fingerprint": {
  "title": "指纹登录",
  "desc": "绑定后，可在本机使用指纹免密登录",
  "unsupported": "当前浏览器或设备不支持指纹登录",
  "bind": "添加指纹",
  "verifyPassword": "验证登录密码",
  "password": "登录密码",
  "passwordRequired": "请输入登录密码",
  "deviceName": "设备名称",
  "deviceNamePlaceholder": "给这台设备起个名字，如：办公室MacBook",
  "registeredAt": "绑定时间",
  "lastUsedAt": "最后使用",
  "neverUsed": "从未使用",
  "rename": "改名",
  "unbind": "解绑",
  "unbindConfirm": "解绑后，将无法使用该设备的指纹登录，确定解绑？",
  "empty": "尚未绑定任何指纹",
  "bindSuccess": "指纹绑定成功",
  "unbindSuccess": "已解绑",
  "renameSuccess": "改名成功"
}
```

en-US 同步提供英文（title "Fingerprint Login" 等，键结构相同）。

- [ ] **Step 2: 安装依赖**

Run: `cd /Users/tangyh/gitee/mdp-vben && pnpm add @simplewebauthn/browser --filter @vben/web-workbench`
Expected: package.json 出现 `@simplewebauthn/browser`（^13）

- [ ] **Step 3: data/fingerprint.ts**

组合函数（结构参照 `data/social.ts` 的 `useSocialAccounts`）：

```ts
import { startRegistration, browserSupportsWebAuthn } from '@simplewebauthn/browser';
import { ref } from 'vue';

import { $t } from '@vben/locales';

import { message } from 'antdv-next';

import { SecurityFingerprintApi } from '#/api/workbench/user/security/fingerprint';

import { useDeviceInfo } from '#/hooks/use-device-info'; // 以实际 hook 路径为准（auth.ts 中 useDeviceInfo 的 import 路径）

export function useFingerprint() {
  const credentials = ref<FingerprintModel.FingerprintCredentialVo[]>([]);
  const loading = ref(false);
  const errorMessage = ref('');
  const supported = browserSupportsWebAuthn();

  async function load() {
    loading.value = true;
    errorMessage.value = '';
    try {
      credentials.value = await SecurityFingerprintApi.list();
    } catch (error) {
      errorMessage.value = (error as Error).message || '加载指纹列表失败';
    } finally {
      loading.value = false;
    }
  }

  /** 绑定流程：验证密码 → 录入挑战 → WebAuthn 采集 → 提交验证 */
  async function bind(password: string, deviceName: string) {
    await SecurityFingerprintApi.checkPassword(password);
    const options = await SecurityFingerprintApi.registrationOptions();
    const attResp = await startRegistration({
      optionsJSON: JSON.parse(options.optionsJson) as PublicKeyCredentialCreationOptionsJSON,
    });
    const { deviceId } = useDeviceInfo();
    await SecurityFingerprintApi.registrationVerify({
      challengeId: options.challengeId,
      responseJson: JSON.stringify(attResp),
      deviceId: deviceId.value,
      deviceName,
      platform: navigator.platform || '',
    });
    message.success($t('workbench.user.security.fingerprint.bindSuccess'));
    await load();
  }

  async function remove(id: number) {
    await SecurityFingerprintApi.remove(id);
    message.success($t('workbench.user.security.fingerprint.unbindSuccess'));
    await load();
  }

  async function rename(id: number, name: string) {
    await SecurityFingerprintApi.rename({ id, name });
    message.success($t('workbench.user.security.fingerprint.renameSuccess'));
    await load();
  }

  return { credentials, loading, errorMessage, supported, load, bind, remove, rename };
}
```

说明：`PublicKeyCredentialCreationOptionsJSON` 类型来自 `@simplewebauthn/browser` 的 re-export；若 typecheck 报缺类型，改用 `JSON.parse(options.optionsJson) as any` 并加注释说明（yubico 输出为 W3C 标准 JSON）。`useDeviceInfo` 的真实 import 路径与返回结构以 `#/store/auth.ts` 中的用法为准。

- [ ] **Step 4: modules/fingerprint.vue**

结构参照 `modules/social.vue`（Card + 列表 + Alert + 操作确认区）：
- `#title`：`$t('workbench.user.security.fingerprint.title')` + desc
- `#extra`：`supported` 为 true 时显示「添加指纹」按钮（打开密码验证 + 设备名输入的弹层/内联区），false 时按钮禁用 + Alert 提示 unsupported
- 列表：`v-for` 渲染 credentials（设备名、platform、registeredAt、lastUsedAt——空显示 neverUsed），行内「改名」「解绑」按钮；解绑前用 `Modal.confirm`（antdv-next）展示 unbindConfirm 文案
- onMounted 调 `load()`

- [ ] **Step 5: 更新 index.vue 与删除 face**

- `index.vue`：import `Face from './modules/face.vue'` → `Fingerprint from './modules/fingerprint.vue'`；tabItems 中 `face` 项改为 `{ key: 'fingerprint', label: $t('workbench.user.security.fingerprint.title'), content: h(Fingerprint) }`
- 删除 `data/face.tsx` 与 `modules/face.vue`
- `grep -rn "face" apps/web-workbench/src/views/workbench/user/security apps/web-workbench/src/views/workbench/user/security/data` 清理残留引用（含 index.test.ts、form-display.test.ts 中相关断言）

- [ ] **Step 6: 测试与类型检查**

Run: `cd /Users/tangyh/gitee/mdp-vben && pnpm test --run apps/web-workbench && pnpm -F @vben/web-workbench run typecheck`
Expected: 现有测试通过（face 相关断言更新后），无类型错误

---

### Task 11: 登录页指纹入口（mdp-vben）

**Files:**
- Modify: `apps/web-workbench/src/views/_core/sso/login.vue`（第三方登录插槽旁增加指纹入口）
- Modify: `apps/web-workbench/src/views/_core/sso/data/account.tsx`（新增 handleFingerprintLogin）——或新建 `data/fingerprint.ts`（推荐，逻辑独立）

**Interfaces:**
- Consumes: Task 9 `FingerprintApi`、`authStore.completeLogin`（`#/store/auth.ts` L58-89）、`redirectAfterLogin`（`./data/login.tsx` L48-57）、`useDeviceInfo`、`@simplewebauthn/browser` 的 `browserSupportsWebAuthn/startAuthentication`、`state.ssoParam`（SSO 回跳参数）。
- Produces: 登录页「指纹登录」按钮 → 完成登录跳转。

- [ ] **Step 1: 新建 data/fingerprint.ts**

```ts
import { startAuthentication, browserSupportsWebAuthn } from '@simplewebauthn/browser';

import { FingerprintApi } from '#/api/common/fingerprint';
import { useAuthStore } from '#/store';

import { redirectAfterLogin } from './login';

/** 是否支持指纹登录（浏览器 + 平台认证器） */
export const fingerprintSupported = browserSupportsWebAuthn();

/** 发起指纹登录：挑战 → WebAuthn 认证（系统弹出账号选择器）→ 验证并完成登录 */
export async function handleFingerprintLogin(ssoParam?: Record<string, any>) {
  const options = await FingerprintApi.loginOptions();
  const asseResp = await startAuthentication({
    optionsJSON: JSON.parse(options.optionsJson) as any,
  });
  const loginVO = await FingerprintApi.loginVerify({
    challengeId: options.challengeId,
    responseJson: JSON.stringify(asseResp),
    // deviceId/deviceInfo 组装方式参照 authStore.authLogin 中的 useDeviceInfo 用法
  });
  const authStore = useAuthStore();
  await authStore.completeLogin(loginVO, () => redirectAfterLogin(ssoParam));
}
```

（`useDeviceInfo` 的 deviceId/deviceInfo 组装直接参照 `#/store/auth.ts` L40-41 在 `authLogin` 中的写法复制过来；`redirectAfterLogin` 若未 export 则改为在 completeLogin 成功回调里复用 `login.tsx` 的 `handleSuccess` 模式 `setTimeout(() => redirectAfterLogin(ssoParam), 500)`。）

- [ ] **Step 2: login.vue 加按钮**

在 `<SocialLogin>` 所在插槽同级（第三方登录区域上方）加：

```vue
<Button
  v-if="fingerprintSupported"
  block
  class="mt-2"
  @click="handleFingerprintLogin(state.ssoParam)"
>
  指纹登录
</Button>
```

按钮文案如走 i18n 则在 `locales/langs/zh-CN/workbench/user/security.json` 不合适——登录页文案位置以登录页现有文案组织方式为准（`$t` 键加到对应 locale 文件，en-US 同步）。失败时异常由全局响应拦截器提示（`errorMessageResponseInterceptor`），无需额外 catch；可加 try/catch 转安静处理避免重复 toast。

- [ ] **Step 3: 手工联调验证清单（需要启动后端 + 前端）**

以下为人工验收（对应设计文档第 9 节），无法自动化：
1. 在 application 配置 `mdp.fingerprint.enabled=true`、`rp-id`、`origins` 后启动 boot-server
2. 安全设置页绑定指纹（密码验证 → 系统指纹采集）
3. 同一用户第二台浏览器/浏览器 Profile 再绑定一台
4. 同一台电脑换账号再绑定
5. 退出登录 → 登录页点「指纹登录」→ 系统账号选择器显示"姓名（用户名）"→ 选择 → 登录成功跳转
6. 解绑后登录失败；登录日志记录"指纹登录"

- [ ] **Step 4: 测试与类型检查**

Run: `cd /Users/tangyh/gitee/mdp-vben && pnpm test --run apps/web-workbench && pnpm -F @vben/web-workbench run typecheck`
Expected: 通过

---

### Task 12: 收尾验证（两个仓库）

- [ ] **Step 1: 后端全量相关测试**

Run: `cd /Users/tangyh/gitee/mdp && mvn test -pl mdp-apps/md-workbench/workbench-service -pl mdp-apps/md-public/md-cache-key -am -Dtest='Fingerprint*' -DfailIfNoTests=false`
Expected: PASS

- [ ] **Step 2: 前端构建**

Run: `cd /Users/tangyh/gitee/mdp-vben && pnpm -F @vben/web-workbench run build`
Expected: BUILD 成功

- [ ] **Step 3: 向用户汇报**

汇报内容：新增/修改文件清单、SQL 脚本位置（提醒用户手工执行 2 个 SQL）、需要用户在环境配置中填写的 `mdp.fingerprint.*` 配置项、手工验收清单（Task 11 Step 3）。**不执行任何 git 操作。**

---

## Self-Review 记录

1. **Spec 覆盖**：表结构（Task 2）、8 个 Service 方法（Task 5/6/7）、Controller（Task 8）、AuthTypeEnum（Task 1）、缓存（Task 3）、资源 API SQL（Task 8）、前端 API/安全设置/登录页/i18n（Task 9/10/11）、验收清单（Task 11 Step 3）——全部覆盖。密码票据、挑战一次性消费、越权校验、上限校验都在移植代码中。
2. **占位符**：无 TBD/TODO。有两处"以实际父类/hook API 为准"的条件指令（SuperServiceImpl 的 cacheOps/removeById、useDeviceInfo 路径），均已写明确认方法与备选方案，属于代码库事实核查而非设计缺口。
3. **类型一致性**：`FingerprintService` 8 方法签名在 Task 5 一次声明，Task 6/7 只补实现；前端 API 命名与 Task 8 的 HTTP 路径一一对应（checkPassword/registration/options/registration/verify/list/rename/delete、login/options、login/verify）。
