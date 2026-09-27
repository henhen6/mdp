package top.mddata.console.entity.permission.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 接口权限 实体基类（只放 mdc_resource_api 表字段）。
 *
 * <p>注意：必须 extends SuperEntity&lt;Long&gt;（具体类型），若写成泛型
 * SuperEntity&lt;T&gt; 并配合 Xxx extends XxxBase&lt;Xxx&gt;，继承的 id 会被解析为
 * 实体自身类型，MyBatis-Flex 不识别为主键（TableInfo idCount=0），
 * 插入时 id 列缺失报 "Field 'id' doesn't have a default value"。</p>
 */
@Data
@Accessors(chain = true)
public class ResourceApiBase extends SuperEntity<Long> implements Serializable {
    public static final String TABLE_NAME = "mdc_resource_api";

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "资源id（菜单表id；按钮即 menu_type=50 的菜单行，id 空间统一）")
    private Long resourceId;

    @Schema(description = "接口路径（裸路径，Ant风格通配）")
    private String uri;

    @Schema(description = "请求方式[GET POST PUT DELETE ALL]")
    private String requestMethod;

    @Schema(description = "接口名")
    private String name;

    @Schema(description = "来源类名（手动录入为空）")
    private String controller;

    @Schema(description = "来源服务（手动录入为空）")
    private String applicationName;

    @Schema(description = "是否手动录入")
    private Boolean isInput;
}
