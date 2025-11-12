package top.mddata.console.system.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.system.entity.Dict;
import top.mddata.console.system.entity.DictItem;
import top.mddata.console.system.vo.DictItemVo;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 字典项 服务层。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
public interface DictItemService extends SuperService<DictItem> {
    /**
     * 根据字典id，修改字典项数据
     * @param entity 字典
     */
    void updateItemByDictId(Dict entity);

    /**
     * 字典回显
     * @param dictKeys 唯一键
     * @return 回显数据
     */
    Map<Serializable, Object> findByIds(Set<Serializable> dictKeys);


    /**
     * 根据字典标识，查询字典项
     * @param uniqKeyList 字典标识
     * @return 字典项
     */
    Map<String, List<DictItemVo>> findDictItemByUniqKey(List<String> uniqKeyList);


    /**
     * 检查字典项标识是否可用
     *
     * @param key    标识
     * @param dictId 所属字典id
     * @param id     当前id
     * @return 存在返回true
     */
    boolean checkItemByKey(String key, Long dictId, Long id);

    /**
     * 根据字典id删除字典项
     * @param idList 字典id
     * @return 是否成功
     */
    boolean removeByDictIds(Collection<? extends Serializable> idList);

}
