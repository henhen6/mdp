package top.mddata.console.service.organization.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.util.ContextUtil;
import top.mddata.common.enumeration.organization.UserIdentityEnum;
import top.mddata.console.service.organization.OrgVisibilityService;
import top.mddata.console.service.organization.UserIdentityService;

import java.util.List;

/**
 * 组织可见性 服务层实现。
 */
@Service
@RequiredArgsConstructor
public class OrgVisibilityServiceImpl implements OrgVisibilityService {
    private final UserIdentityService userIdentityService;

    @Override
    public UserIdentityEnum currentIdentity() {
        return userIdentityService.getIdentity(ContextUtil.getUserId());
    }

    @Override
    public List<Long> currentVisibleRootOrgIds() {
        return OrgVisibilityService.visibleRootOrgIds(currentIdentity());
    }
}
