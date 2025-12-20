package top.mddata.console.message.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.console.message.entity.InterfaceConfig;
import top.mddata.console.message.mapper.InterfaceConfigMapper;
import top.mddata.console.message.service.InterfaceConfigService;

/**
 * 接口 服务层实现。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:47
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InterfaceConfigServiceImpl extends SuperServiceImpl<InterfaceConfigMapper, InterfaceConfig> implements InterfaceConfigService {

}
