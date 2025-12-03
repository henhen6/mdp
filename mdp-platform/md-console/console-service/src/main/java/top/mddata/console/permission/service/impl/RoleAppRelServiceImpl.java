package top.mddata.console.permission.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.console.permission.dto.RoleAppRelDto;
import top.mddata.console.permission.entity.RoleAppRel;
import top.mddata.console.permission.mapper.RoleAppRelMapper;
import top.mddata.console.permission.service.RoleAppRelService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色应用关联 服务层实现。
 *
 * @author henhen6
 * @since 2025-12-03 14:54:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoleAppRelServiceImpl extends SuperServiceImpl<RoleAppRelMapper, RoleAppRel> implements RoleAppRelService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(RoleAppRelDto dto) {
        return super.remove(QueryWrapper.create()
                .eq(RoleAppRel::getRoleId, dto.getRoleId()).in(RoleAppRel::getAppId, dto.getAppId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveByDto(RoleAppRelDto dto) {
        List<RoleAppRel> existList = list(QueryWrapper.create().eq(RoleAppRel::getRoleId, dto.getRoleId()));
        List<Long> existAppIdList = existList.stream().map(RoleAppRel::getAppId).distinct().toList();

        List<RoleAppRel> saveList = dto.getAppId().stream()
                // 已存在的不添加
                .filter(appId -> !existAppIdList.contains(appId))
                .map(appId -> {
                    RoleAppRel scopeRel = new RoleAppRel();
                    scopeRel.setRoleId(dto.getRoleId());
                    scopeRel.setAppId(appId);
                    return scopeRel;
                })
                .collect(Collectors.toList());

        return super.saveBatch(saveList);
    }
}
