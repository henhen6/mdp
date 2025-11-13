package top.mddata.open.manage.vo;

import com.mybatisflex.annotation.Id;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * unionid表 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-09-10 23:54:49
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "unionid表")
public class OauthUnionidVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private Long id;

    /**
     * 所属主体
     */
    @Schema(description = "所属主体")
    private Long subjectId;

    /**
     * 所属用户
     */
    @Schema(description = "所属用户")
    private Long userId;

    /**
     * Unionid
     */
    @Schema(description = "Unionid")
    private String unionid;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

}
