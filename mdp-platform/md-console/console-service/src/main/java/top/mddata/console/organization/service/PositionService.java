package top.mddata.console.organization.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.common.entity.Position;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 岗位 服务层。
 *
 * @author henhen6
 * @since 2025-11-12 15:48:54
 */
public interface PositionService extends SuperService<Position> {
    /**
     * 根据id查询待回显参数
     *
     * @param ids 唯一键（可能不是主键ID)
     * @return 回显数据
     */
    Map<Serializable, Object> findByIds(Set<Serializable> ids);

    /**
     * 修改 状态
     * @param id id
     * @param state 状态
     * @return
     */
    Boolean updateState(Long id, Boolean state);
}
