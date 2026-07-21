package top.mddata.console.service.dashboard.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.User;
import top.mddata.common.enumeration.StateEnum;
import top.mddata.common.enumeration.organization.OrgTypeEnum;
import top.mddata.common.enumeration.organization.UserTypeEnum;
import top.mddata.common.mapper.OrgMapper;
import top.mddata.common.mapper.UserMapper;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.mapper.permission.RoleMapper;
import top.mddata.console.service.dashboard.DashboardUserService;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.OverviewUserVo;
import top.mddata.console.vo.dashboard.RankVo;
import top.mddata.console.vo.dashboard.TrendVo;

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
import java.util.stream.Collectors;

/**
 * 用户与组织统计 服务层实现
 *
 * <p>说明：MyBatis-Flex 内置处理 deletedAt 逻辑删除字段，
 * 无需在 QueryWrapper 中手动添加 .eq(Xxx::getDeletedAt, 0L)。
 * 但手写 SQL（UserMapper.countByDay/countByState/countByType）已显式添加 deleted_at = 0 过滤条件。</p>
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardUserServiceImpl implements DashboardUserService {

    /** 默认分页大小 */
    private static final int DEFAULT_LIMIT = 10;
    /** 最大分页大小 */
    private static final int MAX_LIMIT = 100;
    /** 默认日期范围：6天前到今天 */
    private static final int DEFAULT_DAYS = 6;
    /** 百分比计算精度 */
    private static final int PERCENT_SCALE = 2;

    private final UserMapper userMapper;
    private final OrgMapper orgMapper;
    private final RoleMapper roleMapper;

    @Override
    public OverviewUserVo getOverviewUser() {
        OverviewUserVo vo = new OverviewUserVo();

        vo.setUserCount(userMapper.selectCountByQuery(
                QueryWrapper.create().eq(User::getState, true)));

        vo.setCompanyCount(orgMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(Org::getState, true)
                        .eq(Org::getOrgType, OrgTypeEnum.COMPANY.getCode())));

        vo.setDeptCount(orgMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(Org::getState, true)
                        .eq(Org::getOrgType, OrgTypeEnum.DEPT.getCode())));

        vo.setRoleCount(roleMapper.selectCountByQuery(QueryWrapper.create().eq(Role::getState, true)));

        return vo;
    }

    @Override
    public List<TrendVo> getUserTrend(String startDate, String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(DEFAULT_DAYS);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);

        List<Map<String, Object>> rawList = userMapper.countByDayRange(startTime, endTime);

        Map<String, Long> dateCountMap = new java.util.HashMap<>();
        for (Map<String, Object> raw : rawList) {
            String date = String.valueOf(raw.get("date"));
            Long count = Convert.toLong(raw.get("value"));
            dateCountMap.put(date, count);
        }

        List<TrendVo> result = new ArrayList<>();
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        for (int i = 0; i < daysBetween; i++) {
            LocalDate d = start.plusDays(i);
            String key = d.toString();
            TrendVo vo = new TrendVo();
            vo.setDate(key);
            vo.setValue(dateCountMap.getOrDefault(key, 0L));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<RankVo> getOrgRank(int limit) {
        return toRankVoList(orgMapper.rankByUserCount(normalizeLimit(limit)));
    }

    @Override
    public List<RankVo> getRoleRank(int limit) {
        return toRankVoList(roleMapper.rankByUserCount(normalizeLimit(limit)));
    }

    @Override
    public List<DistributionVo> getStatusDistribution() {
        List<Map<String, Object>> rawList = userMapper.countByState();
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = rawList.stream().mapToLong(raw -> Convert.toLong(raw.get("count"))).sum();
        return rawList.stream().map(raw -> {
            DistributionVo vo = new DistributionVo();
            vo.setName(convertUserStatus(toBoolean(raw.get("code"))));
            long count = Convert.toLong(raw.get("count"));
            vo.setCount(count);
            vo.setPercent(calcPercent(count, total));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DistributionVo> getTypeDistribution() {
        List<Map<String, Object>> rawList = userMapper.countByType();
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = rawList.stream().mapToLong(raw -> Convert.toLong(raw.get("count"))).sum();
        return rawList.stream().map(raw -> {
            DistributionVo vo = new DistributionVo();
            vo.setName(convertUserType(Convert.toLong(raw.get("code"))));
            long count = Convert.toLong(raw.get("count"));
            vo.setCount(count);
            vo.setPercent(calcPercent(count, total));
            return vo;
        }).collect(Collectors.toList());
    }

    /** 归一化分页大小 */
    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    /** 计算百分比 */
    private double calcPercent(long count, long total) {
        if (total <= 0) {
            return 0d;
        }
        return BigDecimal.valueOf(count)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), PERCENT_SCALE, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /** 转换为排行列表 */
    private List<RankVo> toRankVoList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        return rawList.stream().map(raw -> {
            RankVo vo = new RankVo();
            vo.setName(Convert.toStr(raw.get("name")));
            vo.setValue(Convert.toLong(raw.get("value")));
            return vo;
        }).collect(Collectors.toList());
    }

    private String convertUserStatus(Boolean enabled) {
        if (enabled == null) {
            return null;
        }
        return enabled ? StateEnum.ENABLE.getDesc() : StateEnum.DISABLE.getDesc();
    }

    private String convertUserType(Long code) {
        if (code == null) {
            return null;
        }
        for (UserTypeEnum enumVal : UserTypeEnum.values()) {
            if (enumVal.getCode().equals(code.intValue())) {
                return enumVal.getDesc();
            }
        }
        return String.valueOf(code);
    }

    /** Boolean转换，null返回null（区别于Hutool的false） */
    private static Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }
        return Convert.toBool(value);
    }
}
