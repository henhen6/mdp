package top.mddata.console.system.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.console.system.entity.DictItem;
import top.mddata.console.system.mapper.DictItemMapper;
import top.mddata.console.system.service.DictItemService;

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

}
