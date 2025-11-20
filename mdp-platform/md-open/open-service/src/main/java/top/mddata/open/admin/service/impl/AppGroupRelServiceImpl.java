package top.mddata.open.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.admin.entity.AppGroupRel;
import top.mddata.open.admin.mapper.AppGroupRelMapper;
import top.mddata.open.admin.service.AppGroupRelService;

/**
 * 应用拥有的权限分组 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppGroupRelServiceImpl extends SuperServiceImpl<AppGroupRelMapper, AppGroupRel> implements AppGroupRelService {

}
