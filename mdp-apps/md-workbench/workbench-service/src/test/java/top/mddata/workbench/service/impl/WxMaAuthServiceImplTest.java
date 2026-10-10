package top.mddata.workbench.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import me.chanjar.weixin.common.error.WxErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import top.mddata.base.exception.BizException;
import top.mddata.common.entity.User;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.console.facade.organization.UserFacade;
import top.mddata.workbench.service.SsoUserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link WxMaAuthServiceImpl#findOrRegisterUser} 用户匹配逻辑单测。
 *
 * <p>覆盖三条匹配路径：openid 命中直接登录、手机号命中绑定 openid、均不命中自动注册；
 * 以及微信接口异常时的快速失败行为。</p>
 *
 * @author tangyh
 */
class WxMaAuthServiceImplTest {
    private static final String LOGIN_CODE = "login-code";
    private static final String PHONE_CODE = "phone-code";
    private static final String OPENID = "openid-1";
    private static final String PHONE = "13800000000";

    private WxMaUserService wxMaUserService;
    private SsoUserService ssoUserService;
    private UserFacade userFacade;
    private WxMaAuthServiceImpl wxMaAuthService;

    @BeforeEach
    void setUp() {
        WxMaService wxMaService = mock(WxMaService.class);
        wxMaUserService = mock(WxMaUserService.class);
        ssoUserService = mock(SsoUserService.class);
        userFacade = mock(UserFacade.class);
        when(wxMaService.getUserService()).thenReturn(wxMaUserService);
        wxMaAuthService = new WxMaAuthServiceImpl(wxMaService, ssoUserService, userFacade);
    }

    @Test
    void openid已绑定_直接返回用户且不再换取手机号() throws Exception {
        mockSessionInfo(OPENID);
        User bound = user(1L, PHONE);
        when(ssoUserService.getByWxOpenid(OPENID)).thenReturn(bound);

        User result = wxMaAuthService.findOrRegisterUser(LOGIN_CODE, PHONE_CODE);

        assertSame(bound, result);
        verify(wxMaUserService, never()).getPhoneNoInfo(anyString());
        verify(ssoUserService, never()).getByPhone(anyString());
        verify(userFacade, never()).registerByPhone(any());
    }

    @Test
    void openid未绑定_手机号已注册_绑定openid后返回() throws Exception {
        mockSessionInfo(OPENID);
        mockPhoneNoInfo(PHONE);
        when(ssoUserService.getByWxOpenid(OPENID)).thenReturn(null);
        User existing = user(2L, PHONE);
        when(ssoUserService.getByPhone(PHONE)).thenReturn(existing);

        User result = wxMaAuthService.findOrRegisterUser(LOGIN_CODE, PHONE_CODE);

        assertSame(existing, result);
        assertEquals(OPENID, result.getWxOpenid());
        verify(ssoUserService).updateById(any(User.class));
        verify(userFacade, never()).registerByPhone(any());
    }

    @Test
    void openid与手机号均不存在_自动注册并绑定openid() throws Exception {
        mockSessionInfo(OPENID);
        mockPhoneNoInfo(PHONE);
        when(ssoUserService.getByWxOpenid(OPENID)).thenReturn(null);
        when(ssoUserService.getByPhone(PHONE)).thenReturn(null);

        User result = wxMaAuthService.findOrRegisterUser(LOGIN_CODE, PHONE_CODE);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userFacade).registerByPhone(captor.capture());
        User registered = captor.getValue();
        assertEquals(PHONE, registered.getPhone());
        assertEquals(OPENID, registered.getWxOpenid());
        assertEquals(OrgNatureEnum.HEAD_COMPANY.getCode(), registered.getNature());
        assertSame(registered, result);
    }

    @Test
    void 无手机号授权码_按openid静默注册() throws Exception {
        mockSessionInfo(OPENID);
        when(ssoUserService.getByWxOpenid(OPENID)).thenReturn(null);

        User result = wxMaAuthService.findOrRegisterUser(LOGIN_CODE, null);

        // 不再换取手机号，也不按手机号查询
        verify(wxMaUserService, never()).getPhoneNoInfo(anyString());
        verify(ssoUserService, never()).getByPhone(anyString());

        // 用户名注册：wx_ + openid
        ArgumentCaptor<User> registerCaptor = ArgumentCaptor.forClass(User.class);
        verify(userFacade).registerByUsername(registerCaptor.capture());
        assertEquals(WxMaAuthServiceImpl.USER_NAME_PREFIX + OPENID, registerCaptor.getValue().getUsername());
        assertEquals(OrgNatureEnum.HEAD_COMPANY.getCode(), registerCaptor.getValue().getNature());

        // 注册后回写昵称与 openid
        ArgumentCaptor<User> updateCaptor = ArgumentCaptor.forClass(User.class);
        verify(ssoUserService).updateById(updateCaptor.capture());
        assertEquals(OPENID, updateCaptor.getValue().getWxOpenid());
        assertEquals(true, updateCaptor.getValue().getName().startsWith(WxMaAuthServiceImpl.NICK_NAME_PREFIX));

        assertEquals(OPENID, result.getWxOpenid());
    }

    @Test
    void 登录凭证非法_抛出业务异常() throws Exception {
        when(wxMaUserService.getSessionInfo(LOGIN_CODE)).thenThrow(new WxErrorException("invalid code"));

        assertThrows(BizException.class, () -> wxMaAuthService.findOrRegisterUser(LOGIN_CODE, PHONE_CODE));
        verify(ssoUserService, never()).getByWxOpenid(anyString());
    }

    @Test
    void 手机号授权失败_抛出业务异常() throws Exception {
        mockSessionInfo(OPENID);
        when(ssoUserService.getByWxOpenid(OPENID)).thenReturn(null);
        when(wxMaUserService.getPhoneNoInfo(PHONE_CODE)).thenThrow(new WxErrorException("code been used"));

        assertThrows(BizException.class, () -> wxMaAuthService.findOrRegisterUser(LOGIN_CODE, PHONE_CODE));
        verify(userFacade, never()).registerByPhone(any());
    }

    @Test
    void 换取手机号_优先返回不带区号的purePhoneNumber() throws Exception {
        WxMaPhoneNumberInfo info = new WxMaPhoneNumberInfo();
        info.setPhoneNumber("+8613800000000");
        info.setPurePhoneNumber(PHONE);
        when(wxMaUserService.getPhoneNoInfo(PHONE_CODE)).thenReturn(info);

        assertEquals(PHONE, wxMaAuthService.getPhoneNumber(PHONE_CODE));
    }

    private void mockSessionInfo(String openid) throws Exception {
        WxMaJscode2SessionResult session = new WxMaJscode2SessionResult();
        session.setOpenid(openid);
        when(wxMaUserService.getSessionInfo(LOGIN_CODE)).thenReturn(session);
    }

    private void mockPhoneNoInfo(String phone) throws Exception {
        WxMaPhoneNumberInfo info = new WxMaPhoneNumberInfo();
        info.setPurePhoneNumber(phone);
        when(wxMaUserService.getPhoneNoInfo(PHONE_CODE)).thenReturn(info);
    }

    private User user(Long id, String phone) {
        User user = new User();
        user.setId(id);
        user.setPhone(phone);
        return user;
    }
}
