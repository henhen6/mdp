package top.mddata.open.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 开放接口实体类。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApiBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_api";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用名称
     */
    private String applicationName;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * 版本号
     */
    private String apiVersion;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 接口类名
     */
    private String interfaceClassName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 参数信息
     */
    private String paramInfo;

    /**
     * 需要授权
     * [0-否 1-是]
     */
    private Integer permission;

    /**
     * 需要token
     * [0-否 1-是]
     */
    private Integer needToken;

    /**
     * 公共响应参数
     * [0-否 1-是]
     */
    private Integer commonResponse;

    /**
     * 注册来源
     * [1-系统注册 2-手动注册]
     */
    private Integer regSource;

    /**
     * 接口模式
     * [1-open接口 2-Restful模式]
     */
    private Integer apiMode;

    /**
     * 状态
     * [1-启用 0-禁用]
     */
    private Integer state;

}
