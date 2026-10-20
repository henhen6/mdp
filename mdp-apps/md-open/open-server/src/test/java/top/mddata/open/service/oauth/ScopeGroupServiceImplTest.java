package top.mddata.open.service.oauth;

import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import top.mddata.base.util.ContextUtil;
import top.mddata.open.entity.admin.App;
import top.mddata.open.entity.admin.AppGroupRel;
import top.mddata.open.entity.admin.AppScopeApply;
import top.mddata.open.entity.admin.ScopeGroup;
import top.mddata.open.service.app.AppGroupRelService;
import top.mddata.open.service.app.AppScopeApplyService;
import top.mddata.open.service.app.AppService;
import top.mddata.open.vo.admin.ScopeGroupDetailVo;
import top.mddata.open.vo.admin.ScopeGroupVo;
import top.mddata.common.enumeration.AuditStatusEnum;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 权限组 Service 扩展方法测试。
 *
 * @author henhen6
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "mdp.log.ipv4.enabled=false",
                "mdp.log.ipv6.enabled=false"
        })
class ScopeGroupServiceImplTest {

    @Resource
    private ScopeGroupService scopeGroupService;

    @Resource
    private AppGroupRelService appGroupRelService;

    @Resource
    private AppScopeApplyService appScopeApplyService;

    @Resource
    private AppService appService;

    /** 测试用用户ID */
    private static final Long TEST_USER_ID = 1L;

    /** 测试用应用ID（复用现有数据） */
    private Long testAppId;
    /** 测试用已拥有的权限组ID（本测试创建并建立授权） */
    private Long ownedGroupId;
    /** 测试用未拥有的启用权限组ID（本测试创建，未授权） */
    private Long appliableGroupId;

    /** 本测试创建的权限组ID列表（测试结束时清理） */
    private final List<Long> createdGroupIds = new ArrayList<>();
    /** 本测试创建的授权关系ID列表（测试结束时清理） */
    private final List<Long> createdRelIds = new ArrayList<>();
    /** 本测试创建的申请单ID列表（测试结束时清理） */
    private final List<Long> createdApplyIds = new ArrayList<>();

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
            if (!isAlreadyExistsError(e)) {
                throw e;
            }
        }

        // 确保 mdo_scope_group 有 intro 列（兼容新旧 schema）
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE `mdo_scope_group` ADD COLUMN `intro` varchar(512) DEFAULT NULL COMMENT '简介' AFTER `name`");
        } catch (Exception e) {
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
            // 1. 清理本测试创建的申请单
            if (!createdApplyIds.isEmpty()) {
                appScopeApplyService.removeByIds(createdApplyIds);
                createdApplyIds.clear();
            }

            // 2. 清理本测试创建的授权关系
            if (!createdRelIds.isEmpty()) {
                appGroupRelService.removeByIds(createdRelIds);
                createdRelIds.clear();
            }

            // 3. 清理本测试创建的权限组
            if (!createdGroupIds.isEmpty()) {
                scopeGroupService.removeByIds(createdGroupIds);
                createdGroupIds.clear();
            }

            // （无应用需要清理，应用复用现有数据）
        } finally {
            ContextUtil.remove();
        }
    }

    @Test
    void listAppliable_排除已拥有和在审的组() {
        List<ScopeGroupVo> list = scopeGroupService.listAppliable(testAppId);
        assertNotNull(list);
        // 已拥有的组不应出现在可申请列表
        assertTrue(list.stream().noneMatch(g -> ownedGroupId.equals(g.getId())));
        // 所有返回组都应是启用状态
        assertTrue(list.stream().allMatch(g -> Boolean.TRUE.equals(g.getState())));
        // 未拥有且启用的组应在可申请列表中
        assertTrue(list.stream().anyMatch(g -> appliableGroupId.equals(g.getId())));
    }

    @Test
    void listAppliable_在审申请单也排除() {
        // 创建一条在审申请单
        AppScopeApply apply = new AppScopeApply();
        apply.setAppId(testAppId);
        apply.setGroupId(appliableGroupId);
        apply.setApplyReason("测试在审排除");
        apply.setAuditStatus(AuditStatusEnum.PENDING.getCode());
        appScopeApplyService.save(apply);
        createdApplyIds.add(apply.getId());

        // 验证该组不再出现在可申请列表
        List<ScopeGroupVo> afterApply = scopeGroupService.listAppliable(testAppId);
        assertTrue(afterApply.stream().noneMatch(g -> appliableGroupId.equals(g.getId())));
    }

    @Test
    void listOwned_返回已生效的组() {
        List<ScopeGroupVo> list = scopeGroupService.listOwned(testAppId);
        assertNotNull(list);
        // 已建立授权关系的组应出现在列表中
        assertTrue(list.stream().anyMatch(g -> ownedGroupId.equals(g.getId())));
        // 未授权的组不应出现在列表中
        assertTrue(list.stream().noneMatch(g -> appliableGroupId.equals(g.getId())));
    }

    @Test
    void listOwned_自建授权关系也能查到() {
        // 给 appliableGroupId 建立授权关系
        AppGroupRel rel = new AppGroupRel();
        rel.setAppId(testAppId);
        rel.setGroupId(appliableGroupId);
        appGroupRelService.save(rel);
        createdRelIds.add(rel.getId());

        // 验证已拥有列表包含该组
        List<ScopeGroupVo> owned = scopeGroupService.listOwned(testAppId);
        assertTrue(owned.stream().anyMatch(g -> appliableGroupId.equals(g.getId())));
    }

    @Test
    void listOwned_结果中权限组ID不重复() {
        // 验证 listOwned 返回结果中每个权限组ID只出现一次
        // 注：mdo_app_group_rel 有 uk_app_id_group_id 唯一索引，业务上不会产生重复授权关系，
        //     此处作为防御性验证，确保查询层 distinct 生效，防止未来 schema 变更引入重复。
        List<ScopeGroupVo> owned = scopeGroupService.listOwned(testAppId);
        assertNotNull(owned);

        long idCount = owned.stream().map(ScopeGroupVo::getId).distinct().count();
        assertEquals(owned.size(), idCount, "listOwned 返回的权限组不应有重复 ID");

        // 已建立授权关系的组恰好出现一次
        long matchCount = owned.stream().filter(g -> ownedGroupId.equals(g.getId())).count();
        assertEquals(1L, matchCount, "已拥有的权限组在结果中应恰好出现一次");
    }

    @Test
    void getGroupDetail_返回组基本信息() {
        ScopeGroupDetailVo detail = scopeGroupService.getGroupDetail(ownedGroupId);
        assertNotNull(detail);
        assertEquals(ownedGroupId, detail.getId());
        assertNotNull(detail.getName());
        assertNotNull(detail.getApis());
        assertNotNull(detail.getScopes());
    }

    @Test
    void getGroupDetail_组不存在时报错() {
        assertThrows(Exception.class, () -> scopeGroupService.getGroupDetail(-999L));
    }

    @Test
    void getGroupDetail_自建组验证空集合返回() {
        // 创建一个全新的空组（没有接口和权限关联）
        ScopeGroup group = new ScopeGroup();
        group.setName("空测试组-" + System.currentTimeMillis());
        group.setState(true);
        group.setWeight(0L);
        scopeGroupService.save(group);
        createdGroupIds.add(group.getId());

        ScopeGroupDetailVo detail = scopeGroupService.getGroupDetail(group.getId());
        assertNotNull(detail);
        assertNotNull(detail.getApis());
        assertNotNull(detail.getScopes());
        assertTrue(detail.getApis().isEmpty());
        assertTrue(detail.getScopes().isEmpty());
    }

    /**
     * 准备测试数据：复用现有应用，创建两个新权限组，其中一个建立授权关系。
     * 权限组全部新建，确保状态完全可控。
     */
    private void prepareTestData() {
        // 1. 找一个现有应用（取第一个即可，只用作权限组归属的 appId）
        List<App> apps = appService.list(QueryWrapper.create()
                .eq(App::getState, true)
                .limit(1));
        assertFalse(apps.isEmpty(), "测试库中没有可用应用，请先准备测试数据");
        testAppId = apps.get(0).getId();

        // 2. 创建"已拥有"的权限组并建立授权关系
        ScopeGroup ownedGroup = new ScopeGroup();
        ownedGroup.setName("已拥有测试组-" + System.currentTimeMillis());
        ownedGroup.setState(true);
        ownedGroup.setWeight(10L);
        scopeGroupService.save(ownedGroup);
        ownedGroupId = ownedGroup.getId();
        createdGroupIds.add(ownedGroup.getId());

        AppGroupRel rel = new AppGroupRel();
        rel.setAppId(testAppId);
        rel.setGroupId(ownedGroupId);
        appGroupRelService.save(rel);
        createdRelIds.add(rel.getId());

        // 3. 创建"可申请"的权限组（不建立授权关系）
        ScopeGroup appliableGroup = new ScopeGroup();
        appliableGroup.setName("可申请测试组-" + System.currentTimeMillis());
        appliableGroup.setState(true);
        appliableGroup.setWeight(20L);
        scopeGroupService.save(appliableGroup);
        appliableGroupId = appliableGroup.getId();
        createdGroupIds.add(appliableGroup.getId());

        assertNotNull(testAppId);
        assertNotNull(ownedGroupId);
        assertNotNull(appliableGroupId);
    }

    /**
     * 判断异常是否为"表/列已存在"类错误。
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
}
