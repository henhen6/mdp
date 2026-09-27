package top.mddata.common.controller;

import cn.hutool.core.map.MapUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.common.properties.IgnoreProperties;

import java.util.Map;

/**
 * 单体版服务前缀端点。
 *
 * <p>前端统一通过 /api/gateway/findOnlineServicePrefix 访问；
 * 单体版 dev proxy（boot 组）会把 /api/{servicePrefix} 整段剥掉，
 * 后端实际收到的路径为 /findOnlineServicePrefix，因此这里映射裸路径。
 * 与网关 GateController.findOnlineServicePrefix 同逻辑路径，保证前端零分支。
 */
@RestController
@RequiredArgsConstructor
public class ServicePrefixController {
    private final IgnoreProperties ignoreProperties;

    @Operation(summary = "查询在线服务的前缀")
    @GetMapping("/findOnlineServicePrefix")
    public R<Map<String, String>> findOnlineServicePrefix() {
        // 单体版所有模块同一 JVM，前缀集合即"在线服务"
        Map<String, String> map = MapUtil.newHashMap();
        ignoreProperties.getServicePrefixes().forEach(p -> map.put(p, p));
        return R.success(map);
    }
}
