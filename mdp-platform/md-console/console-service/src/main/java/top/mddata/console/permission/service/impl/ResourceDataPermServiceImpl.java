package top.mddata.console.permission.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.console.permission.entity.ResourceDataPerm;
import top.mddata.console.permission.mapper.ResourceDataPermMapper;
import top.mddata.console.permission.service.ResourceDataPermService;

/**
 * 数据权限 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceDataPermServiceImpl extends SuperServiceImpl<ResourceDataPermMapper, ResourceDataPerm> implements ResourceDataPermService {

}
