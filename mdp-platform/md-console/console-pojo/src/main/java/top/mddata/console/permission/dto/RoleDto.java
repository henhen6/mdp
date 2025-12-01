package top.mddata.console.permission.dto;

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
 * 角色 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-12-01 00:12:36
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色")
public class RoleDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 角色编码
     */
    @NotEmpty(message = "请填写角色编码")
    @Size(max = 255, message = "角色编码长度不能超过{max}")
    @Schema(description = "角色编码")
    private String code;

    /**
     * 角色名称
     */
    @NotEmpty(message = "请填写角色名称")
    @Size(max = 255, message = "角色名称长度不能超过{max}")
    @Schema(description = "角色名称")
    private String name;


    /**
     * 角色类型
     * [10-功能角色 20-桌面角色 30-数据角色]
     */
    @NotEmpty(message = "请填写角色类型")
    @Size(max = 2, message = "角色类型长度不能超过{max}")
    @Schema(description = "角色类型")
    private String roleType;

    /**
     * 组织性质
     * [1-默认 90-开发者 99-运维]
     */
    @NotNull(message = "请填写组织性质")
    @Schema(description = "组织性质")
    private Integer orgNature;

    /**
     * 说明
     */
    @Size(max = 255, message = "说明长度不能超过{max}")
    @Schema(description = "说明")
    private String remarks;

    /**
     * 状态
     * [0-禁用 1-启用]
     */
    @NotNull(message = "请填写状态")
    @Schema(description = "状态")
    private Boolean state;

}
