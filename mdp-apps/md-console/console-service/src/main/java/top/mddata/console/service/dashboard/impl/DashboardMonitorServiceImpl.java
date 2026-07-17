package top.mddata.console.service.dashboard.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.console.mapper.message.InterfaceConfigMapper;
import top.mddata.console.mapper.message.InterfaceLogMapper;
import top.mddata.console.mapper.message.InterfaceStatMapper;
import top.mddata.console.service.dashboard.DashboardMonitorService;
import top.mddata.console.vo.dashboard.InterfaceRankVo;
import top.mddata.console.vo.dashboard.OverviewMonitorVo;
import top.mddata.console.vo.dashboard.SuccessRateVo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 接口监控统计 服务层实现
 *
 * <p>数据来源：mdc_interface_config / mdc_interface_stat / mdc_interface_log</p>
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardMonitorServiceImpl implements DashboardMonitorService {

    private final InterfaceConfigMapper interfaceConfigMapper;
    private final InterfaceStatMapper interfaceStatMapper;
    private final InterfaceLogMapper interfaceLogMapper;

    @Override
    public OverviewMonitorVo getOverview() {
        OverviewMonitorVo vo = new OverviewMonitorVo();
        vo.setInterfaceCount(interfaceConfigMapper.selectCountByQuery(QueryWrapper.create()));

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Map<String, Object> todaySum = interfaceLogMapper.sumToday(todayStart);
        long todaySuccess = toLong(todaySum != null ? todaySum.get("successCount") : 0L);
        long todayFail = toLong(todaySum != null ? todaySum.get("failCount") : 0L);
        vo.setTodaySuccessCount(todaySuccess);
        vo.setTodayFailCount(todayFail);
        vo.setTodayCallCount(todaySuccess + todayFail);

        Map<String, Object> totalSum = interfaceLogMapper.sumAll();
        long totalSuccess = toLong(totalSum != null ? totalSum.get("successCount") : 0L);
        long totalFail = toLong(totalSum != null ? totalSum.get("failCount") : 0L);
        vo.setTotalSuccessCount(totalSuccess);
        vo.setTotalFailCount(totalFail);
        vo.setTotalCount(totalSuccess + totalFail);

        return vo;
    }

    @Override
    public SuccessRateVo getSuccessRate() {
        SuccessRateVo vo = new SuccessRateVo();

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Map<String, Object> todaySum = interfaceLogMapper.sumToday(todayStart);
        long success = toLong(todaySum != null ? todaySum.get("successCount") : 0L);
        long fail = toLong(todaySum != null ? todaySum.get("failCount") : 0L);
        long total = success + fail;

        vo.setSuccessCount(success);
        vo.setFailCount(fail);
        vo.setTotalCount(total);
        if (total > 0) {
            double rate = BigDecimal.valueOf(success)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                    .doubleValue();
            vo.setRate(rate);
        } else {
            vo.setRate(0d);
        }
        return vo;
    }

    @Override
    public List<InterfaceRankVo> getCallRank(int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 100);
        List<Map<String, Object>> rawList = interfaceStatMapper.rankByTotalCount(safeLimit);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<InterfaceRankVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            InterfaceRankVo vo = new InterfaceRankVo();
            vo.setId(toLong(raw.get("id")));
            vo.setName(toStr(raw.get("name")));
            vo.setSuccessCount(toLong(raw.get("successCount")));
            vo.setFailCount(toLong(raw.get("failCount")));
            vo.setTotalCount(toLong(raw.get("totalCount")));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<InterfaceRankVo> getFailRank(int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 100);
        List<Map<String, Object>> rawList = interfaceStatMapper.rankByFailCount(safeLimit);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<InterfaceRankVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            InterfaceRankVo vo = new InterfaceRankVo();
            vo.setId(toLong(raw.get("id")));
            vo.setName(toStr(raw.get("name")));
            vo.setSuccessCount(toLong(raw.get("successCount")));
            vo.setFailCount(toLong(raw.get("failCount")));
            vo.setTotalCount(toLong(raw.get("totalCount")));
            result.add(vo);
        }
        return result;
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static String toStr(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
