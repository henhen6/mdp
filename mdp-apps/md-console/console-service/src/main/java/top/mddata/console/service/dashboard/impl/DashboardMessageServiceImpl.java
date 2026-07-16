package top.mddata.console.service.dashboard.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.console.entity.message.MsgTask;
import top.mddata.console.enumeration.message.MsgTypeEnum;
import top.mddata.console.mapper.message.MsgTaskMapper;
import top.mddata.console.service.dashboard.DashboardMessageService;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.OverviewMessageVo;
import top.mddata.console.vo.dashboard.RankVo;
import top.mddata.console.vo.dashboard.TrendVo;
import top.mddata.workbench.facade.NoticeFacade;

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
 * 消息通知统计 服务层实现
 *
 * <p>说明：mdc_msg_task 表没有 deleted_at 字段，
 * MyBatis-Flex 不会自动追加删除过滤条件。
 * 手写 SQL 也无需处理 deleted_at。</p>
 *
 * <p>消息分类分布（mdc_notice 表）通过 NoticeFacade（workbench 模块）远程调用。</p>
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardMessageServiceImpl implements DashboardMessageService {

    /** 消息任务状态：草稿 */
    private static final int MSG_TASK_STATUS_DRAFT = 0;
    /** 消息任务状态：待执行 */
    private static final int MSG_TASK_STATUS_PENDING = 1;
    /** 消息任务状态：执行成功 */
    private static final int MSG_TASK_STATUS_SUCCESS = 2;
    /** 消息任务状态：执行失败 */
    private static final int MSG_TASK_STATUS_FAIL = 3;

    private final MsgTaskMapper msgTaskMapper;
    private final NoticeFacade noticeFacade;

    @Override
    public OverviewMessageVo getOverviewMessage() {
        OverviewMessageVo vo = new OverviewMessageVo();

        // 消息任务总数（mdc_msg_task 没有 deleted_at 字段）
        vo.setMsgCount((long) msgTaskMapper.selectCountByQuery(QueryWrapper.create()));

        // 今日发送消息数（执行成功）
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        vo.setTodaySendCount(msgTaskMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(MsgTask::getStatus, MSG_TASK_STATUS_SUCCESS)
                        .ge(MsgTask::getSendTime, todayStart)));

        // 待执行消息数
        vo.setPendingCount(msgTaskMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(MsgTask::getStatus, MSG_TASK_STATUS_PENDING)));
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
        List<Map<String, Object>> rawList = noticeFacade.countByCategoryDistribution();
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }

        // 转换 msgCategory 数字到中文名
        List<Map<String, Object>> translated = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            Integer category = toInt(raw.get("msgCategory"));
            Map<String, Object> row = new HashMap<>(3);
            row.put("name", categoryName(category));
            row.put("count", raw.get("count"));
            translated.add(row);
        }
        return toDistributionList(translated);
    }

    @Override
    public List<TrendVo> getTrend(int days) {
        int safeDays = (days != 7 && days != 30) ? 7 : days;
        LocalDateTime startTime = LocalDateTime.of(LocalDate.now().minusDays(safeDays - 1L), LocalTime.MIN);

        List<Map<String, Object>> rawList = msgTaskMapper.countByDay(startTime);
        Map<String, Long> dateMap = new HashMap<>();
        for (Map<String, Object> raw : rawList) {
            dateMap.put(toStr(raw.get("date")), toLong(raw.get("value")));
        }

        List<TrendVo> result = new ArrayList<>(safeDays);
        LocalDate today = LocalDate.now();
        for (int i = safeDays - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String key = d.toString();
            TrendVo vo = new TrendVo();
            vo.setDate(key);
            vo.setValue(dateMap.getOrDefault(key, 0L));
            result.add(vo);
        }
        return result;
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
