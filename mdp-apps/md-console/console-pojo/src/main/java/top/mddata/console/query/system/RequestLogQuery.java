package top.mddata.console.query.system;

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
 * 请求日志 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2026-05-08 12:35:58
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Schema(description = "请求日志")
public class RequestLogQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * IP地址
     */
    @Schema(description = "IP地址")
    private String ipAddress;
    /**
     * 国家
     */
    @Schema(description = "国家")
    private String ipCountry;
    /**
     * 区域
     */
    @Schema(description = "区域")
    private String ipRegion;
    /**
     * 省
     */
    @Schema(description = "省")
    private String ipProvince;
    /**
     * 市
     */
    @Schema(description = "市")
    private String ipCity;
    /**
     * 运营商
     */
    @Schema(description = "运营商")
    private String ipIsp;
    /**
     * 是否异常
     */
    @Schema(description = "是否异常")
    private Boolean abnormal;

    /**
     * 日志类型
     * [1-查询 2-新增 3-修改 4-删除 9-其他]
     */
    @Schema(description = "日志类型")
    private String logType;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private Long userId;

    /**
     * 操作人
     */
    @Schema(description = "操作人")
    private String userName;

    /**
     * 类路径
     */
    @Schema(description = "类路径")
    private String classPath;

    /**
     * 方法名
     */
    @Schema(description = "方法名")
    private String methodName;

    /**
     * 请求地址
     */
    @Schema(description = "请求地址")
    private String httpUri;

    /**
     * 请求方式
     */
    @Schema(description = "请求方式")
    private String httpMethod;

    /**
     * 操作描述
     */
    @Schema(description = "操作描述")
    private String description;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private LocalDateTime finishTime;

    /**
     * 耗时
     */
    @Schema(description = "耗时")
    private Long consumingTime;

    /**
     * 浏览器
     */
    @Schema(description = "浏览器")
    private String ua;

    /**
     * 浏览器名称
     */
    @Schema(description = "浏览器名称")
    private String browserName;

    /**
     * 浏览器版本
     */
    @Schema(description = "浏览器版本")
    private String browserVersion;

    /**
     * 操作系统
     */
    @Schema(description = "操作系统")
    private String os;
    /**
     * 调用链
     */
    @Schema(description = "调用链")
    private String trace;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

}
