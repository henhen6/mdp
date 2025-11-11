package com.gitee.sop.support.constant;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author 六如
 */
public final class SopConstants {

    public static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;
    public static final String UTF8 = "UTF-8";
    public static final String NULL = "null";
    public static final String DUBBO_TAG = "dubbo.tag";
    public static final String OPEN_CONTEXT = "sop.open-context";
    public static final String WEB_CONTEXT = "sop.web-context";
    public static final String DEFAULT_VERSION = "1.0";
    public static final String SPRING_APPLICATION_NAME = "spring.application.name";

    private SopConstants() {
    }

}
