package top.mddata.console.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import top.mddata.base.annotation.constraints.NotEmptyPattern;

import java.io.Serial;
import java.io.Serializable;

import static top.mddata.base.utils.ValidatorUtil.REGEX_PASSWORD;


/**
 * <p>
 * 用户密码修改
 * </p>
 *
 * @author henhen6
 * @since 2025年06月23日21:57:19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(title = "UserResetPasswordDto", description = "管理员重置用户密码")
public class UserResetPasswordDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "是否使用默认密码")
    @NotNull(message = "请选择是否使用默认密码")
    private Boolean defPassword;

    /**
     * 密码
     */
    @Schema(description = "密码")
    @Size(min = 6, max = 20, message = "密码长度不能小于{min}且超过{max}个字符")
    @NotEmptyPattern(regexp = REGEX_PASSWORD, message = "至少包含字母、数字、特殊字符")
    private String password;

}
