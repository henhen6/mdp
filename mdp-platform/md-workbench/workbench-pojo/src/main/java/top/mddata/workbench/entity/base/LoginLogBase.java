package top.mddata.workbench.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录日志实体类。
 *
 * @author henhen6
 * @since 2025-11-12 23:46:53
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LoginLogBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdw_login_log";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 登录用户
     */
    private Long userId;

    /**
     * 登录IP
     */
    private String requestIp;

    /**
     * 姓名
     */
    private String name;

    /**
     * 登录人账号
     */
    private String username;

    /**
     * 登录状态
     */
    private String status;

    /**
     * 登录描述
     */
    private String description;

    /**
     * 登录时间
     */
    private String loginDate;

    /**
     * 浏览器请求头
     */
    private String ua;

    /**
     * 浏览器名称
     */
    private String browser;

    /**
     * 浏览器版本
     */
    private String browserVersion;

    /**
     * 操作系统
     */
    private String operatingSystem;

    /**
     * 登录地点
     */
    private String location;

}
