package top.mddata.open.admin.enumeration;

import top.mddata.base.interfaces.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 来源类型
 * 1-torna,2-自建
 *
 * @author henhen6
 */
@Getter
@AllArgsConstructor
@Schema(description = "来源类型-枚举")
public enum DocSourceTypeEnum implements BaseEnum<Integer> {
    TORNA(1, "Torna"),
    CUSTOM(2, "自建");
    private final Integer code;
    private final String desc;


}
