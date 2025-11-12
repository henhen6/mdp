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
 * 字典 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字典")
public class DictDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 字典项结构
     * [01-列表 02-树结构]
     */
    @NotEmpty(message = "请填写字典项结构")
    @Size(max = 2, message = "字典项结构长度不能超过{max}")
    @Schema(description = "字典项结构")
    private String itemType;

    /**
     * 字典分组
     */
    @Size(max = 100, message = "字典分组长度不能超过{max}")
    @Schema(description = "字典分组")
    private String dictGroup;

    /**
     * 字典类型
     * [10-系统字典 20-枚举字典 30-业务字典]
     */
    @NotEmpty(message = "请填写字典类型")
    @Size(max = 2, message = "字典类型长度不能超过{max}")
    @Schema(description = "字典类型")
    private String dictType;

    /**
     * 数据类型
     * [1-字符串 2-整型 3-布尔]
     */
    @NotEmpty(message = "请填写数据类型")
    @Size(max = 1, message = "数据类型长度不能超过{max}")
    @Schema(description = "数据类型")
    private String dataType;

    /**
     * 标识
     */
    @NotEmpty(message = "请填写标识")
    @Size(max = 255, message = "标识长度不能超过{max}")
    @Schema(description = "标识")
    private String uniqKey;

    /**
     * 名称
     */
    @NotEmpty(message = "请填写名称")
    @Size(max = 255, message = "名称长度不能超过{max}")
    @Schema(description = "名称")
    private String name;

    /**
     * 状态
     */
    @NotNull(message = "请填写状态")
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过{max}")
    @Schema(description = "备注")
    private String remark;

}
