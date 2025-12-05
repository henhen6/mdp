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
import top.mddata.console.organization.dto.UserRoleRelDto;
import top.mddata.console.organization.service.UserRoleRelService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveByDto(UserRoleRelDto dto) {
        List<UserRoleRel> existList = list(QueryWrapper.create().eq(UserRoleRel::getRoleId, dto.getRoleId()));
        List<Long> existUserIdList = existList.stream().map(UserRoleRel::getUserId).distinct().toList();

        List<UserRoleRel> saveList = dto.getUserIdList().stream()
                // 已存在的不添加
                .filter(userId -> !existUserIdList.contains(userId))
                .map(userId -> {
                    UserRoleRel rel = new UserRoleRel();
                    rel.setRoleId(dto.getRoleId());
                    rel.setUserId(userId);
                    return rel;
                })
                .collect(Collectors.toList());

        return super.saveBatch(saveList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(UserRoleRelDto dto) {
        return super.remove(QueryWrapper.create().eq(UserRoleRel::getRoleId, dto.getRoleId()).in(UserRoleRel::getUserId, dto.getUserIdList()));

    }
}
