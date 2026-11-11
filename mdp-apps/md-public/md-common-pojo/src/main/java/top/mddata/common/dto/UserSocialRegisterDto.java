package top.mddata.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.mddata.common.entity.User;
import top.mddata.common.entity.UserSocialAuth;

import java.io.Serial;
import java.io.Serializable;

/**
 * 社交注册入参：待创建用户与初始第三方绑定。
 *
 * <p>两者必须在控制台同一本地事务内落库，避免微服务场景拆成两个远程写请求。</p>
 *
 * @author tangyh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSocialRegisterDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 待创建用户 */
    private User user;

    /** 初始第三方绑定 */
    private UserSocialAuth socialAuth;
}
