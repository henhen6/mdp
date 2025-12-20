package top.mddata.console.message.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.console.message.entity.base.NoticeBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内通知 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "站内通知Vo")
@Table(NoticeBase.TABLE_NAME)
public class NoticeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 短信记录ID
     */
    @Id
    @Schema(description = "短信记录ID")
    private Long id;

    /**
     * 消息任务
     */
    @Schema(description = "消息任务")
    private Long taskId;

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
     * 消息分类
     * [1-待办 2-公告 3-预警]
     */
    @Schema(description = "消息分类")
    private Integer msgCategory;

    /**
     * 发布人
     */
    @Schema(description = "发布人")
    private String author;

    /**
     * 跳转地址
     */
    @Schema(description = "跳转地址")
    private String url;

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
