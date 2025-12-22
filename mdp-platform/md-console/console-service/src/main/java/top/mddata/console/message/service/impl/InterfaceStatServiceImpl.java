package top.mddata.console.message.service.impl;

import com.mybatisflex.core.update.UpdateChain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.console.message.entity.InterfaceStat;
import top.mddata.console.message.entity.table.InterfaceStatTableDef;
import top.mddata.console.message.mapper.InterfaceStatMapper;
import top.mddata.console.message.service.InterfaceStatService;

import java.time.LocalDateTime;

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrFailCount(Long id) {
        UpdateChain.of(InterfaceStat.class)
                .set(InterfaceStat::getFailCount, InterfaceStatTableDef.INTERFACE_STAT.FAIL_COUNT.add(1))
                .set(InterfaceStat::getLastExecAt, LocalDateTime.now())
                .where(InterfaceStat::getId).eq(id)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrSuccessCount(Long id) {
        UpdateChain.of(InterfaceStat.class)
                .set(InterfaceStat::getSuccessCount, InterfaceStatTableDef.INTERFACE_STAT.SUCCESS_COUNT.add(1))
                .set(InterfaceStat::getLastExecAt, LocalDateTime.now())
                .where(InterfaceStat::getId).eq(id)
                .update();
    }
}
