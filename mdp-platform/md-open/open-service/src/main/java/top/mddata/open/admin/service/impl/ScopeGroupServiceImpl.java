package top.mddata.open.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.admin.entity.ScopeGroup;
import top.mddata.open.admin.mapper.ScopeGroupMapper;
import top.mddata.open.admin.service.ScopeGroupService;

/**
 * 应用权限分组 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScopeGroupServiceImpl extends SuperServiceImpl<ScopeGroupMapper, ScopeGroup> implements ScopeGroupService {

}
