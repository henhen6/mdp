package top.mddata.common.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

import java.util.stream.Stream;

/**
 * <p>
 * 实体注释中生成的类型枚举
 * 用户
 * </p>
 *
 * @author henhen6
 * @since 2021-10-09
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Sex", description = "性别-枚举")
public enum Sex implements BaseEnum<String> {

    /**
     * M="男"
     */
    M("0", "男"),
    /**
     * W="女"
     */
    W("1", "女");

    @Schema(description = "编码")
    private String code;
    @Schema(description = "描述")
    private String desc;


    /**
     * 根据当前枚举的name匹配
     */
    public static Sex match(String val, Sex def) {
        return Stream.of(values()).parallel().filter(item -> item.name().equalsIgnoreCase(val)).findAny().orElse(def);
    }

    public static Sex get(String val) {
        return match(val, null);
    }

    public boolean eq(Sex val) {
        return val != null && eq(val.name());
    }

}
