package top.mddata.console.organization.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.common.entity.UserOrgRel;
import top.mddata.common.mapper.UserOrgRelMapper;
import top.mddata.console.organization.service.UserOrgRelService;

/**
 * 用户所属组织 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 15:50:00
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserOrgRelServiceImpl extends SuperServiceImpl<UserOrgRelMapper, UserOrgRel> implements UserOrgRelService {

}
