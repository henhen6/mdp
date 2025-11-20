package top.mddata.open.manage.facade.impl;


import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.open.manage.facade.AppFacade;
import top.mddata.open.admin.vo.AppVo;
import top.mddata.open.admin.vo.OauthScopeVo;

import java.util.List;

/**
 * 应用管理单体版实现类
 *
 * @author henhen6
 * @since 2025/8/12 11:28
 */
@Service
public class OpApplicationFacadeImpl implements AppFacade {
    @Override
    public R<List<AppVo>> listNeedPushApp() {
        return R.success(null);
    }

    @Override
    public R<List<OauthScopeVo>> getScopeListByCode(List<String> scopes) {
        return R.success(null);
    }

    @Override
    public R<AppVo> getByAppId(String id) {
        return R.success(null);
    }
}
