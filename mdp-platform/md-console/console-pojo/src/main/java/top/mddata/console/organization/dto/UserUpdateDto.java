package top.mddata.console.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.annotation.constraints.NotEmptyPattern;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import static top.mddata.base.utils.ValidatorUtil.REGEX_EMAIL;
import static top.mddata.base.utils.ValidatorUtil.REGEX_MOBILE;

/**
 * 用户修改 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 15:48:54
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户修改")
public class UserUpdateDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 用户名
     */
    @NotEmpty(message = "请填写用户名")
    @Size(max = 64, message = "用户名长度不能超过{max}")
    @Schema(description = "用户名")
    private String username;

    /**
     * 性别
     * [0-男 1-女]
     */
    @Size(max = 1, message = "性别长度不能超过{max}")
    @Schema(description = "性别")
    private String sex;


    /**
     * 电话号码
     */
    @Size(max = 20, message = "电话号码长度不能超过{max}")
    @Schema(description = "电话号码")
    @NotEmptyPattern(regexp = REGEX_MOBILE, message = "请输入11位的手机号")
    private String phone;

    /**
     * 头像
     *
     * 前端传递的必须是文件表的id， 存储到数据库时，需要设置为对象id
     */
    @Schema(description = "头像")
    private Long avatar;

    /**
     * 姓名
     */
    @Size(max = 255, message = "姓名长度不能超过{max}")
    @Schema(description = "姓名")
    @NotEmpty(message = "请填写姓名")
    private String name;

    /**
     * 邮箱地址
     */
    @Size(max = 128, message = "邮箱地址长度不能超过{max}")
    @Schema(description = "邮箱地址")
    @NotEmptyPattern(regexp = REGEX_EMAIL, message = "邮箱格式不合法")
    private String email;

    /**
     * 状态
     * [0-禁用 1-正常]
     */
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 所属岗位
     */
    @Schema(description = "所属岗位")
    private Long positionId;

    /**
     * 机构ID
     */
    @Schema(description = "所属部门")
    private List<Long> orgIdList;

}
