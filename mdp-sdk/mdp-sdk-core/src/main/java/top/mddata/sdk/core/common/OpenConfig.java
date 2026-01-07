package top.mddata.sdk.core.common;

/**
 * 配置
 * @author 六如
 */
public class OpenConfig {
    public static final DataNameBuilder DATA_NAME_BUILDER = new CustomDataNameBuilder();

    /** 成功返回码值 */
    private String successCode = "0";
    /** 默认版本号 */
    private String defaultVersion = "1.0";
    /** 字符编码 */
    private String charset = "UTF-8";
    /** 签名方式 */
    private String signType = "RSA2";
    /** 格式类型名称 */
    private String formatType = "json";
    /** 时间戳格式 */
    private String timestampPattern = "yyyy-MM-dd HH:mm:ss";

    /** 接口属性名 */
    private String methodName = "method";
    /** 版本号名称 */
    private String versionName = "version";
    /** 编码名称 */
    private String charsetName = "charset";
    /** appKey名称 */
    private String appKeyName = "app_key";
    /** data名称 */
    private String dataName = "biz_content";
    /** 回调地址 */
    private String notifyUrl = "notify_url";
    /** 时间戳名称 */
    private String timestampName = "timestamp";
    /** 签名串名称 */
    private String signName = "sign";
    /** 签名类型名称 */
    private String signTypeName = "sign_type";
    /** 格式化名称 */
    private String formatName = "format";
    /** accessToken名称 */
    private String accessTokenName = "app_auth_token";
    /** 国际化语言 */
    private String locale = "zh-CN";
    /** 响应code名称 */
    private String responseCodeName = "code";
    /** 错误响应节点 */
    private String errorResponseName = "error_response";

    /** 请求超时时间 */
    private int connectTimeoutSeconds = 60;
    /** http读取超时时间 */
    private int readTimeoutSeconds = 60;
    /** http写超时时间 */
    private int writeTimeoutSeconds = 60;

    /**
     * 构建数据节点名称
     */
    private DataNameBuilder dataNameBuilder = DATA_NAME_BUILDER;

    public String getSuccessCode() {
        return successCode;
    }

    public OpenConfig setSuccessCode(String successCode) {
        this.successCode = successCode;
        return this;
    }

    public String getDefaultVersion() {
        return defaultVersion;
    }

    public OpenConfig setDefaultVersion(String defaultVersion) {
        this.defaultVersion = defaultVersion;
        return this;
    }

    public String getCharset() {
        return charset;
    }

    public OpenConfig setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    public String getSignType() {
        return signType;
    }

    public OpenConfig setSignType(String signType) {
        this.signType = signType;
        return this;
    }

    public String getFormatType() {
        return formatType;
    }

    public OpenConfig setFormatType(String formatType) {
        this.formatType = formatType;
        return this;
    }

    public String getTimestampPattern() {
        return timestampPattern;
    }

    public OpenConfig setTimestampPattern(String timestampPattern) {
        this.timestampPattern = timestampPattern;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public OpenConfig setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public String getVersionName() {
        return versionName;
    }

    public OpenConfig setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public OpenConfig setCharsetName(String charsetName) {
        this.charsetName = charsetName;
        return this;
    }

    public String getAppKeyName() {
        return appKeyName;
    }

    public OpenConfig setAppKeyName(String appKeyName) {
        this.appKeyName = appKeyName;
        return this;
    }

    public String getDataName() {
        return dataName;
    }

    public OpenConfig setDataName(String dataName) {
        this.dataName = dataName;
        return this;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public OpenConfig setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
        return this;
    }

    public String getTimestampName() {
        return timestampName;
    }

    public OpenConfig setTimestampName(String timestampName) {
        this.timestampName = timestampName;
        return this;
    }

    public String getSignName() {
        return signName;
    }

    public OpenConfig setSignName(String signName) {
        this.signName = signName;
        return this;
    }

    public String getSignTypeName() {
        return signTypeName;
    }

    public OpenConfig setSignTypeName(String signTypeName) {
        this.signTypeName = signTypeName;
        return this;
    }

    public String getFormatName() {
        return formatName;
    }

    public OpenConfig setFormatName(String formatName) {
        this.formatName = formatName;
        return this;
    }

    public String getAccessTokenName() {
        return accessTokenName;
    }

    public OpenConfig setAccessTokenName(String accessTokenName) {
        this.accessTokenName = accessTokenName;
        return this;
    }

    public String getLocale() {
        return locale;
    }

    public OpenConfig setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    public String getResponseCodeName() {
        return responseCodeName;
    }

    public OpenConfig setResponseCodeName(String responseCodeName) {
        this.responseCodeName = responseCodeName;
        return this;
    }

    public String getErrorResponseName() {
        return errorResponseName;
    }

    public OpenConfig setErrorResponseName(String errorResponseName) {
        this.errorResponseName = errorResponseName;
        return this;
    }

    public int getConnectTimeoutSeconds() {
        return connectTimeoutSeconds;
    }

    public OpenConfig setConnectTimeoutSeconds(int connectTimeoutSeconds) {
        this.connectTimeoutSeconds = connectTimeoutSeconds;
        return this;
    }

    public int getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    public OpenConfig setReadTimeoutSeconds(int readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
        return this;
    }

    public int getWriteTimeoutSeconds() {
        return writeTimeoutSeconds;
    }

    public OpenConfig setWriteTimeoutSeconds(int writeTimeoutSeconds) {
        this.writeTimeoutSeconds = writeTimeoutSeconds;
        return this;
    }

    public DataNameBuilder getDataNameBuilder() {
        return dataNameBuilder;
    }

    public OpenConfig setDataNameBuilder(DataNameBuilder dataNameBuilder) {
        this.dataNameBuilder = dataNameBuilder;
        return this;
    }
}
