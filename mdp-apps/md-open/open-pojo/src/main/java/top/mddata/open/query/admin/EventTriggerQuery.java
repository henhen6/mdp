package top.mddata.open.query.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事件触发 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2026-01-12 21:29:13
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Schema(description = "事件触发")
public class EventTriggerQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 事件编码
     */
    @Schema(description = "事件编码")
    private String eventCode;

    /**
     * 事件ID
     */
    @Schema(description = "事件ID")
    private Long eventId;

    /**
     * 触发时间
     */
    @Schema(description = "触发时间")
    private LocalDateTime triggerAt;

    /**
     * 事件内容
     */
    @Schema(description = "事件内容")
    private String eventContent;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

}
