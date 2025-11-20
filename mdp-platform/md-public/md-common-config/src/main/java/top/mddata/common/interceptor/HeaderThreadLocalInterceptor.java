package top.mddata.common.interceptor;

import cn.hutool.core.convert.Convert;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import top.mddata.base.constant.ContextConstants;
import top.mddata.base.utils.ContextUtil;
import top.mddata.base.utils.WebUtils;

import java.util.Map;

/**
 * 在微服务架构专用： 使用本拦截器的前置条件：
 * 网关服务把Token解析后，将解析出的用户信息封装到请求头
 *
 * 本拦截器作用：
 * 将请求头中的数据，封装到 ContextUtil
 *
 * 本拦截器与 {@link TokenContextFilter} 互斥，只能使用一个
 *
 * @author henhen6
 * @date 2020/10/31 9:49 下午
 */
@Slf4j
@RequiredArgsConstructor
public class HeaderThreadLocalInterceptor implements AsyncHandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

//        其他信息
        ContextUtil.setPath(WebUtils.getHeader(request, ContextConstants.PATH_HEADER));
        ContextUtil.setAppId(WebUtils.getHeader(request, ContextConstants.APP_ID_HEADER));
        ContextUtil.setLogTraceId(WebUtils.getHeader(request, ContextConstants.TRACE_ID_HEADER));
        ContextUtil.setGrayVersion(WebUtils.getHeader(request, ContextConstants.GRAY_VERSION));

//        用户信息
        String userId = WebUtils.getHeader(request, ContextConstants.JWT_KEY_USER_ID);
        MDC.put(ContextConstants.USER_ID_HEADER, userId);
        ContextUtil.setUserId(userId);
        ContextUtil.setCurrentTopCompanyId(WebUtils.getHeader(request, ContextConstants.CURRENT_TOP_COMPANY_ID_HEADER));
        ContextUtil.setCurrentCompanyId(WebUtils.getHeader(request, ContextConstants.CURRENT_COMPANY_ID_HEADER));
        ContextUtil.setCurrentDeptId(WebUtils.getHeader(request, ContextConstants.CURRENT_DEPT_ID_HEADER));
        ContextUtil.setTopCompanyIsAdmin(Convert.toBool(WebUtils.getHeader(request, ContextConstants.CURRENT_TOP_COMPANY_IS_ADMIN_HEADER)));

        Map<String, String> localMap = ContextUtil.getLocalMap();
        localMap.forEach(MDC::put);
        log.info("HeaderThreadLocalInterceptor url={}, method={}", request.getRequestURI(), request.getMethod());
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ContextUtil.remove();
        MDC.clear();
    }
}
