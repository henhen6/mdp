package top.mddata.workbench.service.social;

import top.mddata.workbench.enumeration.SocialActionEnum;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.service.social.model.PendingSocialUser;
import top.mddata.workbench.service.social.model.SocialIdentity;
import top.mddata.workbench.service.social.model.SocialReauth;
import top.mddata.workbench.service.social.model.SocialState;

/**
 * 第三方登录一次性票据服务
 *
 * <p>提供 OAuth state、未绑定待选择票据和敏感操作二次验证票据的签发、校验与
 * 原子消费。所有票据均为高熵随机串，凭独立消费标记保证一次性，不做 get 后 del。</p>
 *
 * @author tangyh
 */
public interface SocialTicketService {

    /**
     * 创建 OAuth state
     *
     * @param state 授权状态上下文
     * @return 随机 state 票据
     */
    String createState(SocialState state);

    /**
     * 消费 OAuth state，校验平台与动作
     *
     * @param ticket   state 票据
     * @param provider 期望平台
     * @param action   期望动作
     * @return 授权状态上下文
     */
    SocialState consumeState(String ticket, SocialProviderEnum provider, SocialActionEnum action);

    /**
     * 为已验证但未绑定的第三方身份创建待选择票据
     *
     * @param identity 第三方身份
     * @return 随机待选择票据
     */
    String createPending(SocialIdentity identity);

    /**
     * 读取待选择票据，确认未消费
     *
     * @param ticket 待选择票据
     * @return 待选择上下文
     */
    PendingSocialUser getPending(String ticket);

    /**
     * 消费待选择票据
     *
     * @param ticket 待选择票据
     */
    void consumePending(String ticket);

    /**
     * 登记一次凭据校验失败，达到上限后使待选择票据失效
     *
     * @param ticket 待选择票据
     */
    void registerPendingFailure(String ticket);

    /**
     * 创建敏感操作二次验证票据
     *
     * @param reauth 二次验证上下文
     * @return 随机二次验证票据
     */
    String createReauth(SocialReauth reauth);

    /**
     * 读取二次验证票据，校验用户、动作与平台
     *
     * @param ticket   二次验证票据
     * @param userId   期望用户
     * @param action   期望动作
     * @param provider 期望平台
     * @return 二次验证上下文
     */
    SocialReauth getReauth(String ticket, Long userId, SocialActionEnum action,
                           SocialProviderEnum provider);

    /**
     * 消费二次验证票据
     *
     * @param ticket 二次验证票据
     */
    void consumeReauth(String ticket);
}
