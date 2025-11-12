package top.mddata.console.system.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典项 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "字典项")
public class DictItemQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属字典
     */
    @Schema(description = "所属字典")
    @NotNull(message = "请选择字典")
    private Long dictId;

    @Schema(description = "所属字典key")
    private String dictUniqKey;

    /**
     * 父节点
     */
    @Schema(description = "父节点")
    private Long parentId;

    /**
     * 标识
     */
    @Schema(description = "标识")
    private String uniqKey;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 树路径
     */
    @Schema(description = "树路径")
    private String treePath;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer weight;

    /**
     * 图标
     */
    @Schema(description = "图标")
    private String icon;

    /**
     * 组件样式
     */
    @Schema(description = "组件样式")
    private String cssStyle;

    /**
     * 组件类名
     */
    @Schema(description = "组件类名")
    private String cssClass;

    /**
     * 组件属性
     * 用于Tag时，用于配置color属性
     * 用于Button时，用于配置type属性
     */
    @Schema(description = "组件属性")
    private String propType;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private Long updatedBy;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 数据类型
     * [1-字符串 2-整型 3-布尔]
     */
    @Schema(description = "数据类型")
    private String dataType;
    /**
     * 字典类型
     * [10-系统字典 20-枚举字典 30-业务字典]
     */
    @Schema(description = "字典类型")
    private String dictType;

}
