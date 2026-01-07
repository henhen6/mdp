package top.mddata.gateway.sop.service.validate.impl;

import cn.hutool.core.convert.Convert;
import com.gitee.sop.support.enums.ApiModeEnum;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import top.mddata.common.enumeration.BooleanEnum;
import top.mddata.common.enumeration.StateEnum;
import top.mddata.gateway.sop.config.GateApiConfig;
import top.mddata.gateway.sop.exception.ApiException;
import top.mddata.gateway.sop.manager.ApiManager;
import top.mddata.gateway.sop.manager.AppManager;
import top.mddata.gateway.sop.manager.IpBlacklistManager;
import top.mddata.gateway.sop.message.ErrorEnum;
import top.mddata.gateway.sop.common.ApiDto;
import top.mddata.gateway.sop.common.AppDto;
import top.mddata.gateway.sop.request.ApiRequest;
import top.mddata.gateway.sop.request.ApiRequestContext;
import top.mddata.gateway.sop.request.UploadContext;
import top.mddata.gateway.sop.response.FormatEnum;
import top.mddata.gateway.sop.service.validate.Signer;
import top.mddata.gateway.sop.service.validate.ValidateReturn;
import top.mddata.gateway.sop.service.validate.Validator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 负责校验,校验工作都在这里
 *
 * @author 六如
 */
@Slf4j
@Service
public class ApiValidator implements Validator {

    private static final long MILLISECOND_OF_ONE_SECOND = 1000;

    /**
     * 单个文件内容最大值
     */
    @Value("${mdp.upload.one-file-max-size}")
    private DataSize oneFileMaxSize;

    /**
     * 总文件最大值
     */
    @Value("${mdp.upload.total-file-max-size}")
    private DataSize maxFileSize;

    @Resource
    private Signer signer;

    @Resource
    private GateApiConfig apiConfig;

    @Resource
    private IpBlacklistManager ipBlacklistManager;

    @Resource
    private ApiManager apiManager;

    @Resource
    private AppManager appManager;

    private DateTimeFormatter dateTimeFormatter;

    @Override
    public ValidateReturn validate(ApiRequestContext apiRequestContext) {
        ApiRequest apiRequest = apiRequestContext.getApiRequest();
        ApiDto apiInfo = apiManager.getByMethodAndVersion(apiRequest.getMethod(), apiRequest.getVersion());
        // 检查接口信息
        checkApiInfo(apiRequestContext, apiInfo);
        // 校验字段完整性
        checkField(apiRequestContext);
        // 检查isv
        AppDto appDto = checkApp(apiRequestContext);
        // 检查isv接口授权
        checkPermission(apiRequestContext, apiInfo, appDto);
        // 校验签名
        checkSign(apiRequestContext, appDto);
        // 检查是否超时
        checkTimeout(apiRequestContext);
        // 检查格式化
        checkFormat(apiRequestContext);
        // IP能否访问
        checkIP(apiRequestContext);
        // 检查上传文件
        checkUploadFile(apiRequestContext);
        // 检查token
        checkToken(apiRequestContext, apiInfo);
        return new ValidateReturn(apiInfo, appDto);
    }

    @Override
    public ValidateReturn validateRest(ApiRequestContext apiRequestContext) {
        ApiRequest apiRequest = apiRequestContext.getApiRequest();
        ApiDto apiInfo = apiManager.getByMethodAndVersion(apiRequest.getMethod(), apiRequest.getVersion());
        // 检查接口信息
        checkApiInfo(apiRequestContext, apiInfo);

        if (!Objects.equals(apiInfo.getApiMode(), ApiModeEnum.RESTFUL.getCode())) {
            log.error("Open模式接口不允许使用Restful进行访问, apiInfo={}", apiInfo);
            throw new ApiException(ErrorEnum.ISV_INVALID_METHOD, apiRequestContext.getLocale());
        }
        // IP能否访问
        checkIP(apiRequestContext);
        // 检查上传文件
        checkUploadFile(apiRequestContext);
        // 检查token
        checkToken(apiRequestContext, apiInfo);
        return new ValidateReturn(apiInfo, null);
    }

    public void checkApiInfo(ApiRequestContext apiRequestContext, ApiDto apiDto) {
        // 检查路由是否存在
        if (apiDto == null) {
            throw new ApiException(ErrorEnum.ISV_INVALID_METHOD, apiRequestContext.getLocale());
        }
        // 检查路由是否启用
        if (StateEnum.of(apiDto.getState()) != StateEnum.ENABLE) {
            throw new ApiException(ErrorEnum.ISP_API_DISABLED, apiRequestContext.getLocale());
        }
    }

    public void checkPermission(ApiRequestContext apiRequestContext, ApiDto apiDto, AppDto appDto) {
        // 校验是否需要授权访问
        boolean needCheckPermission = Convert.toBool(apiDto.getPermission());
        if (needCheckPermission) {
            boolean hasPermission = appManager.hasPermission(appDto.getId(), apiDto);
            if (!hasPermission) {
                throw new ApiException(ErrorEnum.ISV_ROUTE_NO_PERMISSIONS, apiRequestContext.getLocale());
            }
        }
    }

    public void checkField(ApiRequestContext apiRequestContext) {
        ApiRequest apiRequest = apiRequestContext.getApiRequest();
        if (apiRequest == null) {
            throw new ApiException(ErrorEnum.ISV_INVALID_PARAMETER, apiRequestContext.getLocale());
        }
        Locale locale = apiRequestContext.getLocale();
        if (ObjectUtils.isEmpty(apiRequest.getAppKey())) {
            throw new ApiException(ErrorEnum.ISV_MISSING_APP_KEY, locale);
        }
        if (ObjectUtils.isEmpty(apiRequest.getMethod())) {
            throw new ApiException(ErrorEnum.ISV_MISSING_METHOD, locale);
        }
        if (ObjectUtils.isEmpty(apiRequest.getVersion())) {
            throw new ApiException(ErrorEnum.ISV_MISSING_VERSION, locale);
        }
        if (ObjectUtils.isEmpty(apiRequest.getSignType())) {
            throw new ApiException(ErrorEnum.ISV_MISSING_SIGNATURE_CONFIG, locale);
        }
        if (ObjectUtils.isEmpty(apiRequest.getCharset())) {
            throw new ApiException(ErrorEnum.ISV_INVALID_CHARSET, locale);
        }
        if (ObjectUtils.isEmpty(apiRequest.getSign())) {
            throw new ApiException(ErrorEnum.ISV_MISSING_SIGNATURE, locale);
        }
        if (ObjectUtils.isEmpty(apiRequest.getTimestamp())) {
            throw new ApiException(ErrorEnum.ISV_MISSING_TIMESTAMP, locale);
        }
    }

    /**
     * 是否在IP黑名单中
     *
     * @param apiRequestContext 接口参数
     */
    protected void checkIP(ApiRequestContext apiRequestContext) {
        String ip = apiRequestContext.getIp();
        if (ipBlacklistManager.contains(ip)) {
            throw new ApiException(ErrorEnum.ISV_IP_FORBIDDEN, apiRequestContext.getLocale());
        }
    }


    /**
     * 校验上传文件内容
     *
     * @param apiRequestContext apiRequestContext
     */
    protected void checkUploadFile(ApiRequestContext apiRequestContext) {
        // 校验上传文件内容
        UploadContext uploadContext = apiRequestContext.getUploadContext();
        if (uploadContext != null) {
            List<MultipartFile> allFiles = uploadContext.getAllFile();
            if (ObjectUtils.isEmpty(allFiles)) {
                return;
            }

            for (MultipartFile multipartFile : allFiles) {
                checkSingleFileSize(apiRequestContext, multipartFile);
            }

            long totalSize = allFiles.stream()
                    .map(MultipartFile::getSize)
                    .mapToLong(Long::longValue)
                    .sum();

            if (totalSize > maxFileSize.toBytes()) {
                throw new ApiException(ErrorEnum.ISV_INVALID_FILE_SIZE, apiRequestContext.getLocale(), totalSize, maxFileSize);
            }
        }
    }

    /**
     * 校验单个文件大小
     *
     * @param file 文件
     */
    private void checkSingleFileSize(ApiRequestContext apiRequestContext, MultipartFile file) {
        long fileSize = file.getSize();
        long maxSize = oneFileMaxSize.toBytes();
        if (fileSize > maxSize) {
            throw new ApiException(ErrorEnum.ISV_INVALID_FILE_SIZE, apiRequestContext.getLocale(), fileSize, maxSize);
        }
    }

    protected void checkTimeout(ApiRequestContext apiRequestContext) {
        ApiRequest apiRequest = apiRequestContext.getApiRequest();
        int timeoutSeconds = apiConfig.getTimeoutSeconds();
        // 如果设置为0，表示不校验
        if (timeoutSeconds == 0) {
            return;
        }
        if (timeoutSeconds < 0) {
            throw new IllegalArgumentException("服务端timeoutSeconds设置错误");
        }
        String requestTime = apiRequest.getTimestamp();
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(requestTime, dateTimeFormatter);
            long diffMills = Duration.between(localDateTime, LocalDateTime.now()).toMillis();
            if (diffMills > timeoutSeconds * MILLISECOND_OF_ONE_SECOND) {
                throw new ApiException(ErrorEnum.ISV_INVALID_TIMESTAMP, apiRequestContext.getLocale());
            }
        } catch (DateTimeParseException e) {
            throw new ApiException(ErrorEnum.ISV_INVALID_TIMESTAMP, apiRequestContext.getLocale(), apiRequest.takeNameVersion());
        }
    }

    protected AppDto checkApp(ApiRequestContext apiRequestContext) {
        ApiRequest apiRequest = apiRequestContext.getApiRequest();
        AppDto appDto = appManager.getByAppKey(apiRequest.getAppKey());
        // 没有用户
        if (appDto == null) {
            throw new ApiException(ErrorEnum.ISV_INVALID_APP_KEY, apiRequestContext.getLocale());
        }
        // 禁止访问
        if (appDto.getState() == null || !appDto.getState()) {
            throw new ApiException(ErrorEnum.ISV_ACCESS_FORBIDDEN, apiRequestContext.getLocale());
        }

        // 有效期
        if (appDto.getValidityStart() != null && LocalDateTime.now().isBefore(appDto.getValidityStart())) {
            throw new ApiException(ErrorEnum.ISV_INVALID_EXPIRATION_TIME, apiRequestContext.getLocale());
        }
        if (appDto.getValidityEnd() != null && LocalDateTime.now().isAfter(appDto.getValidityEnd())) {
            throw new ApiException(ErrorEnum.ISV_INVALID_EXPIRATION_TIME, apiRequestContext.getLocale());
        }
        return appDto;
    }

    protected void checkSign(ApiRequestContext apiRequestContext, AppDto appDto) {
        ApiRequest apiRequest = apiRequestContext.getApiRequest();
        String clientSign = apiRequest.getSign();
        if (ObjectUtils.isEmpty(clientSign)) {
            throw new ApiException(ErrorEnum.ISV_MISSING_SIGNATURE, apiRequestContext.getLocale(),
                    apiRequest.takeNameVersion(), apiConfig.getSignName());
        }
        // ISV上传的公钥
        String publicKey = appManager.getAppPublicKey(appDto.getId());
        if (ObjectUtils.isEmpty(publicKey)) {
            throw new ApiException(ErrorEnum.ISV_MISSING_SIGNATURE_CONFIG, apiRequestContext.getLocale(),
                    apiRequest.takeNameVersion());
        }
        // 错误的sign
        if (!signer.checkSign(apiRequestContext, publicKey)) {
            throw new ApiException(ErrorEnum.ISV_INVALID_SIGNATURE, apiRequestContext.getLocale(),
                    apiRequest.takeNameVersion());
        }
    }


    protected void checkFormat(ApiRequestContext apiRequestContext) {
        ApiRequest apiRequest = apiRequestContext.getApiRequest();
        String format = apiRequest.getFormat();
        if (ObjectUtils.isEmpty(format)) {
            return;
        }
        if (FormatEnum.of(format) == FormatEnum.NONE) {
            throw new ApiException(ErrorEnum.ISV_INVALID_FORMAT, apiRequestContext.getLocale(),
                    apiRequest.takeNameVersion(), format);
        }
    }


    /**
     * 校验token
     *
     * @param apiRequestContext 参数
     */
    protected void checkToken(ApiRequestContext apiRequestContext, ApiDto apiInfoDTO) {
        Integer isNeedToken = apiInfoDTO.getNeedToken();
        if (BooleanEnum.of(isNeedToken) == BooleanEnum.FALSE) {
            return;
        }
        // 这里做校验token操作
        String appAuthToken = apiRequestContext.getApiRequest().getAppAuthToken();
        if (StringUtils.isBlank(appAuthToken)) {
            throw new ApiException(ErrorEnum.AOP_INVALID_AUTH_TOKEN, apiRequestContext.getLocale());
        }
    }

    @PostConstruct
    public void init() {
        dateTimeFormatter = DateTimeFormatter.ofPattern(apiConfig.getTimestampPattern());
    }

}
