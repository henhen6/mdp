package top.mddata.open.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.admin.entity.GroupScopeRel;
import top.mddata.open.admin.mapper.GroupScopeRelMapper;
import top.mddata.open.admin.service.GroupScopeRelService;

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

}
