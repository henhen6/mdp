package top.mddata.sdk.core.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import okhttp3.Headers;
import okhttp3.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import top.mddata.sdk.core.common.DataNameBuilder;
import top.mddata.sdk.core.common.FileResult;
import top.mddata.sdk.core.common.OpenConfig;
import top.mddata.sdk.core.common.RequestForm;
import top.mddata.sdk.core.common.Result;
import top.mddata.sdk.core.common.SopSdkConstants;
import top.mddata.sdk.core.common.SopSdkErrors;
import top.mddata.sdk.core.exception.SdkException;
import top.mddata.sdk.core.exception.SopSignException;
import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.core.param.DownloadAware;
import top.mddata.sdk.core.param.DownloadRequest;
import top.mddata.sdk.core.sign.SignUtil;
import top.mddata.sdk.core.sign.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * 请求客户端，申明一个即可
 *
 * @author 六如
 */
public class OpenClient {
    private static final Log log = LogFactory.getLog(OpenClient.class);

    /**
     * 默认配置
     */
    private static final OpenConfig DEFAULT_CONFIG = new OpenConfig();

    /**
     * 接口请求url
     */
    private final String url;

    /**
     * 平台提供的appId
     */
    private final String appId;

    /**
     * 开放平台提供的私钥
     */
    private final String privateKey;

    /**
     * 开放平台提供的公钥
     */
    private String publicKeyPlatform;

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
     * 构建请求客户端
     *
     * @param url           接口url
     * @param appId         平台分配的appId
     * @param privateKeyIsv 平台分配的私钥
     */
    public OpenClient(String url, String appId, String privateKeyIsv) {
        this(url, appId, privateKeyIsv, DEFAULT_CONFIG);
    }

    /**
     * 构建请求客户端
     *
     * @param url               接口url
     * @param appId             平台分配的appId
     * @param privateKeyIsv     平台分配的私钥
     * @param publicKeyPlatform 平台分配的公钥
     */
    public OpenClient(String url, String appId, String privateKeyIsv, String publicKeyPlatform) {
        this(url, appId, privateKeyIsv);
        this.publicKeyPlatform = publicKeyPlatform;
    }

    /**
     * 构建请求客户端
     *
     * @param url           接口url
     * @param appId         平台分配的appId
     * @param privateKeyIsv 平台分配的私钥
     * @param openConfig    配置项
     */
    public OpenClient(String url, String appId, String privateKeyIsv, OpenConfig openConfig) {
        if (openConfig == null) {
            throw new IllegalArgumentException("openConfig不能为null");
        }
        this.url = url;
        this.appId = appId;
        this.privateKey = privateKeyIsv;
        this.openConfig = openConfig;

        this.openRequest = new OpenRequest(openConfig);
        this.dataNameBuilder = openConfig.getDataNameBuilder();
    }

    /**
     * 构建请求客户端
     *
     * @param url               接口url
     * @param appId             平台分配的appId
     * @param privateKeyIsv     平台分配的私钥
     * @param publicKeyPlatform 平台分配的公钥
     * @param openConfig        配置项
     */
    public OpenClient(String url, String appId, String privateKeyIsv, String publicKeyPlatform, OpenConfig openConfig) {
        this(url, appId, privateKeyIsv, openConfig);
        this.publicKeyPlatform = publicKeyPlatform;
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
        return this.execute(param, null);
    }

    public <Req> FileResult download(DownloadRequest<Req> request) {
        return download(request, null);
    }

    public <Req> FileResult download(DownloadRequest<Req> request, String accessToken) {
        Result<FileResult> result = execute(request, accessToken);
        return result.getData();
    }


    /**
     * 请求接口
     *
     * @param param     请求对象
     * @param accessToken jwt
     * @param <Req> 对应的Request对象
     * @param <Resp> 对应的Response对象
     * @return 返回Response
     */
    public <Req, Resp> Result<Resp> execute(BaseParam<Req, Resp> param, String accessToken) {
        RequestForm requestForm = param.createRequestForm(this.openConfig);
        // 表单数据
        Map<String, String> form = requestForm.getForm();
        if (accessToken != null) {
            form.put(this.openConfig.getAccessTokenName(), accessToken);
        }
        form.put(this.openConfig.getAppKeyName(), this.appId);

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

        if (param instanceof DownloadAware) {
            try (Response response = openRequest.download(url, requestForm, Collections.emptyMap())) {
                Result result = new Result<>();
                FileResult fileResult = buildFileResult(response);
                result.setData(fileResult);
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

    protected FileResult buildFileResult(Response response) {
        FileResult fileResult = new FileResult();
        Headers headers = response.headers();
        try {
            byte[] bytes = response.body().bytes();
            fileResult.setFileData(bytes);
        } catch (IOException e) {
            log.error("文件不存在", e);
        }
        fileResult.setHeaders(headers);
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

        String sign = jsonObject.getString(openConfig.getSignName());
        // 是否要验证返回的sign
        if (StringUtils.areNotEmpty(sign, publicKeyPlatform)) {
            String signContent = buildBizJson(rootNodeName, resp);
            if (!this.checkResponseSign(signContent, sign, publicKeyPlatform)) {
                return SopSdkErrors.CHECK_RESPONSE_SIGN_ERROR.getErrorResult();
            }
        }
        // 指定下划线转驼峰
        Result<Resp> result = jsonObject.toJavaObject(Result.class, JSONReader.Feature.SupportSmartMatch);

        Object data = jsonObject.get(rootNodeName);
        Resp dataObj;
        if (data == null) {
            result.setData(null);
            return result;
        }
        if (data instanceof JSONArray) {
            JSONArray arr = (JSONArray) data;
            dataObj = (Resp) arr.toJavaList(param.getResponseClass());
        } else {
            dataObj = ((JSONObject) data).toJavaObject(param.getResponseClass());
        }
        result.setData(dataObj);
        return result;
    }

    /**
     * 构建业务json内容。
     * 假设返回的结果是：<br>
     * {"alipay_story_get_response":{"msg":"Success","code":"10000","name":"海底小纵队","id":1},"sign":"xxx"}
     * 将解析得到：<br>
     * {"msg":"Success","code":"10000","name":"海底小纵队","id":1}
     *
     * @param rootNodeName 根节点名称
     * @param body         返回内容
     * @return 返回业务json
     */
    protected String buildBizJson(String rootNodeName, String body) {
        int indexOfRootNode = body.indexOf(rootNodeName);
        if (indexOfRootNode < 0) {
            rootNodeName = SopSdkConstants.ERROR_RESPONSE_KEY;
            indexOfRootNode = body.indexOf(rootNodeName);
        }
        String result = null;
        if (indexOfRootNode > 0) {
            result = buildJsonNodeData(body, rootNodeName, indexOfRootNode);
        }
        return result;
    }

    /**
     * 获取业务结果，如下结果：<br>
     * {"alipay_story_get_response":{"msg":"Success","code":"10000","name":"海底小纵队","id":1},"sign":"xxx"}
     * 将返回：<br>
     * {"msg":"Success","code":"10000","name":"海底小纵队","id":1}
     *
     * @param body            返回内容
     * @param rootNodeName    根节点名称
     * @param indexOfRootNode 根节点名称位置
     * @return 返回业务json内容
     */
    protected String buildJsonNodeData(String body, String rootNodeName, int indexOfRootNode) {
        /*
          得到起始索引位置。{"alipay_story_get_response":{"msg":"Success","code":"10000","name":"海底小纵队","id":1},"sign":"xxx"}
          得到第二个`{`索引位置
         */
        int signDataStartIndex = indexOfRootNode + rootNodeName.length() + 2;
        // 然后这里计算出"sign"字符串所在位置
        int indexOfSign = body.indexOf("\"" + openConfig.getSignName() + "\"");
        if (indexOfSign < 0) {
            return null;
        }
        int length = indexOfSign - 1;
        // 根据起始位置和长度，截取出json：{"msg":"Success","code":"10000","name":"海底小纵队","id":1}
        return body.substring(signDataStartIndex, length);
    }

    /**
     * 校验返回结果中的sign
     *
     * @param signContent       校验内容
     * @param sign              sign
     * @param publicKeyPlatform 平台公钥
     * @return true：正确
     */
    protected boolean checkResponseSign(String signContent, String sign, String publicKeyPlatform) {
        try {
            String charset = this.openConfig.getCharset();
            String signType = this.openConfig.getSignType();
            return SignUtil.rsaCheck(signContent, sign, publicKeyPlatform, charset, signType);
        } catch (SopSignException e) {
            log.error("验证服务端sign出错，signContent：" + signContent, e);
            return false;
        }
    }


}
