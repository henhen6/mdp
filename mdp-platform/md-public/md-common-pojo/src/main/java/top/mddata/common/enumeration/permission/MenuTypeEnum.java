package top.mddata.common.enumeration.permission;

import com.mybatisflex.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 菜单类型 [10-目录  20-菜单 30-内链 40-外链]
 *
 * @author henhen6
 * @since 2021/3/12 21:20
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "MENU_TYPE", description = "菜单类型-枚举")
public enum MenuTypeEnum implements BaseEnum<String> {
    /**
     * 目录
     */
    DIR("10", "目录"),
    /**
     * 菜单
     */
    MENU("20", "菜单"),
    /**
     * 内链
     */
    INNER_HREF("30", "内链"),
    /**
     * 外链
     */
    OUTER_HREF("40", "外链"),
    /**
     * 按钮
     */
    BUTTON("50", "按钮");

    /**
     * 资源类型
     */
    @EnumValue
    private String code;

    /**
     * 资源描述
     */
    private String desc;

}

