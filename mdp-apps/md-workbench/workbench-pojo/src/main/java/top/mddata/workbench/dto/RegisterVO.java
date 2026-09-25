package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 登录参数
 *
 * @author henhen6
 * @date 2020年01月05日22:18:12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Schema(title = "RegisterVO", description = "注册")
public class RegisterVO {
    @Schema(description = "用户身份")
    @NotNull(message = "请选择注册身份")
    private Integer nature;

}
