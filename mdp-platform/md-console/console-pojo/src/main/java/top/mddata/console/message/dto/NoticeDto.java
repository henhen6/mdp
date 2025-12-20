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

/**
 * 站内通知 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "站内通知Dto")
public class NoticeDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 短信记录ID
     */
    @NotNull(message = "请填写短信记录ID", groups = BaseEntity.Update.class)
    @Schema(description = "短信记录ID")
    private Long id;

    /**
     * 消息任务
     */
    @NotNull(message = "请填写消息任务")
    @Schema(description = "消息任务")
    private Long taskId;

    /**
     * 标题
     */
    @NotEmpty(message = "请填写标题")
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
     * 消息分类
     * [1-待办 2-公告 3-预警]
     */
    @NotNull(message = "请填写消息分类")
    @Schema(description = "消息分类")
    private Integer msgCategory;

    /**
     * 发布人
     */
    @Size(max = 255, message = "发布人长度不能超过{max}")
    @Schema(description = "发布人")
    private String author;

    /**
     * 跳转地址
     */
    @Size(max = 255, message = "跳转地址长度不能超过{max}")
    @Schema(description = "跳转地址")
    private String url;

}
