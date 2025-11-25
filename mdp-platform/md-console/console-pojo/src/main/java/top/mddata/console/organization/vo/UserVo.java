package top.mddata.console.organization.vo;

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
import top.mddata.common.entity.base.UserBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-11-12 15:48:54
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户")
@Table(UserBase.TABLE_NAME)
public class UserVo implements Serializable, EchoVO {

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
    @Echo(api = EchoApi.POSITION_CLASS)
    private Long positionId;
    /**
     * 用户来源
     */
    @Schema(description = "用户来源")
    private String userSource;
    /**
     * 组织
     */
    @Echo(api = EchoApi.ORG_CLASS)
    private List<Long> orgIdList;

    /**
     * 创建日期
     */
    @Schema(description = "创建日期")
    private LocalDateTime createdAt;


    /**
     * 修改日期
     */
    @Schema(description = "修改日期")
    private LocalDateTime updatedAt;
}
