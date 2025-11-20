package top.mddata.open.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
 * 文档内容 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档内容")
public class DocContentDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @NotNull(message = "请填写id", groups = BaseEntity.Update.class)
    @Schema(description = "id")
    private Long id;

    /**
     * 文档ID
     * doc_info.id
     */
    @NotNull(message = "请填写文档ID")
    @Schema(description = "文档ID")
    private Long docInfoId;

    /**
     * 文档内容
     */
    @Size(max = 536870911, message = "文档内容长度不能超过{max}")
    @Schema(description = "文档内容")
    private String content;

}
