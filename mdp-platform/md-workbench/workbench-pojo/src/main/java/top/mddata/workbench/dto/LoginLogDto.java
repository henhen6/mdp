package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录日志 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-12-14 00:53:23
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "登录日志Dto")
public class LoginLogDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "请填写主键", groups = BaseEntity.Update.class)
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
    @Size(max = 255, message = "姓名长度不能超过{max}")
    @Schema(description = "姓名")
    private String name;

    /**
     * 登录人账号
     */
    @Size(max = 255, message = "登录人账号长度不能超过{max}")
    @Schema(description = "登录人账号")
    private String username;

    /**
     * 登录状态
     */
    @Size(max = 2, message = "登录状态长度不能超过{max}")
    @Schema(description = "登录状态")
    private String status;

    /**
     * 登录描述
     */
    @Size(max = 255, message = "登录描述长度不能超过{max}")
    @Schema(description = "登录描述")
    private String description;

    /**
     * 登录时间
     */
    @Size(max = 10, message = "登录时间长度不能超过{max}")
    @Schema(description = "登录时间")
    private String loginDate;

    /**
     * 登录IP
     */
    @Size(max = 50, message = "登录IP长度不能超过{max}")
    @Schema(description = "登录IP")
    private String loginIp;

    /**
     * IP 归属地
     */
    @Size(max = 128, message = "IP 归属地长度不能超过{max}")
    @Schema(description = "IP 归属地")
    private String ipLocation;

    /**
     * 浏览器请求头
     */
    @Size(max = 512, message = "浏览器请求头长度不能超过{max}")
    @Schema(description = "浏览器请求头")
    private String ua;

    /**
     * 浏览器名称
     */
    @Size(max = 255, message = "浏览器名称长度不能超过{max}")
    @Schema(description = "浏览器名称")
    private String browserName;

    /**
     * 浏览器版本
     */
    @Size(max = 255, message = "浏览器版本长度不能超过{max}")
    @Schema(description = "浏览器版本")
    private String browserVersion;

    /**
     * 操作系统
     */
    @Size(max = 255, message = "操作系统长度不能超过{max}")
    @Schema(description = "操作系统")
    private String os;

    /**
     * 设备信息
     */
    @Size(max = 256, message = "设备信息长度不能超过{max}")
    @Schema(description = "设备信息")
    private String deviceInfo;

    /**
     * 登录终端类型
     * PC / 移动端 / 平板 / 接口调用等
     */
    @Size(max = 100, message = "登录终端类型长度不能超过{max}")
    @Schema(description = "登录终端类型")
    private String loginType;

    /**
     * 认证方式
     * [01-密码 02-手机短信验证码 03-邮箱验证码登录]
     */
    @Size(max = 2, message = "认证方式长度不能超过{max}")
    @Schema(description = "认证方式")
    private String authType;

    /**
     * 应用Key
     */
    @Size(max = 100, message = "应用Key长度不能超过{max}")
    @Schema(description = "应用Key")
    private String appKey;

    /**
     * 应用名称
     */
    @Size(max = 255, message = "应用名称长度不能超过{max}")
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 登录渠道
     * [01-系统登录页 02-移动端]
     */
    @Size(max = 2, message = "登录渠道长度不能超过{max}")
    @Schema(description = "登录渠道")
    private String loginChannel;

    /**
     * 登录失败原因
     */
    @Size(max = 255, message = "登录失败原因长度不能超过{max}")
    @Schema(description = "登录失败原因")
    private String failReason;

    /**
     * 登录令牌
     */
    @Size(max = 16383, message = "登录令牌长度不能超过{max}")
    @Schema(description = "登录令牌")
    private String tokenInfo;

}
