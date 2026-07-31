package top.mddata.open.facade.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.open.facade.admin.AppKeysFacade;
import top.mddata.open.facade.admin.api.AppKeysApi;
import top.mddata.open.vo.admin.AppKeysVo;

/**
 * 应用秘钥微服务版实现类
 *
 * @author henhen6
 * @since 2026/06/29
 */
@Service
@RequiredArgsConstructor
public class AppKeysFacadeImpl implements AppKeysFacade {
    private final AppKeysApi appKeysApi;

    @Override
    public R<AppKeysVo> getByAppKey(String appKey) {
        return appKeysApi.getAppKeysByAppKey(appKey);
    }
}
