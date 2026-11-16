package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 指纹凭证改名入参
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Data
@Accessors(chain = true)
@Schema(title = "FingerprintRenameDto", description = "指纹凭证改名入参")
public class FingerprintRenameDto {

    @Schema(description = "凭证ID")
    @NotNull(message = "凭证ID不能为空")
    private Long id;

    @Schema(description = "设备名称")
    @NotBlank(message = "设备名称不能为空")
    private String name;
}
