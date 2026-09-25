package top.mddata.console.service.organization.accountop;

/**
 * 账号管理操作策略。
 *
 * <p>每个策略只回答一个问题："我这类操作人，能否对该目标执行该操作"。
 * 通用规则（自身目标、ops_admin 目标）由 AccountOperationGuard 前置拦截，不进入策略。
 * 策略按 Spring @Order 排序，门面取首个 supports=true 的策略执行 check。</p>
 */
public interface AccountOperationStrategy {

    /**
     * 本策略是否适用于该操作人
     *
     * @param operatorId 操作人id
     * @return true=适用
     */
    boolean supports(Long operatorId);

    /**
     * 校验操作人能否对目标执行操作，无权则 fail fast 抛 ArgumentException
     *
     * @param op     操作
     * @param target 目标账号画像
     */
    void check(AccountOperation op, TargetAccountProfile target);
}
