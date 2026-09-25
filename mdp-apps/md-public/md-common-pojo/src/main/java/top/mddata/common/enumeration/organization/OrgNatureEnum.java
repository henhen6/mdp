package top.mddata.common.enumeration.organization;

import com.mybatisflex.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 组织性质
 * [1-总公司 90-开发者 99-运营]
 *
 * @author henhen6
 * @since 2021/3/12 21:20
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "组织性质-枚举")
public enum OrgNatureEnum implements BaseEnum<Integer> {
    /**
     * 总公司
     */
    HEAD_COMPANY(1, "总公司"),
    /**
     * 开发者
     */
    DEVELOPER(90, "开发者"),
    /**
     * 运营
     */
    OPERATIONS(99, "运营");

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
