package com.gitee.sop.support.register;

import com.alibaba.fastjson2.JSON;
import com.gitee.sop.support.annotation.Open;
import com.gitee.sop.support.annotation.OpenGroup;
import com.gitee.sop.support.enums.ApiModeEnum;
import com.gitee.sop.support.service.ApiRegisterService;
import com.gitee.sop.support.service.dto.RegisterDTO;
import com.gitee.sop.support.service.dto.RegisterResult;
import lombok.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 接口注册
 *
 * @author 六如
 */
public class ApiRegister {

    private static final Log LOG = LogFactory.getLog(ApiRegister.class);


    private final ApiRegisterService apiRegisterService;

    public ApiRegister(ApiRegisterService apiRegisterService) {
        this.apiRegisterService = apiRegisterService;
    }

    public void reg(String appName, Collection<Object> objects) throws ApiRegException {
        if (objects == null || objects.isEmpty()) {
            return;
        }

        List<RegisterDTO> registerDTOS = new ArrayList<>();
        for (Object serviceObj : objects) {
            Class<?> objClass = serviceObj.getClass();
            doWithMethod(objClass, (interfaceClass, method, open) -> {
                RegisterDTO registerDTO = this.buildRegisterDTO(appName, interfaceClass, method, open);
                registerDTOS.add(registerDTO);
            });
        }

        reg(registerDTOS);
    }

    public void reg(List<RegisterDTO> registerDTOS) throws ApiRegException {
        if (registerDTOS.isEmpty()) {
            return;
        }
        LOG.info(">>> 开始注册开放接口");
        try {
            RegisterResult result = apiRegisterService.register(registerDTOS);
            if (result.getSuccess()) {
                LOG.info(">>> 开放接口注册成功，接口数量：" + registerDTOS.size());
                for (RegisterDTO registerDTO : registerDTOS) {
                    LOG.info(registerDTO);
                }
            } else {
                LOG.error(">>> 开放接口注册失败，网关处理异常：" + result.getMsg());
                throw new RuntimeException(result.getMsg());
            }
        } catch (Exception e) {
            throw new ApiRegException(e, registerDTOS);
        }
    }

    protected void doWithMethod(Class<?> objClass, RegisterCallback callback) {
        if (objClass.getAnnotation(DubboService.class) == null) {
            return;
        }
        checkClass(objClass);

        // 只找接口中的注解,暂时用不到,先注释
        /*for (Class<?> anInterface : objClass.getInterfaces()) {
            for (Method method : anInterface.getMethods()) {
                Open open = method.getAnnotation(Open.class);
                if (open != null) {
                    callback.callback(anInterface, method, open);
                }
            }
        }*/

        Set<Method> cache = new HashSet<>();
        Set<Class<?>> interfaceSet = new HashSet<>();
        Class<?> current = objClass;
        while (current != null) {
            for (Class<?> iface : current.getInterfaces()) {
                interfaceSet.add(iface);
            }
            current = current.getSuperclass();
        }
        Class<?>[] interfaces = interfaceSet.toArray(new Class<?>[0]);
        for (Class<?> interfaceClass : interfaces) {
            for (Method method : interfaceClass.getMethods()) {
                Open open = method.getAnnotation(Open.class);
                if (open != null) {
                    cache.add(method);
                    callback.callback(interfaceClass, method, open);
                }
            }
        }
        // 实现类方法有注解,接口方法没有注解
        for (Method method : objClass.getMethods()) {
            for (Class<?> interfaceClass : interfaces) {
                try {
                    // 接口中方法签名一致的方法,有返回表示找到,找不到抛出NoSuchMethodException
                    Method parentMethod = interfaceClass.getMethod(method.getName(), method.getParameterTypes());
                    Open open = method.getAnnotation(Open.class);
                    // 存在且未添加过
                    if (open != null && !cache.contains(parentMethod)) {
                        callback.callback(interfaceClass, parentMethod, open);
                    }
                } catch (NoSuchMethodException e) {
                    // ignore
                }
            }
        }

        cache.clear();
    }

    private RegisterDTO buildRegisterDTO(String appName, Class<?> interfaceClass, Method method, Open open) {
        List<ParamInfo> paramInfos = buildParamInfo(method);
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setAppName(appName);
        registerDTO.setApiName(getApiName(interfaceClass, open));
        registerDTO.setApiVersion(open.version());
        registerDTO.setInterfaceClassName(interfaceClass.getName());
        registerDTO.setMethodName(method.getName());
        registerDTO.setParamInfo(JSON.toJSONString(paramInfos));
        registerDTO.setPermission(parseBoolean(open.permission()));
        registerDTO.setNeedToken(parseBoolean(open.needToken()));
        registerDTO.setCommonResponse(parseBoolean(open.hasCommonResponse()));
        OpenGroup openGroup = interfaceClass.getAnnotation(OpenGroup.class);
        ApiModeEnum apiMode = openGroup == null ? ApiModeEnum.OPEN : ApiModeEnum.RESTFUL;
        registerDTO.setApiMode(apiMode.getCode());
        return registerDTO;
    }

    private void regApi(String appName, Class<?> interfaceClass, Method method, Open open) {
        List<ParamInfo> paramInfos = buildParamInfo(method);
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setAppName(appName);
        registerDTO.setApiName(getApiName(interfaceClass, open));
        registerDTO.setApiVersion(open.version());
        registerDTO.setInterfaceClassName(interfaceClass.getName());
        registerDTO.setMethodName(method.getName());
        registerDTO.setParamInfo(JSON.toJSONString(paramInfos));
        registerDTO.setPermission(parseBoolean(open.permission()));
        registerDTO.setNeedToken(parseBoolean(open.needToken()));
        registerDTO.setCommonResponse(parseBoolean(open.hasCommonResponse()));
        OpenGroup openGroup = interfaceClass.getAnnotation(OpenGroup.class);
        ApiModeEnum apiMode = openGroup == null ? ApiModeEnum.OPEN : ApiModeEnum.RESTFUL;
        registerDTO.setApiMode(apiMode.getCode());
        LOG.info("注册开放接口, apiInfo=" + registerDTO);
        RegisterResult result = apiRegisterService.register(registerDTO);
        if (!result.getSuccess()) {
            throw new RuntimeException(result.getMsg());
        }
    }

    protected String getApiName(Class<?> interfaceClass, Open open) {
        String apiName = StringUtils.trimLeadingCharacter(open.value(), '/');
        OpenGroup openGroup = interfaceClass.getAnnotation(OpenGroup.class);
        if (openGroup != null) {
            apiName = openGroup.value() + "/" + apiName;
        }
        return StringUtils.trimLeadingCharacter(apiName, '/');
    }

    private List<ParamInfo> buildParamInfo(Method method) {
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return Collections.emptyList();
        }
        return Stream.of(parameters)
                .map(parameter -> {
                    ParamInfo paramInfo = new ParamInfo();
                    paramInfo.setName(parameter.getName());
                    paramInfo.setType(parameter.getType().getName());
                    Type parameterizedType = parameter.getParameterizedType();
                    // 如果是集合
                    if (parameterizedType instanceof ParameterizedType) {
//                        if (!Collection.class.isAssignableFrom(parameter.getType())) {
//                            throw new IllegalArgumentException("开放接口参数不正确,只支持集合泛型, method={}" + method);
//                        }
                        ParameterizedType genericType = (ParameterizedType) parameterizedType;
                        Type[] actualTypeArguments = genericType.getActualTypeArguments();
                        Type actualTypeArgument = actualTypeArguments[0];
                        paramInfo.setActualType(actualTypeArgument.getTypeName());
                    }
                    return paramInfo;
                })
                .collect(Collectors.toList());
    }


    private int parseBoolean(boolean b) {
        return b ? 1 : 0;
    }

    private void checkClass(Class<?> objClass) {
        Class<?>[] interfaces = objClass.getInterfaces();
        if (interfaces.length == 0) {
            throw new RuntimeException("Dubbo接口必须要实现接口, class=" + objClass.getName());
        }
    }


    @Data
    private static class ParamInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = -404173450677698875L;

        private String name;
        private String type;
        private String actualType;
    }

    @Data
    private static class FileTree {
        private String name;

        private FileTree parent;

        private boolean isLeaf;

        @Override
        public String toString() {
            return "FileTree{" +
                   "name='" + name + '\'' +
                   '}';
        }
    }


}
