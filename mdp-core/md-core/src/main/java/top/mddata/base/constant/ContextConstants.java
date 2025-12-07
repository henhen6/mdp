package top.mddata.base.constant;

/**
 * 跟上下文常量工具类
 *
 * @author henhen6
 * @since 2018/12/21
 */
public final class ContextConstants {
    /**
     * 请求头中携带的 token key
     */
    public static final String TOKEN = "Token";
    /**
     * 请求头中携带的 客户端信息 key
     */
    public static final String AUTHORIZATION = "Authorization";
    /**
     * 请求头中携带的 应用id key
     */
    public static final String APP_ID = "AppId";
    /**
     * JWT中封装的 用户id
     */
    public static final String USER_ID = "UserId";
    /**
     * JWT中封装的 用户当前所有公司ID
     */
    public static final String COMPANY_ID = "CurrentCompanyId";
    /**
     * JWT中封装的 用户当前所有公司组织性质
     */
    public static final String COMPANY_NATURE = "CurrentCompanyNature";
    /**
     * 请求头中携带的 当前所属的顶级公司ID key
     */
    public static final String TOP_COMPANY_ID = "CurrentTopCompanyId";
    /**
     * 请求头中携带的 当前所属的顶级公司组织性质 key
     */
    public static final String TOP_COMPANY_NATURE = "CurrentTopCompanyNature";
    /**
     * 请求头中携带的 当前所属的部门ID key
     */
    public static final String DEPT_ID = "CurrentDeptId";
    /**
     * 请求头中携带的 当前所属的顶级公司是否是管理员
     */
    public static final String TOP_COMPANY_IS_ADMIN = "CurrentTopCompanyIsAdmin";
    /**
     * 请求头和线程变量中的 前端页面地址栏#号后的路径
     */
    public static final String PATH = "Path";
    /**
     * 日志链路追踪id信息头
     */
    public static final String TRACE = "trace";
    /**
     * 灰度发布版本号
     */
    public static final String GRAY_VERSION = "gray_version";
    /**
     * WriteInterceptor 放行标志
     */
    public static final String PROCEED = "proceed";

    private ContextConstants() {
    }

}
