package top.mddata.base.mvcflex.advice;

import com.mybatisflex.core.mask.MaskManager;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import top.mddata.base.apiperm.engine.ApiPermChecker;
import top.mddata.base.apiperm.spi.ApiPermProvider;
import top.mddata.base.fieldperm.engine.BuiltinMasker;
import top.mddata.base.fieldperm.engine.FieldPermEngine;
import top.mddata.base.fieldperm.model.FieldRule;
import top.mddata.base.fieldperm.spi.FieldPermProvider;
import top.mddata.base.util.ContextUtil;

import java.util.Map;

/**
 * 字段权限响应处理。
 *
 * <p>JSON 写出前：URI 归一化 → 反查字段权限菜单 → 取当前用户受限字段（缓存）
 * → {@link FieldPermEngine} 遍历响应对象树，命中字段按规则隐藏/脱敏。</p>
 *
 * <p>不适用通道：文件下载/导出（body 为 byte[]/Resource 直接放行，导出由导出基类
 * 手动调引擎）；未登录或无 HTTP 上下文的内部调用直接放行。</p>
 *
 * @author henhen6
 * @since 2026-10-02
 */
@ControllerAdvice
public class FieldPermAdvice implements ResponseBodyAdvice<Object> {

    private final FieldPermProvider fieldPermProvider;
    private final ApiPermProvider apiPermProvider;
    private final FieldPermEngine engine;

    public FieldPermAdvice(FieldPermProvider fieldPermProvider, ApiPermProvider apiPermProvider) {
        this.fieldPermProvider = fieldPermProvider;
        this.apiPermProvider = apiPermProvider;
        // 脱敏优先走 MaskManager（含业务侧自定义注册），未注册回退内置 9 规则
        BuiltinMasker fallback = new BuiltinMasker();
        this.engine = new FieldPermEngine((rule, value) ->
                MaskManager.getProcessorMap().containsKey(rule)
                        ? MaskManager.mask(rule, value) : fallback.mask(rule, value));
    }

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null || body instanceof byte[] || body instanceof Resource) {
            return body;
        }
        Map<String, FieldRule> rules = resolveRules(fieldPermProvider, apiPermProvider,
                request.getURI().getPath(), request.getMethod().name(), currentUserIdOrNull());
        if (rules != null) {
            engine.apply(body, rules);
        }
        return body;
    }

    /**
     * 决策链（纯函数，便于单测）：开关 → 登录态 → URI 反查菜单 → 用户受限规则。
     * 任一环节不命中返回 null（放行）。
     */
    static Map<String, FieldRule> resolveRules(FieldPermProvider fieldPermProvider,
                                               ApiPermProvider apiPermProvider,
                                               String rawPath, String method, Long userId) {
        if (!fieldPermProvider.isAuthEnabled() || userId == null) {
            return null;
        }
        String path = ApiPermChecker.normalizePath(rawPath,
                apiPermProvider.getGatewayPrefix(), apiPermProvider.getServicePrefixes());
        Long menuId = fieldPermProvider.findMenuId(path, method);
        if (menuId == null) {
            return null;
        }
        return fieldPermProvider.findUserPerm(userId).rulesOf(menuId);
    }

    /** 非 HTTP 入口（定时任务/worker）无请求上下文时安全获取用户ID */
    static Long currentUserIdOrNull() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes)) {
            return null;
        }
        return ContextUtil.getUserId();
    }
}
