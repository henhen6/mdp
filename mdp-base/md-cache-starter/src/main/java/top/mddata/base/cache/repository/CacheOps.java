package top.mddata.base.cache.repository;

import org.springframework.lang.NonNull;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.model.cache.CacheKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 缓存操作公共接口
 *
 * @author henhen6
 * @date 2019/08/07
 */
public interface CacheOps {

    /**
     * 删除指定的key
     *
     * @param keys 多个key
     * @return 删除个数
     */
    Long del(@NonNull CacheKey... keys);

    /**
     * 删除指定的key
     *
     * @param keys 多个key
     * @return 删除个数
     */
    Long del(@NonNull Collection<CacheKey> keys);

    /**
     * 删除指定的key
     *
     * @param keys 多个key
     * @return 删除个数
     */
    Long del(@NonNull String... keys);

    /**
     * 设置带过期时间的缓存，仅当 key 不存在时生效（SETNX 语义，用于抢占与分布式锁）。
     *
     * @param key   缓存键（TTL 由 key.getExpire() 携带；null 时不过期）
     * @param value 值
     * @return true=设置成功（原 key 不存在）；false=key 已存在
     */
    Boolean setIfAbsent(@NonNull CacheKey key, Object value);

    /**
     * 判断指定的key 是否存在
     *
     * @param key key
     * @return 是否存在
     */
    Boolean exists(@NonNull CacheKey key);

    /**
     * 添加到带有 过期时间的  缓存
     *
     * @param key             redis主键
     * @param value           值
     * @param cacheNullValues 是否缓存null对象
     */
    void set(@NonNull CacheKey key, Object value, boolean... cacheNullValues);

    /**
     * 根据key获取对象
     *
     * @param key             redis主键
     * @param cacheNullValues 是否缓存null对象
     * @return 值 不存在时，返回null
     */
    <T> CacheResult<T> get(@NonNull CacheKey key, boolean... cacheNullValues);

    /**
     * 根据key获取对象
     *
     * @param key             redis主键
     * @param cacheNullValues 是否缓存null对象
     * @return 值 不存在时，返回null
     */
    <T> CacheResult<T> get(@NonNull String key, boolean... cacheNullValues);

    /**
     * 根据keys获取对象
     *
     * @param keys redis主键
     * @return 值 不存在时，返回空集合
     */
    <T> List<CacheResult<T>> find(@NonNull Collection<CacheKey> keys);

    /**
     * 根据key获取对象
     * 不存在时，调用function回调获取数据，并set进入，然后返回
     *
     * @param key             redis主键
     * @param loader          加载器
     * @param cacheNullValues 是否缓存null对象
     * @return 值
     */
    <T> CacheResult<T> get(@NonNull CacheKey key, Function<CacheKey, ? extends T> loader, boolean... cacheNullValues);

    /**
     * 清空所有存储的数据
     */
    void flushDb();

    /**
     * 原子计数原语：key 不存在时先以裸字符串 "0" 占位（携带 key.getExpire() 的 TTL），再自增 1；
     * key 已存在且为整数时直接自增；key 已存在但值不是整数时（脏值，如被对象通道的
     * NullVal 占位污染），按 0 次语义重置为 "0" 并设置同样的 TTL 后再自增，完成自愈。
     * <p>
     * 占位与自增必须走字符串通道（Redis 原生 INCR 整数编码）。不能用
     * {@link #setIfAbsent(CacheKey, Object)} 占位：对象序列化器会把 0L 写成 JSON/JDK 字节，
     * 随后 INCR 报 "value is not an integer"，任何序列化器配置下都不兼容。
     * TTL 仅在首次占位或脏值重置时设置，正常自增不续期（固定窗口）。
     *
     * @param key 缓存键（TTL 由 key.getExpire() 携带；为 null 时计数键不过期）
     * @return 返回键 key 在自增之后的值。
     */
    Long initAndIncr(@NonNull CacheKey key);

    /**
     * 为键 key 储存的数字值加上一。
     *
     * @param key 一定不能为 {@literal null}.
     * @return 返回键 key 在执行加一操作之后的值。
     */
    Long incr(@NonNull CacheKey key);

    /**
     * 获取key中存放的Long值
     *
     * @param key    一定不能为 {@literal null}.
     * @param loader 加载
     * @return key中存储的的数字
     */
    Long getCounter(@NonNull CacheKey key, Function<CacheKey, Long> loader);

    /**
     * 为键 key 储存的数字值加上increment。
     *
     * @param key       一定不能为 {@literal null}.
     * @param increment 增量值
     * @return 返回键 key 在执行加一操作之后的值。
     */
    Long incrBy(@NonNull CacheKey key, long increment);

    /**
     * 为键 key 储存的数字值加上一。
     *
     * @param key       一定不能为 {@literal null}.
     * @param increment 增量值
     * @return 返回键 key 在执行加一操作之后的值。
     */
    Double incrByFloat(@NonNull CacheKey key, double increment);

    /**
     * 为键 key 储存的数字值减去一。
     *
     * @param key 一定不能为 {@literal null}.
     * @return 在减去增量 1 之后， 键 key 的值。
     */
    Long decr(@NonNull CacheKey key);

    /**
     * 将 key 所储存的值减去减量 decrement 。
     *
     * @param key       一定不能为 {@literal null}.
     * @param decrement 增量值
     * @return 在减去增量 decrement 之后， 键 key 的值。
     */
    Long decrBy(@NonNull CacheKey key, long decrement);

    /**
     * 批量设置缓存
     *
     * @param map 集合
     * @param cacheNullValues 是否缓存空值
     */
    void mSet(Map<String, ?> map, boolean... cacheNullValues);
}
