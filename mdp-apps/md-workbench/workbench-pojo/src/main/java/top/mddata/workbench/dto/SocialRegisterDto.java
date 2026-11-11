package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 第三方身份创建新 MDP 账号入参
 *
 * @author tangyh
 */
@Data
@Schema(title = "SocialRegisterDto", description = "第三方身份创建新账号入参")
public class SocialRegisterDto {

    /** 第三方登录待选择票据 */
    @Schema(description = "待选择票据")
    @NotBlank(message = "待选择票据不能为空")
    private String pendingTicket;

    /** 显示姓名，为空时默认取第三方昵称 */
    @Schema(description = "显示姓名")
    private String name;

    /** 注册身份（总公司、开发者等） */
    @Schema(description = "注册身份")
    @NotNull(message = "注册身份不能为空")
    private Integer nature;

    /** 用户协议确认 */
    @Schema(description = "已阅读并同意用户协议")
    @AssertTrue(message = "请先阅读并同意用户协议")
    private Boolean agreement;

    /** 客户端设备id，可选 */
    @Schema(description = "客户端设备id")
    private String deviceId;

    /** 设备信息，可选 */
    @Schema(description = "设备信息")
    private String deviceInfo;
}
