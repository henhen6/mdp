package top.mddata.open.admin.dto;

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
 * oauth2权限 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "oauth2权限")
public class OauthScopeDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;

    /**
     * 编码
     */
    @NotEmpty(message = "请填写编码")
    @Size(max = 255, message = "编码长度不能超过{max}")
    @Schema(description = "编码")
    private String code;

    /**
     * 名称
     */
    @NotEmpty(message = "请填写名称")
    @Size(max = 255, message = "名称长度不能超过{max}")
    @Schema(description = "名称")
    private String name;

    /**
     * 图标
     */
    @Size(max = 255, message = "图标长度不能超过{max}")
    @Schema(description = "图标")
    private String icon;

    /**
     * 介绍
     */
    @Size(max = 255, message = "介绍长度不能超过{max}")
    @Schema(description = "介绍")
    private String intro;

    /**
     * 申请提示语
     */
    @Size(max = 255, message = "申请提示语长度不能超过{max}")
    @Schema(description = "申请提示语")
    private String applyPrompt;

    /**
     * 确认提示语
     */
    @Size(max = 255, message = "确认提示语长度不能超过{max}")
    @Schema(description = "确认提示语")
    private String confirmPrompt;

    /**
     * 权重
     */
    @Schema(description = "权重")
    private Long weight;

    /**
     * 权限等级
     * [1-公开 2-特殊]
     */
    @NotNull(message = "请填写权限等级")
    @Schema(description = "权限等级")
    private Integer level;

}
