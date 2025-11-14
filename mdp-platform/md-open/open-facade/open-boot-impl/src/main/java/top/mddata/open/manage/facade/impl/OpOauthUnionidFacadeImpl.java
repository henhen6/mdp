package top.mddata.open.manage.facade.impl;

import org.springframework.stereotype.Component;
import top.mddata.base.base.R;
import top.mddata.open.manage.facade.OauthUnionidFacade;
import top.mddata.open.manage.vo.OauthUnionidVo;

/**
 *
 * @author henhen6
 * @since 2025/8/22 00:07
 */
@Component
public class OpOauthUnionidFacadeImpl implements OauthUnionidFacade {
    @Override
    public R<OauthUnionidVo> getBySubjectIdAndUserId(Long subjectId, Long userId) {
        return null;
    }
}
