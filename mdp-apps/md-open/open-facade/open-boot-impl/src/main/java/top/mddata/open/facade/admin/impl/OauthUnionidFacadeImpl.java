package top.mddata.open.facade.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.mddata.base.base.R;
import top.mddata.open.facade.admin.OauthUnionidFacade;
import top.mddata.open.service.admin.OauthUnionidService;
import top.mddata.open.vo.admin.OauthUnionidVo;

/**
 *
 * @author henhen6
 * @since 2025/8/22 00:07
 */
@Component
@RequiredArgsConstructor
public class OauthUnionidFacadeImpl implements OauthUnionidFacade {
    private final OauthUnionidService oauthUnionidService;

    @Override
    public R<OauthUnionidVo> getBySubjectIdAndUserId(Long subjectId, Long userId) {
        return R.success(oauthUnionidService.getBySubjectIdAndUserId(subjectId, userId));
    }
}
