package top.mddata.base.mvcflex.context;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;

/**
 * 当前请求"提交过的字段集合"持有器。
 *
 * <p>解决表单修改的核心歧义：JSON 反序列化后，"字段没提交"与"提交了 null"都是 Java null。
 * UpdateFieldAdvice 在请求体反序列化前，把请求体顶层 key 集合存入 request attribute，
 * SuperServiceImpl.updateBefore 据此实现"只更新提交过的字段，提交为 null 的字段置空"。</p>
 *
 * <p>选用 request attribute 而非 ThreadLocal：随请求销毁，无残留与跨请求污染。</p>
 */
public final class UpdateFieldContext {

    private static final String PRESENT_FIELDS_ATTR = "mdp.update.presentFields";

    private UpdateFieldContext() {
    }

    public static void set(Set<String> fields) {
        ServletRequestAttributes attrs = currentRequest();
        if (attrs != null) {
            attrs.getRequest().setAttribute(PRESENT_FIELDS_ATTR, fields);
        }
    }

    /**
     * @return 提交过的字段集合；null 表示非 HTTP 表单入口（内部调用、单元测试）
     */
    @SuppressWarnings("unchecked")
    public static Set<String> get() {
        ServletRequestAttributes attrs = currentRequest();
        if (attrs == null) {
            return null;
        }
        return (Set<String>) attrs.getRequest().getAttribute(PRESENT_FIELDS_ATTR);
    }

    private static ServletRequestAttributes currentRequest() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs
                ? attrs : null;
    }
}
