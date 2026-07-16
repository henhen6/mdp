package top.mddata.console.service.dashboard.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.console.mapper.message.InterfaceConfigMapper;
import top.mddata.console.mapper.message.InterfaceStatMapper;
import top.mddata.console.mapper.system.RequestLogMapper;
import top.mddata.console.service.dashboard.DashboardMonitorService;
import top.mddata.console.vo.dashboard.ConsumingTimeVo;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.InterfaceRankVo;
import top.mddata.console.vo.dashboard.OverviewMonitorVo;
import top.mddata.console.vo.dashboard.RegionDistributionVo;
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
 * <p>说明：mdc_interface_config / mdc_interface_stat / mdc_request_log 表均没有 deleted_at 字段，
 * MyBatis-Flex 不会自动追加删除过滤条件。</p>
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
    private final RequestLogMapper requestLogMapper;

    @Override
    public OverviewMonitorVo getOverview() {
        OverviewMonitorVo vo = new OverviewMonitorVo();
        Long total = interfaceConfigMapper.selectCountByQuery(QueryWrapper.create());
//        Long total = interfaceConfigMapper.countAll();
        vo.setInterfaceCount(total != null ? total : 0L);

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Map<String, Object> sumMap = interfaceStatMapper.sumAfter(todayStart);
        long success = toLong(sumMap != null ? sumMap.get("successCount") : 0L);
        long fail = toLong(sumMap != null ? sumMap.get("failCount") : 0L);
        vo.setTodaySuccessCount(success);
        vo.setTodayFailCount(fail);
        vo.setTodayCallCount(success + fail);

        Long abnormal = requestLogMapper.countAbnormal();
        vo.setAbnormalCount(abnormal != null ? abnormal : 0L);

        return vo;
    }

    @Override
    public SuccessRateVo getSuccessRate() {
        SuccessRateVo vo = new SuccessRateVo();

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Map<String, Object> sumMap = interfaceStatMapper.sumAfter(todayStart);
        long success = toLong(sumMap != null ? sumMap.get("successCount") : 0L);
        long fail = toLong(sumMap != null ? sumMap.get("failCount") : 0L);
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

    @Override
    public List<DistributionVo> getLogTypeDistribution() {
        List<Map<String, Object>> rawList = requestLogMapper.countByLogType();
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }

        long total = 0L;
        for (Map<String, Object> raw : rawList) {
            total += toLong(raw.get("count"));
        }

        List<DistributionVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            DistributionVo vo = new DistributionVo();
            vo.setName(toStr(raw.get("name")));
            long count = toLong(raw.get("count"));
            vo.setCount(count);
            if (total > 0) {
                double percent = BigDecimal.valueOf(count)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                        .doubleValue();
                vo.setPercent(percent);
            } else {
                vo.setPercent(0d);
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<RegionDistributionVo> getRegionDistribution() {
        List<Map<String, Object>> rawList = requestLogMapper.countByProvince();
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<RegionDistributionVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            RegionDistributionVo vo = new RegionDistributionVo();
            vo.setProvince(toStr(raw.get("province")));
            vo.setCount(toLong(raw.get("count")));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<ConsumingTimeVo> getConsumingTimeDistribution() {
        List<Map<String, Object>> rawList = requestLogMapper.countByConsumingRange();
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<ConsumingTimeVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            ConsumingTimeVo vo = new ConsumingTimeVo();
            vo.setName(toStr(raw.get("name")));
            vo.setCount(toLong(raw.get("count")));
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