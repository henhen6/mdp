package top.mddata.console.service.coderule;

import org.junit.jupiter.api.Test;
import top.mddata.base.exception.ArgumentException;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.enumeration.coderule.CodeRuleTypeEnum;
import top.mddata.console.enumeration.coderule.OverflowStrategyEnum;
import top.mddata.console.service.coderule.generator.GenerateContext;
import top.mddata.console.service.coderule.generator.impl.SerialRuleItemGenerator;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SerialGeneratorTest {

    private CodeRuleItemConfig config(Integer length) {
        return new CodeRuleItemConfig().setLength(length);
    }

    @Test
    void 类别_返回流水号() {
        assertEquals(CodeRuleTypeEnum.SERIAL, new SerialRuleItemGenerator().getType());
    }

    @Test
    void 补位_不足长度左补零() {
        assertEquals("0001", SerialRuleItemGenerator.padAndCheck(1L, config(4)));
    }

    @Test
    void 补位_自定义补位字符() {
        CodeRuleItemConfig config = config(4).setPadChar("X");
        assertEquals("XXX1", SerialRuleItemGenerator.padAndCheck(1L, config));
    }

    @Test
    void 补位_补位字符为空时默认零() {
        CodeRuleItemConfig config = config(4).setPadChar(" ");
        assertEquals("0001", SerialRuleItemGenerator.padAndCheck(1L, config));
    }

    @Test
    void 补位_等长时原样返回() {
        assertEquals("1234", SerialRuleItemGenerator.padAndCheck(1234L, config(4)));
    }

    @Test
    void 补位_长度为空或非正数时原样返回() {
        assertEquals("12345", SerialRuleItemGenerator.padAndCheck(12345L, config(null)));
        assertEquals("12345", SerialRuleItemGenerator.padAndCheck(12345L, config(0)));
    }

    @Test
    void 超长_默认策略报错() {
        ArgumentException ex = assertThrows(ArgumentException.class,
                () -> SerialRuleItemGenerator.padAndCheck(12345L, config(4)));
        // 错误消息需带具体值与长度，便于定位
        assertEquals(true, ex.getMessage().contains("12345") && ex.getMessage().contains("4"));
    }

    @Test
    void 超长_增长策略原样返回() {
        CodeRuleItemConfig config = config(4)
                .setOverflowStrategy(OverflowStrategyEnum.GROW.getCode());
        assertEquals("12345", SerialRuleItemGenerator.padAndCheck(12345L, config));
    }

    @Test
    void 生成_从上下文取预分配值并补位() {
        GenerateContext context = new GenerateContext("testRule", Collections.emptyMap(),
                LocalDateTime.of(2026, 10, 28, 15, 30, 0), null);
        context.setCurrentItemId(123L);
        context.setSerialSupplier(id -> id == 123L ? 8L : null);

        String result = new SerialRuleItemGenerator().generate(config(4), context);
        assertEquals("0008", result);
    }
}
