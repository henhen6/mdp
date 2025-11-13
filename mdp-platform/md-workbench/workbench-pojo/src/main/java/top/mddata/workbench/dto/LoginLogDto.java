package top.mddata.workbench.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;

/**
 * 登录日志 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 23:46:53
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录日志")
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
     * 登录IP
     */
    @Size(max = 50, message = "登录IP长度不能超过{max}")
    @Schema(description = "登录IP")
    private String requestIp;

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
    private String browser;

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
    private String operatingSystem;

    /**
     * 登录地点
     */
    @Size(max = 50, message = "登录地点长度不能超过{max}")
    @Schema(description = "登录地点")
    private String location;

}
