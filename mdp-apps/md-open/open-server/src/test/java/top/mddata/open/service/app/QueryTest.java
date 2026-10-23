package top.mddata.open.service.app;

import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import top.mddata.open.entity.admin.App;
import top.mddata.open.entity.admin.AppGroupRel;
import top.mddata.open.entity.admin.ScopeGroup;
import top.mddata.open.service.oauth.ScopeGroupService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "mdp.log.ipv4.enabled=false",
                "mdp.log.ipv6.enabled=false"
        })
@Sql(scripts = "/sql/schema-app-scope-apply.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class QueryTest {
    @Resource
    private AppService appService;
    @Resource
    private ScopeGroupService scopeGroupService;
    @Resource
    private AppGroupRelService appGroupRelService;

    @Test
    void queryData() {
        List<App> apps = appService.list(QueryWrapper.create().limit(10));
        assertFalse(apps.isEmpty(), "无应用数据");
        log.info("=== Apps (first 10) ===");
        for (App app : apps) {
            log.info("  id={}, name={}, createdBy={}", app.getId(), app.getName(), app.getCreatedBy());
        }

        List<ScopeGroup> groups = scopeGroupService.list(QueryWrapper.create().limit(10));
        assertFalse(groups.isEmpty(), "无权限组数据");
        log.info("=== ScopeGroups (first 10) ===");
        for (ScopeGroup g : groups) {
            log.info("  id={}, name={}, state={}", g.getId(), g.getName(), g.getState());
        }

        List<AppGroupRel> rels = appGroupRelService.list(QueryWrapper.create().limit(10));
        log.info("=== AppGroupRels (first 10) ===");
        for (AppGroupRel r : rels) {
            log.info("  appId={}, groupId={}", r.getAppId(), r.getGroupId());
        }
    }
}
