package top.mddata.api.oepn.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 组织 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 15:49:10
 */
@Accessors(chain = true)
@Data
@Schema(description = "组织修改")
public class OrgUpdateDto extends OrgSaveDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
    @Schema(description = "ID")
    private Long id;


}
