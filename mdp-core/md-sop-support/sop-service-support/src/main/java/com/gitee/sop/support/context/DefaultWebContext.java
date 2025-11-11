package com.gitee.sop.support.context;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author 六如
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DefaultWebContext extends WebContext implements Serializable {
    private static final long serialVersionUID = -6070543618514206391L;

    private String method;
    private String pathInfo;
    private String pathTranslated;
    private String contextPath;
    private String queryString;
    private String requestURI;
    private StringBuffer requestURL;
    private String servletPath;
    private int contentLength;
    private String contentType;
    private String remoteAddr;
    private String remoteHost;
    private int remotePort;
    private Locale locale;
    private Map<String, String> headers;
    private Map<String, String[]> paramtreMap;
    private String realIp;

    @Override
    public String getParameter(String name) {
        String[] value = paramtreMap.get(name);
        return value == null || value.length == 0 ? null : String.join(",", value);
    }

    @Override
    public List<String> getParameterNames() {
        return new ArrayList<>(paramtreMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return paramtreMap.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return paramtreMap;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void initContext() {
        this.setContext(this);
    }

    public void remove() {
        this.clear();
    }
}
