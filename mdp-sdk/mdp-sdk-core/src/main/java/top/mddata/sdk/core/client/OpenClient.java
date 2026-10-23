package top.mddata.sdk.core.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mddata.sdk.core.common.DataNameBuilder;
import top.mddata.sdk.core.common.FileResult;
import top.mddata.sdk.core.common.OpenConfig;
import top.mddata.sdk.core.common.RequestForm;
import top.mddata.sdk.core.common.Result;
import top.mddata.sdk.core.exception.SdkException;
import top.mddata.sdk.core.exception.SopSignException;
import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.core.param.DownloadAware;
import top.mddata.sdk.core.param.DownloadRequest;
import top.mddata.sdk.core.sign.SignUtil;
import top.mddata.sdk.core.sign.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

/**
 * 请求客户端，申明一个即可
 *
 * @author 六如
 */
public class OpenClient {
    private static final Logger log = LoggerFactory.getLogger(OpenClient.class);

    /**
     * 默认配置
     */
    private static final OpenConfig DEFAULT_CONFIG = new OpenConfig();

    /**
     * 接口请求url
     */
    private final String url;

    /**
     * 平台提供的appKey
     */
    private final String appKey;

    /**
     * 开放平台提供的私钥
     */
    private final String privateKey;

    /**
     * 配置项
     */
    private final OpenConfig openConfig;

    /**
     * 请求对象
     */
    private final OpenRequest openRequest;

    /**
     * 节点处理
     */
    private final DataNameBuilder dataNameBuilder;

    /**
     * 默认访问令牌，设置后所有请求会自动携带
     */
    private String defaultAccessToken;

    /**
     * 构建请求客户端
     *
     * @param url           接口url
     * @param appKey         平台分配的appKey
     * @param privateKeyIsv 平台分配的私钥
     */
    public OpenClient(String url, String appKey, String privateKeyIsv) {
        this(url, appKey, privateKeyIsv, DEFAULT_CONFIG);
    }

    /**
     * 构建请求客户端
     *
     * @param url           接口url
     * @param appKey         平台分配的appKey
     * @param privateKeyIsv 平台分配的私钥
     * @param openConfig    配置项
     */
    public OpenClient(String url, String appKey, String privateKeyIsv, OpenConfig openConfig) {
        if (openConfig == null) {
            throw new IllegalArgumentException("openConfig不能为null");
        }
        this.url = url;
        this.appKey = appKey;
        this.privateKey = privateKeyIsv;
        this.openConfig = openConfig;

        this.openRequest = new OpenRequest(openConfig);
        this.dataNameBuilder = openConfig.getDataNameBuilder();
    }

    public <Req> FileResult download(DownloadRequest<Req> request) {
        Result<FileResult> result = execute(request);
        return result.getData();
    }

    /**
     * 请求接口
     *
     * @param param 请求对象
     * @param <Req> 对应的Request对象
     * @param <Resp> 对应的Response对象
     * @return 返回Response
     */
    public <Req, Resp> Result<Resp> execute(BaseParam<Req, Resp> param) {
        RequestForm requestForm = param.createRequestForm(this.openConfig);
        // 表单数据
        Map<String, String> form = requestForm.getForm();
        // 优先使用请求中的 accessToken，其次使用默认 accessToken
        String token = StringUtils.isEmpty(param.getAccessToken()) ? this.defaultAccessToken : param.getAccessToken();
        if (token != null) {
            form.put(this.openConfig.getAccessTokenName(), token);
        }
        form.put(this.openConfig.getAppKeyName(), this.appKey);

        // 根据请求参数或全局配置决定是否签名
        boolean needSign = param.getSignEnabled() != null ? param.getSignEnabled() : openConfig.isSignEnabled();
        if (needSign) {
            String content = SignUtil.getSignContent(form);
            String sign;
            try {
                sign = SignUtil.rsaSign(content, privateKey, openConfig.getCharset(), openConfig.getSignType());
            } catch (SopSignException e) {
                throw new SdkException("构建签名错误", e);
            }
            form.put(this.openConfig.getSignName(), sign);

            if (log.isDebugEnabled()) {
                log.debug("----------- 请求信息 -----------"
                          + "\n请求参数：" + SignUtil.getSignContent(form)
                          + "\n待签名内容：" + content
                          + "\n签名(sign)：" + sign
                );
            }
        } else if (log.isDebugEnabled()) {
            log.debug("----------- 请求信息（未签名） -----------"
                      + "\n请求参数：" + SignUtil.getSignContent(form)
            );
        }

        if (param instanceof DownloadAware) {
            try (Response response = openRequest.download(url, requestForm, Collections.emptyMap())) {
                Result<Resp> result = new Result<>();
                FileResult fileResult = buildFileResult(response);
                result.setData((Resp) fileResult);
                return result;
            }
        } else {
            String resp = doExecute(this.url, requestForm, Collections.emptyMap());
            if (log.isDebugEnabled()) {
                log.debug("----------- 返回结果 -----------" + "\n" + resp);
            }
            return this.parseResponse(resp, param);
        }
    }

    /**
     * 获取默认访问令牌
     *
     * @return 默认访问令牌
     */
    public String getDefaultAccessToken() {
        return defaultAccessToken;
    }

    /**
     * 设置默认访问令牌，设置后所有请求会自动携带
     *
     * @param defaultAccessToken 默认访问令牌
     * @return 当前客户端实例
     */
    public OpenClient setDefaultAccessToken(String defaultAccessToken) {
        this.defaultAccessToken = defaultAccessToken;
        return this;
    }

    protected FileResult buildFileResult(Response response) {
        if (!response.isSuccessful()) {
            // 非 2xx 直接失败（旧版会把错误页字节当文件内容返回）
            throw new SdkException("文件下载失败, HTTP状态码=" + response.code());
        }
        FileResult fileResult = new FileResult();
        fileResult.setHeaders(response.headers());
        try {
            byte[] bytes = null;
            if (response.body() != null) {
                bytes = response.body().bytes();
            }
            fileResult.setFileData(bytes);
        } catch (IOException e) {
            // fail fast：不再只记日志后返回半空结果
            throw new SdkException("读取下载文件内容失败", e);
        }
        return fileResult;
    }

    protected String doExecute(String url, RequestForm requestForm, Map<String, String> header) {
        return openRequest.request(url, requestForm, header);
    }

    /**
     * 解析返回结果
     *
     * @param resp    返回结果
     * @param param 请求对象
     * @param <Req> 对应的Request对象
     * @param <Resp> 对应的Response对象
     * @return 返回对于的Response对象
     */
    protected <Req, Resp> Result<Resp> parseResponse(String resp, BaseParam<Req, Resp> param) {
        String method = param.getMethod();
        String rootNodeName = dataNameBuilder.build(method);
        JSONObject jsonObject = JSON.parseObject(resp, JSONReader.Feature.FieldBased);

        // 指定下划线转驼峰
        Result<Resp> result = jsonObject.toJavaObject(Result.class, JSONReader.Feature.SupportSmartMatch);

        Object data = jsonObject.get(rootNodeName);
        Resp dataObj;
        if (data == null) {
            result.setData(null);
            return result;
        }
        // 优先用完整泛型 Type 反序列化：裸 Class（如 Page.class）会丢失元素类型，集合元素退化为 JSONObject
        Type responseType = param.getResponseType() != null ? param.getResponseType() : param.getResponseClass();
        if (data instanceof JSONArray arr) {
            dataObj = (Resp) arr.to(responseType);
        } else if (data instanceof JSONObject obj) {
            dataObj = (Resp) obj.to(responseType);
        } else {
            // data 为标量（字符串/数字等）时直接报具体错误，不再延迟到调用方 CCE
            throw new SdkException("响应" + rootNodeName + "节点类型与预期不符, method=" + param.getMethod()
                    + ", 实际类型=" + data.getClass().getSimpleName() + ", 值=" + data);
        }
        result.setData(dataObj);
        return result;
    }


}
