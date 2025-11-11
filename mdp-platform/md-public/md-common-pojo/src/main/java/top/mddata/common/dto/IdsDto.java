package top.mddata.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 通用 id 表单对象
 * @author henhen6
 */
@Data
@Schema(description = "id集合")
public class IdsDto {
    @NotEmpty(message = "请填写主键")
    @Schema(description = "主键")
    private List<Long> ids;
}
