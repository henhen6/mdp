package top.mddata.console.service.dashboard.impl;

import cn.hutool.core.convert.Convert;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.utils.DefValueHelper;
import top.mddata.common.constant.FileObjectType;
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
        LocalDateTime monthStart = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime monthEnd = today.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        // 统计启用状态的用户总数
        vo.setUserCount(userMapper.selectCountByQuery(
                QueryWrapper.create().eq(User::getState, true)));

        // 统计本月新增用户数
        Long userNewCount = userMapper.countNewUsersInMonth(monthStart, monthEnd);
        vo.setUserNewCount(DefValueHelper.nvl(userNewCount, 0L));

        // 统计启用状态的组织总数
        vo.setOrgCount(orgMapper.selectCountByQuery(
                QueryWrapper.create().eq(Org::getState, true)));

        // 统计文件总数
        vo.setFileCount(fileMapper.selectCountByQuery(QueryWrapper.create()));

        // 统计文件总容量
        Long totalSize = fileMapper.sumFileSize();
        vo.setFileTotalSize(DefValueHelper.nvl(totalSize, 0L));

        // 统计今日通知数
        vo.setTodoNoticeCount(noticeFacade.countByCategory(todayStart, MsgCategoryEnum.TO_DO.getCode()));
        vo.setWarningNoticeCount(noticeFacade.countByCategory(todayStart, MsgCategoryEnum.EARLY_WARNING.getCode()));
        vo.setAnnouncementNoticeCount(noticeFacade.countByCategory(todayStart, MsgCategoryEnum.NOTICE.getCode()));

        // 统计站内通知未读数
        Long unreadCount = noticeFacade.countUnread();
        vo.setUnreadNoticeCount(DefValueHelper.nvl(unreadCount, 0L));

        // 统计临时文件占用率
        vo.setTempFileRate(calcTempFileRate(totalSize));

        // 统计消息成功率
        vo.setMessageSuccessRate(calcMessageSuccessRate());

        // 统计接口成功率
        vo.setInterfaceSuccessRate(calcInterfaceSuccessRate());

        // 通过 RPC 调用 open 模块获取回调、API调用、事件通知成功率
        calcOpenSuccessRates(vo);

        return vo;
    }

    /** 计算临时文件占用率 */
    private BigDecimal calcTempFileRate(Long totalSize) {
        Map<String, Object> tempFileStat = fileMapper.statByObjectType(FileObjectType.TEMP_OBJECT_TYPE);
        if (tempFileStat == null || totalSize == null || totalSize <= 0) {
            return BigDecimal.ZERO;
        }
        Long tempFileSize = Convert.toLong(tempFileStat.get("totalSize"));
        return DefValueHelper.calcPercent(tempFileSize, totalSize);
    }

    /** 计算消息成功率 */
    private BigDecimal calcMessageSuccessRate() {
        Map<String, Object> msgStat = msgTaskMapper.statSuccessRate();
        if (msgStat == null) {
            return BigDecimal.ZERO;
        }
        long successCount = Convert.toLong(msgStat.get("successCount"));
        long totalCount = Convert.toLong(msgStat.get("totalCount"));
        return DefValueHelper.calcPercent(successCount, totalCount);
    }

    /** 计算接口成功率 */
    private BigDecimal calcInterfaceSuccessRate() {
        Map<String, Object> interfaceStat = interfaceLogMapper.sumAll();
        if (interfaceStat == null) {
            return BigDecimal.ZERO;
        }
        long successCount = Convert.toLong(interfaceStat.get("successCount"));
        long totalCount = Convert.toLong(interfaceStat.get("totalCount"));
        return DefValueHelper.calcPercent(successCount, totalCount);
    }

    /** 计算 open 模块的三个成功率 */
    private void calcOpenSuccessRates(OverviewConsoleVo vo) {
        try {
            Map<String, Map<String, Long>> openRates = dashboardOpenFacade.getSuccessRates().getData();
            if (openRates == null) {
                vo.setCallbackSuccessRate(BigDecimal.ZERO);
                vo.setApiCallSuccessRate(BigDecimal.ZERO);
                vo.setEventPushSuccessRate(BigDecimal.ZERO);
                return;
            }
            vo.setCallbackSuccessRate(calcSuccessRate(openRates.get("callback")));
            vo.setApiCallSuccessRate(calcSuccessRate(openRates.get("apiCall")));
            vo.setEventPushSuccessRate(calcSuccessRate(openRates.get("eventPush")));
        } catch (Exception e) {
            log.error("调用open模块获取成功率统计失败", e);
            vo.setCallbackSuccessRate(BigDecimal.ZERO);
            vo.setApiCallSuccessRate(BigDecimal.ZERO);
            vo.setEventPushSuccessRate(BigDecimal.ZERO);
        }
    }

    /** 从 Map 中计算成功率 */
    private BigDecimal calcSuccessRate(Map<String, Long> rateMap) {
        if (rateMap == null || rateMap.get("totalCount") == null || rateMap.get("totalCount") <= 0) {
            return BigDecimal.ZERO;
        }
        return DefValueHelper.calcPercent(rateMap.get("successCount"), rateMap.get("totalCount"));
    }
}
