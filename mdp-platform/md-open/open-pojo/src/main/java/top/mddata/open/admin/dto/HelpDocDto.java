package top.mddata.open.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 帮助文档 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "帮助文档Dto")
public class HelpDocDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @NotNull(message = "请填写id", groups = BaseEntity.Update.class)
    @Schema(description = "id")
    private Long id;

    /**
     * 文档名称
     */
    @NotEmpty(message = "请填写文档名称")
    @Size(max = 64, message = "文档名称长度不能超过{max}")
    @Schema(description = "文档名称")
    private String title;

    /**
     * 排序
     */
    @NotNull(message = "请填写排序")
    @Schema(description = "排序")
    private Integer weight;

    /**
     * 状态
     * 1：启用，0：禁用
     */
    @NotNull(message = "请填写状态")
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 内容
     */
    @Size(max = 536870911, message = "内容长度不能超过{max}")
    @Schema(description = "内容")
    private String content;

    /**
     * 内容类型
     * 1-Markdown,2-富文本
     */
    @NotNull(message = "请填写内容类型")
    @Schema(description = "内容类型")
    private Integer contentType;

    /**
     * 父级id
     */
    @NotNull(message = "请填写父级id")
    @Schema(description = "父级id")
    private Long parentId;

}
