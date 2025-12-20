package top.mddata.console.message.dto;

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
 * 消息任务 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "消息任务Dto")
public class MsgTaskDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 短信记录ID
     */
    @NotNull(message = "请填写短信记录ID", groups = BaseEntity.Update.class)
    @Schema(description = "短信记录ID")
    private Long id;

    /**
     * 消息模板
     */
    @Schema(description = "消息模板")
    private Long templateId;

    /**
     * 消息类型
     * [1-站内信 2-短信 3-邮件]
     */
    @NotNull(message = "请填写消息类型")
    @Schema(description = "消息类型")
    private Integer type;

    /**
     * 执行状态
     * [0-草稿 1-待执行 2-执行成功 3-执行失败]
     */
    @NotNull(message = "请填写执行状态")
    @Schema(description = "执行状态")
    private Integer status;

    /**
     * 发送渠道
     * [1-后台发送 2-API发送 3-JOB发送]
     */
    @NotNull(message = "请填写发送渠道")
    @Schema(description = "发送渠道")
    private Integer channel;

    /**
     * 消息分类
     * [1-待办 2-公告 3-预警]
     */
    @Schema(description = "消息分类")
    private Integer msgCategory;

    /**
     * 接收范围
     * [0-所有人 1-指定用户 2-指定角色 3-指定部门]
     */
    @NotNull(message = "请填写接收范围")
    @Schema(description = "接收范围")
    private Integer recipientScope;

    /**
     * 参数
     * 需要封装为[{‘key’:‘‘,;’value’:‘‘}, {’key2’:‘‘, ’value2’:‘‘}]格式
     */
    @Size(max = 16383, message = "参数长度不能超过{max}")
    @Schema(description = "参数")
    private String param;

    /**
     * 标题
     */
    @Size(max = 255, message = "标题长度不能超过{max}")
    @Schema(description = "标题")
    private String title;

    /**
     * 发送内容
     */
    @NotEmpty(message = "请填写发送内容")
    @Size(max = 536870911, message = "发送内容长度不能超过{max}")
    @Schema(description = "发送内容")
    private String content;

    /**
     * 跳转地址
     */
    @Size(max = 255, message = "跳转地址长度不能超过{max}")
    @Schema(description = "跳转地址")
    private String url;

    /**
     * 是否定时发送
     */
    @Schema(description = "是否定时发送")
    private Boolean isTiming;

    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    private LocalDateTime scheduledSendTime;

    /**
     * 业务ID
     * Api发送和job发送时指定，用于业务追踪
     */
    @Schema(description = "业务ID")
    private Long bizId;

    /**
     * 业务类型
     * Api发送和job发送时指定，用于业务追踪
     */
    @Size(max = 255, message = "业务类型长度不能超过{max}")
    @Schema(description = "业务类型")
    private String bizType;

    /**
     * 发送人
     */
    @Size(max = 255, message = "发送人长度不能超过{max}")
    @Schema(description = "发送人")
    private String author;

    /**
     * 发送人ID
     */
    @Schema(description = "发送人ID")
    private Long senderId;

}
