package top.mddata.open.admin.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.open.admin.entity.base.HelpDocBase;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 帮助文档 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "帮助文档Vo")
@Table(HelpDocBase.TABLE_NAME)
public class HelpDocVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * id
     */
    @Id
    @Schema(description = "id")
    private BigInteger id;

    /**
     * 文档名称
     */
    @Schema(description = "文档名称")
    private String title;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer weight;

    /**
     * 状态
     * 1：启用，0：禁用
     */
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 内容
     */
    @Schema(description = "内容")
    private String content;

    /**
     * 内容类型
     * 1-Markdown,2-富文本
     */
    @Schema(description = "内容类型")
    private Integer contentType;

    /**
     * 父级id
     */
    @Schema(description = "父级id")
    private Long parentId;

    /**
     * 添加时间
     */
    @Schema(description = "添加时间")
    private LocalDateTime createdAt;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;

    /**
     * 创建人id
     */
    @Schema(description = "创建人id")
    private Long createdBy;

    /**
     * 修改人id
     */
    @Schema(description = "修改人id")
    private Long updatedBy;

}
