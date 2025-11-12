package top.mddata.console.organization.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.common.entity.User;
import top.mddata.common.mapper.UserMapper;
import top.mddata.console.organization.service.UserService;

/**
 * 用户 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 15:44:52
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl extends SuperServiceImpl<UserMapper, User> implements UserService {

}
