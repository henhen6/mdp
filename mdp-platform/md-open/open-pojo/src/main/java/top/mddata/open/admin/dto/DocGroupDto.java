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
 * 文档分组 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档分组")
public class DocGroupDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @NotNull(message = "请填写id", groups = BaseEntity.Update.class)
    @Schema(description = "id")
    private Long id;

    /**
     * 名称
     */
    @NotEmpty(message = "请填写名称")
    @Size(max = 64, message = "名称长度不能超过{max}")
    @Schema(description = "名称")
    private String name;

    /**
     * 令牌
     */
    @NotEmpty(message = "请填写令牌")
    @Size(max = 64, message = "令牌长度不能超过{max}")
    @Schema(description = "令牌")
    private String token;

    /**
     * 是否发布
     */
    @NotNull(message = "请填写是否发布")
    @Schema(description = "是否发布")
    private Integer publish;

}
