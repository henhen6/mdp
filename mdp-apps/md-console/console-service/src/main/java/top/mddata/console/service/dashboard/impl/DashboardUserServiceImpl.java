package top.mddata.console.service.dashboard.impl;

import cn.hutool.core.convert.Convert;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.utils.DefValueHelper;
import top.mddata.common.entity.Org;
import top.mddata.common.enumeration.Sex;
import top.mddata.common.enumeration.StateEnum;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.organization.OrgTypeEnum;
import top.mddata.common.mapper.OrgMapper;
import top.mddata.common.mapper.UserMapper;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.mapper.permission.RoleMapper;
import top.mddata.console.service.dashboard.DashboardUserService;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.OverviewUserVo;
import top.mddata.console.vo.dashboard.RankVo;
import top.mddata.console.vo.dashboard.TrendChartVo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户与组织统计 服务层实现
 *
 * <p>所有统计均按当前登录人顶级公司的组织性质做数据域隔离：
 * 运营=全平台；总公司/开发者=其顶级公司 tree_path 子树范围。</p>
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardUserServiceImpl implements DashboardUserService {

    /** 默认日期范围：最近30天（含今天），趋势与性别分布共用 */
    private static final int DEFAULT_DAYS = 29;
    /** 默认分页大小 */
    private static final int DEFAULT_LIMIT = 10;
    /** 最大分页大小 */
    private static final int MAX_LIMIT = 100;

    private final UserMapper userMapper;
    private final OrgMapper orgMapper;
    private final RoleMapper roleMapper;

    /** Boolean转换，null返回null（区别于Hutool的false） */
    private static Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }
        return Convert.toBool(value);
    }

    @Override
    public OverviewUserVo getOverviewUser() {
        OrgScope scope = resolveScope();
        OverviewUserVo vo = new OverviewUserVo();
        vo.setNature(scope.nature());

        vo.setUserCount(userMapper.countUsersInScope(scope.treePathPrefix()));

        vo.setCompanyCount(orgMapper.selectCountByQuery(baseOrgWrapper(scope)
                .eq(Org::getOrgType, OrgTypeEnum.COMPANY.getCode())));
        vo.setDeptCount(orgMapper.selectCountByQuery(baseOrgWrapper(scope)
                .eq(Org::getOrgType, OrgTypeEnum.DEPT.getCode())));

        QueryWrapper roleQuery = QueryWrapper.create()
                .eq(Role::getState, true)
                .eq(Role::getDeletedAt, 0L);
        if (!scope.all()) {
            roleQuery.eq(Role::getOrgNature, scope.nature());
        }
        vo.setRoleCount(roleMapper.selectCountByQuery(roleQuery));

        return vo;
    }

    @Override
    public TrendChartVo getUserTrend(LocalDate startDate, LocalDate endDate) {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(DEFAULT_DAYS);

        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);
        OrgScope scope = resolveScope();

        List<String> dates = buildDateList(start, end);
        TrendChartVo chart = new TrendChartVo();
        chart.setDates(dates);
        List<TrendChartVo.Series> seriesList = new ArrayList<>();

        if (scope.all()) {
            // 曲线1：总用户（按用户去重）
            Map<String, Long> totalMap = toDateCountMap(userMapper.countByDayRange(startTime, endTime, null));
            seriesList.add(buildSeries("总用户", dates, totalMap));

            // 曲线2-4：按组织性质（同一性质内按用户去重，跨性质分别计入，与「用户类型分布」同口径）
            // date -> (nature -> count)
            Map<String, Map<Integer, Long>> dateNatureMap = new HashMap<>();
            for (Map<String, Object> raw : userMapper.countByDayRangeGroupByNature(startTime, endTime)) {
                String date = String.valueOf(raw.get("date"));
                Integer nature = Convert.toInt(raw.get("nature"));
                Long count = Convert.toLong(raw.get("value"));
                dateNatureMap.computeIfAbsent(date, k -> new HashMap<>()).put(nature, count);
            }
            List<OrgNatureEnum> natureOrder = List.of(OrgNatureEnum.HEAD_COMPANY, OrgNatureEnum.DEVELOPER, OrgNatureEnum.OPERATIONS);
            for (OrgNatureEnum natureEnum : natureOrder) {
                TrendChartVo.Series series = new TrendChartVo.Series();
                series.setName(natureEnum.getDesc());
                List<Long> data = new ArrayList<>(dates.size());
                for (String date : dates) {
                    data.add(dateNatureMap.getOrDefault(date, Collections.emptyMap())
                            .getOrDefault(natureEnum.getCode(), 0L));
                }
                series.setData(data);
                seriesList.add(series);
            }
        } else {
            Map<String, Long> dateCountMap = toDateCountMap(userMapper.countByDayRange(startTime, endTime, scope.treePathPrefix()));
            seriesList.add(buildSeries("新增用户", dates, dateCountMap));
        }

        chart.setSeries(seriesList);
        return chart;
    }

    @Override
    public List<RankVo> getOrgRank(int limit) {
        OrgScope scope = resolveScope();
        return toRankVoList(orgMapper.rankByUserCount(scope.treePathPrefix(),
                DefValueHelper.normalizeLimit(limit, DEFAULT_LIMIT, MAX_LIMIT)));
    }

    @Override
    public List<RankVo> getRoleRank(int limit) {
        OrgScope scope = resolveScope();
        return toRankVoList(roleMapper.rankByUserCount(scope.all() ? null : scope.nature(),
                DefValueHelper.normalizeLimit(limit, DEFAULT_LIMIT, MAX_LIMIT)));
    }

    @Override
    public List<DistributionVo> getStatusDistribution(LocalDate startDate, LocalDate endDate) {
        TimeRange range = resolveTimeRange(startDate, endDate);
        OrgScope scope = resolveScope();
        List<Map<String, Object>> rawList = userMapper.countByState(range.startTime(), range.endTime(),
                scope.treePathPrefix());
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = rawList.stream().mapToLong(raw -> Convert.toLong(raw.get("count"))).sum();
        return rawList.stream().map(raw -> {
            DistributionVo vo = new DistributionVo();
            vo.setName(convertUserStatus(toBoolean(raw.get("code"))));
            long count = Convert.toLong(raw.get("count"));
            vo.setCount(count);
            vo.setPercent(DefValueHelper.calcPercent(count, total));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DistributionVo> getGenderDistribution(LocalDate startDate, LocalDate endDate) {
        TimeRange range = resolveTimeRange(startDate, endDate);
        OrgScope scope = resolveScope();

        List<Map<String, Object>> rawList = userMapper.countBySex(range.startTime(), range.endTime(),
                scope.treePathPrefix());
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = rawList.stream().mapToLong(raw -> Convert.toLong(raw.get("count"))).sum();
        return rawList.stream().map(raw -> {
            DistributionVo vo = new DistributionVo();
            vo.setName(convertSex(Convert.toStr(raw.get("code"))));
            long count = Convert.toLong(raw.get("count"));
            vo.setCount(count);
            vo.setPercent(DefValueHelper.calcPercent(count, total));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DistributionVo> getTypeDistribution(LocalDate startDate, LocalDate endDate) {
        // 类型分布为全平台口径，仅运营性质可见，其他性质直接返回空（前端也不展示该卡片）
        if (!resolveScope().all()) {
            return Collections.emptyList();
        }
        TimeRange range = resolveTimeRange(startDate, endDate);
        List<Map<String, Object>> rawList = userMapper.countByNature(range.startTime(), range.endTime());
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = rawList.stream().mapToLong(raw -> Convert.toLong(raw.get("count"))).sum();
        return rawList.stream().map(raw -> {
            DistributionVo vo = new DistributionVo();
            vo.setName(convertNature(Convert.toLong(raw.get("code"))));
            long count = Convert.toLong(raw.get("count"));
            vo.setCount(count);
            vo.setPercent(DefValueHelper.calcPercent(count, total));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 解析分布统计的日期区间：两个日期都为 null 表示全部时间（不加日期过滤）；
     * 只给一个时按默认天数推导另一个
     */
    private TimeRange resolveTimeRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return new TimeRange(null, null);
        }
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(DEFAULT_DAYS);
        return new TimeRange(start.atStartOfDay(), end.atTime(LocalTime.MAX));
    }

    /**
     * 解析当前登录人的统计数据域：运营=全量；其他性质=顶级公司 treePath 子树
     */
    /**
     * 解析当前登录人的统计数据域：运营=全量；其他性质=顶级公司 treePath 子树。
     * 包级静态，供同包的概览统计服务复用
     */
    static OrgScope resolveScope() {
        Integer nature = ContextUtil.getCurrentTopCompanyNature();
        if (nature == null || OrgNatureEnum.OPERATIONS.getCode().equals(nature)) {
            // nature 缺失（老 token/内部调用）时按全量处理，保持向后兼容
            return new OrgScope(true, nature, null, null);
        }
        Long topCompanyId = ContextUtil.getCurrentTopCompanyId();
        // topCompanyId 缺失时兜底为「查不到任何数据」，避免数据域意外放大
        String prefix = topCompanyId == null ? "/-1/%" : "/" + topCompanyId + "/%";
        return new OrgScope(false, nature, topCompanyId, prefix);
    }

    /** 生成日期区间内的连续日期列表（yyyy-MM-dd） */
    private List<String> buildDateList(LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        List<String> dates = new ArrayList<>((int) days);
        for (int i = 0; i < days; i++) {
            dates.add(start.plusDays(i).toString());
        }
        return dates;
    }

    /** 查询结果转 date -> count 映射 */
    private Map<String, Long> toDateCountMap(List<Map<String, Object>> rawList) {
        Map<String, Long> dateCountMap = new HashMap<>();
        for (Map<String, Object> raw : rawList) {
            dateCountMap.put(String.valueOf(raw.get("date")), Convert.toLong(raw.get("value")));
        }
        return dateCountMap;
    }

    /** 按日期列表构建曲线（缺日期补 0） */
    private TrendChartVo.Series buildSeries(String name, List<String> dates, Map<String, Long> dateCountMap) {
        TrendChartVo.Series series = new TrendChartVo.Series();
        series.setName(name);
        List<Long> data = new ArrayList<>(dates.size());
        for (String date : dates) {
            data.add(dateCountMap.getOrDefault(date, 0L));
        }
        series.setData(data);
        return series;
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

    private String convertNature(Long code) {
        if (code == null) {
            return null;
        }
        for (OrgNatureEnum enumVal : OrgNatureEnum.values()) {
            if (enumVal.getCode().equals(code.intValue())) {
                return enumVal.getDesc();
            }
        }
        return String.valueOf(code);
    }

    /** Sex.get() 按枚举名匹配而非 code，这里按 code 手写映射 */
    private String convertSex(String code) {
        if (Sex.M.getCode().equals(code)) {
            return Sex.M.getDesc();
        }
        if (Sex.W.getCode().equals(code)) {
            return Sex.W.getDesc();
        }
        return "未知";
    }

    /**
     * 统计数据域
     *
     * @param all            是否全量（运营）
     * @param nature         当前登录人顶级公司组织性质
     * @param topCompanyId   顶级公司ID
     * @param treePathPrefix 组织树路径前缀（全量时为 null）
     */
    /**
     * 组织统计的基础查询条件：启用、未删除，限定范围时按顶级公司 treePath 过滤。
     * QueryWrapper 可变，共享后追加条件会互相污染，必须每次新建
     * （且不能用 QueryWrapper.create(已有wrapper)——该重载把入参当实体建 FROM）。
     */
    static QueryWrapper baseOrgWrapper(OrgScope scope) {
        QueryWrapper wrapper = QueryWrapper.create()
                .from(Org.class)
                .eq(Org::getState, true)
                .eq(Org::getDeletedAt, 0L);
        if (!scope.all()) {
            // like 为 contains 语义（%/id/%），顶级公司自身及其子孙节点的 treePath 均包含 /id/ 片段
            wrapper.like(Org::getTreePath, "/" + scope.topCompanyId() + "/");
        }
        return wrapper;
    }

    record OrgScope(boolean all, Integer nature, Long topCompanyId, String treePathPrefix) {
    }

    /**
     * 日期区间（两端均可为 null，表示不限）
     *
     * @param startTime 开始日期时间（包含）
     * @param endTime   截止日期时间（包含）
     */
    record TimeRange(LocalDateTime startTime, LocalDateTime endTime) {
    }
}
