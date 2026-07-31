package top.mddata.open.facade.admin.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.open.facade.admin.AppFacade;
import top.mddata.open.facade.admin.api.AppApi;
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
    private final AppApi appApi;

    @Override
    public R<List<AppVo>> listNeedPushApp() {
        return appApi.listNeedPushApp();
    }

    @Override
    public R<Boolean> checkAppByUserId(Long userId, Long appId) {
        return appApi.checkAppByUserId(userId, appId);
    }

    @Override
    public R<AppVo> getAppByAppKey(String key) {
        return appApi.getAppByAppKey(key);
    }
}
