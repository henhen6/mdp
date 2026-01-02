package top.mddata.open.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 调用日志 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "调用日志Dto")
public class ApiCallLogDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 调用链id
     */
    @Size(max = 255, message = "调用链id长度不能超过{max}")
    @Schema(description = "调用链id")
    private String traceId;

    /**
     * 所属应用
     */
    @NotNull(message = "请填写所属应用")
    @Schema(description = "所属应用")
    private Long appId;

    /**
     * 应用秘钥
     */
    @Size(max = 255, message = "应用秘钥长度不能超过{max}")
    @Schema(description = "应用秘钥")
    private String appKey;

    /**
     * 应用名称
     */
    @Size(max = 255, message = "应用名称长度不能超过{max}")
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 请求IP
     */
    @Size(max = 255, message = "请求IP长度不能超过{max}")
    @Schema(description = "请求IP")
    private String requestIp;

    /**
     * 所属接口
     */
    @NotNull(message = "请填写所属接口")
    @Schema(description = "所属接口")
    private Long apiId;

    /**
     * 接口名称
     */
    @Size(max = 255, message = "接口名称长度不能超过{max}")
    @Schema(description = "接口名称")
    private String apiName;

    /**
     * 版本号
     */
    @Size(max = 255, message = "版本号长度不能超过{max}")
    @Schema(description = "版本号")
    private String apiVersion;

    /**
     * 接口类名
     */
    @Size(max = 255, message = "接口类名长度不能超过{max}")
    @Schema(description = "接口类名")
    private String interfaceClassName;

    /**
     * 方法名称
     */
    @Size(max = 255, message = "方法名称长度不能超过{max}")
    @Schema(description = "方法名称")
    private String methodName;

    /**
     * 请求参数
     */
    @Size(max = 536870911, message = "请求参数长度不能超过{max}")
    @Schema(description = "请求参数")
    private String paramInfo;

    /**
     * 请求头
     */
    @Size(max = 16383, message = "请求头长度不能超过{max}")
    @Schema(description = "请求头")
    private String requestHeader;

    /**
     * 请求时间
     */
    @Schema(description = "请求时间")
    private LocalDateTime requestTime;

    /**
     * 执行状态
     * [1-执行成功,2-执行失败]
     */
    @Size(max = 1, message = "执行状态长度不能超过{max}")
    @Schema(description = "执行状态")
    private String execStatus;

    /**
     * 响应内容
     */
    @Size(max = 536870911, message = "响应内容长度不能超过{max}")
    @Schema(description = "响应内容")
    private String responseData;

    /**
     * 响应时间
     */
    @Schema(description = "响应时间")
    private LocalDateTime responseTime;

    /**
     * 异常消息
     */
    @Size(max = 536870911, message = "异常消息长度不能超过{max}")
    @Schema(description = "异常消息")
    private String errorMsg;

}
