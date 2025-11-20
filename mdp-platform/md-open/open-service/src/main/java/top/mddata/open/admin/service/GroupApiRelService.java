package top.mddata.open.admin.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.admin.entity.GroupApiRel;

import java.util.List;

/**
 * 分组拥有的对外接口 服务层。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
public interface GroupApiRelService extends SuperService<GroupApiRel> {
    /**
     * 删除分组下的指定开放接口
     * @param groupId 分组
     * @param apiIdList 开放接口ID
     * @return 是否成功
     */
    Boolean delete(Long groupId, List<Long> apiIdList);
}
