package top.mddata.base.mapstruct;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.mapstruct.Qualifier;
import top.mddata.base.utils.ClassUtils;
import top.mddata.base.utils.JsonUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * 类型转换
 * @author henhen6
 * @since 2024/6/28 21:31
 */
public interface MapStructMethod {
    /**
     * 获取类
     *
     * @param name 类名
     * @return 类
     */
    @NameToClass
    default Class<?> nameToClass(String name) {
        return ClassUtils.forName(name);
    }

    /**
     * 字符串转为对象
     *
     * 因为这个方法是2个参数，不能通过 qualifiedBy 方法使用
     * 只能通过这种方式使用 expression = "java(parseObject(xx, xxx.class))"
     *
     * @param str 带解析字符串
     * @param clazz 目标对象
     * @return 目标对象
     */
    default <T> T parseObject(String str, Class<T> clazz) {
        if (StrUtil.isBlank(str)) {
            return null;
        }
        return JsonUtil.parse(str, clazz);
    }

    /**
     * 对象转json字符串
     *
     * @param obj 对象
     * @return json字符串
     */
    @ToJson
    default String toJsonString(Object obj) {
        return JsonUtil.toJson(obj);
    }

    /**
     * 字符串转为Map
     *
     * @param str 带解析字符串
     * @return Map
     */
    @ParseMap
    default Map<String, String> parseMap(String str) {
        return JsonUtil.parse(str, new TypeReference<>() {
        });
    }

    /**
     * 任意对象 转为json字符串
     */
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    @interface ToJson {
    }

    /**
     * json字符串 解析为Map
     */
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    @interface ParseMap {
    }

    /**
     * 获取类
     */
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    @interface NameToClass {
    }

}
