package top.mddata.common.enumeration;

import top.mddata.common.enumeration.system.DataTypeEnum;
import top.mddata.common.properties.SystemProperties;
import top.mddata.common.vo.Option;
import top.mddata.base.interfaces.BaseEnum;
import top.mddata.base.utils.ClassUtils;
import top.mddata.base.utils.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;

/**
 *
 * @author henhen6
 * @since 2025/9/23 20:19
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class EnumService {

    // 只扫描继承了 BaseEnum 接口的枚举类
    private static final Predicate<Class<?>> CLASS_FILTER = item -> item != null && item.isEnum() && BaseEnum.class.isAssignableFrom(item) && !item.isInterface();
    // 优化泛型定义，明确键值类型
    private static final Map<Option, List<Option>> ENUM_MAP = new LinkedHashMap<>();
    private final SystemProperties systemProperties;

    public static void main(String[] args) {
        String enumPackage = "top.mddata";
        if (StrUtil.isEmpty(enumPackage)) {
            log.warn("请在配置文件中配置{}.enumPackage", SystemProperties.PREFIX);
            return;
        }
        Set<Class<?>> enumClass = ClassUtils.scanPackage(enumPackage, CLASS_FILTER);

        StringJoiner enumSb = new StringJoiner(StrPool.COMMA);
        enumClass.forEach(item -> {
            Object[] enumConstants = item.getEnumConstants();
            BaseEnum<Serializable>[] baseEnums = Arrays.stream(enumConstants).map(constant -> (BaseEnum<Serializable>) constant).toArray(BaseEnum[]::new);

            // 解析数据类型
            Type typeArgument = TypeUtil.getTypeArgument(baseEnums[0].getClass().getGenericInterfaces()[0]);

            DataTypeEnum dataType = DataTypeEnum.match(typeArgument.getTypeName());

            Option key = new Option();
            // 2. 获取Schema注解的title属性
            Schema schemaAnnotation = item.getAnnotation(Schema.class);
            if (schemaAnnotation != null) {
                String title = schemaAnnotation.description();
                key.setLabel(title);
            } else {
                // 3. 获取类注释的首行内容
                log.warn("{}类上没有@Schema注解", item.getSimpleName());
                key.setLabel(item.getSimpleName());
            }
            key.setValue(item.getSimpleName());
            key.setRemark(dataType.getCode());

            ENUM_MAP.put(key, Option.mapOptions(baseEnums));
            enumSb.add(item.getSimpleName());
        });
    }

    @PostConstruct
    public void init() {
        String enumPackage = systemProperties.getEnumPackage();
        if (StrUtil.isEmpty(enumPackage)) {
            log.warn("请在配置文件中配置{}.enumPackage", SystemProperties.PREFIX);
            return;
        }
        Set<Class<?>> enumClass = ClassUtils.scanPackage(enumPackage, CLASS_FILTER);

        StringJoiner enumSb = new StringJoiner(StrPool.COMMA);
        enumClass.forEach(item -> {
            Object[] enumConstants = item.getEnumConstants();
            BaseEnum<Serializable>[] baseEnums = Arrays.stream(enumConstants).map(constant -> (BaseEnum<Serializable>) constant).toArray(BaseEnum[]::new);
            // 解析数据类型
            Type typeArgument = TypeUtil.getTypeArgument(baseEnums[0].getClass().getGenericInterfaces()[0]);

            DataTypeEnum dataType = DataTypeEnum.match(typeArgument.getTypeName());

            Option key = new Option();
            // 2. 获取Schema注解的title属性
            Schema schemaAnnotation = item.getAnnotation(Schema.class);
            if (schemaAnnotation != null) {
                String title = schemaAnnotation.description();
                key.setLabel(title);
            } else {
                // 3. 获取类注释的首行内容
                log.warn("{}类上没有@Schema注解", item.getSimpleName());
                key.setLabel(item.getSimpleName());
            }
            key.setValue(item.getSimpleName());
            key.setRemark(dataType.getCode());

            ENUM_MAP.put(key, Option.mapOptions(baseEnums));
            enumSb.add(item.getSimpleName());
        });

        log.info("扫描: {} ,共加载了{}个枚举类, 分别为: {}", enumPackage, ENUM_MAP.size(), enumSb);
    }

    /**
     * 查找本服务中，所有的枚举类
     * @param rescan 是否重新扫描
     * @return 枚举数据
     */
    public Map<Option, List<Option>> findAll(Boolean rescan) {
        if (rescan != null && rescan) {
            init();
            return ENUM_MAP;
        }
        return ENUM_MAP;
    }


}
