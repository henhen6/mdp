package top.mddata.workbench.service.dashboard.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.workbench.entity.LoginLog;
import top.mddata.workbench.mapper.LoginLogMapper;
import top.mddata.workbench.service.dashboard.DashboardWorkbenchService;
import top.mddata.workbench.vo.dashboard.OverviewWorkbenchVo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 系统概览统计 服务层实现 (workbench部分)
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardWorkbenchServiceImpl implements DashboardWorkbenchService {

    /** loginDate 字段格式：yyyy-MM-dd */
    private static final DateTimeFormatter LOGIN_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final LoginLogMapper loginLogMapper;

    @Override
    public OverviewWorkbenchVo getOverviewWorkbench() {
        OverviewWorkbenchVo vo = new OverviewWorkbenchVo();

        // 统计今日登录次数 (登录成功)
        // loginDate 字段是 char(10) 类型，格式 yyyy-MM-dd
        String today = LocalDate.now().format(LOGIN_DATE_FORMAT);
        QueryWrapper wrapper = QueryWrapper.create()
                .eq(LoginLog::getLoginDate, today)
                .eq(LoginLog::getStatus, "01");
        vo.setTodayLoginCount(loginLogMapper.selectCountByQuery(wrapper));

        return vo;
    }
}