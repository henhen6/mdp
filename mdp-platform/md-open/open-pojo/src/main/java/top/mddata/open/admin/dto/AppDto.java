package top.mddata.open.admin.dto;

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
import java.util.List;

/**
 * 应用 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "应用")
public class AppDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;


    /**
     * 应用名称
     */
    @NotEmpty(message = "请填写应用名称")
    @Size(max = 255, message = "应用名称长度不能超过{max}")
    @Schema(description = "应用名称")
    private String name;

    /**
     * 登录方式
     * [10-单点登录 20-oauth2]
     */
    @NotEmpty(message = "请填写登录方式")
    @Size(max = 2, message = "登录方式长度不能超过{max}")
    @Schema(description = "登录方式")
    private String loginType;

    /**
     * 应用类型
     * [10-自建应用 20-第三方应用]
     */
    @NotEmpty(message = "请填写应用类型")
    @Size(max = 2, message = "应用类型长度不能超过{max}")
    @Schema(description = "应用类型")
    private String type;

    /**
     * 状态
     */
    @NotNull(message = "请填写状态")
    @Schema(description = "状态")
    private Boolean state;
    /**
     * 权重
     */
    @Schema(description = "权重")
    private Integer weight;

    /**
     * 应用简介
     */
    @Size(max = 255, message = "应用简介长度不能超过{max}")
    @Schema(description = "应用简介")
    private String intro;

    /**
     * 应用图标
     */
    @Schema(description = "应用图标")
    private Long logo;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过{max}")
    @Schema(description = "备注")
    private String remark;

    /**
     * 开始有效期
     */
    @Schema(description = "开始有效期")
    private LocalDateTime validityStart;

    /**
     * 结束有效期
     */
    @Schema(description = "结束有效期")
    private LocalDateTime validityEnd;

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
     * 是否接收单点注销推送
     */
    @NotNull(message = "请填写是否接收单点注销推送")
    @Schema(description = "是否接收单点注销推送")
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
