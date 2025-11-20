package top.mddata.open.admin.enumeration;

import com.mybatisflex.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 应用权限等级
 * [1-公开 2-特殊]
 *
 * @author henhen6
 * @date 2025年11月20日19:53:37
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "应用权限等级-枚举")
public enum ScopeLevelEnum implements BaseEnum<Integer> {

    OPEN(1, "公开"),
    SPECIAL(2, "特殊");
    /**
     * 资源类型
     */
    @EnumValue
    private Integer code;

    /**
     * 资源描述
     */
    private String desc;


}
