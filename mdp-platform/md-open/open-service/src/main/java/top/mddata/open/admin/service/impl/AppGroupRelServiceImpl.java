package top.mddata.open.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.admin.dto.AppGroupRelDto;
import top.mddata.open.admin.entity.AppGroupRel;
import top.mddata.open.admin.entity.GroupApiRel;
import top.mddata.open.admin.mapper.AppGroupRelMapper;
import top.mddata.open.admin.mapper.GroupApiRelMapper;
import top.mddata.open.admin.service.AppGroupRelService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 应用拥有的权限分组 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppGroupRelServiceImpl extends SuperServiceImpl<AppGroupRelMapper, AppGroupRel> implements AppGroupRelService {
    private final AppGroupRelMapper appGroupRelMapper;
    private final GroupApiRelMapper groupApiRelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Long> listGroupIdByAppId(Long appId) {
        List<AppGroupRel> list = list(QueryWrapper.create().eq(AppGroupRel::getAppId, appId));
        return list.stream().map(AppGroupRel::getGroupId).distinct().toList();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppGroupRel saveDto(Object save) {
        AppGroupRelDto dto = (AppGroupRelDto) save;
        remove(QueryWrapper.create().eq(AppGroupRel::getAppId, dto.getAppId()));

        if (CollUtil.isEmpty(dto.getGroupIdList())) {
            return null;
        }

        List<AppGroupRel> list = dto.getGroupIdList().stream()
                .map(groupId -> {
                    AppGroupRel opApplicationGroupRel = new AppGroupRel();
                    opApplicationGroupRel.setAppId(dto.getAppId());
                    opApplicationGroupRel.setGroupId(groupId);
                    return opApplicationGroupRel;
                }).toList();

        saveBatch(list);
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        boolean flag = super.removeByIds(idList);
        appGroupRelMapper.deleteByQuery(QueryWrapper.create().in(AppGroupRel::getGroupId, idList));
        groupApiRelMapper.deleteByQuery(QueryWrapper.create().in(GroupApiRel::getGroupId, idList));
        return flag;
    }
}
