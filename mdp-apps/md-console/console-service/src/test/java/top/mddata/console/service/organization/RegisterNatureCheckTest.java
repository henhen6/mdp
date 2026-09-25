package top.mddata.console.service.organization;

import org.junit.jupiter.api.Test;
import top.mddata.common.enumeration.organization.OrgNatureEnum;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 注册身份白名单测试：运营身份(99)不开放注册，非法值快速失败。
 */
class RegisterNatureCheckTest {

    @Test
    void 总公司和开发者允许注册() {
        assertTrue(UserIdentityService.checkRegisterNature(OrgNatureEnum.HEAD_COMPANY.getCode()));
        assertTrue(UserIdentityService.checkRegisterNature(OrgNatureEnum.DEVELOPER.getCode()));
    }

    @Test
    void 运营身份与非法值禁止注册() {
        assertFalse(UserIdentityService.checkRegisterNature(OrgNatureEnum.OPERATIONS.getCode()));
        assertFalse(UserIdentityService.checkRegisterNature(null));
        assertFalse(UserIdentityService.checkRegisterNature(2));
    }
}
