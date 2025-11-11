package top.mddata.common.enumeration;

import top.mddata.base.interfaces.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态
 *
 * @author henhen6
 * @since 2021/4/16 11:26 上午
 */
@Getter
@AllArgsConstructor
@Schema(title = "StateEnum", description = "状态-枚举")
public enum StateEnum implements BaseEnum<Boolean> {
    /**
     * 启用
     */
    ENABLE(true, 1, "1", "启用"),
    /**
     * 禁用
     */
    DISABLE(false, 0, "0", "禁用");
    private final Boolean bool;
    private final int integer;
    private final String str;
    private final String desc;

    public static StateEnum match(String val, StateEnum... defs) {
        StateEnum def = defs.length > 0 ? defs[0] : DISABLE;
        if (val == null) {
            return def;
        }

        for (StateEnum value : StateEnum.values()) {
            if (value.getStr().equals(val)) {
                return value;
            }
        }
        return def;
    }

    @Override
    public Boolean getCode() {
        return this.bool;
    }

    public boolean eq(Integer val) {
        if (val == null) {
            return DISABLE.getBool();
        }
        return val.equals(this.getInteger());
    }

    public boolean eq(String val) {
        if (val == null) {
            return DISABLE.getBool();
        }
        return val.equals(this.getStr());
    }

    public boolean eq(Boolean val) {
        if (val == null) {
            return DISABLE.getBool();
        }
        return val.equals(this.getBool());
    }
}
