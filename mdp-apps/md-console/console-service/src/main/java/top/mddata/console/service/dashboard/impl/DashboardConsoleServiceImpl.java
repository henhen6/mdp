package top.mddata.console.service.dashboard.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.User;
import top.mddata.common.mapper.OrgMapper;
import top.mddata.common.mapper.UserMapper;
import top.mddata.console.enumeration.message.MsgCategoryEnum;
import top.mddata.console.mapper.message.InterfaceLogMapper;
import top.mddata.console.mapper.message.MsgTaskMapper;
import top.mddata.console.mapper.system.FileMapper;
import top.mddata.console.service.dashboard.DashboardConsoleService;
import top.mddata.console.vo.dashboard.OverviewConsoleVo;
import top.mddata.open.facade.admin.DashboardOpenFacade;
import top.mddata.workbench.facade.NoticeFacade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

/**
 * 系统概览统计 服务层实现
 *
 * <p>说明：MyBatis-Flex 内置处理 deletedAt 逻辑删除字段，
 * 无需在 QueryWrapper 中手动添加 .eq(Xxx::getDeletedAt, 0L)。</p>
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardConsoleServiceImpl implements DashboardConsoleService {

    private final UserMapper userMapper;
    private final OrgMapper orgMapper;
    private final FileMapper fileMapper;
    private final MsgTaskMapper msgTaskMapper;
    private final InterfaceLogMapper interfaceLogMapper;
    private final NoticeFacade noticeFacade;
    private final DashboardOpenFacade dashboardOpenFacade;

    @Override
    public OverviewConsoleVo getOverviewConsole() {
        OverviewConsoleVo vo = new OverviewConsoleVo();

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        // 本月开始和结束时间
        LocalDateTime monthStart = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime monthEnd = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        // 统计启用状态的用户总数（MyBatis-Flex 自动过滤已删除数据）
        vo.setUserCount(userMapper.selectCountByQuery(
                QueryWrapper.create().eq(User::getState, true)));

        // 统计本月新增用户数
        Long userNewCount = userMapper.countNewUsersInMonth(monthStart, monthEnd);
        vo.setUserNewCount(userNewCount != null ? userNewCount : 0L);

        // 统计启用状态的组织总数（MyBatis-Flex 自动过滤已删除数据）
        vo.setOrgCount(orgMapper.selectCountByQuery(
                QueryWrapper.create().eq(Org::getState, true)));

        // 统计文件总数（mdc_file 表无逻辑删除字段）
        vo.setFileCount(fileMapper.selectCountByQuery(QueryWrapper.create()));

        // 统计文件总容量 - 使用 SQL 聚合（手写SQL，mdc_file 表无 deleted_at 字段）
        Long totalSize = fileMapper.sumFileSize();
        vo.setFileTotalSize(totalSize != null ? totalSize : 0L);

        // 统计今日通知数 (按分类) - 通过 RPC 调用 workbench 模块
        vo.setTodoNoticeCount(noticeFacade.countByCategory(todayStart, MsgCategoryEnum.TO_DO.getCode()));
        vo.setWarningNoticeCount(noticeFacade.countByCategory(todayStart, MsgCategoryEnum.EARLY_WARNING.getCode()));
        vo.setAnnouncementNoticeCount(noticeFacade.countByCategory(todayStart, MsgCategoryEnum.NOTICE.getCode()));

        // 统计站内通知未读数 - 通过 RPC 调用 workbench 模块
        Long unreadCount = noticeFacade.countUnread();
        vo.setUnreadNoticeCount(unreadCount != null ? unreadCount : 0L);

        // 统计临时文件占用率
        Map<String, Object> tempFileStat = fileMapper.statByObjectType("temp");
        if (tempFileStat != null && totalSize != null && totalSize > 0) {
            Long tempFileSize = toLong(tempFileStat.get("totalSize"));
            double rate = BigDecimal.valueOf(tempFileSize)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalSize), 2, RoundingMode.HALF_UP)
                    .doubleValue();
            vo.setTempFileRate(rate);
        } else {
            vo.setTempFileRate(0d);
        }

        // 统计消息成功率
        Map<String, Object> msgStat = msgTaskMapper.statSuccessRate();
        if (msgStat != null) {
            Long successCount = toLong(msgStat.get("successCount"));
            Long totalCount = toLong(msgStat.get("totalCount"));
            if (totalCount > 0) {
                double rate = BigDecimal.valueOf(successCount)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP)
                        .doubleValue();
                vo.setMessageSuccessRate(rate);
            } else {
                vo.setMessageSuccessRate(0d);
            }
        } else {
            vo.setMessageSuccessRate(0d);
        }

        // 统计接口成功率（mdc_interface_log 表，status = 2）
        Map<String, Object> interfaceStat = interfaceLogMapper.sumAll();
        if (interfaceStat != null) {
            Long successCount = toLong(interfaceStat.get("successCount"));
            Long totalCount = toLong(interfaceStat.get("totalCount"));
            if (totalCount > 0) {
                double rate = BigDecimal.valueOf(successCount)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP)
                        .doubleValue();
                vo.setInterfaceSuccessRate(rate);
            } else {
                vo.setInterfaceSuccessRate(0d);
            }
        } else {
            vo.setInterfaceSuccessRate(0d);
        }

        // 通过 RPC 调用 open 模块获取回调、API调用、事件通知成功率
        try {
            Map<String, Map<String, Long>> openRates = dashboardOpenFacade.getSuccessRates().getData();
            if (openRates != null) {
                // 回调成功率
                Map<String, Long> callback = openRates.get("callback");
                if (callback != null && callback.get("totalCount") > 0) {
                    double rate = BigDecimal.valueOf(callback.get("successCount"))
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(callback.get("totalCount")), 2, RoundingMode.HALF_UP)
                            .doubleValue();
                    vo.setCallbackSuccessRate(rate);
                } else {
                    vo.setCallbackSuccessRate(0d);
                }

                // API调用成功率
                Map<String, Long> apiCall = openRates.get("apiCall");
                if (apiCall != null && apiCall.get("totalCount") > 0) {
                    double rate = BigDecimal.valueOf(apiCall.get("successCount"))
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(apiCall.get("totalCount")), 2, RoundingMode.HALF_UP)
                            .doubleValue();
                    vo.setApiCallSuccessRate(rate);
                } else {
                    vo.setApiCallSuccessRate(0d);
                }

                // 事件通知成功率
                Map<String, Long> eventPush = openRates.get("eventPush");
                if (eventPush != null && eventPush.get("totalCount") > 0) {
                    double rate = BigDecimal.valueOf(eventPush.get("successCount"))
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(eventPush.get("totalCount")), 2, RoundingMode.HALF_UP)
                            .doubleValue();
                    vo.setEventPushSuccessRate(rate);
                } else {
                    vo.setEventPushSuccessRate(0d);
                }
            } else {
                vo.setCallbackSuccessRate(0d);
                vo.setApiCallSuccessRate(0d);
                vo.setEventPushSuccessRate(0d);
            }
        } catch (Exception e) {
            log.error("调用open模块获取成功率统计失败", e);
            vo.setCallbackSuccessRate(0d);
            vo.setApiCallSuccessRate(0d);
            vo.setEventPushSuccessRate(0d);
        }

        return vo;
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
}
