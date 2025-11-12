package top.mddata.console.organization.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.CollHelper;
import top.mddata.common.entity.Position;
import top.mddata.common.mapper.PositionMapper;
import top.mddata.console.organization.service.PositionService;

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
