package top.mddata.console.organization.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 15:48:54
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户")
public class UserQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码")
    private String password;

    /**
     * 性别
     * [1-男 2-女]
     */
    @Schema(description = "性别")
    private String sex;

    /**
     * 盐值
     */
    @Schema(description = "盐值")
    private String salt;

    /**
     * 电话号码
     */
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
    @Schema(description = "姓名")
    private String name;

    /**
     * 邮箱地址
     */
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
    @Schema(description = "微信登录openId")
    private String wxOpenid;

    /**
     * 钉钉openId
     */
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
    @Schema(description = "用户来源")
    private String userSource;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private Long updatedBy;

    /**
     * 最后修改时间
     */
    @Schema(description = "最后修改时间")
    private LocalDateTime updatedAt;

    /**
     * 删除人
     */
    @Schema(description = "删除人")
    private Long deletedBy;

    /**
     * 删除标志
     */
    @Schema(description = "删除标志")
    private Long deletedAt;

}
