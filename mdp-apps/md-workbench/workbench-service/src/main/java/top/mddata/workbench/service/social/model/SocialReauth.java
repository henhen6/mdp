package top.mddata.workbench.service.social.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.workbench.enumeration.SocialActionEnum;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.enumeration.SocialReauthMethodEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 敏感操作二次验证票据上下文
 *
 * <p>绑定用户、动作、平台和验证方式，消费前任何一项不匹配都必须拒绝。</p>
 *
 * @author tangyh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SocialReauth implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 被验证的用户ID */
    private Long userId;
    /** 允许执行的动作 */
    private SocialActionEnum action;
    /** 允许操作的第三方平台 */
    private SocialProviderEnum provider;
    /** 通过的验证方式 */
    private SocialReauthMethodEnum method;
}
