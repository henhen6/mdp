package top.mddata.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 通用 状态 修改对象
 * @author henhen6
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "状态修改参数")
public class StatusUpdateDto extends IdDto {

    @NotNull(message = "状态不能为空")
    private Integer status;

}
