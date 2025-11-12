package top.mddata.console.organization.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.common.entity.UserOrgRel;

import java.io.Serializable;
import java.util.Collection;

/**
 * 用户所属组织 服务层。
 *
 * @author henhen6
 * @since 2025-11-12 15:50:00
 */
public interface UserOrgRelService extends SuperService<UserOrgRel> {
    /**
     * 根据用户id删除 用户-组织 表
     * @param userIds 用户id
     * @return 是否成功
     */
    boolean removeByUserIds(Collection<Long> userIds);

    /**
     * 根据 组织id删除 用户-组织 表
     * @param idList 组织id
     * @return 是否成功
     */
    boolean removeByOrgIds(Collection<? extends Serializable> idList);

}
