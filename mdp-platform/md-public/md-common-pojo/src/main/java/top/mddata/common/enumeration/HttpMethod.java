package top.mddata.common.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

import java.util.stream.Stream;

/**
 * HTTP方法枚举
 *
 * @author henhen6
 */
@Getter
@Schema(description = "HTTP方法-枚举")
public enum HttpMethod implements BaseEnum<String> {
    /**
     * ALL
     */
    ALL,
    /**
     * GET:GET
     */
    GET,
    /**
     * POST:POST
     */
    POST,
    /**
     * PUT:PUT
     */
    PUT,
    /**
     * DELETE:DELETE
     */
    DELETE,
    /**
     * PATCH:PATCH
     */
    PATCH,
    /**
     * TRACE:TRACE
     */
    TRACE,
    /**
     * HEAD:HEAD
     */
    HEAD,
    /**
     * OPTIONS:OPTIONS
     */
    OPTIONS;


    public static HttpMethod match(String val, HttpMethod def) {
        return Stream.of(values()).parallel().filter(item -> item.getCode().equalsIgnoreCase(val)).findAny().orElse(def);
    }

    public static HttpMethod match(String val) {
        return match(val, null);
    }

    public boolean eq(HttpMethod val) {
        return val != null && eq(val.name());
    }


    @Override
    public String getCode() {
        return this.name();
    }

    @Override
    public String getDesc() {
        return this.name();
    }
}
