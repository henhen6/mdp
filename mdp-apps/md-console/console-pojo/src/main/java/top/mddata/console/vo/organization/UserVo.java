package top.mddata.console.vo.organization;

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
     * 手机号
     */
    @Schema(description = "手机号")
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
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 状态
     * [0-禁用 1-正常]
     */
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 最近登录部门
     */
    @Schema(description = "最近登录部门")
    private Long lastDeptId;

    /**
     * 最近登录单位
     */
    @Schema(description = "最近登录单位")
    private Long lastCompanyId;

    /**
     * 最近登录顶级单位
     */
    @Schema(description = "最近登录顶级单位")
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
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;


    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private LocalDateTime updatedAt;

    /**
     * 最近密码错误时间
     */
    private LocalDateTime pwErrorLastTime;

    /**
     * 密码错误次数
     */
    private Integer pwErrorNum;

    /**
     * 密码过期时间
     */
    private LocalDateTime pwExpireTime;

    /**
     * 最近登录时间
     */
    private LocalDateTime lastLoginTime;
}
