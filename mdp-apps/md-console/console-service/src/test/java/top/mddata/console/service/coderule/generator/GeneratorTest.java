package top.mddata.console.service.coderule.generator;

import org.junit.jupiter.api.Test;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.service.coderule.generator.impl.ConstantRuleItemGenerator;
import top.mddata.console.service.coderule.generator.impl.DateRuleItemGenerator;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeneratorTest {

    private GenerateContext ctx() {
        return new GenerateContext("testRule", Collections.emptyMap(), LocalDateTime.of(2026, 10, 28, 15, 30, 0), null);
    }

    @Test
    void 常量_返回固定值() {
        ConstantRuleItemGenerator g = new ConstantRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setValue("SO-");
        assertEquals("SO-", g.generate(config, ctx()));
    }

    @Test
    void 日期_按格式实时生成() {
        DateRuleItemGenerator g = new DateRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setPattern("yyyyMMdd");
        assertEquals("20261028", g.generate(config, ctx()));
    }

    @Test
    void 日期_非法格式报错() {
        DateRuleItemGenerator g = new DateRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setPattern("xx@@@");
        assertThrows(Exception.class, () -> g.generate(config, ctx()));
    }

    @Test
    void nextSerial_透传当前明细ID() {
        // 验证 GenerateContext.nextSerial() 把 currentItemId 传给 serialSupplier
        GenerateContext ctx = ctx();
        java.util.concurrent.atomic.AtomicLong captured = new java.util.concurrent.atomic.AtomicLong();
        ctx.setSerialSupplier(id -> {
            captured.set(id);
            return 99L;
        });
        ctx.setCurrentItemId(123L);
        Long value = ctx.nextSerial();
        assertEquals(123L, captured.get());
        assertEquals(99L, value);
    }
}
