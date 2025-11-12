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
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 15:48:54
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户")
public class UserDto implements Serializable {

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
     * 密码
     */
    @Size(max = 255, message = "密码长度不能超过{max}")
    @Schema(description = "密码")
    private String password;

    /**
     * 性别
     * [1-男 2-女]
     */
    @Size(max = 1, message = "性别长度不能超过{max}")
    @Schema(description = "性别")
    private String sex;

    /**
     * 盐值
     */
    @Size(max = 255, message = "盐值长度不能超过{max}")
    @Schema(description = "盐值")
    private String salt;

    /**
     * 电话号码
     */
    @Size(max = 20, message = "电话号码长度不能超过{max}")
    @Schema(description = "电话号码")
    private String phone;

    /**
     * 头像
     */
    @Schema(description = "头像")
    private Long avatar;

    /**
     * 姓名
     */
    @Size(max = 255, message = "姓名长度不能超过{max}")
    @Schema(description = "姓名")
    private String name;

    /**
     * 邮箱地址
     */
    @Size(max = 128, message = "邮箱地址长度不能超过{max}")
    @Schema(description = "邮箱地址")
    private String email;

    /**
     * 状态
     * [0-禁用 1-正常]
     */
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 上次登录的部门
     */
    @Schema(description = "上次登录的部门")
    private Long lastDeptId;

    /**
     * 上次登录的单位
     */
    @Schema(description = "上次登录的单位")
    private Long lastCompanyId;

    /**
     * 上次登录的顶级单位
     */
    @Schema(description = "上次登录的顶级单位")
    private Long lastTopCompanyId;

    /**
     * 输错密码时间
     */
    @Schema(description = "输错密码时间")
    private LocalDateTime pwErrorLastTime;

    /**
     * 密码错误次数
     */
    @Schema(description = "密码错误次数")
    private Integer pwErrorNum;

    /**
     * 密码过期时间
     */
    @Schema(description = "密码过期时间")
    private LocalDateTime pwExpireTime;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    /**
     * 微信登录openId
     */
    @Size(max = 32, message = "微信登录openId长度不能超过{max}")
    @Schema(description = "微信登录openId")
    private String wxOpenid;

    /**
     * 钉钉openId
     */
    @Size(max = 32, message = "钉钉openId长度不能超过{max}")
    @Schema(description = "钉钉openId")
    private String ddOpenid;

    /**
     * 人员类型
     * [1-普通用户 2-管理员 99-运维管理员]
     */
    @Schema(description = "人员类型")
    private Integer userType;

    /**
     * 所属岗位
     */
    @Schema(description = "所属岗位")
    private Long positionId;

    /**
     * 用户来源
     */
    @Size(max = 255, message = "用户来源长度不能超过{max}")
    @Schema(description = "用户来源")
    private String userSource;

}
