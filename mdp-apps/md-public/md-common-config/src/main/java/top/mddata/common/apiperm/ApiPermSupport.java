package top.mddata.common.apiperm;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.mddata.base.apiperm.engine.ApiPermChecker;
import top.mddata.base.exception.ArgumentException;
import top.mddata.common.properties.IgnoreProperties;

/**
 * 单体版接口权限判定入口（TokenContextFilter 调用）。
 *
 * <p>注意：在 SaInterceptor.auth 阶段执行时 ContextUtil 尚未填充，
 * 登录态与 userId 直接从 StpUtil 获取，不依赖 ContextUtil。</p>
 */
@Component
@RequiredArgsConstructor
public class ApiPermSupport {
    private final ApiPermProviderImpl apiPermProvider;
    private final IgnoreProperties ignoreProperties;

    public void check(String uri, String method) {
        // 未登录（ignoreUser 接口）不判定接口权限
        if (!StpUtil.isLogin()) {
            return;
        }
        // anyone/anyUser/baseUri 名单：登录即可访问，免接口权限判定
        if (ignoreProperties.isIgnoreUriAuth(method, uri)) {
            return;
        }
        boolean pass = ApiPermChecker.check(apiPermProvider, uri, method, StpUtil.getLoginIdAsLong());
        if (!pass) {
            throw new ArgumentException("无权限访问该接口：{}", uri);
        }
    }
}
