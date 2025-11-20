package top.mddata.open.admin.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.open.admin.dto.GroupApiRelDto;
import top.mddata.open.admin.entity.GroupApiRel;
import top.mddata.open.admin.mapper.GroupApiRelMapper;
import top.mddata.open.admin.service.GroupApiRelService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分组拥有的对外接口 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GroupApiRelServiceImpl extends SuperServiceImpl<GroupApiRelMapper, GroupApiRel> implements GroupApiRelService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GroupApiRel saveDto(Object save) {
        GroupApiRelDto dto = (GroupApiRelDto) save;
        Long groupId = dto.getGroupId();
        List<Long> apiIdList = dto.getApiIdList();
        if (CollectionUtils.isEmpty(apiIdList)) {
            return null;
        }

        List<GroupApiRel> existList = list(QueryWrapper.create().eq(GroupApiRel::getGroupId, groupId));
        List<Long> existApiIdList = existList.stream().map(GroupApiRel::getApiId).distinct().toList();

        List<GroupApiRel> saveList = apiIdList.stream()
                // 已存在的不添加
                .filter(apiId -> !existApiIdList.contains(apiId))
                .map(apiId -> {
                    GroupApiRel apiRel = new GroupApiRel();
                    apiRel.setGroupId(groupId);
                    apiRel.setApiId(apiId);
                    return apiRel;
                })
                .collect(Collectors.toList());
        super.saveBatch(saveList);

        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long groupId, List<Long> apiIdList) {
        ArgumentAssert.notNull(groupId, "权限分组ID不能为空");
        ArgumentAssert.notEmpty(apiIdList, "接口ID不能为空");
        boolean cnt = super.remove(QueryWrapper.create()
                .eq(GroupApiRel::getGroupId, groupId).in(GroupApiRel::getApiId, apiIdList));
        return cnt;
    }
}
