package top.mddata.console.service.organization.impl;

import com.mybatisflex.core.update.UpdateWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import org.junit.jupiter.api.Test;
import top.mddata.common.entity.User;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UserServiceImpl.correctLastOrg 纯函数测试：
 * 部门关系变更时，仅清理已失效的最近登录组织上下文，未失效项保留。
 */
class UserServiceImplCorrectLastOrgTest {

    @Test
    void shouldKeepAllWhenNothingInvalid() {
        User dbUser = userWithLastOrg(10L, 20L, 30L);
        User target = UpdateEntity.of(User.class);

        UserServiceImpl.correctLastOrg(dbUser, List.of(10L, 20L, 30L), target);

        assertTrue(updatesOf(target).isEmpty(), "last* 均有效时不应更新任何字段");
    }

    @Test
    void shouldClearOnlyInvalidDept() {
        User dbUser = userWithLastOrg(10L, 20L, 30L);
        User target = UpdateEntity.of(User.class);

        // 部门 10 被移除，单位 20、顶级单位 30 仍保留
        UserServiceImpl.correctLastOrg(dbUser, List.of(20L, 30L), target);
        Map<String, Object> updates = updatesOf(target);

        assertTrue(updates.containsKey("lastDeptId"));
        assertNull(updates.get("lastDeptId"));
        assertFalse(updates.containsKey("lastCompanyId"), "仍有效的单位不应被清理");
        assertFalse(updates.containsKey("lastTopCompanyId"), "仍有效的顶级单位不应被清理");
    }

    @Test
    void shouldClearAllWhenOrgListEmptied() {
        User dbUser = userWithLastOrg(10L, 20L, 30L);
        User target = UpdateEntity.of(User.class);

        // 用户主动清空全部部门
        UserServiceImpl.correctLastOrg(dbUser, List.of(), target);
        Map<String, Object> updates = updatesOf(target);

        assertEquals(3, updates.size());
        assertNull(updates.get("lastDeptId"));
        assertNull(updates.get("lastCompanyId"));
        assertNull(updates.get("lastTopCompanyId"));
    }

    @Test
    void shouldSkipWhenLastOrgAlreadyNull() {
        User dbUser = userWithLastOrg(null, null, null);
        User target = UpdateEntity.of(User.class);

        UserServiceImpl.correctLastOrg(dbUser, List.of(10L), target);

        assertTrue(updatesOf(target).isEmpty(), "last* 本就不存在时不应触发更新");
    }

    private User userWithLastOrg(Long deptId, Long companyId, Long topCompanyId) {
        User user = new User();
        user.setLastDeptId(deptId);
        user.setLastCompanyId(companyId);
        user.setLastTopCompanyId(topCompanyId);
        return user;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> updatesOf(User entity) {
        return ((UpdateWrapper<User>) entity).getUpdates();
    }
}
