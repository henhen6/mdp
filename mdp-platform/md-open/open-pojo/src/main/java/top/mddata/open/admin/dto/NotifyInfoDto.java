package top.mddata.open.admin.dto;

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
 * 回调任务 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "回调任务Dto")
public class NotifyInfoDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
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
    @Size(max = 255, message = "事件类型长度不能超过{max}")
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
    @NotNull(message = "请填写通知类型")
    @Schema(description = "通知类型")
    private Integer notifyType;

    /**
     * 所属应用
     */
    @NotNull(message = "请填写所属应用")
    @Schema(description = "所属应用")
    private Long appId;

    /**
     * 应用秘钥
     */
    @NotEmpty(message = "请填写应用秘钥")
    @Size(max = 100, message = "应用秘钥长度不能超过{max}")
    @Schema(description = "应用秘钥")
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
     * 回调url
     */
    @Size(max = 255, message = "回调url长度不能超过{max}")
    @Schema(description = "回调url")
    private String notifyUrl;

    /**
     * 请求参数
     */
    @Size(max = 16383, message = "请求参数长度不能超过{max}")
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
