package top.mddata.common.enumeration.organization;

import top.mddata.base.interfaces.BaseEnum;
import com.mybatisflex.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 用户来源
 *
 * @author henhen6
 * @since 2021/3/12 21:20
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户来源-枚举")
public enum UserSourceEnum implements BaseEnum<String> {
    /**
     * 平台用户
     */
    PLATFORM("platform", "平台用户");

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
