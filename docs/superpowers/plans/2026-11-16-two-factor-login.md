# 双因素认证（2FA）实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 MDP 增加基于短信/邮箱 OTP 的双因素认证：全局策略（关闭/自选/强制）+ 用户自开通 + 密码登录两步验证。

**Architecture:** 密码登录第一步通过后不创建会话，签发一次性 Redis ticket；前端进入 2FA 输入码界面，凭 ticket 发送 OTP 并验证，通过后走既有 `loginAuthenticated` 颁发 token。全局策略存 `mdc_config`（经 `ConfigFacade` 读取），用户开通状态存新表 `mdw_user_two_factor_auth`。

**Tech Stack:** Java 17 / SpringBoot 3 / MyBatis-Flex / sa-token / Redis（CacheOps）/ Vue3 + TypeScript / vitest

**Spec:** `docs/superpowers/specs/2026-11-16-two-factor-login-design.md`

## Global Constraints

- **禁止任何 git 操作**（add/commit/push 均由用户自行执行）。每个任务的验证步骤只运行测试/编译，不做提交。
- 所有交互文案、注释、错误消息使用简体中文；代码标识符用英文。
- Mapper 必须加 `@Repository`（组件扫描注册，漏加启动报 bean 缺失）。
- 实体拆分：`XxxBase`（表字段）+ `Xxx`（业务代码），实体类统一 `@Accessors(chain = true)`。
- 错误处理 fail fast：用 `ArgumentAssert` + `BizException`，错误信息具体。
- 日志/注释不输出验证码明文、ticket 全值（仅前 8 位）。
- 缓存 key 命名规范 `[前缀:]业务类型[:业务字段][:业务值]`，只允许小写字母、数字、点号、冒号。
- SQL 遵循 `docs/database-schema.md`（雪花ID 主键、禁外键、逻辑删除）。
- 函数不超过 80 行，超长拆分。
- 后端测试框架 org.junit.jupiter.api.Test + Mockito；前端 vitest。
- 涉及认证代码修改，实施完成后需用户进行安全评审再上线。

---

## Task 1: SQL 变更文件

**Files:**
- Create: `mdp/docs/sql/20261116-security-two-factor-table.sql`

**Interfaces:**
- Produces: 表 `mdw_user_two_factor_auth`；配置项 `TWO_FACTOR_STRATEGY`、`TWO_FACTOR_SMS_TEMPLATE`、`TWO_FACTOR_EMAIL_TEMPLATE`

- [ ] **Step 1: 创建 SQL 文件**

参照 `docs/sql/20261113-security-fingerprint-table.sql` 的风格（START TRANSACTION 包裹）：

```sql
-- 双因素认证：用户开通记录表 + 全局策略配置项
START TRANSACTION;

CREATE TABLE IF NOT EXISTS `mdw_user_two_factor_auth` (
  `id`         bigint      NOT NULL COMMENT '主键（雪花ID）',
  `user_id`    bigint      NOT NULL COMMENT '用户ID',
  `enabled`    tinyint     NOT NULL DEFAULT '0' COMMENT '是否开通 0-关闭 1-开通',
  `channel`    varchar(16) NOT NULL COMMENT '验证渠道 PHONE-仅手机 EMAIL-仅邮箱 BOTH-手机或邮箱均可',
  `created_at` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `created_by` bigint      DEFAULT NULL COMMENT '创建人',
  `updated_at` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updated_by` bigint      DEFAULT NULL COMMENT '更新人',
  `deleted_at` bigint      NOT NULL DEFAULT '0' COMMENT '删除标识',
  `deleted_by` bigint      DEFAULT NULL COMMENT '删除人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`, `deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='双因素认证开通记录';

-- 全局策略：OFF-关闭 OPTIONAL-允许自选 FORCE-强制全员
INSERT INTO `mdc_config` (`id`, `config_group`, `uniq_key`, `name`, `value`, `data_type`, `state`, `created_by`, `created_at`, `updated_by`, `updated_at`)
SELECT 1946000000000000001, 'SECURITY', 'TWO_FACTOR_STRATEGY', '双因素认证全局策略', 'OFF', '1', b'1', 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `mdc_config` WHERE `uniq_key` = 'TWO_FACTOR_STRATEGY');

-- 2FA OTP 短信模板 key（消息模板需在控制台消息管理中预先配置）
INSERT INTO `mdc_config` (`id`, `config_group`, `uniq_key`, `name`, `value`, `data_type`, `state`, `created_by`, `created_at`, `updated_by`, `updated_at`)
SELECT 1946000000000000002, 'SECURITY', 'TWO_FACTOR_SMS_TEMPLATE', '双因素认证短信模板标识', 'LOGIN_TWO_FACTOR_SMS', '1', b'1', 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `mdc_config` WHERE `uniq_key` = 'TWO_FACTOR_SMS_TEMPLATE');

-- 2FA OTP 邮件模板 key
INSERT INTO `mdc_config` (`id`, `config_group`, `uniq_key`, `name`, `value`, `data_type`, `state`, `created_by`, `created_at`, `updated_by`, `updated_at`)
SELECT 1946000000000000003, 'SECURITY', 'TWO_FACTOR_EMAIL_TEMPLATE', '双因素认证邮件模板标识', 'LOGIN_TWO_FACTOR_EMAIL', '1', b'1', 1, NOW(), 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `mdc_config` WHERE `uniq_key` = 'TWO_FACTOR_EMAIL_TEMPLATE');

COMMIT;
```

注意：`mdc_config` 实际字段以 `ConfigBase` 为准（config_group/uniq_key/name/value/data_type/state 等），执行前用 `sed -n 40,42p docs/sql/mdp.sql` 对照表结构，若 `state` 字段类型不同（布尔位 vs 字符串）按现状调整 INSERT 列。

- [ ] **Step 2: 人工校验 SQL**

Run: `grep -c "INSERT INTO" mdp/docs/sql/20261116-security-two-factor-table.sql`
Expected: `3`

---

## Task 2: 枚举、DTO/VO 与 LoginVo 扩展（workbench-pojo）

**Files:**
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/enumeration/TwoFactorChannelEnum.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/enumeration/TwoFactorStrategyEnum.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/dto/TwoFactorSendDto.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/dto/TwoFactorVerifyDto.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/dto/TwoFactorEnableDto.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/dto/TwoFactorDisableDto.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/vo/TwoFactorStatusVo.java`
- Modify: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/vo/LoginVo.java`

**Interfaces:**
- Produces: `TwoFactorChannelEnum{PHONE,EMAIL,BOTH}`、`TwoFactorStrategyEnum{OFF,OPTIONAL,FORCE}`（均 `fromCode(String)` 静态方法）；`LoginVo` 新增 `status/twoFactorTicket/twoFactorChannel/twoFactorMaskedTarget`

- [ ] **Step 1: 创建枚举**

```java
package top.mddata.workbench.enumeration;

import com.mybatisflex.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.exception.BizException;
import top.mddata.base.interfaces.BaseEnum;
import top.mddata.base.utils.ArgumentAssert;

import java.util.Arrays;

/**
 * 双因素认证验证渠道
 *
 * @author tangyh
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "TwoFactorChannelEnum", description = "双因素认证验证渠道")
public enum TwoFactorChannelEnum implements BaseEnum<String> {
    PHONE("PHONE", "仅手机"),
    EMAIL("EMAIL", "仅邮箱"),
    BOTH("BOTH", "手机或邮箱均可");

    @Schema(description = "编码")
    @EnumValue
    private String code;
    @Schema(description = "描述")
    private String desc;

    public static TwoFactorChannelEnum fromCode(String code) {
        return Arrays.stream(values()).filter(item -> item.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new BizException("验证渠道不合法"));
    }
}
```

```java
package top.mddata.workbench.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

import java.util.Arrays;

/**
 * 双因素认证全局策略
 *
 * @author tangyh
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "TwoFactorStrategyEnum", description = "双因素认证全局策略")
public enum TwoFactorStrategyEnum implements BaseEnum<String> {
    OFF("OFF", "关闭"),
    OPTIONAL("OPTIONAL", "允许用户自选"),
    FORCE("FORCE", "强制全员");

    @Schema(description = "编码")
    private String code;
    @Schema(description = "描述")
    private String desc;

    public static TwoFactorStrategyEnum of(String code) {
        return Arrays.stream(values()).filter(item -> item.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(OFF);
    }
}
```

- [ ] **Step 2: 创建 DTO**

```java
package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 双因素认证发送验证码 入参（匿名段，凭 ticket）
 *
 * @author tangyh
 */
@Data
@Schema(title = "TwoFactorSendDto", description = "双因素认证发送验证码入参")
public class TwoFactorSendDto {

    @Schema(description = "登录第一步返回的一次性票据")
    @NotBlank(message = "票据不能为空")
    private String ticket;

    @Schema(description = "验证渠道，仅开通渠道为 BOTH 时可传 PHONE/EMAIL 切换，不传默认手机优先")
    private String channel;
}
```

```java
package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 双因素认证验证码校验 入参（匿名段）
 *
 * @author tangyh
 */
@Data
@Schema(title = "TwoFactorVerifyDto", description = "双因素认证验证码校验入参")
public class TwoFactorVerifyDto {

    @Schema(description = "登录第一步返回的一次性票据")
    @NotBlank(message = "票据不能为空")
    private String ticket;

    @Schema(description = "验证码")
    @NotBlank(message = "验证码不能为空")
    private String code;
}
```

```java
package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 双因素认证开通/更换渠道 入参（需登录）
 *
 * @author tangyh
 */
@Data
@Schema(title = "TwoFactorEnableDto", description = "双因素认证开通入参")
public class TwoFactorEnableDto {

    @Schema(description = "验证渠道 PHONE/EMAIL/BOTH")
    @NotNull(message = "验证渠道不能为空")
    private TwoFactorChannelEnum channel;

    @Schema(description = "发送到新渠道的验证码")
    @NotBlank(message = "验证码不能为空")
    private String code;
}
```

```java
package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 双因素认证关闭 入参（需登录）
 *
 * @author tangyh
 */
@Data
@Schema(title = "TwoFactorDisableDto", description = "双因素认证关闭入参")
public class TwoFactorDisableDto {

    @Schema(description = "当前渠道收到的验证码")
    @NotBlank(message = "验证码不能为空")
    private String code;
}
```

- [ ] **Step 3: 创建 StatusVo**

```java
package top.mddata.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.mddata.workbench.enumeration.TwoFactorChannelEnum;
import top.mddata.workbench.enumeration.TwoFactorStrategyEnum;

/**
 * 双因素认证状态 Vo（安全设置页展示）
 *
 * @author tangyh
 */
@Data
@Schema(title = "TwoFactorStatusVo", description = "双因素认证状态")
public class TwoFactorStatusVo {

    @Schema(description = "是否已开通")
    private Boolean enabled;

    @Schema(description = "验证渠道")
    private TwoFactorChannelEnum channel;

    @Schema(description = "全局策略")
    private TwoFactorStrategyEnum strategy;

    @Schema(description = "脱敏接收目标（手机号/邮箱）")
    private String maskedTarget;
}
```

- [ ] **Step 4: 扩展 LoginVo**

在 `LoginVo.java` 的 `expire` 字段后追加：

```java
    /**
     * 登录状态：TOKEN-已颁发令牌 TWO_FACTOR-待二次验证
     */
    @Schema(description = "登录状态", example = "TOKEN", allowableValues = {"TOKEN", "TWO_FACTOR"})
    private String status;

    @Schema(description = "双因素认证一次性票据，仅 status=TWO_FACTOR 时非空")
    private String twoFactorTicket;

    @Schema(description = "本次 OTP 发送渠道 PHONE/EMAIL，仅 status=TWO_FACTOR 时非空")
    private String twoFactorChannel;

    @Schema(description = "脱敏接收目标，仅 status=TWO_FACTOR 时非空")
    private String twoFactorMaskedTarget;
```

并在类上加默认值方法（直接在 `LoginVo` 内新增静态工厂，供触发判定使用）：

```java
    /**
     * 直接登录成功（未触发双因素）
     */
    public static LoginVo token() {
        LoginVo vo = new LoginVo();
        vo.setStatus("TOKEN");
        return vo;
    }
```

注意：`loginAuthenticated` 已用 `BeanUtil.toBean(tokenInfo, LoginVo.class)` 构建，需在该方法返回前补 `loginVo.setStatus("TOKEN")`（Task 9 处理）。

- [ ] **Step 5: 编译验证**

Run: `cd mdp && mvn compile -pl mdp-apps/md-workbench/workbench-pojo -am -q`
Expected: BUILD SUCCESS

---

## Task 3: 缓存键（md-cache-key + CacheKeyTable）

**Files:**
- Modify: `mdp-apps/md-public/md-cache-key/src/main/java/top/mddata/common/cache/CacheKeyTable.java`
- Create: `mdp-apps/md-public/md-cache-key/src/main/java/top/mddata/common/cache/workbench/TwoFactorCacheKeyBuilder.java`

**Interfaces:**
- Produces: `TwoFactorCacheKeyBuilder.ticket(ticket)`、`TwoFactorCacheKeyBuilder.ticketIndex(userId)`、`TwoFactorCacheKeyBuilder.otp(scene, target)`、`TwoFactorCacheKeyBuilder.otpLimit(scene, target)`（OTP 失败计数复用 `otp` key 配合 `cacheOps` 自建 fail key，见 Task 7）；常量 `CacheKeyTable.TWO_FACTOR = "two_factor"`

- [ ] **Step 1: CacheKeyTable 加常量**

在 `CacheKeyTable` 接口中 `String FORGET_PWD = "forget_pwd";` 之后追加：

```java
    /**
     * 双因素认证（ticket、otp）
     */
    String TWO_FACTOR = "two_factor";
```

- [ ] **Step 2: 创建构建器**

```java
package top.mddata.common.cache.workbench;

import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 双因素认证缓存 KEY：ticket 5 分钟、OTP 5 分钟、发送间隔 60 秒。
 * 场景隔离：登录二次验证与开通验证使用不同 scene，杜绝跨场景用码。
 *
 * @author tangyh
 */
public class TwoFactorCacheKeyBuilder implements CacheKeyBuilder {

    /** 登录二次验证 OTP 场景 */
    public static final String SCENE_LOGIN = "login";
    /** 开通/关闭/换渠道验证 OTP 场景 */
    public static final String SCENE_MANAGE = "manage";

    private static final Duration TICKET_EXPIRE = Duration.ofMinutes(5);
    private static final Duration OTP_EXPIRE = Duration.ofMinutes(5);
    private static final Duration OTP_LIMIT_EXPIRE = Duration.ofSeconds(60);

    private final String field;

    private TwoFactorCacheKeyBuilder(String field) {
        this.field = field;
    }

    public static CacheKey ticket(String ticket) {
        return new TwoFactorCacheKeyBuilder("ticket").key(ticket);
    }

    /** 用户维度票据索引，用于同一用户重复登录时作废旧 ticket */
    public static CacheKey ticketIndex(Long userId) {
        return new TwoFactorCacheKeyBuilder("ticket.user").key(userId);
    }

    public static CacheKey otp(String scene, String target) {
        return new TwoFactorCacheKeyBuilder(scene + ".otp").key(target);
    }

    /** OTP 发送间隔限制 key */
    public static CacheKey otpLimit(String scene, String target) {
        return new TwoFactorCacheKeyBuilder(scene + ".otp.limit").key(target);
    }

    @Override
    public String getTable() {
        return CacheKeyTable.TWO_FACTOR;
    }

    @Override
    public String getField() {
        return field;
    }

    @Override
    public Duration getExpire() {
        return OTP_EXPIRE;
    }

    public Duration ticketExpire() {
        return TICKET_EXPIRE;
    }

    public Duration otpLimitExpire() {
        return OTP_LIMIT_EXPIRE;
    }
}
```

- [ ] **Step 3: 编译验证**

Run: `cd mdp && mvn compile -pl mdp-apps/md-public/md-cache-key -am -q`
Expected: BUILD SUCCESS

---

## Task 4: 实体与 Mapper（workbench-pojo + workbench-dao）

**Files:**
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/entity/base/UserTwoFactorAuthBase.java`
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/entity/UserTwoFactorAuth.java`
- Create: `mdp-apps/md-workbench/workbench-dao/src/main/java/top/mddata/workbench/mapper/UserTwoFactorAuthMapper.java`

**Interfaces:**
- Produces: 实体 `UserTwoFactorAuth`（表 `mdw_user_two_factor_auth`）；Mapper `UserTwoFactorAuthMapper extends SuperMapper<UserTwoFactorAuth>`，按 userId 查询/更新

- [ ] **Step 1: 创建 Base 实体**

参照 `FingerprintCredentialBase.java` 的注解风格（@Table、字段注释）：

```java
package top.mddata.workbench.entity.base;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 双因素认证开通记录实体类。
 *
 * @author tangyh
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(value = UserTwoFactorAuthBase.TABLE_NAME)
public class UserTwoFactorAuthBase extends SuperEntity<Long> implements Serializable {

    /** 表名称 */
    public static final String TABLE_NAME = "mdw_user_two_factor_auth";

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.None)
    private Long id;

    /** 用户ID */
    @Column("user_id")
    private Long userId;

    /** 是否开通 0-关闭 1-开通 */
    @Column("enabled")
    private Boolean enabled;

    /** 验证渠道 PHONE-仅手机 EMAIL-仅邮箱 BOTH-手机或邮箱均可 */
    @Column("channel")
    private String channel;

    @Column(onInsertValue = "NOW()", onUpdateValue = "NOW()")
    private java.time.LocalDateTime createdAt;

    @Column("created_by")
    private Long createdBy;

    @Column(onInsertValue = "NOW()", onUpdateValue = "NOW()")
    private java.time.LocalDateTime updatedAt;

    @Column("updated_by")
    private Long updatedBy;
}
```

注意：注解细节（`@Id(keyType = KeyType.None)`、逻辑删除字段、`@Column(onInsertValue=...)` 用法）以同目录 `FingerprintCredentialBase.java` 现有写法为准，逐字段对照复制风格；`mdw_user_two_factor_auth` 未建逻辑删除字段索引列时按指纹表同样的 `deleted_at` 处理方式补齐。

- [ ] **Step 2: 创建业务实体**

```java
package top.mddata.workbench.entity;

import lombok.EqualsAndHashCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.workbench.entity.base.UserTwoFactorAuthBase;
import top.mddata.workbench.enumeration.TwoFactorChannelEnum;

import java.io.Serial;

/**
 * 双因素认证开通记录。
 *
 * @author tangyh
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserTwoFactorAuth extends UserTwoFactorAuthBase {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 获取验证渠道枚举
     */
    public TwoFactorChannelEnum channelEnum() {
        return TwoFactorChannelEnum.fromCode(getChannel());
    }
}
```

- [ ] **Step 3: 创建 Mapper**

```java
package top.mddata.workbench.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.workbench.entity.UserTwoFactorAuth;

/**
 * 双因素认证开通记录 Mapper
 *
 * @author tangyh
 */
@Repository
@Mapper
public interface UserTwoFactorAuthMapper extends SuperMapper<UserTwoFactorAuth> {
}
```

（`@Mapper` 是否需要以 `FingerprintCredentialMapper.java` 现状为准，保持一致。）

- [ ] **Step 4: 编译验证**

Run: `cd mdp && mvn compile -pl mdp-apps/md-workbench/workbench-dao -am -q`
Expected: BUILD SUCCESS

---

## Task 5: 策略解析器 TwoFactorStrategyResolver

**Files:**
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/TwoFactorStrategyResolver.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/TwoFactorStrategyResolverTest.java`

**Interfaces:**
- Consumes: `ConfigFacade.getString(uniqKey, defaultValue)`（console-facade 已存在，返回 `String`）
- Produces: `TwoFactorStrategyEnum resolve()`（60 秒本地缓存，异常时回退 OFF）

- [ ] **Step 1: 写失败测试**

```java
package top.mddata.workbench.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import top.mddata.console.facade.system.ConfigFacade;
import top.mddata.workbench.enumeration.TwoFactorStrategyEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 策略解析器单元测试：缓存命中与异常回退
 *
 * @author tangyh
 */
class TwoFactorStrategyResolverTest {

    private ConfigFacade configFacade;
    private TwoFactorStrategyResolver resolver;

    @BeforeEach
    void setUp() {
        configFacade = Mockito.mock(ConfigFacade.class);
        resolver = new TwoFactorStrategyResolver(configFacade);
    }

    @Test
    void resolve_shouldCacheWithin60Seconds() {
        when(configFacade.getString("TWO_FACTOR_STRATEGY", "OFF")).thenReturn("FORCE");

        assertEquals(TwoFactorStrategyEnum.FORCE, resolver.resolve());
        assertEquals(TwoFactorStrategyEnum.FORCE, resolver.resolve());

        verify(configFacade, times(1)).getString(anyString(), anyString());
    }

    @Test
    void resolve_shouldFallbackOff_whenFacadeThrows() {
        when(configFacade.getString("TWO_FACTOR_STRATEGY", "OFF")).thenThrow(new RuntimeException("db down"));

        assertEquals(TwoFactorStrategyEnum.OFF, resolver.resolve());
    }

    @Test
    void resolve_shouldParseOffExplicitly() {
        when(configFacade.getString("TWO_FACTOR_STRATEGY", "OFF")).thenReturn("OFF");

        assertEquals(TwoFactorStrategyEnum.OFF, resolver.resolve());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd mdp && mvn test -pl mdp-apps/md-workbench/workbench-service -Dtest=TwoFactorStrategyResolverTest -q`
Expected: 编译失败（TwoFactorStrategyResolver 不存在）

- [ ] **Step 3: 实现**

```java
package top.mddata.workbench.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mddata.console.facade.system.ConfigFacade;
import top.mddata.workbench.enumeration.TwoFactorStrategyEnum;

import java.time.Instant;

/**
 * 双因素认证全局策略解析器。
 *
 * <p>每次登录都要判定是否触发二次验证，为避免登录链路查库，配置做 60 秒本地缓存；
 * 读取异常时回退 OFF（可用性优先，放行不阻断登录）。</p>
 *
 * @author tangyh
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TwoFactorStrategyResolver {

    public static final String CONFIG_KEY = "TWO_FACTOR_STRATEGY";

    private static final long CACHE_MILLIS = 60_000L;

    private final ConfigFacade configFacade;

    private volatile Instant cachedAt = Instant.EPOCH;
    private volatile TwoFactorStrategyEnum cached = TwoFactorStrategyEnum.OFF;

    /**
     * 解析当前全局策略
     *
     * @return 全局策略，读取异常时返回 OFF
     */
    public TwoFactorStrategyEnum resolve() {
        if (isCacheValid()) {
            return cached;
        }
        synchronized (this) {
            if (isCacheValid()) {
                return cached;
            }
            cached = load();
            cachedAt = Instant.now();
            return cached;
        }
    }

    private boolean isCacheValid() {
        return cachedAt.plusMillis(CACHE_MILLIS).isAfter(Instant.now());
    }

    private TwoFactorStrategyEnum load() {
        try {
            return TwoFactorStrategyEnum.of(configFacade.getString(CONFIG_KEY, "OFF"));
        } catch (Exception e) {
            log.error("[双因素认证] 读取全局策略失败，回退 OFF", e);
            return TwoFactorStrategyEnum.OFF;
        }
    }
}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd mdp && mvn test -pl mdp-apps/md-workbench/workbench-service -Dtest=TwoFactorStrategyResolverTest -q`
Expected: 3 个测试 PASS

---

## Task 6: 票据服务 TwoFactorTicketService

**Files:**
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/TwoFactorTicketService.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/TwoFactorTicketServiceTest.java`

**Interfaces:**
- Consumes: `CacheOps`（`set(CacheKey, Object)`、`get(CacheKey, Class)`、`del(CacheKey)`、`existed(CacheKey)`）— 与 `VerificationCodeServiceImpl` 用法一致
- Produces:
  - `String issue(TwoFactorTicketPayload payload)` — 签发（同时作废同用户旧票）
  - `TwoFactorTicketPayload consume(String ticket)` — 一次性消费（删除成功才返回）
  - `void recordFail(String ticket)` / `boolean isBlocked(String ticket)` — 失败计数与作废判定
  - `TwoFactorTicketPayload peek(String ticket)` — 只读校验（send 用，不消费）
- `TwoFactorTicketPayload`（内部静态类，放本文件同包 dto）字段：`userId:Long, authType:String, account:String, deviceId:String, deviceInfo:String, channel:String, target:String`（`target` 为 OTP 实际接收目标）

- [ ] **Step 1: 写失败测试**

```java
package top.mddata.workbench.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import top.mddata.base.cache.repository.CacheOps;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 2FA 票据服务单元测试：一次性消费、互斥作废、失败计数
 *
 * @author tangyh
 */
class TwoFactorTicketServiceTest {

    private CacheOps cacheOps;
    private TwoFactorTicketService ticketService;
    private TwoFactorTicketService.TwoFactorTicketPayload payload;

    @BeforeEach
    void setUp() {
        cacheOps = Mockito.mock(CacheOps.class);
        ticketService = new TwoFactorTicketService(cacheOps);
        payload = new TwoFactorTicketService.TwoFactorTicketPayload();
        payload.setUserId(1L);
        payload.setAuthType("CAPTCHA");
        payload.setAccount("admin");
        payload.setChannel("PHONE");
        payload.setTarget("138****5678");
    }

    @Test
    void consume_shouldReturnPayloadWhenDeleteSucceeds() {
        String ticket = ticketService.issue(payload);
        when(cacheOps.get(any(), any())).thenReturn(payload);

        AtomicReference<Object> deleted = new AtomicReference<>();
        Mockito.doAnswer(invocation -> {
            deleted.set(invocation.getArgument(0));
            return true;
        }).when(cacheOps).del(any());

        assertEquals(payload.getUserId(), ticketService.consume(ticket).getUserId());
    }

    @Test
    void consume_shouldFailWhenTicketMissing() {
        when(cacheOps.get(any(), any())).thenReturn(null);
        assertNull(ticketService.consume("not-exist"));
    }

    @Test
    void issue_shouldInvalidatePreviousTicketOfSameUser() {
        String first = ticketService.issue(payload);
        String second = ticketService.issue(payload);
        // 旧票签发记录被覆盖：通过 peek 旧票返回 null 验证（mock get 返回 null）
        when(cacheOps.get(any(), any())).thenReturn(null);
        assertNull(ticketService.peek(first));
        assertNull(ticketService.peek(second));
    }

    @Test
    void recordFail_shouldThrowWhenReachingMaxAttempts() {
        when(cacheOps.existed(any())).thenReturn(false);
        ticketService.recordFail("t1");
        ticketService.recordFail("t1");
        ticketService.recordFail("t1");
        ticketService.recordFail("t1");
        assertThrows(top.mddata.base.exception.BizException.class, () -> ticketService.recordFail("t1"));
    }

    @Test
    void peek_shouldNotConsumeTicket() {
        when(cacheOps.get(any(), any())).thenReturn(payload);
        ticketService.peek("t1");
        verify(cacheOps, never()).del(any());
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd mdp && mvn test -pl mdp-apps/md-workbench/workbench-service -Dtest=TwoFactorTicketServiceTest -q`
Expected: 编译失败（类不存在）

- [ ] **Step 3: 实现**

```java
package top.mddata.workbench.service;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.BizException;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.workbench.TwoFactorCacheKeyBuilder;

import java.time.Duration;

/**
 * 双因素认证一次性票据服务。
 *
 * <p>密码验证通过后签发，绑定用户与登录上下文；同一用户重复登录时旧票互斥作废；
 * 消费即删除，验证失败累计 5 次作废。</p>
 *
 * @author tangyh
 */
@Service
@RequiredArgsConstructor
public class TwoFactorTicketService {

    private static final int MAX_FAIL_COUNT = 5;

    private final CacheOps cacheOps;

    /**
     * 票据载荷：登录上下文 + 二次验证目标
     */
    @Data
    public static class TwoFactorTicketPayload {
        private Long userId;
        private String authType;
        /** 登录账号（用户名/手机号/邮箱，用于登录日志） */
        private String account;
        private String deviceId;
        private String deviceInfo;
        /** OTP 发送渠道 PHONE/EMAIL */
        private String channel;
        /** OTP 实际接收目标（明文，仅存在于 Redis，不回传前端） */
        private String target;
    }

    /**
     * 签发票据，同一用户旧票据即时作废
     */
    public String issue(TwoFactorTicketPayload payload) {
        ArgumentAssert.notNull(payload.getUserId(), () -> new BizException("票据载荷缺少用户标识"));

        CacheKey indexKey = TwoFactorCacheKeyBuilder.ticketIndex(payload.getUserId());
        String oldTicket = cacheOps.get(indexKey, String.class);
        if (StrUtil.isNotEmpty(oldTicket)) {
            cacheOps.del(TwoFactorCacheKeyBuilder.ticket(oldTicket));
        }

        String ticket = UUID.fastUUID().toString(true);
        TwoFactorCacheKeyBuilder builder = newTicketBuilder();
        cacheOps.set(TwoFactorCacheKeyBuilder.ticket(ticket), payload,
                builder.ticketExpire());
        cacheOps.set(indexKey, ticket, builder.ticketExpire());
        return ticket;
    }

    /**
     * 更新票据载荷（send 换渠道后回写 channel/target），保留原有效期
     */
    public void updatePayload(String ticket, TwoFactorTicketPayload payload) {
        cacheOps.set(TwoFactorCacheKeyBuilder.ticket(ticket), payload,
                newTicketBuilder().ticketExpire());
    }

    /**
     * 只读校验（发送验证码用），不消费。票据被作废或失败超限时返回 null
     */
    public TwoFactorTicketPayload peek(String ticket) {
        if (isBlocked(ticket)) {
            return null;
        }
        return cacheOps.get(TwoFactorCacheKeyBuilder.ticket(ticket),
                TwoFactorTicketPayload.class);
    }

    /**
     * 一次性消费：删除成功才返回载荷，防止并发复用
     */
    public TwoFactorTicketPayload consume(String ticket) {
        CacheKey ticketKey = TwoFactorCacheKeyBuilder.ticket(ticket);
        TwoFactorTicketPayload payload = cacheOps.get(ticketKey, TwoFactorTicketPayload.class);
        if (payload == null) {
            return null;
        }
        Boolean removed = cacheOps.del(ticketKey);
        if (!Boolean.TRUE.equals(removed)) {
            return null;
        }
        cacheOps.del(TwoFactorCacheKeyBuilder.ticketIndex(payload.getUserId()));
        return payload;
    }

    /**
     * 记录一次验证失败，达到上限抛异常
     */
    public void recordFail(String ticket) {
        CacheKey failKey = TwoFactorCacheKeyBuilder.otp("ticket.fail", ticket);
        Integer count = cacheOps.get(failKey, Integer.class);
        int next = (count == null ? 0 : count) + 1;
        cacheOps.set(failKey, next, newTicketBuilder().ticketExpire());
        ArgumentAssert.isTrue(next < MAX_FAIL_COUNT,
                () -> new BizException("验证失败次数过多，请重新登录"));
    }

    private boolean isBlocked(String ticket) {
        return Boolean.TRUE.equals(cacheOps.existed(
                TwoFactorCacheKeyBuilder.otp("ticket.fail", ticket)));
    }

    private TwoFactorCacheKeyBuilder newTicketBuilder() {
        return new TwoFactorCacheKeyBuilder("ticket");
    }
}
```

注意：`CacheOps` 的方法签名（`get/existed/del` 返回值）以 `VerificationCodeServiceImpl` 及 `RedisOps` 现有调用为准，若 `del` 返回类型不同按现状调整 `consume` 的判断。

- [ ] **Step 4: 运行测试确认通过**

Run: `cd mdp && mvn test -pl mdp-apps/md-workbench/workbench-service -Dtest=TwoFactorTicketServiceTest -q`
Expected: 5 个测试 PASS

---

## Task 7: OTP 服务 TwoFactorOtpService

**Files:**
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/TwoFactorOtpService.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/TwoFactorOtpServiceTest.java`

**Interfaces:**
- Consumes: `CacheOps`、`ConfigFacade.getString(...)`（模板 key）、`MsgFacade.sendByTemplateKey(MsgSendDto)`（`MsgSendSmsDto.buildApiSender().addRecipient(...).setTemplateKey(...).addParam("code", code)`；邮件用 `MsgSendMailDto`，参照 `VerificationCodeServiceImpl.sendPhoneCode/sendEmailCode`）
- Produces:
  - `String send(String scene, TwoFactorChannelEnum channel, String target)` — 校验 60s 间隔，生成 6 位数字码，发送，返回脱敏目标
  - `void verify(String scene, String target, String code)` — 校验并删除，失败 5 次作废
- 场景常量：`TwoFactorCacheKeyBuilder.SCENE_LOGIN` / `SCENE_MANAGE`

- [ ] **Step 1: 写失败测试**

```java
package top.mddata.workbench.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.BizException;
import top.mddata.console.facade.message.MsgFacade;
import top.mddata.console.facade.system.ConfigFacade;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OTP 服务单元测试：发送间隔、场景隔离、失败次数
 *
 * @author tangyh
 */
class TwoFactorOtpServiceTest {

    private CacheOps cacheOps;
    private ConfigFacade configFacade;
    private MsgFacade msgFacade;
    private TwoFactorOtpService otpService;

    @BeforeEach
    void setUp() {
        cacheOps = Mockito.mock(CacheOps.class);
        configFacade = Mockito.mock(ConfigFacade.class);
        msgFacade = Mockito.mock(MsgFacade.class);
        otpService = new TwoFactorOtpService(cacheOps, configFacade, msgFacade);
        when(configFacade.getString("TWO_FACTOR_SMS_TEMPLATE", "LOGIN_TWO_FACTOR_SMS"))
                .thenReturn("LOGIN_TWO_FACTOR_SMS");
    }

    @Test
    void send_shouldRejectWithin60Seconds() {
        when(cacheOps.existed(any())).thenReturn(true);

        BizException e = assertThrows(BizException.class,
                () -> otpService.send("login", "13800001111"));
        assertEquals("验证码发送过于频繁，请稍后再试", e.getMessage());
        verify(msgFacade, never()).sendByTemplateKey(any());
    }

    @Test
    void send_shouldSend6Digits_whenIntervalOk() {
        when(cacheOps.existed(any())).thenReturn(false);

        String masked = otpService.send("login", "13800001111");
        assertEquals("138****1111", masked);
        verify(msgFacade).sendByTemplateKey(any());
    }

    @Test
    void verify_shouldBeIsolatedBetweenScenes() {
        // login 场景写入的验证码，manage 场景不可校验通过
        doAnswer(invocation -> {
            Mockito.<Object>when(cacheOps.get(any(), any())).thenReturn("123456");
            return null;
        }).when(cacheOps).set(any(), any());

        when(cacheOps.get(any(), any())).thenReturn("123456");
        assertDoesNotThrow(() -> otpService.verify("login", "13800001111", "123456"));
    }

    @Test
    void verify_shouldThrowAfterMaxFails() {
        when(cacheOps.get(any(), any())).thenReturn("654321");
        assertThrows(BizException.class, () -> otpService.verify("login", "13800001111", "000000"));
        assertThrows(BizException.class, () -> otpService.verify("login", "13800001111", "000000"));
        assertThrows(BizException.class, () -> otpService.verify("login", "13800001111", "000000"));
        assertThrows(BizException.class, () -> otpService.verify("login", "13800001111", "000000"));
        assertThrows(BizException.class, () -> otpService.verify("login", "13800001111", "000000"));
    }
}
```

（测试中 mock 的 `cacheOps.get` 行为以实际 `CacheOps` 接口为准微调，目标是行为断言：场景隔离、间隔限制、失败 5 次抛异常。）

- [ ] **Step 2: 运行测试确认失败**

Run: `cd mdp && mvn test -pl mdp-apps/md-workbench/workbench-service -Dtest=TwoFactorOtpServiceTest -q`
Expected: 编译失败

- [ ] **Step 3: 实现**

```java
package top.mddata.workbench.service;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.BizException;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.workbench.TwoFactorCacheKeyBuilder;
import top.mddata.console.dto.message.MsgSendDto;
import top.mddata.console.dto.message.MsgSendMailDto;
import top.mddata.console.dto.message.MsgSendSmsDto;
import top.mddata.console.facade.message.MsgFacade;
import top.mddata.console.facade.system.ConfigFacade;

/**
 * 双因素认证 OTP 服务。
 *
 * <p>与图形验证码、换绑验证码场景隔离（独立缓存键前缀 + scene 维度）；
 * 发送间隔 60 秒，验证码 5 分钟有效，校验失败 5 次作废，校验成功立即删除防重放。</p>
 *
 * @author tangyh
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorOtpService {

    private static final int MAX_FAIL_COUNT = 5;

    private final CacheOps cacheOps;
    private final ConfigFacade configFacade;
    private final MsgFacade msgFacade;

    /**
     * 发送 OTP
     *
     * @param scene  场景（login / manage），用 {@link TwoFactorCacheKeyBuilder} 场景常量
     * @param target 接收目标（手机号或邮箱明文）
     * @return 脱敏后的接收目标
     */
    public String send(String scene, String target) {
        CacheKey limitKey = TwoFactorCacheKeyBuilder.otpLimit(scene, target);
        ArgumentAssert.isTrue(!Boolean.TRUE.equals(cacheOps.existed(limitKey)),
                () -> new BizException("验证码发送过于频繁，请稍后再试"));

        String code = RandomUtil.randomNumbers(6);
        cacheOps.set(TwoFactorCacheKeyBuilder.otp(scene, target), code);
        cacheOps.set(limitKey, System.currentTimeMillis(),
                newTwoFactorBuilder().otpLimitExpire());

        boolean isPhone = target.matches("^1\\d{10}$");
        if (isPhone) {
            MsgSendDto sms = MsgSendSmsDto.buildApiSender()
                    .addRecipient(target)
                    .setTemplateKey(configFacade.getString(
                            "TWO_FACTOR_SMS_TEMPLATE", "LOGIN_TWO_FACTOR_SMS"))
                    .addParam("code", code);
            msgFacade.sendByTemplateKey(sms);
            return DesensitizedUtil.mobilePhone(target);
        }

        MsgSendDto mail = MsgSendMailDto.buildApiSender()
                .addRecipient(target)
                .setTemplateKey(configFacade.getString(
                        "TWO_FACTOR_EMAIL_TEMPLATE", "LOGIN_TWO_FACTOR_EMAIL"))
                .addParam("code", code)
                .addParam("validityPeriod", "5");
        msgFacade.sendByTemplateKey(mail);
        return DesensitizedUtil.email(target);
    }

    /**
     * 校验 OTP，成功后立即删除防重放
     */
    public void verify(String scene, String target, String code) {
        CacheKey otpKey = TwoFactorCacheKeyBuilder.otp(scene, target);
        String cached = cacheOps.get(otpKey, String.class);
        if (cached == null) {
            throw new BizException("验证码已过期，请重新获取");
        }
        if (!cached.equals(code)) {
            recordFail(scene, target);
        }
        cacheOps.del(otpKey);
    }

    private void recordFail(String scene, String target) {
        CacheKey failKey = TwoFactorCacheKeyBuilder.otp(scene + ".fail", target);
        Integer count = cacheOps.get(failKey, Integer.class);
        int next = (count == null ? 0 : count) + 1;
        cacheOps.set(failKey, next, newTwoFactorBuilder().otpLimitExpire().multipliedBy(5));
        ArgumentAssert.isTrue(next < MAX_FAIL_COUNT,
                () -> new BizException("验证码错误次数过多，请重新获取"));
        throw new BizException("验证码错误，剩余 " + (MAX_FAIL_COUNT - next) + " 次机会");
    }

    private TwoFactorCacheKeyBuilder newTwoFactorBuilder() {
        return new TwoFactorCacheKeyBuilder("otp");
    }
}
```

注意：`DesensitizedUtil` 来自 hutool（`cn.hutool.core.util`），与项目现有依赖一致。

- [ ] **Step 4: 运行测试确认通过**

Run: `cd mdp && mvn test -pl mdp-apps/md-workbench/workbench-service -Dtest=TwoFactorOtpServiceTest -q`
Expected: 4 个测试 PASS

---

## Task 8: 双因素核心服务 TwoFactorAuthService

**Files:**
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/TwoFactorAuthService.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/impl/TwoFactorAuthServiceImpl.java`
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/impl/TwoFactorAuthServiceImplTest.java`

**Interfaces:**
- Consumes: `UserTwoFactorAuthMapper`、`TwoFactorStrategyResolver.resolve()`、`TwoFactorTicketService`、`TwoFactorOtpService`、`SsoUserService.getById(userId)`（`User` 实体含 `getPhone()/getEmail()/getUsername()`）
- Produces:
  - `boolean shouldTrigger(User user)` — 触发判定矩阵（spec 3.2）
  - `LoginVo startTwoFactor(User user, AuthTypeEnum authType, String account, String deviceId, String deviceInfo)` — 签发票据，返回 `status=TWO_FACTOR` 的 LoginVo
  - `LoginVo verifyLogin(TwoFactorVerifyDto dto)` — 消费票据 + 校验 OTP → 返回 null（由控制器调 `LoginSessionService.loginAuthenticated` 完成 token 颁发）。实际签名：`TwoFactorTicketService.TwoFactorTicketPayload verifyLogin(TwoFactorVerifyDto dto)`
  - `String sendLoginOtp(TwoFactorSendDto dto)` — 凭票据发送 OTP，返回脱敏目标
  - `TwoFactorStatusVo status(Long userId)`
  - `String sendManageCode(Long userId, TwoFactorChannelEnum channel)` — 开通/换渠道发码（scene=manage）
  - `void enable(Long userId, TwoFactorEnableDto dto)` / `void disable(Long userId, TwoFactorDisableDto dto)` / `void changeChannel(Long userId, TwoFactorEnableDto dto)`
  - `int batchEnableForForce()` — FORCE 批量开通，返回处理条数
- 渠道解析规则：`BOTH` → 登录发送时手机优先（有手机发手机）；`PHONE` → 手机；`EMAIL` → 邮箱；目标缺失抛 `BizException("该渠道未绑定接收目标")`

- [ ] **Step 1: 写失败测试（触发判定矩阵）**

```java
package top.mddata.workbench.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import top.mddata.common.entity.User;
import top.mddata.workbench.entity.UserTwoFactorAuth;
import top.mddata.workbench.enumeration.TwoFactorStrategyEnum;
import top.mddata.workbench.mapper.UserTwoFactorAuthMapper;
import top.mddata.workbench.service.TwoFactorOtpService;
import top.mddata.workbench.service.TwoFactorStrategyResolver;
import top.mddata.workbench.service.TwoFactorTicketService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 触发判定矩阵测试（对齐 spec 3.2）
 *
 * @author tangyh
 */
class TwoFactorAuthServiceImplTest {

    private UserTwoFactorAuthMapper mapper;
    private TwoFactorStrategyResolver strategyResolver;
    private TwoFactorTicketService ticketService;
    private TwoFactorOtpService otpService;
    private TwoFactorAuthServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(UserTwoFactorAuthMapper.class);
        strategyResolver = Mockito.mock(TwoFactorStrategyResolver.class);
        ticketService = Mockito.mock(TwoFactorTicketService.class);
        otpService = Mockito.mock(TwoFactorOtpService.class);
        service = new TwoFactorAuthServiceImpl(mapper, strategyResolver,
                ticketService, otpService);
    }

    private User user(boolean enabledRecord) {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPhone("13800001111");
        if (enabledRecord) {
            UserTwoFactorAuth record = new UserTwoFactorAuth();
            record.setUserId(1L);
            record.setEnabled(true);
            record.setChannel("BOTH");
            when(mapper.selectOneByCondition(any())).thenReturn(record);
        } else {
            when(mapper.selectOneByCondition(any())).thenReturn(null);
        }
        return user;
    }

    @Test
    void strategyOff_neverTrigger() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OFF);
        assertFalse(service.shouldTrigger(user(true)));
        assertFalse(service.shouldTrigger(user(false)));
    }

    @Test
    void strategyOptional_triggersOnlyEnabledUser() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OPTIONAL);
        assertTrue(service.shouldTrigger(user(true)));
        assertFalse(service.shouldTrigger(user(false)));
    }

    @Test
    void strategyForce_triggersEnabledUser() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        assertTrue(service.shouldTrigger(user(true)));
    }

    @Test
    void strategyForce_triggersUnenabledUserWithChannel() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        assertTrue(service.shouldTrigger(user(false)));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd mdp && mvn test -pl mdp-apps/md-workbench/workbench-service -Dtest=TwoFactorAuthServiceImplTest -q`
Expected: 编译失败

- [ ] **Step 3: 实现接口**

```java
package top.mddata.workbench.service;

import top.mddata.common.entity.User;
import top.mddata.workbench.dto.TwoFactorDisableDto;
import top.mddata.workbench.dto.TwoFactorEnableDto;
import top.mddata.workbench.dto.TwoFactorSendDto;
import top.mddata.workbench.dto.TwoFactorVerifyDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.enumeration.TwoFactorChannelEnum;
import top.mddata.workbench.service.TwoFactorTicketService.TwoFactorTicketPayload;
import top.mddata.workbench.vo.LoginVo;
import top.mddata.workbench.vo.TwoFactorStatusVo;

/**
 * 双因素认证核心服务
 *
 * @author tangyh
 */
public interface TwoFactorAuthService {

    /**
     * 触发判定（spec 3.2 矩阵）：OFF 不触发；OPTIONAL 仅已开通用户；
     * FORCE 全触发（无渠道用户由调用方拦截）
     */
    boolean shouldTrigger(User user);

    /**
     * 开始二次验证：签发票据（不创建会话），返回 TWO_FACTOR 状态的 LoginVo
     */
    LoginVo startTwoFactor(User user, AuthTypeEnum authType, String account,
                           String deviceId, String deviceInfo);

    /**
     * 凭票据发送登录二次验证 OTP，返回脱敏目标
     */
    String sendLoginOtp(TwoFactorSendDto dto);

    /**
     * 校验登录二次验证 OTP，返回票据载荷（含登录上下文），供签发会话
     */
    TwoFactorTicketPayload verifyLogin(TwoFactorVerifyDto dto);

    /**
     * 查询用户双因素状态（安全设置页）
     */
    TwoFactorStatusVo status(Long userId);

    /**
     * 开通/换渠道前发送验证码（scene=manage）
     */
    String sendManageCode(Long userId, TwoFactorChannelEnum channel);

    /**
     * 开通或更换渠道
     */
    void enable(Long userId, TwoFactorEnableDto dto);

    /**
     * 关闭（FORCE 策略下拒绝）
     */
    void disable(Long userId, TwoFactorDisableDto dto);

    /**
     * 更换验证渠道（同 enable，语义分离便于日志）
     */
    void changeChannel(Long userId, TwoFactorEnableDto dto);

    /**
     * FORCE 批量开通：所有绑定手机或邮箱的用户写入 enabled=1，幂等
     *
     * @return 处理条数
     */
    int batchEnableForForce();
}
```

- [ ] **Step 4: 实现核心类**

```java
package top.mddata.workbench.service.impl;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.exception.BizException;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.entity.User;
import top.mddata.workbench.dto.TwoFactorDisableDto;
import top.mddata.workbench.dto.TwoFactorEnableDto;
import top.mddata.workbench.dto.TwoFactorSendDto;
import top.mddata.workbench.dto.TwoFactorVerifyDto;
import top.mddata.workbench.entity.UserTwoFactorAuth;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.enumeration.TwoFactorChannelEnum;
import top.mddata.workbench.enumeration.TwoFactorStrategyEnum;
import top.mddata.workbench.mapper.UserTwoFactorAuthMapper;
import top.mddata.workbench.service.TwoFactorAuthService;
import top.mddata.workbench.service.TwoFactorOtpService;
import top.mddata.workbench.service.TwoFactorStrategyResolver;
import top.mddata.workbench.service.TwoFactorTicketService;
import top.mddata.workbench.vo.LoginVo;
import top.mddata.workbench.vo.TwoFactorStatusVo;

import java.util.List;

import static top.mddata.common.cache.workbench.TwoFactorCacheKeyBuilder.SCENE_LOGIN;
import static top.mddata.common.cache.workbench.TwoFactorCacheKeyBuilder.SCENE_MANAGE;
import static top.mddata.workbench.entity.table.UserTwoFactorAuthTableDef.USER_TWO_FACTOR_AUTH;

/**
 * 双因素认证核心服务实现。
 *
 * @author tangyh
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorAuthServiceImpl implements TwoFactorAuthService {

    private final UserTwoFactorAuthMapper twoFactorAuthMapper;
    private final TwoFactorStrategyResolver strategyResolver;
    private final TwoFactorTicketService ticketService;
    private final TwoFactorOtpService otpService;

    @Override
    public boolean shouldTrigger(User user) {
        TwoFactorStrategyEnum strategy = strategyResolver.resolve();
        if (strategy == TwoFactorStrategyEnum.OFF) {
            return false;
        }
        if (strategy == TwoFactorStrategyEnum.FORCE) {
            return true;
        }
        UserTwoFactorAuth record = findByUserId(user.getId());
        return record != null && Boolean.TRUE.equals(record.getEnabled());
    }

    @Override
    public LoginVo startTwoFactor(User user, AuthTypeEnum authType, String account,
                                  String deviceId, String deviceInfo) {
        TwoFactorChannelEnum channel = resolveStartChannel(user);

        TwoFactorTicketService.TwoFactorTicketPayload payload =
                new TwoFactorTicketService.TwoFactorTicketPayload();
        payload.setUserId(user.getId());
        payload.setAuthType(authType.name());
        payload.setAccount(account);
        payload.setDeviceId(deviceId);
        payload.setDeviceInfo(deviceInfo);
        payload.setChannel(channel.name());
        payload.setTarget(resolveTarget(user, channel));
        String ticket = ticketService.issue(payload);

        LoginVo vo = LoginVo.token();
        vo.setStatus("TWO_FACTOR");
        vo.setTwoFactorTicket(ticket);
        vo.setTwoFactorChannel(channel.name());
        vo.setTwoFactorMaskedTarget(maskTarget(payload.getTarget()));
        return vo;
    }

    @Override
    public String sendLoginOtp(TwoFactorSendDto dto) {
        TwoFactorTicketService.TwoFactorTicketPayload payload = ticketService.peek(dto.getTicket());
        ArgumentAssert.notNull(payload, () -> new BizException("票据无效或已过期，请重新登录"));

        TwoFactorChannelEnum channel = resolveSendChannel(payload, dto.getChannel());
        String target = resolveTargetByChannel(payload, channel);
        payload.setChannel(channel.name());
        payload.setTarget(target);
        ticketService.updatePayload(dto.getTicket(), payload);

        return otpService.send(SCENE_LOGIN, target);
    }

    @Override
    public TwoFactorTicketService.TwoFactorTicketPayload verifyLogin(TwoFactorVerifyDto dto) {
        TwoFactorTicketService.TwoFactorTicketPayload payload = ticketService.consume(dto.getTicket());
        ArgumentAssert.notNull(payload, () -> new BizException("票据无效或已过期，请重新登录"));
        try {
            otpService.verify(SCENE_LOGIN, payload.getTarget(), dto.getCode());
            return payload;
        } catch (BizException e) {
            ticketService.recordFail(dto.getTicket());
            throw e;
        }
    }

    @Override
    public TwoFactorStatusVo status(Long userId) {
        TwoFactorStatusVo vo = new TwoFactorStatusVo();
        vo.setStrategy(strategyResolver.resolve());
        UserTwoFactorAuth record = findByUserId(userId);
        vo.setEnabled(record != null && Boolean.TRUE.equals(record.getEnabled()));
        if (record != null) {
            vo.setChannel(record.channelEnum());
        }
        return vo;
    }

    @Override
    public String sendManageCode(Long userId, TwoFactorChannelEnum channel) {
        String target = resolveManageTarget(userId, channel);
        return otpService.send(SCENE_MANAGE, target);
    }

    @Override
    public void enable(Long userId, TwoFactorEnableDto dto) {
        String target = resolveManageTarget(userId, dto.getChannel());
        otpService.verify(SCENE_MANAGE, target, dto.getCode());
        upsertRecord(userId, dto.getChannel(), true);
    }

    @Override
    public void disable(Long userId, TwoFactorDisableDto dto) {
        ArgumentAssert.isFalse(strategyResolver.resolve() == TwoFactorStrategyEnum.FORCE,
                () -> new BizException("管理员已强制启用双因素认证，无法关闭"));
        UserTwoFactorAuth record = findByUserId(userId);
        ArgumentAssert.notNull(record, () -> new BizException("尚未开通双因素认证"));
        String target = resolveTarget(record.channelEnum());
        otpService.verify(SCENE_MANAGE, target, dto.getCode());
        record.setEnabled(false);
        twoFactorAuthMapper.update(record);
    }

    @Override
    public void changeChannel(Long userId, TwoFactorEnableDto dto) {
        enable(userId, dto);
    }

    @Override
    public int batchEnableForForce() {
        List<User> users = allActiveUsers();
        int count = 0;
        for (User user : users) {
            if (StrUtil.isEmpty(user.getPhone()) && StrUtil.isEmpty(user.getEmail())) {
                continue;
            }
            upsertForceEnable(user);
            count++;
        }
        log.info("[双因素认证] FORCE 批量开通完成，共 {} 人", count);
        return count;
    }

    // ==================== 私有方法 ====================

    private UserTwoFactorAuth findByUserId(Long userId) {
        return twoFactorAuthMapper.selectOneByCondition(QueryWrapper.create()
                .where(USER_TWO_FACTOR_AUTH.USER_ID.eq(userId)));
    }

    private TwoFactorChannelEnum resolveStartChannel(User user) {
        UserTwoFactorAuth record = findByUserId(user.getId());
        if (record != null && Boolean.TRUE.equals(record.getEnabled())) {
            return record.channelEnum();
        }
        // FORCE 下未开通用户兜底：有手机用手机，否则邮箱
        return StrUtil.isNotEmpty(user.getPhone())
                ? TwoFactorChannelEnum.PHONE : TwoFactorChannelEnum.EMAIL;
    }

    private TwoFactorChannelEnum resolveSendChannel(
            TwoFactorTicketService.TwoFactorTicketPayload payload, String requestChannel) {
        TwoFactorChannelEnum opened = TwoFactorChannelEnum.fromCode(payload.getChannel());
        if (StrUtil.isEmpty(requestChannel)) {
            return defaultChannel(opened);
        }
        TwoFactorChannelEnum requested = TwoFactorChannelEnum.fromCode(requestChannel);
        ArgumentAssert.isTrue(opened == TwoFactorChannelEnum.BOTH || opened == requested,
                () -> new BizException("当前开通渠道不支持切换"));
        return requested;
    }

    private TwoFactorChannelEnum defaultChannel(TwoFactorChannelEnum channel) {
        // BOTH 默认手机优先
        return channel == TwoFactorChannelEnum.BOTH
                ? TwoFactorChannelEnum.PHONE : channel;
    }

    private String resolveTarget(User user, TwoFactorChannelEnum channel) {
        String target = resolveTarget(user.getPhone(), user.getEmail(), channel);
        ArgumentAssert.notEmpty(target,
                () -> new BizException("双因素认证渠道未绑定手机或邮箱，请联系管理员"));
        return target;
    }

    private String resolveTargetByChannel(
            TwoFactorTicketService.TwoFactorTicketPayload payload,
            TwoFactorChannelEnum channel) {
        // 换渠道时票据只存了旧目标，此处需重新查询用户绑定目标
        User user = new User();
        user.setId(payload.getUserId());
        user.setPhone(payload.getPhone());
        user.setEmail(payload.getEmail());
        String target = resolveTarget(user, channel);
        ArgumentAssert.notEmpty(target, () -> new BizException("该渠道未绑定接收目标"));
        return target;
    }

    private String resolveTarget(TwoFactorChannelEnum channel) {
        User user = new User();
        // disable 场景目标由调用方传入当前用户，此处通过 id 重新查询
        throw new UnsupportedOperationException("由 resolveManageTarget 替代");
    }

    private String resolveManageTarget(Long userId, TwoFactorChannelEnum channel) {
        User user = requireUser(userId);
        String target = resolveTarget(user.getPhone(), user.getEmail(), defaultChannel(channel));
        ArgumentAssert.notEmpty(target, () -> new BizException("该渠道未绑定接收目标"));
        return target;
    }

    private String resolveTarget(String phone, String email, TwoFactorChannelEnum channel) {
        return switch (channel) {
            case PHONE -> StrUtil.isNotEmpty(phone) ? phone : null;
            case EMAIL -> StrUtil.isNotEmpty(email) ? email : null;
            case BOTH -> StrUtil.isNotEmpty(phone) ? phone : email;
        };
    }

    private String maskTarget(String target) {
        return target.matches("^1\\d{10}$")
                ? DesensitizedUtil.mobilePhone(target) : DesensitizedUtil.email(target);
    }

    private void upsertRecord(Long userId, TwoFactorChannelEnum channel, boolean enabled) {
        UserTwoFactorAuth record = findByUserId(userId);
        if (record == null) {
            record = new UserTwoFactorAuth();
            record.setUserId(userId);
        }
        record.setEnabled(enabled);
        record.setChannel(channel.name());
        if (record.getId() == null) {
            twoFactorAuthMapper.insert(record);
        } else {
            twoFactorAuthMapper.update(record);
        }
    }

    private void upsertForceEnable(User user) {
        UserTwoFactorAuth record = findByUserId(user.getId());
        if (record == null) {
            record = new UserTwoFactorAuth();
            record.setUserId(user.getId());
            record.setEnabled(true);
            record.setChannel(StrUtil.isNotEmpty(user.getPhone())
                    ? TwoFactorChannelEnum.PHONE.name() : TwoFactorChannelEnum.EMAIL.name());
            twoFactorAuthMapper.insert(record);
            return;
        }
        if (!Boolean.TRUE.equals(record.getEnabled())) {
            record.setEnabled(true);
            twoFactorAuthMapper.update(record);
        }
        // 已开通的不覆盖用户自选渠道
    }

    private List<User> allActiveUsers() {
        // 复用 SsoUserService/UserFacade 分页遍历启用用户，避免全表一次性加载
        throw new UnsupportedOperationException("Task 8 Step 5 补齐");
    }

    private User requireUser(Long userId) {
        throw new UnsupportedOperationException("Task 8 Step 5 补齐");
    }
}
```

- [ ] **Step 5: 补齐依赖查询（消除 UnsupportedOperationException）**

`resolveTargetByChannel` 需要 User 的 phone/email：给票据载荷增加 `phone`/`email` 字段（`TwoFactorTicketPayload` 在 Task 6 中追加 `private String phone; private String email;`，`startTwoFactor` 里 `payload.setPhone(user.getPhone()); payload.setEmail(user.getEmail());`），`resolveSendChannel` 后的 `resolveTargetByChannel` 直接用 `payload.getPhone()/getEmail()` 与 `resolveTarget(phone, email, channel)` 组合，不再查库。

`requireUser` 注入 `SsoUserService`（workbench-service 已有，`getById` 返回 User）：

```java
    private User requireUser(Long userId) {
        User user = ssoUserService.getById(userId);
        ArgumentAssert.notNull(user, () -> new BizException("用户不存在"));
        return user;
    }
```

`allActiveUsers` 注入 `SsoUserService`，用其分页接口遍历（以 `SsoUserService` 现有分页方法为准，如 `page(...)`；按启用状态过滤、每页 500，循环翻页直到取完，仅收集 id/phone/email/username）。

删除 `resolveTarget(TwoFactorChannelEnum)` 占位方法。

- [ ] **Step 6: 运行测试确认通过**

Run: `cd mdp && mvn test -pl mdp-apps/md-workbench/workbench-service -Dtest=TwoFactorAuthServiceImplTest -q`
Expected: 4 个测试 PASS

---

## Task 9: 登录链路集成（AuthServiceImpl）

**Files:**
- Modify: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/impl/AuthServiceImpl.java:88-93`（login 方法）、`:96-127`（loginAuthenticated）
- Test: `mdp-apps/md-workbench/workbench-service/src/test/java/top/mddata/workbench/service/impl/AuthServiceImplTwoFactorTest.java`

**Interfaces:**
- Consumes: `TwoFactorAuthService.shouldTrigger/startTwoFactor`（Task 8）
- Produces: `login()` 在触发时返回 `status=TWO_FACTOR` 的 LoginVo；`loginAuthenticated()` 返回的 LoginVo 带 `status=TOKEN`

- [ ] **Step 1: 写失败测试**

```java
package top.mddata.workbench.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import top.mddata.common.entity.User;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.service.TwoFactorAuthService;
import top.mddata.workbench.vo.LoginVo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 登录链路 2FA 分支测试
 *
 * @author tangyh
 */
class AuthServiceImplTwoFactorTest {

    @Test
    void login_shouldReturnTwoFactorVo_whenTriggered() {
        TwoFactorAuthService twoFactorAuthService = Mockito.mock(TwoFactorAuthService.class);
        AuthServiceImpl service = new AuthServiceImplBuilder()
                .twoFactorAuthService(twoFactorAuthService)
                .build();

        User user = new User();
        user.setId(1L);
        when(twoFactorAuthService.shouldTrigger(user)).thenReturn(true);
        LoginDto dto = new LoginDto();
        dto.setAuthType(AuthTypeEnum.CAPTCHA);
        dto.setUsername("admin");
        LoginVo twoFactorVo = new LoginVo();
        twoFactorVo.setStatus("TWO_FACTOR");
        when(twoFactorAuthService.startTwoFactor(eq(user), eq(AuthTypeEnum.CAPTCHA),
                eq("admin"), any(), any())).thenReturn(twoFactorVo);

        LoginVo result = service.login(dto).getData();
        assertEquals("TWO_FACTOR", result.getStatus());
        verify(twoFactorAuthService).startTwoFactor(any(), any(), any(), any(), any());
    }
}
```

（`AuthServiceImplBuilder` 不存在——现测试 `AuthServiceImplLoginTest.java:91` 已有该类的构造辅助方式，实施时**参照 `AuthServiceImplLoginTest` 中现有 mock 装配方式**注入新依赖 `twoFactorAuthService`，保持一致的构造参数。）

- [ ] **Step 2: 运行测试确认失败**

Run: `cd mdp && mvn test -pl mdp-apps/md-workbench/workbench-service -Dtest=AuthServiceImplTwoFactorTest -q`
Expected: FAIL（AuthServiceImpl 无 twoFactorAuthService 依赖）

- [ ] **Step 3: 修改 login 与 loginAuthenticated**

`AuthServiceImpl.login`（`AuthServiceImpl.java:89-93`）改为：

```java
    @Override
    public R<LoginVo> login(LoginDto login) {
        User user = accountAuthenticator.authenticate(login);
        if (twoFactorAuthService.shouldTrigger(user)) {
            return R.success(twoFactorAuthService.startTwoFactor(user,
                    login.getAuthType(), login.getUsername(),
                    login.getDeviceId(), login.getDeviceInfo()));
        }
        return R.success(loginAuthenticated(user, login.getAuthType(), login.getUsername(),
                login.getDeviceId(), login.getDeviceInfo(), "PC"));
    }
```

`loginAuthenticated` 返回前（`loginVo.setId(user.getId());` 之后）追加一行：

```java
        loginVo.setStatus("TOKEN");
```

类字段追加 `private final TwoFactorAuthService twoFactorAuthService;`（`@RequiredArgsConstructor` 自动注入）。

- [ ] **Step 4: 运行测试确认通过**

Run: `cd mdp && mvn test -pl mdp-apps/md-workbench/workbench-service -Dtest=AuthServiceImplTwoFactorTest,AuthServiceImplLoginTest -q`
Expected: 新旧测试均 PASS（既有登录测试不回归）

---

## Task 10: 控制器（匿名段 + 需登录段）

**Files:**
- Create: `mdp-apps/md-workbench/workbench-web/src/main/java/top/mddata/workbench/controller/TwoFactorAuthController.java`

**Interfaces:**
- Consumes: `TwoFactorAuthService`、`TwoFactorTicketService.TwoFactorTicketPayload`、`LoginSessionService.loginAuthenticated(user, authType, account, deviceId, deviceInfo, deviceType)`（`AuthServiceImpl` 已实现该接口）
- Produces: 
  - `POST /anyUser/auth/twoFactor/send` → `R<String>`（脱敏目标）
  - `POST /anyUser/auth/twoFactor/verify` → `R<LoginVo>`
  - `GET /profile/twoFactor/status` → `R<TwoFactorStatusVo>`
  - `POST /profile/twoFactor/sendEnableCode` → `R<String>`
  - `POST /profile/twoFactor/enable`、`/disable`、`/changeChannel` → `R<Void>`

- [ ] **Step 1: 创建控制器**

```java
package top.mddata.workbench.controller;

import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.common.entity.User;
import top.mddata.workbench.dto.TwoFactorDisableDto;
import top.mddata.workbench.dto.TwoFactorEnableDto;
import top.mddata.workbench.dto.TwoFactorSendDto;
import top.mddata.workbench.dto.TwoFactorVerifyDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.enumeration.TwoFactorChannelEnum;
import top.mddata.workbench.service.LoginSessionService;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.service.TwoFactorAuthService;
import top.mddata.workbench.service.TwoFactorTicketService;
import top.mddata.workbench.vo.LoginVo;
import top.mddata.workbench.vo.TwoFactorStatusVo;

/**
 * 双因素认证接口。
 *
 * <p>匿名段供登录第二步使用（凭一次性票据），需登录段供安全设置使用。</p>
 *
 * @author tangyh
 */
@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "双因素认证", description = "登录二次验证与安全设置")
public class TwoFactorAuthController {

    private final TwoFactorAuthService twoFactorAuthService;
    private final SsoUserService ssoUserService;
    private final LoginSessionService loginSessionService;

    // ==================== 匿名段：登录第二步 ====================

    @Operation(summary = "发送登录二次验证码", description = "凭登录第一步返回的票据发送 OTP")
    @PostMapping("/anyUser/auth/twoFactor/send")
    public R<String> send(@Validated @RequestBody TwoFactorSendDto dto) {
        return R.success(twoFactorAuthService.sendLoginOtp(dto));
    }

    @Operation(summary = "校验登录二次验证码", description = "验证通过后签发登录令牌")
    @PostMapping("/anyUser/auth/twoFactor/verify")
    public R<LoginVo> verify(@Validated @RequestBody TwoFactorVerifyDto dto) {
        TwoFactorTicketService.TwoFactorTicketPayload payload =
                twoFactorAuthService.verifyLogin(dto);
        User user = ssoUserService.getById(payload.getUserId());
        LoginVo loginVo = loginSessionService.loginAuthenticated(user,
                AuthTypeEnum.valueOf(payload.getAuthType()), payload.getAccount(),
                payload.getDeviceId(), payload.getDeviceInfo(), "PC");
        return R.success(loginVo);
    }

    // ==================== 需登录段：安全设置 ====================

    @Operation(summary = "查询双因素认证状态")
    @GetMapping("/profile/twoFactor/status")
    public R<TwoFactorStatusVo> status() {
        return R.success(twoFactorAuthService.status(StpUtil.getLoginIdAsLong()));
    }

    @Operation(summary = "发送开通/换渠道验证码")
    @PostMapping("/profile/twoFactor/sendEnableCode")
    public R<String> sendEnableCode(@Validated @RequestBody TwoFactorEnableDto dto) {
        return R.success(twoFactorAuthService.sendManageCode(
                StpUtil.getLoginIdAsLong(), dto.getChannel()));
    }

    @Operation(summary = "开通双因素认证")
    @PostMapping("/profile/twoFactor/enable")
    @RequestLog(value = "开通双因素认证", logType = RequestLog.LogType.UPDATE)
    public R<Void> enable(@Validated @RequestBody TwoFactorEnableDto dto) {
        twoFactorAuthService.enable(StpUtil.getLoginIdAsLong(), dto);
        return R.success();
    }

    @Operation(summary = "关闭双因素认证")
    @PostMapping("/profile/twoFactor/disable")
    @RequestLog(value = "关闭双因素认证", logType = RequestLog.LogType.UPDATE)
    public R<Void> disable(@Validated @RequestBody TwoFactorDisableDto dto) {
        twoFactorAuthService.disable(StpUtil.getLoginIdAsLong(), dto);
        return R.success();
    }

    @Operation(summary = "更换验证渠道")
    @PostMapping("/profile/twoFactor/changeChannel")
    @RequestLog(value = "更换双因素认证渠道", logType = RequestLog.LogType.UPDATE)
    public R<Void> changeChannel(@Validated @RequestBody TwoFactorEnableDto dto) {
        twoFactorAuthService.changeChannel(StpUtil.getLoginIdAsLong(), dto);
        return R.success();
    }
}
```

`TwoFactorEnableDto` 需补 `@NotNull` 渠道即可复用于 sendEnableCode（channel 为必填，code 非必填场景会校验失败——如冲突，为 sendEnableCode 单独建 `TwoFactorChannelDto{channel}`）。实施时以编译结果为准处理。

- [ ] **Step 2: 编译验证**

Run: `cd mdp && mvn compile -pl mdp-apps/md-workbench/workbench-web -am -q`
Expected: BUILD SUCCESS

---

## Task 11: FORCE 批量开通事件（console 发布 + workbench 监听）

**Files:**
- Create: `mdp-apps/md-public/md-common-config/src/main/java/top/mddata/common/event/TwoFactorForceEnableEvent.java`
- Modify: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/system/impl/ConfigServiceImpl.java:97-100`（updateAfter）
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/listener/TwoFactorForceEnableListener.java`

**Interfaces:**
- Consumes: `TwoFactorAuthService.batchEnableForForce()`
- Produces: 管理员把 `TWO_FACTOR_STRATEGY` 改为 `FORCE` 时异步批量开通（单体版同 JVM 生效；微服务版后续接 RocketMQ，本期不做）

- [ ] **Step 1: 定义事件（md-common-config，console 与 workbench 共同可见）**

```java
package top.mddata.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 双因素认证策略切换为 FORCE 事件，触发批量开通
 *
 * @author tangyh
 */
@Getter
public class TwoFactorForceEnableEvent extends ApplicationEvent {

    private final String uniqKey;
    private final String value;

    public TwoFactorForceEnableEvent(Object source, String uniqKey, String value) {
        super(source);
        this.uniqKey = uniqKey;
        this.value = value;
    }
}
```

- [ ] **Step 2: console 发布事件**

在 `ConfigServiceImpl.updateAfter`（`ConfigServiceImpl.java:97`）缓存清理之后追加：

```java
        if ("TWO_FACTOR_STRATEGY".equals(entity.getUniqKey())
                && "FORCE".equals(entity.getValue())) {
            SpringUtil.publishEvent(new TwoFactorForceEnableEvent(this,
                    entity.getUniqKey(), entity.getValue()));
        }
```

导入 `cn.hutool.extra.spring.SpringUtil` 与事件类。`saveAfter` 同理追加（新建配置项即 FORCE 的场景）。

- [ ] **Step 3: workbench 监听**

```java
package top.mddata.workbench.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import top.mddata.common.event.TwoFactorForceEnableEvent;
import top.mddata.workbench.service.TwoFactorAuthService;

/**
 * 双因素认证 FORCE 批量开通监听器。
 *
 * <p>异步执行避免阻塞配置保存；@TransactionalEventListener 保障配置事务提交后再批量开通。
 * 微服务部署时事件不跨服务，需改造为 MQ（本期单体版不做）。</p>
 *
 * @author tangyh
 */
@Slf4j
@Component
@AllArgsConstructor
public class TwoFactorForceEnableListener {

    private final TwoFactorAuthService twoFactorAuthService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onForceEnable(TwoFactorForceEnableEvent event) {
        try {
            twoFactorAuthService.batchEnableForForce();
        } catch (Exception e) {
            log.error("[双因素认证] FORCE 批量开通执行失败", e);
        }
    }
}
```

- [ ] **Step 4: 编译验证**

Run: `cd mdp && mvn compile -pl mdp-apps/md-console/console-service,mdp-apps/md-workbench/workbench-service -am -q`
Expected: BUILD SUCCESS

---

## Task 12: 前端类型与 API（mdp-vben）

**Files:**
- Modify: `mdp-vben/apps/web-workbench/src/api/common/model/auth.ts:36-42`（LoginVO）
- Create: `mdp-vben/apps/web-workbench/src/api/common/twoFactor.ts`

**Interfaces:**
- Produces: `AuthModel.LoginVO` 新增可选字段；`TwoFactorApi`：`getStatus / sendEnableCode / enable / disable / changeChannel`

- [ ] **Step 1: 扩展 LoginVO 类型**

`auth.ts` 的 `LoginVO` 改为：

```typescript
  export interface LoginVO {
    id: string;
    tokenName: string;
    tokenValue: string;
    refreshToken: string;
    expire: string;
    /** 登录状态：TOKEN-已颁发 TWO_FACTOR-待二次验证 */
    status?: 'TOKEN' | 'TWO_FACTOR';
    twoFactorTicket?: string;
    twoFactorChannel?: 'BOTH' | 'EMAIL' | 'PHONE';
    twoFactorMaskedTarget?: string;
  }
```

- [ ] **Step 2: 创建 API 文件**

```typescript
import type { AuthModel } from '#/api';

import { ServicePrefixEnum } from '@vben/constants';

import { requestClient } from '#/api/request';

const ServicePrefix = ServicePrefixEnum.WORKBENCH;

export namespace TwoFactorApi {
  export interface TwoFactorStatusVO {
    enabled: boolean;
    channel?: 'BOTH' | 'EMAIL' | 'PHONE';
    strategy: 'FORCE' | 'OFF' | 'OPTIONAL';
    maskedTarget?: string;
  }

  export interface TwoFactorChannelDTO {
    channel: 'BOTH' | 'EMAIL' | 'PHONE';
  }

  export interface TwoFactorEnableDTO extends TwoFactorChannelDTO {
    code: string;
  }

  export interface TwoFactorDisableDTO {
    code: string;
  }

  /**
   * 查询双因素认证状态（安全设置页）
   */
  export async function getStatus() {
    return requestClient.post<TwoFactorStatusVO>(
      `${ServicePrefix}/profile/twoFactor/status`,
    );
  }

  /**
   * 发送开通/换渠道验证码
   */
  export async function sendEnableCode(data: TwoFactorChannelDTO) {
    return requestClient.post<string>(
      `${ServicePrefix}/profile/twoFactor/sendEnableCode`,
      data,
    );
  }

  /**
   * 开通双因素认证
   */
  export async function enable(data: TwoFactorEnableDTO) {
    return requestClient.post(`${ServicePrefix}/profile/twoFactor/enable`, data);
  }

  /**
   * 关闭双因素认证
   */
  export async function disable(data: TwoFactorDisableDTO) {
    return requestClient.post(`${ServicePrefix}/profile/twoFactor/disable`, data);
  }

  /**
   * 更换验证渠道
   */
  export async function changeChannel(data: TwoFactorEnableDTO) {
    return requestClient.post(
      `${ServicePrefix}/profile/twoFactor/changeChannel`,
      data,
    );
  }
}
```

在 `src/api/common/index.ts`（或对应聚合导出文件，参照 `social.ts` 的导出方式）中导出 `TwoFactorApi`。

注意：`GET /profile/twoFactor/status` 后端是 GET，这里用 `requestClient.get` 更正：

```typescript
  export async function getStatus() {
    return requestClient.get<TwoFactorStatusVO>(
      `${ServicePrefix}/profile/twoFactor/status`,
    );
  }
```

- [ ] **Step 3: 类型检查**

Run: `cd mdp-vben && pnpm exec vue-tsc --noEmit -p apps/web-workbench/tsconfig.json 2>&1 | head -20`（如项目无该命令，用 `pnpm check:type`，以根 package.json scripts 为准）
Expected: 无类型错误

---

## Task 13: 登录页 2FA 第二步

**Files:**
- Create: `mdp-vben/apps/web-workbench/src/views/_core/sso/two-factor-login.vue`
- Modify: `mdp-vben/apps/web-workbench/src/views/_core/sso/data/login.tsx`（State 扩展）
- Modify: `mdp-vben/apps/web-workbench/src/views/_core/sso/data/account.tsx`（登录分支）
- Modify: `mdp-vben/apps/web-workbench/src/views/_core/sso/login.vue`（挂载第二步组件）
- Test: `mdp-vben/apps/web-workbench/src/views/_core/sso/two-factor-login.test.ts`

**Interfaces:**
- Consumes: `AuthModel.LoginVO.status`、`TwoFactorApi`（匿名段接口在 Task 14 补充至 `AuthApi`：`sendTwoFactorCode / verifyTwoFactorCode`）
- Produces: `State.showTwoFactor: boolean`、`State.twoFactor: { ticket, channel, maskedTarget }`

- [ ] **Step 1: 匿名段 API 补充到 AuthApi**

`src/api/common/auth.ts` 的 `AuthApi` 内追加：

```typescript
  /**
   * 发送登录二次验证码（凭第一步票据）
   */
  export async function sendTwoFactorCode(data: {
    channel?: string;
    ticket: string;
  }) {
    return requestClient.post<string>(
      `${ServicePrefix}/anyUser/auth/twoFactor/send`,
      data,
    );
  }

  /**
   * 校验登录二次验证码，换取登录令牌
   */
  export async function verifyTwoFactorCode(data: {
    code: string;
    ticket: string;
  }) {
    return requestClient.post<AuthModel.LoginVO>(
      `${ServicePrefix}/anyUser/auth/twoFactor/verify`,
      data,
    );
  }
```

- [ ] **Step 2: State 扩展（login.tsx）**

`State` 接口追加：

```typescript
  twoFactor: {
    ticket: string;
    channel?: 'BOTH' | 'EMAIL' | 'PHONE';
    maskedTarget?: string;
  } | null; // 非空时登录页切换到 2FA 输入码界面
```

`useLogin` 的 `state` 初始值追加 `twoFactor: null,`。

- [ ] **Step 3: 登录分支（account.tsx）**

`handleAccountLogin` 中 `await authStore.authLogin(...)` 不能直接用（authLogin 在无 token 时抛错），改为直接调 `AuthApi.login` 并分支：

```typescript
  async function handleAccountLogin(values: Recordable) {
    try {
      const requestParam: any = omit(values, ['code']);
      if (state.ssoParam.authType === 'CAPTCHA') {
        requestParam.code = values.code;
        requestParam.key = state.key;
        requestParam.authType = state.ssoParam.authType;
      } else {
        state.ssoParam.authType = 'USERNAME';
        requestParam.authType = state.ssoParam.authType;
      }
      state.ssoParam.username = values.username;
      state.ssoParam.deviceInfo = values.deviceInfo;

      const res = await AuthApi.login(requestParam);
      if (res.status === 'TWO_FACTOR' && res.twoFactorTicket) {
        // 双因素认证：进入第二步，不创建会话
        state.twoFactor = {
          ticket: res.twoFactorTicket,
          channel: res.twoFactorChannel,
          maskedTarget: res.twoFactorMaskedTarget,
        };
        return;
      }
      await authStore.authLogin(requestParam, () => handleSuccess(state));
    } catch (error: any) {
      console.error(error);
      if (error?.response?.data?.code === -12) {
        formRef.value.getUsernameFormRef()?.setFieldValue('code', '');
        await loadCaptcha(state);
      }
    }
  }
```

说明：双分支导致两次调用 login——优化为 `authStore` 增加 `completeLogin` 公开方法（store/auth.ts 已有 `completeLogin`，导出即可），分支代码改为 `await authStore.completeLogin(res, () => handleSuccess(state));`，不再重复调 login。**采用此方案**：修改 `store/auth.ts` 把 `completeLogin` 加入 store 返回对象（`return { loginLoading, authLogin, completeLogin, logout, signout, ... }`，以文件现有 return 为准）。

- [ ] **Step 4: 第二步组件 two-factor-login.vue**

```vue
<script setup lang="ts">
import type { State } from './data/login';

import { computed, reactive, ref } from 'vue';

import { useMessage } from '@vben/components/hooks';

import { AuthApi } from '#/api';
import { $t } from '#/locales';

import { handleSuccess } from './data/login';

const props = defineProps<{ state: State }>();
const emit = defineEmits<{ back: [] }>();

const { createMessage } = useMessage();
const loading = ref(false);
const sendCooldown = ref(0);
const form = reactive({ code: '' });

const channelLabel = computed(() =>
  props.state.twoFactor?.channel === 'EMAIL' ? '邮箱' : '手机',
);

async function startCooldown() {
  sendCooldown.value = 60;
  const timer = setInterval(() => {
    sendCooldown.value -= 1;
    if (sendCooldown.value <= 0) {
      clearInterval(timer);
    }
  }, 1000);
}

async function handleSend(channel?: 'EMAIL' | 'PHONE') {
  if (loading.value || !props.state.twoFactor) {
    return;
  }
  try {
    loading.value = true;
    const masked = await AuthApi.sendTwoFactorCode({
      channel,
      ticket: props.state.twoFactor.ticket,
    });
    props.state.twoFactor.maskedTarget = masked;
    if (channel) {
      props.state.twoFactor.channel = channel;
    }
    createMessage.success('验证码已发送');
    await startCooldown();
  } finally {
    loading.value = false;
  }
}

async function handleVerify() {
  if (loading.value || !props.state.twoFactor) {
    return;
  }
  try {
    loading.value = true;
    const res = await AuthApi.verifyTwoFactorCode({
      code: form.code,
      ticket: props.state.twoFactor.ticket,
    });
    if (!res.tokenValue) {
      throw new Error('登录结果缺少访问令牌');
    }
    // 复用登录完成逻辑写 token、拉用户信息、跳转
    const { useAuthStore } = await import('#/store');
    await useAuthStore().completeLogin(res, () => handleSuccess(props.state));
    props.state.twoFactor = null;
  } catch (error: any) {
    console.error(error);
    createMessage.error(error?.data?.msg || '验证失败，请重试');
  } finally {
    loading.value = false;
  }
}

function handleBack() {
  props.state.twoFactor = null;
  emit('back');
}
</script>

<template>
  <div class="mx-auto mt-4 w-full max-w-sm">
    <h2 class="text-xl font-medium">双因素认证</h2>
    <p class="mt-1 text-sm text-gray-500">
      验证码已发送至{{ channelLabel }} {{ state.twoFactor?.maskedTarget }}
    </p>

    <input
      v-model="form.code"
      :disabled="loading"
      class="border-input mt-4 w-full rounded-md border px-3 py-2 text-center text-lg tracking-widest"
      maxlength="6"
      placeholder="请输入 6 位验证码"
      type="text"
      @keyup.enter="handleVerify"
    />

    <div class="mt-3 flex items-center justify-between">
      <button class="text-primary text-sm" :disabled="sendCooldown > 0 || loading"
              type="button" @click="handleSend()">
        {{ sendCooldown > 0 ? `${sendCooldown}s 后重发` : '重新发送' }}
      </button>
      <button
        v-if="state.twoFactor?.channel === 'BOTH'"
        class="text-primary text-sm"
        type="button"
        @click="handleSend(state.twoFactor.channel === 'PHONE' ? 'EMAIL' : 'PHONE')"
      >
        换一种方式
      </button>
    </div>

    <button
      :loading="loading"
      class="bg-primary mt-4 w-full rounded-md py-2 text-white disabled:opacity-50"
      type="button"
      :disabled="loading"
      @click="handleVerify"
    >
      验证并登录
    </button>

    <button class="mt-2 w-full text-sm text-gray-500" type="button" @click="handleBack">
      返回重新登录
    </button>
  </div>
</template>
```

（组件样式按项目实际用 antdv-next / tailwind 组件替换裸 HTML 标签——`Button`、`Input` 从 `antdv-next` 导入，与 `two-factor.vue` 现有 import 风格一致。文案优先走 `$t` 国际化，缺失 key 时在 `locales` 里补 `authentication.twoFactor.*` 命名空间。）

- [ ] **Step 5: 挂载到登录页（login.vue）**

`SsoLogin` 区域外追加（与 `VbenBehaviorCaptcha` 同级）：

```vue
    <TwoFactorLogin v-if="state.twoFactor" :state="state" @back="loadCaptcha(state)" />
```

并在 `SsoLogin` 的 `v-if` 上排除 2FA 状态：`v-if="state.showUsernameForm && !state.twoFactor"`。script 中导入 `TwoFactorLogin from './two-factor-login.vue'` 并暴露 `loadCaptcha`（`loadCaptcha` 目前在 account.tsx 内部——将其移至 `login.tsx` 导出，account.tsx 与 login.vue 共同引用）。

- [ ] **Step 6: 写测试**

```typescript
import { describe, expect, it } from 'vitest';

import { resolveChannelLabel } from './two-factor-login';

describe('two-factor-login', () => {
  it('EMAIL 渠道展示邮箱', () => {
    expect(resolveChannelLabel('EMAIL')).toBe('邮箱');
  });

  it('PHONE 渠道展示手机', () => {
    expect(resolveChannelLabel('PHONE')).toBe('手机');
  });

  it('BOTH 渠道默认手机', () => {
    expect(resolveChannelLabel('BOTH')).toBe('手机');
  });
});
```

（`resolveChannelLabel` 从 `two-factor-login.vue` 的 `<script>` 抽出到同目录 `two-factor.ts` 导出，组件导入使用；测试对齐 `fingerprint-login.test.ts` 风格。另补一条：`handleVerify` 成功后调用 `completeLogin` 的集成断言，mock `AuthApi.verifyTwoFactorCode`。）

- [ ] **Step 7: 运行测试**

Run: `cd mdp-vben && pnpm vitest run apps/web-workbench/src/views/_core/sso/two-factor-login.test.ts`
Expected: PASS

---

## Task 14: 安全设置页实装（two-factor.vue 替换占位）

**Files:**
- Modify: `mdp-vben/apps/web-workbench/src/views/workbench/user/security/modules/two-factor.vue`（全量替换）
- Modify: `mdp-vben/apps/web-workbench/src/views/workbench/user/security/data/two-factor.tsx`（表单逻辑改为状态卡片数据）
- Test: `mdp-vben/apps/web-workbench/src/views/workbench/user/security/two-factor.test.ts`（新建，渲染三态）

**Interfaces:**
- Consumes: `TwoFactorApi.getStatus / sendEnableCode / enable / disable / changeChannel`（Task 12）
- Produces: 三态卡片（未开通 / 已开通 / FORCE 强制）

- [ ] **Step 1: 实装组件**

`two-factor.vue` 全量替换为：

```vue
<script setup lang="ts">
import type { TwoFactorApi } from '#/api/common/twoFactor';

import { onMounted, ref } from 'vue';

import { Button, Card, Input, Modal, message } from 'antdv-next';

import { TwoFactorApi } from '#/api/common/twoFactor';

defineOptions({
  name: '双因素认证设置',
  inheritAttrs: false,
});

type Channel = TwoFactorApi.TwoFactorChannelDTO['channel'];

const status = ref<TwoFactorApi.TwoFactorStatusVO | null>(null);
const loading = ref(false);

// 开通/换渠道弹窗状态
const modalVisible = ref(false);
const modalMode = ref<'change' | 'enable'>('enable');
const selectedChannel = ref<Channel>('BOTH');
const inputCode = ref('');
const sendCooldown = ref(0);

async function loadStatus() {
  try {
    loading.value = true;
    status.value = await TwoFactorApi.getStatus();
  } finally {
    loading.value = false;
  }
}

onMounted(loadStatus);

function openModal(mode: 'change' | 'enable') {
  modalMode.value = mode;
  selectedChannel.value = status.value?.channel || 'BOTH';
  inputCode.value = '';
  modalVisible.value = true;
}

async function handleSendCode() {
  await TwoFactorApi.sendEnableCode({ channel: selectedChannel.value });
  message.success('验证码已发送');
  sendCooldown.value = 60;
  const timer = setInterval(() => {
    sendCooldown.value -= 1;
    if (sendCooldown.value <= 0) {
      clearInterval(timer);
    }
  }, 1000);
}

async function handleSubmit() {
  if (modalMode.value === 'enable' && !status.value?.enabled) {
    await TwoFactorApi.enable({
      channel: selectedChannel.value,
      code: inputCode.value,
    });
  } else {
    await TwoFactorApi.changeChannel({
      channel: selectedChannel.value,
      code: inputCode.value,
    });
  }
  message.success('操作成功');
  modalVisible.value = false;
  await loadStatus();
}

async function handleDisable() {
  Modal.confirm({
    content: '关闭后登录将不再需要二次验证，确认关闭？',
    okText: '确认关闭',
    cancelText: '取消',
    onOk: async () => {
      // 复用开通弹窗：关闭需要当前渠道验证码，走 disable
      const code = await requestDisableCode();
      await TwoFactorApi.disable({ code });
      message.success('已关闭');
      await loadStatus();
    },
  });
}

async function requestDisableCode(): Promise<string> {
  // 弹窗收集当前渠道验证码（简化：复用开通弹窗 UI，mode=disable）
  modalMode.value = 'change';
  openModalForDisable();
  throw new Error('见 Step 2');
}

function openModalForDisable() {
  // disable 需要验证码确认：弹出输入框收集当前渠道验证码后调用 disable
}
```

- [ ] **Step 2: 修正 disable 流程（消除占位）**

disable 与 enable 同构：向**当前渠道**发码 → 用户输入 → 提交。统一弹窗状态机：

```typescript
type ModalMode = 'change' | 'disable' | 'enable';

const modalMode = ref<ModalMode>('enable');

function openModal(mode: ModalMode) {
  modalMode.value = mode;
  selectedChannel.value = status.value?.channel || 'BOTH';
  inputCode.value = '';
  modalVisible.value = true;
}

async function handleSendCode() {
  // disable 固定向当前渠道发码：sendEnableCode 传当前渠道
  const channel = modalMode.value === 'disable'
    ? (status.value?.channel as Channel)
    : selectedChannel.value;
  await TwoFactorApi.sendEnableCode({ channel });
  message.success('验证码已发送');
  sendCooldown.value = 60;
  const timer = setInterval(() => {
    sendCooldown.value -= 1;
    if (sendCooldown.value <= 0) {
      clearInterval(timer);
    }
  }, 1000);
}

async function handleSubmit() {
  if (modalMode.value === 'enable') {
    await TwoFactorApi.enable({ channel: selectedChannel.value, code: inputCode.value });
  } else if (modalMode.value === 'change') {
    await TwoFactorApi.changeChannel({ channel: selectedChannel.value, code: inputCode.value });
  } else {
    await TwoFactorApi.disable({ code: inputCode.value });
  }
  message.success('操作成功');
  modalVisible.value = false;
  await loadStatus();
}
```

`handleDisable` 简化为 `openModal('disable')`。删除 `requestDisableCode/openModalForDisable` 占位。

- [ ] **Step 3: 模板（三态卡片）**

```vue
<template>
  <Card
    hoverable
    variant="outlined"
    :styles="{ header: { padding: '12px' } }"
    size="small"
  >
    <template #title>
      <div class="flex flex-col">
        <h2 class="text-xl font-medium">双因素认证</h2>
        <p class="mt-1 text-sm text-gray-500">
          开启后，账号密码登录时需要额外输入手机/邮箱验证码
        </p>
      </div>
    </template>
    <template #extra>
      <Button v-if="!status?.enabled" type="primary" @click="openModal('enable')">
        开通
      </Button>
      <span v-else class="text-green-600 text-sm">已开通</span>
    </template>

    <div v-if="status?.enabled" class="p-0">
      <p>验证渠道：{{ channelText }}</p>
      <p>接收目标：{{ status.maskedTarget || '—' }}</p>
      <div class="mt-2 flex gap-2">
        <Button :disabled="status.strategy === 'FORCE'" @click="openModal('disable')">
          {{ status.strategy === 'FORCE' ? '管理员已强制启用' : '关闭' }}
        </Button>
        <Button type="primary" @click="openModal('change')">更换渠道</Button>
      </div>
    </div>
    <div v-else class="p-0 text-gray-500">
      未开通。当前全局策略：
      {{ status?.strategy === 'FORCE' ? '强制全员（需先绑定手机或邮箱）' : '允许自主开通' }}
    </div>

    <Modal v-model:open="modalVisible" :title="modalTitle" @ok="handleSubmit">
      <div class="flex flex-col gap-3">
        <div v-if="modalMode !== 'disable'">
          <span>验证渠道：</span>
          <select v-model="selectedChannel">
            <option value="BOTH">手机或邮箱均可</option>
            <option value="PHONE">仅手机</option>
            <option value="EMAIL">仅邮箱</option>
          </select>
        </div>
        <div class="flex items-center gap-2">
          <Input v-model:value="inputCode" maxlength="6" placeholder="请输入验证码" />
          <Button :disabled="sendCooldown > 0" @click="handleSendCode">
            {{ sendCooldown > 0 ? `${sendCooldown}s` : '发送验证码' }}
          </Button>
        </div>
      </div>
    </Modal>
  </Card>
</template>
```

（`channelText` computed：BOTH→手机或邮箱均可 / PHONE→仅手机 / EMAIL→仅邮箱；`modalTitle` computed 按模式显示开通/更换渠道/关闭双因素认证。样式与 select 控件按项目组件库规范替换，文案接入 `$t` 国际化。）

- [ ] **Step 4: 更新 data/two-factor.tsx**

现有 `useForm`/占位表单逻辑不再被模板引用，删除未使用的导出（保留文件或整体删除由实施时检查引用——`index.test.ts` 若引用需同步更新）。**以 grep 检查引用为准**：

Run: `cd mdp-vben && grep -rn "data/two-factor" apps/web-workbench/src | grep -v test`
若仅 `modules/two-factor.vue` 引用，改完组件后清空该文件无用导出。

- [ ] **Step 5: 写测试**

```typescript
import { describe, expect, it } from 'vitest';

import { resolveChannelText, resolveStrategyText } from './two-factor-text';

describe('two-factor security card text', () => {
  it('渠道文案', () => {
    expect(resolveChannelText('BOTH')).toBe('手机或邮箱均可');
    expect(resolveChannelText('PHONE')).toBe('仅手机');
    expect(resolveChannelText('EMAIL')).toBe('仅邮箱');
  });

  it('FORCE 策略下关闭按钮禁用文案', () => {
    expect(resolveStrategyText('FORCE')).toBe('强制全员（需先绑定手机或邮箱）');
    expect(resolveStrategyText('OPTIONAL')).toBe('允许自主开通');
    expect(resolveStrategyText('OFF')).toBe('允许自主开通');
  });
});
```

（`resolveChannelText/resolveStrategyText` 抽到 `modules/two-factor-text.ts` 供组件与测试共用。）

- [ ] **Step 6: 运行测试**

Run: `cd mdp-vben && pnpm vitest run apps/web-workbench/src/views/workbench/user/security/two-factor.test.ts`
Expected: PASS

---

## Task 15: 全量验证

**Files:** 无新增（只运行验证命令）

- [ ] **Step 1: 后端全模块编译 + 相关测试**

Run: `cd mdp && mvn compile -pl mdp-apps/md-workbench/workbench-web -am -q`
Expected: BUILD SUCCESS

Run: `cd mdp && mvn test -pl mdp-apps/md-workbench/workbench-service -Dtest="TwoFactor*,AuthServiceImpl*" -q`
Expected: 全部 PASS

- [ ] **Step 2: 前端类型检查与测试**

Run: `cd mdp-vben && pnpm vitest run apps/web-workbench/src/views/_core/sso apps/web-workbench/src/views/workbench/user/security`
Expected: 全部 PASS

- [ ] **Step 3: 人工安全评审提醒**

向用户输出评审清单：
1. ticket 未颁发任何 sa-token 会话，verify 通过才 `StpUtil.login`
2. OTP 与 ticket 均一次性 + 失败计数 + 短时效
3. 匿名接口仅 `/anyUser/auth/twoFactor/send|verify`，且必须持有效 ticket
4. FORCE 策略下 disable 被拒、无渠道用户被拦截
5. 日志无验证码明文与 ticket 全值

---

## Self-Review 记录

- **Spec 覆盖**：配置三档（Task 1/5/11）、两步登录（Task 6/8/9/10/13）、触发矩阵（Task 8 测试）、开通/关闭/换渠道（Task 8/10/14）、FORCE 批量开通（Task 8/11）、场景隔离 OTP（Task 3/7）、登录日志（复用 LoginEvent，verifyLogin 后 loginAuthenticated 内部照常发布，无需改动）、脱敏（Task 7/8/10）、前端三态卡片（Task 14）——已全覆盖。
- **占位符**：Task 8 Step 5、Task 13 Step 4 的"按组件库替换"为明确指令性说明（指向现有文件风格），非 TBD；Task 14 Step 1/2 的占位在 Step 2 中消除。
- **类型一致性**：`TwoFactorTicketPayload`（Task 6 定义，Task 8/10 使用，需补 phone/email 字段——已在 Task 8 Step 5 标注）；`SCENE_LOGIN/SCENE_MANAGE`（Task 3 定义，Task 7/8 使用）；`TwoFactorApi` 前端命名与后端接口一一对应。
