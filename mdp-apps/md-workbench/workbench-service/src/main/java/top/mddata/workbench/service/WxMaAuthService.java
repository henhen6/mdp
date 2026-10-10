package top.mddata.workbench.service;

import top.mddata.common.entity.User;

/**
 * 微信小程序认证 服务层。
 *
 * <p>封装与微信服务器的交互（code 换 openid、授权码换手机号）以及
 * 「openid → 手机号 → 自动注册」的用户匹配逻辑，与 sa-token 签发令牌解耦，便于单元测试。</p>
 *
 * @author tangyh
 * @since 2026/10/10
 */
public interface WxMaAuthService {

    /**
     * 微信一键登录：查找或注册用户。
     *
     * <p>匹配顺序：</p>
     * <ol>
     *     <li>loginCode 换 openid，按 wxOpenid 查到用户则直接返回（不再消耗手机号授权码）</li>
     *     <li>openid 未绑定且传了 phoneCode，则换手机号，按手机号查到用户则绑定 openid，
     *     查不到则按手机号自动注册</li>
     *     <li>openid 未绑定且未传 phoneCode（个人主体小程序无组件权限），
     *     则按 openid 静默注册（不绑定手机号）</li>
     * </ol>
     *
     * @param loginCode wx.login() 返回的临时登录凭证
     * @param phoneCode 手机号快速验证组件返回的动态令牌，可为空
     * @return 匹配或新注册的用户
     */
    User findOrRegisterUser(String loginCode, String phoneCode);

    /**
     * 用手机号授权码换取手机号（用于换绑手机号场景）。
     *
     * @param phoneCode 手机号快速验证组件返回的动态令牌
     * @return 手机号（优先返回不带区号的 purePhoneNumber）
     */
    String getPhoneNumber(String phoneCode);
}
