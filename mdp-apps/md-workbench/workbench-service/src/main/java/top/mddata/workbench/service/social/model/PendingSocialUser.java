package top.mddata.workbench.service.social.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 未绑定第三方用户的待选择上下文
 *
 * <p>第三方回调未查询到绑定时，以 pendingTicket 缓存已验证的第三方身份，
 * 供用户选择“绑定已有账号”或“创建新账号”，票据 10 分钟内有效。</p>
 *
 * @author tangyh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PendingSocialUser implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 已验证的第三方身份 */
    private SocialIdentity identity;
}
