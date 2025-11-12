package top.mddata.common.enumeration.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

import java.util.stream.Stream;

/**
 * 数据类型
 * [1-字符串 2-整型 3-布尔]
 * @author henhen6
 * @since 2025/9/24 00:03
 */
@Getter
@RequiredArgsConstructor
@Schema(title = "DataTypeEnum", description = "数据类型-枚举")
public enum DataTypeEnum implements BaseEnum<String> {
    STRING("1", "字符串"),
    INTEGER("2", "整型"),
    BOOLEAN("3", "布尔型");
    private final String code;
    private final String desc;

    /**
     * 根据当前枚举的name匹配
     */
    public static DataTypeEnum match(String val, DataTypeEnum def) {
        return Stream.of(values()).parallel().filter(item -> item.getCode().equalsIgnoreCase(val)).findAny().orElse(def);
    }

    public static DataTypeEnum match(String val) {
        if (String.class.getName().equals(val)) {
            return STRING;
        } else if (Integer.class.getName().equals(val)) {
            return INTEGER;
        } else if (Boolean.class.getName().equals(val)) {
            return BOOLEAN;
        } else {
            return STRING;
        }
    }

    public static DataTypeEnum get(String val) {
        return match(val, null);
    }

}
