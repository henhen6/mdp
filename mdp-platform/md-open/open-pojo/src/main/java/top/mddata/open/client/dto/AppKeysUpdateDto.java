package top.mddata.open.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用秘钥修改
 * @author henhen6
 * @since 2025/11/20 20:48
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "应用秘钥修改")
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
     * 秘钥格式
     * [1-PKCS8(JAVA适用) 2-PKCS1(非JAVA适用)]
     */
    @NotNull(message = "请填写秘钥格式")
    @Schema(description = "秘钥格式")
    private Integer keyFormat;

    /**
     * 开发者公钥
     * 平台方用来校验开发者推送过来的数据
     */
    @NotEmpty(message = "请填写开发者公钥")
    @Size(max = 16383, message = "开发者公钥长度不能超过{max}")
    @Schema(description = "开发者公钥")
    private String publicKeyApp;

}
