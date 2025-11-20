package top.mddata.open.admin.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.open.admin.dto.GroupScopeRelDto;
import top.mddata.open.admin.entity.GroupScopeRel;
import top.mddata.open.admin.mapper.GroupScopeRelMapper;
import top.mddata.open.admin.service.GroupScopeRelService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分组拥有的oauth2权限 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GroupScopeRelServiceImpl extends SuperServiceImpl<GroupScopeRelMapper, GroupScopeRel> implements GroupScopeRelService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long groupId, List<Long> scopeIdList) {
        ArgumentAssert.notNull(groupId, "权限分组ID不能为空");
        ArgumentAssert.notEmpty(scopeIdList, "权限ID不能为空");
        boolean cnt = super.remove(QueryWrapper.create()
                .eq(GroupScopeRel::getGroupId, groupId).in(GroupScopeRel::getScopeId, scopeIdList));
        return cnt;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupScopeRel saveDto(Object save) {
        GroupScopeRelDto dto = (GroupScopeRelDto) save;
        Long groupId = dto.getGroupId();
        List<Long> scopeIdList = dto.getScopeIdList();
        if (CollectionUtils.isEmpty(scopeIdList)) {
            return null;
        }
        List<GroupScopeRel> existList = list(QueryWrapper.create().eq(GroupScopeRel::getGroupId, groupId));
        List<Long> existScopeIdList = existList.stream().map(GroupScopeRel::getScopeId).distinct().toList();

        List<GroupScopeRel> saveList = scopeIdList.stream()
                // 已存在的不添加
                .filter(apiId -> !existScopeIdList.contains(apiId))
                .map(apiId -> {
                    GroupScopeRel scopeRel = new GroupScopeRel();
                    scopeRel.setGroupId(groupId);
                    scopeRel.setScopeId(apiId);
                    return scopeRel;
                })
                .collect(Collectors.toList());
        super.saveBatch(saveList);

        return null;
    }
}
