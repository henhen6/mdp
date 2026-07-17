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

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private final MsgTaskMapper msgTaskMapper;

    @Override
    public OverviewMessageVo getOverviewMessage() {
        OverviewMessageVo vo = new OverviewMessageVo();

        // 消息任务总数
        vo.setMsgCount(msgTaskMapper.selectCountByQuery(QueryWrapper.create()));

        // 今日发送消息数（执行成功）
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        vo.setTodaySendCount(msgTaskMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(MsgTask::getStatus, MsgTaskStatusEnum.SUCCESS.getCode())
                        .ge(MsgTask::getSendTime, todayStart)));

        // 待执行消息数
        vo.setPendingCount(msgTaskMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(MsgTask::getStatus, MsgTaskStatusEnum.WAITING.getCode())));

        // 草稿消息数
        vo.setDraftCount(msgTaskMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(MsgTask::getStatus, MsgTaskStatusEnum.DRAFT.getCode())));

        // 执行成功消息数
        vo.setSuccessCount(msgTaskMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(MsgTask::getStatus, MsgTaskStatusEnum.SUCCESS.getCode())));

        // 执行失败消息数
        vo.setFailCount(msgTaskMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(MsgTask::getStatus, MsgTaskStatusEnum.FAIL.getCode())));

        return vo;
    }

    @Override
    public List<DistributionVo> getTypeDistribution() {
        List<Map<String, Object>> rawList = msgTaskMapper.countByType();
        return toMsgTypeDistributionList(rawList);
    }

    private List<DistributionVo> toMsgTypeDistributionList(List<Map<String, Object>> rawList) {
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
            vo.setName(convertMsgType(toLong(raw.get("code"))));
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

    @Override
    public List<DistributionVo> getCategoryDistribution() {
        List<Map<String, Object>> rawList = msgTaskMapper.countByCategoryLocal();
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }

        // 转换 msgCategory 数字到中文名
        List<Map<String, Object>> translated = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            Long category = toLong(raw.get("code"));
            Map<String, Object> row = new HashMap<>(3);
            row.put("name", convertMsgCategory(category));
            row.put("count", raw.get("count"));
            translated.add(row);
        }
        return toDistributionList(translated);
    }

    @Override
    public List<TrendLineVo> getTrend(String startDate, String endDate, Integer type) {
        // 默认近7天
        LocalDate end = (endDate == null || endDate.isBlank())
                ? LocalDate.now()
                : LocalDate.parse(endDate, DATE_FORMATTER);
        LocalDate start = (startDate == null || startDate.isBlank())
                ? end.minusDays(6)
                : LocalDate.parse(startDate, DATE_FORMATTER);

        LocalDateTime startTime = LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<Map<String, Object>> rawList = msgTaskMapper.countTrendByDayRange(startTime, endTime);
        Map<String, TrendLineVo> dateMap = new HashMap<>();
        for (Map<String, Object> raw : rawList) {
            String date = toStr(raw.get("date"));
            TrendLineVo vo = new TrendLineVo();
            vo.setDate(date);
            vo.setNoticeCount(toLong(raw.get("noticeCount")));
            vo.setSmsCount(toLong(raw.get("smsCount")));
            vo.setMailCount(toLong(raw.get("mailCount")));
            vo.setTotalCount(toLong(raw.get("totalCount")));
            dateMap.put(date, vo);
        }

        // 补全缺失日期
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

    @Override
    public List<RankVo> getTemplateRank(int limit) {
        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 100);
        List<Map<String, Object>> rawList = msgTaskMapper.templateRank(safeLimit);
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

    private List<DistributionVo> toDistributionList(List<Map<String, Object>> rawList) {
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

    private static String categoryName(Integer category) {
        if (category == null) {
            return "未知";
        }
        return switch (category) {
            case 1 -> "待办";
            case 2 -> "公告";
            case 3 -> "预警";
            default -> "其他-" + category;
        };
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
            return null;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String toStr(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
