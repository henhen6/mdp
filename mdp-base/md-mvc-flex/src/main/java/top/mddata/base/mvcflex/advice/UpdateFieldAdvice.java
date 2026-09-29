package top.mddata.base.mvcflex.advice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.mvcflex.context.UpdateFieldContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 更新端点请求体字段采集。
 *
 * <p>仅拦截标注 {@code @Validated(BaseEntity.Update.class)} 的修改端点（代码生成器约定，全量覆盖），
 * 在反序列化前读取请求体顶层 key 集合，写入 UpdateFieldContext 供
 * SuperServiceImpl.updateBefore 做选择性更新。</p>
 */
@ControllerAdvice
public class UpdateFieldAdvice implements RequestBodyAdvice {

    /** 只读取 JSON 结构的共享实例，ObjectMapper.readTree 线程安全 */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        Validated validated = methodParameter.getParameterAnnotation(Validated.class);
        if (validated == null) {
            return false;
        }
        return Arrays.stream(validated.value()).anyMatch(BaseEntity.Update.class::equals);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType)
            throws IOException {
        byte[] body = StreamUtils.copyToByteArray(inputMessage.getBody());
        UpdateFieldContext.set(extractTopLevelFields(body));
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(body);
            }

            @Override
            public HttpHeaders getHeaders() {
                return inputMessage.getHeaders();
            }
        };
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                  Type targetType,
                                  Class<? extends HttpMessageConverter<?>> converterType) {
        // 空请求体等价于"未提交任何字段"，后续不更新任何列
        UpdateFieldContext.set(Set.of());
        return body;
    }

    /**
     * 提取 JSON 对象的顶层字段名；非对象结构（数组、标量、空）视为无字段。
     */
    static Set<String> extractTopLevelFields(byte[] body) throws IOException {
        Set<String> fields = new HashSet<>();
        if (body == null || body.length == 0) {
            return fields;
        }
        JsonNode node = OBJECT_MAPPER.readTree(body);
        if (node != null && node.isObject()) {
            node.fieldNames().forEachRemaining(fields::add);
        }
        return fields;
    }
}
