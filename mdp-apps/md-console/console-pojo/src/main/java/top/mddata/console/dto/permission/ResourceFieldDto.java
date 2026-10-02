package top.mddata.console.dto.permission;

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
 * 字段权限 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字段权限")
public class ResourceFieldDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属菜单
     */
    @NotNull(message = "请填写所属菜单")
    @Schema(description = "所属菜单")
    private Long menuId;

    /**
     * 实体类字段
     */
    @NotEmpty(message = "请填写实体类字段")
    @Size(max = 255, message = "实体类字段长度不能超过{max}")
    @Schema(description = "实体类字段")
    private String property;

    /**
     * 名称
     */
    @NotEmpty(message = "请填写名称")
    @Size(max = 255, message = "名称长度不能超过{max}")
    @Schema(description = "名称")
    private String name;

    /**
     * 处理动作
     * [10-隐藏 20-脱敏]
     */
    @NotNull(message = "请填写处理动作")
    @Schema(description = "处理动作")
    private Integer ruleType;

    /**
     * 脱敏规则(rule_type=20时必填)，取 MaskManager 注册名
     */
    @Size(max = 64, message = "脱敏规则长度不能超过{max}")
    @Schema(description = "脱敏规则")
    private String maskRule;

    /**
     * 状态
     * [0-禁用 1-启用]
     */
    @NotNull(message = "请填写状态")
    @Schema(description = "状态")
    private Boolean state;

}
