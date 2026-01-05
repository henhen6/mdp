package top.mddata.gateway.sop.pojo.dto;

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

/**
 * 开放接口 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "开放接口")
public class ApiDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @NotNull(message = "请填写id", groups = BaseEntity.Update.class)
    @Schema(description = "id")
    private Long id;

    /**
     * 应用名称
     */
    @NotEmpty(message = "请填写应用名称")
    @Size(max = 64, message = "应用名称长度不能超过{max}")
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 接口名称
     */
    @NotEmpty(message = "请填写接口名称")
    @Size(max = 128, message = "接口名称长度不能超过{max}")
    @Schema(description = "接口名称")
    private String apiName;

    /**
     * 版本号
     */
    @NotEmpty(message = "请填写版本号")
    @Size(max = 16, message = "版本号长度不能超过{max}")
    @Schema(description = "版本号")
    private String apiVersion;

    /**
     * 接口描述
     */
    @Size(max = 64, message = "接口描述长度不能超过{max}")
    @Schema(description = "接口描述")
    private String description;

    /**
     * 备注
     */
    @Size(max = 16383, message = "备注长度不能超过{max}")
    @Schema(description = "备注")
    private String remark;

    /**
     * 接口类名
     */
    @NotEmpty(message = "请填写接口类名")
    @Size(max = 128, message = "接口类名长度不能超过{max}")
    @Schema(description = "接口类名")
    private String interfaceClassName;

    /**
     * 方法名称
     */
    @NotEmpty(message = "请填写方法名称")
    @Size(max = 128, message = "方法名称长度不能超过{max}")
    @Schema(description = "方法名称")
    private String methodName;

    /**
     * 参数信息
     */
    @Size(max = 16383, message = "参数信息长度不能超过{max}")
    @Schema(description = "参数信息")
    private String paramInfo;

    /**
     * 需要授权
     * [0-否 1-是]
     */
    @NotNull(message = "请填写需要授权")
    @Schema(description = "需要授权")
    private Integer permission;

    /**
     * 需要token
     * [0-否 1-是]
     */
    @NotNull(message = "请填写需要token")
    @Schema(description = "需要token")
    private Integer needToken;

    /**
     * 公共响应参数
     * [0-否 1-是]
     */
    @NotNull(message = "请填写公共响应参数")
    @Schema(description = "公共响应参数")
    private Integer commonResponse;

    /**
     * 注册来源
     * [1-系统注册 2-手动注册]
     */
    @NotNull(message = "请填写注册来源")
    @Schema(description = "注册来源")
    private Integer regSource;

    /**
     * 接口模式
     * [1-open接口 2-Restful模式]
     */
    @NotNull(message = "请填写接口模式")
    @Schema(description = "接口模式")
    private Integer apiMode;

    /**
     * 状态
     * [1-启用 0-禁用]
     */
    @NotNull(message = "请填写状态")
    @Schema(description = "状态")
    private Integer state;

    public String buildApiNameVersion() {
        return apiName + apiVersion;
    }
}
