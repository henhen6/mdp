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
import java.util.List;

/**
 * 消息任务 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "消息任务Dto")
public class MsgTaskDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;


    /**
     * 消息分类
     * [1-待办 2-公告 3-预警]
     */
    @Schema(description = "消息分类")
    @NotNull(message = "请填写消息分类")
    private Integer msgCategory;


    /**
     * 标题
     */
    @Size(max = 255, message = "标题长度不能超过{max}")
    @Schema(description = "标题")
    @NotEmpty(message = "请填写标题")
    private String title;

    /**
     * 发送内容
     */
    @NotEmpty(message = "请填写发送内容")
    @Size(max = 536870911, message = "发送内容长度不能超过{max}")
    @Schema(description = "发送内容")
    private String content;

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
     * 发布人
     */
    @Size(max = 255, message = "发布人长度不能超过{max}")
    @Schema(description = "发布人")
    private String author;

    /**
     * 接收范围
     * [0-所有人 1-指定用户 2-指定角色 3-指定部门]
     */
    @NotNull(message = "请填写接收范围")
    @Schema(description = "接收范围")
    private Integer recipientScope;

    @Schema(description = "接收人列表")
    private List<Long> recipientList;

}
