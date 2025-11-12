package top.mddata.console.system.query;

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
import java.time.LocalDateTime;

/**
 * 系统配置 Query类（查询方法入参）。
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
@Schema(description = "系统配置")
public class ConfigQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 参数ID
     */
    @Schema(description = "参数ID")
    private Long id;

    /**
     * 参数分组
     */
    @Schema(description = "参数分组")
    private String configGroup;

    /**
     * 参数标识
     */
    @Schema(description = "参数标识")
    private String uniqKey;

    /**
     * 参数名称
     */
    @Schema(description = "参数名称")
    private String name;

    /**
     * 参数值
     */
    @Schema(description = "参数值")
    private String value;

    /**
     * 数据类型
     * [1-字符串 2-整型 3-布尔]
     */
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
    @Schema(description = "备注")
    private String remark;

    /**
     * 当前机构id
     */
    @Schema(description = "当前机构id")
    private Long orgId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 创建用户
     */
    @Schema(description = "创建用户")
    private Long createdBy;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 更新用户
     */
    @Schema(description = "更新用户")
    private Long updatedBy;

    /**
     * 删除标识
     */
    @Schema(description = "删除标识")
    private Long deletedAt;

    /**
     * 删除用户
     */
    @Schema(description = "删除用户")
    private Long deletedBy;

}
