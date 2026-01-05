package top.mddata.gateway.sop.interceptor.internal;

import com.gitee.sop.support.dto.CommonFileData;
import com.gitee.sop.support.dto.FileData;
import org.springframework.stereotype.Component;
import top.mddata.gateway.sop.common.RouteContext;
import top.mddata.gateway.sop.interceptor.RouteInterceptor;
import top.mddata.gateway.sop.interceptor.RouteInterceptorOrders;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * 对结果进行处理
 *
 * @author 六如
 */
@Component
public class ResultRouteInterceptor implements RouteInterceptor {

    private static final String CLASS = "class";
    private static final String KEY_NAME = "name";
    private static final String KEY_ORIGINAL_FILENAME = "originalFilename";
    private static final String KEY_CONTENT_TYPE = "contentType";
    private static final String KEY_BYTES = "bytes";

    @Override
    public Object afterRoute(RouteContext routeContext, Object result) {
        if (result == null) {
            return new HashMap<>();
        }

        if (result instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) result;
            Object className = map.get(CLASS);
            // 处理文件下载
            if (Objects.equals(className, FileData.class.getName()) || Objects.equals(className, CommonFileData.class.getName())) {
                /*
                {
                    "size": 27,
                    "bytes": "c3ByaW5nLnByb2ZpbGVzLmFjdGl2ZT1kZXYK",
                    "name": null,
                    "inputStream": {
                        "class": "java.io.ByteArrayInputStream"
                    },
                    "contentType": null,
                    "originalFilename": "application.properties",
                    "empty": false
                 */
                CommonFileData fileData = new CommonFileData();
                fileData.setName(String.valueOf(map.get(KEY_NAME)));
                fileData.setOriginalFilename(String.valueOf(map.get(KEY_ORIGINAL_FILENAME)));
                fileData.setContentType(String.valueOf(map.get(KEY_CONTENT_TYPE)));
                fileData.setData((byte[]) map.get(KEY_BYTES));
                return fileData;
            }
        }
        return result;
    }

    @Override
    public int getOrder() {
        return RouteInterceptorOrders.RESULT_INTERCEPTOR;
    }
}
