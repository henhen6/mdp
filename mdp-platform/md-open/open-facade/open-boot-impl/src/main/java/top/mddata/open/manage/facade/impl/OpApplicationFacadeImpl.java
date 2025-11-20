package top.mddata.open.manage.facade.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.open.admin.service.AppService;
import top.mddata.open.admin.service.OauthScopeService;
import top.mddata.open.admin.vo.AppVo;
import top.mddata.open.admin.vo.OauthScopeVo;
import top.mddata.open.manage.facade.AppFacade;

import java.util.List;

/**
 * 应用管理单体版实现类
 *
 * @author henhen6
 * @since 2025/8/12 11:28
 */
@Service
@RequiredArgsConstructor
public class OpApplicationFacadeImpl implements AppFacade {
    private final AppService appService;
    private final OauthScopeService oauthScopeService;

    @Override
    public R<List<AppVo>> listNeedPushApp() {
        return R.success(appService.listNeedPushApp());
    }

    @Override
    public R<List<OauthScopeVo>> getScopeListByCode(List<String> scopes) {
        return R.success(oauthScopeService.getScopeListByCode(scopes));
    }

    @Override
    public R<AppVo> getByAppKey(String key) {
        return R.success(appService.getAppByAppKey(key));
    }
}
