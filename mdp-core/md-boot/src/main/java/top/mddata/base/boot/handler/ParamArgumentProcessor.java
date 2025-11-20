package top.mddata.base.boot.handler;

import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.RequestParamMethodArgumentResolver;
import top.mddata.base.annotation.web.ParamName;

/**
 * 简单参数自定义 处理器
 * 在Controller直接使用 @ParamName() 注解时使用
 *
 * @author henhen6
 * @since 2025/9/3 11:34
 */
public class ParamArgumentProcessor extends RequestParamMethodArgumentResolver {
    public ParamArgumentProcessor() {
        super(true);
    }

    // 当参数上拥有 ParanName 注解，且参数类型为基础类型时，匹配
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ParamName.class) && BeanUtils.isSimpleProperty(parameter.getParameterType());
    }


    // 根据自定义的映射name，从传参中获取对应的value
    @Override
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
        ParamName paramName = parameter.getParameterAnnotation(ParamName.class);
        if (paramName != null) {
            String ans = request.getParameter(paramName.value());
            if (ans == null) {
                return request.getParameter(name);
            }
            return ans;
        }
        return super.resolveName(name, parameter, request);
    }
}
