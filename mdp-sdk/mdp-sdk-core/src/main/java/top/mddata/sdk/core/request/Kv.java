package top.mddata.sdk.core.request;

import java.io.Serializable;
import java.util.Objects;

/**
 * 键值对 通用对象
 *
 * @author henhen6
 */
public class Kv implements Serializable {
    private String key;
    private String value;

    public Kv() {
    }

    public Kv(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Kv kv = (Kv) o;
        return Objects.equals(key, kv.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    public String getKey() {
        return key;
    }

    public Kv setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Kv setValue(String value) {
        this.value = value;
        return this;
    }
}
