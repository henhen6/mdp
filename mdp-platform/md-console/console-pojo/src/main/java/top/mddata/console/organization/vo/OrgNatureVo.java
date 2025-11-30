package top.mddata.console.organization.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.common.entity.base.OrgNatureBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 组织性质 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-11-12 15:50:00
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "组织性质")
@Table(OrgNatureBase.TABLE_NAME)
public class OrgNatureVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * id
     */
    @Id
    @Schema(description = "id")
    private Long id;

    /**
     * 组织id
     */
    @Schema(description = "组织id")
    private Long orgId;

    /**
     * 组织性质
     * [1-默认 90-开发者 99-运维]
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
