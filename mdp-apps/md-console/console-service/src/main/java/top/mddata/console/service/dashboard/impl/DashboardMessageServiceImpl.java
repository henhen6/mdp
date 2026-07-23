package top.mddata.console.service.dashboard.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.console.entity.message.MsgTask;
import top.mddata.console.enumeration.message.MsgCategoryEnum;
import top.mddata.console.enumeration.message.MsgTaskStatusEnum;
import top.mddata.console.enumeration.message.MsgTypeEnum;
import top.mddata.console.mapper.message.MsgTaskMapper;
import top.mddata.console.service.dashboard.DashboardMessageService;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.OverviewMessageVo;
import top.mddata.console.vo.dashboard.RankVo;
import top.mddata.console.vo.dashboard.TrendLineVo;

import top.mddata.base.utils.DefValueHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import cn.hutool.core.convert.Convert;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息通知统计 服务层实现
 *
 * <p>说明：mdc_msg_task 表没有 deleted_at 字段，
 * MyBatis-Flex 不会自动追加删除过滤条件。
 * 手写 SQL 也无需处理 deleted_at。</p>
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardMessageServiceImpl implements DashboardMessageService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /** 默认分页大小 */
    private static final int DEFAULT_LIMIT = 10;
    /** 最大分页大小 */
    private static final int MAX_LIMIT = 100;
    /** 默认日期范围 */
    private static final int DEFAULT_DAYS = 6;

    private final MsgTaskMapper msgTaskMapper;

    @Override
    public OverviewMessageVo getOverviewMessage() {
        OverviewMessageVo vo = new OverviewMessageVo();

        vo.setMsgCount(msgTaskMapper.selectCountByQuery(QueryWrapper.create()));

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        vo.setTodaySendCount(msgTaskMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(MsgTask::getStatus, MsgTaskStatusEnum.SUCCESS.getCode())
                        .ge(MsgTask::getSendTime, todayStart)));

        vo.setPendingCount(msgTaskMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(MsgTask::getStatus, MsgTaskStatusEnum.WAITING.getCode())));

        vo.setDraftCount(msgTaskMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(MsgTask::getStatus, MsgTaskStatusEnum.DRAFT.getCode())));

        vo.setSuccessCount(msgTaskMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(MsgTask::getStatus, MsgTaskStatusEnum.SUCCESS.getCode())));

        vo.setFailCount(msgTaskMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(MsgTask::getStatus, MsgTaskStatusEnum.FAIL.getCode())));

        return vo;
    }

    @Override
    public List<DistributionVo> getTypeDistribution() {
        List<Map<String, Object>> rawList = msgTaskMapper.countByType();
        return convertDistributionList(rawList, code -> convertMsgType(Convert.toLong(code)));
    }

    @Override
    public List<DistributionVo> getCategoryDistribution() {
        List<Map<String, Object>> rawList = msgTaskMapper.countByCategoryLocal();
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> translated = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            Long category = Convert.toLong(raw.get("code"));
            Map<String, Object> row = new HashMap<>(3);
            row.put("name", convertMsgCategory(category));
            row.put("count", raw.get("count"));
            translated.add(row);
        }
        return toDistributionList(translated);
    }

    @Override
    public List<TrendLineVo> getTrend(LocalDate startDate, LocalDate endDate, Integer type) {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(DEFAULT_DAYS);

        LocalDateTime startTime = LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<Map<String, Object>> rawList = msgTaskMapper.countTrendByDayRange(startTime, endTime);
        Map<String, TrendLineVo> dateMap = new HashMap<>();
        for (Map<String, Object> raw : rawList) {
            String date = Convert.toStr(raw.get("date"));
            TrendLineVo vo = new TrendLineVo();
            vo.setDate(date);
            vo.setNoticeCount(Convert.toLong(raw.get("noticeCount")));
            vo.setSmsCount(Convert.toLong(raw.get("smsCount")));
            vo.setMailCount(Convert.toLong(raw.get("mailCount")));
            vo.setTotalCount(Convert.toLong(raw.get("totalCount")));
            dateMap.put(date, vo);
        }

        List<TrendLineVo> result = new ArrayList<>();
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        for (int i = 0; i < daysBetween; i++) {
            LocalDate d = start.plusDays(i);
            String key = d.toString();
            TrendLineVo vo = dateMap.get(key);
            if (vo == null) {
                vo = new TrendLineVo();
                vo.setDate(key);
                vo.setNoticeCount(0L);
                vo.setSmsCount(0L);
                vo.setMailCount(0L);
                vo.setTotalCount(0L);
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<RankVo> getTemplateRank(int limit) {
        List<Map<String, Object>> rawList = msgTaskMapper.templateRank(DefValueHelper.normalizeLimit(limit, DEFAULT_LIMIT, MAX_LIMIT));
        return toRankList(rawList);
    }

    /** 转换分发列表（带名称转换器） */
    private List<DistributionVo> convertDistributionList(List<Map<String, Object>> rawList,
                                                        java.util.function.Function<Object, String> nameConverter) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = rawList.stream().mapToLong(raw -> Convert.toLong(raw.get("count"))).sum();
        return rawList.stream().map(raw -> {
            DistributionVo vo = new DistributionVo();
            vo.setName(nameConverter.apply(raw.get("code")));
            long count = Convert.toLong(raw.get("count"));
            vo.setCount(count);
            vo.setPercent(DefValueHelper.calcPercent(count, total));
            return vo;
        }).collect(Collectors.toList());
    }

    /** 转换为分发列表（无转换器，直接用name字段） */
    private List<DistributionVo> toDistributionList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = rawList.stream().mapToLong(raw -> Convert.toLong(raw.get("count"))).sum();
        return rawList.stream().map(raw -> {
            DistributionVo vo = new DistributionVo();
            vo.setName(Convert.toStr(raw.get("name")));
            long count = Convert.toLong(raw.get("count"));
            vo.setCount(count);
            vo.setPercent(DefValueHelper.calcPercent(count, total));
            return vo;
        }).collect(Collectors.toList());
    }

    /** 转换为排行列表 */
    private List<RankVo> toRankList(List<Map<String, Object>> rawList) {
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

    private String convertMsgType(Long code) {
        if (code == null) {
            return null;
        }
        for (MsgTypeEnum enumVal : MsgTypeEnum.values()) {
            if (enumVal.getCode().equals(code.intValue())) {
                return enumVal.getDesc();
            }
        }
        return "其他-" + code;
    }

    private String convertMsgCategory(Long code) {
        if (code == null) {
            return "未知";
        }
        for (MsgCategoryEnum enumVal : MsgCategoryEnum.values()) {
            if (enumVal.getCode().equals(code.intValue())) {
                return enumVal.getDesc();
            }
        }
        return "其他-" + code;
    }
}
