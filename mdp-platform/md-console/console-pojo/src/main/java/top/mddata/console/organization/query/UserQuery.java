package top.mddata.console.organization.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 15:48:54
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户")
public class UserQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 性别
     * [0-男 1-女]
     */
    @Schema(description = "性别")
    private String sex;

    /**
     * 电话号码
     */
    @Schema(description = "电话号码")
    private String phone;

    /**
     * 头像
     */
    @Schema(description = "头像")
    private Long avatar;

    /**
     * 姓名
     */
    @Schema(description = "姓名")
    private String name;

    /**
     * 邮箱地址
     */
    @Schema(description = "邮箱地址")
    private String email;

    /**
     * 状态
     * [0-禁用 1-正常]
     */
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 上次登录的部门
     */
    @Schema(description = "上次登录的部门")
    private Long lastDeptId;

    /**
     * 上次登录的单位
     */
    @Schema(description = "上次登录的单位")
    private Long lastCompanyId;

    /**
     * 上次登录的顶级单位
     */
    @Schema(description = "上次登录的顶级单位")
    private Long lastTopCompanyId;

    /**
     * 所属岗位
     */
    @Schema(description = "所属岗位")
    private Long positionId;
    /**
     * 用户来源
     */
    @Schema(description = "用户来源")
    private String userSource;
    /**
     * 所属组织
     */
    @Schema(description = "所属组织")
    private List<Long> orgIdList;


    @NotNull(message = "角色ID不能为空", groups = {RolePage.class})
    @Schema(description = "角色ID")
    private Long roleId;

    /**
     * true: 查询角色拥有的用户
     * false: 查询角色没有的用户
     */
    @Schema(description = "是否有权限")
    private Boolean hasUser;

    public interface RolePage {

    }

}
