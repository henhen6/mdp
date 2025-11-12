package top.mddata.console.permission.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 接口权限实体类。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:29
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ResourceUriBase extends BaseEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_resource_uri";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属菜单
     */
    private Long menuId;

    /**
     * 所属应用
     */
    private String applicationName;

    /**
     * 类名
     */
    private String controller;

    /**
     * 接口名
     */
    private String name;

    /**
     * 接口地址
     */
    private String uri;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 手动录入
     */
    private Boolean isInput;

}
