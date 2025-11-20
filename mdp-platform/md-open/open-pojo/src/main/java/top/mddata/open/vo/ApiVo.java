package top.mddata.open.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.open.entity.base.ApiBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 开放接口 VO类（通常用作Controller出参）。
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
@Table(ApiBase.TABLE_NAME)
public class ApiVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * id
     */
    @Id
    @Schema(description = "id")
    private Long id;

    /**
     * 应用名称
     */
    @Schema(description = "应用名称")
    private String applicationName;

    /**
     * 接口名称
     */
    @Schema(description = "接口名称")
    private String apiName;

    /**
     * 版本号
     */
    @Schema(description = "版本号")
    private String apiVersion;

    /**
     * 接口描述
     */
    @Schema(description = "接口描述")
    private String description;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 接口类名
     */
    @Schema(description = "接口类名")
    private String interfaceClassName;

    /**
     * 方法名称
     */
    @Schema(description = "方法名称")
    private String methodName;

    /**
     * 参数信息
     */
    @Schema(description = "参数信息")
    private String paramInfo;

    /**
     * 需要授权
     * [0-否 1-是]
     */
    @Schema(description = "需要授权")
    private Integer permission;

    /**
     * 需要token
     * [0-否 1-是]
     */
    @Schema(description = "需要token")
    private Integer needToken;

    /**
     * 公共响应参数
     * [0-否 1-是]
     */
    @Schema(description = "公共响应参数")
    private Integer commonResponse;

    /**
     * 注册来源
     * [1-系统注册 2-手动注册]
     */
    @Schema(description = "注册来源")
    private Integer regSource;

    /**
     * 接口模式
     * [1-open接口 2-Restful模式]
     */
    @Schema(description = "接口模式")
    private Integer apiMode;

    /**
     * 状态
     * [1-启用 0-禁用]
     */
    @Schema(description = "状态")
    private Integer state;

    /**
     * 添加时间
     */
    @Schema(description = "添加时间")
    private LocalDateTime createdAt;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;

    /**
     * 创建人id
     */
    @Schema(description = "创建人id")
    private Long createdBy;

    /**
     * 修改人id
     */
    @Schema(description = "修改人id")
    private Long updatedBy;

}
