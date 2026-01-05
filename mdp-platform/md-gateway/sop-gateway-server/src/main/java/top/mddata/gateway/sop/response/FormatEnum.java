package top.mddata.gateway.sop.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应格式化类型
 * @author 六如
 */
@Getter
@AllArgsConstructor
public enum FormatEnum {
    NONE(""),
    JSON("json"),
    XML("xml");

    private final String value;

    public static FormatEnum of(String value) {
        for (FormatEnum requestFormatEnum : FormatEnum.values()) {
            if (requestFormatEnum.value.equalsIgnoreCase(value)) {
                return requestFormatEnum;
            }
        }
        return NONE;
    }
}
