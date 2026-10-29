package top.mddata.console.service.coderule.generator;

import com.baidu.fsg.uid.UidGenerator;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import top.mddata.base.exception.ArgumentException;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.UserOrgRel;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.enumeration.coderule.CodeRuleTypeEnum;
import top.mddata.console.enumeration.coderule.CutTypeEnum;
import top.mddata.console.enumeration.coderule.GuidTypeEnum;
import top.mddata.console.enumeration.coderule.OrgCodeSourceEnum;
import top.mddata.console.enumeration.coderule.VarTypeEnum;
import top.mddata.console.service.coderule.generator.impl.CheckDigitRuleItemGenerator;
import top.mddata.console.service.coderule.generator.impl.GuidRuleItemGenerator;
import top.mddata.console.service.coderule.generator.impl.OrgCodeRuleItemGenerator;
import top.mddata.console.service.coderule.generator.impl.ParentCutRuleItemGenerator;
import top.mddata.console.service.coderule.generator.impl.RandomRuleItemGenerator;
import top.mddata.console.service.coderule.generator.impl.VariableRuleItemGenerator;
import top.mddata.console.service.organization.OrgService;
import top.mddata.console.service.organization.UserOrgRelService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 变量/随机码/校验位/GUID/组织编码/父单号截取 6 类生成器单测。
 */
class GeneratorExtTest {

    private GenerateContext context(Map<String, String> params, Long userId) {
        return new GenerateContext("TEST", params, LocalDateTime.of(2026, 10, 28, 10, 0), userId);
    }

    private GenerateContext context(Map<String, String> params) {
        return context(params, null);
    }

    // ===== 变量生成器 =====

    @Test
    void 变量_替换_传啥显示啥() {
        VariableRuleItemGenerator generator = new VariableRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setVarName("orderType");
        assertEquals("B2B", generator.generate(config, context(Map.of("orderType", "B2B"))));
    }

    @Test
    void 变量_替换_缺少key时报错() {
        VariableRuleItemGenerator generator = new VariableRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setVarName("orderType");
        ArgumentException ex = assertThrows(ArgumentException.class,
                () -> generator.generate(config, context(Map.of())));
        assertTrue(ex.getMessage().contains("缺少变量"), ex.getMessage());
    }

    @Test
    void 变量_未配置变量名时报错() {
        VariableRuleItemGenerator generator = new VariableRuleItemGenerator();
        assertThrows(ArgumentException.class,
                () -> generator.generate(new CodeRuleItemConfig(), context(Map.of("x", "1"))));
    }

    @Test
    void 变量_映射_有映射时翻译() {
        VariableRuleItemGenerator generator = new VariableRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setVarName("category")
                .setVarType(VarTypeEnum.MAPPING.getCode())
                .setMappings(Map.of("1", "电子产品"));
        assertEquals("电子产品", generator.generate(config, context(Map.of("category", "1"))));
    }

    @Test
    void 变量_映射_无映射时key本身兜底() {
        VariableRuleItemGenerator generator = new VariableRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setVarName("category")
                .setVarType(VarTypeEnum.MAPPING.getCode())
                .setMappings(Map.of("1", "电子产品"));
        assertEquals("9", generator.generate(config, context(Map.of("category", "9"))));
    }

    // ===== 随机码生成器 =====

    @Test
    void 随机码_数字字符集_6位纯数字() {
        RandomRuleItemGenerator generator = new RandomRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setLength(6).setCharset("DIGIT");
        String value = generator.generate(config, context(Map.of()));
        assertEquals(6, value.length());
        assertTrue(value.matches("\\d{6}"), value);
    }

    @Test
    void 随机码_全字符集_6位字母数字() {
        RandomRuleItemGenerator generator = new RandomRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setLength(6).setCharset("ALL");
        String value = generator.generate(config, context(Map.of()));
        assertEquals(6, value.length());
        assertTrue(value.matches("[0-9A-Za-z]{6}"), value);
    }

    @Test
    void 随机码_两次生成值不同() {
        RandomRuleItemGenerator generator = new RandomRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setLength(6).setCharset("ALL");
        String first = generator.generate(config, context(Map.of()));
        String second = generator.generate(config, context(Map.of()));
        assertNotEquals(first, second);
    }

    @Test
    void 随机码_未配置长度时报错() {
        RandomRuleItemGenerator generator = new RandomRuleItemGenerator();
        assertThrows(ArgumentException.class,
                () -> generator.generate(new CodeRuleItemConfig().setCharset("DIGIT"), context(Map.of())));
    }

    // ===== 校验位生成器 =====

    @Test
    void 校验位_Luhn经典用例_7992739871得3() {
        CheckDigitRuleItemGenerator generator = new CheckDigitRuleItemGenerator();
        GenerateContext ctx = context(Map.of());
        ctx.appendPrefix("7992739871");
        assertEquals("3", generator.generate(new CodeRuleItemConfig(), ctx));
    }

    @Test
    void 校验位_无前文时报错() {
        CheckDigitRuleItemGenerator generator = new CheckDigitRuleItemGenerator();
        assertThrows(ArgumentException.class,
                () -> generator.generate(new CodeRuleItemConfig(), context(Map.of())));
    }

    @Test
    void 校验位_含字母前文自动走ModN返回1位() {
        CheckDigitRuleItemGenerator generator = new CheckDigitRuleItemGenerator();
        GenerateContext ctx = context(Map.of());
        ctx.appendPrefix("ABC123");
        String value = generator.generate(new CodeRuleItemConfig(), ctx);
        assertEquals(1, value.length());
        assertTrue(value.matches("[0-9A-Z]"), value);
    }

    @Test
    void 校验位_强制ModN_纯数字前文返回1位() {
        CheckDigitRuleItemGenerator generator = new CheckDigitRuleItemGenerator();
        GenerateContext ctx = context(Map.of());
        ctx.appendPrefix("7992739871");
        String value = generator.generate(new CodeRuleItemConfig().setAlgorithm("LUHN_MOD_N"), ctx);
        assertEquals(1, value.length());
        assertTrue(value.matches("[0-9A-Z]"), value);
    }

    // ===== GUID 生成器 =====

    @Test
    void GUID_雪花ID截取前8位数字() {
        UidGenerator uidGenerator = mock(UidGenerator.class);
        when(uidGenerator.getUid()).thenReturn(1234567890123456789L);
        GuidRuleItemGenerator generator = new GuidRuleItemGenerator(uidGenerator);
        CodeRuleItemConfig config = new CodeRuleItemConfig()
                .setGuidType(GuidTypeEnum.SNOWFLAKE.getCode()).setLength(8);
        String value = generator.generate(config, context(Map.of()));
        assertEquals("12345678", value);
        assertTrue(value.matches("\\d{8}"), value);
    }

    @Test
    void GUID_标准UUID_长度0时返回完整32位() {
        UidGenerator uidGenerator = mock(UidGenerator.class);
        GuidRuleItemGenerator generator = new GuidRuleItemGenerator(uidGenerator);
        CodeRuleItemConfig config = new CodeRuleItemConfig()
                .setGuidType(GuidTypeEnum.GUID.getCode()).setLength(0);
        String value = generator.generate(config, context(Map.of()));
        assertTrue(Pattern.matches("[0-9a-f]{32}", value), value);
    }

    // ===== 组织编码生成器 =====

    @Test
    void 组织编码_DEPT返回部门编码() {
        UserOrgRelService userOrgRelService = mock(UserOrgRelService.class);
        OrgService orgService = mock(OrgService.class);
        Long deptId = 5001L;
        UserOrgRel rel = new UserOrgRel();
        rel.setUserId(1L);
        rel.setOrgId(deptId);
        Org dept = new Org();
        dept.setName("研发部");
        dept.setCode("DEPT01");
        when(userOrgRelService.getOne(any(QueryWrapper.class))).thenReturn(rel);
        when(orgService.getById(deptId)).thenReturn(dept);

        OrgCodeRuleItemGenerator generator = new OrgCodeRuleItemGenerator(userOrgRelService, orgService);
        CodeRuleItemConfig config = new CodeRuleItemConfig()
                .setSource(OrgCodeSourceEnum.DEPT.getCode());
        assertEquals("DEPT01", generator.generate(config, context(Map.of(), 1L)));
    }

    @Test
    void 组织编码_COMPANY取treePath第一段顶级公司编码() {
        UserOrgRelService userOrgRelService = mock(UserOrgRelService.class);
        OrgService orgService = mock(OrgService.class);
        Long companyId = 6001L;
        Long deptId = 5002L;
        UserOrgRel rel = new UserOrgRel();
        rel.setUserId(1L);
        rel.setOrgId(deptId);
        when(userOrgRelService.getOne(any(QueryWrapper.class))).thenReturn(rel);
        // tree_path 形如 /顶级id/父id/.../，split("/")[1] 即顶级公司
        Org dept = new Org();
        dept.setName("下属部门");
        dept.setTreePath("/" + companyId + "/" + deptId + "/");
        dept.setId(deptId);
        when(orgService.getById(deptId)).thenReturn(dept);
        Org company = new Org();
        company.setName("总公司");
        company.setCode("C001");
        when(orgService.getById(companyId)).thenReturn(company);

        OrgCodeRuleItemGenerator generator = new OrgCodeRuleItemGenerator(userOrgRelService, orgService);
        CodeRuleItemConfig config = new CodeRuleItemConfig()
                .setSource(OrgCodeSourceEnum.COMPANY.getCode());
        assertEquals("C001", generator.generate(config, context(Map.of(), 1L)));
    }

    @Test
    void 组织编码_无登录用户时报错() {
        UserOrgRelService userOrgRelService = mock(UserOrgRelService.class);
        OrgService orgService = mock(OrgService.class);
        OrgCodeRuleItemGenerator generator = new OrgCodeRuleItemGenerator(userOrgRelService, orgService);
        CodeRuleItemConfig config = new CodeRuleItemConfig()
                .setSource(OrgCodeSourceEnum.DEPT.getCode());
        ArgumentException ex = assertThrows(ArgumentException.class,
                () -> generator.generate(config, context(Map.of(), null)));
        assertTrue(ex.getMessage().contains("登录"), ex.getMessage());
    }

    @Test
    void 组织编码_用户未分配组织时报错() {
        UserOrgRelService userOrgRelService = mock(UserOrgRelService.class);
        OrgService orgService = mock(OrgService.class);
        when(userOrgRelService.getOne(any(QueryWrapper.class))).thenReturn(null);
        OrgCodeRuleItemGenerator generator = new OrgCodeRuleItemGenerator(userOrgRelService, orgService);
        assertThrows(ArgumentException.class,
                () -> generator.generate(new CodeRuleItemConfig(), context(Map.of(), 1L)));
    }

    // ===== 父单号截取生成器 =====

    @Test
    void 父单号_INDEX_未配长度时截到末尾() {
        ParentCutRuleItemGenerator generator = new ParentCutRuleItemGenerator();
        // "总公司-2026-HW0002" 下标：总0 公1 司2 -3 2=4
        CodeRuleItemConfig config = new CodeRuleItemConfig().setVarName("parentCode")
                .setCutType(CutTypeEnum.INDEX.getCode()).setStartIndex(3);
        assertEquals("-2026-HW0002",
                generator.generate(config, context(Map.of("parentCode", "总公司-2026-HW0002"))));
    }

    @Test
    void 父单号_INDEX_起始4时得到年份起的内容() {
        ParentCutRuleItemGenerator generator = new ParentCutRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setVarName("parentCode")
                .setCutType(CutTypeEnum.INDEX.getCode()).setStartIndex(4);
        assertEquals("2026-HW0002",
                generator.generate(config, context(Map.of("parentCode", "总公司-2026-HW0002"))));
    }

    @Test
    void 父单号_INDEX_指定长度截取() {
        ParentCutRuleItemGenerator generator = new ParentCutRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setVarName("parentCode")
                .setCutType(CutTypeEnum.INDEX.getCode()).setStartIndex(4).setLength(4);
        assertEquals("2026",
                generator.generate(config, context(Map.of("parentCode", "总公司-2026-HW0002"))));
    }

    @Test
    void 父单号_INDEX_下标超出长度时报错() {
        ParentCutRuleItemGenerator generator = new ParentCutRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setVarName("parentCode")
                .setCutType(CutTypeEnum.INDEX.getCode()).setStartIndex(100);
        assertThrows(ArgumentException.class,
                () -> generator.generate(config, context(Map.of("parentCode", "总公司-2026-HW0002"))));
    }

    @Test
    void 父单号_SEPARATOR_第1个分隔符之后() {
        ParentCutRuleItemGenerator generator = new ParentCutRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setVarName("parentCode")
                .setCutType(CutTypeEnum.SEPARATOR.getCode()).setSeparator("-").setSplitPosition(1);
        assertEquals("2026-HW0002",
                generator.generate(config, context(Map.of("parentCode", "总公司-2026-HW0002"))));
    }

    @Test
    void 父单号_SEPARATOR_负1取最后一个分隔符之后() {
        ParentCutRuleItemGenerator generator = new ParentCutRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setVarName("parentCode")
                .setCutType(CutTypeEnum.SEPARATOR.getCode()).setSeparator("-").setSplitPosition(-1);
        assertEquals("HW0002",
                generator.generate(config, context(Map.of("parentCode", "总公司-2026-HW0002"))));
    }

    @Test
    void 父单号_SEPARATOR_位置0报错() {
        ParentCutRuleItemGenerator generator = new ParentCutRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setVarName("parentCode")
                .setCutType(CutTypeEnum.SEPARATOR.getCode()).setSeparator("-").setSplitPosition(0);
        ArgumentException ex = assertThrows(ArgumentException.class,
                () -> generator.generate(config, context(Map.of("parentCode", "总公司-2026-HW0002"))));
        assertTrue(ex.getMessage().contains("不能为 0"), ex.getMessage());
    }

    @Test
    void 父单号_SEPARATOR_超过分隔符总数报错() {
        ParentCutRuleItemGenerator generator = new ParentCutRuleItemGenerator();
        CodeRuleItemConfig config = new CodeRuleItemConfig().setVarName("parentCode")
                .setCutType(CutTypeEnum.SEPARATOR.getCode()).setSeparator("-").setSplitPosition(3);
        ArgumentException ex = assertThrows(ArgumentException.class,
                () -> generator.generate(config, context(Map.of("parentCode", "总公司-2026-HW0002"))));
        assertTrue(ex.getMessage().contains("不存在第[3]"), ex.getMessage());
    }

    @Test
    void 各生成器_返回对应类别() {
        UidGenerator uidGenerator = mock(UidGenerator.class);
        assertEquals(CodeRuleTypeEnum.VARIABLE, new VariableRuleItemGenerator().getType());
        assertEquals(CodeRuleTypeEnum.RANDOM, new RandomRuleItemGenerator().getType());
        assertEquals(CodeRuleTypeEnum.CHECK_DIGIT, new CheckDigitRuleItemGenerator().getType());
        assertEquals(CodeRuleTypeEnum.GUID, new GuidRuleItemGenerator(uidGenerator).getType());
        assertEquals(CodeRuleTypeEnum.ORG_CODE,
                new OrgCodeRuleItemGenerator(mock(UserOrgRelService.class), mock(OrgService.class)).getType());
        assertEquals(CodeRuleTypeEnum.PARENT_CUT, new ParentCutRuleItemGenerator().getType());
    }
}
