package top.mddata.open.admin.enumeration;

import com.mybatisflex.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 应用类型
 * [10-自建应用 20-第三方应用]
 *
 * @author henhen6
 * @date 2025年11月20日19:53:37
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "应用类型-枚举")
public enum AppTypeEnum implements BaseEnum<String> {

    SELF_BUILT("10", "自建应用"),
    THIRD_PARTY("20", "第三方应用");
    /**
     * 资源类型
     */
    @EnumValue
    private String code;

    /**
     * 资源描述
     */
    private String desc;


}
