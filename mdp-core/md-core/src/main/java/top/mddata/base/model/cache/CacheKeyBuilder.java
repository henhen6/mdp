package top.mddata.base.model.cache;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import top.mddata.base.base.entity.SuperEntity;

import java.time.Duration;
import java.util.ArrayList;


/**
 * cache key
 * <p>
 * key命名风格
 * 【推荐】 Redis key命名需具有可读性以及可管理性，不该使用含义不清的key以及特别长的key名；
 * 【强制】以英文字母开头，命名中只能出现小写字母、数字、英文点号(.)和英文半角冒号(:)；
 * 【强制】不要包含特殊字符，如下划线、空格、换行、单双引号以及其他转义字符；
 * <p>
 * 命名规范
 * 【强制】命名规范：[前缀:][租户ID:]表名[:字段名][:唯一键值]
 * 0）前缀： 可选。 用来区分不同项目，不同环境。 如：区分fs-cloud 或 fs-boot、fs-cloud的dev或test环境
 * 1）租户ID： 可选。 用来区分不同租户数据缓存。 如： 内置租户和阿里巴巴租户分别用0000 和 1111 来区分。
 * 3) 表名： 必填。 用来区分不同业务类型的数据缓存。 通常设置为表名。 同一key有多个业务类型时， 使用英文半角点号 (.)分割，用来表示一个完整的语义。 如user.activity 表示存储用户和活动、 user表示存储用户如：用户、应用表分别用 user、application来区分。
 * 4) 字段名: 可选。用来区分业务值是那个字段。 通常设置为字段名。 同一key有多个业务类型时，业务字段对应多个业务类型。使用英文半角点号 (.)分割，用来表示一个完整的语义。 如业务类型为 user.activity 表示存储用户和活动， 则应该用id.id 跟user.activity 对应，表示key中包user的id 和 activity的id
 * 6) 唯一键值 ：可选。 用来区分同一业务类型的不同行的数据缓存。如：用户id为1的数据和id为2的，分别是指该值为1和2。
 * <p>
 *
 * @author henhen6
 * @since 2024年08月23日16:51:06
 *
 */
@FunctionalInterface
public interface CacheKeyBuilder {

    /**
     * 缓存前缀，用于区分项目，环境等等
     *
     * @return 缓存前缀
     */
    default String getPrefix() {
        return null;
    }

    /**
     * key的业务类型， 用于区分表
     *
     * @return 通常是表名
     */
    String getTable();

    /**
     * key的字段名， 用于区分字段
     *
     * @return 通常是key的字段名
     */
    default String getField() {
        return SuperEntity.ID_FIELD;
    }


    /**
     * 缓存自动过期时间
     *
     * @return 缓存自动过期时间
     */
    @Nullable
    default Duration getExpire() {
        return null;
    }

    /**
     * 获取通配符
     *
     * @return key 前缀
     */
    default String getPattern() {
        return StrUtil.format("*:{}:*", getTable());
    }

    /**
     * 构建通用KV模式 的 cache key
     * 兼容 redis caffeine
     *
     * @param uniques 参数
     * @return cache key
     */
    default CacheKey key(Object... uniques) {
        String key = getKey(uniques);
        Assert.notEmpty(key, "key 不能为空");
        return new CacheKey(key, getExpire());
    }

    /**
     * 构建 redis 类型的 hash cache key
     *
     * @param field   field
     * @param uniques 动态参数
     * @return cache key
     */
    default CacheHashKey hashFieldKey(@NonNull Object field, Object... uniques) {
        String key = getKey(uniques);

        Assert.notEmpty(key, "key 不能为空");
        Assert.notNull(field, "field 不能为空");
        return new CacheHashKey(key, field, getExpire());
    }

    /**
     * 构建 redis 类型的 hash cache key （无field)
     *
     * @param uniques 动态参数
     * @return cache key
     */
    default CacheHashKey hashKey(Object... uniques) {
        String key = getKey(uniques);

        Assert.notEmpty(key, "key 不能为空");
        return new CacheHashKey(key, null, getExpire());
    }

    /**
     * 根据动态参数 拼接key
     * <p>
     * key命名规范：[前缀:][租户ID:][服务模块名:]业务类型[:业务字段][:value类型][:业务值]
     *
     * @param uniques 动态参数
     * @return 字符串型的缓存的key
     */
    private String getKey(Object... uniques) {
        ArrayList<String> regionList = new ArrayList<>();
        String prefix = this.getPrefix();
        if (StrUtil.isNotEmpty(prefix)) {
            regionList.add(prefix);
        }

        // 业务类型
        String table = this.getTable();
        Assert.notEmpty(table, "缓存业务类型不能为空");
        regionList.add(table);
        // 业务字段
        String field = getField();
        if (StrUtil.isNotEmpty(field)) {
            regionList.add(field);
        }

        // 业务值
        for (Object unique : uniques) {
            if (ObjectUtil.isNotEmpty(unique)) {
                regionList.add(String.valueOf(unique));
            }
        }
        return CollUtil.join(regionList, StrPool.COLON);
    }

}
