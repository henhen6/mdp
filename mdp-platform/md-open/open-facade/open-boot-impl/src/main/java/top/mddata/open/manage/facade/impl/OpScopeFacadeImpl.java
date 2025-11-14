package top.mddata.open.manage.facade.impl;

import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.open.manage.facade.OauthScopeFacade;
import top.mddata.open.manage.vo.OauthScopeVo;

import java.util.List;

/**
 *
 * @author henhen6
 * @since 2025/8/24 23:43
 */
@Service
public class OpScopeFacadeImpl implements OauthScopeFacade {
    @Override
    public R<List<OauthScopeVo>> listByAppId(Long appId) {
        return R.success(null);
    }
}
