package top.mddata.sdk.core.common;

import java.util.List;
import java.util.Map;

/**
 * 请求form
 * @author 六如
 */
public class RequestForm {

    /** 请求表单内容 */
    private Map<String, String> form;
    /** 上传文件 */
    private List<UploadFile> files;

    private String charset;

    private RequestMethod requestMethod = RequestMethod.POST;

    public RequestForm(Map<String, String> m) {
        this.form = m;
    }

    public Map<String, String> getForm() {
        return form;
    }

    public RequestForm setForm(Map<String, String> form) {
        this.form = form;
        return this;
    }

    public List<UploadFile> getFiles() {
        return files;
    }

    public RequestForm setFiles(List<UploadFile> files) {
        this.files = files;
        return this;
    }

    public String getCharset() {
        return charset;
    }

    public RequestForm setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public RequestForm setRequestMethod(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }
}
