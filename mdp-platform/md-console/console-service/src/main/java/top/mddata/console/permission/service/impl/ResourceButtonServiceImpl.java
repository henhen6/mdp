package top.mddata.console.permission.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.console.permission.entity.ResourceButton;
import top.mddata.console.permission.mapper.ResourceButtonMapper;
import top.mddata.console.permission.service.ResourceButtonService;

/**
 * 按钮 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceButtonServiceImpl extends SuperServiceImpl<ResourceButtonMapper, ResourceButton> implements ResourceButtonService {

}
