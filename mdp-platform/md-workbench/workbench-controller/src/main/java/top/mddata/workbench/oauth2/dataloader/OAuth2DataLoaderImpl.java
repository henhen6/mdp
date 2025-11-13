package top.mddata.workbench.oauth2.dataloader;

import cn.dev33.satoken.oauth2.data.loader.SaOAuth2DataLoader;
import cn.dev33.satoken.oauth2.data.model.loader.SaClientModel;
import cn.dev33.satoken.util.SaFoxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.mddata.base.base.R;
import top.mddata.base.exception.BizException;
import top.mddata.common.constant.ConfigKey;
import top.mddata.console.system.facade.ConfigFacade;
import top.mddata.open.manage.facade.AppFacade;
import top.mddata.open.manage.facade.OauthOpenidFacade;
import top.mddata.open.manage.facade.OauthScopeFacade;
import top.mddata.open.manage.vo.AppVo;
import top.mddata.open.manage.vo.OauthOpenidVo;
import top.mddata.open.manage.vo.OauthScopeVo;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义数据加载器
 *
 * @author henhen6
 */
@Slf4j
@Component
public class OAuth2DataLoaderImpl implements SaOAuth2DataLoader {
    @Autowired
    private OauthOpenidFacade oauthOpenidFacade;
    @Autowired
    private AppFacade appFacade;
    @Autowired
    private OauthScopeFacade scopeFacade;
    @Autowired
    private ConfigFacade configFacade;

    /**
     * 根据 clientId 获取 Client 信息
     * @param clientId 应用id
     * @return 客户端信息
     */
    @Override
    public SaClientModel getClientModel(String clientId) {
        R<AppVo> result = appFacade.getByAppId(clientId);
        if (!result.getIsSuccess()) {
            return null;
        }
        AppVo opApplicationVo = result.getData();
        if (opApplicationVo == null) {
            return null;
        }
        if (!opApplicationVo.getState()) {
            throw new BizException("该应用已被封禁，无法授权认证");
        }

        R<List<OauthScopeVo>> listR = scopeFacade.listByApplicationId(opApplicationVo.getId());
        List<String> scopes = new ArrayList<>();
        if (listR.getIsSuccess()) {
            List<OauthScopeVo> scopeList = listR.getData();
            scopes = scopeList.stream().map(OauthScopeVo::getCode).toList();
        }
        // 构建 SaClientModel 对象
        SaClientModel model = new SaClientModel()
                // client id
                .setClientId(opApplicationVo.getId().toString())
                // client 秘钥
                .setClientSecret(opApplicationVo.getAppSecret())
                // 所有允许授权的 url
                .addAllowRedirectUris(SaFoxUtil.convertStringToArray(opApplicationVo.getOauth2AllowRedirectUris()))
                // 所有签约的权限
                .addContractScopes(SaFoxUtil.toArray(scopes))
                // 所有允许的授权模式
                .addAllowGrantTypes(SaFoxUtil.convertStringToArray(opApplicationVo.getOauth2AllowGrantTypes()));

        // 是否每次刷新 Refresh-Token
        if (opApplicationVo.getOauth2NewRefresh() == -1) {
            Boolean defaultNewRefresh = configFacade.getBoolean(ConfigKey.Open.APPLICATION_NEW_REFRESH, true);
            model.setIsNewRefresh(defaultNewRefresh);
        } else {
            model.setIsNewRefresh(opApplicationVo.getOauth2NewRefresh() == 1);
        }

        // AccessToken 有效期
        if (opApplicationVo.getOauth2AccessTokenTimeout() == -1) {
            model.setAccessTokenTimeout(configFacade.getLong(ConfigKey.Open.APPLICATION_ACCESS_TOKEN_TIMEOUT, 1L));
        } else {
            model.setAccessTokenTimeout(opApplicationVo.getOauth2AccessTokenTimeout());
        }
        // RefreshToken 有效期
        if (opApplicationVo.getOauth2RefreshTokenTimeout() == -1) {
            model.setRefreshTokenTimeout(configFacade.getLong(ConfigKey.Open.APPLICATION_REFRESH_TOKEN_TIMEOUT, 1L));
        } else {
            model.setRefreshTokenTimeout(opApplicationVo.getOauth2RefreshTokenTimeout());
        }
        // ClientToken 有效期
        if (opApplicationVo.getOauth2ClientTokenTimeout() == -1) {
            model.setClientTokenTimeout(configFacade.getLong(ConfigKey.Open.APPLICATION_CLIENT_TOKEN_TIMEOUT, 1L));
        } else {
            model.setClientTokenTimeout(opApplicationVo.getOauth2ClientTokenTimeout());
        }

        // 是否允许此应用自动确认授权 （高危配置，禁止向不被信任的第三方开启此选项）
        model.setIsAutoConfirm(opApplicationVo.getOauth2IsConfirm());

        // 返回
        return model;
    }

    // 根据 clientId 和 loginId 获取 openid
    @Override
    public String getOpenid(String clientIdString, Object loginId) {
        String appId = SaFoxUtil.getValueByType(clientIdString, String.class);
        Long userId = SaFoxUtil.getValueByType(loginId, Long.class);
        R<OauthOpenidVo> result = oauthOpenidFacade.getByAppIdAndUserId(appId, userId);
        if (!result.getIsSuccess() || result.getData() == null) {
            log.warn("查询openid失败: {}", result.getMsg());
            return null;
        }
        return result.getData().getOpenid();
    }

}
