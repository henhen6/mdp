package top.mddata.base.boot.handler;

import top.mddata.base.annotation.web.ParamName;
import cn.hutool.core.util.ReflectUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author henhen6
 * @since 2025/9/3 12:29
 */
public class ParamAttrProcessor extends ServletModelAttributeMethodProcessor {

    //Rename cache
    private final Map<Class<?>, Map<String, String>> replaceMap = new ConcurrentHashMap<>();

    public ParamAttrProcessor() {
        super(true);
    }

    private static Map<String, String> analyzeClass(Class<?> targetClass) {
        Field[] fields = ReflectUtil.getFields(targetClass);
        Map<String, String> renameMap = new HashMap<>();
        for (Field field : fields) {
            ParamName paramNameAnnotation = field.getAnnotation(ParamName.class);
            if (paramNameAnnotation != null && !paramNameAnnotation.value().isEmpty()) {
                renameMap.put(paramNameAnnotation.value(), field.getName());
            }
        }
        if (renameMap.isEmpty()) {
            return Collections.emptyMap();
        }
        return renameMap;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (!BeanUtils.isSimpleProperty(parameter.getParameterType())) {
            Field[] fields = ReflectUtil.getFields(parameter.getParameterType());
            for (Field field : fields) {
                if (field.getDeclaredAnnotation(ParamName.class) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest nativeWebRequest) {
        Object target = binder.getTarget();
        Class<?> targetClass = target.getClass();
        if (!replaceMap.containsKey(targetClass)) {
            Map<String, String> mapping = analyzeClass(targetClass);
            replaceMap.put(targetClass, mapping);
        }
        Map<String, String> mapping = replaceMap.get(targetClass);
        ParamDataBinder paramNameDataBinder = new ParamDataBinder(target, binder.getObjectName(), mapping);
        super.bindRequestParameters(paramNameDataBinder, nativeWebRequest);
    }
}