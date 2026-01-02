package com.gitee.sop.support.util;



import com.gitee.sop.support.constant.SopConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author 六如
 */
public class SignConfig {
    private static volatile Wrapper wrapper = new Wrapper() {
    };

    public static void enableUrlencodeMode() {
        wrapper = new Wrapper() {
            @Override
            public String wrapVal(Object val) {
                String valStr = String.valueOf(val);
                try {
                    return URLEncoder.encode(valStr, SopConstants.UTF8);
                } catch (UnsupportedEncodingException e) {
                    return valStr;
                }
            }
        };
    }

    public static String wrapVal(Object val) {
        return wrapper.wrapVal(val);
    }

    interface Wrapper {
        default String wrapVal(Object val) {
            return String.valueOf(val);
        }
    }

}
