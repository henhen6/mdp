package top.mddata.base.fieldperm.engine;

import lombok.Data;
import org.junit.jupiter.api.Test;
import top.mddata.base.base.R;
import top.mddata.base.fieldperm.model.FieldRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * 字段权限引擎：对象树遍历 + 隐藏/脱敏规则。
 *
 * @author henhen6
 * @since 2026-10-02
 */
class FieldPermEngineTest {

    private final FieldPermEngine engine = new FieldPermEngine(new BuiltinMasker());

    @Data
    static class UserVo {
        private Long id;
        private String name;
        private String phone;
        private String email;
        private OrgVo org;
    }

    @Data
    static class OrgVo {
        private String name;
        private String phone;
    }

    private static UserVo user() {
        UserVo vo = new UserVo();
        vo.setId(1L);
        vo.setName("张三丰");
        vo.setPhone("13812345678");
        vo.setEmail("zhangsan@example.com");
        OrgVo org = new OrgVo();
        org.setName("研发部");
        org.setPhone("07551234567");
        vo.setOrg(org);
        return vo;
    }

    @Test
    void 隐藏规则将命中属性置空() {
        UserVo vo = user();
        engine.apply(vo, Map.of("phone", FieldRule.hide()));

        assertNull(vo.getPhone());
        // 同名字段语义：嵌套对象中的 phone 一并隐藏
        assertNull(vo.getOrg().getPhone());
        assertEquals("张三丰", vo.getName());
    }

    @Test
    void 脱敏规则按内置规则变形() {
        UserVo vo = user();
        engine.apply(vo, Map.of(
                "phone", FieldRule.mask(BuiltinMasker.MOBILE),
                "name", FieldRule.mask(BuiltinMasker.CHINESE_NAME)));

        assertEquals("138****5678", vo.getPhone());
        assertEquals("张*丰", vo.getName());
        // 固定电话不适用手机号规则，按原文保留（与 mybatis-flex Masks 行为一致）
        assertEquals("07551234567", vo.getOrg().getPhone());
        // 同名字段语义：嵌套对象的 name 一并脱敏
        assertEquals("研*部", vo.getOrg().getName());
    }

    @Test
    void 规则为空或目标为空时不动() {
        UserVo vo = user();
        engine.apply(vo, Map.of());
        engine.apply(null, Map.of("phone", FieldRule.hide()));
        assertEquals("13812345678", vo.getPhone());
    }

    @Test
    void R包装与集合嵌套均能下钻() {
        R<List<UserVo>> r = R.success(List.of(user(), user()));
        engine.apply(r, Map.of("phone", FieldRule.hide()));

        r.getData().forEach(vo -> {
            assertNull(vo.getPhone());
            assertNull(vo.getOrg().getPhone());
        });
    }

    @Test
    void 循环引用不栈溢出() {
        UserVo a = user();
        UserVo b = user();
        a.setOrg(null);
        b.setOrg(null);
        // 互相引用模拟循环（借用 org 字段以外的引用不方便，这里用集合互持）
        List<Object> listA = new ArrayList<>();
        List<Object> listB = new ArrayList<>();
        listA.add(a);
        listA.add(listB);
        listB.add(listA);

        engine.apply(listA, Map.of("phone", FieldRule.hide()));
        assertNull(a.getPhone());
    }

    @Test
    void 超过最大深度后不再下钻() {
        FieldPermEngine shallow = new FieldPermEngine(new BuiltinMasker(), 1, 5000);
        // depth0=R, depth1=UserVo, depth2=OrgVo 超限
        UserVo vo = user();
        R<UserVo> r = R.success(vo);
        shallow.apply(r, Map.of("phone", FieldRule.hide()));

        assertNull(vo.getPhone());
        assertEquals("07551234567", vo.getOrg().getPhone());
    }

    @Test
    void 非字符串字段脱敏规则被跳过() {
        UserVo vo = user();
        engine.apply(vo, Map.of("id", FieldRule.mask(BuiltinMasker.MOBILE)));
        assertEquals(1L, vo.getId());
    }

    @Test
    void 隐藏规则可作用于非字符串字段() {
        UserVo vo = user();
        engine.apply(vo, Map.of("id", FieldRule.hide()));
        assertNull(vo.getId());
    }

    @Test
    void 只读属性不报错且不影响其他规则() {
        Map<String, FieldRule> rules = Map.of(
                "class", FieldRule.hide(),
                "phone", FieldRule.hide());
        UserVo vo = user();
        engine.apply(vo, rules);
        assertNull(vo.getPhone());
    }

    @Test
    void 自定义Masker可插拔() {
        FieldPermEngine custom = new FieldPermEngine((rule, value) -> "[已脱敏]");
        UserVo vo = user();
        custom.apply(vo, Map.of("phone", FieldRule.mask("any")));
        assertEquals("[已脱敏]", vo.getPhone());
    }

    @Test
    void 内置脱敏规则与MybatisFlex行为对齐() {
        BuiltinMasker masker = new BuiltinMasker();
        assertEquals("138****5678", masker.mask(BuiltinMasker.MOBILE, "13812345678"));
        assertEquals("110**********1234", masker.mask(BuiltinMasker.ID_CARD_NUMBER, "11012345678901234"));
        assertEquals("张*丰", masker.mask(BuiltinMasker.CHINESE_NAME, "张三丰"));
        assertEquals("张*", masker.mask(BuiltinMasker.CHINESE_NAME, "张三"));
        assertEquals("zha**@example.com", masker.mask(BuiltinMasker.EMAIL, "zhang@example.com"));
        assertEquals("******", masker.mask(BuiltinMasker.PASSWORD, "123456"));
        assertEquals("4401****8888", masker.mask(BuiltinMasker.BANK_CARD_NUMBER, "440100008888"));
        // 未知规则原样返回
        assertSame("abc", masker.mask("not_exists", "abc"));
    }
}
