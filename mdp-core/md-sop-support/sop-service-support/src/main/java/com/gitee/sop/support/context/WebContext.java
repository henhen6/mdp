package com.gitee.sop.support.context;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author 六如
 */
public abstract class WebContext {

    private static final ThreadLocal<WebContext> THREAD_LOCAL = new InheritableThreadLocal<>();

    /**
     * 获取当前WebContext
     *
     * @return 返回WebContext
     */
    public static WebContext current() {
        return THREAD_LOCAL.get();
    }

    public abstract String getMethod();

    public abstract String getPathInfo();

    public abstract String getPathTranslated();

    public abstract String getContextPath();

    public abstract String getQueryString();

    public abstract String getRequestURI();

    public abstract StringBuffer getRequestURL();

    public abstract String getServletPath();

    public abstract int getContentLength();

    public abstract String getContentType();

    public abstract String getRemoteAddr();

    public abstract String getRemoteHost();

    public abstract int getRemotePort();

    public abstract String getParameter(String name);

    public abstract List<String> getParameterNames();

    public abstract String[] getParameterValues(String name);

    public abstract Map<String, String[]> getParameterMap();

    /**
     * 获取locale
     */
    public abstract Locale getLocale();

    /**
     * 获取请求头
     */
    public abstract Map<String, String> getHeaders();

    /**
     * 获取真实ip地址
     */
    public abstract String getRealIp();

    protected void setContext(WebContext openContext) {
        THREAD_LOCAL.set(openContext);
    }

    protected void clear() {
        THREAD_LOCAL.remove();
    }
}
