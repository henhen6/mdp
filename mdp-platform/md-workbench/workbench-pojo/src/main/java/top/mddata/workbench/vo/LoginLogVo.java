package top.mddata.workbench.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.workbench.entity.base.LoginLogBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-12-14 14:15:09
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "登录日志Vo")
@Table(LoginLogBase.TABLE_NAME)
public class LoginLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;



    /**
     * 主键
     */
    @Id
    @Schema(description = "主键")
    private Long id;

    /**
     * 登录用户
     */
    @Schema(description = "登录用户")
    private Long userId;

    /**
     * 姓名
     */
    @Schema(description = "姓名")
    private String name;

    /**
     * 登录人账号
     */
    @Schema(description = "登录人账号")
    private String username;

    /**
     * 事件类型
     * [01-登录 02-退出 03-注销 04-切换 05-扮演]
     */
    @Schema(description = "事件类型")
    private String eventType;

    /**
     * 登录状态
     * [01-成功 02-失败]
     */
    @Schema(description = "登录状态")
    private String status;

    /**
     * 登录失败原因
     */
    @Schema(description = "登录失败原因")
    private String statusReason;

    /**
     * 认证方式
     * [01-用户名密码验证码登录 02-用户名密码登录 03-手机短信验证码 04-邮箱验证码登录]
     */
    @Schema(description = "认证方式")
    private String authType;

    /**
     * 登录渠道
     * [01-系统登录页 02-移动端]
     */
    @Schema(description = "登录渠道")
    private String loginChannel;

    /**
     * 登录时间
     */
    @Schema(description = "登录时间")
    private String loginDate;

    /**
     * 登录IP
     */
    @Schema(description = "登录IP")
    private String loginIp;

    /**
     * IP 归属地
     */
    @Schema(description = "IP 归属地")
    private String ipLocation;

    /**
     * 浏览器请求头
     */
    @Schema(description = "浏览器请求头")
    private String ua;

    /**
     * 浏览器名称
     */
    @Schema(description = "浏览器名称")
    private String browserName;

    /**
     * 浏览器版本
     */
    @Schema(description = "浏览器版本")
    private String browserVersion;

    /**
     * 操作系统
     */
    @Schema(description = "操作系统")
    private String os;

    /**
     * 设备信息
     */
    @Schema(description = "设备信息")
    private String deviceInfo;

    /**
     * 应用Key
     */
    @Schema(description = "应用Key")
    private String appKey;

    /**
     * 应用名称
     */
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 应用地址
     */
    @Schema(description = "应用地址")
    private String appRedirect;

    /**
     * 登录令牌
     */
    @Schema(description = "登录令牌")
    private String tokenInfo;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

}
