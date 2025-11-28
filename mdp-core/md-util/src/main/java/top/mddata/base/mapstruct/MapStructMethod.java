package top.mddata.base.mapstruct;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.mapstruct.Named;
import top.mddata.base.utils.ClassUtils;
import top.mddata.base.utils.JsonUtil;

import java.util.Map;

/**
 * 类型转换
 * @author henhen6
 * @since 2024/6/28 21:31
 */
public interface MapStructMethod {
    String NAME_TO_CLASS = "nameToClass";
    String TO_JSON_STRING = "toJsonString";
    String PARSE_MAP = "parseMap";
    String PARSE_OBJECT = "parseObject";

    /**
     * 获取类
     *
     * @param name 类名
     * @return 类
     */
    @Named(NAME_TO_CLASS)
    default Class<?> nameToClass(String name) {
        return ClassUtils.forName(name);
    }

    /**
     * 字符串转为对象
     *
     * @param str 带解析字符串
     * @param clazz 目标对象
     * @return 目标对象
     */
    @Named(PARSE_OBJECT)
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
    @Named(TO_JSON_STRING)
    default String toJsonString(Object obj) {
        return JsonUtil.toJson(obj);
    }

    /**
     * 字符串转为Map
     *
     * @param str 带解析字符串
     * @return Map
     */
    @Named(PARSE_MAP)
    default Map<String, String> parseMap(String str) {
        return JsonUtil.parse(str, new TypeReference<>() {
        });
    }

}
