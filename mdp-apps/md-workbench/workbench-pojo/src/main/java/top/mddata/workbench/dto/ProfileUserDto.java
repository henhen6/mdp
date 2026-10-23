package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import static top.mddata.base.utils.ValidatorUtil.REGEX_MOBILE;

/**
 * 个人中心-用户信息
 * @author henhen6
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "个人中心-用户信息")
public class ProfileUserDto {

    /**
     * ID
     */
    @NotNull(message = "请填写ID")
    @Schema(description = "ID")
    private Long id;

    /**
     * 性别
     * [0-男 1-女]
     */
    @Size(max = 1, message = "性别长度不能超过{max}")
    @Schema(description = "性别")
    private String sex;


    /**
     * 头像文件ID
     * <p>
     * 前端传递的必须是文件表的id， 存储到数据库时，需要设置为对象id
     */
    @Schema(description = "头像文件ID")
    private Long avatarFileId;

    /**
     * 姓名
     */
    @Size(max = 255, message = "姓名长度不能超过{max}")
    @Schema(description = "姓名")
    private String name;

    /**
     * 手机号（手动填写，仅做格式与唯一性校验，不验证真实性）
     */
    @Pattern(regexp = REGEX_MOBILE, message = "请输入11位合法的手机号")
    @Schema(description = "手机号")
    private String phone;

}
