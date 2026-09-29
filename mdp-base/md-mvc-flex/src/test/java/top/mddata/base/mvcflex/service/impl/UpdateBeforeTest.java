package top.mddata.base.mvcflex.service.impl;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.update.UpdateWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.mvcflex.context.UpdateFieldContext;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SuperServiceImpl.updateBefore 的选择性更新语义测试。
 *
 * <p>核心歧义：JSON 反序列化后"字段没提交"与"提交了 null"都是 Java null，
 * 由 UpdateFieldContext 中请求体的顶层 key 集合区分。</p>
 */
class UpdateBeforeTest {

    private final TestService service = new TestService();

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void noHttpContextShouldCopyAll() {
        // 无 HTTP 上下文（内部调用、测试）：维持全量更新旧语义
        TestUpdateDto dto = new TestUpdateDto();
        dto.setId(1L);
        dto.setName("张三");

        TestEntity entity = service.updateBefore(dto);
        Map<String, Object> updates = updatesOf(entity);

        assertEquals("张三", updates.get("name"));
        assertTrue(updates.containsKey("remark"), "全量语义下 null 字段也应被追踪");
        assertNull(updates.get("remark"));
    }

    @Test
    void httpContextShouldDistinguishSubmittedNullFromAbsent() {
        // 模拟表单提交 {"id":1,"name":"张三","remark":null}：nickName 未提交
        mockHttpRequest(Set.of("id", "name", "remark"));
        TestUpdateDto dto = new TestUpdateDto();
        dto.setId(1L);
        dto.setName("张三");

        TestEntity entity = service.updateBefore(dto);
        Map<String, Object> updates = updatesOf(entity);

        assertEquals("张三", updates.get("name"), "提交且有值的字段正常更新");
        assertTrue(updates.containsKey("remark"), "提交为 null 的字段应被追踪置空");
        assertNull(updates.get("remark"));
        assertFalse(updates.containsKey("nickName"), "未提交的字段不应被更新");
    }

    @Test
    void httpContextShouldSkipUnknownJsonKeys() {
        // 请求体携带 DTO 中不存在的 key（如前冗余字段），应安全跳过
        mockHttpRequest(Set.of("name", "ghost"));
        TestUpdateDto dto = new TestUpdateDto();
        dto.setName("李四");

        TestEntity entity = service.updateBefore(dto);
        Map<String, Object> updates = updatesOf(entity);

        assertEquals("李四", updates.get("name"));
        assertFalse(updates.containsKey("ghost"));
    }

    @Test
    void emptyPresentFieldsShouldNotSetNull() {
        // 空请求体 {}：所有 DTO 字段均为 null 且均未提交，无字段更新
        mockHttpRequest(Set.of());
        TestUpdateDto dto = new TestUpdateDto();

        TestEntity entity = service.updateBefore(dto);

        assertTrue(updatesOf(entity).isEmpty());
    }

    private void mockHttpRequest(Set<String> presentFields) {
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest()));
        UpdateFieldContext.set(presentFields);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> updatesOf(TestEntity entity) {
        return ((UpdateWrapper<TestEntity>) entity).getUpdates();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    static class TestEntity extends BaseEntity<Long> {
        private String name;
        private String remark;
        private String nickName;
    }

    @Data
    static class TestUpdateDto {
        private Long id;
        private String name;
        private String remark;
        private String nickName;
    }

    static class TestService extends SuperServiceImpl<BaseMapper<TestEntity>, TestEntity> {
    }
}
