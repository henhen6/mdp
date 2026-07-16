package top.mddata.console.service.dashboard.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.User;
import top.mddata.common.enumeration.organization.OrgTypeEnum;
import top.mddata.common.enumeration.organization.UserTypeEnum;
import top.mddata.common.enumeration.StateEnum;
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
import java.util.Map;

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

    private final UserMapper userMapper;
    private final OrgMapper orgMapper;
    private final RoleMapper roleMapper;

    @Override
    public OverviewUserVo getOverviewUser() {
        OverviewUserVo vo = new OverviewUserVo();

        // 启用状态用户总数（MyBatis-Flex 自动过滤已删除数据）
        vo.setUserCount(userMapper.selectCountByQuery(
                QueryWrapper.create().eq(User::getState, true)));

        // 单位数量（org_type=10）
        vo.setCompanyCount(orgMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(Org::getState, true)
                        .eq(Org::getOrgType, OrgTypeEnum.COMPANY.getCode())));

        // 部门数量（org_type=20）
        vo.setDeptCount(orgMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(Org::getState, true)
                        .eq(Org::getOrgType, OrgTypeEnum.DEPT.getCode())));

        // 启用状态角色总数
        vo.setRoleCount(roleMapper.selectCountByQuery(QueryWrapper.create().eq(Role::getState, true)));

        return vo;
    }

    @Override
    public List<TrendVo> getUserTrend(int days) {
        int safeDays = (days != 7 && days != 30) ? 7 : days;
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now().minusDays(safeDays - 1L), LocalTime.MIN);

        List<Map<String, Object>> rawList = userMapper.countByDay(startTime);

        // 用 Map 缓存查询结果，便于补全缺失日期
        Map<String, Long> dateCountMap = new java.util.HashMap<>();
        for (Map<String, Object> raw : rawList) {
            String date = String.valueOf(raw.get("date"));
            Long count = toLong(raw.get("value"));
            dateCountMap.put(date, count);
        }

        // 按日期连续生成区间（包含今天），缺失日期补 0
        List<TrendVo> result = new ArrayList<>(safeDays);
        LocalDate today = LocalDate.now();
        for (int i = safeDays - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
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
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 100);
        return toRankVoList(orgMapper.rankByUserCount(safeLimit));
    }

    @Override
    public List<RankVo> getRoleRank(int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 100);
        return toRankVoList(roleMapper.rankByUserCount(safeLimit));
    }

    @Override
    public List<DistributionVo> getStatusDistribution() {
        List<Map<String, Object>> rawList = userMapper.countByState();
        return toStatusDistributionList(rawList);
    }

    @Override
    public List<DistributionVo> getTypeDistribution() {
        List<Map<String, Object>> rawList = userMapper.countByType();
        return toTypeDistributionList(rawList);
    }

    private List<DistributionVo> toStatusDistributionList(List<Map<String, Object>> rawList) {
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
            vo.setName(convertUserStatus(toLong(raw.get("code"))));
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

    private String convertUserStatus(Long code) {
        if (code == null) {
            return null;
        }
        return StateEnum.of(code.intValue()).getDesc();
    }

    private List<DistributionVo> toTypeDistributionList(List<Map<String, Object>> rawList) {
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
            vo.setName(convertUserType(toLong(raw.get("code"))));
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

    private List<RankVo> toRankVoList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        List<RankVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            RankVo vo = new RankVo();
            vo.setName(toStr(raw.get("name")));
            vo.setValue(toLong(raw.get("value")));
            result.add(vo);
        }
        return result;
    }

    private List<DistributionVo> toDistributionVoList(List<Map<String, Object>> rawList) {
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
