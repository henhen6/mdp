package top.mddata.workbench.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录日志实体类。
 *
 * @author henhen6
 * @since 2025-12-14 14:15:09
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
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
     * 姓名
     */
    private String name;

    /**
     * 登录人账号
     */
    private String account;

    /**
     * 事件类型
     * [01-登录 02-退出 03-注销 04-切换 05-扮演]
     */
    private String eventType;

    /**
     * 登录状态
     * [01-成功 02-失败]
     */
    private String status;

    /**
     * 登录失败原因
     */
    private String statusReason;

    /**
     * 认证方式
     * [01-用户名密码验证码登录 02-用户名密码登录 03-手机短信验证码 04-邮箱验证码登录]
     */
    private String authType;

    /**
     * 登录渠道
     * [01-系统登录页 02-移动端]
     */
    private String loginChannel;

    /**
     * 登录时间
     */
    private String loginDate;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * IP 归属地
     */
    private String ipLocation;

    /**
     * 浏览器请求头
     */
    private String ua;

    /**
     * 浏览器名称
     */
    private String browserName;

    /**
     * 浏览器版本
     */
    private String browserVersion;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 应用Key
     */
    private String appKey;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用地址
     */
    private String appRedirect;

    /**
     * 登录令牌
     */
    private String tokenInfo;

}
