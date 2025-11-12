package top.mddata.console.system.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.console.system.entity.Config;
import top.mddata.console.system.mapper.ConfigMapper;
import top.mddata.console.system.service.ConfigService;

/**
 * 系统配置 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigServiceImpl extends SuperServiceImpl<ConfigMapper, Config> implements ConfigService {

}
