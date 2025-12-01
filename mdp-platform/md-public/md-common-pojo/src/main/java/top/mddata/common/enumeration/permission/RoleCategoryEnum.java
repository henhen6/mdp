package top.mddata.common.enumeration.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 角色分类
 * [10-普通角色 20-管理员角色 30-权限集合]
 *
 * @author henhen6
 * @since 2025/12/1 00:35
 */

@Getter
@AllArgsConstructor
@Schema(description = "角色分类枚举")
public enum RoleCategoryEnum implements BaseEnum<String> {
    /**
     * 普通角色
     */
    NORMAL_ROLE("10", "普通角色"),

    /**
     * 管理员角色
     */
    ADMIN_ROLE("20", "管理员角色"),

    /**
     * 权限集合
     */
    PERM_SET("30", "权限集合");

    /**
     * 分类编码（数据库存储用）
     */
    private final String code;

    /**
     * 分类描述（页面展示/日志打印用）
     */
    private final String desc;
}
