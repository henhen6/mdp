package top.mddata.open.admin.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.open.admin.entity.base.OauthScopeBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * oauth2权限 VO类（通常用作Controller出参）。
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
@Table(OauthScopeBase.TABLE_NAME)
public class OauthScopeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private Long id;

    /**
     * 编码
     */
    @Schema(description = "编码")
    private String code;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 图标
     */
    @Schema(description = "图标")
    private String icon;

    /**
     * 介绍
     */
    @Schema(description = "介绍")
    private String intro;

    /**
     * 申请提示语
     */
    @Schema(description = "申请提示语")
    private String applyPrompt;

    /**
     * 确认提示语
     */
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
    @Schema(description = "权限等级")
    private Integer level;

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

    /**
     * 最后修改人
     */
    @Schema(description = "最后修改人")
    private Long updatedBy;

    /**
     * 最后修改时间
     */
    @Schema(description = "最后修改时间")
    private LocalDateTime updatedAt;

}
