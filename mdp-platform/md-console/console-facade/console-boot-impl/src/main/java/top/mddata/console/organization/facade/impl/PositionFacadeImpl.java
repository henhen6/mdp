package top.mddata.console.organization.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.console.organization.facade.PositionFacade;
import top.mddata.console.organization.service.PositionService;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author henhen6
 * @since 2025/9/21 16:18
 */
@Service
@RequiredArgsConstructor
public class PositionFacadeImpl implements PositionFacade {
    private final PositionService positionService;

    @Override
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        return positionService.findByIds(ids);
    }
}
