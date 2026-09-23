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
import cn.dev33.satoken.oauth2.exception.SaOAuth2Exception;
import cn.dev33.satoken.oauth2.strategy.SaOAuth2Strategy;
import cn.dev33.satoken.oauth2.template.SaOAuth2Template;
import cn.dev33.satoken.oauth2.template.SaOAuth2Util;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.oauth2.core.constant.Oauth2Constants;
import top.mddata.base.oauth2.core.request.Oauth2RevokeRequest;
import top.mddata.base.oauth2.core.request.Oauth2TokenRequest;
import top.mddata.base.oauth2.core.response.Oauth2TokenResponse;
import top.mddata.base.oauth2.core.response.Oauth2UserInfoResponse;
import top.mddata.common.entity.User;
import top.mddata.workbench.service.SsoUserService;

import java.util.List;

import static top.mddata.workbench.oauth2.data.Oauth2DataResolver.readClientIdAndSecret;

/**
 * OAuth2 Server端 资源控制器
 * <p>
 * 响应格式遵循 RFC 6749 / RFC 7009 / OIDC 标准（顶层平铺 JSON + 标准错误格式），
 * 与平台内部接口的 R&lt;&gt; 包装风格不同，请勿混用
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
     * <p>
     * OIDC Core §5.3：UserInfo 端点需同时支持 GET 与 POST
     *
     * @return 用户公开信息（含 OIDC 必填的 sub 字段）
     */
    @RequestMapping(value = "/oauth2/userinfo", method = {RequestMethod.GET, RequestMethod.POST})
    @RequestLog(value = "OAuth2获取用户信息", logType = RequestLog.LogType.QUERY)
    public Oauth2UserInfoResponse getUserinfo() {
        // 校验 Access-Token 是否具有 "userinfo" 权限
        String accessToken = SaOAuth2Manager.getDataResolver().readAccessToken(SaHolder.getRequest());
        SaOAuth2Util.checkAccessTokenScope(accessToken, "userinfo");

        Object loginId = SaOAuth2Util.getLoginIdByAccessToken(accessToken);
        long userId = SaFoxUtil.getValueByType(loginId, long.class);

        User ssoUser = ssoUserService.getById(userId);
        if (ssoUser == null) {
            // token 有效但用户已注销时，按 invalid_token 处理，让客户端重新走授权流程
            throw new SaOAuth2Exception("用户不存在或已注销").setCode(SaOAuth2ErrorCode.CODE_30106);
        }

        Oauth2UserInfoResponse vo = new Oauth2UserInfoResponse();
        vo.setSub(String.valueOf(userId));
        vo.setUsername(ssoUser.getUsername());
        vo.setName(ssoUser.getName());
        vo.setEmail(ssoUser.getEmail());
        vo.setPhone(ssoUser.getPhone());
        vo.setSex(ssoUser.getSex());
        vo.setAvatar(ssoUser.getAvatar());
        return vo;
    }


    /**
     * 通过code 获取 accessToken || 模式三：密码式
     * 【提供给第三方后台调用的】
     *
     * @return 标准令牌响应
     */
    @PostMapping("/oauth2/token")
    @RequestLog(value = "OAuth2获取Token", logType = RequestLog.LogType.QUERY)
    public ResponseEntity<Oauth2TokenResponse> token(@Validated Oauth2TokenRequest param) {
        AccessTokenModel at = SaOAuth2Strategy.instance.grantTypeAuth.apply(SaHolder.getRequest());
        return noStore().body(buildTokenResponse(at));
    }

    /**
     * Refresh-Token 刷新 Access-Token
     * 【提供给第三方后台调用的】
     *
     * @return 标准令牌响应
     */
    @PostMapping("/oauth2/refresh")
    @RequestLog(value = "OAuth2刷新Token", logType = RequestLog.LogType.QUERY)
    public ResponseEntity<Oauth2TokenResponse> refresh(@Validated Oauth2TokenRequest param) {
        // 该端点仅接受 refresh_token 授权类型，其余类型请走 /oauth2/token
        String grantType = param.getGrantType();
        SaOAuth2Exception.throwBy(!Oauth2Constants.GRANT_TYPE_REFRESH_TOKEN.equals(grantType),
                "无效 grant_type：" + grantType, SaOAuth2ErrorCode.CODE_30126);

        AccessTokenModel at = SaOAuth2Strategy.instance.grantTypeAuth.apply(SaHolder.getRequest());
        return noStore().body(buildTokenResponse(at));
    }


    /**
     * 回收 Access-Token / Refresh-Token（RFC 7009）
     * 【提供给第三方后台调用的】
     *
     * @return 固定返回 200 空响应
     */
    @PostMapping("/oauth2/revoke")
    @RequestLog(value = "OAuth2回收Token", logType = RequestLog.LogType.OTHER)
    public ResponseEntity<Void> revoke(@Validated Oauth2RevokeRequest param) {
        SaOAuth2Template oauth2Template = SaOAuth2Manager.getTemplate();
        ClientIdAndSecretModel credentials = readClientIdAndSecret(param);
        String clientId = credentials.getClientId();

        // RFC 7009 §2.1：先认证客户端身份，凭证无效必须返回错误
        oauth2Template.checkClientSecret(clientId, credentials.getClientSecret());

        String token = param.getToken();
        // RFC 7009 §2.2：令牌不存在、已失效或不属于该客户端时同样返回 200，不泄露令牌存在性
        if (Oauth2Constants.TOKEN_TYPE_HINT_REFRESH_TOKEN.equals(param.getTokenTypeHint())) {
            revokeRefreshTokenIfMatch(oauth2Template, token, clientId);
            return ResponseEntity.ok().build();
        }

        AccessTokenModel at = oauth2Template.getAccessToken(token);
        if (at != null && at.getClientId().equals(clientId)) {
            oauth2Template.revokeAccessToken(token);
            // 级联撤销关联的 Refresh-Token，避免残留可换发新 token 的凭证
            if (SaFoxUtil.isNotEmpty(at.getRefreshToken())) {
                oauth2Template.revokeRefreshToken(at.getRefreshToken());
            }
            return ResponseEntity.ok().build();
        }

        // 按 access_token 未命中时，尝试按 refresh_token 撤销
        revokeRefreshTokenIfMatch(oauth2Template, token, clientId);
        return ResponseEntity.ok().build();
    }


    /**
     * 模式四：凭证式
     * 【提供给第三方后台调用的】
     *
     * @return 标准令牌响应（access_token 即 client_token 值）
     */
    @PostMapping("/oauth2/client_token")
    @RequestLog(value = "OAuth2客户端凭证模式获取Token", logType = RequestLog.LogType.QUERY)
    public ResponseEntity<Oauth2TokenResponse> clientToken(@Validated Oauth2TokenRequest param) {
        SaOAuth2ServerConfig cfg = SaOAuth2Manager.getServerConfig();
        SaOAuth2Template oauth2Template = SaOAuth2Manager.getTemplate();

        String grantType = param.getGrantType();
        if (!Oauth2Constants.GRANT_TYPE_CLIENT_CREDENTIALS.equals(grantType)) {
            throw new SaOAuth2Exception("无效 grant_type：" + grantType).setCode(SaOAuth2ErrorCode.CODE_30126);
        }
        if (!cfg.getEnableClientCredentials()) {
            throwErrorSystemNotEnableModel();
        }
        ClientIdAndSecretModel credentials = readClientIdAndSecret(param);
        String clientId = credentials.getClientId();
        SaClientModel clientModel = oauth2Template.checkClientModel(clientId);

        if (!clientModel.getAllowGrantTypes().contains(GrantType.client_credentials)) {
            throwErrorClientNotEnableModel();
        }

        List<String> scopes = SaOAuth2Manager.getDataConverter().convertScopeStringToList(param.getScope());

        // 校验 ClientScope
        oauth2Template.checkContractScope(clientId, scopes);
        // 校验 ClientSecret
        oauth2Template.checkClientSecret(clientId, credentials.getClientSecret());

        // 生成
        ClientTokenModel ct = SaOAuth2Manager.getDataGenerate().generateClientToken(clientId, scopes);

        return noStore().body(buildClientTokenResponse(ct));
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

    /**
     * Refresh-Token 存在且归属当前客户端时才撤销，其他情况静默忽略
     */
    private void revokeRefreshTokenIfMatch(SaOAuth2Template oauth2Template, String token, String clientId) {
        RefreshTokenModel rt = oauth2Template.getRefreshToken(token);
        if (rt != null && rt.getClientId().equals(clientId)) {
            oauth2Template.revokeRefreshToken(token);
        }
    }

    /**
     * RFC 6749 §5.1：令牌响应禁止缓存
     */
    private ResponseEntity.BodyBuilder noStore() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache");
    }

    /**
     * 构建返回值: 凭证式 模式认证 获取 token
     * <p>
     * 按 RFC 6749 标准，client_token 以 access_token 字段返回
     */
    private Oauth2TokenResponse buildClientTokenResponse(ClientTokenModel ct) {
        Oauth2TokenResponse vo = new Oauth2TokenResponse();
        vo.setAccessToken(ct.getClientToken());
        vo.setTokenType(Oauth2Constants.TOKEN_TYPE_BEARER);
        vo.setExpiresIn(ct.getExpiresIn());
        vo.setClientId(ct.getClientId());
        vo.setScope(SaOAuth2Manager.getDataConverter().convertScopeListToString(ct.scopes));
        ct.getExtraData().forEach(vo::addExtra);
        return vo;
    }

    private Oauth2TokenResponse buildTokenResponse(AccessTokenModel at) {
        Oauth2TokenResponse vo = new Oauth2TokenResponse();
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
