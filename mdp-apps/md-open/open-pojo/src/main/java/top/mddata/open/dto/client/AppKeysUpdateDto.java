package top.mddata.open.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 开发者修改应用通知配置
 *
 * @author henhen6
 * @since 2025/11/20 20:48
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "应用秘钥")
public class AppKeysUpdateDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属应用
     */
    @NotNull(message = "请填写所属应用")
    @Schema(description = "所属应用")
    private Long appId;

    /**
     * 开发者应用公钥（RSA2签名校验用）
     */
    @Schema(description = "开发者应用公钥（RSA2签名校验用）")
    private String publicKeyApp;
}
