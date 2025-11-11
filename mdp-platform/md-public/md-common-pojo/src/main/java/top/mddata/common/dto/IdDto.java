package top.mddata.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 通用 id 表单对象
 * @author henhen6
 */
@Data
@Schema(description = "id")
public class IdDto {
    @NotNull(message = "请填写主键")
    @Schema(description = "主键")
    private Long id;
}
