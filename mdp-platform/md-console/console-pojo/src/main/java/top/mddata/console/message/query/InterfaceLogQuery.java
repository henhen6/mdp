package top.mddata.console.message.query;

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
 * 接口执行日志记录 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Schema(description = "接口执行日志记录Query")
public class InterfaceLogQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 接口ID
     */
    @Schema(description = "接口ID")
    private Long interfaceStatId;

    /**
     * 执行时间
     */
    @Schema(description = "执行时间")
    private LocalDateTime execTime;

    /**
     * 执行状态
     * [1-初始化 2-成功 3-失败]
     */
    @Schema(description = "执行状态")
    private Integer status;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    private String params;

    /**
     * 接口返回
     */
    @Schema(description = "接口返回")
    private String result;

    /**
     * 异常信息
     */
    @Schema(description = "异常信息")
    private String errorMsg;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;

    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private Long updatedBy;

}
