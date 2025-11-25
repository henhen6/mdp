package top.mddata.console.organization.vo;

import cn.hutool.core.map.MapUtil;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.annotation.echo.Echo;
import top.mddata.base.base.entity.TreeEntity;
import top.mddata.base.interfaces.echo.EchoVO;
import top.mddata.common.constant.EchoApi;
import top.mddata.common.constant.EchoDictType;
import top.mddata.common.entity.base.OrgBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 组织 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-11-12 15:49:10
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "组织")
@Table(OrgBase.TABLE_NAME)
public class OrgVo extends TreeEntity<Long, OrgVo> implements Serializable, EchoVO {

    @Serial
    private static final long serialVersionUID = 1L;
    @Builder.Default
    private final Map<String, Object> echoMap = MapUtil.newHashMap();


    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private Long id;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 类型
     * [10-单位 20-部门]
     */
    @Schema(description = "类型")
    @Echo(api = EchoApi.DICT_CLASS, dictType = EchoDictType.Console.ORG_TYPE)
    private String orgType;

    /**
     * 简称
     */
    @Schema(description = "简称")
    private String shortName;

    /**
     * 父组织
     */
    @Schema(description = "父组织")
    private Long parentId;

    /**
     * 树路径
     */
    @Schema(description = "树路径")
    private String treePath;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer weight;

    /**
     * 状态
     * [0-禁用 1-启用]
     */
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remarks;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;

    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private Long updatedBy;

    /**
     * 删除人
     */
    @Schema(description = "删除人")
    private Long deletedBy;

    /**
     * 删除标识
     */
    @Schema(description = "删除标识")
    private Long deletedAt;

}
