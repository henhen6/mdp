package top.mddata.common.constant;

import java.util.List;

/**
 * 系统内置用户ID。
 *
 * <p>取值与 docs/mdp.sql 中的内置数据一一对应，
 * 属于跨环境契约，禁止修改。</p>
 */
public interface BuiltInUserId {
    /**
     * 运维管理员（系统最高账号，禁止禁用，密码仅本人可重置）
     */
    long OPS_ADMIN = 680083598598475778L;

    /**
     * 开发者管理员（可管理开发者平台树内的其他账号）
     */
    long OPEN_ADMIN = 680083598598475779L;

    /**
     * 总公司管理员（可管理总公司树内的其他账号）
     */
    long ADMIN = 680083598598475780L;

    /**
     * 所有内置用户ID（均禁止删除）
     */
    List<Long> ALL = List.of(OPS_ADMIN, OPEN_ADMIN, ADMIN);
}
