package top.mddata.base.oauth2.properties;

import cn.dev33.satoken.util.SaFoxUtil;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * oauth2 客户端配置
 * @author henhen6
 * @since 2025/9/3 20:39
 */
@Data
public class Oauth2ClientConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = -2492336644456313771L;
    /**
     * 客户端 应用ID
     */
    private String clientId;
    /**
     * 客户端 应用秘钥
     */
    private String clientSecret;
    /**
     * 配置 Oauth2 Server 端主机总地址
     */
    private String serverUrl;

    /**
     * 获取 授权码 的前端地址
     *
     */
    private String authorizeUrl = "/oauth2/authorize";
    /**
     * 根据授权码获取 Access-Token
     */
    private String tokenUrl = "/oauth2/token";
    /**
     * 根据 Refresh-Token 刷新 Access-Token
     */
    private String refreshUrl = "/oauth2/refresh";
    /**
     * 回收 Access-Token
     */
    private String revokeUrl = "/oauth2/revoke";

    /**
     * 根据 Access-Token 获取相应用户的账号信息
     */
    private String userinfoUrl = "/oauth2/userinfo";
    /**
     * 凭证式（Client Credentials）
     */
    private String clientTokenUrl = "/oauth2/client_token";


    public String splicingAuthorizeUrl() {
        return SaFoxUtil.spliceTwoUrl(getServerUrl(), getAuthorizeUrl());
    }

    public String splicingRefreshUrl() {
        return SaFoxUtil.spliceTwoUrl(getServerUrl(), getRefreshUrl());
    }

    public String splicingTokenUrl() {
        return SaFoxUtil.spliceTwoUrl(getServerUrl(), getTokenUrl());
    }

    public String splicingRevokeUrl() {
        return SaFoxUtil.spliceTwoUrl(getServerUrl(), getRevokeUrl());
    }

    public String splicingUserinfoUrl() {
        return SaFoxUtil.spliceTwoUrl(getServerUrl(), getUserinfoUrl());
    }

    public String splicingClientTokenUrl() {
        return SaFoxUtil.spliceTwoUrl(getServerUrl(), getClientTokenUrl());
    }
}
