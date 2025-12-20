package top.mddata.console.message.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 站内通知实体类。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class NoticeBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_notice";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息任务
     */
    private Long taskId;

    /**
     * 标题
     */
    private String title;

    /**
     * 发送内容
     */
    private String content;

    /**
     * 消息分类
     * [1-待办 2-公告 3-预警]
     */
    private Integer msgCategory;

    /**
     * 发布人
     */
    private String author;

    /**
     * 跳转地址
     */
    private String url;

}
