package top.mddata.open.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
import java.util.List;

/**
 * 应用 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-10-27 22:43:15
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "应用开发信息")
public class AppDevInfoDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 首页地址
     */
    @Size(max = 512, message = "首页地址长度不能超过{max}")
    @Schema(description = "首页地址")
    private String homeUrl;

    /**
     * 是否显示
     * 是否在我的应用显示该应用，若设置为false，就算有权限访问，该应用也不会出现在我的应用列表
     */
    @NotNull(message = "请填写是否显示")
    @Schema(description = "是否显示")
    private Boolean show;

    /**
     * 是否公开
     * [true=公开应用 false-私有应用]
     */
    @NotNull(message = "请填写是否公开")
    @Schema(description = "是否公开")
    private Boolean isPublic;

    /**
     * 允许的IP
     */
    @Size(max = 512, message = "允许的IP长度不能超过{max}")
    @Schema(description = "允许的IP")
    private String allowIp;

    /**
     * 是否接收消息推送
     */
    @NotNull(message = "请填写是否接收消息推送")
    @Schema(description = "是否接收消息推送")
    private Boolean ssoPush;

    /**
     * 是否接收单点注销消息推送
     */
    @NotNull(message = "请填写是否接收单点注销消息推送")
    @Schema(description = "是否接收单点注销消息推送")
    private Boolean ssoSlo;


    /**
     * 接收消息推送的地址
     */
    @Size(max = 512, message = "接收消息推送的地址长度不能超过{max}")
    @Schema(description = "接收消息推送的地址")
    private String ssoPushUrl;

    /**
     * 允许授权的URL
     * 多个用逗号分割
     */
    @Size(max = 5120, message = "允许授权的URL长度不能超过{max}")
    @Schema(description = "允许授权的URL")
    private String ssoAllowUrl;

    /**
     * 允许的重定向uri
     */
    @Size(max = 5120, message = "允许的重定向uri长度不能超过{max}")
    @Schema(description = "允许的重定向uri")
    private String oauth2AllowRedirectUris;

    /**
     * 允许的授权类型
     */
    @Schema(description = "允许的授权类型")
    private List<String> oauth2AllowGrantTypes;

    /**
     * Refresh-Token刷新策略
     * [0-否 1-是 -1-全局配置]
     */
    @Schema(description = "Refresh-Token刷新策略")
    private Integer oauth2NewRefresh;

    /**
     * access-token有效期
     * 单位：秒
     * [-1-全局]
     */
    @Schema(description = "access-token有效期")
    private Long oauth2AccessTokenTimeout;

    /**
     * refresh-token有效期
     * 单位：秒
     * [-1-全局]
     */
    @Schema(description = "refresh-token有效期")
    private Long oauth2RefreshTokenTimeout;

    /**
     * client-token有效期
     * 单位：秒
     * [-1-全局]
     */
    @Schema(description = "client-token有效期")
    private Long oauth2ClientTokenTimeout;

    /**
     * lower-client-token有效期
     * 单位：秒
     * [-1-全局]
     */
    @Schema(description = "lower-client-token有效期")
    private Long oauth2LowerClientTokenTimeout;

    /**
     * 是否自动授权
     * [1-是 0-否]
     */
    @NotNull(message = "请填写是否自动授权")
    @Schema(description = "是否自动授权")
    private Boolean oauth2IsConfirm;


}
