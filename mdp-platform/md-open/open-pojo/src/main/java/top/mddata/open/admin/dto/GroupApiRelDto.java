package top.mddata.open.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分组拥有的对外接口 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分组拥有的对外接口")
public class GroupApiRelDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 所属分组
     * perm_group.id
     */
    @NotNull(message = "请填写所属分组")
    @Schema(description = "所属分组")
    private Long groupId;

    /**
     * 所属文档
     * api_info.id
     */
    @NotEmpty(message = "请填写所属文档")
    @Schema(description = "所属文档")
    private List<Long> apiIdList;

}
