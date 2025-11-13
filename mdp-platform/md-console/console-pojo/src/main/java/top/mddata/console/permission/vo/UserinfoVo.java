package top.mddata.console.permission.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息
 *
 * @author henhen6
 * @since 2025/6/30 12:52
 */
@Data
public class UserinfoVo {

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;
    /**
     * 人员类型
     * [1-普通用户 2-管理员 99-运维管理员]
     */
    @Schema(description = "人员类型")
    private Integer userType;
    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 性别
     * [1-男 2-女]
     */
    @Schema(description = "性别")
    private String sex;

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


}
