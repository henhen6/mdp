package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import top.mddata.workbench.enumeration.SocialProviderEnum;

/**
 * 第三方登录回调入参
 *
 * @author tangyh
 */
@Data
@Schema(title = "SocialCallbackDto", description = "第三方登录回调入参")
public class SocialCallbackDto {

    /** 第三方平台 */
    @Schema(description = "第三方平台")
    @NotNull(message = "第三方平台不能为空")
    private SocialProviderEnum provider;

    /** 第三方回调携带的授权码 */
    @Schema(description = "授权码")
    @NotEmpty(message = "授权码不能为空")
    private String code;

    /** 回调原样带回的 state */
    @Schema(description = "OAuth state")
    @NotEmpty(message = "state不能为空")
    private String state;

    /** 客户端设备id，可选 */
    @Schema(description = "客户端设备id")
    private String deviceId;

    /** 设备信息，可选 */
    @Schema(description = "设备信息")
    private String deviceInfo;
}
