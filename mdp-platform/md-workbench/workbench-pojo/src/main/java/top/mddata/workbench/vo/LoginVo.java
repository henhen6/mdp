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

}
