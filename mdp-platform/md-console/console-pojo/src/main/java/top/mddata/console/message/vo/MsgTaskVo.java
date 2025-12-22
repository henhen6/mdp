package top.mddata.console.message.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.console.message.entity.base.MsgTaskBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息任务 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "消息任务Vo")
@Table(MsgTaskBase.TABLE_NAME)
public class MsgTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
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
    @Schema(description = "消息类型")
    private Integer type;

    /**
     * 执行状态
     * [0-草稿 1-待执行 2-执行成功 3-执行失败]
     */
    @Schema(description = "执行状态")
    private Integer status;

    /**
     * 发送渠道
     * [1-后台发送 2-API发送 3-JOB发送]
     */
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
    @Schema(description = "接收范围")
    private Integer recipientScope;

    /**
     * 参数
     * 需要封装为[{‘key’:‘‘,;’value’:‘‘}, {’key2’:‘‘, ’value2’:‘‘}]格式
     */
    @Schema(description = "参数")
    private String param;

    /**
     * 标题
     */
    @Schema(description = "标题")
    private String title;

    /**
     * 发送内容
     */
    @Schema(description = "发送内容")
    private String content;

    /**
     * 跳转地址
     */
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
    @Schema(description = "业务类型")
    private String bizType;

    /**
     * 发送人
     */
    @Schema(description = "发送人")
    private String author;

    /**
     * 发送人ID
     */
    @Schema(description = "发送人ID")
    private Long senderId;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 最后修改人
     */
    @Schema(description = "最后修改人")
    private Long updatedBy;

    /**
     * 最后修改时间
     */
    @Schema(description = "最后修改时间")
    private LocalDateTime updatedAt;

}
