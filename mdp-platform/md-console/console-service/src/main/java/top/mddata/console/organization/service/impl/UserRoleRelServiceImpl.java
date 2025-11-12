package top.mddata.console.organization.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.mapper.UserRoleRelMapper;
import top.mddata.console.organization.service.UserRoleRelService;

/**
 * 用户角色关联 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 15:50:00
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserRoleRelServiceImpl extends SuperServiceImpl<UserRoleRelMapper, UserRoleRel> implements UserRoleRelService {
}
