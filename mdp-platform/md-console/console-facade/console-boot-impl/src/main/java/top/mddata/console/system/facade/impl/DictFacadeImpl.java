package top.mddata.console.system.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.common.constant.EchoApi;
import top.mddata.console.system.facade.DictFacade;
import top.mddata.console.system.service.DictItemService;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 字典实现
 * @author henhen6
 * @since 2024/9/20 23:29
 */
@Service(EchoApi.DICT_CLASS)
@RequiredArgsConstructor
public class DictFacadeImpl implements DictFacade {
    private final DictItemService dictItemService;

    @Override
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        return dictItemService.findByIds(ids);
    }
}
