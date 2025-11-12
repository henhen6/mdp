package top.mddata.console.organization.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.common.entity.Org;
import top.mddata.common.mapper.OrgMapper;
import top.mddata.console.organization.service.OrgService;

/**
 * 组织 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 15:49:10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrgServiceImpl extends SuperServiceImpl<OrgMapper, Org> implements OrgService {

}
