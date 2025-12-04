package top.mddata.console.organization.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.mapper.UserRoleRelMapper;
import top.mddata.console.organization.service.UserRoleRelService;

import java.io.Serializable;
import java.util.Collection;

/**
 * 用户角色关联 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 15:50:00
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserRoleRelServiceImpl extends SuperServiceImpl<UserRoleRelMapper, UserRoleRel> implements UserRoleRelService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByRoleIds(Collection<? extends Serializable> roleIdList) {
        if (CollUtil.isEmpty(roleIdList)) {
            return;
        }
        super.remove(QueryWrapper.create().in(UserRoleRel::getRoleId, roleIdList));
    }
}
