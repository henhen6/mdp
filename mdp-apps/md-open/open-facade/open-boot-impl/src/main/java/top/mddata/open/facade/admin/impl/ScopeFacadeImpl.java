package top.mddata.open.facade.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.open.facade.admin.OauthScopeFacade;
import top.mddata.open.service.admin.OauthScopeService;
import top.mddata.open.vo.admin.OauthScopeVo;

import java.util.List;

/**
 *
 * @author henhen6
 * @since 2025/8/24 23:43
 */
@Service
@RequiredArgsConstructor
public class ScopeFacadeImpl implements OauthScopeFacade {
    private final OauthScopeService oauthScopeService;

    @Override
    public R<List<OauthScopeVo>> getScopeListByCode(List<String> scopes) {
        return R.success(oauthScopeService.getScopeListByCode(scopes));
    }

    @Override
    public R<List<OauthScopeVo>> listByAppId(Long appId) {
        return R.success(oauthScopeService.listByAppId(appId));
    }
}
