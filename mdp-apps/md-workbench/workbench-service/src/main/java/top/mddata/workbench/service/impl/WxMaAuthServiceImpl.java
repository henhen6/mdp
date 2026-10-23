package top.mddata.workbench.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.exception.BizException;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.entity.User;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.console.facade.organization.UserFacade;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.service.WxMaAuthService;

/**
 * 微信小程序认证 服务层实现。
 *
 * @author tangyh
 * @since 2026/10/10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxMaAuthServiceImpl implements WxMaAuthService {
    /**
     * 静默注册用户的用户名前缀
     */
    static final String USER_NAME_PREFIX = "wx_";
    /**
     * 静默注册用户的昵称前缀
     */
    static final String NICK_NAME_PREFIX = "微信用户";

    private final WxMaService wxMaService;
    private final SsoUserService ssoUserService;
    private final UserFacade userFacade;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User findOrRegisterUser(String loginCode, String phoneCode) {
        String openid = getOpenid(loginCode);

        // 1. openid 已绑定，直接返回（不再消耗手机号授权码，节省一次微信接口调用）
        User user = ssoUserService.getByWxOpenid(openid);
        if (user != null) {
            return user;
        }

        // 2. 有手机号授权码时，按手机号匹配或注册（企业主体小程序）
        if (StrUtil.isNotEmpty(phoneCode)) {
            return findOrRegisterByPhone(openid, phoneCode);
        }

        // 3. 无手机号授权码（个人主体小程序无组件权限）：按 openid 静默注册，不绑定手机号
        return registerByOpenid(openid);
    }

    /**
     * 按手机号匹配用户：已注册则绑定 openid，未注册则按手机号自动注册
     */
    private User findOrRegisterByPhone(String openid, String phoneCode) {
        String phone = getPhoneNumber(phoneCode);

        User user = ssoUserService.getByPhone(phone);
        if (user != null) {
            User updateUser = UpdateEntity.of(User.class, user.getId());
            updateUser.setWxOpenid(openid);
            ssoUserService.updateById(updateUser);
            user.setWxOpenid(openid);
            return user;
        }

        // registerByPhone 内部会初始化默认密码、用户名、默认组织与角色，name 默认为手机号
        User newUser = new User();
        newUser.setPhone(phone);
        newUser.setWxOpenid(openid);
        newUser.setNature(OrgNatureEnum.HEAD_COMPANY.getCode());
        userFacade.registerByPhone(newUser);
        return newUser;
    }

    /**
     * 按 openid 静默注册（无手机号）。
     * registerByUsername 内部会初始化默认组织与角色，但 name 会被覆盖为用户名，
     * 因此注册后再更新昵称与 openid。
     */
    private User registerByOpenid(String openid) {
        User newUser = new User();
        newUser.setUsername(USER_NAME_PREFIX + openid);
        newUser.setNature(OrgNatureEnum.HEAD_COMPANY.getCode());
        userFacade.registerByUsername(newUser);

        User updateUser = UpdateEntity.of(User.class, newUser.getId());
        updateUser.setName(NICK_NAME_PREFIX + RandomUtil.randomString(4));
        updateUser.setWxOpenid(openid);
        ssoUserService.updateById(updateUser);

        newUser.setName(updateUser.getName());
        newUser.setWxOpenid(openid);
        return newUser;
    }

    @Override
    public String getPhoneNumber(String phoneCode) {
        WxMaPhoneNumberInfo phoneNoInfo;
        try {
            phoneNoInfo = wxMaService.getUserService().getPhoneNoInfo(phoneCode);
        } catch (WxErrorException e) {
            log.error("微信手机号换取失败: errcode={}, errmsg={}", e.getError().getErrorCode(), e.getError().getErrorMsg(), e);
            throw new BizException("微信手机号授权失败：" + e.getError().getErrorMsg());
        }
        ArgumentAssert.notNull(phoneNoInfo, "微信手机号授权失败：微信未返回手机号信息");
        // purePhoneNumber 不带区号，与系统内手机号格式一致，优先使用
        String phone = StrUtil.isNotEmpty(phoneNoInfo.getPurePhoneNumber())
                ? phoneNoInfo.getPurePhoneNumber() : phoneNoInfo.getPhoneNumber();
        ArgumentAssert.notEmpty(phone, "微信手机号授权失败：微信未返回手机号");
        return phone;
    }

    private String getOpenid(String loginCode) {
        WxMaJscode2SessionResult sessionInfo;
        try {
            sessionInfo = wxMaService.getUserService().getSessionInfo(loginCode);
        } catch (WxErrorException e) {
            log.error("微信登录凭证校验失败: errcode={}, errmsg={}", e.getError().getErrorCode(), e.getError().getErrorMsg(), e);
            throw new BizException("微信登录失败：" + e.getError().getErrorMsg());
        }
        ArgumentAssert.notNull(sessionInfo, "微信登录失败：微信未返回会话信息");
        ArgumentAssert.notEmpty(sessionInfo.getOpenid(), "微信登录失败：未获取到openid");
        return sessionInfo.getOpenid();
    }
}
