package top.mddata.console.message.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息任务实体类。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class MsgTaskBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_msg_task";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息模板
     */
    private Long templateId;

    /**
     * 执行状态
     * [0-草稿 1-待执行 2-执行成功 3-执行失败]
     */
    private Integer status;

    /**
     * 发送渠道
     * [1-后台发送 2-API发送 3-JOB发送]
     */
    private Integer channel;

    /**
     * 消息类型
     * [1-站内信 2-短信 3-邮件]
     */
    private Integer type;

    /**
     * 消息分类
     * 站内信专属
     * [1-待办 2-公告 3-预警]
     */
    private Integer msgCategory;

    /**
     * 接收范围
     * 站内信专属
     * [0-所有人 1-指定用户 2-指定角色 3-指定部门]
     */
    private Integer recipientScope;

    /**
     * 跳转地址
     * 站内信专属
     */
    private String url;
    /**
     * 参数
     * 需要封装为[{‘key’:‘‘,;’value’:‘‘}, {’key2’:‘‘, ’value2’:‘‘}]格式
     */
    private String param;

    /**
     * 标题
     */
    private String title;

    /**
     * 发送内容
     */
    private String content;


    /**
     * 是否定时发送
     */
    private Boolean isTiming;

    /**
     * 发送时间
     */
    private LocalDateTime scheduledSendTime;

    /**
     * 业务ID
     * Api发送和job发送时指定，用于业务追踪
     */
    private Long bizId;

    /**
     * 业务类型
     * Api发送和job发送时指定，用于业务追踪
     */
    private String bizType;

    /**
     * 发送人
     */
    private String author;

    /**
     * 发送人ID
     */
    private Long senderId;

}
