package top.mddata.gateway.sop.pojo.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 调用日志实体类。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ApiCallLogBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_api_call_log";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 调用链id
     */
    private String traceId;

    /**
     * 所属应用
     */
    private Long appId;

    /**
     * 应用秘钥
     */
    private String appKey;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 请求IP
     */
    private String requestIp;

    /**
     * 所属接口
     */
    private Long apiId;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * 版本号
     */
    private String apiVersion;

    /**
     * 接口类名
     */
    private String interfaceClassName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 请求参数
     */
    private String paramInfo;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 请求时间
     */
    private LocalDateTime requestTime;

    /**
     * 执行状态
     * [1-执行成功,2-执行失败]
     */
    private String execStatus;

    /**
     * 响应内容
     */
    private String responseData;

    /**
     * 响应时间
     */
    private LocalDateTime responseTime;

    /**
     * 异常消息
     */
    private String errorMsg;

}
