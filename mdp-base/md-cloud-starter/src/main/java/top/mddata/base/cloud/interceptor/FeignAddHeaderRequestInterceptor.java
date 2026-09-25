package top.mddata.base.cloud.interceptor;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.URLUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.mddata.base.constant.ContextConstants;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.util.StrPool;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * feign client 拦截器， 实现将 feign 调用方的 请求头封装到 被调用方的请求头
 *
 * @author henhen
 * @date 2019-07-25 11:23
 */
@Slf4j
public class FeignAddHeaderRequestInterceptor implements RequestInterceptor {
    public static final List<String> HEADER_NAME_LIST = Arrays.asList(
            ContextConstants.APP_ID,
            ContextConstants.TOKEN,
            ContextConstants.USER_ID,
            ContextConstants.COMPANY_NATURE, ContextConstants.COMPANY_ID,
            ContextConstants.TOP_COMPANY_ID,
            ContextConstants.TOP_COMPANY_NATURE, ContextConstants.DEPT_ID,
            ContextConstants.PATH, ContextConstants.LOCALE,
            ContextConstants.GRAY_VERSION,
            ContextConstants.PROCEED,
            ContextConstants.FEIGN,
            ContextConstants.TRACE, "X-Real-IP", "x-forwarded-for"
    );

    public FeignAddHeaderRequestInterceptor() {
        super();
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(ContextConstants.FEIGN, StrPool.TRUE);
        log.info("url = {}, thread id ={}, name={}", template.url(), Thread.currentThread().getId(), Thread.currentThread().getName());
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            Map<String, String> localMap = ContextUtil.getLocalMap();
            localMap.forEach((key, value) -> template.header(key, URLUtil.encode(value)));
            return;
        }

        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        if (request == null) {
            log.warn("path={}, 在FeignClient API接口未配置FeignConfiguration类， 故而无法在远程调用时获取请求头中的参数!", template.path());
            return;
        }
        // 传递请求头
        HEADER_NAME_LIST.forEach(headerName -> {
            String header = request.getHeader(headerName);
            template.header(headerName, ObjectUtil.isEmpty(header) ? URLUtil.encode(ContextUtil.get(headerName)) : header);
        });
    }
}
