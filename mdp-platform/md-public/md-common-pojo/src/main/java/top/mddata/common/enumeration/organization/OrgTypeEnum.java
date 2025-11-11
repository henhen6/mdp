package top.mddata.common.enumeration.organization;

import top.mddata.base.interfaces.BaseEnum;
import com.mybatisflex.annotation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 组织类型 ;[10-公司  20-部门]
 *
 * @author henhen6
 * @since 2021/3/12 21:20
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "组织类型-枚举")
public enum OrgTypeEnum implements BaseEnum<String> {
    /**
     * 公司
     */
    COMPANY("10", "公司"),
    /**
     * 部门
     */
    DEPT("20", "部门");

    /**
     * 资源类型
     */
    @EnumValue
    private String code;

    /**
     * 资源描述
     */
    private String desc;

    @Schema(description = "编码", allowableValues = "10,20", example = "10")
    public String getCode() {
        return this.code;
    }

    public boolean eq(String val) {
        return this.getCode().equalsIgnoreCase(val);
    }

}
