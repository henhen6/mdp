package top.mddata.workbench.vo;

import cn.hutool.core.map.MapUtil;
import com.mybatisflex.annotation.Id;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.annotation.echo.Echo;
import top.mddata.base.interfaces.echo.EchoVO;
import top.mddata.common.constant.EchoApi;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户 VO类（通常用作Controller出参）。
 *
 * @author henhen6
 * @since 2025-09-10 23:43:40
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户")
public class SsoUserVo implements Serializable, EchoVO {

    @Serial
    private static final long serialVersionUID = 1L;
    @Builder.Default
    private final Map<String, Object> echoMap = MapUtil.newHashMap();

    /**
     * 用户ID
     */
    @Id
    @Schema(description = "用户ID")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 密码
     */
    @Schema(description = "密码")
    private String password;

    /**
     * 性别
     * [0-男 1-女]
     */
    @Schema(description = "性别")
    private String sex;

    /**
     * 盐值
     */
    @Schema(description = "盐值")
    private String salt;

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
     * 输错密码时间
     */
    @Schema(description = "输错密码时间")
    private LocalDateTime pwErrorLastTime;

    /**
     * 密码错误次数
     */
    @Schema(description = "密码错误次数")
    private Integer pwErrorNum;

    /**
     * 密码过期时间
     */
    @Schema(description = "密码过期时间")
    private LocalDateTime pwExpireTime;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    /**
     * 所属岗位
     */
    @Schema(description = "所属岗位")
    @Echo(api = EchoApi.POSITION_CLASS)
    private Long positionId;

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
     * 修改人
     */
    @Schema(description = "修改人")
    private Long updatedBy;

    /**
     * 最后修改时间
     */
    @Schema(description = "最后修改时间")
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

    /**
     * 组织
     */
    @Echo(api = EchoApi.ORG_CLASS)
    private List<Long> orgIdList;

}
