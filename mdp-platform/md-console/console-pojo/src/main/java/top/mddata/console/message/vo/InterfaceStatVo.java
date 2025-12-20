package top.mddata.console.message.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.console.message.entity.base.InterfaceStatBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 接口统计 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "接口统计Vo")
@Table(InterfaceStatBase.TABLE_NAME)
public class InterfaceStatVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Id
    @Schema(description = "")
    private Long id;

    /**
     * 接口ID;
     * #extend_interface
     */
    @Schema(description = "接口ID;")
    private Long interfaceConfigId;

    /**
     * 接口名称
     */
    @Schema(description = "接口名称")
    private String name;

    /**
     * 成功次数
     */
    @Schema(description = "成功次数")
    private Integer successCount;

    /**
     * 失败次数
     */
    @Schema(description = "失败次数")
    private Integer failCount;

    /**
     * 最后执行时间
     */
    @Schema(description = "最后执行时间")
    private LocalDateTime lastExecAt;

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
