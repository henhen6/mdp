package top.mddata.base.mybatisflex.datascope;

import lombok.Data;

import java.util.List;

/**
 * 数据权限当前用户视图（针对某个菜单的授权快照）。
 *
 * @author henhen
 * @since 2026年09月26日
 */
@Data
public class DataScopeCurrentUser {
    /** 用户id（null 表示无登录上下文，系统线程） */
    private Long userId;
    /** 当前公司id（登录上下文） */
    private Long companyId;
    /** 当前部门id（登录上下文） */
    private Long deptId;
    /** 当前用户所有启用角色对目标菜单的授权 */
    private List<DataScopeGrant> grants = List.of();
}
