package top.mddata.console.permission.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.console.permission.entity.ResourceMenu;
import top.mddata.console.permission.mapper.ResourceMenuMapper;
import top.mddata.console.permission.service.ResourceMenuService;

/**
 * 菜单 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceMenuServiceImpl extends SuperServiceImpl<ResourceMenuMapper, ResourceMenu> implements ResourceMenuService {

}
