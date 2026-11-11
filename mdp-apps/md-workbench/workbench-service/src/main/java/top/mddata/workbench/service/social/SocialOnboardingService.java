package top.mddata.workbench.service.social;

import top.mddata.workbench.dto.SocialBindExistingDto;
import top.mddata.workbench.dto.SocialRegisterDto;
import top.mddata.workbench.vo.LoginVo;

/**
 * 第三方登录首次选择服务：绑定已有账号或社交注册。
 *
 * @author tangyh
 */
public interface SocialOnboardingService {

    /**
     * 验证已有 MDP 账号凭据后绑定第三方身份并登录
     *
     * @param dto 绑定入参
     * @return 登录返回信息
     */
    LoginVo bindExisting(SocialBindExistingDto dto);

    /**
     * 创建新 MDP 账号、绑定第三方身份并登录
     *
     * @param dto 注册入参
     * @return 登录返回信息
     */
    LoginVo register(SocialRegisterDto dto);
}
