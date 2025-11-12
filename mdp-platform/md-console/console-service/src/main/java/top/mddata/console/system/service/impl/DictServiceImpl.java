package top.mddata.console.system.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.console.system.entity.Dict;
import top.mddata.console.system.mapper.DictMapper;
import top.mddata.console.system.service.DictService;

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

}
