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
 * 字典项 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字典项")
public class DictItemDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属字典
     */
    @NotNull(message = "请填写所属字典")
    @Schema(description = "所属字典")
    private Long dictId;

    /**
     * 父节点
     */
    @Schema(description = "父节点")
    private Long parentId;

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
     * 数据类型
     * [1-字符串 2-整型 3-布尔]
     */
    @NotEmpty(message = "请填写数据类型")
    @Size(max = 1, message = "数据类型长度不能超过{max}")
    @Schema(description = "数据类型")
    private String dataType;

    /**
     * 字典类型
     * [10-系统字典 20-枚举字典 30-业务字典]
     */
    @NotEmpty(message = "请填写字典类型")
    @Size(max = 2, message = "字典类型长度不能超过{max}")
    @Schema(description = "字典类型")
    private String dictType;

    /**
     * 树路径
     */
    @NotEmpty(message = "请填写树路径")
    @Size(max = 1024, message = "树路径长度不能超过{max}")
    @Schema(description = "树路径")
    private String treePath;

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

    /**
     * 排序
     */
    @NotNull(message = "请填写排序")
    @Schema(description = "排序")
    private Integer weight;

    /**
     * 图标
     */
    @Size(max = 255, message = "图标长度不能超过{max}")
    @Schema(description = "图标")
    private String icon;

    /**
     * 组件样式
     */
    @Size(max = 255, message = "组件样式长度不能超过{max}")
    @Schema(description = "组件样式")
    private String cssStyle;

    /**
     * 组件类名
     */
    @Size(max = 255, message = "组件类名长度不能超过{max}")
    @Schema(description = "组件类名")
    private String cssClass;

    /**
     * 组件属性
     * 用于Tag时，用于配置color属性
     * 用于Button时，用于配置type属性
     */
    @Size(max = 255, message = "组件属性长度不能超过{max}")
    @Schema(description = "组件属性")
    private String propType;

    /**
     * 国际化配置
     */
    @Size(max = 5120, message = "国际化配置长度不能超过{max}")
    @Schema(description = "国际化配置")
    private String i18nJson;

}
