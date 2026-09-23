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
import top.mddata.console.facade.system.ConfigFacade;
import top.mddata.open.facade.admin.AppFacade;
import top.mddata.open.facade.admin.OauthOpenidFacade;
import top.mddata.open.facade.admin.OauthScopeFacade;
import top.mddata.open.vo.admin.AppVo;
import top.mddata.open.vo.admin.OauthOpenidVo;
import top.mddata.open.vo.admin.OauthScopeVo;

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
    private OauthScopeFacade oauthScopeFacade;
    @Autowired
    private ConfigFacade configFacade;

    /**
     * 根据 clientId 获取 Client 信息
     * @param clientId 应用id
     * @return 客户端信息
     */
    @Override
    public SaClientModel getClientModel(String clientId) {
        R<AppVo> result = appFacade.getAppByAppKey(clientId);
        if (!result.getIsSuccess()) {
            return null;
        }
        AppVo appVo = result.getData();
        if (appVo == null) {
            return null;
        }
        if (!appVo.getState()) {
            throw new BizException("该应用已被封禁，无法授权认证");
        }

        R<List<OauthScopeVo>> listR = oauthScopeFacade.listByAppId(appVo.getId());
        List<String> scopes = new ArrayList<>();
        if (listR.getIsSuccess()) {
            List<OauthScopeVo> scopeList = listR.getData();
            scopes = scopeList.stream().map(OauthScopeVo::getCode).toList();
        }
        // 构建 SaClientModel 对象
        SaClientModel model = new SaClientModel()
                // client id（与全链路保持一致，使用 appKey，而非应用主键 id）
                .setClientId(clientId)
                // client 秘钥
                .setClientSecret(appVo.getAppSecret())
                // 所有允许授权的 url
                .addAllowRedirectUris(SaFoxUtil.convertStringToArray(appVo.getOauth2AllowRedirectUris()))
                // 所有签约的权限
                .addContractScopes(SaFoxUtil.toArray(scopes))
                // 所有允许的授权模式
                .addAllowGrantTypes(SaFoxUtil.convertStringToArray(appVo.getOauth2AllowGrantTypes()));

        // 是否每次刷新 Refresh-Token
        if (appVo.getOauth2NewRefresh() == -1) {
            Boolean defaultNewRefresh = configFacade.getBoolean(ConfigKey.Open.APP_NEW_REFRESH, true);
            model.setIsNewRefresh(defaultNewRefresh);
        } else {
            model.setIsNewRefresh(appVo.getOauth2NewRefresh() == 1);
        }

        // AccessToken 有效期
        if (appVo.getOauth2AccessTokenTimeout() == -1) {
            model.setAccessTokenTimeout(configFacade.getLong(ConfigKey.Open.APP_ACCESS_TOKEN_TIMEOUT, 1L));
        } else {
            model.setAccessTokenTimeout(appVo.getOauth2AccessTokenTimeout());
        }
        // RefreshToken 有效期
        if (appVo.getOauth2RefreshTokenTimeout() == -1) {
            model.setRefreshTokenTimeout(configFacade.getLong(ConfigKey.Open.APP_REFRESH_TOKEN_TIMEOUT, 1L));
        } else {
            model.setRefreshTokenTimeout(appVo.getOauth2RefreshTokenTimeout());
        }
        // ClientToken 有效期
        if (appVo.getOauth2ClientTokenTimeout() == -1) {
            model.setClientTokenTimeout(configFacade.getLong(ConfigKey.Open.APP_CLIENT_TOKEN_TIMEOUT, 1L));
        } else {
            model.setClientTokenTimeout(appVo.getOauth2ClientTokenTimeout());
        }

        // 是否允许此应用自动确认授权 （高危配置，禁止向不被信任的第三方开启此选项）
        model.setIsAutoConfirm(appVo.getOauth2IsConfirm());

        // 返回
        return model;
    }

    // 根据 clientId 和 loginId 获取 openid
    @Override
    public String getOpenid(String clientIdString, Object loginId) {
        String appId = SaFoxUtil.getValueByType(clientIdString, String.class);
        Long userId = SaFoxUtil.getValueByType(loginId, Long.class);
        R<OauthOpenidVo> result = oauthOpenidFacade.getByAppKeyAndUserId(appId, userId);
        if (!result.getIsSuccess() || result.getData() == null) {
            log.warn("查询openid失败: {}", result.getMsg());
            return null;
        }
        return result.getData().getOpenid();
    }

}
