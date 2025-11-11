package top.mddata.common.enumeration.organization;

import top.mddata.base.interfaces.BaseEnum;
import com.mybatisflex.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 人员类型
 *
 * @author henhen6
 * @since 2021/3/12 21:20
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "人员类型-枚举")
public enum UserTypeEnum implements BaseEnum<Integer> {
    /**
     * 普通用户
     */
    USER(1, "普通用户"),
    /**
     * 管理员
     */
    ADMIN(2, "管理员"),
    /**
     * 运维管理员
     */
    OPERATIONS_ADMIN(99, "运维管理员");

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
