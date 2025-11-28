package top.mddata.console.controller;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.common.enumeration.EnumService;
import top.mddata.common.vo.Option;
import top.mddata.console.system.entity.Dict;
import top.mddata.console.system.entity.DictItem;
import top.mddata.console.system.service.DictService;
import top.mddata.console.system.vo.DictItemVo;
import top.mddata.console.system.vo.DictVo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 枚举转换字典
 * @author henhen6
 * @since 2025/9/23 20:18
 */
@Slf4j
@RestController
@RequestMapping("/enumeration")
@Tag(name = "枚举转换字典")
@RequiredArgsConstructor
public class EnumToDictController {
    private final EnumService enumService;
    private final DictService dictService;

    @Operation(summary = "返回服务中所有枚举类", description = "只能扫描实现了BaseEnum类的枚举")
    @PostMapping("/findAll")
    public R<List<DictVo>> findAll(@RequestParam(required = false) Boolean rescan) {
        // 查找系统中已经存在的枚举
        Map<Option, List<Option>> map = enumService.findAll(rescan);
        if (CollUtil.isEmpty(map)) {
            return R.success(Collections.emptyList());
        }

        // 将枚举的value值，转为字典key
        List<String> dictKeyList = new ArrayList<>();
        map.forEach((key, value) -> dictKeyList.add(key.getValue()));

        // 查询数据库库中的所有字典和字典项
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper
                .select(Dict::getId, Dict::getUniqKey, Dict::getName)
                .select(DictItem::getId, DictItem::getUniqKey, DictItem::getName, DictItem::getDictId)
                .from(Dict.class)
                .leftJoin(DictItem.class)
                .on(Dict::getId, DictItem::getDictId)
                .in(Dict::getUniqKey, dictKeyList);
        List<Dict> existsDictList = dictService.list(queryWrapper);

        // 已存在的字典
        Map<String, Dict> existingDictMap = new HashMap<>();
        // 已存在的字典项
        Map<String, DictItem> existingDictItemMap = new HashMap<>();
        for (Dict dict : existsDictList) {
            existingDictMap.put(dict.getUniqKey(), dict);
            List<DictItem> dictItemList = dict.getDictItemList();
            if (CollUtil.isNotEmpty(dictItemList)) {
                for (DictItem item : dictItemList) {
                    existingDictItemMap.put(dict.getUniqKey() + item.getUniqKey(), item);
                }
            }
        }

        //        将 Map<Option, List<Option>> 转换为 List<DictVo>，并判断是否存在
        List<DictVo> list = new ArrayList<>();
        map.forEach((key, value) -> {
            boolean exist = existingDictMap.containsKey(key.getValue());
            DictVo vo = new DictVo();
            vo.setUniqKey(key.getValue());
            vo.setName(key.getLabel());
            vo.setDataType(key.getRemark());
            vo.setExist(exist);
            List<DictItemVo> itemList = new ArrayList<>();
            value.forEach(option -> {
                boolean itemExist = existingDictItemMap.containsKey(key.getValue() + option.getValue());
                DictItemVo item = new DictItemVo();
                item.setUniqKey(option.getValue());
                item.setName(option.getLabel());
                item.setExist(itemExist);
                itemList.add(item);
            });
            vo.setItemList(itemList);
            list.add(vo);
        });
        return R.success(list);
    }
}
