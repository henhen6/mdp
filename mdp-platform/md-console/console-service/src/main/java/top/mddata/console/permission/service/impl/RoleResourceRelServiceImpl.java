package top.mddata.console.permission.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.console.permission.entity.RoleResourceRel;
import top.mddata.console.permission.mapper.RoleResourceRelMapper;
import top.mddata.console.permission.service.RoleResourceRelService;

/**
 * 角色资源关联 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:29
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoleResourceRelServiceImpl extends SuperServiceImpl<RoleResourceRelMapper, RoleResourceRel> implements RoleResourceRelService {

}
