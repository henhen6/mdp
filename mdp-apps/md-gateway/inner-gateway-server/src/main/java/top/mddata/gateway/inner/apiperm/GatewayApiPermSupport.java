package top.mddata.gateway.inner.apiperm;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mddata.base.apiperm.engine.ApiPermChecker;
import top.mddata.base.apiperm.spi.ApiPermProvider;
import top.mddata.common.properties.IgnoreProperties;

/**
 * 网关版接口权限判定入口。
 *
 * <p>因 AuthenticationSaInterceptor（order=-500）先于 TokenContextFilter（order=-1000）
 * 执行，ContextUtil 尚未写入 userId，故直接从 StpUtil.getLoginId() 取登录用户。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayApiPermSupport {
    private final ApiPermProvider apiPermProvider;
    private final IgnoreProperties ignoreProperties;

    public boolean pass(String path, String method) {
        Long userId = getUserId();
        if (userId == null) {
            return true;
        }
        // anyone/anyUser/baseUri 名单：登录即可访问，免接口权限判定
        if (ignoreProperties.isIgnoreUriAuth(method, path)) {
            return true;
        }
        boolean pass = ApiPermChecker.check(apiPermProvider, path, method, userId);
        if (!pass) {
            log.warn("接口权限拒绝：userId={} {} {}", userId, method, path);
        }
        return pass;
    }

    private Long getUserId() {
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            if (loginId == null) {
                return null;
            }
            if (loginId instanceof Number) {
                return ((Number) loginId).longValue();
            }
            return Long.valueOf(loginId.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
