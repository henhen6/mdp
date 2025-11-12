package top.mddata.console.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.baidu.fsg.uid.UidGenerator;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.cache.repository.CachePlusOps;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.MyTreeUtil;
import top.mddata.common.cache.console.system.DictItemHashCacheKeyBuilder;
import top.mddata.console.system.entity.Dict;
import top.mddata.console.system.entity.DictItem;
import top.mddata.console.system.enumeration.DictTypeEnum;
import top.mddata.console.system.enumeration.ItemTypeEnum;
import top.mddata.console.system.mapper.DictMapper;
import top.mddata.console.system.service.DictItemService;
import top.mddata.console.system.service.DictService;
import top.mddata.console.system.vo.DictItemVo;
import top.mddata.console.system.vo.DictVo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DictServiceImpl extends SuperServiceImpl<DictMapper, Dict> implements DictService {
    private final DictItemService dictItemService;
    private final UidGenerator uidGenerator;
    private final CachePlusOps cachePlusOps;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        List<Dict> dictList = listByIds(idList);
        if (CollUtil.isEmpty(dictList)) {
            return false;
        }
        boolean flag = super.removeByIds(idList);

        dictItemService.removeByDictIds(idList);

        List<CacheKey> keyList = dictList.stream().map(Dict::getUniqKey).map(DictItemHashCacheKeyBuilder::builder).toList();
        cachePlusOps.del(keyList);
        return flag;
    }

    @Override
    protected Dict saveBefore(Object save) {
        Dict dict = super.saveBefore(save);
        ArgumentAssert.isFalse(checkByUniqKey(dict.getUniqKey(), null), "字典标识已存在");
        dict.setDictType(DictTypeEnum.SYSTEM.getCode());
        return dict;
    }

    @Override
    protected Dict updateBefore(Object updateDto) {
        Dict dict = super.updateBefore(updateDto);
        ArgumentAssert.isFalse(checkByUniqKey(dict.getUniqKey(), dict.getId()), "字典标识已存在");

        return dict;
    }

    @Override
    protected void updateAfter(Object updateDto, Dict entity) {
        dictItemService.updateItemByDictId(entity);
    }

    @Override
    public Boolean checkByUniqKey(String uniqKey, Long id) {
        ArgumentAssert.notEmpty(uniqKey, "请填写字典标识");
        return count(QueryWrapper.create().eq(Dict::getUniqKey, uniqKey).ne(Dict::getId, id)) > 0;
    }

    /**
     * 将存在的字典进行更新，不存在的字典数据新增
     *
     * @param list 枚举数据
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean importDictByEnum(List<DictVo> list) {
        if (CollUtil.isEmpty(list)) {
            return true;
        }

        List<String> keyList = list.stream().map(DictVo::getUniqKey).toList();
        List<Dict> existDictList = super.list(QueryWrapper.create().in(Dict::getUniqKey, keyList));

        // 将已存在的字典按uniqKey分组，便于快速查找
        Map<String, Dict> existingDictMap = existDictList.stream()
                .collect(Collectors.toMap(Dict::getUniqKey, dict -> dict));

        // 区分需要新增和更新的数据
        List<Dict> toSave = new ArrayList<>();
        List<DictItem> toSaveIictItemList = new ArrayList<>();

        List<Dict> toUpdate = new ArrayList<>();
        List<DictItem> toUpdateIictItemList = new ArrayList<>();

        for (DictVo vo : list) {
            String uniqKey = vo.getUniqKey();
            // 检查是否已存在
            if (existingDictMap.containsKey(uniqKey)) {
                // 已存在则更新
                Dict existingDict = existingDictMap.get(uniqKey);
                existingDict.setName(vo.getName());
                existingDict.setDictType(DictTypeEnum.ENUM.getCode());
                toUpdate.add(existingDict);

                List<DictItemVo> itemList = vo.getItemList();
                if (CollUtil.isNotEmpty(itemList)) {

                    List<String> itemKeyList = itemList.stream().map(DictItemVo::getUniqKey).toList();
                    // 已存在的字典项 list
                    List<DictItem> existDictItemList = dictItemService.list(QueryWrapper.create().eq(DictItem::getDictId, existingDict.getId()).in(DictItem::getUniqKey, itemKeyList));
                    // 已存在的字典项 map
                    Map<String, DictItem> existingDictItemMap = existDictItemList.stream().collect(Collectors.toMap(DictItem::getUniqKey, dict -> dict));

                    int weight = 0;
                    for (DictItemVo itemVo : itemList) {
                        String itemKey = itemVo.getUniqKey();
                        if (existingDictItemMap.containsKey(itemKey)) {
                            // 修改
                            DictItem item = existingDictItemMap.get(itemKey);
                            item.setName(itemVo.getName());
                            item.setDataType(existingDict.getDataType());
                            item.setDictType(DictTypeEnum.ENUM.getCode());

                            toUpdateIictItemList.add(item);
                        } else {
                            DictItem item = new DictItem();
                            item.setId(uidGenerator.getUid());
                            item.setDictId(existingDict.getId());
                            item.setUniqKey(itemVo.getUniqKey());
                            item.setName(itemVo.getName());
                            item.setState(true);
                            item.setWeight(weight++);
                            item.setDictType(DictTypeEnum.ENUM.getCode());
                            item.setDataType(existingDict.getDataType());
                            item.setTreePath(MyTreeUtil.buildTreePath(item.getId()));
                            toSaveIictItemList.add(item);
                        }
                    }
                }

            } else {
                // 不存在则新增
                Dict newDict = new Dict();
                newDict.setId(uidGenerator.getUid());
                newDict.setUniqKey(uniqKey);
                newDict.setRemark("枚举导入");
                newDict.setName(vo.getName());
                newDict.setState(true);
                newDict.setItemType(ItemTypeEnum.LIST.getCode());
                newDict.setDictType(DictTypeEnum.ENUM.getCode());
                newDict.setDictGroup(vo.getDictGroup());
                newDict.setDataType(vo.getDataType());
                toSave.add(newDict);

                List<DictItemVo> itemList = vo.getItemList();
                if (CollUtil.isNotEmpty(itemList)) {
                    int weight = 0;
                    for (DictItemVo itemVo : itemList) {
                        DictItem item = new DictItem();
                        item.setId(uidGenerator.getUid());
                        item.setDictId(newDict.getId());
                        item.setUniqKey(itemVo.getUniqKey());
                        item.setName(itemVo.getName());
                        item.setState(true);
                        item.setWeight(weight++);
                        item.setDictType(DictTypeEnum.ENUM.getCode());
                        item.setDataType(newDict.getDataType());
                        item.setTreePath(MyTreeUtil.buildTreePath(item.getId()));
                        toSaveIictItemList.add(item);
                    }
                }
            }
        }
        // 执行批量操作
        if (!toSave.isEmpty()) {
            saveBatch(toSave);
        }
        if (!toSaveIictItemList.isEmpty()) {
            dictItemService.saveBatch(toSaveIictItemList);
        }
        log.info("已经新增字典：{}条，字典项：{}条", toSave.size(), toSaveIictItemList.size());
        if (!toUpdate.isEmpty()) {
            updateBatch(toUpdate);
        }
        if (!toUpdateIictItemList.isEmpty()) {
            dictItemService.updateBatch(toUpdateIictItemList);
        }
        log.info("已经更新字典：{}条，字典项：{}条", toUpdate.size(), toUpdateIictItemList.size());

        // 淘汰缓存
        List<CacheKey> cacheKeyList = keyList.stream().map(DictItemHashCacheKeyBuilder::builder).toList();
        cachePlusOps.del(cacheKeyList);
        return true;
    }

    @Override
    public void clearCache(List<Serializable> ids) {
        QueryWrapper wrap = QueryWrapper.create().from(getEntityClass());
        if (CollUtil.isNotEmpty(ids)) {
            List<Long> idList = Convert.toList(Long.class, ids);
            wrap.in(Dict::getId, idList);
        }
        List<Dict> list = list(wrap);
        if (CollUtil.isEmpty(list)) {
            return;
        }

        // 字典-字典项 的hash 缓存
        List<CacheKey> hashKeys = list.stream().map(Dict::getUniqKey).map(DictItemHashCacheKeyBuilder::builder).toList();
        cacheOps.del(hashKeys);

        CacheKeyBuilder builder = cacheKeyBuilder();
        if (builder == null) {
            return;
        }
        // 字典（单体）缓存
        List<CacheKey> cacheKeys = list.stream().map(Dict::getId).map(builder::key).toList();
        cacheOps.del(cacheKeys);
    }
}
