package top.mddata.console.organization.query;

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
 * 组织性质 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 15:50:00
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "组织性质")
public class OrgNatureQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Schema(description = "id")
    private Long id;

    /**
     * 组织id
     */
    @Schema(description = "组织id")
    private Long orgId;

    /**
     * 组织性质
     * [1-默认 90-开发者 99-超管]
     */
    @Schema(description = "组织性质")
    private Integer nature;

    /**
     * 创建日期
     */
    @Schema(description = "创建日期")
    private LocalDateTime createdAt;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

}
