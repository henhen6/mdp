package com.gitee.sop.support.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 回调信息
 *
 * @author 六如
 */
@Data
public class NotifyInfo implements Serializable {
    private static final long serialVersionUID = -2492336644456313771L;

    /**
     * 链路id
     */
    private String traceId;

    /**
     * 回调接口
     */
    private String url;

    /**
     * 跟在url后面的参数
     */
    private Map<String, Object> query;

    /**
     * 请求头
     */
    private Map<String, Object> header;

    /**
     * 请求体
     */
    private Map<String, Object> body;

}
