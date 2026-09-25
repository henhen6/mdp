package top.mddata.console.service.organization;

import org.junit.jupiter.api.Test;
import top.mddata.common.constant.BuiltInOrgId;
import top.mddata.common.enumeration.organization.UserIdentityEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 可见组织树根节点映射测试：规则见 spec 第 7 节。
 */
class VisibleRootOrgTest {

    @Test
    void 运营者不限制返回null() {
        assertNull(OrgVisibilityService.visibleRootOrgIds(UserIdentityEnum.OPERATIONS_ADMIN));
    }

    @Test
    void 开发者管理员仅开发者平台() {
        assertEquals(List.of(BuiltInOrgId.DEVELOPER_PLATFORM),
                OrgVisibilityService.visibleRootOrgIds(UserIdentityEnum.DEVELOPER_ADMIN));
    }

    @Test
    void 普通用户仅总公司() {
        assertEquals(List.of(BuiltInOrgId.HEAD_COMPANY),
                OrgVisibilityService.visibleRootOrgIds(UserIdentityEnum.USER));
    }

    @Test
    void 开发者什么都不可见() {
        assertTrue(OrgVisibilityService.visibleRootOrgIds(UserIdentityEnum.DEVELOPER).isEmpty());
    }
}
