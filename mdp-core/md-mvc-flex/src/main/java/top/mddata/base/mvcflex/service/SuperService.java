package top.mddata.base.mvcflex.service;

import com.mybatisflex.core.service.IService;
import org.springframework.lang.NonNull;
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.model.cache.CacheKey;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * 业务层
 *
 * @param <Entity> 实体
 * @author henhen6
 * @since 2020年03月03日20:49:03
 */
public interface SuperService<Entity extends BaseEntity<?>> extends IService<Entity> {

    /**
     * 获取实体的类型
     *
     * @return 实体类class类型
     */
    Class<Entity> getEntityClass();

    /**
     * 插入一条记录（选择字段，策略插入）
     *
     * @param dto 保存对象
     * @return 是否插入成功
     */
    Entity saveDto(Object dto);


    /**
     * 根据 ID 修改实体中非空的字段
     *
     * @param dto 修改对象
     * @return 是否修改成功
     */
    Entity updateDtoById(Object dto);


    /**
     * 根据id 先查缓存，再查db
     *
     * @param id 主键
     * @return 对象
     */
    Entity getByIdCache(Serializable id);

    /**
     * 根据 key 查询缓存中存放的id，缓存不存在根据loader加载并写入数据，然后根据查询出来的id查询 实体
     *
     * @param key    缓存key
     * @param loader 加载器
     * @return 对象
     */
    Entity getByKey(CacheKey key, Function<CacheKey, Object> loader);

    /**
     * 根据id 批量查询缓存，并将loader方法的返回值写入缓存
     *
     * @param ids    主键id
     * @param loader 缓存中不存在的数据，通过此回调去数据库查询
     * @return 对象集合
     */
    List<Entity> findByIds(@NonNull Collection<? extends Serializable> ids, Function<Collection<? extends Serializable>, Collection<Entity>> loader);

    /**
     * 根据id 批量查询缓存
     *
     * @param keyIdList    id集合
     * @param cacheBuilder 缓存key构造器
     * @param loader       加载数据的回调方法
     * @param <E>          查询的对象
     * @return
     */
    <E> Set<E> findCollectByIds(List<? extends Serializable> keyIdList, Function<Serializable, CacheKey> cacheBuilder, Function<Serializable, List<E>> loader);

    /**
     * 刷新缓存
     *
     * @param ids 主键
     */
    void refreshCache(List<Serializable> ids);

    /**
     * 清理缓存
     *
     * @param ids 主键
     */
    void clearCache(List<Serializable> ids);


    /**
     * 清理缓存
     *
     * @param ids
     */
    default void delCache(Serializable... ids) {
        delCache(Arrays.asList(ids));
    }

    /**
     * 清理缓存
     *
     * @param idList
     */
    void delCache(Collection<? extends Serializable> idList);

    /**
     * 清理缓存
     *
     * @param model
     */
    void delCache(Entity model);

    /**
     * 设置缓存
     *
     * @param model
     */
    void setCache(Entity model);
}
