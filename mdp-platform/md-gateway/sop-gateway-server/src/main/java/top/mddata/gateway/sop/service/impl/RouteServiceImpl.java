package top.mddata.gateway.sop.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.gitee.sop.support.constant.SopConstants;
import com.gitee.sop.support.context.DefaultOpenContext;
import com.gitee.sop.support.context.OpenContext;
import com.gitee.sop.support.context.WebContext;
import com.gitee.sop.support.dto.CommonFileData;
import com.gitee.sop.support.dto.FileData;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.ClassUtils;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcContextAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import top.mddata.gateway.sop.common.RouteContext;
import top.mddata.gateway.sop.exception.ApiException;
import top.mddata.gateway.sop.exception.ExceptionExecutor;
import top.mddata.gateway.sop.interceptor.RouteInterceptor;
import top.mddata.gateway.sop.message.ErrorEnum;
import top.mddata.gateway.sop.pojo.dto.ApiDto;
import top.mddata.gateway.sop.pojo.dto.AppDto;
import top.mddata.gateway.sop.pojo.dto.ParamInfoDto;
import top.mddata.gateway.sop.request.ApiRequest;
import top.mddata.gateway.sop.request.ApiRequestContext;
import top.mddata.gateway.sop.request.UploadContext;
import top.mddata.gateway.sop.response.ApiResponse;
import top.mddata.gateway.sop.response.Response;
import top.mddata.gateway.sop.service.GenericServiceInvoker;
import top.mddata.gateway.sop.service.ResultWrapper;
import top.mddata.gateway.sop.service.RouteService;
import top.mddata.gateway.sop.service.Serde;
import top.mddata.gateway.sop.service.validate.ValidateReturn;
import top.mddata.gateway.sop.service.validate.Validator;
import top.mddata.gateway.sop.util.ClassUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * 接口路由
 *
 * @author 六如
 */
@Slf4j
@Service("routeService")
public class RouteServiceImpl implements RouteService {


    @Resource
    protected Validator validator;

    @Resource
    protected GenericServiceInvoker genericServiceInvoker;

    @Resource
    protected ExceptionExecutor exceptionExecutor;

    @Autowired(required = false)
    private List<RouteInterceptor> routeInterceptors;

    @Resource
    private ResultWrapper resultWrapper;

    @Resource
    private Serde serde;

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private Environment environment;

    @Override
    public Response route(ApiRequestContext apiRequestContext) {
        ApiRequest apiRequest = apiRequestContext.getApiRequest();
        log.info("收到客户端请求, ip={}, apiRequest={}", apiRequestContext.getIp(), apiRequest);
        try {
            // 接口校验
            ValidateReturn validateReturn = validate(apiRequestContext);
            RouteContext routeContext = new RouteContext(
                    apiRequestContext,
                    validateReturn.getApiDto(),
                    validateReturn.getAppDto()
            );
            // 执行拦截器前置动作
            this.doPreRoute(routeContext);
            // 执行路由，返回微服务结果
            Object result = doRoute(routeContext);
            // 执行拦截器后置动作
            result = this.doAfterRoute(routeContext, result);
            // 结果处理
            return resultWrapper.wrap(Optional.of(routeContext), result);
        } catch (Exception e) {
            log.error("接口请求报错, ip={}, apiRequest={}", apiRequestContext.getIp(), apiRequest, e);
            ApiResponse apiResponse = exceptionExecutor.executeException(apiRequestContext, e);
            return resultWrapper.wrap(Optional.empty(), apiResponse);
        }
    }

    protected ValidateReturn validate(ApiRequestContext apiRequestContext) {
        return validator.validate(apiRequestContext);
    }

    protected Object doRoute(RouteContext routeContext) {
        ApiRequestContext apiRequestContext = routeContext.getApiRequestContext();
        ApiDto apiInfo = routeContext.getApi();
        AppDto isv = routeContext.getApp();
        RpcContextAttachment clientAttachment = RpcContext.getClientAttachment();
        String paramInfo = apiInfo.getParamInfo();
        List<ParamInfoDto> paramInfoList = JSON.parseArray(paramInfo, ParamInfoDto.class);
        OpenContext openRequest = buildOpenContext(apiRequestContext, isv);
        clientAttachment.setObjectAttachment(SopConstants.OPEN_CONTEXT, openRequest);
        if (apiRequestContext.getIsRest()) {
            clientAttachment.setObjectAttachment(SopConstants.WEB_CONTEXT, apiRequestContext.getWebContext());
        }
        return genericServiceInvoker.invoke(
                apiInfo.getInterfaceClassName(),
                apiInfo.getMethodName(),
                buildParamType(paramInfoList),
                buildInvokeParam(apiRequestContext, paramInfoList, openRequest)
        );
    }

    protected void doPreRoute(RouteContext routeContext) {
        for (RouteInterceptor routeInterceptor : routeInterceptors) {
            if (routeInterceptor.match(routeContext)) {
                routeInterceptor.preRoute(routeContext);
            }
        }
    }

    protected Object doAfterRoute(RouteContext routeContext, Object result) {
        Object ret = result;
        for (RouteInterceptor routeInterceptor : routeInterceptors) {
            if (routeInterceptor.match(routeContext)) {
                ret = routeInterceptor.afterRoute(routeContext, ret);
            }
        }
        return ret;
    }

    protected String[] buildParamType(List<ParamInfoDto> paramInfoList) {
        if (ObjectUtils.isEmpty(paramInfoList)) {
            return new String[0];
        }
        return paramInfoList.stream()
                .map(ParamInfoDto::getType)
                .toArray(String[]::new);
    }

    protected Object[] buildInvokeParam(
            ApiRequestContext apiRequestContext,
            List<ParamInfoDto> paramInfoList,
            OpenContext openRequest) {
        if (ObjectUtils.isEmpty(paramInfoList)) {
            return new Object[0];
        }
        JSONObject jsonObject = getParamObject(apiRequestContext);
        List<Object> params = new ArrayList<>();
        for (ParamInfoDto paramInfoDTO : paramInfoList) {
            String type = paramInfoDTO.getType();
            String actualType = paramInfoDTO.getActualType();

            // 上下文
            if (Objects.equals(type, OpenContext.class.getName())) {
                params.add(openRequest);
            } else if (Objects.equals(type, WebContext.class.getName())) {
                params.add(apiRequestContext.getWebContext());
            } else if (Objects.equals(type, FileData.class.getName()) || Objects.equals(actualType, FileData.class.getName())) {
                // 处理文件上传
                Optional<Object> fileParam = buildFileParam(apiRequestContext, paramInfoDTO);
                if (!fileParam.isPresent()) {
                    continue;
                }
                Object param = fileParam.get();
                params.add(param);
            } else {
                if (ClassUtil.isPrimitive(type)) {
                    String paramName = paramInfoDTO.getName();
                    Object value = null;
                    try {
                        if (jsonObject != null) {
                            value = jsonObject.getObject(paramName, ClassUtils.forName(type));
                            jsonObject.remove(paramName);
                        }
                        params.add(value);
                    } catch (ClassNotFoundException e) {
                        log.error("找不到参数class, paramInfoDTO={}, apiRequest={}", paramInfoDTO, apiRequestContext.getApiRequest(), e);
                        throw new RuntimeException("找不到class:" + type, e);
                    }
                } else {
                    params.add(jsonObject);
                }
            }
        }
        return params.toArray(new Object[0]);
    }

    protected JSONObject getParamObject(ApiRequestContext apiRequestContext) {
        ApiRequest apiRequest = apiRequestContext.getApiRequest();
        String bizContent = apiRequest.getBizContent();
        return serde.parseObject(bizContent);
    }

    protected OpenContext buildOpenContext(ApiRequestContext apiRequestContext, AppDto isv) {
        ApiRequest apiRequest = apiRequestContext.getApiRequest();
        DefaultOpenContext defaultOpenRequest = new DefaultOpenContext();
        defaultOpenRequest.setAppKey(apiRequest.getAppKey());
        defaultOpenRequest.setApiName(apiRequest.getMethod());
        defaultOpenRequest.setVersion(apiRequest.getVersion());
        defaultOpenRequest.setAppAuthToken(apiRequest.getAppAuthToken());
        defaultOpenRequest.setClientIp(apiRequestContext.getIp());
        defaultOpenRequest.setNotifyUrl(apiRequest.getNotifyUrl());
        defaultOpenRequest.setTraceId(apiRequestContext.getTraceId());
        defaultOpenRequest.setLocale(apiRequestContext.getLocale());
        defaultOpenRequest.setCharset(apiRequest.getCharset());
//        defaultOpenRequest.setMerchantCode(routeContext.getIsv().getMerchantCode());

        defaultOpenRequest.initContext();
        return defaultOpenRequest;
    }

    protected Optional<Object> buildFileParam(ApiRequestContext apiRequestContext, ParamInfoDto paramInfoDto) {
        UploadContext uploadContext = apiRequestContext.getUploadContext();
        if (uploadContext == null) {
            return Optional.empty();
        }
        List<MultipartFile> files = uploadContext.getFile(paramInfoDto.getName());
        List<FileData> fileDataList = new ArrayList<>(files.size());
        for (MultipartFile multipartFile : files) {
            CommonFileData fileData = new CommonFileData();
            fileData.setName(multipartFile.getName());
            fileData.setOriginalFilename(multipartFile.getOriginalFilename());
            fileData.setContentType(multipartFile.getContentType());
            try {
                fileData.setData(multipartFile.getBytes());
            } catch (IOException e) {
                log.error("读取文件内容失败, apiRequestContext={}", apiRequestContext, e);
                throw new ApiException(ErrorEnum.ISP_SERVICE_UNKNOWN_ERROR, apiRequestContext.getLocale());
            }
            fileDataList.add(fileData);
        }
        String type = paramInfoDto.getType();
        Object fileParam = isCollectionClass(type) ? fileDataList : fileDataList.get(0);
        return Optional.of(fileParam);
    }

    private boolean isCollectionClass(String className) {
        Class<?> clazz = parseClass(className);
        return clazz != null && Collection.class.isAssignableFrom(clazz);
    }

    private Class<?> parseClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @PostConstruct
    public void init() {
        if (routeInterceptors == null) {
            routeInterceptors = new ArrayList<>();
        }
        routeInterceptors.sort(Comparator.comparing(RouteInterceptor::getOrder));

        for (RouteInterceptor routeInterceptor : routeInterceptors) {
            routeInterceptor.init(applicationContext, environment);
        }
    }

}
