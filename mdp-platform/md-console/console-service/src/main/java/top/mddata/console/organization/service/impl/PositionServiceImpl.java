package top.mddata.console.organization.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.common.entity.Position;
import top.mddata.common.mapper.PositionMapper;
import top.mddata.console.organization.service.PositionService;

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

}
