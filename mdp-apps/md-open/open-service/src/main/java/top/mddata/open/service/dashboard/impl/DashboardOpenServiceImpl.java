package top.mddata.open.service.dashboard.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.open.entity.admin.ApiCallLog;
import top.mddata.open.entity.admin.App;
import top.mddata.open.mapper.admin.ApiCallLogMapper;
import top.mddata.open.mapper.admin.ApiMapper;
import top.mddata.open.mapper.admin.AppApplyMapper;
import top.mddata.open.mapper.admin.AppMapper;
import top.mddata.open.mapper.admin.EventPushMapper;
import top.mddata.open.mapper.admin.EventTriggerMapper;
import top.mddata.open.mapper.admin.OauthLogMapper;
import top.mddata.open.service.dashboard.DashboardOpenService;
import top.mddata.open.vo.dashboard.ApiRankVo;
import top.mddata.open.vo.dashboard.AppRankVo;
import top.mddata.open.vo.dashboard.CallTrendVo;
import top.mddata.open.vo.dashboard.EventPushStatisticsVo;
import top.mddata.open.vo.dashboard.EventPushTrendVo;
import top.mddata.open.vo.dashboard.EventTriggerStatisticsVo;
import top.mddata.open.vo.dashboard.EventTriggerTrendVo;
import top.mddata.open.vo.dashboard.OauthDistributionVo;
import top.mddata.open.vo.dashboard.OverviewOpenVo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 开放平台统计 服务层实现
 *
 * <p>说明：mdo_app / mdo_api / mdo_api_call_log / mdo_app_apply / mdo_oauth_log /
 * mdo_event_trigger / mdo_event_type / mdo_event_push 都没有 deleted_at 字段，
 * MyBatis-Flex 不会自动追加删除过滤条件。</p>
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardOpenServiceImpl implements DashboardOpenService {

    private final AppMapper appMapper;
    private final ApiMapper apiMapper;
    private final ApiCallLogMapper apiCallLogMapper;
    private final AppApplyMapper appApplyMapper;
    private final OauthLogMapper oauthLogMapper;
    private final EventTriggerMapper eventTriggerMapper;
    private final EventPushMapper eventPushMapper;

    @Override
    public OverviewOpenVo getOverviewOpen() {
        OverviewOpenVo vo = new OverviewOpenVo();

        vo.setAppCount(appMapper.selectCountByQuery(
                QueryWrapper.create().eq(App::getState, true)));

        Long selfBuild = appMapper.countSelfBuild();
        vo.setSelfBuildCount(selfBuild != null ? selfBuild : 0L);

        Long thirdParty = appMapper.countThirdParty();
        vo.setThirdPartyCount(thirdParty != null ? thirdParty : 0L);

        Long apiCount = apiMapper.countEnabled();
        vo.setApiCount(apiCount != null ? apiCount : 0L);

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        vo.setTodayApiCallCount(apiCallLogMapper.selectCountByQuery(
                QueryWrapper.create().ge(ApiCallLog::getCreatedAt, todayStart)));

        Long todayFail = apiCallLogMapper.countTodayFail(todayStart);
        vo.setTodayFailCount(todayFail != null ? todayFail : 0L);

        Long pending = appApplyMapper.countPending();
        vo.setPendingApplyCount(pending != null ? pending : 0L);

        return vo;
    }

    @Override
    public List<CallTrendVo> getCallTrend(int days) {
        int safeDays = (days != 7 && days != 30) ? 7 : days;
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now().minusDays(safeDays - 1L), LocalTime.MIN);

        List<Map<String, Object>> rawList = apiCallLogMapper.countByDay(startTime);
        Map<String, long[]> dateMap = new HashMap<>();
        for (Map<String, Object> raw : rawList) {
            String date = String.valueOf(raw.get("date"));
            long call = toLong(raw.get("callCount"));
            long fail = toLong(raw.get("failCount"));
            dateMap.put(date, new long[]{call, fail});
        }

        List<CallTrendVo> result = new ArrayList<>(safeDays);
        LocalDate today = LocalDate.now();
        for (int i = safeDays - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String key = d.toString();
            long[] arr = dateMap.getOrDefault(key, new long[]{0L, 0L});
            CallTrendVo vo = new CallTrendVo();
            vo.setDate(key);
            vo.setCallCount(arr[0]);
            vo.setFailCount(arr[1]);
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<AppRankVo> getAppRank(int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 100);
        List<Map<String, Object>> rawList = apiCallLogMapper.rankByApp(safeLimit);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<AppRankVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            AppRankVo vo = new AppRankVo();
            vo.setAppId(toLong(raw.get("appId")));
            vo.setAppName(toStr(raw.get("appName")));
            vo.setCallCount(toLong(raw.get("callCount")));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<ApiRankVo> getApiRank(int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 100);
        List<Map<String, Object>> rawList = apiCallLogMapper.rankByApi(safeLimit);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<ApiRankVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            ApiRankVo vo = new ApiRankVo();
            vo.setApiId(toLong(raw.get("apiId")));
            vo.setApiName(toStr(raw.get("apiName")));
            vo.setAppName(toStr(raw.get("appName")));
            vo.setCallCount(toLong(raw.get("callCount")));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<OauthDistributionVo> getOauthDistribution() {
        List<Map<String, Object>> rawList = oauthLogMapper.countByGrantType();
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }

        long total = 0L;
        for (Map<String, Object> raw : rawList) {
            total += toLong(raw.get("count"));
        }

        List<OauthDistributionVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            OauthDistributionVo vo = new OauthDistributionVo();
            vo.setGrantType(toStr(raw.get("name")));
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
    public List<EventTriggerStatisticsVo> getEventTriggerStatistics() {
        List<Map<String, Object>> rawList = eventTriggerMapper.countByEventCode();
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<EventTriggerStatisticsVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            EventTriggerStatisticsVo vo = new EventTriggerStatisticsVo();
            vo.setEventCode(toStr(raw.get("eventCode")));
            vo.setEventName(toStr(raw.get("eventName")));
            vo.setTriggerCount(toLong(raw.get("triggerCount")));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<EventTriggerTrendVo> getEventTriggerTrend(LocalDate startDate, LocalDate endDate) {
        LocalDate[] range = normalizeRange(startDate, endDate);
        LocalDateTime startTime = LocalDateTime.of(range[0], LocalTime.MIN);

        List<Map<String, Object>> rawList = eventTriggerMapper.countByDay(startTime);
        Map<String, Long> dateMap = new HashMap<>();
        for (Map<String, Object> raw : rawList) {
            dateMap.put(String.valueOf(raw.get("date")), toLong(raw.get("triggerCount")));
        }
        return fillDateRange(range[0], range[1], dateMap, (date, count) -> {
            EventTriggerTrendVo vo = new EventTriggerTrendVo();
            vo.setDate(date);
            vo.setTriggerCount(count);
            return vo;
        });
    }

    @Override
    public List<EventTriggerStatisticsVo> getEventTriggerRank(int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 100);
        List<Map<String, Object>> rawList = eventTriggerMapper.rankByEventCode(safeLimit);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<EventTriggerStatisticsVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            EventTriggerStatisticsVo vo = new EventTriggerStatisticsVo();
            vo.setEventCode(toStr(raw.get("eventCode")));
            vo.setEventName(toStr(raw.get("eventName")));
            vo.setTriggerCount(toLong(raw.get("triggerCount")));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<EventPushStatisticsVo> getEventPushStatistics() {
        List<Map<String, Object>> rawList = eventPushMapper.statisticsByEventAndApp();
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<EventPushStatisticsVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            EventPushStatisticsVo vo = new EventPushStatisticsVo();
            vo.setEventCode(toStr(raw.get("eventCode")));
            vo.setEventName(toStr(raw.get("eventName")));
            vo.setAppId(toLong(raw.get("appId")));
            vo.setPushCount(toLong(raw.get("pushCount")));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<EventPushTrendVo> getEventPushTrend(LocalDate startDate, LocalDate endDate) {
        LocalDate[] range = normalizeRange(startDate, endDate);
        LocalDateTime startTime = LocalDateTime.of(range[0], LocalTime.MIN);

        List<Map<String, Object>> rawList = eventPushMapper.countByDay(startTime);
        Map<String, long[]> dateMap = new HashMap<>();
        for (Map<String, Object> raw : rawList) {
            String date = String.valueOf(raw.get("date"));
            long trigger = toLong(raw.get("triggerCount"));
            long push = toLong(raw.get("pushCount"));
            dateMap.put(date, new long[]{trigger, push});
        }

        int days = (int) (range[1].toEpochDay() - range[0].toEpochDay()) + 1;
        List<EventPushTrendVo> result = new ArrayList<>(days);
        for (int i = 0; i < days; i++) {
            LocalDate d = range[0].plusDays(i);
            String key = d.toString();
            long[] arr = dateMap.getOrDefault(key, new long[]{0L, 0L});
            EventPushTrendVo vo = new EventPushTrendVo();
            vo.setDate(key);
            vo.setTriggerCount(arr[0]);
            vo.setPushCount(arr[1]);
            result.add(vo);
        }
        return result;
    }

    private static LocalDate[] normalizeRange(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        LocalDate start = startDate != null ? startDate : today.minusDays(6);
        LocalDate end = endDate != null ? endDate : today;
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        long span = end.toEpochDay() - start.toEpochDay();
        if (span > 90) {
            start = end.minusDays(90);
        }
        return new LocalDate[]{start, end};
    }

    private <T> List<T> fillDateRange(LocalDate start, LocalDate end, Map<String, Long> dateMap,
                                       java.util.function.BiFunction<String, Long, T> creator) {
        int days = (int) (end.toEpochDay() - start.toEpochDay()) + 1;
        List<T> result = new ArrayList<>(days);
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            String key = d.toString();
            long count = dateMap.getOrDefault(key, 0L);
            T vo = creator.apply(key, count);
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