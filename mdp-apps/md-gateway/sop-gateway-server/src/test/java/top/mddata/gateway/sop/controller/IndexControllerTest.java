package top.mddata.gateway.sop.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * rest0 版本号解析：必须用不触发 form body 消费的方式读取 query 参数
 */
class IndexControllerTest {

    @Test
    void query中有v时取该版本() {
        assertEquals("2.0", IndexController.resolveVersion("v=2.0"));
        assertEquals("2.0", IndexController.resolveVersion("a=1&v=2.0&b=3"));
    }

    @Test
    void query为空或无v时默认1点0() {
        assertEquals("1.0", IndexController.resolveVersion(null));
        assertEquals("1.0", IndexController.resolveVersion(""));
        assertEquals("1.0", IndexController.resolveVersion("a=1"));
        assertEquals("1.0", IndexController.resolveVersion("v="));
    }
}
