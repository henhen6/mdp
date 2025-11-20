package top.mddata.common.interceptor;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.method.HandlerMethod;
import top.mddata.base.constant.ContextConstants;
import top.mddata.base.utils.ContextUtil;
import top.mddata.common.properties.IgnoreProperties;

import static top.mddata.base.constant.ContextConstants.APP_ID_HEADER;
import static top.mddata.base.constant.ContextConstants.APP_ID_KEY;
import static top.mddata.base.constant.ContextConstants.JWT_KEY_COMPANY_ID;
import static top.mddata.base.constant.ContextConstants.JWT_KEY_DEPT_ID;
import static top.mddata.base.constant.ContextConstants.JWT_KEY_TOP_COMPANY_ID;
import static top.mddata.base.constant.ContextConstants.JWT_KEY_TOP_COMPANY_IS_ADMIN;

/**
 * 单体架构专用， 请求头信息解析器 + uri 接口鉴权拦截器
 *
 *
 * <p>
 * 本拦截器与 {@link HeaderThreadLocalInterceptor} 互斥，只能使用一个
 *
 * @author henhen6
 * @date 2025年07月08日22:20:09
 * @since 1.0.0
 */
@Slf4j
public class TokenContextFilter extends SaInterceptor {
    private final IgnoreProperties ignoreProperties;

    public TokenContextFilter(IgnoreProperties ignoreProperties) {
        this.ignoreProperties = ignoreProperties;

        // TODO 接口拦截器 待实现
        this.auth = handler -> {


        };
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            log.debug("not exec!!! url={}", request.getRequestURL());
            return true;
        }
        ContextUtil.setPath(getHeader(ContextConstants.PATH_HEADER, request));
        String traceId = IdUtil.fastSimpleUUID();
        MDC.put(ContextConstants.TRACE_ID_HEADER, traceId);
        try {

            // 2, 获取 应用id
            parseApplication(request);

            boolean flag = super.preHandle(request, response, handler);

            // 3， 解析token
            parseToken(request);
            return flag;
        } catch (Exception e) {
            log.error("request={}", request.getRequestURL(), e);
            throw e;
        }
    }

    private void parseToken(HttpServletRequest request) {
        // 忽略 token 认证的接口
        if (ignoreProperties.isIgnoreUser(request.getMethod(), request.getRequestURI())) {
            log.debug("access filter not execute");
            return;
        }
        SaSession accountSession = StpUtil.getSession();

        if (accountSession != null) {
            Long userId = (Long) accountSession.getLoginId();
            Object topCompanyId = accountSession.get(JWT_KEY_TOP_COMPANY_ID);
            Object companyId = accountSession.get(JWT_KEY_COMPANY_ID);
            Object deptId = accountSession.get(JWT_KEY_DEPT_ID);
            Object topCompanyIsAdmin = accountSession.get(JWT_KEY_TOP_COMPANY_IS_ADMIN);

            ContextUtil.setUserId(userId);
            ContextUtil.setCurrentCompanyId(companyId);
            ContextUtil.setCurrentTopCompanyId(topCompanyId);
            ContextUtil.setCurrentDeptId(deptId);
            ContextUtil.setTopCompanyIsAdmin(topCompanyIsAdmin != null && Convert.toBool(topCompanyIsAdmin));
            MDC.put(ContextConstants.USER_ID_HEADER, String.valueOf(userId));
        }

    }

    private void parseApplication(HttpServletRequest request) {
        String appIdStr = getHeader(APP_ID_KEY, request);
        if (StrUtil.isNotEmpty(appIdStr)) {
            ContextUtil.setAppId(appIdStr);
            MDC.put(APP_ID_HEADER, appIdStr);
        }
    }


    private String getHeader(String name, HttpServletRequest request) {
        String value = request.getHeader(name);
        if (StrUtil.isEmpty(value)) {
            value = request.getParameter(name);
        }
        if (StrUtil.isEmpty(value)) {
            return null;
        }
        return URLUtil.decode(value);
    }


    /**
     * 忽略应用级token
     *
     * @return
     */
    protected boolean isIgnoreToken(HttpServletRequest request) {
        return ignoreProperties.isIgnoreUser(request.getMethod(), request.getRequestURI());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ContextUtil.remove();
    }
}
