package top.mddata.base.fieldperm.engine;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import top.mddata.base.fieldperm.model.FieldRule;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字段权限执行引擎（纯函数，无 web/DB 依赖）。
 *
 * <p>遍历返回对象树（R→data、Page→records、集合/Map/嵌套 VO），
 * 对命中 {@code rules} 的属性按规则处理：隐藏置 null、脱敏走 {@link Masker}。</p>
 *
 * <p>同名字段语义：对象树中所有名为 property 的字段统一受限——
 * 同一页面里"手机号"就该统一脱敏，这是特性而非误伤。</p>
 *
 * @author henhen6
 * @since 2026-10-02
 */
@Slf4j
public class FieldPermEngine {

    /** 默认最大下钻深度（按 bean 层数计，集合/Map 不占深度） */
    public static final int DEFAULT_MAX_DEPTH = 5;
    /** 默认单次遍历对象数上限，防大响应拖垮请求 */
    public static final int DEFAULT_MAX_OBJECTS = 5000;

    /** 不深入遍历的包前缀（JDK/框架内部对象无业务字段） */
    private static final List<String> SKIP_PACKAGES = List.of(
            "java.", "javax.", "jakarta.", "sun.",
            "org.springframework.", "com.mybatisflex.", "org.apache.", "com.fasterxml.");

    private static final Map<Class<?>, BeanMeta> META_CACHE = new ConcurrentHashMap<>();

    private final Masker masker;
    private final int maxDepth;
    private final int maxObjects;

    public FieldPermEngine(Masker masker) {
        this(masker, DEFAULT_MAX_DEPTH, DEFAULT_MAX_OBJECTS);
    }

    public FieldPermEngine(Masker masker, int maxDepth, int maxObjects) {
        this.masker = masker;
        this.maxDepth = maxDepth;
        this.maxObjects = maxObjects;
    }

    /**
     * 对目标对象树应用字段规则（原地修改）。
     *
     * @param target 响应对象（R/Page/List/VO 均可）
     * @param rules  property → 规则；为空直接返回
     */
    public void apply(Object target, Map<String, FieldRule> rules) {
        if (target == null || MapUtil.isEmpty(rules)) {
            return;
        }
        walk(target, rules, 0, new WalkContext());
    }

    private void walk(Object node, Map<String, FieldRule> rules, int depth, WalkContext ctx) {
        if (node == null) {
            return;
        }
        if (ctx.visited.containsKey(node)) {
            return;
        }
        ctx.visited.put(node, Boolean.TRUE);
        if (++ctx.count > maxObjects) {
            if (!ctx.truncated) {
                ctx.truncated = true;
                log.warn("字段权限遍历对象数超过阈值 {}, 已截断剩余遍历", maxObjects);
            }
            return;
        }

        // 容器判断必须先于"java.* 简单类型"判断：ArrayList/HashMap 同样是 java.* 包
        if (node instanceof Collection<?> coll) {
            coll.forEach(item -> walk(item, rules, depth, ctx));
            return;
        }
        if (node instanceof Map<?, ?> map) {
            map.values().forEach(value -> walk(value, rules, depth, ctx));
            return;
        }
        Class<?> nodeClass = node.getClass();
        if (nodeClass.isArray()) {
            int len = Array.getLength(node);
            for (int i = 0; i < len; i++) {
                walk(Array.get(node, i), rules, depth, ctx);
            }
            return;
        }
        if (isSimple(nodeClass) || isSkippedPackage(nodeClass)) {
            return;
        }

        applyRules(node, rules, ctx);
        if (depth >= maxDepth) {
            return;
        }
        for (PropertyDescriptor pd : metaOf(nodeClass).complexProperties) {
            Object child = read(node, pd);
            walk(child, rules, depth + 1, ctx);
        }
    }

    /** 命中规则的属性：隐藏置 null；脱敏仅处理非空 String */
    private void applyRules(Object bean, Map<String, FieldRule> rules, WalkContext ctx) {
        Map<String, PropertyDescriptor> properties = metaOf(bean.getClass()).allProperties;
        rules.forEach((property, rule) -> {
            PropertyDescriptor pd = properties.get(property);
            if (pd == null || pd.getWriteMethod() == null) {
                return;
            }
            if (rule.isHide()) {
                write(bean, pd, null);
            } else if (rule.isMask()) {
                Object value = read(bean, pd);
                if (value instanceof String s && !s.isEmpty()) {
                    write(bean, pd, masker.mask(rule.getMaskRule(), s));
                }
            }
        });
    }

    private Object read(Object bean, PropertyDescriptor pd) {
        if (pd.getReadMethod() == null) {
            return null;
        }
        try {
            return pd.getReadMethod().invoke(bean);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // 单个属性读取失败不应拖垮整个响应，记录后按无值处理
            log.error("字段权限遍历读取属性失败: {}#{}", bean.getClass().getName(), pd.getName(), e);
            return null;
        }
    }

    private void write(Object bean, PropertyDescriptor pd, Object value) {
        try {
            pd.getWriteMethod().invoke(bean, value);
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            log.error("字段权限写入属性失败: {}#{}", bean.getClass().getName(), pd.getName(), e);
        }
    }

    private static BeanMeta metaOf(Class<?> clazz) {
        return META_CACHE.computeIfAbsent(clazz, FieldPermEngine::introspect);
    }

    private static BeanMeta introspect(Class<?> clazz) {
        try {
            Map<String, PropertyDescriptor> all = new ConcurrentHashMap<>();
            List<PropertyDescriptor> complex = new java.util.ArrayList<>();
            for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz, Object.class).getPropertyDescriptors()) {
                all.put(pd.getName(), pd);
                Class<?> type = pd.getPropertyType();
                if (pd.getReadMethod() != null && type != null && !isSimple(type)) {
                    complex.add(pd);
                }
            }
            return new BeanMeta(all, List.copyOf(complex));
        } catch (IntrospectionException e) {
            log.error("字段权限内省失败: {}", clazz.getName(), e);
            return new BeanMeta(Map.of(), List.of());
        }
    }

    /**
     * 简单类型：叶子，不处理不下钻。
     * 不含"java.* 包名"判断——Object 声明类型的属性（如 R.data）必须视为可下钻，
     * JDK 内部对象统一由 isSkippedPackage 在运行值层面拦截
     */
    private static boolean isSimple(Class<?> type) {
        return type.isPrimitive() || type.isEnum()
                || CharSequence.class.isAssignableFrom(type)
                || Number.class.isAssignableFrom(type)
                || Boolean.class == type || Character.class == type
                || Date.class.isAssignableFrom(type)
                || Temporal.class.isAssignableFrom(type)
                || BigDecimal.class == type || BigInteger.class == type
                || UUID.class == type || Class.class == type;
    }

    private static boolean isSkippedPackage(Class<?> type) {
        String name = type.getName();
        return SKIP_PACKAGES.stream().anyMatch(name::startsWith);
    }

    private static final class WalkContext {
        private final IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<>();
        private int count;
        private boolean truncated;
    }

    private record BeanMeta(Map<String, PropertyDescriptor> allProperties,
                            List<PropertyDescriptor> complexProperties) {
        private BeanMeta {
            allProperties = Collections.unmodifiableMap(allProperties);
        }
    }
}
