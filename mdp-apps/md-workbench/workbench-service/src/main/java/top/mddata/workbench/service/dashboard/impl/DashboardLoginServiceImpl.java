package top.mddata.workbench.service.dashboard.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.workbench.entity.LoginLog;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.enumeration.LoginChannelEnum;
import top.mddata.workbench.enumeration.LoginEventTypeEnum;
import top.mddata.workbench.mapper.LoginLogMapper;
import top.mddata.workbench.service.dashboard.DashboardLoginService;
import top.mddata.workbench.vo.dashboard.DailyLoginVo;
import top.mddata.workbench.vo.dashboard.DashboardDistributionVo;
import top.mddata.workbench.vo.dashboard.DashboardRankVo;
import top.mddata.workbench.vo.dashboard.HourlyDistributionVo;
import top.mddata.workbench.vo.dashboard.OverviewLoginVo;
import top.mddata.workbench.vo.dashboard.RegionDistributionVo;
import top.mddata.base.utils.DefValueHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import cn.hutool.core.convert.Convert;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 登录与安全统计 服务层实现
 *
 * <p>说明：mdw_login_log 表没有逻辑删除字段，
 * 所以手写 SQL 也无需过滤 deleted_at。</p>
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardLoginServiceImpl implements DashboardLoginService {

    /** 登录状态：成功 */
    private static final String LOGIN_STATUS_SUCCESS = "01";
    /** 登录状态：失败 */
    private static final String LOGIN_STATUS_FAIL = "02";
    /** 默认查询天数 */
    private static final int DEFAULT_DAYS = 7;
    /** loginDate 字段格式 */
    private static final DateTimeFormatter LOGIN_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** 默认分页大小 */
    private static final int DEFAULT_LIMIT = 10;
    /** 最大分页大小 */
    private static final int MAX_LIMIT = 100;

    private final LoginLogMapper loginLogMapper;

    @Override
    public OverviewLoginVo getOverviewLogin() {
        OverviewLoginVo vo = new OverviewLoginVo();
        String today = LocalDate.now().format(LOGIN_DATE_FORMAT);

        vo.setTodayLoginCount(loginLogMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(LoginLog::getLoginDate, today)
                        .eq(LoginLog::getStatus, LOGIN_STATUS_SUCCESS)));

        vo.setTodayFailCount(loginLogMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(LoginLog::getLoginDate, today)
                        .eq(LoginLog::getStatus, LOGIN_STATUS_FAIL)));
        return vo;
    }

    @Override
    public List<RegionDistributionVo> getRegionDistribution() {
        String today = LocalDate.now().format(LOGIN_DATE_FORMAT);
        List<Map<String, Object>> rawList = loginLogMapper.rankByProvince(today, 1000);
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        return rawList.stream().map(raw -> {
            RegionDistributionVo vo = new RegionDistributionVo();
            vo.setProvince(Convert.toStr(raw.get("name")));
            vo.setCount(Convert.toLong(raw.get("value")));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DashboardRankVo> getProvinceRank(int limit) {
        return toRankList(loginLogMapper.rankByProvince(LocalDate.now().format(LOGIN_DATE_FORMAT), DefValueHelper.normalizeLimit(limit, DEFAULT_LIMIT, MAX_LIMIT)));
    }

    @Override
    public List<DashboardRankVo> getIpRank(int limit) {
        return toRankList(loginLogMapper.rankByIp(LocalDate.now().format(LOGIN_DATE_FORMAT), DefValueHelper.normalizeLimit(limit, DEFAULT_LIMIT, MAX_LIMIT)));
    }

    @Override
    public List<DashboardRankVo> getNameRank(int limit) {
        return toRankList(loginLogMapper.rankByName(LocalDate.now().format(LOGIN_DATE_FORMAT), DefValueHelper.normalizeLimit(limit, DEFAULT_LIMIT, MAX_LIMIT)));
    }

    @Override
    public List<DashboardDistributionVo> getBrowserDistribution() {
        return toDistributionList(loginLogMapper.countByBrowser());
    }

    @Override
    public List<DashboardDistributionVo> getOsDistribution() {
        return toDistributionList(loginLogMapper.countByOs());
    }

    @Override
    public List<DashboardDistributionVo> getAuthTypeDistribution() {
        List<Map<String, Object>> rawList = loginLogMapper.countByAuthType();
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = rawList.stream().mapToLong(raw -> Convert.toLong(raw.get("count"))).sum();
        return rawList.stream().map(raw -> {
            DashboardDistributionVo vo = new DashboardDistributionVo();
            vo.setName(convertAuthType(Convert.toStr(raw.get("code"))));
            long count = Convert.toLong(raw.get("count"));
            vo.setCount(count);
            vo.setPercent(DefValueHelper.calcPercent(count, total));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DashboardDistributionVo> getChannelDistribution() {
        List<Map<String, Object>> rawList = loginLogMapper.countByChannel();
        return convertDistributionList(rawList, this::convertChannel);
    }

    @Override
    public List<DashboardDistributionVo> getEventTypeDistribution() {
        List<Map<String, Object>> rawList = loginLogMapper.countByEventType();
        return convertDistributionList(rawList, this::convertEventType);
    }

    @Override
    public List<DailyLoginVo> getDailyStatistics(int days) {
        int safeDays = (days != 7 && days != 30) ? DEFAULT_DAYS : days;
        String startDate = LocalDate.now().minusDays(safeDays - 1L).format(LOGIN_DATE_FORMAT);
        List<Map<String, Object>> rawList = loginLogMapper.dailyStatistics(startDate);

        Map<String, long[]> dateMap = new java.util.HashMap<>();
        for (Map<String, Object> raw : rawList) {
            String date = Convert.toStr(raw.get("date"));
            long loginCount = Convert.toLong(raw.get("loginCount"));
            long userCount = Convert.toLong(raw.get("userCount"));
            dateMap.put(date, new long[]{loginCount, userCount});
        }

        List<DailyLoginVo> result = new ArrayList<>(safeDays);
        LocalDate today = LocalDate.now();
        for (int i = safeDays - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String key = d.format(LOGIN_DATE_FORMAT);
            long[] arr = dateMap.getOrDefault(key, new long[]{0L, 0L});
            DailyLoginVo vo = new DailyLoginVo();
            vo.setDate(key);
            vo.setLoginCount(arr[0]);
            vo.setUserCount(arr[1]);
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<DashboardRankVo> getActiveUserRank(int limit) {
        String startDate = LocalDate.now().minusDays(6L).format(LOGIN_DATE_FORMAT);
        return toRankList(loginLogMapper.activeUserRank(startDate, DefValueHelper.normalizeLimit(limit, DEFAULT_LIMIT, MAX_LIMIT)));
    }

    @Override
    public List<HourlyDistributionVo> getHourlyDistribution(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        List<Map<String, Object>> rawList = loginLogMapper.hourlyDistribution(targetDate.format(LOGIN_DATE_FORMAT));

        Map<Integer, Long> hourMap = new java.util.HashMap<>();
        if (rawList != null) {
            for (Map<String, Object> raw : rawList) {
                hourMap.put(Convert.toInt(raw.get("hour")), Convert.toLong(raw.get("count")));
            }
        }

        List<HourlyDistributionVo> result = new ArrayList<>(24);
        for (int h = 0; h < 24; h++) {
            HourlyDistributionVo vo = new HourlyDistributionVo();
            vo.setHour(h);
            vo.setCount(hourMap.getOrDefault(h, 0L));
            result.add(vo);
        }
        return result;
    }

    /** 转换分发列表（通用模板） */
    private List<DashboardDistributionVo> convertDistributionList(List<Map<String, Object>> rawList,
                                                                   java.util.function.Function<String, String> converter) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = rawList.stream().mapToLong(raw -> Convert.toLong(raw.get("count"))).sum();
        return rawList.stream().map(raw -> {
            DashboardDistributionVo vo = new DashboardDistributionVo();
            vo.setName(converter.apply(Convert.toStr(raw.get("code"))));
            long count = Convert.toLong(raw.get("count"));
            vo.setCount(count);
            vo.setPercent(DefValueHelper.calcPercent(count, total));
            return vo;
        }).collect(Collectors.toList());
    }

    /** 转换为排行列表 */
    private List<DashboardRankVo> toRankList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        return rawList.stream().map(raw -> {
            DashboardRankVo vo = new DashboardRankVo();
            vo.setName(Convert.toStr(raw.get("name")));
            vo.setValue(Convert.toLong(raw.get("value")));
            return vo;
        }).collect(Collectors.toList());
    }

    /** 转换为分发列表（无转换器，直接用name字段） */
    private List<DashboardDistributionVo> toDistributionList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = rawList.stream().mapToLong(raw -> Convert.toLong(raw.get("count"))).sum();
        return rawList.stream().map(raw -> {
            DashboardDistributionVo vo = new DashboardDistributionVo();
            vo.setName(Convert.toStr(raw.get("name")));
            long count = Convert.toLong(raw.get("count"));
            vo.setCount(count);
            vo.setPercent(DefValueHelper.calcPercent(count, total));
            return vo;
        }).collect(Collectors.toList());
    }

    private String convertAuthType(String code) {
        if (code == null) {
            return null;
        }
        for (AuthTypeEnum enumVal : AuthTypeEnum.values()) {
            if (enumVal.getCode().equals(code)) {
                return enumVal.getDesc();
            }
        }
        return code;
    }

    private String convertChannel(String code) {
        if (code == null) {
            return null;
        }
        for (LoginChannelEnum enumVal : LoginChannelEnum.values()) {
            if (enumVal.getCode().equals(code)) {
                return enumVal.getDesc();
            }
        }
        return code;
    }

    private String convertEventType(String code) {
        if (code == null) {
            return null;
        }
        for (LoginEventTypeEnum enumVal : LoginEventTypeEnum.values()) {
            if (enumVal.getCode().equals(code)) {
                return enumVal.getDesc();
            }
        }
        return code;
    }
}
