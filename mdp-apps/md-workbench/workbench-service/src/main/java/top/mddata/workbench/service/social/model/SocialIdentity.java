package top.mddata.workbench.service.social.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.workbench.enumeration.SocialProviderEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 第三方身份信息
 *
 * <p>第三方登录成功后由各平台适配器构造，服务层据此建立与平台账号的绑定关系。</p>
 *
 * @author tangyh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SocialIdentity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 第三方平台 */
    private SocialProviderEnum provider;
    /** 第三方应用ID */
    private String clientId;
    /** 应用内用户标识 */
    private String openId;
    /** 跨应用用户标识 */
    private String unionId;
    /** 第三方昵称 */
    private String nickname;
}
