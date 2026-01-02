package com.gitee.sop.support.dto;

import lombok.Data;

/**
 * @author 六如
 */
@Data
public class ApiConfig {

    // ========= 请求参数名 =========

    /**
     * 分配给开发者的应用ID
     */
    private String appIdName = "app_id";
    /**
     * 接口名称
     */
    private String apiName = "method";
    /**
     * 仅支持JSON
     */
    private String formatName = "format";
    /**
     * 请求使用的编码格式
     */
    private String charsetName = "charset";
    /**
     * 商户生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2
     */
    private String signTypeName = "sign_type";
    /**
     * 商户请求参数的签名串
     */
    private String signName = "sign";
    /**
     * 发送请求的时间
     */
    private String timestampName = "timestamp";
    /**
     * 调用的接口版本
     */
    private String versionName = "version";
    /**
     * 开放平台主动通知商户服务器里指定的页面http/https路径
     */
    private String notifyUrlName = "notify_url";
    /**
     * OAuth 2.0授权token
     */
    private String appAuthTokenName = "app_auth_token";
    /**
     * 请求参数的集合，最大长度不限，除公共参数外所有请求参数都必须放在这个参数中传递，具体参照各产品快速接入文档
     */
    private String bizContentName = "biz_content";

    // ========= 请求参数名 end =========


    /**
     * 超时时间
     */
    private int timeoutSeconds = 60 * 5;

    /**
     * 是否开启限流功能
     */
    private boolean openLimit = true;

    /**
     * 显示返回sign
     */
    private boolean showReturnSign = true;

    /**
     * 时间戳格式
     */
    private String timestampPattern = "yyyy-MM-dd HH:mm:ss";

    /**
     * 时区,默认:Asia/Shanghai
     */
    private String zoneId = "Asia/Shanghai";

    /**
     * 字段下划线小写形式
     */
    private Boolean fieldSnakeCase = false;

    private String headerKeyTag = "open-tag";
}
