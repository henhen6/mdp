package top.mddata.gateway.sop.message;

import com.gitee.sop.support.message.I18nMessage;
import com.gitee.sop.support.message.OpenMessage;
import com.gitee.sop.support.message.OpenMessageFactory;
import lombok.Getter;

import java.util.Locale;

/**
 * 网关错误定义
 *
 * @author 六如
 */
@Getter
public enum ErrorEnum implements I18nMessage {
    /**
     * 成功
     */
    SUCCESS(CodeEnum.SUCCESS, ""),

    /**
     * 服务暂不可用
     */
    ISP_UNKNOWN_ERROR(CodeEnum.UNKNOWN, "isp.unknown-error"),
    /**
     * 微服务未知错误
     */
    ISP_SERVICE_UNKNOWN_ERROR(CodeEnum.UNKNOWN, "isp.service-unknown-error"),
    /**
     * 服务不可用，路由被禁用
     */
    ISP_API_DISABLED(CodeEnum.UNKNOWN, "isp.service-not-available"),
    /**
     * 网关响应超时
     */
    ISP_GATEWAY_RESPONSE_TIMEOUT(CodeEnum.UNKNOWN, "isp.gateway-response-timeout"),
    /**
     * 限流处理
     */
    ISV_REQUEST_LIMIT(CodeEnum.UNKNOWN, "isv.service-busy"),

    /**
     * 无效的访问令牌
     */
    AOP_INVALID_AUTH_TOKEN(CodeEnum.AUTH, "aop.invalid-auth-token"),
    /**
     * 访问令牌已过期
     */
    AOP_AUTH_TOKEN_TIME_OUT(CodeEnum.AUTH, "aop.auth-token-time-out"),
    /**
     * 无效的应用授权令牌
     */
    AOP_INVALID_APP_AUTH_TOKEN(CodeEnum.AUTH, "aop.invalid-app-auth-token"),
    /**
     * 商户未授权当前接口
     */
    AOP_INVALID_APP_AUTH_TOKEN_NO_API(CodeEnum.AUTH, "aop.invalid-app-auth-token-no-api"),
    /**
     * 应用授权令牌已过期
     */
    AOP_APP_AUTH_TOKEN_TIME_OUT(CodeEnum.AUTH, "aop.app-auth-token-time-out"),
    /**
     * 商户未签约任何产品
     */
    AOP_NO_PRODUCT_REG_BY_PARTNER(CodeEnum.AUTH, "aop.no-product-reg-by-partner"),

    /**
     * 缺少方法名参数
     */
    ISV_MISSING_METHOD(CodeEnum.MISSING, "isv.missing-method"),
    /**
     * 缺少签名参数
     */
    ISV_MISSING_SIGNATURE(CodeEnum.MISSING, "isv.missing-signature"),
    /**
     * 缺少签名类型参数
     */
    ISV_MISSING_SIGNATURE_TYPE(CodeEnum.MISSING, "isv.missing-signature-type"),
    /**
     * 缺少签名配置
     */
    ISV_MISSING_SIGNATURE_KEY(CodeEnum.MISSING, "isv.missing-signature-key"),
    /**
     * 缺少appKey参数
     */
    ISV_MISSING_APP_KEY(CodeEnum.MISSING, "isv.missing-app-key"),
    /**
     * 缺少时间戳参数
     */
    ISV_MISSING_TIMESTAMP(CodeEnum.MISSING, "isv.missing-timestamp"),
    /**
     * 缺少版本参数
     */
    ISV_MISSING_VERSION(CodeEnum.MISSING, "isv.missing-version"),
    /**
     * 解密出错, 未指定加密算法
     */
    ISV_DECRYPTION_ERROR_MISSING_ENCRYPT_TYPE(CodeEnum.MISSING, "isv.decryption-error-missing-encrypt-type"),

    /**
     * 参数无效
     */
    ISV_INVALID_PARAMETER(CodeEnum.INVALID, "isv.invalid-parameter"),
    /**
     * 参数不正确
     */
    ISV_ERROR_PARAMETER(CodeEnum.INVALID, "isv.error-parameter"),
    /**
     * 文件上传失败
     */
    ISV_UPLOAD_FAIL(CodeEnum.INVALID, "isv.upload-fail"),
    /**
     * 文件扩展名无效
     */
    ISV_INVALID_FILE_EXTENSION(CodeEnum.INVALID, "isv.invalid-file-extension"),
    /**
     * 文件大小无效
     */
    ISV_INVALID_FILE_SIZE(CodeEnum.INVALID, "isv.invalid-file-size"),
    /**
     * 不存在的方法名
     */
    ISV_INVALID_METHOD(CodeEnum.INVALID, "isv.invalid-method"),
    /**
     * 无效的数据格式
     */
    ISV_INVALID_FORMAT(CodeEnum.INVALID, "isv.invalid-format"),
    /**
     * 无效的签名类型
     */
    ISV_INVALID_SIGNATURE_TYPE(CodeEnum.INVALID, "isv.invalid-signature-type"),
    /**
     * 无效签名
     */
    ISV_INVALID_SIGNATURE(CodeEnum.INVALID, "isv.invalid-signature"),
    /**
     * 无效的加密类型
     */
    ISV_INVALID_ENCRYPT_TYPE(CodeEnum.INVALID, "isv.invalid-encrypt-type"),
    /**
     * 解密异常
     */
    ISV_INVALID_ENCRYPT(CodeEnum.INVALID, "isv.invalid-encrypt"),
    /**
     * 无效的appKey参数
     */
    ISV_INVALID_APP_KEY(CodeEnum.INVALID, "isv.invalid-app-key"),
    /**
     * 非法的时间戳参数
     */
    ISV_INVALID_TIMESTAMP(CodeEnum.INVALID, "isv.invalid-timestamp"),
    /**
     * 字符集错误
     */
    ISV_INVALID_CHARSET(CodeEnum.INVALID, "isv.invalid-charset"),
    /**
     * 摘要错误
     */
    ISV_INVALID_DIGEST(CodeEnum.INVALID, "isv.invalid-digest"),
    /**
     * 解密出错，不支持的加密算法
     */
    ISV_DECRYPTION_ERROR_NOT_VALID_ENCRYPT_TYPE(CodeEnum.INVALID, "isv.decryption-error-not-valid-encrypt-type"),
    /**
     * 解密出错, 未配置加密密钥或加密密钥格式错误
     */
    ISV_DECRYPTION_ERROR_NOT_VALID_ENCRYPT_KEY(CodeEnum.INVALID, "isv.decryption-error-not-valid-encrypt-key"),
    /**
     * 解密出错，未知异常
     */
    ISV_DECRYPTION_ERROR_UNKNOWN(CodeEnum.INVALID, "isv.decryption-error-unknown"),
    /**
     * 验签出错, 未配置对应签名算法的公钥或者证书
     */
    ISV_MISSING_SIGNATURE_CONFIG(CodeEnum.INVALID, "isv.missing-signature-config"),
    /**
     * 本接口不支持第三方代理调用
     */
    ISV_NOT_SUPPORT_APP_AUTH(CodeEnum.INVALID, "isv.not-support-app-auth"),
    /**
     * 可疑的攻击请求
     */
    ISV_SUSPECTED_ATTACK(CodeEnum.INVALID, "isv.suspected-attack"),
    /**
     * 无效的content-type
     */
    ISV_INVALID_CONTENT_TYPE(CodeEnum.INVALID, "isv.invalid-content-type"),

    /**
     * 业务处理失败
     */
    BIZ_ERROR(CodeEnum.BIZ, "isp.biz-error"),

    /**
     * 请检查配置的账户是否有当前接口权限
     */
    ISV_INSUFFICIENT_ISV_PERMISSIONS(CodeEnum.ISV_PERM, "isv.insufficient-isv-permissions"),
    /**
     * 代理的商户没有当前接口权限
     */
    ISV_INSUFFICIENT_USER_PERMISSIONS(CodeEnum.ISV_PERM, "isv.insufficient-user-permissions"),
    /**
     * 没有当前接口权限
     */
    ISV_ROUTE_NO_PERMISSIONS(CodeEnum.ISV_PERM, "isv.route-no-permissions"),
    /**
     * 禁止访问
     */
    ISV_ACCESS_FORBIDDEN(CodeEnum.ISV_PERM, "isv.access-forbidden"),
    /** isv 有效期不对 */
    ISV_INVALID_EXPIRATION_TIME(CodeEnum.INVALID, "isv.invalid-expiration-time"),
    /**
     * 禁止IP访问
     */
    ISV_IP_FORBIDDEN(CodeEnum.ISV_PERM, "isv.ip-forbidden");

    private final CodeEnum codeEnum;
    private final String subCode;

    ErrorEnum(CodeEnum codeEnum, String subCode) {
        this.codeEnum = codeEnum;
        this.subCode = subCode;
    }

    public IError getError(Locale locale, Object... params) {
        OpenMessage codeMsg = OpenMessageFactory.getMessage(codeEnum, locale, params);
        OpenMessage subCodeMsg = OpenMessageFactory.getMessage(this, locale, params);
        return new ErrorImpl(
                codeMsg.getCode(),
                codeMsg.getMsg(),
                subCodeMsg.getCode(),
                subCodeMsg.getMsg(),
                subCodeMsg.getSolution()
        );
    }

    public String getCode() {
        return codeEnum.getConfigKey();
    }

    @Override
    public String getConfigKey() {
        return subCode;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 错误码
     * @param subCode 错误码
     * @return 枚举
     */
    public static ErrorEnum getByCode(String code, String subCode) {
        for (ErrorEnum value : ErrorEnum.values()) {
            if (value.getCode().equals(code) && value.getSubCode().equals(subCode)) {
                return value;
            }
        }
        return null;
    }

}
