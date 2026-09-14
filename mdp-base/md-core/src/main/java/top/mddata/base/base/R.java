package top.mddata.base.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 统一的响应结果包装类
 * <p>
 * 用于封装所有 API 接口的返回结果，包含状态码、数据、消息等信息。
 * 遵循 RESTful API 设计规范，提供统一的响应格式。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 成功响应
 * Result&lt;User&gt; result = Result.success(user);
 *
 * // 失败响应
 * Result&lt;Void&gt; result = Result.fail("操作失败");
 *
 * // 自定义状态码
 * Result&lt;Void&gt; result = Result.fail(400, "参数错误");
 * </pre>
 *
 * @param <T> 响应数据类型
 * @author henhen6
 * @since 2017-12-13 10:55
 */
@Getter
@Setter
@Accessors(chain = true)
@SuppressWarnings("ALL")
public class R<T> {
    /**
     * 默认错误消息：系统繁忙时的提示
     */
    public static final String DEF_ERROR_MESSAGE = "系统繁忙，请稍候再试";
    
    /**
     * 超时错误消息：请求超时时的提示
     */
    public static final String HYSTRIX_ERROR_MESSAGE = "请求超时，请稍候再试";
    
    /**
     * 成功状态码
     */
    public static final int SUCCESS_CODE = 0;
    
    /**
     * 失败状态码：系统繁忙
     */
    public static final int FAIL_CODE = -1;
    
    /**
     * 超时状态码
     */
    public static final int TIMEOUT_CODE = -2;
    
    /**
     * 参数验证异常状态码
     */
    public static final int VALID_EX_CODE = -9;
    
    /**
     * 操作异常状态码
     */
    public static final int OPERATION_EX_CODE = -10;
    /**
     * 响应状态码
     * <p>
     * 0 表示请求处理成功，其他值表示各种异常情况。
     * 具体状态码定义请参考 {@link top.mddata.base.exception.code.ExceptionCode}
     * </p>
     */
    @Schema(description = "响应编码:0-请求处理成功")
    private int code;

    /**
     * 是否执行默认操作标识
     * <p>
     * 用于前端或调用方判断是否需要执行默认的成功/失败处理逻辑。
     * true 表示需要执行默认操作，false 表示由调用方自行处理。
     * </p>
     */
    @JsonIgnore
    private Boolean defExec = true;

    /**
     * 响应数据
     * <p>
     * 业务接口返回的实际数据内容，类型由泛型 T 决定。
     * 成功时包含业务数据，失败时通常为 null。
     * </p>
     */
    @Schema(description = "响应数据")
    private T data;

    /**
     * 响应消息
     * <p>
     * 用于向调用方展示操作结果的提示信息。
     * 成功时通常为 "ok"，失败时包含具体的错误描述。
     * </p>
     */
    @Schema(description = "提示消息")
    private String msg = "ok";

    /**
     * 请求路径
     * <p>
     * 记录发起请求的 API 路径，用于日志记录和异常追踪。
     * </p>
     */
    @Schema(description = "请求路径")
    private String path;
    /**
     * 附加数据
     * <p>
     * 用于存放除主要 data 之外的额外信息，如分页信息、统计信息等。
     * 采用 Key-Value 结构，key 为 Object 类型以支持灵活的键名。
     * </p>
     */
    @Schema(description = "附加数据")
    private Map<Object, Object> extra;

    /**
     * 响应时间戳
     * <p>
     * 记录服务器处理完成并返回响应的时间，单位为毫秒。
     * 可用于客户端计算请求耗时或进行时间同步。
     * </p>
     */
    @Schema(description = "响应时间戳")
    private long timestamp = System.currentTimeMillis();

    /**
     * 原始异常消息
     * <p>
     * 仅在开发或测试环境下返回，用于开发者调试定位问题。
     * 生产环境下此字段为空，避免敏感信息泄露。
     * </p>
     */
    @Schema(description = "异常消息")
    private String errorMsg = "";

    /**
     * 私有构造函数：创建默认成功响应
     * <p>
     * 通过私有构造函数防止外部直接实例化，统一使用静态工厂方法创建实例。
     * </p>
     */
    private R() {
        this.defExec = false;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 构造函数：创建指定状态码、数据和消息的响应
     *
     * @param code 响应状态码
     * @param data 响应数据
     * @param msg  响应消息
     */
    public R(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
        this.defExec = false;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 构造函数：创建包含异常消息的响应
     *
     * @param code      响应状态码
     * @param data      响应数据
     * @param msg       响应消息
     * @param errorMsg  原始异常消息（仅开发环境）
     */
    public R(int code, T data, String msg, String errorMsg) {
        this(code, data, msg);
        this.errorMsg = errorMsg;
    }

    /**
     * 构造函数：创建指定默认操作标识的响应
     *
     * @param code    响应状态码
     * @param data    响应数据
     * @param msg     响应消息
     * @param defExec 是否执行默认操作
     */
    public R(int code, T data, String msg, boolean defExec) {
        this(code, data, msg);
        this.defExec = defExec;
    }

    /**
     * 创建自定义响应结果
     *
     * @param code 响应状态码
     * @param data 响应数据
     * @param msg  响应消息
     * @param <E>  数据类型
     * @return 响应结果对象
     */
    public static <E> R<E> result(int code, E data, String msg) {
        return new R<>(code, data, msg);
    }

    /**
     * 创建包含异常消息的自定义响应结果
     *
     * @param code      响应状态码
     * @param data      响应数据
     * @param msg       响应消息
     * @param errorMsg  原始异常消息
     * @param <E>       数据类型
     * @return 响应结果对象
     */
    public static <E> R<E> result(int code, E data, String msg, String errorMsg) {
        return new R<>(code, data, msg, errorMsg);
    }

    /**
     * 创建成功响应（带数据）
     *
     * @param data 业务数据
     * @param <E>  数据类型
     * @return 成功的响应结果
     */
    public static <E> R<E> success(E data) {
        return new R<>(SUCCESS_CODE, data, "ok");
    }

    /**
     * 创建成功响应（无数据）
     * <p>
     * 适用于不需要返回数据的操作，如删除、更新等。
     * </p>
     *
     * @return 成功的响应结果，data 字段为 true
     */
    public static R<Boolean> success() {
        return new R<>(SUCCESS_CODE, true, "ok");
    }

    /**
     * 创建失败响应
     *
     * @param code 错误状态码
     * @param msg  错误消息，如果为空则使用默认错误消息
     * @param <E>  数据类型
     * @return 失败的响应结果
     */
    public static <E> R<E> fail(int code, String msg) {
        return new R<>(code, null, (msg == null || msg.isEmpty()) ? DEF_ERROR_MESSAGE : msg);
    }

    /**
     * 创建包含异常消息的失败响应
     *
     * @param code      错误状态码
     * @param msg       错误消息
     * @param errorMsg  原始异常消息
     * @param <E>       数据类型
     * @return 失败的响应结果
     */
    public static <E> R<E> fail(int code, String msg, String errorMsg) {
        return new R<>(code, null, (msg == null || msg.isEmpty()) ? DEF_ERROR_MESSAGE : msg, errorMsg);
    }

    /**
     * 创建操作失败响应（支持消息格式化）
     *
     * @param msg   错误消息模板，支持 String.format 格式
     * @param args  格式化参数
     * @param <E>   数据类型
     * @return 失败的响应结果
     */
    public static <E> R<E> fail(String msg, Object... args) {
        String message = (msg == null || msg.isEmpty()) ? DEF_ERROR_MESSAGE : msg;
        return new R<>(OPERATION_EX_CODE, null, String.format(message, args));
    }

    /**
     * 创建操作失败响应
     *
     * @param msg 错误消息
     * @param <E> 数据类型
     * @return 失败的响应结果
     */
    public static <E> R<E> fail(String msg) {
        return fail(OPERATION_EX_CODE, msg);
    }

    /**
     * 创建超时响应
     * <p>
     * 用于服务降级、熔断等场景，提示用户稍后重试。
     * </p>
     *
     * @param <E> 数据类型
     * @return 超时的响应结果
     */
    public static <E> R<E> timeout() {
        return fail(TIMEOUT_CODE, HYSTRIX_ERROR_MESSAGE);
    }

    /**
     * 添加附加数据
     * <p>
     * 向 extra 字段中添加键值对，用于存放额外的响应信息。
     * 如果 extra 为 null，会自动初始化。
     * </p>
     *
     * @param key   键
     * @param value 值
     * @return 当前 Result 对象，支持链式调用
     */
    public R<T> put(String key, Object value) {
        if (this.extra == null) {
            this.extra = new ConcurrentHashMap<>(16);
        }
        this.extra.put(key, value);
        return this;
    }

    /**
     * 批量添加附加数据
     * <p>
     * 将指定的 Map 中的所有键值对添加到 extra 中。
     * 如果 extra 为 null，会自动初始化。
     * </p>
     *
     * @param extra 附加数据 Map
     * @return 当前 Result 对象，支持链式调用
     */
    public R<T> putAll(Map<Object, Object> extra) {
        if (this.extra == null) {
            this.extra = new ConcurrentHashMap<>(16);
        }
        this.extra.putAll(extra);
        return this;
    }

    /**
     * 判断请求是否成功
     * <p>
     * 根据状态码判断请求是否处理成功。
     * 状态码为 0 时视为成功，其他值视为失败。
     * </p>
     *
     * @return true 表示成功，false 表示失败
     */
    public Boolean getIsSuccess() {
        return this.code == SUCCESS_CODE;
    }

    @Override
    public String toString() {
        return "R{" +
               "code=" + code +
               ", defExec=" + defExec +
               ", data=" + data +
               ", msg='" + msg + '\'' +
               ", path='" + path + '\'' +
               ", extra=" + extra +
               ", timestamp=" + timestamp +
               ", errorMsg='" + errorMsg + '\'' +
               '}';
    }
}
