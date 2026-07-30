package top.mddata.console.service.dashboard.impl;

import cn.hutool.core.convert.Convert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.utils.DefValueHelper;
import top.mddata.console.mapper.system.RequestLogMapper;
import top.mddata.console.service.dashboard.DashboardRequestLogService;
import top.mddata.console.vo.dashboard.ConsumingTimeVo;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.IpRankVo;
import top.mddata.console.vo.dashboard.OverviewRequestVo;
import top.mddata.console.vo.dashboard.RegionDistributionVo;
import top.mddata.console.vo.dashboard.RequestInterfaceRankVo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /** 默认分页大小 */
    private static final int DEFAULT_LIMIT = 10;
    /** 最大分页大小 */
    private static final int MAX_LIMIT = 100;

    private final RequestLogMapper requestLogMapper;

    @Override
    public List<DistributionVo> getLogTypeDistribution() {
        List<Map<String, Object>> rawList = requestLogMapper.countByLogType();
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = rawList.stream().mapToLong(raw -> Convert.toLong(raw.get("count"))).sum();
        return rawList.stream().map(raw -> {
            DistributionVo vo = new DistributionVo();
            vo.setName(convertLogType(Convert.toStr(raw.get("code"))));
            long count = Convert.toLong(raw.get("count"));
            vo.setCount(count);
            vo.setPercent(DefValueHelper.calcPercent(count, total));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<RegionDistributionVo> getRegionDistribution() {
        List<Map<String, Object>> rawList = requestLogMapper.countByProvince();
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        return rawList.stream().map(raw -> {
            RegionDistributionVo vo = new RegionDistributionVo();
            vo.setProvince(Convert.toStr(raw.get("province")));
            vo.setCount(Convert.toLong(raw.get("count")));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ConsumingTimeVo> getConsumingTimeDistribution() {
        List<Map<String, Object>> rawList = requestLogMapper.countByConsumingRange();
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        return rawList.stream().map(raw -> {
            ConsumingTimeVo vo = new ConsumingTimeVo();
            vo.setName(Convert.toStr(raw.get("name")));
            vo.setCount(Convert.toLong(raw.get("count")));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public OverviewRequestVo getOverview() {
        OverviewRequestVo vo = new OverviewRequestVo();
        Long total = requestLogMapper.countTotal();
        Long abnormal = requestLogMapper.countAbnormal();
        Long success = requestLogMapper.countSuccess();
        vo.setTotalCount(DefValueHelper.nvl(total, 0L));
        vo.setAbnormalCount(DefValueHelper.nvl(abnormal, 0L));
        vo.setSuccessCount(DefValueHelper.nvl(success, 0L));
        return vo;
    }

    @Override
    public List<IpRankVo> getIpRank(int limit) {
        List<Map<String, Object>> rawList = requestLogMapper.countByIpRank(DefValueHelper.normalizeLimit(limit, DEFAULT_LIMIT, MAX_LIMIT));
        return toIpRankList(rawList);
    }

    @Override
    public List<RequestInterfaceRankVo> getInterfaceRank(int limit) {
        List<Map<String, Object>> rawList = requestLogMapper.countByInterfaceRank(DefValueHelper.normalizeLimit(limit, DEFAULT_LIMIT, MAX_LIMIT));
        return toInterfaceRankList(rawList);
    }

    /** 转换为IP排行列表 */
    private List<IpRankVo> toIpRankList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        return rawList.stream().map(raw -> {
            IpRankVo vo = new IpRankVo();
            vo.setIpAddress(Convert.toStr(raw.get("ipAddress")));
            vo.setCount(Convert.toLong(raw.get("count")));
            return vo;
        }).collect(Collectors.toList());
    }

    /** 转换为接口排行列表 */
    private List<RequestInterfaceRankVo> toInterfaceRankList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        return rawList.stream().map(raw -> {
            RequestInterfaceRankVo vo = new RequestInterfaceRankVo();
            String classPath = Convert.toStr(raw.get("classPath"));
            String methodName = Convert.toStr(raw.get("methodName"));
            String httpUri = Convert.toStr(raw.get("httpUri"));
            String httpMethod = Convert.toStr(raw.get("httpMethod"));
            String description = Convert.toStr(raw.get("description"));
            vo.setInterfaceName(classPath + "." + methodName);
            vo.setHttpUri(httpUri);
            vo.setHttpMethod(httpMethod);
            vo.setDescription(description);
            vo.setCount(Convert.toLong(raw.get("count")));
            vo.setFullName(buildFullName(classPath, methodName, httpUri, httpMethod, description));
            return vo;
        }).collect(Collectors.toList());
    }

    private String buildFullName(String classPath, String methodName, String httpUri, String httpMethod, String description) {
        return classPath + "." + methodName + "(" + httpUri + " " + httpMethod + ")(" + description + ")";
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
}
