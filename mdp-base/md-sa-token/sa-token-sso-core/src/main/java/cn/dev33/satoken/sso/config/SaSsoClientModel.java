/*
 * Copyright 2020-2099 sa-token.cc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.dev33.satoken.sso.config;


import cn.dev33.satoken.sso.error.SaSsoErrorCode;
import cn.dev33.satoken.sso.exception.SaSsoException;
import cn.dev33.satoken.util.SaFoxUtil;

import java.io.Serial;
import cn.dev33.satoken.json.SaJsonType;

import java.io.Serializable;
import java.util.List;

/**
 * Sa-Token SSO 客户端信息配置 （在 Server 端配置允许接入的 Client 信息）
 *
 * @author click33
 * @since 1.43.0
 */
public class SaSsoClientModel implements SaJsonType, Serializable {

    @Serial
    private static final long serialVersionUID = -6541180061782004705L;

    /**
     * Client 名称标识
     */
    private String client;

    /**
     * 所有允许的授权回调地址，多个用逗号隔开 (不在此列表中的 URL 将禁止下放 ticket )
     */
    private String allowUrl = "";

    /**
     * 是否接收推送消息
     */
    private Boolean isPush = false;

    /**
     * 是否打开单点注销功能
     */
    private Boolean isSlo = true;

    /**
     * API 调用签名秘钥
     */
    private String secretKey;

    /**
     * 此 Client 端主机总地址
     */
    private String serverUrl;

    /**
     * 此 Client 端推送消息的地址 (如不配置，默认根据 serverUrl + '/sso/pushC' 进行拼接)
     */
    private String pushUrl = "/sso/pushC";


    // 额外添加的一些函数

    /**
     * 以数组形式写入允许的授权回调地址
     * @param url 所有集合
     * @return 对象自身
     */
    public SaSsoClientModel setAllow(String... url) {
        this.setAllowUrl(SaFoxUtil.arrayJoin(url));
        return this;
    }

    /**
     * 获取拼接 url：此 Client 端推送消息的地址
     *
     * @return /
     */
    public String splicingPushUrl() {
        String tempPushUrl = SaFoxUtil.spliceTwoUrl(getServerUrl(), getPushUrl());
        if (!SaFoxUtil.isUrl(tempPushUrl)) {
            throw new SaSsoException("应用 [" + getClient() + "] 推送地址无效：" + tempPushUrl).setCode(SaSsoErrorCode.CODE_30023);
        }
        return tempPushUrl;
    }


    // get set

    /**
     * @return Client 名称标识
     */
    public String getClient() {
        return client;
    }

    /**
     * @param client Client 名称标识
     */
    public SaSsoClientModel setClient(String client) {
        this.client = client;
        return this;
    }

    /**
     * @return 所有允许的授权回调地址，多个用逗号隔开 (不在此列表中的 URL 将禁止下放 ticket )
     */
    public String getAllowUrl() {
        return allowUrl;
    }

    /**
     * @param allowUrl 所有允许的授权回调地址，多个用逗号隔开 (不在此列表中的 URL 将禁止下放 ticket )
     * @return 对象自身
     */
    public SaSsoClientModel setAllowUrl(String allowUrl) {
        // 提前校验一下配置的 allowUrl 是否合法，让开发者尽早发现错误
        if (SaFoxUtil.isNotEmpty(allowUrl)) {
            List<String> allowUrlList = SaFoxUtil.convertStringToList(allowUrl);
            checkAllowUrlListStaticMethod(allowUrlList);
        }
        this.allowUrl = allowUrl;
        return this;
    }

    /**
     * 校验配置的 AllowUrl 是否合规，如果不合规则抛出异常
     * @param allowUrlList 待校验的 allow-url 地址列表
     */
    public static void checkAllowUrlListStaticMethod(List<String> allowUrlList) {
        for (String url : allowUrlList) {
            int index = url.indexOf("*");
            // 如果配置了 * 字符，则必须出现在最后一位，否则属于无效配置项
            if (index != -1 && index != url.length() - 1) {
                //  为什么不允许 * 字符出现在中间位置呢，因为这有可能导致 redirect 参数绕过 allow-url 列表的校验
                //
                //  举个例子 配置文件：
                //      sa-token.sso-server.allow-url=http://*.sa-sso-client1.com
                //
                //  开发者原意是为了允许 sa-sso-client1.com 下的所有子域名都可以下放ticket
                //      例如：http://shop.sa-sso-client1.com
                //
                //  但是如果攻击者精心构建一个url：
                //       http://sa-sso-server.com:9000/sso/auth?redirect=http://sa-token.cc/a.sa-sso-client1.com/sso/login
                //
                //  那么这个 url 就会绕过 allow-url 的校验，ticket 被下发到了第三方服务器地址：
                //       http://sa-token.cc/a.sa-sso-client1.com/sso/login?ticket=v2KKMUFK7dDsMMzXLQ3aWGsyGUjrA0dBB2jeOWrpCnC8b5ScmXXQSv20mIwPK7Cx
                //
                //  造成了 ticket 参数劫持
                //  所以此处需要禁止 allow-url 配置项的中间位置出现 * 字符（出现在末尾是没有问题的）
                //
                //  这么一刀切的做法，可能会导致正常场景下的子域名url也无法通过校验，例如：
                //       http://sa-sso-server.com:9000/sso/auth?redirect=http://shop.sa-sso-client1.com/sso/login
                //
                //  但是为了安全起见，这么做还是有必要的
                throw new SaSsoException("无效的 allow-url 配置（*通配符只允许出现在最后一位）：" + url).setCode(SaSsoErrorCode.CODE_30015);
            }
        }
    }

    /**
     * @return isHttp 是否打开模式三
     */
    public Boolean getIsPush() {
        return isPush;
    }

    /**
     * @param isPush 是否打开模式三
     * @return 对象自身
     */
    public SaSsoClientModel setIsPush(Boolean isPush) {
        this.isPush = isPush;
        return this;
    }

    /**
     * @return 是否打开单点注销功能
     */
    public Boolean getIsSlo() {
        return isSlo;
    }

    /**
     * @param isSlo 是否打开单点注销功能
     * @return 对象自身
     */
    public SaSsoClientModel setIsSlo(Boolean isSlo) {
        this.isSlo = isSlo;
        return this;
    }

    /**
     * 获取 API 调用签名秘钥
     *
     * @return /
     */
    public String getSecretKey() {
        return this.secretKey;
    }

    /**
     * 设置 API 调用签名秘钥
     *
     * @param secretKey /
     * @return 对象自身
     */
    public SaSsoClientModel setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    /**
     * 获取 此 Client 端主机总地址
     *
     * @return serverUrl 此 Client 端主机总地址
     */
    public String getServerUrl() {
        return this.serverUrl;
    }

    /**
     * 设置 此 Client 端主机总地址
     *
     * @param serverUrl 此 Client 端主机总地址
     * @return 对象自身
     */
    public SaSsoClientModel setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        return this;
    }

    /**
     * 获取 此 Client 端推送消息的地址 (如不配置，默认根据 serverUrl + '/sso/pushC' 进行拼接)
     *
     * @return /
     */
    public String getPushUrl() {
        return this.pushUrl;
    }

    /**
     * 设置 此 Client 端推送消息的地址 (如不配置，默认根据 serverUrl + '/sso/pushC' 进行拼接)
     *
     * @param pushUrl 此 Client 端推送消息的地址
     * @return 对象自身
     */
    public SaSsoClientModel setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl;
        return this;
    }

    @Override
    public String toString() {
        return "SaSsoClientModel ["
               + "client=" + client
               + ", allowUrl=" + allowUrl
               + ", isSlo=" + isSlo
               + ", isPush=" + isPush
               + ", secretKey=" + secretKey
               + ", serverUrl=" + serverUrl
               + ", pushUrl=" + pushUrl
               + "]";
    }

}
