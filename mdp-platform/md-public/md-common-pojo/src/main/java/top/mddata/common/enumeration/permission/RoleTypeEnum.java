package top.mddata.common.enumeration.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 角色类型
 * [10-功能角色 20-桌面角色 30-数据角色]
 *
 * @author henhen6
 * @since 2025/12/1 00:35
 */

@Getter
@AllArgsConstructor
@Schema(description = "角色类型枚举")
public enum RoleTypeEnum implements BaseEnum<String> {
    /**
     * 功能角色
     */
    FUNCTION("10", "功能角色"),

    /**
     * 桌面角色
     */
    DESKTOP("20", "桌面角色"),

    /**
     * 数据角色
     */
    DATA("30", "数据角色");

    /**
     * 分类编码（数据库存储用）
     */
    private final String code;

    /**
     * 分类描述（页面展示/日志打印用）
     */
    private final String desc;
}
