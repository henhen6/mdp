package top.mddata.console.message.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.console.message.entity.base.InterfaceConfigBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 接口 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:47
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "接口Vo")
@Table(InterfaceConfigBase.TABLE_NAME)
public class InterfaceConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Id
    @Schema(description = "")
    private Long id;

    /**
     * 接口名称
     */
    @Schema(description = "接口名称")
    private String name;

    /**
     * 执行方式
     * [1-实现类 2-脚本 3-magic-api]
     */
    @Schema(description = "执行方式")
    private Integer execMode;

    /**
     * 实现脚本
     */
    @Schema(description = "实现脚本")
    private String script;

    /**
     * 实现类
     */
    @Schema(description = "实现类")
    private String implClass;

    /**
     * 实现ID
     */
    @Schema(description = "实现ID")
    private String magicApiId;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Boolean state;

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

    /**
     * 配置参数
     * (JSON存储：AppId, SecretKey等)
     */
    @Schema(description = "配置参数")
    private String configJson;

}
