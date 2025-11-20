package top.mddata.open.manage.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.mddata.base.base.R;
import top.mddata.open.admin.service.OauthUnionidService;
import top.mddata.open.admin.vo.OauthUnionidVo;
import top.mddata.open.manage.facade.OauthUnionidFacade;

/**
 *
 * @author henhen6
 * @since 2025/8/22 00:07
 */
@Component
@RequiredArgsConstructor
public class OpOauthUnionidFacadeImpl implements OauthUnionidFacade {
    private final OauthUnionidService oauthUnionidService;

    @Override
    public R<OauthUnionidVo> getBySubjectIdAndUserId(Long subjectId, Long userId) {
        return R.success(oauthUnionidService.getBySubjectIdAndUserId(subjectId, userId));
    }
}
