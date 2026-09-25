package top.mddata.console.service.organization.accountop;

/**
 * 账号管理操作门面：禁用/启用/重置密码/删除的权限矩阵统一入口。
 *
 * <p>权限矩阵见 docs/用户体系/账号禁用与重置密码权限矩阵设计.md。
 * 前端按钮隐藏仅体验优化，真正的保护在这里。
 * 本守护仅覆盖人工操作入口（用户管理的编辑、重置密码、删除），
 * 内部任务直接操作数据库不受本守护限制。</p>
 */
public interface AccountOperationGuard {

    /**
     * 校验当前登录人能否对目标账号执行操作，无权则 fail fast 抛 ArgumentException
     *
     * @param targetId 目标账号id
     * @param op       操作
     */
    void check(Long targetId, AccountOperation op);
}
