package top.mddata.common.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户第三方身份绑定实体基类。
 *
 * @author tangyh
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserSocialAuthBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_user_social_auth";

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 第三方平台（SocialProviderEnum 名称） */
    private String provider;

    /** 第三方应用ID */
    private String clientId;

    /** 应用内用户标识 */
    private String openId;

    /** 跨应用用户标识 */
    private String unionId;

    /** 第三方昵称 */
    private String nickname;

    /** 绑定时间 */
    private LocalDateTime boundAt;

    /** 删除人 */
    private Long deletedBy;

    /** 删除标志 */
    private Long deletedAt;
}
