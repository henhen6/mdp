package top.mddata.gateway.sop.request;

import com.alibaba.fastjson2.JSONObject;
import com.gitee.sop.support.context.WebContext;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;

/**
 * 接口请求上下文
 * @author 六如
 */
@Builder
@Getter
@Setter
public class ApiRequestContext {
    /**
     * 请求参数
     */
    private ApiRequest apiRequest;
    /**
     * 本地语言
     */
    private Locale locale;
    /**
     * 客户端ip
     */
    private String ip;
    /**
     * 全链路唯一ID
     */
    private String traceId;
    /**
     * 上传文件
     */
    private UploadContext uploadContext;

    /**
     * 是否需要签名校验
     */
    @Builder.Default
    private Boolean isRest = false;

    /**
     * 原始参数
     */
    private JSONObject rawParams;

    /**
     * WEB Context
     */
    private WebContext webContext;
}
