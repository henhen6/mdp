package top.mddata.console.permission.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Vue路由 Meta
 *
 * @author henhen6
 * @date 2025年07月01日22:06:28
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RouterMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = 5499925008927195914L;
    /**
     * 路由标题
     *
     * 可用于文档标题中
     */
//    @NotEmpty(message = "请填写路由标题")
    private String title;
    /**
     * Iconify 图标
     *
     * 可用于菜单或面包屑中
     */
    private String icon;
    /**
     * 用于配置页面的激活图标，会在菜单中显示。一般会配合图标库使用，如果是http链接，会自动加载图片。
     */
    private String activeIcon;

    /**
     * 是否缓存该路由
     * 用于配置页面是否开启缓存，开启后页面会缓存，不会重新加载，仅在标签页启用时有效。
     */
    private Boolean keepAlive;

    /** 用于配置页面是否在菜单中隐藏，隐藏后页面不会在菜单中显示。 */
    private Boolean hideInMenu;
    /** 菜单可见，但是禁止访问 */
    private Boolean menuVisibleWithForbidden;

    /** 用于配置页面是否在标签页中隐藏，隐藏后页面不会在标签页中显示。 */
    private Boolean hideInTab;
    /** 用于配置页面是否在面包屑中隐藏，隐藏后页面不会在面包屑中显示。 */
    private Boolean hideInBreadcrumb;
    /** 用于配置页面的子页面是否在菜单中隐藏，隐藏后子页面不会在菜单中显示。     */
    private Boolean hideChildrenInMenu;
    /** 用于配置页面的权限，只有拥有对应权限的用户才能访问页面，不配置则不需要权限。 */
    private List<String> authority;
    /** 用于配置页面的徽标，会在菜单显示。 */
    private String badge;
    /** 用于配置页面的徽标类型，dot 为小红点，normal 为文本。 'dot' | 'normal'  */
    private String badgeType;
    /** 用于配置页面的徽标颜色。
     * 'default' | 'destructive' | 'primary' | 'success' | 'warning' | string
     */
    private String badgeVariants;
    /** 是否将路由的完整路径作为tab key（默认true） */
    private Boolean fullPathKey;
    /** 用于配置当前激活的菜单，有时候页面没有显示在菜单内，需要激活父级菜单时使用。 */
    private String activePath;
    /**  用于配置页面是否固定标签页，固定后页面不可关闭。 */
    private Boolean affixTab;
    /**
     * 用于配置内嵌页面的 iframe 地址，设置后会在当前页面内嵌对应的页面。
     */
    private String iframeSrc;
    /** 是否不需要登录就能直接访问。  */
    private Boolean ignoreLogin;
    /** 用于配置页面是否忽略权限，直接可以访问。 */
    private Boolean ignoreAccess;
    /** 用于配置外链跳转路径，会在新窗口打开。 */
    private String link;
    /** 设置为 true 时，会在新窗口打开页面。 */
    private Boolean openInNewWindow;

    /**
     * 组件
     */
    private String component;
}
