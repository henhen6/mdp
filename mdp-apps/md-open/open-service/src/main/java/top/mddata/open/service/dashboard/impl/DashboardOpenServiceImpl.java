package top.mddata.open.service.dashboard.impl;

import cn.hutool.core.convert.Convert;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.utils.DefValueHelper;
import top.mddata.common.enumeration.AuditStatusEnum;
import top.mddata.open.entity.admin.Api;
import top.mddata.open.entity.admin.ApiCallLog;
import top.mddata.open.entity.admin.App;
import top.mddata.open.entity.admin.AppApply;
import top.mddata.open.enumeration.admin.AppTypeEnum;
import top.mddata.open.enumeration.admin.ExecStatusEnum;
import top.mddata.open.mapper.admin.ApiCallLogMapper;
import top.mddata.open.mapper.admin.ApiMapper;
import top.mddata.open.mapper.admin.AppApplyMapper;
import top.mddata.open.mapper.admin.AppMapper;
import top.mddata.open.mapper.admin.EventPushLogMapper;
import top.mddata.open.mapper.admin.EventPushMapper;
import top.mddata.open.mapper.admin.EventTriggerMapper;
import top.mddata.open.mapper.admin.NotifyInfoLogMapper;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** 默认查询范围：近7天 */
    private static final int DEFAULT_DAYS = 6;
    /** 最大日期范围跨度：90天 */
    private static final int MAX_RANGE_DAYS = 90;
    /** 默认分页大小 */
    private static final int DEFAULT_LIMIT = 10;
    /** 最大分页大小 */
    private static final int MAX_LIMIT = 100;

    private final AppMapper appMapper;
    private final ApiMapper apiMapper;
    private final ApiCallLogMapper apiCallLogMapper;
    private final AppApplyMapper appApplyMapper;
    private final OauthLogMapper oauthLogMapper;
    private final EventTriggerMapper eventTriggerMapper;
    private final EventPushMapper eventPushMapper;
    private final NotifyInfoLogMapper notifyInfoLogMapper;
    private final EventPushLogMapper eventPushLogMapper;

    private static LocalDate[] normalizeRange(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        LocalDate start = startDate != null ? startDate : today.minusDays(DEFAULT_DAYS);
        LocalDate end = endDate != null ? endDate : today;
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        long span = end.toEpochDay() - start.toEpochDay();
        if (span > MAX_RANGE_DAYS) {
            start = end.minusDays(MAX_RANGE_DAYS);
        }
        return new LocalDate[]{start, end};
    }

    @Override
    public OverviewOpenVo getOverviewOpen() {
        OverviewOpenVo vo = new OverviewOpenVo();

        vo.setAppCount(appMapper.selectCountByQuery(QueryWrapper.create().eq(App::getState, true)));
        vo.setSelfBuildCount(appMapper.selectCountByQuery(QueryWrapper.create().eq(App::getState, true).eq(App::getType, AppTypeEnum.SELF_BUILT.getCode())));
        vo.setThirdPartyCount(appMapper.selectCountByQuery(QueryWrapper.create().eq(App::getState, true).eq(App::getType, AppTypeEnum.THIRD_PARTY.getCode())));

        vo.setApiCount(apiMapper.selectCountByQuery(QueryWrapper.create().eq(Api::getState, true)));

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        vo.setTodayApiCallCount(apiCallLogMapper.selectCountByQuery(QueryWrapper.create().ge(ApiCallLog::getCreatedAt, todayStart)));

        vo.setTodayFailCount(apiCallLogMapper.selectCountByQuery(QueryWrapper.create().ge(ApiCallLog::getCreatedAt, todayStart).eq(ApiCallLog::getExecStatus, ExecStatusEnum.FAIL.getCode())));

        vo.setPendingApplyCount(appApplyMapper.selectCountByQuery(QueryWrapper.create().eq(AppApply::getAuditStatus, AuditStatusEnum.PENDING.getCode())));

        vo.setRejectedApplyCount(appApplyMapper.selectCountByQuery(QueryWrapper.create().eq(AppApply::getAuditStatus, AuditStatusEnum.REJECTED.getCode())));

        return vo;
    }

    @Override
    public List<CallTrendVo> getCallTrend(LocalDate startDate, LocalDate endDate) {
        LocalDate[] range = normalizeRange(startDate, endDate);
        LocalDateTime startTime = LocalDateTime.of(range[0], LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(range[1], LocalTime.MAX);

        List<Map<String, Object>> rawList = apiCallLogMapper.countByDayRange(startTime, endTime);
        Map<String, long[]> dateMap = new HashMap<>();
        for (Map<String, Object> raw : rawList) {
            String date = String.valueOf(raw.get("date"));
            long call = Convert.toLong(raw.get("callCount"));
            long fail = Convert.toLong(raw.get("failCount"));
            dateMap.put(date, new long[]{call, fail});
        }

        List<CallTrendVo> result = new ArrayList<>();
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(range[0], range[1]) + 1;
        for (int i = 0; i < daysBetween; i++) {
            LocalDate d = range[0].plusDays(i);
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
        int safeLimit = DefValueHelper.normalizeLimit(limit, DEFAULT_LIMIT, MAX_LIMIT);
        List<Map<String, Object>> rawList = apiCallLogMapper.rankByApp(safeLimit);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<AppRankVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            AppRankVo vo = new AppRankVo();
            vo.setAppId(Convert.toLong(raw.get("appId")));
            vo.setAppName(Convert.toStr(raw.get("appName")));
            vo.setCallCount(Convert.toLong(raw.get("callCount")));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<ApiRankVo> getApiRank(int limit) {
        int safeLimit = DefValueHelper.normalizeLimit(limit, DEFAULT_LIMIT, MAX_LIMIT);
        List<Map<String, Object>> rawList = apiCallLogMapper.rankByApi(safeLimit);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<ApiRankVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            ApiRankVo vo = new ApiRankVo();
            vo.setApiId(Convert.toLong(raw.get("apiId")));
            vo.setApiName(Convert.toStr(raw.get("apiName")));
            vo.setAppName(Convert.toStr(raw.get("appName")));
            vo.setCallCount(Convert.toLong(raw.get("callCount")));
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

        long total = rawList.stream().mapToLong(raw -> Convert.toLong(raw.get("count"))).sum();

        List<OauthDistributionVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            OauthDistributionVo vo = new OauthDistributionVo();
            vo.setGrantType(Convert.toStr(raw.get("name")));
            long count = Convert.toLong(raw.get("count"));
            vo.setCount(count);
            vo.setPercent(DefValueHelper.calcPercent(count, total));
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
            vo.setEventCode(Convert.toStr(raw.get("eventCode")));
            vo.setEventName(Convert.toStr(raw.get("eventName")));
            vo.setTriggerCount(Convert.toLong(raw.get("triggerCount")));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<EventTriggerTrendVo> getEventTriggerTrend(LocalDate startDate, LocalDate endDate) {
        LocalDate[] range = normalizeRange(startDate, endDate);
        LocalDateTime startTime = LocalDateTime.of(range[0], LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(range[1], LocalTime.MAX);

        List<Map<String, Object>> rawList = eventTriggerMapper.countByDayRange(startTime, endTime);
        Map<String, Long> dateMap = new HashMap<>();
        for (Map<String, Object> raw : rawList) {
            dateMap.put(String.valueOf(raw.get("date")), Convert.toLong(raw.get("triggerCount")));
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
        int safeLimit = DefValueHelper.normalizeLimit(limit, DEFAULT_LIMIT, MAX_LIMIT);
        List<Map<String, Object>> rawList = eventTriggerMapper.rankByEventCode(safeLimit);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<EventTriggerStatisticsVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            EventTriggerStatisticsVo vo = new EventTriggerStatisticsVo();
            vo.setEventCode(Convert.toStr(raw.get("eventCode")));
            vo.setEventName(Convert.toStr(raw.get("eventName")));
            vo.setTriggerCount(Convert.toLong(raw.get("triggerCount")));
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
            vo.setEventCode(Convert.toStr(raw.get("eventCode")));
            vo.setEventName(Convert.toStr(raw.get("eventName")));
            vo.setAppId(Convert.toLong(raw.get("appId")));
            vo.setAppName(Convert.toStr(raw.get("appName")));
            vo.setPushCount(Convert.toLong(raw.get("pushCount")));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<EventPushTrendVo> getEventPushTrend(LocalDate startDate, LocalDate endDate) {
        LocalDate[] range = normalizeRange(startDate, endDate);
        LocalDateTime startTime = LocalDateTime.of(range[0], LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(range[1], LocalTime.MAX);

        List<Map<String, Object>> rawList = eventPushMapper.countByDayRange(startTime, endTime);
        Map<String, long[]> dateMap = new HashMap<>();
        for (Map<String, Object> raw : rawList) {
            String date = String.valueOf(raw.get("date"));
            long trigger = Convert.toLong(raw.get("triggerCount"));
            long push = Convert.toLong(raw.get("pushCount"));
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

    @Override
    public Map<String, Map<String, Long>> getSuccessRates() {
        Map<String, Map<String, Long>> result = new HashMap<>();

        // 1. 回调成功率（mdo_notify_info_log 表，exec_status = 1）
        Map<String, Object> callbackStat = notifyInfoLogMapper.sumAll();
        result.put("callback", Map.of(
                "successCount", Convert.toLong(callbackStat != null ? callbackStat.get("successCount") : 0L),
                "totalCount", Convert.toLong(callbackStat != null ? callbackStat.get("totalCount") : 0L)
        ));

        // 2. API调用成功率（mdo_api_call_log 表，exec_status = 1）
        Map<String, Object> apiCallStat = apiCallLogMapper.sumAll();
        result.put("apiCall", Map.of(
                "successCount", Convert.toLong(apiCallStat != null ? apiCallStat.get("successCount") : 0L),
                "totalCount", Convert.toLong(apiCallStat != null ? apiCallStat.get("totalCount") : 0L)
        ));

        // 3. 事件通知成功率（mdo_event_push_log 表，exec_status = 1）
        Map<String, Object> eventPushStat = eventPushLogMapper.sumAll();
        result.put("eventPush", Map.of(
                "successCount", Convert.toLong(eventPushStat != null ? eventPushStat.get("successCount") : 0L),
                "totalCount", Convert.toLong(eventPushStat != null ? eventPushStat.get("totalCount") : 0L)
        ));

        return result;
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

}