package top.mddata.common.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类。
 *
 * @author henhen6
 * @since 2025-11-12 15:48:54
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_user";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 性别
     * [1-男 2-女]
     */
    private String sex;

    /**
     * 盐值
     */
    private String salt;

    /**
     * 电话号码
     */
    private String phone;

    /**
     * 头像
     */
    private Long avatar;

    /**
     * 姓名
     */
    private String name;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 状态
     * [0-禁用 1-正常]
     */
    private Boolean state;

    /**
     * 上次登录的部门
     */
    private Long lastDeptId;

    /**
     * 上次登录的单位
     */
    private Long lastCompanyId;

    /**
     * 上次登录的顶级单位
     */
    private Long lastTopCompanyId;

    /**
     * 输错密码时间
     */
    private LocalDateTime pwErrorLastTime;

    /**
     * 密码错误次数
     */
    private Integer pwErrorNum;

    /**
     * 密码过期时间
     */
    private LocalDateTime pwExpireTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 微信登录openId
     */
    private String wxOpenid;

    /**
     * 钉钉openId
     */
    private String ddOpenid;

    /**
     * 人员类型
     * [1-普通用户 2-管理员 99-运维管理员]
     */
    private Integer userType;

    /**
     * 所属岗位
     */
    private Long positionId;

    /**
     * 用户来源
     */
    private String userSource;

    /**
     * 删除人
     */
    private Long deletedBy;

    /**
     * 删除标志
     */
    private Long deletedAt;

}
