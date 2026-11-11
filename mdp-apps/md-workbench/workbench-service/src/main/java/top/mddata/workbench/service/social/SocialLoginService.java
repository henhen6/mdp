package top.mddata.workbench.service.social;

import top.mddata.workbench.dto.SocialCallbackDto;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.vo.SocialLoginResultVo;
import top.mddata.workbench.vo.SocialProviderVo;

import java.util.List;

/**
 * 第三方登录编排服务。
 *
 * <p>负责已启用平台列表、授权地址生成，以及回调后的登录编排：已绑定身份直接登录，
 * 未绑定身份只签发待选择票据，不创建用户。</p>
 *
 * @author tangyh
 */
public interface SocialLoginService {

    /**
     * 列出已启用的第三方登录平台
     *
     * @return 平台展示信息
     */
    List<SocialProviderVo> providers();

    /**
     * 生成带一次性 state 的第三方授权地址
     *
     * @param provider 第三方平台
     * @return 授权跳转地址
     */
    String loginUrl(SocialProviderEnum provider);

    /**
     * 处理第三方登录回调
     *
     * @param callback 回调参数
     * @return 登录成功或未绑定待选择结果
     */
    SocialLoginResultVo login(SocialCallbackDto callback);
}
