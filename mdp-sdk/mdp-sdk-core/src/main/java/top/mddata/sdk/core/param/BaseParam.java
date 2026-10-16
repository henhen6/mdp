package top.mddata.sdk.core.param;

import com.alibaba.fastjson2.JSON;
import top.mddata.sdk.core.common.OpenConfig;
import top.mddata.sdk.core.common.RequestForm;
import top.mddata.sdk.core.common.RequestMethod;
import top.mddata.sdk.core.common.UploadFile;
import top.mddata.sdk.core.util.ClassUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请求对象父类，后续请求对象都要继承这个类
 * <p>
 * 参数	            类型	    是否必填	    最大长度	    描述	            示例值
 * appKey	        String	是	        32	    支付宝分配给开发者的应用ID	2014072300007148
 * method	        String	是	        128	    接口名称	alipay.trade.fastpay.refund.query
 * version	        String	是	        3	    调用的接口版本，固定为：1.0	1.0
 * format	        String	否	        40	    仅支持JSON	JSON
 * charset	    String	是	        10	    请求使用的编码格式，如utf-8,gbk,gb2312等	utf-8
 * signType	    String	是	        10	    商户生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2	RSA2
 * sign	        String	是	        344	    商户请求参数的签名串，详见签名	详见示例
 * timestamp	    String	是	        19	    发送请求的时间，格式"yyyy-MM-dd HH:mm:ss"	2014-07-24 03:07:50
 * accessToken	    String	否	        512	    访问令牌，通过 accessToken.get 接口获取
 * bizContent	    String	是		请求参数的集合，最大长度不限，除公共参数外所有请求参数都必须放在这个参数中传递，具体参照各产品快速接入文档
 *
 * @param <Req> 对应的Request对象
 * @param <Resp> 对应的Response对象
 *
 * @author 六如
 */
public abstract class BaseParam<Req, Resp> {

    private static final String EMPTY_JSON = "{}";

    private final String method;
    private final Class<Resp> responseClass = initResponseClass();
    private final Type responseType = initResponseType();

    /**
     * 解析 Resp 泛型对应的 Class。
     * 常规子类（如 XxxApi extends BaseParam&lt;XxxReq, XxxResp&gt;）直接取父类第 2 个泛型参数；
     * 单泛型中间父类（如 DownloadRequest&lt;Req&gt;）没有第 2 个泛型参数，返回 null，
     * 由中间类覆写 getResponseClass() 提供（旧版在此直接抛"泛型索引不正确"，DownloadRequest 子类无法实例化）。
     */
    @SuppressWarnings("unchecked")
    private Class<Resp> initResponseClass() {
        Type superType = this.getClass().getGenericSuperclass();
        if (superType instanceof ParameterizedType parameterizedType
                && parameterizedType.getActualTypeArguments().length > 1) {
            return (Class<Resp>) ClassUtil.getSuperClassGenricType(this.getClass(), 1);
        }
        return null;
    }

    /**
     * 解析 Resp 泛型对应的完整 Type，保留 Page&lt;UserResp&gt; 这类参数化信息。
     * responseClass 只是裸 Class（如 Page.class），按它反序列化时集合元素会退化为 JSONObject；
     * 与 initResponseClass() 同理：单泛型中间父类（DownloadRequest&lt;Req&gt;）返回 null，由中间类覆写 getResponseType()。
     */
    private Type initResponseType() {
        Type superType = this.getClass().getGenericSuperclass();
        if (superType instanceof ParameterizedType parameterizedType
                && parameterizedType.getActualTypeArguments().length > 1) {
            return parameterizedType.getActualTypeArguments()[1];
        }
        return null;
    }
    /**
     * -- SETTER --
     *  指定版本号
     *
     */
    private String version;
    /**
     * 回调地址
     */
    private String notifyUrl;
    private String bizContent = EMPTY_JSON;
    private Req bizModel;
    private RequestMethod requestMethod = RequestMethod.POST;
    /**
     * 上传文件
     */
    private List<UploadFile> files;
    /**
     * 是否启用签名，为 null 时使用 OpenConfig 的默认值
     */
    private Boolean signEnabled;
    /**
     * 本次请求的访问令牌，为空时使用 OpenClient 的默认令牌
     */
    private String accessToken;

    public BaseParam() {
        this.method = method();
        this.version = version();
    }

    protected BaseParam(String method, String version) {
        this.method = method;
        this.version = version;
    }

    /**
     * 定义接口名称
     *
     * 被调用接口（使用@Open标记的接口），value值
     *
     * @return 接口名称
     */
    protected abstract String method();

    protected String version() {
        return version;
    }

    /**
     * 添加上传文件
     *
     * @param file 文件
     */
    public void addFile(UploadFile file) {
        if (this.files == null) {
            this.files = new ArrayList<>();
        }
        this.files.add(file);
    }

    public RequestForm createRequestForm(OpenConfig openConfig) {
        // 公共请求参数
        Map<String, String> params = new SkipNullHashMap(20);
        params.put(openConfig.getMethodName(), this.method);
        params.put(openConfig.getFormatName(), openConfig.getFormatType());
        params.put(openConfig.getCharsetName(), openConfig.getCharset());
        params.put(openConfig.getSignTypeName(), openConfig.getSignType());
        String timestamp = new SimpleDateFormat(openConfig.getTimestampPattern()).format(new Date());
        params.put(openConfig.getTimestampName(), timestamp);
        String v = this.version == null ? openConfig.getDefaultVersion() : this.version;
        params.put(openConfig.getVersionName(), v);
        params.put(openConfig.getNotifyUrl(), this.notifyUrl);

        // 业务参数
        String biz_content = buildBizContent();

        params.put(openConfig.getDataName(), biz_content);

        RequestForm requestForm = new RequestForm(params);
        requestForm.setRequestMethod(getRequestMethod());
        requestForm.setCharset(openConfig.getCharset());
        requestForm.setFiles(this.files);
        return requestForm;
    }

    protected String buildBizContent() {
        if (bizModel != null) {
            return JSON.toJSONString(bizModel);
        } else {
            return this.bizContent;
        }
    }

    static class SkipNullHashMap extends HashMap<String, String> {
        private static final long serialVersionUID = -5660619374444097587L;

        SkipNullHashMap(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public String put(String key, String value) {
            if (value == null) {
                return null;
            }
            return super.put(key, value);
        }
    }

    public String getMethod() {
        return method;
    }

    public Class<Resp> getResponseClass() {
        return responseClass;
    }

    /**
     * Resp 的完整泛型 Type（如 Page&lt;UserResp&gt;），用于保留元素类型的反序列化；可能为 null（单泛型中间父类场景）
     */
    public Type getResponseType() {
        return responseType;
    }

    public String getVersion() {
        return version;
    }

    public BaseParam<Req, Resp> setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public BaseParam<Req, Resp> setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
        return this;
    }

    public String getBizContent() {
        return bizContent;
    }

    public BaseParam<Req, Resp> setBizContent(String bizContent) {
        this.bizContent = bizContent;
        return this;
    }

    public Req getBizModel() {
        return bizModel;
    }

    public BaseParam<Req, Resp> setBizModel(Req bizModel) {
        this.bizModel = bizModel;
        return this;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public BaseParam<Req, Resp> setRequestMethod(RequestMethod requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }

    public List<UploadFile> getFiles() {
        return files;
    }

    public BaseParam<Req, Resp> setFiles(List<UploadFile> files) {
        this.files = files;
        return this;
    }

    public Boolean getSignEnabled() {
        return signEnabled;
    }

    public BaseParam<Req, Resp> setSignEnabled(Boolean signEnabled) {
        this.signEnabled = signEnabled;
        return this;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public BaseParam<Req, Resp> setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }
}
