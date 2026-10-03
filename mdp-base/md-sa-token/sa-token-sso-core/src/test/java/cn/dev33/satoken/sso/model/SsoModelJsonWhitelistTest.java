package cn.dev33.satoken.sso.model;

import cn.dev33.satoken.json.SaJsonTemplateForJackson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * SSO 模型类的 JSON 全局类型白名单回归测试。
 *
 * <p>sa-token 1.46 起反序列化启用 PolymorphicTypeValidator 白名单：
 * 仅 {@code SaJsonType} 标记接口的实现类及 SPI 注册类型可反序列化。
 * fork 的 SSO 源码若未同步 1.46 的 {@code implements SaJsonType}，
 * 模式二 ticket 校验会抛 SaJsonConvertException（无法反序列化的类型）。</p>
 *
 * @author henhen6
 * @since 2026-10-04
 */
class SsoModelJsonWhitelistTest {

    private final SaJsonTemplateForJackson jsonTemplate = new SaJsonTemplateForJackson();

    @Test
    void TicketModel可经白名单往返序列化() {
        TicketModel model = new TicketModel("ticket-1", "web-console", 1001L, "token-abc");

        TicketModel back = jsonTemplate.jsonToObject(jsonTemplate.objectToJson(model), TicketModel.class);

        assertEquals("ticket-1", back.getTicket());
        assertEquals("web-console", back.getClient());
        assertEquals(1001L, back.getLoginId());
        assertEquals("token-abc", back.getTokenValue());
    }

    @Test
    void SaCheckTicketResult可经白名单往返序列化() {
        SaCheckTicketResult model = new SaCheckTicketResult();
        model.setLoginId(1001L);
        model.setTokenValue("token-abc");

        SaCheckTicketResult back = jsonTemplate.jsonToObject(
                jsonTemplate.objectToJson(model), SaCheckTicketResult.class);

        assertEquals(1001L, back.getLoginId());
        assertEquals("token-abc", back.getTokenValue());
    }

    @Test
    void SaSsoClientInfo可经白名单往返序列化() {
        SaSsoClientInfo model = new SaSsoClientInfo();
        model.setClient("web-console");

        SaSsoClientInfo back = jsonTemplate.jsonToObject(
                jsonTemplate.objectToJson(model), SaSsoClientInfo.class);

        assertEquals("web-console", back.getClient());
    }

    @Test
    void SaSsoClientModel可经白名单往返序列化() {
        cn.dev33.satoken.sso.config.SaSsoClientModel model = new cn.dev33.satoken.sso.config.SaSsoClientModel();
        model.setClient("web-console");

        cn.dev33.satoken.sso.config.SaSsoClientModel back = jsonTemplate.jsonToObject(
                jsonTemplate.objectToJson(model), cn.dev33.satoken.sso.config.SaSsoClientModel.class);

        assertEquals("web-console", back.getClient());
    }
}
