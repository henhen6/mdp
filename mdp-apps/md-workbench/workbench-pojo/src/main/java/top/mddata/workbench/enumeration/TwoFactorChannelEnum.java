package top.mddata.workbench.enumeration;

import com.mybatisflex.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.exception.BizException;
import top.mddata.base.interfaces.BaseEnum;

import java.util.Arrays;

/**
 * 双因素认证验证渠道
 *
 * @author tangyh
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "TwoFactorChannelEnum", description = "双因素认证验证渠道")
public enum TwoFactorChannelEnum implements BaseEnum<String> {
    PHONE("PHONE", "仅手机"),
    EMAIL("EMAIL", "仅邮箱"),
    BOTH("BOTH", "手机或邮箱均可");

    @Schema(description = "编码")
    @EnumValue
    private String code;
    @Schema(description = "描述")
    private String desc;

    public static TwoFactorChannelEnum fromCode(String code) {
        return Arrays.stream(values()).filter(item -> item.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new BizException("验证渠道不合法"));
    }
}
