package top.mddata.workbench.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

import java.util.Arrays;

/**
 * 双因素认证全局策略
 *
 * @author tangyh
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "TwoFactorStrategyEnum", description = "双因素认证全局策略")
public enum TwoFactorStrategyEnum implements BaseEnum<String> {
    OFF("OFF", "关闭"),
    OPTIONAL("OPTIONAL", "允许用户自选"),
    FORCE("FORCE", "强制全员");

    @Schema(description = "编码")
    private String code;
    @Schema(description = "描述")
    private String desc;

    public static TwoFactorStrategyEnum of(String code) {
        return Arrays.stream(values()).filter(item -> item.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(OFF);
    }
}
