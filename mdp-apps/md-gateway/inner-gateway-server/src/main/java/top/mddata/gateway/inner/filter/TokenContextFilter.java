package top.mddata.gateway.inner.filter;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import top.mddata.base.base.R;
import top.mddata.base.constant.ContextConstants;
import top.mddata.base.exception.BizException;
import top.mddata.base.exception.UnauthorizedException;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.util.StrPool;
import top.mddata.common.properties.IgnoreProperties;

import static top.mddata.base.constant.ContextConstants.APP_ID;
import static top.mddata.base.constant.ContextConstants.COMPANY_ID;
import static top.mddata.base.constant.ContextConstants.COMPANY_NATURE;
import static top.mddata.base.constant.ContextConstants.DEPT_ID;
import static top.mddata.base.constant.ContextConstants.TOP_COMPANY_ID;
import static top.mddata.base.constant.ContextConstants.TOP_COMPANY_NATURE;


/**
 * 过滤器
 *
 * @author henhen
 * @date 2019/07/31
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TokenContextFilter implements WebFilter, Ordered {
    protected final SaTokenConfig saTokenConfig;
    private final IgnoreProperties ignoreProperties;
    @Value("${spring.profiles.active:dev}")
    protected String profiles;

    protected boolean isDev(String token) {
        return !StrPool.PROD.equalsIgnoreCase(profiles) && (StrPool.TEST_TOKEN.equalsIgnoreCase(token) || StrPool.TEST.equalsIgnoreCase(token));
    }

    @Override
    public int getOrder() {
        return OrderedConstant.TOKEN;
    }


    /**
     * 忽略 用户token
     */
    protected boolean isIgnoreToken(ServerHttpRequest request) {
        return ignoreProperties.isIgnoreUser(request.getMethod().name(), request.getPath().toString());
    }

    protected String getHeader(String headerName, ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String token = StrUtil.EMPTY;
        if (headers == null || headers.isEmpty()) {
            return token;
        }

        token = headers.getFirst(headerName);

        if (StrUtil.isNotBlank(token)) {
            return token;
        }

        return request.getQueryParams().getFirst(headerName);
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest.Builder mutate = request.mutate();

        ContextUtil.setGrayVersion(getHeader(ContextConstants.GRAY_VERSION, request));
        ContextUtil.setLocale(getHeader(ContextConstants.LOCALE, request));

        try {
            // 2,解码 Authorization
//            parseClient(request, mutate);

            // 3, 获取 应用id
            parseApplication(request, mutate);


            Mono<Void> token = parseToken(exchange, chain, mutate);
            if (token != null) {
                return token;
            }

        } catch (UnauthorizedException e) {
            log.error(request.getPath().toString(), e);
            return errorResponse(response, e.getMessage(), e.getCode(), HttpStatus.UNAUTHORIZED);
        } catch (BizException e) {
            log.error(request.getPath().toString(), e);
            return errorResponse(response, e.getMessage(), e.getCode(), HttpStatus.BAD_REQUEST);
        } catch (SaTokenException e) {
            log.error(request.getPath().toString(), e);
            return errorResponse(response, e.getMessage(), e.getCode(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error(request.getPath().toString(), e);
            return errorResponse(response, "验证token出错", R.FAIL_CODE, HttpStatus.BAD_REQUEST);
        }

        ServerHttpRequest build = mutate.build();
        return chain.filter(exchange.mutate().request(build).build());
    }

    private Mono<Void> parseToken(ServerWebExchange exchange, WebFilterChain chain, ServerHttpRequest.Builder mutate) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 判断接口是否需要忽略token验证
        if (isIgnoreToken(request)) {
            log.debug("当前接口：{}, 不解析用户token", request.getPath());
            return chain.filter(exchange);
        }

        SaSession tokenSession = StpUtil.getTokenSessionByToken(getHeader(saTokenConfig.getTokenName(), request));
        log.info("{}", tokenSession);

        if (tokenSession != null) {
            Long userId = (Long) tokenSession.getLoginId();
            Object topCompanyId = tokenSession.get(TOP_COMPANY_ID);
            Object companyId = tokenSession.get(COMPANY_ID);
            Object topCompanyNature = tokenSession.get(TOP_COMPANY_NATURE);
            Object companyNature = tokenSession.get(COMPANY_NATURE);
            Object deptId = tokenSession.get(DEPT_ID);


            if (userId != null) {
                mutate.header(ContextConstants.USER_ID, String.valueOf(userId));
            }
            if (topCompanyId != null) {
                mutate.header(ContextConstants.TOP_COMPANY_ID, String.valueOf(topCompanyId));
            }
            if (topCompanyNature != null) {
                mutate.header(ContextConstants.TOP_COMPANY_NATURE, String.valueOf(topCompanyNature));
            }
            if (companyId != null) {
                mutate.header(ContextConstants.COMPANY_ID, String.valueOf(companyId));
            }
            if (companyNature != null) {
                mutate.header(ContextConstants.COMPANY_NATURE, String.valueOf(companyNature));
            }
            if (deptId != null) {
                mutate.header(ContextConstants.DEPT_ID, String.valueOf(deptId));
            }

        }

        return null;
    }

//    private void parseClient(ServerHttpRequest request, ServerHttpRequest.Builder mutate) {
//        try {
//            String pattern = "/actuator/**";
//            if (!SaPathPatternParserUtil.match(pattern, request.getPath().toString())) {
//                String base64Authorization = getHeader(CLIENT_KEY, request);
//                if (StrUtil.isNotEmpty(base64Authorization)) {
//                    String[] client = Base64Util.getClient(base64Authorization);
//                    ContextUtil.setClientId(client[0]);
//                    addHeader(mutate, CLIENT_ID_HEADER, ContextUtil.getClientId());
//                }
//            }
//        } catch (Exception ignore) {
//        }
//    }

    private void parseApplication(ServerHttpRequest request, ServerHttpRequest.Builder mutate) {
        String applicationIdStr = getHeader(APP_ID, request);
        if (StrUtil.isNotEmpty(applicationIdStr)) {
            ContextUtil.setAppId(applicationIdStr);
            addHeader(mutate, APP_ID, ContextUtil.getAppId());
            MDC.put(APP_ID, applicationIdStr);
        }
    }

    private void addHeader(ServerHttpRequest.Builder mutate, String name, Object value) {
        if (value == null) {
            return;
        }
        String valueStr = value.toString();
        String valueEncode = URLUtil.encode(valueStr);
        mutate.header(name, valueEncode);
    }

    protected Mono<Void> errorResponse(ServerHttpResponse response, String errMsg, int errCode, HttpStatus httpStatus) {
        R tokenError = R.fail(errCode, errMsg);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.setStatusCode(HttpStatus.OK);
        DataBuffer dataBuffer = response.bufferFactory().wrap(JSON.toJSONString(tokenError).getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }

}
