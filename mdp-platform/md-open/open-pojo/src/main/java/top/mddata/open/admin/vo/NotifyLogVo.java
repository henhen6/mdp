package top.mddata.open.admin.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.open.admin.entity.base.NotifyLogBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 回调日志 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "回调日志Vo")
@Table(NotifyLogBase.TABLE_NAME)
public class NotifyLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 主键
     */
    @Id
    @Schema(description = "主键")
    private Long id;

    /**
     * 所属回调
     */
    @Schema(description = "所属回调")
    private Long notifyInfoId;

    /**
     * 请求时间
     */
    @Schema(description = "请求时间")
    private LocalDateTime requestTime;

    /**
     * 响应内容
     */
    @Schema(description = "响应内容")
    private String responseData;

    /**
     * 响应时间
     */
    @Schema(description = "响应时间")
    private LocalDateTime responseTime;

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
