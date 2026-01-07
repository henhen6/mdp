package top.mddata.sdk.core.client;

import com.alibaba.fastjson2.JSON;
import okhttp3.Response;
import top.mddata.sdk.core.common.OpenConfig;
import top.mddata.sdk.core.common.RequestForm;
import top.mddata.sdk.core.common.RequestMethod;
import top.mddata.sdk.core.common.Result;
import top.mddata.sdk.core.common.SopSdkErrors;
import top.mddata.sdk.core.common.UploadFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * 负责请求操作
 *
 * @author 六如
 */
public class OpenRequest {

    private final OpenHttp openHttp;

    public OpenRequest(OpenConfig openConfig) {
        this.openHttp = new OpenHttp(openConfig);
    }

    public String request(String url, RequestForm requestForm, Map<String, String> header) {
        try {
            Map<String, String> form = requestForm.getForm();
            List<UploadFile> files = requestForm.getFiles();
            if (files != null && !files.isEmpty()) {
                return openHttp.requestFile(url, form, header, files);
            } else {
                RequestMethod requestMethod = requestForm.getRequestMethod();
                if (requestMethod == RequestMethod.GET) {
                    String query = this.buildGetQueryString(form, requestForm.getCharset());
                    if (query != null && !query.isEmpty()) {
                        url = url + "?" + query;
                    }
                    return openHttp.get(url, header);
                } else {
                    return openHttp.request(url, form, header, requestMethod);
                }
            }
        } catch (IOException e) {
            return this.causeException(e);
        }
    }

    public Response download(String url, RequestForm requestForm, Map<String, String> header) {
        try {
            Map<String, String> form = requestForm.getForm();
            List<UploadFile> files = requestForm.getFiles();
            if (files != null && files.size() > 0) {
                return openHttp.requestFileAndDownload(url, form, header, files);
            } else {
                RequestMethod requestMethod = requestForm.getRequestMethod();
                if (requestMethod == RequestMethod.GET) {
                    String query = this.buildGetQueryString(form, requestForm.getCharset());
                    if (query != null && query.length() > 0) {
                        url = url + "?" + query;
                    }
                    return openHttp.download(url, header);
                } else {
                    return openHttp.download(url, form, header, requestMethod);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String buildGetQueryString(Map<String, String> params, String charset) throws UnsupportedEncodingException {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder query = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (i++ > 0) {
                query.append("&");
            }
            query.append(name).append("=").append(URLEncoder.encode(value, charset));
        }
        return query.toString();
    }

    protected String causeException(Exception e) {
        Result result = SopSdkErrors.HTTP_ERROR.getErrorResult();
        return JSON.toJSONString(result);
    }
}
