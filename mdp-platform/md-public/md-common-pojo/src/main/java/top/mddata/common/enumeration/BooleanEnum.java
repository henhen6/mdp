package top.mddata.common.enumeration;

import top.mddata.base.interfaces.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是否
 *
 * @author henhen6
 * @since 2021/4/16 11:26 上午
 */
@Getter
@AllArgsConstructor
@Schema(description = "是否-枚举")
public enum BooleanEnum implements BaseEnum<Boolean> {
    /**
     * true
     */
    TRUE(true, 1, "1", "是"),
    /**
     * false
     */
    FALSE(false, 0, "0", "否");
    private final Boolean bool;
    private final int integer;
    private final String str;
    private final String desc;


    @Override
    public Boolean getCode() {
        return this.bool;
    }

    public boolean eq(Integer val) {
        if (val == null) {
            return FALSE.getBool();
        }
        return val.equals(this.getInteger());
    }

    public boolean eq(String val) {
        if (val == null) {
            return FALSE.getBool();
        }
        return val.equals(this.getStr());
    }

    public boolean eq(Boolean val) {
        if (val == null) {
            return FALSE.getBool();
        }
        return val.equals(this.getBool());
    }
}
