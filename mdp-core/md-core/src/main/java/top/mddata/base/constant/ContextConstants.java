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
    public static final String TOKEN_KEY = "Token";
    /**
     * 请求头中携带的 客户端信息 key
     */
    public static final String CLIENT_KEY = "Authorization";
    /**
     * 请求头中携带的 应用id key
     */
    public static final String APPLICATION_ID_KEY = "ApplicationId";
    /**
     * JWT中封装的 用户id
     */
    public static final String JWT_KEY_USER_ID = "UserId";
    /**
     * JWT中封装的 用户当前所有单位ID
     */
    public static final String JWT_KEY_COMPANY_ID = "CurrentCompanyId";
    /**
     * 请求头中携带的 当前所属的顶级公司ID key
     */
    public static final String JWT_KEY_TOP_COMPANY_ID = "CurrentTopCompanyId";
    /**
     * 请求头中携带的 当前所属的部门ID key
     */
    public static final String JWT_KEY_DEPT_ID = "CurrentDeptId";
    /**
     * 请求头中携带的 当前所属的顶级公司是否是管理员
     */
    public static final String JWT_KEY_TOP_COMPANY_IS_ADMIN = "CurrentTopCompanyIsAdmin";
    /**
     * 请求头和线程变量中的 用户ID
     */
    public static final String USER_ID_HEADER = JWT_KEY_USER_ID;
    /**
     * 请求头和线程变量中的 当前单位ID
     */
    public static final String CURRENT_COMPANY_ID_HEADER = JWT_KEY_COMPANY_ID;
    /**
     * 请求头和线程变量中的 当前所属的顶级公司ID
     */
    public static final String CURRENT_TOP_COMPANY_ID_HEADER = JWT_KEY_TOP_COMPANY_ID;
    /**
     * 请求头和线程变量中的 当前所属的部门ID
     */
    public static final String CURRENT_DEPT_ID_HEADER = JWT_KEY_DEPT_ID;
    /**
     * 线程变量和请求头中的 当前所属的顶级公司是否是管理员
     */
    public static final String CURRENT_TOP_COMPANY_IS_ADMIN_HEADER = JWT_KEY_TOP_COMPANY_IS_ADMIN;
    /**
     * 请求头和线程变量中的 应用ID
     */
    public static final String APPLICATION_ID_HEADER = APPLICATION_ID_KEY;
    /**
     * 请求头和线程变量中的 前端页面地址栏#号后的路径
     */
    public static final String PATH_HEADER = "Path";
    /**
     * 请求头和线程变量中的 token
     */
    public static final String TOKEN_HEADER = TOKEN_KEY;
    /**
     * 日志链路追踪id信息头
     */
    public static final String TRACE_ID_HEADER = "trace";
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
