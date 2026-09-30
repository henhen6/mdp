package top.mddata.open.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 回调任务 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2026-01-12 21:28:36
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "回调任务")
public class NotifyInfoDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属调用
     */
    @Schema(description = "所属调用")
    @NotNull(message = "所属调用必填")
    private Long callLogId;
    /**
     * 所属应用
     */
    @NotNull(message = "请填写所属应用")
    @Schema(description = "所属应用")
    private Long appId;

    /**
     * 应用Key
     */
    @NotEmpty(message = "请填写应用Key")
    @Size(max = 100, message = "应用Key长度不能超过{max}")
    @Schema(description = "应用Key")
    private String appKey;

    /**
     * 接口名称
     */
    @NotEmpty(message = "请填写接口名称")
    @Size(max = 128, message = "接口名称长度不能超过{max}")
    @Schema(description = "接口名称")
    private String apiName;

    /**
     * 接口版本
     */
    @NotEmpty(message = "请填写接口版本")
    @Size(max = 16, message = "接口版本长度不能超过{max}")
    @Schema(description = "接口版本")
    private String apiVersion;

    /**
     * 编码
     */
    @NotBlank(message = "charset必填")
    private String charset;

    /**
     * 访问令牌，没有返回null
     */
    private String accessToken;

    /**
     * 客户端ip
     */
    private String clientIp;

    /**
     * 回调地址
     */
    @Size(max = 255, message = "回调地址长度不能超过{max}")
    @Schema(description = "回调地址")
    private String notifyUrl;

    /**
     * 业务参数
     */
    @NotNull(message = "bizParams必填")
    private Map<String, Object> bizParams;

    /**
     * 备注
     */
    @Size(max = 256, message = "备注长度不能超过{max}")
    @Schema(description = "备注")
    private String remark;

}
