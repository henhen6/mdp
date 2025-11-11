package top.mddata.base.mvcflex.service.impl;

import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.base.utils.CollHelper;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import com.mybatisflex.core.util.UpdateEntity;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * 若子类 cacheKeyBuilder() 方法返回null，则该类中的所有方法均直接操作数据库。
 *
 * 若子类 cacheKeyBuilder() 方法返回非null，则该类中的大部分方法会操作缓存。
 * 默认的key规则： #{CacheKeyBuilder#key()}:id
 *
 * @param <M>      Manager
 * @param <Entity> 实体
 * @author henhen6
 * @since 2020年02月27日18:15:17
 */
@Slf4j
public abstract class SuperServiceImpl<M extends BaseMapper<Entity>, Entity extends BaseEntity<?>>
        extends ServiceImpl<M, Entity> implements SuperService<Entity> {
    protected static final int MAX_BATCH_KEY_SIZE = 500;
    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    protected CacheOps cacheOps;

    /**
     * 缓存key 构造器
     *
     * @return 缓存key构造器
     */
    protected CacheKeyBuilder cacheKeyBuilder() {
        return null;
    }


    @Override
    public Class<Entity> getEntityClass() {
        return (Class<Entity>) TypeUtil.getTypeArgument(this.getClass(), 1);
    }


    /**
     * 保存之前处理参数等操作
     *
     * @param save 保存参数
     */
    protected Entity saveBefore(Object save) {
        Entity entity = BeanUtil.toBean(save, getEntityClass());
        entity.setId(null);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Entity saveDto(Object save) {
        Entity entity = saveBefore(save);
        save(entity);
        saveAfter(save, entity);
        return entity;
    }

    /**
     * 保存之后设置参数值，淘汰缓存等操作
     *
     * @param save 保存参数
     * @param entity 实体
     */
    protected void saveAfter(Object save, Entity entity) {
    }

    /**
     * 修改之前处理参数等操作
     *
     * @param updateDto 修改对象
     */
    protected Entity updateBefore(Object updateDto) {
        // 这个方法可以让你在调用updateById时，将手动设置过的所有字段，就修改到数据库。 没有手动调用过set的字段，不会更新
        Entity entity = UpdateEntity.of(getEntityClass());
        BeanUtil.copyProperties(updateDto, entity);
        return entity;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Entity updateDtoById(Object updateDto) {
        Entity entity = updateBefore(updateDto);
        updateById(entity);
        updateAfter(updateDto, entity);
        return entity;
    }

    /**
     * 修改之后设置参数值，淘汰缓存等操作
     *
     * @param updateDto 修改对象
     * @param entity   实体
     */
    protected void updateAfter(Object updateDto, Entity entity) {
    }

    @Override
    @Transactional(readOnly = true)
    public Entity getByIdCache(Serializable id) {
        if (id == null) {
            return null;
        }
        CacheKeyBuilder cacheKeyBuilder = cacheKeyBuilder();
        if (cacheKeyBuilder == null) {
            return super.getById(id);
        }
        CacheKey cacheKey = cacheKeyBuilder.key(id);
        CacheResult<Entity> result = cacheOps.get(cacheKey, k -> super.getById(id));
        return result.getValue();
    }


    // ===== 保存（增）操作 =====
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Entity model) {
        boolean save = super.save(model);
        setCache(model);
        return save;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(Collection<Entity> list, int batchSize) {
        boolean flag = super.saveBatch(list, batchSize);
        CacheKeyBuilder cacheKeyBuilder = cacheKeyBuilder();
        if (cacheKeyBuilder != null) {
            Map<String, Entity> map = new HashMap<>(list.size());
            for (Entity entity : list) {
                CacheKey key = cacheKeyBuilder.key(entity.getId());
                map.put(key.getKey(), entity);
            }
            cacheOps.mSet(map);
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdate(Entity entity) {
        TableInfo tableInfo = TableInfoFactory.ofEntityClass(entity.getClass());
        Object[] pkArgs = tableInfo.buildPkSqlArgs(entity);
        if (pkArgs.length == 0 || pkArgs[0] == null) {
            return save(entity);
        } else {
            return updateById(entity);
        }
    }

    // ===== 删除（删）操作 =====

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        boolean bool = super.removeById(id);
        delCache(id);
        return bool;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Entity entity) {
        boolean bool = super.removeById(entity);
        delCache(entity);
        return bool;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(QueryWrapper query) {
        CacheKeyBuilder builder = cacheKeyBuilder();
        if (builder == null) {
            return super.remove(query);
        }

        List<Entity> list = getMapper().selectListByQuery(query);
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        boolean flag = super.remove(query);
        delCache(list);
        return flag;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(QueryCondition condition) {
        CacheKeyBuilder builder = cacheKeyBuilder();
        if (builder == null) {
            return super.remove(condition);
        }
        List<Entity> list = getMapper().selectListByCondition(condition);
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        boolean flag = super.remove(condition);
        delCache(list);
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByMap(Map<String, Object> query) {
        CacheKeyBuilder builder = cacheKeyBuilder();
        if (builder == null) {
            return super.removeByMap(query);
        }
        List<Entity> list = getMapper().selectListByMap(query);
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        boolean flag = super.removeByMap(query);
        delCache(list);
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        if (CollUtil.isEmpty(idList)) {
            return true;
        }
        boolean flag = super.removeByIds(idList);

        delCache(idList);
        return flag;
    }


    // ===== 更新（改）操作 =====

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(Entity model) {
        boolean updateBool = super.updateById(model);
        delCache(model);
        return updateBool;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Entity entity, Map<String, Object> query) {
        CacheKeyBuilder builder = cacheKeyBuilder();
        if (builder == null) {
            return super.update(entity, query);
        }
        List<Entity> list = getMapper().selectListByMap(query);
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        boolean bool = super.update(entity, query);
        delCache(list);
        return bool;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Entity entity, QueryWrapper query) {
        CacheKeyBuilder builder = cacheKeyBuilder();
        if (builder == null) {
            return super.update(entity, query);
        }
        List<Entity> list = getMapper().selectListByQuery(query);
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        boolean bool = super.update(entity, query);
        delCache(list);
        return bool;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Entity entity, QueryCondition condition) {
        CacheKeyBuilder builder = cacheKeyBuilder();
        if (builder == null) {
            return super.update(entity, condition);
        }
        List<Entity> list = getMapper().selectListByCondition(condition);
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        boolean bool = super.update(entity, condition);
        delCache(list);
        return bool;
    }


    private List<CacheResult<Entity>> find(List<CacheKey> keys) {
        return cacheOps.find(keys);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Entity> findByIds(@NonNull Collection<? extends Serializable> ids, Function<Collection<? extends Serializable>, Collection<Entity>> loader) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        CacheKeyBuilder builder = cacheKeyBuilder();
        if (builder == null) {
            if (loader == null) {
                loader = missIds -> super.listByIds(missIds.stream().filter(Objects::nonNull).map(Convert::toLong).toList());
            }
            /*
             * 从数据库加载数据
             *
             * 数据库中确实不存在某条数据时， missedIds 和 missList的数量可能不一致
             */
            Collection<Entity> missList = loader.apply(ids);

            return Lists.newArrayList(missList);
        }

        // 拼接keys
        List<CacheKey> keys = ids.stream().map(builder::key).toList();
        // 切割
        List<List<CacheKey>> partitionKeys = Lists.partition(keys, MAX_BATCH_KEY_SIZE);

        // 用切割后的 partitionKeys 分批去缓存查， 返回的是缓存中存在的数据
        List<CacheResult<Entity>> valueList = partitionKeys.stream().map(this::find).flatMap(Collection::stream).toList();

        // 所有的key
        List<Serializable> keysList = Lists.newArrayList(ids);
        log.debug(StrUtil.format("keySize={}, valueSize={}", keysList.size(), valueList.size()));

        // 缓存中不存在的key
        Set<Serializable> missedIds = Sets.newLinkedHashSet();

        Map<Serializable, Entity> allMap = new LinkedHashMap<>();
        for (int i = 0; i < valueList.size(); i++) {
            CacheResult<Entity> v = valueList.get(i);
            Serializable k = keysList.get(i);
            if (v == null || v.isNull()) {
                missedIds.add(k);
                // null 占位
                allMap.put(k, null);
            } else {
                allMap.put(k, v.getValue());
            }
        }

        // 加载miss 的数据，并设置到缓存
        if (CollUtil.isNotEmpty(missedIds)) {
            if (loader == null) {
                loader = missIds -> super.listByIds(missIds.stream().filter(Objects::nonNull).map(Convert::toLong).toList());
            }
            /*
             * 从数据库加载数据
             *
             * 数据库中确实不存在某条数据时， missedIds 和 missList的数量可能不一致
             */
            Collection<Entity> missList = loader.apply(missedIds);

            ImmutableMap<Serializable, Entity> missMap = CollHelper.uniqueIndex(missList, item -> (Serializable) item.getId(), item -> item);

            // 将数据库中查询出来的数据
            allMap.forEach((k, v) -> {
                if (missMap.containsKey(k)) {
                    allMap.put(k, missMap.get(k));
                }
            });

            // 将缓存中不存在的数据，缓存 "空值" 或 "实际值"
            for (Serializable missedKey : missedIds) {
                CacheKey key = builder.key(missedKey);
                // 存在就缓存 实际值
                // 不存在就缓存 空值，防止缓存击穿
                cacheOps.set(key, missMap.getOrDefault(missedKey, null));
            }
        }

        Collection<Entity> values = allMap.values();
        return Lists.newArrayList(values);
    }

    @Override
    @Transactional(readOnly = true)
    public Entity getByKey(CacheKey key, Function<CacheKey, Object> loader) {
        Object id = cacheOps.get(key, loader);
        return id == null ? null : getByIdCache(Convert.toLong(id));
    }

    @Override
    public void refreshCache(List<Serializable> ids) {
        CacheKeyBuilder builder = cacheKeyBuilder();
        if (builder == null) {
            return;
        }

        QueryWrapper wrap = QueryWrapper.create().from(getEntityClass());
        if (CollUtil.isNotEmpty(ids)) {
            wrap.in(Entity::getId, ids);
        }
        list(wrap).forEach(this::setCache);
    }

    @Override
    public void clearCache(List<Serializable> ids) {
        CacheKeyBuilder builder = cacheKeyBuilder();
        if (builder == null) {
            return;
        }
        QueryWrapper wrap = QueryWrapper.create().from(getEntityClass());
        if (CollUtil.isNotEmpty(ids)) {
            wrap.in(Entity::getId, ids);
        }
        List<Entity> list = list(wrap);
        List<CacheKey> cacheKeys = list.stream().map(Entity::getId).map(builder::key).toList();
        cacheOps.del(cacheKeys);
    }

    @Override
    public <E> Set<E> findCollectByIds(List<? extends Serializable> keyIdList, Function<Serializable, CacheKey> cacheBuilder, Function<Serializable, List<E>> loader) {
        if (CollUtil.isEmpty(keyIdList)) {
            return Collections.emptySet();
        }
        List<CacheKey> cacheKeys = keyIdList.stream().map(cacheBuilder).toList();
        // 通过 mGet 方法批量查询缓存
        List<CacheResult<List<E>>> resultList = cacheOps.find(cacheKeys);

        if (resultList.size() != cacheKeys.size()) {
            log.warn("key和结果数量不一致，请排查原因!");
        }
        /*
         * 有可能缓存中不存在某些缓存，导致resultList中的部分元素是null
         */
        Set<E> resultIdSet = new HashSet<>();
        for (int i = 0; i < resultList.size(); i++) {
            CacheResult<List<E>> result = resultList.get(i);
            List<E> resultIdList = result.asList();
            if (result.isNull()) {
                Serializable keyId = keyIdList.get(i);

                List<E> idList = loader.apply(keyId);
                CacheKey cacheKey = cacheKeys.get(i);
                cacheOps.set(cacheKey, idList);
                resultIdList = idList;
            }
            resultIdSet.addAll(resultIdList);
        }
        return resultIdSet;
    }


    @Override
    public void delCache(Collection<? extends Serializable> idList) {
        CacheKeyBuilder builder = cacheKeyBuilder();
        if (builder != null) {
            CacheKey[] keys = idList.stream().map(builder::key).toArray(CacheKey[]::new);
            cacheOps.del(keys);
        }
    }

    @Override
    public void delCache(Entity model) {
        CacheKeyBuilder builder = cacheKeyBuilder();
        if (builder != null) {
            CacheKey key = builder.key(model.getId());
            cacheOps.del(key);
        }
    }

    private void delCache(List<Entity> list) {
        CacheKeyBuilder builder = cacheKeyBuilder();
        if (builder != null) {
            CacheKey[] keys = list.stream().map(item -> builder.key(item.getId())).toArray(CacheKey[]::new);
            cacheOps.del(keys);
        }
    }

    @Override
    public void setCache(Entity model) {
        CacheKeyBuilder cacheKeyBuilder = cacheKeyBuilder();
        if (cacheKeyBuilder != null) {
            Object id = model.getId();
            if (id != null) {
                CacheKey key = cacheKeyBuilder().key(id);
                cacheOps.set(key, model);
            }
        }
    }
}
