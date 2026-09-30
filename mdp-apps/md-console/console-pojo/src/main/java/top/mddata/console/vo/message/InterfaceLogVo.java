package top.mddata.console.vo.message;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.console.entity.message.base.InterfaceLogBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 接口执行日志记录 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "接口执行日志记录")
@Table(InterfaceLogBase.TABLE_NAME)
public class InterfaceLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * id
     */
    @Id
    @Schema(description = "id")
    private Long id;

    /**
     * 接口ID
     */
    @Schema(description = "接口ID")
    private Long interfaceStatId;
    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    private Long msgTaskId;

    /**
     * 执行开始时间
     */
    @Schema(description = "执行开始时间")
    private LocalDateTime execStartTime;
    /**
     * 执行结束时间
     */
    @Schema(description = "执行结束时间")
    private LocalDateTime execEndTime;

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
    private String param;

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
