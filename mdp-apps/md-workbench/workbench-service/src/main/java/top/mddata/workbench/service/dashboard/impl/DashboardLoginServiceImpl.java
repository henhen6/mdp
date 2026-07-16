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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** loginDate 字段格式：yyyy-MM-dd */
    private static final DateTimeFormatter LOGIN_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final LoginLogMapper loginLogMapper;

    @Override
    public OverviewLoginVo getOverviewLogin() {
        OverviewLoginVo vo = new OverviewLoginVo();
        String today = LocalDate.now().format(LOGIN_DATE_FORMAT);

        // 今日登录成功次数（mdw_login_log 没有 deletedAt 字段，QueryWrapper 不会自动追加过滤条件）
        vo.setTodayLoginCount(loginLogMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(LoginLog::getLoginDate, today)
                        .eq(LoginLog::getStatus, LOGIN_STATUS_SUCCESS)));

        // 今日登录失败次数
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
        List<RegionDistributionVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            RegionDistributionVo vo = new RegionDistributionVo();
            vo.setProvince(toStr(raw.get("name")));
            vo.setCount(toLong(raw.get("value")));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<DashboardRankVo> getProvinceRank(int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 100);
        String today = LocalDate.now().format(LOGIN_DATE_FORMAT);
        return toRankList(loginLogMapper.rankByProvince(today, safeLimit));
    }

    @Override
    public List<DashboardRankVo> getIpRank(int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 100);
        String today = LocalDate.now().format(LOGIN_DATE_FORMAT);
        return toRankList(loginLogMapper.rankByIp(today, safeLimit));
    }

    @Override
    public List<DashboardRankVo> getNameRank(int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 100);
        String today = LocalDate.now().format(LOGIN_DATE_FORMAT);
        return toRankList(loginLogMapper.rankByName(today, safeLimit));
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
        return toAuthTypeDistributionList(loginLogMapper.countByAuthType());
    }

    private List<DashboardDistributionVo> toAuthTypeDistributionList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = 0L;
        for (Map<String, Object> raw : rawList) {
            total += toLong(raw.get("count"));
        }
        List<DashboardDistributionVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            DashboardDistributionVo vo = new DashboardDistributionVo();
            String code = toStr(raw.get("code"));
            vo.setName(convertAuthType(code));
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

    @Override
    public List<DashboardDistributionVo> getChannelDistribution() {
        return toChannelDistributionList(loginLogMapper.countByChannel());
    }

    @Override
    public List<DashboardDistributionVo> getEventTypeDistribution() {
        return toEventTypeDistributionList(loginLogMapper.countByEventType());
    }

    private List<DashboardDistributionVo> toChannelDistributionList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = 0L;
        for (Map<String, Object> raw : rawList) {
            total += toLong(raw.get("count"));
        }
        List<DashboardDistributionVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            DashboardDistributionVo vo = new DashboardDistributionVo();
            String code = toStr(raw.get("code"));
            vo.setName(convertChannel(code));
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

    private List<DashboardDistributionVo> toEventTypeDistributionList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = 0L;
        for (Map<String, Object> raw : rawList) {
            total += toLong(raw.get("count"));
        }
        List<DashboardDistributionVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            DashboardDistributionVo vo = new DashboardDistributionVo();
            String code = toStr(raw.get("code"));
            vo.setName(convertEventType(code));
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

    @Override
    public List<DailyLoginVo> getDailyStatistics(int days) {
        int safeDays = (days != 7 && days != 30) ? 7 : days;
        String startDate = LocalDate.now().minusDays(safeDays - 1L).format(LOGIN_DATE_FORMAT);
        List<Map<String, Object>> rawList = loginLogMapper.dailyStatistics(startDate);

        Map<String, long[]> dateMap = new HashMap<>();
        for (Map<String, Object> raw : rawList) {
            String date = toStr(raw.get("date"));
            long loginCount = toLong(raw.get("loginCount"));
            long userCount = toLong(raw.get("userCount"));
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
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 100);
        String startDate = LocalDate.now().minusDays(6L).format(LOGIN_DATE_FORMAT);
        return toRankList(loginLogMapper.activeUserRank(startDate, safeLimit));
    }

    @Override
    public List<HourlyDistributionVo> getHourlyDistribution(String date) {
        String targetDate = (date == null || date.isBlank())
                ? LocalDate.now().format(LOGIN_DATE_FORMAT)
                : date;
        List<Map<String, Object>> rawList = loginLogMapper.hourlyDistribution(targetDate);

        Map<Integer, Long> hourMap = new HashMap<>();
        if (rawList != null) {
            for (Map<String, Object> raw : rawList) {
                hourMap.put(toInt(raw.get("hour")), toLong(raw.get("count")));
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

    private List<DashboardRankVo> toRankList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<DashboardRankVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            DashboardRankVo vo = new DashboardRankVo();
            vo.setName(toStr(raw.get("name")));
            vo.setValue(toLong(raw.get("value")));
            result.add(vo);
        }
        return result;
    }

    private List<DashboardDistributionVo> toDistributionList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = 0L;
        for (Map<String, Object> raw : rawList) {
            total += toLong(raw.get("count"));
        }
        List<DashboardDistributionVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            DashboardDistributionVo vo = new DashboardDistributionVo();
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

    private static Integer toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String toStr(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
