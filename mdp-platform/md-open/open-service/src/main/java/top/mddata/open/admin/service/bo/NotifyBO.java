package top.mddata.open.admin.service.bo;

import lombok.Data;

import java.util.Map;

/**
 * @author 六如
 */
@Data
public class NotifyBO {

    /**
     * appId
     */
    private Long appId;
    /**
     * appKey
     */
    private String appKey;

    /**
     * apiName
     */
    private String apiName;

    /**
     * version
     */
    private String version;

    /**
     * token,没有返回null
     */
    private String appAuthToken;

    /**
     * 客户端ip
     */
    private String clientIp;

    /**
     * 回调地址
     */
    private String notifyUrl;

    /**
     * 编码
     */
    private String charset;

    /**
     * 业务参数
     */
    private Map<String, Object> bizParams;

    /**
     * 备注
     */
    private String remark;

}
