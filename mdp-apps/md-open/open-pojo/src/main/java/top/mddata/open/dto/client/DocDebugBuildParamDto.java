package top.mddata.open.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 接口调试-组装请求参数
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "接口调试-组装请求参数")
public class DocDebugBuildParamDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文档ID
     */
    @NotNull(message = "请填写文档ID")
    @Schema(description = "文档ID")
    private Long docId;

    /**
     * 环境：sandbox 沙箱 / prod 生产
     */
    @NotBlank(message = "请选择环境")
    @Pattern(regexp = "sandbox|prod", message = "环境仅支持 sandbox 或 prod")
    @Schema(description = "环境：sandbox 沙箱 / prod 生产")
    private String env;

    /**
     * 平台分配的 appKey
     */
    @NotBlank(message = "请填写 appKey")
    @Schema(description = "平台分配的 appKey")
    private String appKey;

    /**
     * 开发者私钥（为空则不计算签名，适配免签名接口）
     */
    @Schema(description = "开发者私钥，为空则不签名")
    private String privateKey;

    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌")
    private String accessToken;

    /**
     * 版本号，为空时取文档版本，文档也无则默认 1.0
     */
    @Schema(description = "版本号")
    private String version;

    /**
     * 签名方式
     */
    @Schema(description = "签名方式", defaultValue = "RSA2")
    private String signType;

    /**
     * 字符集
     */
    @Schema(description = "字符集", defaultValue = "UTF-8")
    private String charset;

    /**
     * 格式
     */
    @Schema(description = "格式", defaultValue = "json")
    private String format;

    /**
     * 业务参数 JSON 字符串
     */
    @NotBlank(message = "请填写业务参数")
    @Schema(description = "业务参数 JSON 字符串")
    private String bizContent;
}
