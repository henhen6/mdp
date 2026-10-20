package top.mddata.open.service.app;

import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import top.mddata.base.exception.ArgumentException;
import top.mddata.base.util.ContextUtil;
import top.mddata.common.enumeration.AuditStatusEnum;
import top.mddata.open.dto.admin.AppScopeApplyDto;
import top.mddata.open.dto.admin.AppScopeApplyReviewDto;
import top.mddata.open.entity.admin.App;
import top.mddata.open.entity.admin.AppGroupRel;
import top.mddata.open.entity.admin.AppScopeApply;
import top.mddata.open.entity.admin.ScopeGroup;
import top.mddata.open.service.oauth.ScopeGroupService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 权限组申请 Service 测试。
 *
 * @author henhen6
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "mdp.log.ipv4.enabled=false",
                "mdp.log.ipv6.enabled=false"
        })
class AppScopeApplyServiceImplTest {

    @Resource
    private AppScopeApplyService appScopeApplyService;

    @Resource
    private AppGroupRelService appGroupRelService;

    @Resource
    private AppService appService;

    @Resource
    private ScopeGroupService scopeGroupService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    /** 测试用用户ID */
    private static final Long TEST_USER_ID = 1L;
    /** 测试用应用ID */
    private Long testAppId;
    /** 测试用权限组ID（启用，应用未拥有） */
    private Long testGroupId;
    /** 测试用已拥有的权限组ID */
    private Long ownedGroupId;
    /** 本测试类创建的应用ID（测试结束时清理） */
    private Long createdAppId;
    /** 本测试类创建的权限组ID列表（测试结束时清理） */
    private final List<Long> createdGroupIds = new ArrayList<>();
    /** 本测试类创建的授权关系ID列表（测试结束时清理） */
    private final List<Long> createdRelIds = new ArrayList<>();

    @BeforeAll
    static void initSchema(@org.springframework.beans.factory.annotation.Autowired JdbcTemplate jdbcTemplate) {
        // 确保 mdo_app_scope_apply 表存在
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS `mdo_app_scope_apply` (
                      `id` bigint NOT NULL COMMENT '主键',
                      `app_id` bigint NOT NULL COMMENT '所属应用',
                      `group_id` bigint NOT NULL COMMENT '申请权限组',
                      `apply_reason` varchar(512) NOT NULL COMMENT '申请理由',
                      `audit_status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '审核状态 [1-待审批 2-通过 99-退回]',
                      `audit_at` datetime(3) DEFAULT NULL COMMENT '审核时间',
                      `review_comments` varchar(255) DEFAULT NULL COMMENT '审核意见',
                      `created_at` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                      `created_by` bigint DEFAULT '0' COMMENT '创建人ID',
                      `updated_at` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后修改时间',
                      `updated_by` bigint DEFAULT NULL COMMENT '最后修改人ID',
                      PRIMARY KEY (`id`) USING BTREE,
                      KEY `idx_app_id` (`app_id`),
                      KEY `idx_group_status` (`group_id`, `audit_status`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='权限组申请'
                    """);
        } catch (Exception e) {
            // 仅忽略"表已存在"类错误，其余原样抛出
            if (!isAlreadyExistsError(e)) {
                throw e;
            }
        }

        // 确保 mdo_scope_group 有 intro 列（兼容新旧 schema）
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE `mdo_scope_group` ADD COLUMN `intro` varchar(512) DEFAULT NULL COMMENT '简介' AFTER `name`");
        } catch (Exception e) {
            // 仅忽略"列已存在"类错误，其余原样抛出
            if (!isAlreadyExistsError(e)) {
                throw e;
            }
        }
    }

    @BeforeEach
    void setUp() {
        ContextUtil.setUserId(TEST_USER_ID);
        prepareTestData();
    }

    @AfterEach
    void tearDown() {
        try {
            // 1. 清理所有本测试相关的申请单（按 app_id 范围）
            if (testAppId != null) {
                appScopeApplyService.remove(QueryWrapper.create()
                        .eq(AppScopeApply::getAppId, testAppId));
            }

            // 2. 清理本测试创建的授权关系
            if (!createdRelIds.isEmpty()) {
                appGroupRelService.removeByIds(createdRelIds);
                createdRelIds.clear();
            }
            // 额外兜底：清理 testAppId + testGroupId/ownedGroupId 组合
            if (testAppId != null) {
                if (testGroupId != null) {
                    appGroupRelService.remove(QueryWrapper.create()
                            .eq(AppGroupRel::getAppId, testAppId)
                            .eq(AppGroupRel::getGroupId, testGroupId));
                }
                if (ownedGroupId != null) {
                    appGroupRelService.remove(QueryWrapper.create()
                            .eq(AppGroupRel::getAppId, testAppId)
                            .eq(AppGroupRel::getGroupId, ownedGroupId));
                }
            }

            // 3. 清理本测试创建的权限组
            if (!createdGroupIds.isEmpty()) {
                scopeGroupService.removeByIds(createdGroupIds);
                createdGroupIds.clear();
            }

            // 4. 清理本测试创建的应用
            if (createdAppId != null) {
                appService.removeById(createdAppId);
                createdAppId = null;
            }
        } finally {
            ContextUtil.remove();
        }
    }

    /**
     * 准备测试数据：确保有测试用的应用、权限组和授权关系。
     * 优先复用现有数据，不存在时创建。
     */
    private void prepareTestData() {
        // 1. 找一个归属于 TEST_USER_ID 的应用，没有则创建
        List<App> apps = appService.list(QueryWrapper.create()
                .eq(App::getCreatedBy, TEST_USER_ID)
                .limit(2));
        if (apps.isEmpty()) {
            App app = new App();
            app.setName("测试应用-" + System.currentTimeMillis());
            app.setAppKey("test-app-" + System.currentTimeMillis());
            app.setState(true);
            app.setCreatedBy(TEST_USER_ID);
            appService.save(app);
            testAppId = app.getId();
            createdAppId = app.getId();
        } else {
            testAppId = apps.get(0).getId();
        }

        // 2. 找两个启用的权限组
        List<ScopeGroup> groups = scopeGroupService.list(QueryWrapper.create()
                .eq(ScopeGroup::getState, true)
                .limit(5));
        assertFalse(groups.isEmpty(), "测试库中没有启用的权限组，请先准备测试数据");

        // 3. 找出 testAppId 已拥有的权限组
        List<Long> ownedGroupIds = appGroupRelService.listGroupIdByAppId(testAppId);

        // 选一个未拥有的作为 testGroupId
        testGroupId = null;
        ownedGroupId = null;
        for (ScopeGroup g : groups) {
            if (testGroupId == null && !ownedGroupIds.contains(g.getId())) {
                testGroupId = g.getId();
            } else if (ownedGroupId == null && ownedGroupIds.contains(g.getId())) {
                ownedGroupId = g.getId();
            }
        }

        // 如果没有未拥有的权限组，就创建一个新的权限组
        if (testGroupId == null) {
            ScopeGroup newGroup = new ScopeGroup();
            newGroup.setName("测试权限组-" + System.currentTimeMillis());
            newGroup.setState(true);
            newGroup.setWeight(0L);
            scopeGroupService.save(newGroup);
            testGroupId = newGroup.getId();
            createdGroupIds.add(newGroup.getId());
        }

        // 如果没有已拥有的权限组，就建立一条授权关系
        if (ownedGroupId == null) {
            // 找另一个权限组建立授权关系
            for (ScopeGroup g : groups) {
                if (!g.getId().equals(testGroupId)) {
                    AppGroupRel rel = new AppGroupRel();
                    rel.setAppId(testAppId);
                    rel.setGroupId(g.getId());
                    appGroupRelService.save(rel);
                    createdRelIds.add(rel.getId());
                    ownedGroupId = g.getId();
                    break;
                }
            }
            if (ownedGroupId == null) {
                // 实在没有就再创建一个
                ScopeGroup g = new ScopeGroup();
                g.setName("已拥有组-" + System.currentTimeMillis());
                g.setState(true);
                g.setWeight(0L);
                scopeGroupService.save(g);
                createdGroupIds.add(g.getId());
                AppGroupRel rel = new AppGroupRel();
                rel.setAppId(testAppId);
                rel.setGroupId(g.getId());
                appGroupRelService.save(rel);
                createdRelIds.add(rel.getId());
                ownedGroupId = g.getId();
            }
        }

        assertNotNull(testGroupId, "测试数据准备失败：无可用权限组");
        assertNotNull(ownedGroupId, "测试数据准备失败：无已拥有权限组");
    }

    @Test
    void apply_应用不存在时报错() {
        AppScopeApplyDto dto = new AppScopeApplyDto()
                .setAppId(-999L)
                .setGroupIds(List.of(testGroupId))
                .setApplyReason("测试申请");
        ArgumentException e = assertThrows(ArgumentException.class,
                () -> appScopeApplyService.apply(dto));
        assertTrue(e.getMessage().contains("应用"));
    }

    @Test
    void apply_权限组不存在时报错() {
        AppScopeApplyDto dto = new AppScopeApplyDto()
                .setAppId(testAppId)
                .setGroupIds(List.of(-999L))
                .setApplyReason("测试申请");
        ArgumentException e = assertThrows(ArgumentException.class,
                () -> appScopeApplyService.apply(dto));
        assertTrue(e.getMessage().contains("权限组"));
    }

    @Test
    void apply_已拥有权限组时报错() {
        AppScopeApplyDto dto = new AppScopeApplyDto()
                .setAppId(testAppId)
                .setGroupIds(List.of(ownedGroupId))
                .setApplyReason("重复拥有");
        ArgumentException e = assertThrows(ArgumentException.class,
                () -> appScopeApplyService.apply(dto));
        assertTrue(e.getMessage().contains("已拥有"));
    }

    @Test
    void apply_成功后生成待审批申请单() {
        AppScopeApplyDto dto = new AppScopeApplyDto()
                .setAppId(testAppId)
                .setGroupIds(List.of(testGroupId))
                .setApplyReason("单元测试申请");
        Integer count = appScopeApplyService.apply(dto);
        assertEquals(1, count);

        AppScopeApply saved = appScopeApplyService.getOne(
                QueryWrapper.create()
                        .eq(AppScopeApply::getAppId, testAppId)
                        .eq(AppScopeApply::getGroupId, testGroupId)
                        .eq(AppScopeApply::getAuditStatus, AuditStatusEnum.PENDING.getCode()));
        assertNotNull(saved);
        assertEquals("单元测试申请", saved.getApplyReason());
    }

    @Test
    void apply_在审单存在时禁止重复申请() {
        // 先创建一条在审单
        AppScopeApplyDto dto = new AppScopeApplyDto()
                .setAppId(testAppId)
                .setGroupIds(List.of(testGroupId))
                .setApplyReason("首次申请");
        appScopeApplyService.apply(dto);

        // 再次申请应报错
        AppScopeApplyDto dupDto = new AppScopeApplyDto()
                .setAppId(testAppId)
                .setGroupIds(List.of(testGroupId))
                .setApplyReason("重复申请");
        ArgumentException e = assertThrows(ArgumentException.class,
                () -> appScopeApplyService.apply(dupDto));
        assertTrue(e.getMessage().contains("待审批"));
    }

    @Test
    void review_申请单不存在时报错() {
        AppScopeApplyReviewDto dto = new AppScopeApplyReviewDto()
                .setId(-999L)
                .setApprove(true);
        ArgumentException e = assertThrows(ArgumentException.class,
                () -> appScopeApplyService.review(dto));
        assertTrue(e.getMessage().contains("申请单不存在"));
    }

    @Test
    void review_非待审批状态时报错() {
        // 先创建一条已通过的申请单（直接写入数据库，不走 apply）
        AppScopeApply apply = buildApply(testAppId, testGroupId,
                AuditStatusEnum.APPROVED, "测试");
        appScopeApplyService.save(apply);

        AppScopeApplyReviewDto dto = new AppScopeApplyReviewDto()
                .setId(apply.getId())
                .setApprove(true);
        ArgumentException e = assertThrows(ArgumentException.class,
                () -> appScopeApplyService.review(dto));
        assertTrue(e.getMessage().contains("状态已变更"));
    }

    @Test
    void review_退回时必须填审核意见() {
        // 创建待审批申请单
        AppScopeApply apply = buildApply(testAppId, testGroupId,
                AuditStatusEnum.PENDING, "测试");
        appScopeApplyService.save(apply);

        AppScopeApplyReviewDto dto = new AppScopeApplyReviewDto()
                .setId(apply.getId())
                .setApprove(false)
                .setReviewComments(" ");
        ArgumentException e = assertThrows(ArgumentException.class,
                () -> appScopeApplyService.review(dto));
        assertTrue(e.getMessage().contains("审核意见"));
    }

    @Test
    void review_通过后写入授权关系并更新状态() {
        // 创建待审批申请单
        AppScopeApply apply = buildApply(testAppId, testGroupId,
                AuditStatusEnum.PENDING, "测试通过");
        appScopeApplyService.save(apply);

        AppScopeApplyReviewDto dto = new AppScopeApplyReviewDto()
                .setId(apply.getId())
                .setApprove(true)
                .setReviewComments("同意");
        Boolean result = appScopeApplyService.review(dto);
        assertTrue(result);

        // 验证状态已更新
        AppScopeApply updated = appScopeApplyService.getById(apply.getId());
        assertEquals(AuditStatusEnum.APPROVED.getCode(), updated.getAuditStatus());
        assertEquals("同意", updated.getReviewComments());
        assertNotNull(updated.getAuditAt());

        // 验证授权关系已建立
        long relCount = appGroupRelService.count(QueryWrapper.create()
                .eq(AppGroupRel::getAppId, testAppId)
                .eq(AppGroupRel::getGroupId, testGroupId));
        assertEquals(1, relCount);
    }

    @Test
    void review_通过时授权关系幂等() {
        // 创建待审批申请单
        AppScopeApply apply = buildApply(testAppId, testGroupId,
                AuditStatusEnum.PENDING, "测试幂等");
        appScopeApplyService.save(apply);

        // 预先建立授权关系
        AppGroupRel rel = new AppGroupRel();
        rel.setAppId(testAppId);
        rel.setGroupId(testGroupId);
        appGroupRelService.save(rel);

        AppScopeApplyReviewDto dto = new AppScopeApplyReviewDto()
                .setId(apply.getId())
                .setApprove(true);
        Boolean result = appScopeApplyService.review(dto);
        assertTrue(result);

        // 验证仍然只有一条授权关系
        long relCount = appGroupRelService.count(QueryWrapper.create()
                .eq(AppGroupRel::getAppId, testAppId)
                .eq(AppGroupRel::getGroupId, testGroupId));
        assertEquals(1, relCount);
    }

    @Test
    void review_退回时更新状态和意见() {
        // 创建待审批申请单
        AppScopeApply apply = buildApply(testAppId, testGroupId,
                AuditStatusEnum.PENDING, "测试退回");
        appScopeApplyService.save(apply);

        AppScopeApplyReviewDto dto = new AppScopeApplyReviewDto()
                .setId(apply.getId())
                .setApprove(false)
                .setReviewComments("资料不全");
        Boolean result = appScopeApplyService.review(dto);
        assertTrue(result);

        // 验证状态已更新
        AppScopeApply updated = appScopeApplyService.getById(apply.getId());
        assertEquals(AuditStatusEnum.REJECTED.getCode(), updated.getAuditStatus());
        assertEquals("资料不全", updated.getReviewComments());
        assertNotNull(updated.getAuditAt());

        // 验证未建立授权关系
        long relCount = appGroupRelService.count(QueryWrapper.create()
                .eq(AppGroupRel::getAppId, testAppId)
                .eq(AppGroupRel::getGroupId, testGroupId));
        assertEquals(0, relCount);
    }

    /**
     * 判断异常是否为"表/列已存在"类错误。
     * 沿异常链搜索，只要任意一层消息包含 "already exists" / "duplicate column" /
     * "duplicate key" 即视为可忽略。
     */
    private static boolean isAlreadyExistsError(Exception e) {
        Throwable t = e;
        while (t != null) {
            String msg = t.getMessage() == null ? "" : t.getMessage().toLowerCase();
            if (msg.contains("already exists")
                    || msg.contains("duplicate column")
                    || msg.contains("duplicate key")) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    /**
     * 构建申请单对象（规避继承链下 chain setter 返回父类类型的编译问题）。
     */
    private AppScopeApply buildApply(Long appId, Long groupId,
                                     AuditStatusEnum status, String reason) {
        AppScopeApply apply = new AppScopeApply();
        apply.setAppId(appId);
        apply.setGroupId(groupId);
        apply.setAuditStatus(status.getCode());
        apply.setApplyReason(reason);
        return apply;
    }
}
