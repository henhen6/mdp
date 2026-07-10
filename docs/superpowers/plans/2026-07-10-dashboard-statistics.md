# 统计大屏功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 MDP 平台实现数据大屏统计功能，包含7个Tab页面，展示用户、组织、登录、消息、文件、开放平台、接口监控等统计数据。

**Architecture:** 按业务模块划分，在各模块的 web 层新增 dashboard 包，创建统计 Controller 和 Service。前端通过调用各模块的统计接口聚合数据，后端不跨模块查询数据库。

**Tech Stack:** SpringBoot 3.x, MyBatis-Flex, Java 17

## Global Constraints

- 后端不跨模块调用（包括RPC），各模块只统计自己负责的表
- 接口路径不包含模块前缀（如 `/console`、`/open`、`/workbench`）
- 使用 TDD 方式开发，先写测试再写实现
- 代码覆盖率 >= 80%
- 遵循现有代码风格和命名规范

---

## 文件结构

### md-console 模块新增文件
```
mdp-apps/md-console/
├── console-web/src/main/java/top/mddata/console/controller/
│   └── dashboard/
│       ├── DashboardConsoleController.java    # 系统概览统计接口
│       ├── DashboardUserController.java       # 用户与组织统计接口
│       ├── DashboardMessageController.java    # 消息通知统计接口
│       ├── DashboardFileController.java       # 文件存储统计接口
│       └── DashboardMonitorController.java    # 接口监控统计接口
├── console-service/src/main/java/top/mddata/console/service/
│   └── dashboard/
│       ├── DashboardConsoleService.java
│       ├── DashboardUserService.java
│       ├── DashboardMessageService.java
│       ├── DashboardFileService.java
│       ├── DashboardMonitorService.java
│       └── impl/
│           ├── DashboardConsoleServiceImpl.java
│           ├── DashboardUserServiceImpl.java
│           ├── DashboardMessageServiceImpl.java
│           ├── DashboardFileServiceImpl.java
│           └── DashboardMonitorServiceImpl.java
├── console-pojo/src/main/java/top/mddata/console/vo/dashboard/
│   ├── OverviewConsoleVo.java
│   ├── OverviewUserVo.java
│   ├── TrendVo.java
│   ├── RankVo.java
│   ├── DistributionVo.java
│   ├── OverviewMessageVo.java
│   ├── OverviewFileVo.java
│   └── OverviewMonitorVo.java
└── console-server/src/test/java/top/mddata/console/service/dashboard/
    ├── DashboardConsoleServiceTest.java
    ├── DashboardUserServiceTest.java
    ├── DashboardMessageServiceTest.java
    ├── DashboardFileServiceTest.java
    └── DashboardMonitorServiceTest.java
```

### md-open 模块新增文件
```
mdp-apps/md-open/
├── open-web/src/main/java/top/mddata/open/controller/
│   └── dashboard/
│       ├── DashboardOpenController.java       # 开放平台概览统计接口
│       └── DashboardEventController.java      # 事件统计接口
├── open-service/src/main/java/top/mddata/open/service/
│   └── dashboard/
│       ├── DashboardOpenService.java
│       ├── DashboardEventService.java
│       └── impl/
│           ├── DashboardOpenServiceImpl.java
│           └── DashboardEventServiceImpl.java
├── open-pojo/src/main/java/top/mddata/open/vo/dashboard/
│   ├── OverviewOpenVo.java
│   ├── EventTriggerStatVo.java
│   └── EventPushStatVo.java
└── open-server/src/test/java/top/mddata/open/service/dashboard/
    ├── DashboardOpenServiceTest.java
    └── DashboardEventServiceTest.java
```

### md-workbench 模块新增文件
```
mdp-apps/md-workbench/
├── workbench-web/src/main/java/top/mddata/workbench/controller/
│   └── dashboard/
│       ├── DashboardWorkbenchController.java  # 工作台概览统计接口
│       └── DashboardLoginController.java      # 登录安全统计接口
├── workbench-service/src/main/java/top/mddata/workbench/service/
│   └── dashboard/
│       ├── DashboardWorkbenchService.java
│       ├── DashboardLoginService.java
│       └── impl/
│           ├── DashboardWorkbenchServiceImpl.java
│           └── DashboardLoginServiceImpl.java
├── workbench-pojo/src/main/java/top/mddata/workbench/vo/dashboard/
│   ├── OverviewWorkbenchVo.java
│   ├── LoginRegionVo.java
│   ├── LoginDailyStatVo.java
│   └── LoginRankVo.java
└── workbench-server/src/test/java/top/mddata/workbench/service/dashboard/
    ├── DashboardWorkbenchServiceTest.java
    └── DashboardLoginServiceTest.java
```

---

## Task 1: 创建通用 VO 类

**Files:**
- Create: `mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/vo/dashboard/TrendVo.java`
- Create: `mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/vo/dashboard/RankVo.java`
- Create: `mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/vo/dashboard/DistributionVo.java`

**Interfaces:**
- Produces: 通用 VO 类供其他统计接口使用

- [ ] **Step 1: 创建 TrendVo 类**

```java
package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 趋势数据 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "趋势数据")
public class TrendVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "日期")
    private String date;

    @Schema(description = "数值")
    private Long value;
}
```

- [ ] **Step 2: 创建 RankVo 类**

```java
package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 排行榜数据 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "排行榜数据")
public class RankVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "数值")
    private Long value;
}
```

- [ ] **Step 3: 创建 DistributionVo 类**

```java
package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分布数据 VO
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "分布数据")
public class DistributionVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "数量")
    private Long count;

    @Schema(description = "占比")
    private Double percent;
}
```

---

## Task 2: 系统概览统计 - md-console 部分

**Files:**
- Create: `mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/vo/dashboard/OverviewConsoleVo.java`
- Create: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/dashboard/DashboardConsoleService.java`
- Create: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/dashboard/impl/DashboardConsoleServiceImpl.java`
- Create: `mdp-apps/md-console/console-web/src/main/java/top/mddata/console/controller/dashboard/DashboardConsoleController.java`
- Test: `mdp-apps/md-console/console-server/src/test/java/top/mddata/console/service/dashboard/DashboardConsoleServiceTest.java`

**Interfaces:**
- Consumes: UserService, OrgService, NoticeService, FileService
- Produces: `GET /dashboard/overview/console` 返回 OverviewConsoleVo

- [ ] **Step 1: 创建 OverviewConsoleVo 类**

```java
package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统概览统计 VO (console部分)
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "系统概览统计(console部分)")
public class OverviewConsoleVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户总数")
    private Long userCount;

    @Schema(description = "组织总数")
    private Long orgCount;

    @Schema(description = "待办通知数")
    private Long todoNoticeCount;

    @Schema(description = "公告通知数")
    private Long announcementNoticeCount;

    @Schema(description = "预警通知数")
    private Long warningNoticeCount;

    @Schema(description = "文件总数")
    private Long fileCount;

    @Schema(description = "文件总容量(字节)")
    private Long fileTotalSize;
}
```

- [ ] **Step 2: 编写测试**

```java
package top.mddata.console.service.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.console.vo.dashboard.OverviewConsoleVo;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DashboardConsoleService 测试
 *
 * @author henhen6
 * @since 2026-07-10
 */
@SpringBootTest
class DashboardConsoleServiceTest {

    @Autowired
    private DashboardConsoleService dashboardConsoleService;

    @Test
    void getOverviewConsole() {
        OverviewConsoleVo result = dashboardConsoleService.getOverviewConsole();
        
        assertNotNull(result);
        assertNotNull(result.getUserCount());
        assertNotNull(result.getOrgCount());
        assertNotNull(result.getFileCount());
        assertTrue(result.getUserCount() >= 0);
        assertTrue(result.getOrgCount() >= 0);
        assertTrue(result.getFileCount() >= 0);
    }
}
```

- [ ] **Step 3: 运行测试验证失败**

```bash
cd mdp-apps/md-console/console-service
mvn test -Dtest=DashboardConsoleServiceTest -pl console-service
```
Expected: FAIL with "DashboardConsoleService not found"

- [ ] **Step 4: 创建 DashboardConsoleService 接口**

```java
package top.mddata.console.service.dashboard;

import top.mddata.console.vo.dashboard.OverviewConsoleVo;

/**
 * 系统概览统计 服务层
 *
 * @author henhen6
 * @since 2026-07-10
 */
public interface DashboardConsoleService {

    /**
     * 获取系统概览统计(console部分)
     *
     * @return 概览统计
     */
    OverviewConsoleVo getOverviewConsole();
}
```

- [ ] **Step 5: 创建 DashboardConsoleServiceImpl 实现**

```java
package top.mddata.console.service.dashboard.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.common.entity.Notice;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.User;
import top.mddata.common.mapper.FileMapper;
import top.mddata.common.mapper.NoticeMapper;
import top.mddata.common.mapper.OrgMapper;
import top.mddata.common.mapper.UserMapper;
import top.mddata.console.service.dashboard.DashboardConsoleService;
import top.mddata.console.vo.dashboard.OverviewConsoleVo;

/**
 * 系统概览统计 服务层实现
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardConsoleServiceImpl implements DashboardConsoleService {

    private final UserMapper userMapper;
    private final OrgMapper orgMapper;
    private final NoticeMapper noticeMapper;
    private final FileMapper fileMapper;

    @Override
    public OverviewConsoleVo getOverviewConsole() {
        OverviewConsoleVo vo = new OverviewConsoleVo();
        
        // 统计用户总数 (未删除且启用)
        QueryWrapper userWrapper = QueryWrapper.create()
                .eq(User::getDeletedAt, 0L)
                .eq(User::getState, true);
        vo.setUserCount(userMapper.selectCountByQuery(userWrapper));
        
        // 统计组织总数 (未删除且启用)
        QueryWrapper orgWrapper = QueryWrapper.create()
                .eq(Org::getDeletedAt, 0L)
                .eq(Org::getState, true);
        vo.setOrgCount(orgMapper.selectCountByQuery(orgWrapper));
        
        // 统计今日通知数 (按分类)
        String today = java.time.LocalDate.now().toString();
        QueryWrapper noticeWrapper = QueryWrapper.create()
                .like(Notice::getCreatedAt, today);
        
        // 待办通知
        QueryWrapper todoWrapper = noticeWrapper.clone()
                .eq(Notice::getMsgCategory, 1);
        vo.setTodoNoticeCount(noticeMapper.selectCountByQuery(todoWrapper));
        
        // 公告通知
        QueryWrapper announcementWrapper = noticeWrapper.clone()
                .eq(Notice::getMsgCategory, 2);
        vo.setAnnouncementNoticeCount(noticeMapper.selectCountByQuery(announcementWrapper));
        
        // 预警通知
        QueryWrapper warningWrapper = noticeWrapper.clone()
                .eq(Notice::getMsgCategory, 3);
        vo.setWarningNoticeCount(noticeMapper.selectCountByQuery(warningWrapper));
        
        // 统计文件
        vo.setFileCount(fileMapper.selectCountByQuery(QueryWrapper.create()));
        
        // 统计文件总容量
        // 注意: MyBatis-Flex 需要使用 aggregate 查询
        vo.setFileTotalSize(0L); // TODO: 实现 sum 查询
        
        return vo;
    }
}
```

- [ ] **Step 6: 运行测试验证通过**

```bash
cd mdp-apps/md-console/console-service
mvn test -Dtest=DashboardConsoleServiceTest -pl console-service
```
Expected: PASS

- [ ] **Step 7: 创建 DashboardConsoleController**

```java
package top.mddata.console.controller.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.console.service.dashboard.DashboardConsoleService;
import top.mddata.console.vo.dashboard.OverviewConsoleVo;

/**
 * 系统概览统计 控制层
 *
 * @author henhen6
 * @since 2026-07-10
 */
@RestController
@Tag(name = "大屏统计-系统概览(console)")
@RequestMapping("/dashboard/overview")
@RequiredArgsConstructor
public class DashboardConsoleController {

    private final DashboardConsoleService dashboardConsoleService;

    /**
     * 获取系统概览统计(console部分)
     *
     * @return 概览统计
     */
    @GetMapping("/console")
    @Operation(summary = "系统概览统计(console部分)", description = "获取用户数、组织数、通知数、文件统计等")
    @RequestLog(value = "查询系统概览统计(console)", response = false)
    public R<OverviewConsoleVo> getOverviewConsole() {
        return R.success(dashboardConsoleService.getOverviewConsole());
    }
}
```

---

## Task 3: 系统概览统计 - md-open 部分

**Files:**
- Create: `mdp-apps/md-open/open-pojo/src/main/java/top/mddata/open/vo/dashboard/OverviewOpenVo.java`
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/dashboard/DashboardOpenService.java`
- Create: `mdp-apps/md-open/open-service/src/main/java/top/mddata/open/service/dashboard/impl/DashboardOpenServiceImpl.java`
- Create: `mdp-apps/md-open/open-web/src/main/java/top/mddata/open/controller/dashboard/DashboardOpenController.java`
- Test: `mdp-apps/md-open/open-server/src/test/java/top/mddata/open/service/dashboard/DashboardOpenServiceTest.java`

**Interfaces:**
- Consumes: AppMapper, ApiCallLogMapper
- Produces: `GET /dashboard/overview/open` 返回 OverviewOpenVo

- [ ] **Step 1: 创建 OverviewOpenVo 类**

```java
package top.mddata.open.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统概览统计 VO (open部分)
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "系统概览统计(open部分)")
public class OverviewOpenVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "应用总数")
    private Long appCount;

    @Schema(description = "今日API调用量")
    private Long todayApiCallCount;
}
```

- [ ] **Step 2: 编写测试**

```java
package top.mddata.open.service.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.open.vo.dashboard.OverviewOpenVo;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DashboardOpenService 测试
 *
 * @author henhen6
 * @since 2026-07-10
 */
@SpringBootTest
class DashboardOpenServiceTest {

    @Autowired
    private DashboardOpenService dashboardOpenService;

    @Test
    void getOverviewOpen() {
        OverviewOpenVo result = dashboardOpenService.getOverviewOpen();
        
        assertNotNull(result);
        assertNotNull(result.getAppCount());
        assertNotNull(result.getTodayApiCallCount());
        assertTrue(result.getAppCount() >= 0);
        assertTrue(result.getTodayApiCallCount() >= 0);
    }
}
```

- [ ] **Step 3: 运行测试验证失败**

```bash
cd mdp-apps/md-open/open-service
mvn test -Dtest=DashboardOpenServiceTest -pl open-service
```
Expected: FAIL with "DashboardOpenService not found"

- [ ] **Step 4: 创建 DashboardOpenService 接口**

```java
package top.mddata.open.service.dashboard;

import top.mddata.open.vo.dashboard.OverviewOpenVo;

/**
 * 系统概览统计 服务层 (open部分)
 *
 * @author henhen6
 * @since 2026-07-10
 */
public interface DashboardOpenService {

    /**
     * 获取系统概览统计(open部分)
     *
     * @return 概览统计
     */
    OverviewOpenVo getOverviewOpen();
}
```

- [ ] **Step 5: 创建 DashboardOpenServiceImpl 实现**

```java
package top.mddata.open.service.dashboard.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.common.entity.ApiCallLog;
import top.mddata.common.entity.App;
import top.mddata.common.mapper.ApiCallLogMapper;
import top.mddata.common.mapper.AppMapper;
import top.mddata.open.service.dashboard.DashboardOpenService;
import top.mddata.open.vo.dashboard.OverviewOpenVo;

import java.time.LocalDate;

/**
 * 系统概览统计 服务层实现 (open部分)
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardOpenServiceImpl implements DashboardOpenService {

    private final AppMapper appMapper;
    private final ApiCallLogMapper apiCallLogMapper;

    @Override
    public OverviewOpenVo getOverviewOpen() {
        OverviewOpenVo vo = new OverviewOpenVo();
        
        // 统计应用总数 (启用状态)
        QueryWrapper appWrapper = QueryWrapper.create()
                .eq(App::getState, true);
        vo.setAppCount(appMapper.selectCountByQuery(appWrapper));
        
        // 统计今日API调用量
        String today = LocalDate.now().toString();
        QueryWrapper callLogWrapper = QueryWrapper.create()
                .like(ApiCallLog::getCreatedAt, today);
        vo.setTodayApiCallCount(apiCallLogMapper.selectCountByQuery(callLogWrapper));
        
        return vo;
    }
}
```

- [ ] **Step 6: 运行测试验证通过**

```bash
cd mdp-apps/md-open/open-service
mvn test -Dtest=DashboardOpenServiceTest -pl open-service
```
Expected: PASS

- [ ] **Step 7: 创建 DashboardOpenController**

```java
package top.mddata.open.controller.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.open.service.dashboard.DashboardOpenService;
import top.mddata.open.vo.dashboard.OverviewOpenVo;

/**
 * 系统概览统计 控制层 (open部分)
 *
 * @author henhen6
 * @since 2026-07-10
 */
@RestController
@Tag(name = "大屏统计-系统概览(open)")
@RequestMapping("/dashboard/overview")
@RequiredArgsConstructor
public class DashboardOpenController {

    private final DashboardOpenService dashboardOpenService;

    /**
     * 获取系统概览统计(open部分)
     *
     * @return 概览统计
     */
    @GetMapping("/open")
    @Operation(summary = "系统概览统计(open部分)", description = "获取应用数、API调用量等")
    @RequestLog(value = "查询系统概览统计(open)", response = false)
    public R<OverviewOpenVo> getOverviewOpen() {
        return R.success(dashboardOpenService.getOverviewOpen());
    }
}
```

---

## Task 4: 系统概览统计 - md-workbench 部分

**Files:**
- Create: `mdp-apps/md-workbench/workbench-pojo/src/main/java/top/mddata/workbench/vo/dashboard/OverviewWorkbenchVo.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/dashboard/DashboardWorkbenchService.java`
- Create: `mdp-apps/md-workbench/workbench-service/src/main/java/top/mddata/workbench/service/dashboard/impl/DashboardWorkbenchServiceImpl.java`
- Create: `mdp-apps/md-workbench/workbench-web/src/main/java/top/mddata/workbench/controller/dashboard/DashboardWorkbenchController.java`
- Test: `mdp-apps/md-workbench/workbench-server/src/test/java/top/mddata/workbench/service/dashboard/DashboardWorkbenchServiceTest.java`

**Interfaces:**
- Consumes: LoginLogMapper
- Produces: `GET /dashboard/overview/workbench` 返回 OverviewWorkbenchVo

- [ ] **Step 1: 创建 OverviewWorkbenchVo 类**

```java
package top.mddata.workbench.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统概览统计 VO (workbench部分)
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Data
@Schema(description = "系统概览统计(workbench部分)")
public class OverviewWorkbenchVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "今日登录次数")
    private Long todayLoginCount;
}
```

- [ ] **Step 2: 编写测试**

```java
package top.mddata.workbench.service.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.workbench.vo.dashboard.OverviewWorkbenchVo;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DashboardWorkbenchService 测试
 *
 * @author henhen6
 * @since 2026-07-10
 */
@SpringBootTest
class DashboardWorkbenchServiceTest {

    @Autowired
    private DashboardWorkbenchService dashboardWorkbenchService;

    @Test
    void getOverviewWorkbench() {
        OverviewWorkbenchVo result = dashboardWorkbenchService.getOverviewWorkbench();
        
        assertNotNull(result);
        assertNotNull(result.getTodayLoginCount());
        assertTrue(result.getTodayLoginCount() >= 0);
    }
}
```

- [ ] **Step 3: 运行测试验证失败**

```bash
cd mdp-apps/md-workbench/workbench-service
mvn test -Dtest=DashboardWorkbenchServiceTest -pl workbench-service
```
Expected: FAIL with "DashboardWorkbenchService not found"

- [ ] **Step 4: 创建 DashboardWorkbenchService 接口**

```java
package top.mddata.workbench.service.dashboard;

import top.mddata.workbench.vo.dashboard.OverviewWorkbenchVo;

/**
 * 系统概览统计 服务层 (workbench部分)
 *
 * @author henhen6
 * @since 2026-07-10
 */
public interface DashboardWorkbenchService {

    /**
     * 获取系统概览统计(workbench部分)
     *
     * @return 概览统计
     */
    OverviewWorkbenchVo getOverviewWorkbench();
}
```

- [ ] **Step 5: 创建 DashboardWorkbenchServiceImpl 实现**

```java
package top.mddata.workbench.service.dashboard.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.common.entity.LoginLog;
import top.mddata.common.mapper.LoginLogMapper;
import top.mddata.workbench.service.dashboard.DashboardWorkbenchService;
import top.mddata.workbench.vo.dashboard.OverviewWorkbenchVo;

import java.time.LocalDate;

/**
 * 系统概览统计 服务层实现 (workbench部分)
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardWorkbenchServiceImpl implements DashboardWorkbenchService {

    private final LoginLogMapper loginLogMapper;

    @Override
    public OverviewWorkbenchVo getOverviewWorkbench() {
        OverviewWorkbenchVo vo = new OverviewWorkbenchVo();
        
        // 统计今日登录次数 (登录成功)
        String today = LocalDate.now().toString();
        QueryWrapper wrapper = QueryWrapper.create()
                .eq(LoginLog::getLoginDate, today)
                .eq(LoginLog::getStatus, "01");
        vo.setTodayLoginCount(loginLogMapper.selectCountByQuery(wrapper));
        
        return vo;
    }
}
```

- [ ] **Step 6: 运行测试验证通过**

```bash
cd mdp-apps/md-workbench/workbench-service
mvn test -Dtest=DashboardWorkbenchServiceTest -pl workbench-service
```
Expected: PASS

- [ ] **Step 7: 创建 DashboardWorkbenchController**

```java
package top.mddata.workbench.controller.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.workbench.service.dashboard.DashboardWorkbenchService;
import top.mddata.workbench.vo.dashboard.OverviewWorkbenchVo;

/**
 * 系统概览统计 控制层 (workbench部分)
 *
 * @author henhen6
 * @since 2026-07-10
 */
@RestController
@Tag(name = "大屏统计-系统概览(workbench)")
@RequestMapping("/dashboard/overview")
@RequiredArgsConstructor
public class DashboardWorkbenchController {

    private final DashboardWorkbenchService dashboardWorkbenchService;

    /**
     * 获取系统概览统计(workbench部分)
     *
     * @return 概览统计
     */
    @GetMapping("/workbench")
    @Operation(summary = "系统概览统计(workbench部分)", description = "获取今日登录次数等")
    @RequestLog(value = "查询系统概览统计(workbench)", response = false)
    public R<OverviewWorkbenchVo> getOverviewWorkbench() {
        return R.success(dashboardWorkbenchService.getOverviewWorkbench());
    }
}
```

- [ ] **Step 8: Commit**
---

由于实现计划内容较长，我将创建一个续篇来包含剩余的 Task（用户与组织、登录与安全、消息通知、文件存储、开放平台事件统计、接口监控）。
