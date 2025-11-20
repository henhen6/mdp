package top.mddata.open.client.dto;

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
 * 应用 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-10-27 22:43:15
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "应用基础信息修改")
public class AppInfoUpdateDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;


    /**
     * 应用名称
     */
    @NotEmpty(message = "请填写应用名称")
    @Size(max = 255, message = "应用名称长度不能超过{max}")
    @Schema(description = "应用名称")
    private String name;


    /**
     * 状态
     */
    @NotNull(message = "请填写状态")
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 应用简介
     */
    @Size(max = 255, message = "应用简介长度不能超过{max}")
    @Schema(description = "应用简介")
    private String intro;

    /**
     * 应用图标
     */
    @Schema(description = "应用图标")
    private Long logo;

}
