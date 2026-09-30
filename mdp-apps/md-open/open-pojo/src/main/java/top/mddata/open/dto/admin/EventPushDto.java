package top.mddata.open.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
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
 * 事件推送任务 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2026-01-12 21:28:36
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "事件推送任务")
public class EventPushDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 事件触发ID
     */
    @NotNull(message = "请填写事件触发ID")
    @Schema(description = "事件触发ID")
    private Long eventTriggerId;

    /**
     * 事件编码
     */
    @NotEmpty(message = "请填写事件编码")
    @Size(max = 255, message = "事件编码长度不能超过{max}")
    @Schema(description = "事件编码")
    private String eventCode;

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
     * 回调地址
     */
    @Size(max = 255, message = "回调地址长度不能超过{max}")
    @Schema(description = "回调地址")
    private String notifyUrl;

    /**
     * 请求参数
     */
    @Size(max = 16383, message = "请求参数长度不能超过{max}")
    @Schema(description = "请求参数")
    private String requestData;

    /**
     * 最近请求时间
     */
    @Schema(description = "最近请求时间")
    private LocalDateTime lastRequestTime;

    /**
     * 下次请求时间
     */
    @Schema(description = "下次请求时间")
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
     * [0-待执行 1-执行成功 2-执行失败 3-重试结束 4-手动结束]
     */
    @Size(max = 1, message = "执行状态长度不能超过{max}")
    @Schema(description = "执行状态")
    private String execStatus;

    /**
     * 备注
     */
    @Size(max = 256, message = "备注长度不能超过{max}")
    @Schema(description = "备注")
    private String remark;

}
