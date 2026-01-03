package top.mddata.open.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 通知参数
 * @author henhen
 */
@Data
public class NotifyRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -4018307141661725928L;

    /**
     * appId
     */
    @NotBlank(message = "appId必填")
    private String appId;

    /**
     * apiName
     */
    @NotBlank(message = "apiName必填")
    private String apiName;

    /**
     * version
     */
    @NotBlank(message = "version必填")
    private String version;

    /**
     * 编码
     */
    @NotBlank(message = "charset必填")
    private String charset;

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
     * 业务参数
     */
    @NotNull(message = "bizParams必填")
    private Map<String, Object> bizParams;

    /**
     * 备注
     */
    private String remark;
}
