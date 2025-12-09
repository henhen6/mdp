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
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 应用申请 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-27 03:31:55
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "应用申请")
@FieldNameConstants
public class AppApplyDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @NotNull(message = "请填写id", groups = BaseEntity.Update.class)
    @Schema(description = "id")
    private Long id;

    /**
     * 名称
     */
    @NotEmpty(message = "请填写名称")
    @Size(max = 255, message = "名称长度不能超过{max}")
    @Schema(description = "名称")
    private String name;

    /**
     * 应用简介
     */
    @NotEmpty(message = "请填写应用简介")
    @Size(max = 512, message = "应用简介长度不能超过{max}")
    @Schema(description = "应用简介")
    private String intro;

    /**
     * 首页地址
     */
    @Size(max = 512, message = "首页地址长度不能超过{max}")
    @Schema(description = "首页地址")
    private String homeUrl;

    /**
     * 图标
     */
    @Schema(description = "图标")
    private Long logo;

    /**
     * 备注
     */
    @Size(max = 1024, message = "备注长度不能超过{max}")
    @Schema(description = "备注")
    private String remark;

    /**
     * 资质文件
     */
    @Schema(description = "资质文件")
    private List<Long> credentialFile;

}
