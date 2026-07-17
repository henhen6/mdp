package top.mddata.console.service.dashboard.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.console.mapper.system.RequestLogMapper;
import top.mddata.console.service.dashboard.DashboardRequestLogService;
import top.mddata.console.vo.dashboard.ConsumingTimeVo;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.IpRankVo;
import top.mddata.console.vo.dashboard.OverviewRequestVo;
import top.mddata.console.vo.dashboard.RegionDistributionVo;
import top.mddata.console.vo.dashboard.RequestInterfaceRankVo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 请求日志统计 服务层实现
 *
 * <p>数据来源：mdc_request_log</p>
 *
 * @author henhen6
 * @since 2026-07-17
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardRequestLogServiceImpl implements DashboardRequestLogService {

    private final RequestLogMapper requestLogMapper;

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
            vo.setName(convertLogType(toStr(raw.get("code"))));
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

    @Override
    public OverviewRequestVo getOverview() {
        OverviewRequestVo vo = new OverviewRequestVo();
        Long total = requestLogMapper.countTotal();
        Long abnormal = requestLogMapper.countAbnormal();
        Long success = requestLogMapper.countSuccess();
        vo.setTotalCount(total != null ? total : 0L);
        vo.setAbnormalCount(abnormal != null ? abnormal : 0L);
        vo.setSuccessCount(success != null ? success : 0L);
        return vo;
    }

    @Override
    public List<IpRankVo> getIpRank(int limit) {
        if (limit <= 0) {
            limit = 10;
        }
        List<Map<String, Object>> rawList = requestLogMapper.countByIpRank(limit);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<IpRankVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            IpRankVo vo = new IpRankVo();
            vo.setIpAddress(toStr(raw.get("ipAddress")));
            vo.setCount(toLong(raw.get("count")));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<RequestInterfaceRankVo> getInterfaceRank(int limit) {
        if (limit <= 0) {
            limit = 10;
        }
        List<Map<String, Object>> rawList = requestLogMapper.countByInterfaceRank(limit);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<RequestInterfaceRankVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            RequestInterfaceRankVo vo = new RequestInterfaceRankVo();
            String classPath = toStr(raw.get("classPath"));
            String methodName = toStr(raw.get("methodName"));
            String httpUri = toStr(raw.get("httpUri"));
            String httpMethod = toStr(raw.get("httpMethod"));
            String description = toStr(raw.get("description"));
            vo.setInterfaceName(classPath + "." + methodName);
            vo.setHttpUri(httpUri);
            vo.setHttpMethod(httpMethod);
            vo.setDescription(description);
            vo.setCount(toLong(raw.get("count")));
            vo.setFullName(classPath + "." + methodName + "(" + httpUri + " " + httpMethod + ")(" + description + ")");
            result.add(vo);
        }
        return result;
    }

    private String convertLogType(String value) {
        if (value == null) {
            return null;
        }
        for (RequestLog.LogType type : RequestLog.LogType.values()) {
            if (type.getValue().equals(value)) {
                return type.getDesc();
            }
        }
        return value;
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
