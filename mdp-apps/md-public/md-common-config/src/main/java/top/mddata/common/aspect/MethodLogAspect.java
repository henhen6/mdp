package top.mddata.common.aspect;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import top.mddata.common.properties.SystemProperties;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Service 层通用方法日志切面：进入方法前打印入参，执行结束打印返回值与耗时（controllerLayer 切点保留，需要时加入 @Around 即可）。
 * <p>
 * 与 SysLogAspect（@RequestLog 注解驱动、异步审计入库）定位不同，本切面用于开发/测试期全量观察方法调用链，
 * 由 mdp.system.recordLog / recordArgs / recordResult 三个配置控制（SystemProperties 带 @RefreshScope，
 * Nacos 修改后即时生效）。
 * <p>
 * 异常只记录方法上下文后原样抛出，由 AbstractGlobalExceptionHandler 统一处理响应与堆栈，切面不吞异常。
 *
 * @author henhen
 * @since 2026/9/8
 */
@Slf4j
@Aspect
public class MethodLogAspect {

    /**
     * 单条日志内容最大长度，防止大结果集刷爆日志
     */
    private static final int MAX_LENGTH = 2000;

    private final SystemProperties systemProperties;

    public MethodLogAspect(SystemProperties systemProperties) {
        this.systemProperties = systemProperties;
    }

    /**
     * Controller 层切点。按声明类所在包匹配，继承自 SuperController 的 CRUD 方法（声明在
     * top.mddata.base.mvcflex.controller 包）同样会被拦截
     */
    @Pointcut("execution(public * top.mddata..controller..*.*(..))")
    public void controllerLayer() {
    }

    /**
     * Service 层切点。
     * Spring AOP 中 execution/within/this 的类型模式在逐方法匹配时都按"方法声明类"判定：
     * pageAs/listAs 声明在 IService（com.mybatisflex 包）、list/save 声明在 ServiceImpl，按 top.mddata 包名永远拦不到。
     * 主表达式用 IService+ —— + 表示"该类型及其子类型"，IService 自身声明的 default 方法与
     * SuperService/业务接口/impl 声明的方法全部命中（所有业务 Service 最终都实现 IService）；
     * within 兜底未走 IService 体系但位于 service 包下的 Bean
     */
    @Pointcut("""
            execution(public * com.mybatisflex.core.service.IService+.*(..)) || \
            (within(top.mddata..service..*) && execution(public * *(..)))""")
    public void serviceLayer() {
    }

    @Around("serviceLayer()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // 总开关关闭时不做任何序列化；SystemProperties 是 @RefreshScope 代理，每次读取都能感知配置刷新
        if (!Boolean.TRUE.equals(systemProperties.getRecordLog())) {
            return pjp.proceed();
        }
        long start = System.nanoTime();
        String location = pjp.getTarget().getClass().getSimpleName() + "#" + pjp.getSignature().getName();
        // 入参先于执行打印：方法死锁/超时/线程被杀时仍可见现场，配合结束日志可定位卡点
        log.info("方法进入[{}] 入参:{}", location, formatArgs(pjp.getArgs()));
        try {
            Object result = pjp.proceed();
            log.info("方法完成[{}] 耗时:{}ms 返回:{}", location, costMs(start), formatResult(result));
            return result;
        } catch (Throwable e) {
            // 入参已在进入方法时打印过，这里不重复；只记异常类名+消息不打堆栈：
            // 堆栈由 AbstractGlobalExceptionHandler 统一记录，避免同一异常在切面各层级重复刷屏
            log.warn("方法异常[{}] 耗时:{}ms 异常:[{}] {}",
                    location, costMs(start), e.getClass().getName(), e.getMessage());
            throw e;
        }
    }

    private String formatArgs(Object[] args) {
        if (!Boolean.TRUE.equals(systemProperties.getRecordArgs())) {
            return "-";
        }
        if (args == null || args.length == 0) {
            return "[]";
        }
        try {
            return truncate(JSON.toJSONString(Arrays.stream(args).map(this::sanitize).toArray()));
        } catch (Exception e) {
            // 日志序列化失败降级为 toString，绝不影响业务方法本身
            log.warn("方法入参序列化失败，已降级为 toString", e);
            return truncate(Arrays.toString(args));
        }
    }

    private String formatResult(Object result) {
        if (!Boolean.TRUE.equals(systemProperties.getRecordResult())) {
            return "-";
        }
        if (result == null) {
            return "null";
        }
        Object value = sanitize(result);
        if (value != result) {
            return String.valueOf(value);
        }
        try {
            return truncate(JSON.toJSONString(result));
        } catch (Exception e) {
            log.warn("方法返回值序列化失败，已降级为 toString", e);
            return truncate(String.valueOf(result));
        }
    }

    /**
     * 将不可/不宜序列化的参数置换为描述占位符。
     * 文件类参数只读取文件名与大小元信息，绝不触碰内容流；
     * 仅用于日志展示，不会传给 proceed()，不影响接口实际取参
     */
    private Object sanitize(Object arg) {
        if (arg == null) {
            return null;
        }
        if (arg instanceof MultipartFile file) {
            return "[文件 name=" + file.getOriginalFilename() + ",size=" + file.getSize() + "]";
        }
        if (arg instanceof MultipartFile[] files) {
            return Arrays.stream(files).map(this::sanitize).filter(Objects::nonNull).toList();
        }
        if (arg instanceof Collection<?> collection && collection.stream().anyMatch(MultipartFile.class::isInstance)) {
            return collection.stream().map(this::sanitize).filter(Objects::nonNull).toList();
        }
        // ResponseEntity 常见于文件下载接口，序列化其 body 可能触发读取资源流，只记状态码
        if (arg instanceof ResponseEntity<?> responseEntity) {
            Object body = responseEntity.getBody();
            return "[ResponseEntity status=" + responseEntity.getStatusCode()
                   + ",bodyType=" + (body == null ? "null" : body.getClass().getSimpleName()) + "]";
        }
        if (arg instanceof ServletRequest || arg instanceof ServletResponse || arg instanceof HttpSession
            || arg instanceof Part || arg instanceof File || arg instanceof Resource
            || arg instanceof InputStream || arg instanceof StreamingResponseBody
            || arg instanceof Errors || arg instanceof Model || arg instanceof RedirectAttributes) {
            return "[已省略:" + arg.getClass().getSimpleName() + "]";
        }
        if (arg instanceof byte[]) {
            return "[已省略:byte[]]";
        }
        return arg;
    }

    private long costMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private String truncate(String text) {
        return text.length() <= MAX_LENGTH ? text : text.substring(0, MAX_LENGTH) + "...(截断)";
    }
}
