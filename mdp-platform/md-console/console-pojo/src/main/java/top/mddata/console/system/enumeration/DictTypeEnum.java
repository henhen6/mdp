package top.mddata.console.system.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

import java.util.stream.Stream;

/**
 * 字典类型
 * [10-系统字典 20-枚举字典 30-业务字典]
 * @author henhen6
 * @since 2025/9/24 00:03
 */
@Getter
@RequiredArgsConstructor
@Schema(title = "DictTypeEnum", description = "字典类型-枚举")
public enum DictTypeEnum implements BaseEnum<String> {
    SYSTEM("10", "系统字典"),
    ENUM("20", "枚举字典"),
    BUSINESS("30", "业务字典");
    private final String code;
    private final String desc;

    /**
     * 根据当前枚举的name匹配
     */
    public static DictTypeEnum match(String val, DictTypeEnum def) {
        return Stream.of(values()).parallel().filter(item -> item.getCode().equalsIgnoreCase(val)).findAny().orElse(def);
    }

    public static DictTypeEnum get(String val) {
        return match(val, null);
    }

}
