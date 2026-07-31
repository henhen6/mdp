package top.mddata.open.facade.admin.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.open.facade.admin.AppFacade;
import top.mddata.open.service.admin.AppService;
import top.mddata.open.service.admin.OauthScopeService;
import top.mddata.open.vo.admin.AppVo;

import java.util.List;

/**
 * 应用管理单体版实现类
 *
 * @author henhen6
 * @since 2025/8/12 11:28
 */
@Service
@RequiredArgsConstructor
public class AppFacadeImpl implements AppFacade {
    private final AppService appService;
    private final OauthScopeService oauthScopeService;

    @Override
    public R<List<AppVo>> listNeedPushApp() {
        return R.success(appService.listNeedPushApp());
    }

    @Override
    public R<Boolean> checkAppByUserId(Long userId, Long appId) {
        return R.success(appService.checkAppByUserId(userId, appId));
    }

    @Override
    public R<AppVo> getAppByAppKey(String key) {
        return R.success(appService.getAppByAppKey(key));
    }
}
