package top.mddata.console.system.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

import java.util.stream.Stream;

/**
 * 字典项结构
 * [01-列表 02-树结构]
 * @author henhen6
 * @since 2025/9/24 00:03
 */
@Getter
@RequiredArgsConstructor
@Schema(title = "ItemTypeEnum", description = "字典项结构-枚举")
public enum ItemTypeEnum implements BaseEnum<String> {
    LIST("01", "列表"),
    TREE("02", "树结构");
    private final String code;
    private final String desc;

    /**
     * 根据当前枚举的name匹配
     */
    public static ItemTypeEnum match(String val, ItemTypeEnum def) {
        return Stream.of(values()).parallel().filter(item -> item.getCode().equalsIgnoreCase(val)).findAny().orElse(def);
    }

    public static ItemTypeEnum get(String val) {
        return match(val, null);
    }

}
