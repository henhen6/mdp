package top.mddata.api.oepn.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;

/**
 * 组织 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 15:49:10
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "组织查询")
public class OrgQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 类型
     * [10-单位 20-部门]
     */
    @Schema(description = "类型")
    private String orgType;

    /**
     * 简称
     */
    @Schema(description = "简称")
    private String shortName;

    /**
     * 父组织
     */
    @Schema(description = "父组织")
    private Long parentId;

    /**
     * 树路径
     */
    @Schema(description = "树路径")
    private String treePath;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer weight;

    /**
     * 状态
     * [0-禁用 1-启用]
     */
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remarks;

}
