package top.mddata.workbench.service.social.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.workbench.enumeration.SocialActionEnum;
import top.mddata.workbench.enumeration.SocialProviderEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * OAuth 授权状态上下文
 *
 * <p>与随机 state 票据一起缓存，回调时校验平台与动作；个人中心场景通过 userId
 * 绑定发起人，requestedAction/requestedProvider 携带真实操作目标。</p>
 *
 * @author tangyh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SocialState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 发起授权的第三方平台 */
    private SocialProviderEnum provider;
    /** 授权动作 */
    private SocialActionEnum action;
    /** 发起操作的用户ID，登录场景为空 */
    private Long userId;
    /** 个人中心场景下真实请求的动作 */
    private SocialActionEnum requestedAction;
    /** 个人中心场景下真实请求的目标平台 */
    private SocialProviderEnum requestedProvider;
    /** 绑定时关联的二次验证票据ID */
    private String reauthTicketId;
}
