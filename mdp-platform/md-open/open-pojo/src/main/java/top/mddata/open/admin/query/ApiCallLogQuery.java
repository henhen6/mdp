package top.mddata.open.admin.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 调用日志 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Schema(description = "调用日志Query")
public class ApiCallLogQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 调用链id
     */
    @Schema(description = "调用链id")
    private String traceId;

    /**
     * 所属应用
     */
    @Schema(description = "所属应用")
    private Long appId;

    /**
     * 应用秘钥
     */
    @Schema(description = "应用秘钥")
    private String appKey;

    /**
     * 应用名称
     */
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 请求IP
     */
    @Schema(description = "请求IP")
    private String requestIp;

    /**
     * 所属接口
     */
    @Schema(description = "所属接口")
    private Long apiId;

    /**
     * 接口名称
     */
    @Schema(description = "接口名称")
    private String apiName;

    /**
     * 版本号
     */
    @Schema(description = "版本号")
    private String apiVersion;

    /**
     * 接口类名
     */
    @Schema(description = "接口类名")
    private String interfaceClassName;

    /**
     * 方法名称
     */
    @Schema(description = "方法名称")
    private String methodName;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    private String paramInfo;

    /**
     * 请求头
     */
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
    @Schema(description = "执行状态")
    private String execStatus;

    /**
     * 响应内容
     */
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
    @Schema(description = "异常消息")
    private String errorMsg;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

}
