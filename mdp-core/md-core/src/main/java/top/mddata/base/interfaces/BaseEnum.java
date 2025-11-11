package top.mddata.base.interfaces;


import java.io.Serializable;

/**
 * 枚举类型基类
 *
 * @author henhen6
 * @param <T> 唯一标识类型
 * @date 2019/07/26
 */
public interface BaseEnum<T extends Serializable> {

    /**
     * 唯一标识
     *
     * @return 唯一标识
     */
    T getCode();

    /**
     * 中文名称
     *
     * @return 中文名称
     */
    String getDesc();

    /**
     * 判断val是否跟当前枚举相等
     *
     * @param val 值
     * @return 是否等于
     */
    default boolean eq(T val) {
        return this.getCode().equals(val);
    }

    /**
     * 判断val是否跟当前枚举相等
     * @param val 值
     * @return 是否等于
     */
    default boolean eq(BaseEnum<T> val) {
        return val != null && this.getCode().equals(val.getCode());
    }

}
