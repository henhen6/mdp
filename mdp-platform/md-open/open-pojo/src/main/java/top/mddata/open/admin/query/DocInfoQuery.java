package top.mddata.open.admin.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档信息 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文档信息")
public class DocInfoQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 所属分组
     */
    @Schema(description = "所属分组")
    private Long docGroupId;

    /**
     * 文档标题
     */
    @Schema(description = "文档标题")
    private String docTitle;

    /**
     * 文档编码
     */
    @Schema(description = "文档编码")
    private String docCode;

    /**
     * 文档类型
     * [1-dubbo 2-富文本 3-Markdown]
     */
    @Schema(description = "文档类型")
    private Integer docType;

    /**
     * 来源类型
     * [1-torna 2-自建]
     */
    @Schema(description = "来源类型")
    private Integer sourceType;

    /**
     * 文档版本号
     */
    @Schema(description = "文档版本号")
    private String docVersion;

    /**
     * 文档名称
     */
    @Schema(description = "文档名称")
    private String docName;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    /**
     * 是否分类
     */
    @Schema(description = "是否分类")
    private Integer folder;

    /**
     * 发布状态
     * [0-未发布 1-已发布]
     */
    @Schema(description = "发布状态")
    private Integer publish;

    /**
     * 父文档
     */
    @Schema(description = "父文档")
    private Long parentId;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer weight;

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
