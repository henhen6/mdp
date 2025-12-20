package top.mddata.console.message.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.console.message.entity.InterfaceStat;
import top.mddata.console.message.mapper.InterfaceStatMapper;
import top.mddata.console.message.service.InterfaceStatService;

/**
 * 接口统计 服务层实现。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InterfaceStatServiceImpl extends SuperServiceImpl<InterfaceStatMapper, InterfaceStat> implements InterfaceStatService {

}
