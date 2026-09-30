package top.mddata.console.service.permission;

import org.junit.jupiter.api.Test;
import top.mddata.console.dto.permission.ResourceApiBindDto;
import top.mddata.console.entity.permission.ResourceApi;
import top.mddata.console.service.permission.impl.ResourceApiServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ResourceApiService 纯函数单元测试。
 *
 * @author henhen6
 * @since 2026-09-27
 */
class ResourceApiServiceTest {

    private static ResourceApiBindDto.ApiItem item(String uri, String method) {
        ResourceApiBindDto.ApiItem it = new ResourceApiBindDto.ApiItem();
        it.setUri(uri);
        it.setRequestMethod(method);
        return it;
    }

    @Test
    void filterNewApis_已关联的跳过() {
        ResourceApi existed = new ResourceApi();
        existed.setUri("/a/page");
        existed.setRequestMethod("POST");
        ResourceApiBindDto.ApiItem dup = item("/a/page", "POST");
        ResourceApiBindDto.ApiItem fresh = item("/a/list", "POST");
        List<ResourceApiBindDto.ApiItem> result =
                ResourceApiServiceImpl.filterNewApis(List.of(existed), List.of(dup, fresh));
        assertEquals(1, result.size());
        assertEquals("/a/list", result.get(0).getUri());
    }

    @Test
    void filterNewApis_入参自身去重() {
        List<ResourceApiBindDto.ApiItem> result = ResourceApiServiceImpl.filterNewApis(
                List.of(), List.of(item("/a/page", "POST"), item("/a/page", "POST")));
        assertEquals(1, result.size());
    }
}
