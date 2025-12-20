package top.mddata.workbench.oauth2.handler;

import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.data.model.ClientTokenModel;
import cn.dev33.satoken.oauth2.scope.CommonScope;
import cn.dev33.satoken.oauth2.scope.handler.SaOAuth2ScopeHandlerInterface;
import cn.dev33.satoken.util.SaFoxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.mddata.base.base.R;
import top.mddata.open.admin.vo.AppVo;
import top.mddata.open.admin.vo.OauthUnionidVo;
import top.mddata.open.manage.facade.AppFacade;
import top.mddata.open.manage.facade.OauthUnionidFacade;

/**
 * UnionId Scope 处理器，在返回的 AccessToken 中增加 unionid 字段
 *
 * @author henhen6
 * @since 2025年11月13日10:27:53
 */
@Component
@Slf4j
public class UnionIdScopeHandler implements SaOAuth2ScopeHandlerInterface {

    @Autowired
    private AppFacade opApplicationFacade;
    @Autowired
    private OauthUnionidFacade opOauthUnionidFacade;

    @Override
    public String getHandlerScope() {
        return CommonScope.UNIONID;
    }

    @Override
    public void workAccessToken(AccessTokenModel at) {
        Long userId = SaFoxUtil.getValueByType(at.getLoginId(), long.class);
        String clientId = SaFoxUtil.getValueByType(at.getClientId(), String.class);
        R<AppVo> result = opApplicationFacade.getAppByAppKey(clientId);
        if (!result.getIsSuccess() || result.getData() == null) {
            log.warn("应用不存在, {}", result.getMsg());
            return;
        }
        Long clientUserId = result.getData().getCreatedBy();

        Long subjectId = (clientUserId == null ? -1 : clientUserId);
        R<OauthUnionidVo> r = opOauthUnionidFacade.getBySubjectIdAndUserId(subjectId, userId);
        if (!r.getIsSuccess() || r.getData() == null) {
            log.warn("用户不存在, {}", result.getMsg());
            return;
        }
        at.extraData.put(CommonScope.UNIONID, r.getData().getUnionid());
    }

    @Override
    public void workClientToken(ClientTokenModel ct) {

    }
}