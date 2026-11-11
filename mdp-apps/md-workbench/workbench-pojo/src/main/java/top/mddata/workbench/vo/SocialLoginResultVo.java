package top.mddata.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 第三方登录结果
 *
 * <p>已绑定身份返回 SUCCESS 和会话信息；未绑定身份返回 PENDING 和待选择票据，
 * 公开响应不携带 openId、unionId、clientId 等身份标识。</p>
 *
 * @author tangyh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SocialLoginResultVo", description = "第三方登录结果")
public class SocialLoginResultVo {

    /** 登录成功 */
    public static final String STATUS_SUCCESS = "SUCCESS";
    /** 身份未绑定，等待用户选择 */
    public static final String STATUS_PENDING = "PENDING";

    /** 状态：SUCCESS 或 PENDING */
    @Schema(description = "状态：SUCCESS 或 PENDING")
    private String status;

    /** 登录成功时的会话信息 */
    @Schema(description = "登录会话信息")
    private LoginVo login;

    /** 未绑定时的待选择票据 */
    @Schema(description = "待选择票据")
    private String pendingTicket;

    /** 未绑定时的平台编码 */
    @Schema(description = "平台编码")
    private String provider;

    /** 未绑定时的第三方昵称 */
    @Schema(description = "第三方昵称")
    private String nickname;

    /**
     * 构造登录成功结果
     *
     * @param login 会话信息
     * @return 登录结果
     */
    public static SocialLoginResultVo success(LoginVo login) {
        return new SocialLoginResultVo(STATUS_SUCCESS, login, null, null, null);
    }

    /**
     * 构造未绑定待选择结果
     *
     * @param ticket   待选择票据
     * @param provider 平台编码
     * @param nickname 第三方昵称
     * @return 登录结果
     */
    public static SocialLoginResultVo pending(String ticket, String provider, String nickname) {
        return new SocialLoginResultVo(STATUS_PENDING, null, ticket, provider, nickname);
    }
}
