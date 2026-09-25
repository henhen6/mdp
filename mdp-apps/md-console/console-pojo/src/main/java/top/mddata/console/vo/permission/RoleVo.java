package top.mddata.console.vo.permission;

import cn.hutool.core.map.MapUtil;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.annotation.echo.Echo;
import top.mddata.base.interfaces.echo.EchoVO;
import top.mddata.common.constant.EchoApi;
import top.mddata.common.constant.EchoDictType;
import top.mddata.console.entity.permission.base.RoleBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 角色 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-12-01 00:12:36
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色")
@Table(RoleBase.TABLE_NAME)
public class RoleVo implements Serializable, EchoVO {

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
     * 角色编码
     */
    @Schema(description = "角色编码")
    private String code;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String name;

    /**
     * 角色分类
     * [10-普通角色 20-管理员角色 30-权限集合]
     */
    @Schema(description = "角色分类")
    @Echo(api = EchoApi.DICT_CLASS, dictType = EchoDictType.Console.ROLE_CATEGORY)
    private String roleCategory;

    /**
     * 组织性质
     * [1-总公司 90-开发者 99-运营]
     */
    @Schema(description = "组织性质")
    @Echo(api = EchoApi.DICT_CLASS, dictType = EchoDictType.Console.ORG_NATURE)
    private Integer orgNature;

    /**
     * 数据范围
     * [10-全部 20-本公司及以下 30-本部门及以下
     * 40-本部门 50-仅本人 90-自定义实现]
     */
    @Schema(description = "数据范围")
    private String dataScope;

    /**
     * 自定义数据范围实现类（Spring Bean 名）
     * 仅 dataScope=90 时使用，实现 DataScopeCustomHandler 接口
     */
    @Schema(description = "自定义数据范围实现类（Spring Bean 名）")
    private String dataScopeImpl;

    /**
     * 是否模版
     */
    @Schema(description = "是否模版")
    private Boolean templateRole;

    /**
     * 说明
     */
    @Schema(description = "说明")
    private String remarks;

    /**
     * 状态
     * [0-禁用 1-启用]
     */
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 创建人id
     */
    @Schema(description = "创建人id")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新人id
     */
    @Schema(description = "更新人id")
    private Long updatedBy;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 删除人
     */
    @Schema(description = "删除人")
    private Long deletedBy;

    /**
     * 删除标志
     */
    @Schema(description = "删除标志")
    private Long deletedAt;

}
