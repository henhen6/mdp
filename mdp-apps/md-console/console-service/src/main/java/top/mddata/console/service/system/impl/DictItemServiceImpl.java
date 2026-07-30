package top.mddata.console.service.system.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CachePlusOps;
import top.mddata.base.echo.properties.EchoProperties;
import top.mddata.base.exception.BizException;
import top.mddata.base.model.cache.CacheHashKey;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.CollHelper;
import top.mddata.base.utils.MyTreeUtil;
import top.mddata.common.cache.console.system.DictItemHashCacheKeyBuilder;
import top.mddata.common.constant.DefValConstants;
import top.mddata.console.entity.system.Dict;
import top.mddata.console.entity.system.DictItem;
import top.mddata.console.enumeration.system.DictTypeEnum;
import top.mddata.console.mapper.system.DictItemMapper;
import top.mddata.console.mapper.system.DictMapper;
import top.mddata.console.service.system.DictItemService;
import top.mddata.console.vo.system.DictItemVo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * 字典项 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DictItemServiceImpl extends SuperServiceImpl<DictItemMapper, DictItem> implements DictItemService {
    private final DictMapper dictMapper;
    private final CachePlusOps cachePlusOps;
    private final UidGenerator uidGenerator;
    private final EchoProperties ips;

    private static void execI18n(Map<Serializable, DictItem> defMap, String locale, Map<Serializable, Object> map) {
        defMap.forEach((key, value) -> {
            String name = value.getName();
            if (StrUtil.isNotEmpty(locale)) {
                String i18nJson = value.getI18nJson();
                try {
                    JSONObject i18n = JSONUtil.parseObj(i18nJson);
                    String i18nValue = i18n.getStr(locale);
                    if (StrUtil.isNotEmpty(i18nValue)) {
                        name = i18nValue;
                    }
                } catch (Exception e) {
                    log.debug("字典翻译失败: {}=={} ", value.getUniqKey(), value.getName());
                }
            }
            map.put(key, name);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Serializable, Object> findByIds(Set<Serializable> dictKeys) {
        if (dictKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Serializable, DictItem> codeValueMap = MapUtil.newHashMap();
        dictKeys.forEach(dictKey -> {
            Function<CacheKey, Map<String, DictItem>> fun = ck -> {
                Dict dict = dictMapper.selectOneByQuery(QueryWrapper.create().eq(Dict::getUniqKey, dictKey));
                if (dict == null) {
                    return Collections.emptyMap();
                }

                Long dictId = dict.getId();
                List<DictItem> list = this.list(QueryWrapper.create().eq(DictItem::getDictId, dictId));

                if (CollUtil.isNotEmpty(list)) {
                    return CollHelper.uniqueIndex(list, DictItem::getUniqKey, item -> item);
                } else {
                    return MapBuilder.<String, DictItem>create().put(DefValConstants.DICT_NULL_VAL_KEY, new DictItem()).build();
                }
            };
            Map<String, CacheResult<DictItem>> map = cachePlusOps.hGetAll(DictItemHashCacheKeyBuilder.builder(dictKey), fun);
            map.forEach((itemKey, itemName) -> {
                if (!DefValConstants.DICT_NULL_VAL_KEY.equals(itemKey)) {
                    codeValueMap.put(StrUtil.join(ips.getDictSeparator(), dictKey, itemKey), itemName.getValue());
                }
            });
        });

        Map<Serializable, Object> echoMap = MapUtil.newHashMap();
        String locale = ContextUtil.getLocale();
        execI18n(codeValueMap, locale, echoMap);

        return echoMap;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<DictItemVo>> findDictItemByUniqKey(List<String> uniqKeyList) {
        if (CollUtil.isEmpty(uniqKeyList)) {
            return Collections.emptyMap();
        }
        Iterable<QueryColumn> queryColumns = QueryMethods.defaultColumns(DictItem.class);
        // 关联查询
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(QueryMethods.column(Dict::getUniqKey).as(DictItem::getDictKey))
                .select(queryColumns)
                .from(DictItem.class).as("item")
                .leftJoin(Dict.class).on(Dict::getId, DictItem::getDictId)
                .in(Dict::getUniqKey, uniqKeyList)
                .orderBy(DictItem::getWeight, true);

        List<DictItem> list = super.list(queryWrapper);
        List<DictItemVo> voList = BeanUtil.copyToList(list, DictItemVo.class);

        //key 是类型
        return voList.stream().collect(groupingBy(DictItemVo::getDictKey, LinkedHashMap::new, toList()));
    }


    @Override
    @Transactional(readOnly = true)
    public Map<String, DictItem> getDictItemByUniqKey(String uniqKey) {
        if (StrUtil.isEmpty(uniqKey)) {
            return Collections.emptyMap();
        }
        Iterable<QueryColumn> queryColumns = QueryMethods.defaultColumns(DictItem.class);
        // 关联查询
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(QueryMethods.column(Dict::getUniqKey).as(DictItem::getDictKey))
                .select(queryColumns)
                .from(DictItem.class).as("item")
                .leftJoin(Dict.class).on(Dict::getId, DictItem::getDictId)
                .eq(Dict::getUniqKey, uniqKey)
                .orderBy(DictItem::getWeight, true);

        List<DictItem> list = super.list(queryWrapper);

        return CollHelper.buildMap(list, DictItem::getUniqKey, item -> item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByDictIds(Collection<? extends Serializable> idList) {
        return remove(QueryWrapper.create().in(DictItem::getDictId, idList));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkItemByKey(String key, Long dictId, Long id) {
        ArgumentAssert.notEmpty(key, "请填写字典项标识");
        ArgumentAssert.notNull(dictId, "字典不能为空");
        return count(QueryWrapper.create().eq(DictItem::getUniqKey, key)
                .eq(DictItem::getDictId, dictId).ne(DictItem::getId, id)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictItem saveDto(Object save) {
        DictItem dictItem = super.saveBefore(save);
        dictItem.setId(uidGenerator.getUid());
        ArgumentAssert.isFalse(checkItemByKey(dictItem.getUniqKey(), dictItem.getDictId(), null), "字典项[{}]已经存在，请勿重复创建", dictItem.getUniqKey());
        Dict dict = dictMapper.selectOneById(dictItem.getDictId());
        ArgumentAssert.notNull(dict, "字典不存在");

        dictItem.setDataType(dict.getDataType());
        dictItem.setDictType(DictTypeEnum.BUSINESS.getCode());
        if (dictItem.getParentId() != null) {
            DictItem parent = getById(dictItem.getParentId());
            ArgumentAssert.notNull(parent, "父字典项不存在");
            dictItem.setTreePath(MyTreeUtil.buildTreePath(parent.getTreePath(), dictItem.getId()));
        } else {
            dictItem.setTreePath(MyTreeUtil.buildTreePath(dictItem.getId()));
        }

        save(dictItem);

        CacheHashKey hashKey = DictItemHashCacheKeyBuilder.builder(dict.getUniqKey(), dictItem.getUniqKey());
        cachePlusOps.hSet(hashKey, dictItem.getName());
        return dictItem;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictItem updateDtoById(Object updateDto) {
        DictItem dictItem = super.updateBefore(updateDto);

        DictItem old = getById(dictItem.getId());
        ArgumentAssert.notNull(old, "字典项不存在");

        ArgumentAssert.isFalse(checkItemByKey(dictItem.getUniqKey(), dictItem.getDictId(), dictItem.getId()), "字典项[{}]已经存在，请勿重复创建", dictItem.getUniqKey());
        Dict dict = dictMapper.selectOneById(dictItem.getDictId());
        ArgumentAssert.notNull(dict, "字典不存在");

        // 修改时，不能更新 dictType 字段
        dictItem.setDataType(dict.getDataType());
        if (dictItem.getParentId() != null) {
            DictItem parent = getById(dictItem.getParentId());
            ArgumentAssert.notNull(parent, "父字典项不存在");

            List<DictItem> childrenList = list(QueryWrapper.create().eq(DictItem::getDictId, dictItem.getDictId()).likeLeft(DictItem::getTreePath, old.getTreePath()));
            List<Long> childIdList = childrenList.stream().map(DictItem::getId).toList();
            if (CollUtil.contains(childIdList, dictItem.getParentId())) {
                throw new BizException("不能挂在自己以及自己的子孙节点");
            }

            DictItem current = MyTreeUtil.buildSingleTreeEntity(childrenList, old.getId(), DictItem::new);
            fill(current, parent);
            if (CollUtil.isNotEmpty(current.getChildren())) {
                recursiveFill(current.getChildren(), current);
            }
            updateBatch(childrenList);

            dictItem.setTreePath(current.getTreePath());
        } else {

            List<DictItem> childrenList = list(QueryWrapper.create().eq(DictItem::getDictId, dictItem.getDictId()).likeLeft(DictItem::getTreePath, old.getTreePath()));
            DictItem current = MyTreeUtil.buildSingleTreeEntity(childrenList, old.getId(), DictItem::new);
            fill(current, null);
            if (CollUtil.isNotEmpty(current.getChildren())) {
                recursiveFill(current.getChildren(), current);
            }
            updateBatch(childrenList);

            dictItem.setTreePath(current.getTreePath());
        }
        updateById(dictItem, false);
        delCache(dictItem);

        // 淘汰旧缓存
        CacheHashKey oldHashKey = DictItemHashCacheKeyBuilder.builder(dict.getUniqKey(), old.getUniqKey());
        cachePlusOps.hDel(oldHashKey);
        // 设置新缓存
        CacheHashKey hashKey = DictItemHashCacheKeyBuilder.builder(dict.getUniqKey(), dictItem.getUniqKey());
        cachePlusOps.hSet(hashKey, dictItem.getName());
        return dictItem;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItemByDictId(Dict entity) {
        DictItem item = new DictItem();
        item.setDataType(entity.getDataType());
        QueryWrapper updateWrapper = QueryWrapper.create();
        updateWrapper.eq(DictItem::getDictId, entity.getId());
        update(item, updateWrapper);

        // 淘汰缓存
        cachePlusOps.del(DictItemHashCacheKeyBuilder.builder(entity.getUniqKey()));
    }

    private void recursiveFill(List<DictItem> tree, DictItem parent) {
        for (DictItem node : tree) {
            fill(node, parent);

            if (CollUtil.isNotEmpty(node.getChildren())) {
                recursiveFill(node.getChildren(), node);
            }
        }
    }

    private void fill(DictItem item, DictItem parent) {
        if (parent == null) {
            item.setParentId(MyTreeUtil.DEF_PARENT_ID);
            item.setTreePath(MyTreeUtil.buildTreePath(item.getId()));
        } else {
            item.setParentId(parent.getId());
            item.setTreePath(MyTreeUtil.buildTreePath(parent.getTreePath(), item.getId()));
        }
    }
}
