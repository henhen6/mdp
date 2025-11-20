package top.mddata.open.manage.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.open.admin.service.OauthScopeService;
import top.mddata.open.admin.vo.OauthScopeVo;
import top.mddata.open.manage.facade.OauthScopeFacade;

import java.util.List;

/**
 *
 * @author henhen6
 * @since 2025/8/24 23:43
 */
@Service
@RequiredArgsConstructor
public class OpScopeFacadeImpl implements OauthScopeFacade {
    private final OauthScopeService oauthScopeService;

    @Override
    public R<List<OauthScopeVo>> listByAppId(Long appId) {
        return R.success(oauthScopeService.listByAppId(appId));
    }
}
