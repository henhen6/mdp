package top.mddata.open.manage.facade.impl;

import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.open.manage.facade.OauthOpenidFacade;
import top.mddata.open.admin.vo.OauthOpenidVo;

/**
 *
 * @author henhen6
 * @since 2025/8/22 12:43
 */
@Service
public class OpOauthOpenidFacadeImpl implements OauthOpenidFacade {
    @Override
    public R<OauthOpenidVo> getByAppIdAndUserId(String appId, Long userId) {
        return null;
    }
}
