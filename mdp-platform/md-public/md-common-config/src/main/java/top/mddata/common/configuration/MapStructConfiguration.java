package top.mddata.common.configuration;

import top.mddata.base.utils.ClassUtils;
import io.github.linpeilie.annotations.ComponentModelConfig;
import org.mapstruct.Named;

/**
 * 类型转换
 * @author henhen6
 * @since 2024/6/28 21:31
 */

@ComponentModelConfig(componentModel = "default")
public class MapStructConfiguration {
    public static final String NAME_TO_CLASS = "nameToClass";

    @Named(NAME_TO_CLASS)
    public Class<?> nameToClass(String name) {
        return ClassUtils.forName(name);
    }

}
