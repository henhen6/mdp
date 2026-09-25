package top.mddata.console.service.organization.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.CollHelper;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.Position;
import top.mddata.common.mapper.PositionMapper;
import top.mddata.console.dto.organization.PositionDto;
import top.mddata.console.service.organization.OrgService;
import top.mddata.console.service.organization.OrgVisibilityService;
import top.mddata.console.service.organization.PositionService;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 岗位 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 15:48:54
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PositionServiceImpl extends SuperServiceImpl<PositionMapper, Position> implements PositionService {
    private final OrgService orgService;
    private final OrgVisibilityService orgVisibilityService;

    @Override
    protected Position saveBefore(Object save) {
        PositionDto data = (PositionDto) save;
        Position entity = super.saveBefore(data);
        checkOrgVisible(data.getOrgId());
        return entity;
    }

    @Override
    protected Position updateBefore(Object update) {
        PositionDto data = (PositionDto) update;
        Position entity = super.updateBefore(data);
        checkOrgVisible(data.getOrgId());
        return entity;
    }

    /**
     * 校验所属组织在当前操作人可见树内；rootIds 为 null（运营者不限制）时放行。
     *
     * @param orgId 所属组织id
     */
    private void checkOrgVisible(Long orgId) {
        if (orgId == null) {
            return;
        }
        List<Long> rootIds = orgVisibilityService.currentVisibleRootOrgIds();
        if (rootIds == null) {
            return;
        }
        Org org = orgService.getByIdCache(orgId);
        ArgumentAssert.notNull(org, "所属组织[{}]不存在", orgId);
        boolean visible = rootIds.stream().anyMatch(rootId ->
                org.getTreePath() != null && org.getTreePath().startsWith("/" + rootId + "/"));
        ArgumentAssert.isTrue(visible, "无权在组织[{}]下维护岗位", org.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateState(Long id, Boolean state) {
        Position bean = new Position();
        bean.setId(id);
        bean.setState(state);
        return super.updateById(bean);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        Set<Serializable> param = new HashSet<>();
        ids.forEach(item -> {
            if (item instanceof Collection<?> tempItem) {
                List<Long> list = Arrays.stream(Convert.toLongArray(tempItem)).toList();
                param.addAll(list);
            } else {
                param.add(Convert.toLong(item));
            }
        });

        List<Position> list = findByIds(param, null);

        return CollHelper.uniqueIndex(list.stream().filter(Objects::nonNull).toList(), Position::getId, Position::getName);
    }
}
