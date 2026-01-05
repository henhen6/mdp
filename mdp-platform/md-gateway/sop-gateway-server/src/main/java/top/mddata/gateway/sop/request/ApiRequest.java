package top.mddata.gateway.sop.request;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 请求参数名定义
 * <pre>
 * 参数	            类型	   是否必填	最大长度	    描述	                        示例值
 * app_key	        String	是	    32	    平台分配给开发者的应用ID	        2014072300007148
 * method	        String	是	    128	    接口名称	                        alipay.trade.fastpay.refund.query
 * format	        String	否	    40	    仅支持JSON	                    JSON
 * charset	        String	是	    10	    请求使用的编码格式，如utf-8,gbk,gb2312等	utf-8
 * sign_type	    String	是	    10	    商户生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2	RSA2
 * sign	            String	是	    344	    商户请求参数的签名串，详见签名	详见示例
 * timestamp	    String	是	    19	    发送请求的时间，格式"yyyy-MM-dd HH:mm:ss"	2014-07-24 03:07:50
 * version	        String	是	    3	    调用的接口版本，固定为：1.0	1.0
 * notify_url	    String	否	    256	    平台服务器主动通知商户服务器里指定的页面http/https路径。	http://api.test.alipay.net/atinterface/receive_notify.htm
 * app_auth_token	String	否	    40	    详见应用授权概述
 * biz_content	    String	是		        请求参数的集合，最大长度不限，除公共参数外所有请求参数都必须放在这个参数中传递，具体参照各产品快速接入文档
 *</pre>
 * @author 六如
 */
@Data
public class ApiRequest implements Serializable {
    private static final long serialVersionUID = 1815097687653555654L;

    /**
     * 分配给开发者的应用ID
     *
     * @mock 2014072300007148
     */
    @NotBlank(message = "应用ID不能为空")
    @Max(32)
    private String appKey;

    /**
     * 接口名称
     *
     * @mock shop.goods.get
     */
    @NotBlank(message = "method不能为空")
    @Max(128)
    private String method;

    /**
     * 仅支持JSON
     *
     * @mock json
     */
    @NotBlank(message = "format不能为空")
    @Max(40)
    private String format;

    /**
     * 请求使用的编码格式，如utf-8,gbk,gb2312等
     *
     * @mock utf-8
     */
    @Max(10)
    private String charset = "utf-8";

    /**
     * 商户生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2
     *
     * @mock RSA2
     */
    @NotBlank(message = "sign_type不能为空")
    @Max(10)
    private String signType;

    /**
     * 商户请求参数的签名串，详见签名
     */
    @NotBlank(message = "sign不能为空")
    private String sign;

    /**
     * 发送请求的时间，格式"yyyy-MM-dd HH:mm:ss"
     *
     * @mock 2014-07-24 03:07:50
     */
    @NotBlank(message = "timestamp不能为空")
    @Max(19)
    private String timestamp;

    /**
     * 调用的接口版本，固定为：1.0
     *
     * @mock 1.0
     */
    @NotBlank(message = "version不能为空")
    @Max(10)
    private String version;

    /**
     * 平台服务器主动通知商户服务器里指定的页面http/https路径
     *
     * @mock http://ww.xx.com/callback
     */
    @Max(256)
    private String notifyUrl;

    /**
     * 授权token,详见应用授权概述
     */
    @Max(64)
    private String appAuthToken;

    /**
     * 请求参数的集合，最大长度不限，除公共参数外所有请求参数都必须放在这个参数中传递，具体参照各产品快速接入文档
     */
    @NotBlank(message = "biz_content不能为空")
    private String bizContent;

    @JsonIgnore
    @JSONField(serialize = false)
    public String takeNameVersion() {
        return method + version;
    }
}
