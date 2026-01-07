package top.mddata.gateway.sop.service.impl;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gitee.sop.support.constant.SopConstants;
import com.gitee.sop.support.dto.FileData;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import top.mddata.gateway.sop.config.GateApiConfig;
import top.mddata.gateway.sop.request.ApiRequest;
import top.mddata.gateway.sop.request.ApiRequestContext;
import top.mddata.gateway.sop.request.UploadContext;
import top.mddata.gateway.sop.response.FormatEnum;
import top.mddata.gateway.sop.response.NoCommonResponse;
import top.mddata.gateway.sop.response.Response;
import top.mddata.gateway.sop.service.ParamExecutor;
import top.mddata.gateway.sop.service.Serde;
import top.mddata.gateway.sop.util.RequestUtil;
import top.mddata.gateway.sop.util.ResponseUtil;

import java.io.IOException;
import java.util.UUID;


/**
 * 请求参数默认实现
 *
 * @author 六如
 */
@Slf4j
@Service
public class ParamExecutorImpl implements ParamExecutor<HttpServletRequest, HttpServletResponse> {

    private static final String CONTENT_TYPE = "content-type";
    private static final String JSON_NAME = "json";
    private static final String TEXT_NAME = "text";
    private static final String MULTIPART = "multipart";
    private static final String FORM = "form";

    @Resource
    private GateApiConfig apiConfig;

    @Resource
    private Serde serde;

    @Override
    public ApiRequestContext build(HttpServletRequest request) {
        // get请求可能返回null
        String contentType = request.getHeader(CONTENT_TYPE);
        if (contentType == null) {
            contentType = "";
        }
        ApiRequest apiRequest = new ApiRequest();
        String ip = JakartaServletUtil.getClientIP(request);
        byte[] body;
        try {
            body = IOUtils.toByteArray(request.getInputStream());
        } catch (IOException e) {
            log.error("获取请求体失败", e);
            body = new byte[0];
        }
        JSONObject params = null;
        UploadContext uploadContext = null;
        String contentTypeStr = contentType.toLowerCase();
        // 如果是json方式提交
        if (StringUtils.containsAny(contentTypeStr, JSON_NAME, TEXT_NAME)) {
            params = JSON.parseObject(body);
        } else if (StringUtils.containsIgnoreCase(contentTypeStr, MULTIPART)) {
            // 如果是文件上传请求
            RequestUtil.UploadInfo uploadInfo = RequestUtil.getUploadInfo(request);
            params = uploadInfo.getApiParam();
            uploadContext = uploadInfo.getUploadContext();
        } else if (StringUtils.containsIgnoreCase(contentTypeStr, FORM)) {
            // APPLICATION_FORM_URLENCODED请求
            params = RequestUtil.parseQuerystring(new String(body, SopConstants.CHARSET_UTF8));
        } else {
            // get请求,参数跟在url后面
            params = RequestUtil.parseParameterMap(request.getParameterMap());
        }
        if (params != null) {
            apiRequest = convertApiRequest(request, params);
        }

        return ApiRequestContext.builder()
                .apiRequest(apiRequest)
                .locale(request.getLocale())
                .ip(ip)
                .uploadContext(uploadContext)
                .traceId(UUID.randomUUID().toString().replace("-", ""))
                .rawParams(params)
                .isRest(false)
                .build();
    }


    protected ApiRequest convertApiRequest(HttpServletRequest request, JSONObject jsonObject) {
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setAppKey(jsonObject.getString(apiConfig.getAppKeyName()));
        apiRequest.setMethod(jsonObject.getString(apiConfig.getApiName()));
        apiRequest.setFormat(jsonObject.getString(apiConfig.getFormatName()));
        apiRequest.setCharset(jsonObject.getOrDefault(apiConfig.getCharsetName(), SopConstants.UTF8).toString());
        apiRequest.setSignType(jsonObject.getString(apiConfig.getSignTypeName()));
        apiRequest.setSign(jsonObject.getString(apiConfig.getSignName()));
        apiRequest.setTimestamp(jsonObject.getString(apiConfig.getTimestampName()));
        apiRequest.setVersion(jsonObject.getString(apiConfig.getVersionName()));
        apiRequest.setNotifyUrl(jsonObject.getString(apiConfig.getNotifyUrlName()));
        apiRequest.setAppAuthToken(jsonObject.getString(apiConfig.getAppAuthTokenName()));
        String bizContent = jsonObject.getString(apiConfig.getBizContentName());
        apiRequest.setBizContent(bizContent);
        return apiRequest;
    }


    @Override
    public void write(ApiRequestContext apiRequestContext, Response apiResponse, HttpServletResponse response) throws IOException {
        Object data = apiResponse.getData();
        if (data instanceof FileData) {
            // 处理文件下载
            FileData fileData = (FileData) data;
            ResponseUtil.writerFile(fileData, response);
        } else {
            Object responseData = apiResponse;
            // 不需要公共参数
            if (apiResponse instanceof NoCommonResponse || !apiResponse.needWrap()) {
                responseData = data;
            }
            this.writerText(apiRequestContext, responseData, response);
        }
    }

    protected void writerText(ApiRequestContext apiRequestContext, Object apiResponse, HttpServletResponse response) throws IOException {
        ApiRequest apiRequest = apiRequestContext.getApiRequest();
        String charset = apiRequest.getCharset();
        response.setCharacterEncoding(charset);
        String format = apiRequest.getFormat();
        if (FormatEnum.of(format) == FormatEnum.XML) {
            response.setContentType(MediaType.APPLICATION_XML_VALUE);
            String xml = serde.toXml(apiResponse);
            response.getWriter().write(xml);
        } else {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String json = serde.toJson(apiResponse);
            response.getWriter().write(json);
        }
    }
}
