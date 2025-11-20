package top.mddata.workbench.oauth2.handler;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.oauth2.SaOAuth2Manager;
import cn.dev33.satoken.oauth2.consts.GrantType;
import cn.dev33.satoken.oauth2.consts.SaOAuth2Consts;
import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.data.model.ClientTokenModel;
import cn.dev33.satoken.oauth2.scope.handler.SaOAuth2ScopeHandlerInterface;
import cn.dev33.satoken.util.SaFoxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.mddata.base.base.R;
import top.mddata.base.utils.DateUtils;
import top.mddata.open.admin.dto.OauthLogDto;
import top.mddata.open.manage.facade.AppFacade;
import top.mddata.open.manage.facade.OauthLogFacade;
import top.mddata.open.admin.vo.AppVo;

/**
 * 最终权限处理器：在所有权限处理器工作完成之后，执行此权限处理器：
 * 1、记录授权日志。
 * 2、发送授权消息通知。
 *
 * 参考： {@link https://sa-token.cc/doc.html#/oauth2/oauth2-custom-scope?id=_4%e3%80%81%e6%9c%80%e7%bb%88%e6%9d%83%e9%99%90%e5%a4%84%e7%90%86%e5%99%a8}
 *
 * @author henhen6
 * @since 2025年11月13日10:27:53
 */
@Component
@Slf4j
public class LoginFinallyScopeHandler implements SaOAuth2ScopeHandlerInterface {

    @Autowired
    private OauthLogFacade oauthLogFacade;
    @Autowired
    private AppFacade appFacade;

    @Override
    public String getHandlerScope() {
        return SaOAuth2Consts._FINALLY_WORK_SCOPE;
    }

    @Override
    public void workAccessToken(AccessTokenModel at) {
        String appId = SaFoxUtil.getValueByType(at.getClientId(), String.class);
        R<AppVo> result = appFacade.getByAppId(appId);
        if (!result.getIsSuccess()) {
            return;
        }
        AppVo data = result.getData();

        OauthLogDto ool = new OauthLogDto();
        ool.setAppId(data.getId());
        ool.setUserId(SaFoxUtil.getValueByType(at.getLoginId(), long.class));
        ool.setOpenid(SaFoxUtil.getValueByType(at.extraData.get("openid"), String.class));
        ool.setUnionid(SaFoxUtil.getValueByType(at.extraData.get("unionid"), String.class));
        ool.setAccessToken(at.getAccessToken());
        ool.setAccessTokenExpires(DateUtils.fromTimestamp(at.getExpiresTime(), true));
        ool.setRefreshToken(null);
        ool.setRefreshTokenExpires(null);
        ool.setGrantType(getGrantType());
        ool.setScopes(SaOAuth2Manager.getDataConverter().convertScopeListToString(at.getScopes()));
        ool.setTokenType(at.getTokenType());
        ool.setRemarks(null);
        ool.setRedirectUri(null);
        oauthLogFacade.save(ool);

        // TODO henhen6 发送消息通知
        if (ool.getGrantType() != null && !ool.getGrantType().equals(GrantType.refresh_token)) {
            log.info("这里还没有实现");
//            msgFacade.sendMessage(message);
        }
    }

    @Override
    public void workClientToken(ClientTokenModel ct) {

    }

    private String getGrantType() {
        SaRequest req = SaHolder.getRequest();
        // 如果是 refresh_token
        if (req.isPath(SaOAuth2Consts.Api.refresh)) {
            return GrantType.refresh_token;
        }
        boolean isPath = req.isPath(SaOAuth2Consts.Api.authorize) || req.isPath("/oauth2/getRedirectUri");
        boolean isParam = req.isParam(SaOAuth2Consts.Param.response_type, SaOAuth2Consts.ResponseType.token);
        // 如果是 Implicit 隐藏式
        if (isPath && isParam) {
            return GrantType.implicit;
        }
        // 其它场景
        return req.getParam(SaOAuth2Consts.Param.grant_type);
    }

}
