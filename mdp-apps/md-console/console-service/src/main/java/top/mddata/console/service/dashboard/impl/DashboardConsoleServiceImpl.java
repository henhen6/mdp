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
import top.mddata.console.mapper.system.FileMapper;
import top.mddata.console.service.dashboard.DashboardConsoleService;
import top.mddata.console.vo.dashboard.OverviewConsoleVo;
import top.mddata.workbench.facade.NoticeFacade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
    private final NoticeFacade noticeFacade;

    @Override
    public OverviewConsoleVo getOverviewConsole() {
        OverviewConsoleVo vo = new OverviewConsoleVo();

        // 今日开始时间（用于通知统计）
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // 统计启用状态的用户总数（MyBatis-Flex 自动过滤已删除数据）
        vo.setUserCount(userMapper.selectCountByQuery(
                QueryWrapper.create().eq(User::getState, true)));

        // 统计启用状态的组织总数（MyBatis-Flex 自动过滤已删除数据）
        vo.setOrgCount(orgMapper.selectCountByQuery(
                QueryWrapper.create().eq(Org::getState, true)));

        // 统计今日通知数 (按分类) - 通过 RPC 调用 workbench 模块
        vo.setTodoNoticeCount(noticeFacade.countByCategory(todayStart, MsgCategoryEnum.TO_DO.getCode()));
        vo.setWarningNoticeCount(noticeFacade.countByCategory(todayStart, MsgCategoryEnum.EARLY_WARNING.getCode()));
        vo.setAnnouncementNoticeCount(noticeFacade.countByCategory(todayStart, MsgCategoryEnum.NOTICE.getCode()));

        // 统计文件总数（mdc_file 表无逻辑删除字段）
        vo.setFileCount(fileMapper.selectCountByQuery(QueryWrapper.create()));

        // 统计文件总容量 - 使用 SQL 聚合（手写SQL，mdc_file 表无 deleted_at 字段）
        Long totalSize = fileMapper.sumFileSize();
        vo.setFileTotalSize(totalSize != null ? totalSize : 0L);

        return vo;
    }
}