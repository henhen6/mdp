package com.gitee.sop.support.context;

import java.util.Locale;

/**
 * @author 六如
 */
public abstract class OpenContext {

    private static final ThreadLocal<OpenContext> THREAD_LOCAL = new InheritableThreadLocal<>();

    /**
     * 获取当前OpenContext
     *
     * @return 返回OpenContext
     */
    public static OpenContext current() {
        return THREAD_LOCAL.get();
    }

    /**
     * 获取appId
     */
    public abstract String getAppKey();

    /**
     * 获取apiName
     */
    public abstract String getApiName();

    /**
     * 获取version
     */
    public abstract String getVersion();

    /**
     * 获取token,没有返回null
     */
    public abstract String getAppAuthToken();

    /**
     * 获取客户端ip
     */
    public abstract String getClientIp();

    /**
     * 获取回调地址
     */
    public abstract String getNotifyUrl();

    /**
     * 获取唯一请求id
     */
    public abstract String getTraceId();

    /**
     * 获取locale
     */
    public abstract Locale getLocale();

    /**
     * 获取charset
     */
    public abstract String getCharset();

    protected void setContext(OpenContext openContext) {
        THREAD_LOCAL.set(openContext);
    }

    protected void clear() {
        THREAD_LOCAL.remove();
    }

}
