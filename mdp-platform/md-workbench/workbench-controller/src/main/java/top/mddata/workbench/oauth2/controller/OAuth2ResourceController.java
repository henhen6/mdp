package top.mddata.workbench.oauth2.controller;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.oauth2.SaOAuth2Manager;
import cn.dev33.satoken.oauth2.config.SaOAuth2ServerConfig;
import cn.dev33.satoken.oauth2.consts.GrantType;
import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.data.model.ClientTokenModel;
import cn.dev33.satoken.oauth2.data.model.RefreshTokenModel;
import cn.dev33.satoken.oauth2.data.model.loader.SaClientModel;
import cn.dev33.satoken.oauth2.data.model.request.ClientIdAndSecretModel;
import cn.dev33.satoken.oauth2.error.SaOAuth2ErrorCode;
import cn.dev33.satoken.oauth2.exception.SaOAuth2ClientModelException;
import cn.dev33.satoken.oauth2.exception.SaOAuth2Exception;
import cn.dev33.satoken.oauth2.exception.SaOAuth2RefreshTokenException;
import cn.dev33.satoken.oauth2.granttype.handler.SaOAuth2GrantTypeHandlerInterface;
import cn.dev33.satoken.oauth2.strategy.SaOAuth2Strategy;
import cn.dev33.satoken.oauth2.template.SaOAuth2Template;
import cn.dev33.satoken.oauth2.template.SaOAuth2Util;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.common.entity.User;
import top.mddata.workbench.oauth2.dto.ClientTokenDto;
import top.mddata.workbench.oauth2.dto.RefreshDto;
import top.mddata.workbench.oauth2.dto.RevokeDto;
import top.mddata.workbench.oauth2.vo.ClientTokenVo;
import top.mddata.workbench.oauth2.vo.TokenVo;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.vo.SsoUserVo;

import java.util.List;

import static top.mddata.workbench.oauth2.data.Oauth2DataResolver.readClientIdAndSecret;

/**
 * OAuth2 Server端 资源控制器
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Oauth2资源", description = "此模块只处理Oauth2资源获取")
public class OAuth2ResourceController {
    private final SsoUserService ssoUserService;

    /**
     * 通过 accessToken 获取 userinfo
     * 【提供给第三方后台调用的】
     *
     * @return
     */
    @PostMapping("/oauth2/userinfo")
    public R<SsoUserVo> getUserinfo() {
        // 校验 Access-Token 是否具有 "userinfo" 权限
        String accessToken = SaOAuth2Manager.getDataResolver().readAccessToken(SaHolder.getRequest());
        SaOAuth2Util.checkAccessTokenScope(accessToken, "userinfo");

        Object loginId = SaOAuth2Util.getLoginIdByAccessToken(accessToken);
        long userId = SaFoxUtil.getValueByType(loginId, long.class);

        User ssoUser = ssoUserService.getById(userId);
        ssoUser.setPassword(null);
        ssoUser.setSalt(null);
        return R.success(BeanUtil.toBean(ssoUser, SsoUserVo.class));
    }


    /**
     * 通过code 获取 accessToken || 模式三：密码式
     * 【提供给第三方后台调用的】
     *
     * @return
     */
    @PostMapping("/oauth2/token")
    public R<TokenVo> token() {
        AccessTokenModel at = SaOAuth2Strategy.instance.grantTypeAuth.apply(SaHolder.getRequest());

        TokenVo vo = buildRefreshTokenReturnValue(at);
        return R.success(vo);
    }

    /**
     *  Refresh-Token 刷新 Access-Token
     * 【提供给第三方后台调用的】
     *
     * @return
     */
    @PostMapping("/oauth2/refresh")
    public R<TokenVo> refresh(@Validated RefreshDto param) {
        // 校验 grant_type 必须为 refresh_token
        String grantType = param.getGrantType();
        SaOAuth2Exception.throwBy(!grantType.equals(GrantType.refresh_token), "无效 grant_type：" + grantType, SaOAuth2ErrorCode.CODE_30126);

        // 刷新 Access-Token
        // 先校验提供的 grant_type 是否有效

        SaOAuth2GrantTypeHandlerInterface grantTypeHandler = SaOAuth2Strategy.instance.grantTypeHandlerMap.get(grantType);
        if (grantTypeHandler == null) {
            throw new SaOAuth2Exception("无效 grant_type: " + grantType).setCode(SaOAuth2ErrorCode.CODE_30126);
        }

        // 针对 authorization_code 与 password 两种特殊 grant_type，需要判断全局是否开启
        SaOAuth2ServerConfig config = SaOAuth2Manager.getServerConfig();
        if (grantType.equals(GrantType.authorization_code) && !config.getEnableAuthorizationCode()) {
            throw new SaOAuth2Exception("系统未开放的 grant_type: " + grantType).setCode(SaOAuth2ErrorCode.CODE_30126);
        }
        if (grantType.equals(GrantType.password) && !config.getEnablePassword()) {
            throw new SaOAuth2Exception("系统未开放的 grant_type: " + grantType).setCode(SaOAuth2ErrorCode.CODE_30126);
        }

        // 校验 clientSecret 和 scope
        ClientIdAndSecretModel clientIdAndSecretModel = readClientIdAndSecret(param);
        List<String> scopes = SaOAuth2Manager.getDataConverter().convertScopeStringToList(param.getScope());
        SaClientModel clientModel = SaOAuth2Manager.getTemplate().checkClientSecretAndScope(clientIdAndSecretModel.getClientId(), clientIdAndSecretModel.getClientSecret(), scopes);

        // 检测应用是否开启此 grantType
        if (!clientModel.getAllowGrantTypes().contains(grantType)) {
            throw new SaOAuth2Exception("应用未开放的 grant_type: " + grantType).setCode(SaOAuth2ErrorCode.CODE_30141);
        }

        // 调用 处理器构建 Access-Token
        AccessTokenModel accessTokenModel = getAccessToken(param, clientIdAndSecretModel.getClientId(), scopes);

        TokenVo tokenVo = buildRefreshTokenReturnValue(accessTokenModel);
        return R.success(tokenVo);
    }


    /**
     *  回收 Access-Token
     * 【提供给第三方后台调用的】
     *
     * @return
     */
    @PostMapping("/oauth2/revoke")
    public R<Boolean> revoke(@Validated RevokeDto param) {
        // 获取变量
        SaOAuth2Template oauth2Template = SaOAuth2Manager.getTemplate();

        // 获取参数
        ClientIdAndSecretModel clientIdAndSecret = readClientIdAndSecret(param);
        String clientId = clientIdAndSecret.getClientId();
        String clientSecret = clientIdAndSecret.getClientSecret();
        String accessToken = param.getAccessToken();

        // 如果 Access-Token 不存在，直接返回
        if (oauth2Template.getAccessToken(accessToken) == null) {
            return R.fail("access_token 不存在：" + accessToken);
        }

        // 校验参数
        oauth2Template.checkAccessTokenParam(clientId, clientSecret, accessToken);

        // 回收 Access-Token
        oauth2Template.revokeAccessToken(accessToken);

        // 返回
        return R.success();
    }


    /**
     *  模式四：凭证式
     * 【提供给第三方后台调用的】
     *
     * @return
     */
    @PostMapping("/oauth2/client_token")
    public R<ClientTokenVo> clientToken(@Validated ClientTokenDto param) {
        // 获取变量
//        SaRequest req = SaHolder.getRequest();
        SaOAuth2ServerConfig cfg = SaOAuth2Manager.getServerConfig();
        SaOAuth2Template oauth2Template = SaOAuth2Manager.getTemplate();

        String grantType = param.getGrantType();
        if (!grantType.equals(GrantType.client_credentials)) {
            throw new SaOAuth2Exception("无效 grant_type：" + grantType).setCode(SaOAuth2ErrorCode.CODE_30126);
        }
        if (!cfg.getEnableClientCredentials()) {
            throwErrorSystemNotEnableModel();
        }
        ClientIdAndSecretModel clientIdAndSecret = readClientIdAndSecret(param);
        SaClientModel saClientModel = oauth2Template.checkClientModel(clientIdAndSecret.getClientId());

        if (!saClientModel.getAllowGrantTypes().contains(GrantType.client_credentials)) {
            throwErrorClientNotEnableModel();
        }

        // 获取参数
        String clientId = clientIdAndSecret.getClientId();
        String clientSecret = clientIdAndSecret.getClientSecret();
        List<String> scopes = SaOAuth2Manager.getDataConverter().convertScopeStringToList(param.getScope());

        // 校验 ClientScope
        oauth2Template.checkContractScope(clientId, scopes);

        // 校验 ClientSecret
        oauth2Template.checkClientSecret(clientId, clientSecret);

        // 生成
        ClientTokenModel ct = SaOAuth2Manager.getDataGenerate().generateClientToken(clientId, scopes);

        // 返回
        return R.success(buildClientTokenReturnValue(ct));
    }

    /**
     * 系统未开放此授权模式时抛出异常
     */
    private void throwErrorSystemNotEnableModel() {
        throw new SaOAuth2Exception("系统暂未开放此授权模式").setCode(SaOAuth2ErrorCode.CODE_30141);
    }

    /**
     * 应用未开放此授权模式时抛出异常
     */
    private void throwErrorClientNotEnableModel() {
        throw new SaOAuth2Exception("应用暂未开放此授权模式").setCode(SaOAuth2ErrorCode.CODE_30142);
    }

    // 这里可以做成策略模式
    private AccessTokenModel getAccessToken(RefreshDto param, String clientId, List<String> scopes) {
        // 获取参数
        String refreshToken = param.getRefreshToken();

        // 校验：Refresh-Token 是否存在
        RefreshTokenModel rt = SaOAuth2Manager.getDao().getRefreshToken(refreshToken);
        SaOAuth2RefreshTokenException.throwBy(rt == null, "无效refresh_token: " + refreshToken, refreshToken, SaOAuth2ErrorCode.CODE_30111);

        // 校验：Refresh-Token 代表的 ClientId 与提供的 ClientId 是否一致
        SaOAuth2ClientModelException.throwBy(!rt.getClientId().equals(clientId), "无效client_id: " + clientId, clientId, SaOAuth2ErrorCode.CODE_30122);

        // 获取新 Access-Token
        return SaOAuth2Manager.getDataGenerate().refreshAccessToken(refreshToken);
    }

    /**
     * 构建返回值: 凭证式 模式认证 获取 token
     */
    private ClientTokenVo buildClientTokenReturnValue(ClientTokenModel ct) {
        ClientTokenVo vo = new ClientTokenVo();
        vo.setClientToken(ct.getClientToken());
        vo.setClientToken(ct.getClientToken());
        if (SaOAuth2Manager.getServerConfig().getMode4ReturnAccessToken()) {
            vo.setAccessToken(ct.getClientToken());
        }
        vo.setExpiresIn(ct.getExpiresIn());
        vo.setClientId(ct.getClientId());
        vo.setScope(SaOAuth2Manager.getDataConverter().convertScopeListToString(ct.scopes));
        ct.getExtraData().forEach(vo::addExtra);
        return vo;
    }

    private TokenVo buildRefreshTokenReturnValue(AccessTokenModel at) {
        TokenVo vo = new TokenVo();
        vo.setTokenType(at.getTokenType());
        vo.setAccessToken(at.getAccessToken());
        vo.setRefreshToken(at.getRefreshToken());
        vo.setExpiresIn(at.getExpiresIn());
        vo.setRefreshExpiresIn(at.getRefreshExpiresIn());
        vo.setClientId(at.getClientId());
        vo.setScope(SaOAuth2Manager.getDataConverter().convertScopeListToString(at.getScopes()));
        if (CollUtil.isNotEmpty(at.getExtraData())) {
            at.getExtraData().forEach(vo::addExtra);
        }
        return vo;
    }


}
