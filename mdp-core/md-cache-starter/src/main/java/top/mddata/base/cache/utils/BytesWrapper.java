package top.mddata.base.cache.utils;

import lombok.Getter;
import lombok.Setter;

/**
 * 字节包装器
 * @param <T> 实际值
 * @author henhen6
 */
@Setter
@Getter
public class BytesWrapper<T> implements Cloneable {
    private T value;

    public BytesWrapper() {
    }

    public BytesWrapper(T value) {
        this.value = value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BytesWrapper<T> clone() {
        try {
            return (BytesWrapper<T>) super.clone();
        } catch (CloneNotSupportedException e) {
            return new BytesWrapper<>();
        }
    }
}
