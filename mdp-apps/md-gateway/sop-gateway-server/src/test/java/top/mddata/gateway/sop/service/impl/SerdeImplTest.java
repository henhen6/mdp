package top.mddata.gateway.sop.service.impl;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SerdeImplTest {

    @Test
    void Long类型序列化为字符串避免前端精度丢失() {
        SerdeImpl serde = new SerdeImpl();
        serde.dateFormat = "yyyy-MM-dd HH:mm:ss";
        serde.init();

        Map<String, Object> data = new LinkedHashMap<>();
        // 雪花 id：超过 JS Number.MAX_SAFE_INTEGER（2^53），前端解析会丢精度
        data.put("id", 147117490476188673L);
        // Integer 不受影响，保持数值
        data.put("code", 0);
        data.put("name", "test");

        String json = serde.toJson(data);

        assertEquals("{\"id\":\"147117490476188673\",\"code\":0,\"name\":\"test\"}", json);
    }
}
