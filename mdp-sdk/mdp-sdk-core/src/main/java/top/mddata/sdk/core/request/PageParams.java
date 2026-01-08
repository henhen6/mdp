package top.mddata.sdk.core.request;

import java.util.HashMap;
import java.util.Map;

public class PageParams<T> {

    private T model;

    private long size = 10;

    private long current = 1;

    private String sort;

    private String order;

    private Map<String, Object> extra = new HashMap<>();

    public PageParams() {
    }

    public PageParams(long current, long size) {
        this.size = size;
        this.current = current;
    }


    /**
     * 计算当前分页偏移量
     */
    public long offset() {
        long curr = this.current;
        if (curr <= 1L) {
            return 0L;
        }
        return (curr - 1) * this.size;
    }

    public PageParams<T> put(String key, Object value) {
        if (this.extra == null) {
            this.extra = new HashMap<>(16);
        }
        this.extra.put(key, value);
        return this;
    }

    public PageParams<T> putAll(Map<String, Object> extra) {
        if (this.extra == null) {
            this.extra = new HashMap<>(16);
        }
        this.extra.putAll(extra);
        return this;
    }

    public T getModel() {
        return model;
    }

    public PageParams<T> setModel(T model) {
        this.model = model;
        return this;
    }

    public long getSize() {
        return size;
    }

    public PageParams<T> setSize(long size) {
        this.size = size;
        return this;
    }

    public long getCurrent() {
        return current;
    }

    public PageParams<T> setCurrent(long current) {
        this.current = current;
        return this;
    }

    public String getSort() {
        return sort;
    }

    public PageParams<T> setSort(String sort) {
        this.sort = sort;
        return this;
    }

    public String getOrder() {
        return order;
    }

    public PageParams<T> setOrder(String order) {
        this.order = order;
        return this;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public PageParams<T> setExtra(Map<String, Object> extra) {
        this.extra = extra;
        return this;
    }
}
