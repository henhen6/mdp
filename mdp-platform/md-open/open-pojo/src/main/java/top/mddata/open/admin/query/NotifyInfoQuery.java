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
 * 回调任务 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Schema(description = "回调任务Query")
public class NotifyInfoQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属事件类型
     */
    @Schema(description = "所属事件类型")
    private Long eventTypeId;

    /**
     * 事件类型
     */
    @Schema(description = "事件类型")
    private String event;

    /**
     * 所属调用
     */
    @Schema(description = "所属调用")
    private Long callLogId;

    /**
     * 通知类型
     * [0-推送 1-回调]
     */
    @Schema(description = "通知类型")
    private Integer notityType;

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
     * 接口名称
     */
    @Schema(description = "接口名称")
    private String apiName;

    /**
     * 接口版本
     */
    @Schema(description = "接口版本")
    private String apiVersion;

    /**
     * 回调url
     */
    @Schema(description = "回调url")
    private String notifyUrl;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    private String requestParam;

    /**
     * 最近一次请求时间
     */
    @Schema(description = "最近一次请求时间")
    private LocalDateTime lastRequestTime;

    /**
     * 下一次请求时间
     */
    @Schema(description = "下一次请求时间")
    private LocalDateTime nextRequestTime;

    /**
     * 最大请求次数
     */
    @Schema(description = "最大请求次数")
    private Integer maxRequestCnt;

    /**
     * 已请求次数
     */
    @Schema(description = "已请求次数")
    private Integer requestCnt;

    /**
     * 执行状态
     * [0-待执行 1-执行成功,2-执行失败, 3-重试结束 4-手动结束]
     */
    @Schema(description = "执行状态")
    private String execStatus;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;

    /**
     * 创建人id
     */
    @Schema(description = "创建人id")
    private Long createdBy;

    /**
     * 修改人id
     */
    @Schema(description = "修改人id")
    private Long updatedBy;

}
