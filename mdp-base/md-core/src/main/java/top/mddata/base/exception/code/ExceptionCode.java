package top.mddata.base.exception.code;


/**
 * 系统异常码枚举
 * <p>
 * 定义了系统中所有业务异常的状态码和描述信息。
 * 异常码遵循 HTTP 状态码规范，并按照业务模块进行分类管理。
 * </p>
 *
 * <h3>异常码分类规则：</h3>
 * <ul>
 *   <li>1xx - 信息性状态码：服务器收到请求，需要请求者继续执行操作</li>
 *   <li>2xx - 成功状态码：操作被成功接收并处理</li>
 *   <li>3xx - 重定向状态码：需要进一步的操作以完成请求</li>
 *   <li>4xx - 客户端错误状态码：请求包含语法错误或无法完成请求</li>
 *   <li>5xx - 服务器错误状态码：服务器在处理请求的过程中发生了错误</li>
 * </ul>
 *
 * <h3>业务编码规则：</h3>
 * <p>
 * 采用分段编码方式：[系统]_[模块]_[功能]
 * 每段3位数，总计9位数字。下划线仅为便于阅读，实际编码中不存在。
 * </p>
 *
 * <p>示例：100_100_001 表示 100系统-100模块-001功能</p>
 *
 * @author henhen6
 * @date 2017-12-13 16:22
 */
public enum ExceptionCode implements BaseExceptionCode {

    // ==================== 系统级异常码 ====================
    
    /**
     * 操作成功
     */
    SUCCESS(0, "成功"),
    
    /**
     * 系统繁忙异常
     */
    SYSTEM_BUSY(-1, "系统繁忙~请稍后再试~"),
    
    /**
     * 系统超时异常
     */
    SYSTEM_TIMEOUT(-2, "系统维护中~请稍后再试~"),
    
    /**
     * 参数类型解析异常
     */
    PARAM_EX(-3, "参数类型解析异常"),
    
    /**
     * SQL执行异常
     */
    SQL_EX(-4, "运行SQL出现异常"),
    
    /**
     * 空指针异常
     */
    NULL_POINT_EX(-5, "空指针异常"),
    
    /**
     * 非法参数异常
     */
    ILLEGAL_ARGUMENT_EX(-6, "无效参数异常"),
    /**
     * 媒体类型异常
     */
    MEDIA_TYPE_EX(-7, "请求类型异常"),
    
    /**
     * 资源加载异常
     */
    LOAD_RESOURCES_ERROR(-8, "加载资源出错"),
    
    /**
     * 参数验证异常
     */
    BASE_VALID_PARAM(-9, "统一验证参数异常"),
    
    /**
     * 操作异常
     */
    OPERATION_EX(-10, "操作异常"),
    
    /**
     * Mapper转换异常
     */
    SERVICE_MAPPER_ERROR(-11, "Mapper类转换异常"),
    
    /**
     * 验证码校验异常
     */
    CAPTCHA_ERROR(-12, "验证码校验失败"),
    
    /**
     * JSON解析异常
     */
    JSON_PARSE_ERROR(-13, "JSON解析异常"),

    IDEMPOTENT_PROCESSING(-14, "相同请求正在处理中，请稍后重试"),
    IDEMPOTENT_CONFLICT(-15, "幂等令牌不能用于不同的请求内容"),
    IDEMPOTENT_TOKEN_INVALID(-16, "幂等令牌缺失、失效或身份不匹配"),


    // ==================== HTTP 标准状态码 ====================
    
    /**
     * 请求成功
     */
    OK(200, "OK"),
    
    /**
     * 错误的请求
     */
    BAD_REQUEST(400, "错误的请求"),
    /**
     * 未授权（401 Unauthorized）
     * <p>
     * 客户端在访问请求的资源之前，对token进行验证，token无法正常解析时，返回此错误码。
     * 该HTTP状态码表示认证错误，它是为了认证设计的，而不是为了授权设计的。
     * 收到401响应，表示请求没有被认证—压根没有认证或者认证不正确—但是请重新认证和重试。
     * </p>
     *
     * @see <a href="http://tools.ietf.org/html/rfc7235#section-3.1">HTTP/1.1: Authentication, section 3.1</a>
     */
    UNAUTHORIZED(401, "未认证"),
    /**
     * 禁止访问（403 Forbidden）
     * <p>
     * 资源不可用，服务器理解客户的请求，但拒绝处理它。
     * 通常由于服务器上文件或目录的权限设置导致，可以简单的理解为没有权限访问此站。
     * 该HTTP状态码是关于授权方面的。从性质上来说是永久的东西，和应用的业务逻辑相关联。
     * 它比401更具体，更实际。收到403响应表示服务器完成认证过程，但是客户端请求没有权限去访问要求的资源。
     * </p>
     */
    FORBIDDEN(403, "禁止访问"),
    /**
     * 资源未找到（404 Not Found）
     *
     * @see <a href="http://tools.ietf.org/html/rfc7231#section-6.5.4">HTTP/1.1: Semantics and Content, section 6.5.4</a>
     */
    NOT_FOUND(404, "没有找到资源"),
    
    /**
     * 请求方法不允许
     */
    METHOD_NOT_ALLOWED(405, "不支持当前请求类型"),

    /**
     * 请求次数过多
     */
    TOO_MANY_REQUESTS(429, "请求超过次数限制"),
    
    /**
     * 内部服务错误
     */
    INTERNAL_SERVER_ERROR(500, "内部服务错误"),
    
    /**
     * 网关错误
     */
    BAD_GATEWAY(502, "网关错误"),
    
    /**
     * 网关超时
     */
    GATEWAY_TIMEOUT(504, "网关超时"),


    // ==================== 业务级异常码 ====================
    
    /**
     * 用户账号被禁用
     */
    JWT_USER_DISABLE(100_000_001, "您的账号被禁用，请联系平台管理员"),
    
    /**
     * 员工账号在公司被禁用
     */
    JWT_EMPLOYEE_DISABLE(100_000_002, "您在该公司的账号被禁用，请联系公司管理员"),
    
    /**
     * 应用未开通权限
     */
    JWT_APPLICATION_FORBIDDEN(100_000_004, "您所在的尚未开通该系统使用权限，现在为您返回基础平台！"),
    
    /**
     * 资源未开通权限
     */
    JWT_RESOURCE_FORBIDDEN(100_000_005, "您所在的企业尚未开通该资源使用权限，请联系公司管理员开通！"),

    /**
     * 跨库事务失败
     */
    TRANSACTIONAL(100_100_001, "跨库处理数据失败"),

    /**
     * 文件参数缺失
     */
    REQUIRED_FILE_PARAM_EX(1001, "请求中必须至少包含一个有效文件"),
    
    /**
     * 数据保存失败
     */
    DATA_SAVE_ERROR(2000, "新增数据失败"),
    
    /**
     * 数据更新失败
     */
    DATA_UPDATE_ERROR(2001, "修改数据失败"),
    
    /**
     * 批量数据过多
     */
    TOO_MUCH_DATA_ERROR(2002, "批量新增数据过多"),

    // ==================== JWT Token 相关异常码 ====================
    
    /**
     * 无效的基本身份验证令牌
     */
    JWT_BASIC_INVALID(40000, "无效的基本身份验证令牌"),
    
    /**
     * Token 过期
     */
    JWT_TOKEN_EXPIRED(40001, "登录超时，请重新登录"),
    
    /**
     * Token 签名不合法
     */
    JWT_SIGNATURE(40002, "不合法的token，请认真比对 token 的签名"),
    
    /**
     * 缺少 Token 参数
     */
    JWT_ILLEGAL_ARGUMENT(40003, "缺少token参数"),
    
    /**
     * 生成 Token 失败
     */
    JWT_GEN_TOKEN_FAIL(40004, "生成token失败"),
    
    /**
     * 解析 Token 失败
     */
    JWT_PARSER_TOKEN_FAIL(40005, "解析用户身份错误，请重新登录！"),
    
    /**
     * 用户认证失败
     */
    JWT_USER_INVALID(40006, "用户名或密码错误"),
    
    /**
     * 用户已被禁用
     */
    JWT_USER_ENABLED(40007, "用户已经被禁用！"),
    
    /**
     * 账号在其他设备登录
     */
    JWT_OFFLINE(40008, "您已在另一个设备登录！"),
    
    /**
     * 登录超时
     */
    JWT_NOT_LOGIN(40009, "登录超时，请重新登录！");

    private final int code;
    private String msg;

    ExceptionCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }


    public ExceptionCode build(String msg, Object... param) {
        this.msg = String.format(msg, param);
        return this;
    }

    public ExceptionCode param(Object... param) {
        msg = String.format(msg, param);
        return this;
    }
}
