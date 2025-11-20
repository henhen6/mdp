package top.mddata.open.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文档信息 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档信息")
public class DocInfoDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @NotNull(message = "请填写id", groups = BaseEntity.Update.class)
    @Schema(description = "id")
    private Long id;

    /**
     * 所属分组
     */
    @NotNull(message = "请填写所属分组")
    @Schema(description = "所属分组")
    private Long docGroupId;

    /**
     * 文档标题
     */
    @NotEmpty(message = "请填写文档标题")
    @Size(max = 128, message = "文档标题长度不能超过{max}")
    @Schema(description = "文档标题")
    private String docTitle;

    /**
     * 文档编码
     */
    @Size(max = 64, message = "文档编码长度不能超过{max}")
    @Schema(description = "文档编码")
    private String docCode;

    /**
     * 文档类型
     * [1-dubbo 2-富文本 3-Markdown]
     */
    @NotNull(message = "请填写文档类型")
    @Schema(description = "文档类型")
    private Integer docType;

    /**
     * 来源类型
     * [1-torna 2-自建]
     */
    @NotNull(message = "请填写来源类型")
    @Schema(description = "来源类型")
    private Integer sourceType;

    /**
     * 文档版本号
     */
    @NotEmpty(message = "请填写文档版本号")
    @Size(max = 16, message = "文档版本号长度不能超过{max}")
    @Schema(description = "文档版本号")
    private String docVersion;

    /**
     * 文档名称
     */
    @NotEmpty(message = "请填写文档名称")
    @Size(max = 64, message = "文档名称长度不能超过{max}")
    @Schema(description = "文档名称")
    private String docName;

    /**
     * 描述
     */
    @NotEmpty(message = "请填写描述")
    @Size(max = 16383, message = "描述长度不能超过{max}")
    @Schema(description = "描述")
    private String description;

    /**
     * 是否分类
     */
    @NotNull(message = "请填写是否分类")
    @Schema(description = "是否分类")
    private Integer folder;

    /**
     * 发布状态
     * [0-未发布 1-已发布]
     */
    @NotNull(message = "请填写发布状态")
    @Schema(description = "发布状态")
    private Integer publish;

    /**
     * 父文档
     */
    @NotNull(message = "请填写父文档")
    @Schema(description = "父文档")
    private Long parentId;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer weight;

}
