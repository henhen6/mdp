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
import cn.hutool.core.convert.Convert;
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

    /** 默认分页大小 */
    private static final int DEFAULT_LIMIT = 10;
    /** 最大分页大小 */
    private static final int MAX_LIMIT = 100;
    /** 百分比计算精度 */
    private static final int PERCENT_SCALE = 2;

    private final InterfaceConfigMapper interfaceConfigMapper;
    private final InterfaceStatMapper interfaceStatMapper;
    private final InterfaceLogMapper interfaceLogMapper;

    @Override
    public OverviewMonitorVo getOverview() {
        OverviewMonitorVo vo = new OverviewMonitorVo();
        vo.setInterfaceCount(interfaceConfigMapper.selectCountByQuery(QueryWrapper.create()));

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Map<String, Object> todaySum = interfaceLogMapper.sumToday(todayStart);
        long todaySuccess = Convert.toLong(todaySum != null ? todaySum.get("successCount") : null);
        long todayFail = Convert.toLong(todaySum != null ? todaySum.get("failCount") : null);
        vo.setTodaySuccessCount(todaySuccess);
        vo.setTodayFailCount(todayFail);
        vo.setTodayCallCount(todaySuccess + todayFail);

        Map<String, Object> totalSum = interfaceLogMapper.sumAll();
        long totalSuccess = Convert.toLong(totalSum != null ? totalSum.get("successCount") : null);
        long totalFail = Convert.toLong(totalSum != null ? totalSum.get("failCount") : null);
        vo.setTotalSuccessCount(totalSuccess);
        vo.setTotalFailCount(totalFail);
        vo.setTotalCount(totalSuccess + totalFail);

        return vo;
    }

    @Override
    public SuccessRateVo getSuccessRate() {
        SuccessRateVo vo = new SuccessRateVo();

        Map<String, Object> todaySum = interfaceLogMapper.sumAll();
        long success = Convert.toLong(todaySum != null ? todaySum.get("successCount") : null);
        long fail = Convert.toLong(todaySum != null ? todaySum.get("failCount") : null);
        long total = success + fail;

        vo.setSuccessCount(success);
        vo.setFailCount(fail);
        vo.setTotalCount(total);
        vo.setRate(calcPercent(success, total));
        return vo;
    }

    @Override
    public List<InterfaceRankVo> getCallRank(int limit) {
        List<Map<String, Object>> rawList = interfaceStatMapper.rankByTotalCount(normalizeLimit(limit));
        return toInterfaceRankList(rawList);
    }

    @Override
    public List<InterfaceRankVo> getFailRank(int limit) {
        List<Map<String, Object>> rawList = interfaceStatMapper.rankByFailCount(normalizeLimit(limit));
        return toInterfaceRankList(rawList);
    }

    /** 归一化分页大小 */
    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    /** 计算百分比 */
    private double calcPercent(long successCount, long totalCount) {
        if (totalCount <= 0) {
            return 0d;
        }
        return BigDecimal.valueOf(successCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCount), PERCENT_SCALE, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /** 转换为接口排行列表 */
    private List<InterfaceRankVo> toInterfaceRankList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<InterfaceRankVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            InterfaceRankVo vo = new InterfaceRankVo();
            vo.setId(Convert.toLong(raw.get("id")));
            vo.setName(Convert.toStr(raw.get("name")));
            vo.setSuccessCount(Convert.toLong(raw.get("successCount")));
            vo.setFailCount(Convert.toLong(raw.get("failCount")));
            vo.setTotalCount(Convert.toLong(raw.get("totalCount")));
            result.add(vo);
        }
        return result;
    }
}
