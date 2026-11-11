package top.mddata.workbench.service.social;

import top.mddata.workbench.service.social.model.SocialIdentity;

/**
 * 第三方登录平台适配器。
 *
 * @author tangyh
 */
public interface SocialProvider {

    /**
     * 生成带 state 的第三方授权地址
     *
     * @param state 服务端签发的一次性 state
     * @return 授权跳转地址
     */
    String authorize(String state);

    /**
     * 使用授权码换取第三方身份
     *
     * @param code  第三方回调携带的授权码
     * @param state 回调原样带回的 state
     * @return 归一化后的第三方身份
     */
    SocialIdentity exchange(String code, String state);
}
