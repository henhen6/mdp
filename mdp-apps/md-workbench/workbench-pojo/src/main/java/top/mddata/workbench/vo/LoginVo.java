package top.mddata.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录返回值
 * @author henhen6
 * @since 2025/6/30 12:52
 */
@Data
public class LoginVo {
    @Schema(description = "用户id")
    private Long id;

    /** token 名称 */
    @Schema(description = "token名称")
    private String tokenName;

    /** token 值 */
    @Schema(description = "token值")
    private String tokenValue;


    @Schema(description = "刷新token")
    private String refreshToken;

    /**
     * Token 剩余有效时间：单位：秒
     */
    @Schema(description = "token剩余有效时间")
    private Long expire;

    /**
     * 登录状态：TOKEN-已颁发令牌 TWO_FACTOR-待二次验证
     */
    @Schema(description = "登录状态", example = "TOKEN", allowableValues = {"TOKEN", "TWO_FACTOR"})
    private String status;

    @Schema(description = "双因素认证一次性票据，仅 status=TWO_FACTOR 时非空")
    private String twoFactorTicket;

    @Schema(description = "用户开通的验证渠道 PHONE/EMAIL/BOTH，仅 status=TWO_FACTOR 时非空")
    private String twoFactorChannel;

    @Schema(description = "脱敏接收目标，仅 status=TWO_FACTOR 时非空")
    private String twoFactorMaskedTarget;

    /**
     * 直接登录成功（未触发双因素）
     */
    public static LoginVo token() {
        LoginVo vo = new LoginVo();
        vo.setStatus("TOKEN");
        return vo;
    }

}
