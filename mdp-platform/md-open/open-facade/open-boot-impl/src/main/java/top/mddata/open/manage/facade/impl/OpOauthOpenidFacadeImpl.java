package top.mddata.open.manage.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.open.admin.service.OauthOpenidService;
import top.mddata.open.admin.vo.OauthOpenidVo;
import top.mddata.open.manage.facade.OauthOpenidFacade;

/**
 *
 * @author henhen6
 * @since 2025/8/22 12:43
 */
@Service
@RequiredArgsConstructor
public class OpOauthOpenidFacadeImpl implements OauthOpenidFacade {
    private final OauthOpenidService oauthOpenidService;

    @Override
    public R<OauthOpenidVo> getByAppKeyAndUserId(String appKey, Long userId) {
        return R.success(oauthOpenidService.getByAppKeyAndUserId(appKey, userId));
    }
}
