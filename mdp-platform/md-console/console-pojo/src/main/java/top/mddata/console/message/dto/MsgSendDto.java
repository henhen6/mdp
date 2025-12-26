package top.mddata.console.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.model.Kv;
import top.mddata.console.message.enumeration.MsgChannelEnum;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 根据模版编码发送消息任务
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "消息发送抽象Dto")
public abstract class MsgSendDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板标识
     */
    @NotEmpty(message = "请填写模板标识")
    @Size(max = 255, message = "模板标识长度不能超过{max}")
    @Schema(description = "模板标识")
    private String templateKey;
    /**
     * 参数;
     * <p>
     * 需要封装为[{{‘key’:'', ’value’:''}, {'key2':'', 'value2':''}]格式
     */
    @Schema(description = "参数")
    private List<Kv> paramList;

    /**
     * 是否定时发送
     */
    @Schema(description = "是否定时发送")
    private Boolean isTiming;

    /**
     * 计划发送时间
     */
    @Schema(description = "计划发送时间")
    private LocalDateTime scheduledSendTime;
    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    private LocalDateTime sendTime;
    /**
     * 业务ID
     */
    @Schema(description = "业务ID")
    private Long bizId;
    /**
     * 业务类型
     */
    @Schema(description = "业务类型")
    @Size(max = 255, message = "业务类型长度不能超过{max}")
    private String bizType;

    /**
     * 发送渠道
     * [1-后台发送 2-API发送 3-JOB发送]
     */
    @Schema(description = "发送渠道")
    private MsgChannelEnum channel;


}
