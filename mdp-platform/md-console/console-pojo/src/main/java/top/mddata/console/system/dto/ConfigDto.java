package top.mddata.console.system.dto;

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
 * 系统配置 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系统配置")
public class ConfigDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 参数ID
     */
    @NotNull(message = "请填写参数ID", groups = BaseEntity.Update.class)
    @Schema(description = "参数ID")
    private Long id;

    /**
     * 参数分组
     */
    @Size(max = 100, message = "参数分组长度不能超过{max}")
    @Schema(description = "参数分组")
    private String configGroup;

    /**
     * 参数标识
     */
    @NotEmpty(message = "请填写参数标识")
    @Size(max = 255, message = "参数标识长度不能超过{max}")
    @Schema(description = "参数标识")
    private String uniqKey;

    /**
     * 参数名称
     */
    @NotEmpty(message = "请填写参数名称")
    @Size(max = 255, message = "参数名称长度不能超过{max}")
    @Schema(description = "参数名称")
    private String name;

    /**
     * 参数值
     */
    @NotEmpty(message = "请填写参数值")
    @Size(max = 5120, message = "参数值长度不能超过{max}")
    @Schema(description = "参数值")
    private String value;

    /**
     * 数据类型
     * [1-字符串 2-整型 3-布尔]
     */
    @NotEmpty(message = "请填写数据类型")
    @Size(max = 1, message = "数据类型长度不能超过{max}")
    @Schema(description = "数据类型")
    private String dataType;

    /**
     * 状态
     * [1-启用 0-禁用]
     */
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过{max}")
    @Schema(description = "备注")
    private String remark;

    /**
     * 当前机构id
     */
    @Schema(description = "当前机构id")
    private Long orgId;

}
