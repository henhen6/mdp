package top.mddata.console.organization.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.common.entity.OrgNature;
import top.mddata.common.mapper.OrgNatureMapper;
import top.mddata.console.organization.service.OrgNatureService;

/**
 * 组织性质 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 15:50:00
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrgNatureServiceImpl extends SuperServiceImpl<OrgNatureMapper, OrgNature> implements OrgNatureService {

}
