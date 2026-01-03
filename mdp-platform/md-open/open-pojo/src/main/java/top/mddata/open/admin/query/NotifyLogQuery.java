package top.mddata.open.admin.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;

/**
 * 回调日志 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Schema(description = "回调日志Query")
public class NotifyLogQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属回调
     */
    @Schema(description = "所属回调")
    @NotNull(message = "所属回调不能为空")
    private Long notifyInfoId;

    /**
     * 响应内容
     */
    @Schema(description = "响应内容")
    private String responseData;


    /**
     * 状态
     * [1-执行成功 2-执行失败]
     */
    @Schema(description = "状态")
    private String execStatus;

    /**
     * 失败原因
     */
    @Schema(description = "失败原因")
    private String errorMsg;

}
