package top.mddata.coderule;

import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import top.mddata.base.exception.ArgumentException;
import top.mddata.console.dto.coderule.CodeRuleDto;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.entity.coderule.CodeRule;
import top.mddata.console.entity.coderule.CodeRuleItem;
import top.mddata.console.entity.coderule.CodeSn;
import top.mddata.console.enumeration.coderule.CodeRuleTypeEnum;
import top.mddata.console.mapper.coderule.CodeRuleItemMapper;
import top.mddata.common.cache.console.SnCkBuilder;
import top.mddata.console.mapper.coderule.CodeSnMapper;
import top.mddata.console.service.coderule.CodeRuleService;
import top.mddata.common.cache.console.CodeRuleCkBuilder;
import top.mddata.TestBootServerApplication;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 编码规则服务测试（连开发环境 Redis/MySQL）。
 * 规则编码一律使用 TEST_ 前缀，@AfterEach 清理规则、明细、缓存键、流水号 key、水位记录。
 */
@SpringBootTest(classes = TestBootServerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "mdp.log.ipv4.enabled=false",
        "mdp.log.ipv6.enabled=false"
})
class CodeRuleServiceTest {

    @Resource
    private CodeRuleService codeRuleService;
    @Resource
    private CodeRuleItemMapper codeRuleItemMapper;
    @Resource
    private CodeSnMapper codeSnMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /** 本测试类创建的规则编码（结束时清理） */
    private final List<String> createdCodes = new ArrayList<>();

    @BeforeAll
    static void initSchema(@org.springframework.beans.factory.annotation.Autowired JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `mdc_code_rule` (
                  `id` bigint NOT NULL COMMENT '主键',
                  `code` varchar(64) NOT NULL COMMENT '编码标识',
                  `name` varchar(128) NOT NULL COMMENT '规则名称',
                  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
                  `state` bit(1) DEFAULT b'1' COMMENT '状态',
                  `created_at` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                  `created_by` bigint DEFAULT '0' COMMENT '创建人ID',
                  `updated_at` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后修改时间',
                  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人ID',
                  PRIMARY KEY (`id`) USING BTREE,
                  UNIQUE KEY `uk_code` (`code`) USING BTREE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='编码规则'
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `mdc_code_rule_item` (
                  `id` bigint NOT NULL COMMENT '主键',
                  `rule_id` bigint NOT NULL COMMENT '所属编码规则',
                  `rule_type` tinyint NOT NULL COMMENT '规则类别',
                  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
                  `config_json` text COMMENT '类别差异化配置',
                  `state` bit(1) DEFAULT b'1' COMMENT '状态',
                  `created_at` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                  `created_by` bigint DEFAULT '0' COMMENT '创建人ID',
                  `updated_at` datetime(3) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后修改时间',
                  `updated_by` bigint DEFAULT NULL COMMENT '最后修改人ID',
                  PRIMARY KEY (`id`) USING BTREE,
                  KEY `idx_rule_id` (`rule_id`) USING BTREE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='编码规则明细'
                """);
    }

    @AfterEach
    void cleanAfter() {
        if (createdCodes.isEmpty()) {
            return;
        }
        // 规则缓存键：规则不存在时会缓存空值占位（DB 查不到规则），必须按编码强制删除
        stringRedisTemplate.delete(createdCodes.stream()
                .map(code -> CodeRuleCkBuilder.builder(code).getKey()).toList());

        List<CodeRule> rules = codeRuleService.list(
                QueryWrapper.create().in(CodeRule::getCode, createdCodes));
        List<Long> ruleIds = rules.stream().map(CodeRule::getId).toList();
        List<CodeRuleItem> items = ruleIds.isEmpty() ? List.of()
                : codeRuleItemMapper.selectListByQuery(
                        QueryWrapper.create().in(CodeRuleItem::getRuleId, ruleIds));

        deleteSerialKeysAndWater(items);
        if (!ruleIds.isEmpty()) {
            codeRuleItemMapper.deleteByQuery(
                    QueryWrapper.create().in(CodeRuleItem::getRuleId, ruleIds));
            codeRuleService.removeByIds(ruleIds);
        }

        // 清理结果验证：规则缓存键无残留
        assertEquals(List.of(), scanKeys("mdp:code_rule:code:TEST_*"),
                "测试结束后规则缓存键仍有残留");
        // 流水号 key 无残留
        List<String> leftSnKeys = new ArrayList<>();
        items.stream().map(CodeRuleItem::getId).distinct()
                .forEach(itemId -> leftSnKeys.addAll(scanKeys(SnCkBuilder.pattern(itemId))));
        assertEquals(List.of(), leftSnKeys, "测试结束后流水号 key 仍有残留");
        // 水位记录无残留
        if (!items.isEmpty()) {
            List<Long> itemIds = items.stream().map(CodeRuleItem::getId).toList();
            assertEquals(0L, codeSnMapper.selectCountByQuery(
                    QueryWrapper.create().in(CodeSn::getRuleItemId, itemIds)),
                    "测试结束后 mdc_code_sn 仍有残留水位记录");
        }
        createdCodes.clear();
    }

    /**
     * 清理上次失败运行可能残留的规则缓存键（失败用例可能没走到 @AfterEach 的删除）
     */
    @BeforeAll
    static void cleanLeftovers(
            @org.springframework.beans.factory.annotation.Autowired StringRedisTemplate redis) {
        List<String> keys = redis.execute((RedisCallback<List<String>>) connection -> {
            List<String> result = new ArrayList<>();
            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions()
                    .match("mdp:code_rule:code:TEST_*").count(500).build());
            while (cursor.hasNext()) {
                result.add(new String(cursor.next()));
            }
            cursor.close();
            return result;
        });
        if (!keys.isEmpty()) {
            redis.delete(keys);
        }
    }

    @Test
    void generate_完整拼装() {
        CodeRule rule = createRule("TEST_SO_RULE");
        addItem(rule, CodeRuleTypeEnum.CONSTANT, new CodeRuleItemConfig().setValue("SO-"), 0);
        addItem(rule, CodeRuleTypeEnum.VARIABLE, new CodeRuleItemConfig().setVarName("orderType"), 1);
        addItem(rule, CodeRuleTypeEnum.DATE, new CodeRuleItemConfig().setPattern("yyyyMMdd"), 2);
        addItem(rule, CodeRuleTypeEnum.CONSTANT, new CodeRuleItemConfig().setValue("-"), 3);
        addItem(rule, CodeRuleTypeEnum.SERIAL, serialConfig(), 4);

        String code = codeRuleService.generate("TEST_SO_RULE", Map.of("orderType", "B2B"));

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertEquals("SO-B2B" + today + "-0001", code);
    }

    @Test
    void generateBatch_流水号连续() {
        CodeRule rule = createRule("TEST_BATCH_RULE");
        addItem(rule, CodeRuleTypeEnum.CONSTANT, new CodeRuleItemConfig().setValue("PO-"), 0);
        addItem(rule, CodeRuleTypeEnum.SERIAL, serialConfig(), 1);

        List<String> codes = codeRuleService.generateBatch("TEST_BATCH_RULE", Map.of(), 3);

        assertEquals(3, codes.size());
        assertEquals(3, new HashSet<>(codes).size(), "单号存在重复");
        assertEquals("PO-0001", codes.get(0));
        assertEquals("PO-0002", codes.get(1));
        assertEquals("PO-0003", codes.get(2));
    }

    @Test
    void generateMulti_参数独立且流水号连续() {
        CodeRule rule = createRule("TEST_MULTI_RULE");
        addItem(rule, CodeRuleTypeEnum.VARIABLE, new CodeRuleItemConfig().setVarName("orderType"), 0);
        addItem(rule, CodeRuleTypeEnum.CONSTANT, new CodeRuleItemConfig().setValue("-"), 1);
        addItem(rule, CodeRuleTypeEnum.SERIAL, serialConfig(), 2);

        List<String> codes = codeRuleService.generateMulti("TEST_MULTI_RULE",
                List.of(Map.of("orderType", "B2B"), Map.of("orderType", "B2C")));

        assertEquals("B2B-0001", codes.get(0));
        assertEquals("B2C-0002", codes.get(1));
    }

    @Test
    void generate_规则不存在报错() {
        String ruleCode = "TEST_NOT_EXIST_RULE";
        createdCodes.add(ruleCode);

        ArgumentException ex = assertThrows(ArgumentException.class,
                () -> codeRuleService.generate(ruleCode, Map.of()));

        assertTrue(ex.getMessage().contains("编码规则[TEST_NOT_EXIST_RULE]不存在"), ex.getMessage());
    }

    @Test
    void generate_规则已禁用报错() {
        CodeRule rule = createRule("TEST_DISABLED_RULE", false);
        addItem(rule, CodeRuleTypeEnum.CONSTANT, new CodeRuleItemConfig().setValue("X"), 0);

        ArgumentException ex = assertThrows(ArgumentException.class,
                () -> codeRuleService.generate("TEST_DISABLED_RULE", Map.of()));

        assertTrue(ex.getMessage().contains("编码规则[TEST_DISABLED_RULE]已禁用"), ex.getMessage());
    }

    @Test
    void generate_无启用明细报错() {
        createRule("TEST_EMPTY_RULE");

        ArgumentException ex = assertThrows(ArgumentException.class,
                () -> codeRuleService.generate("TEST_EMPTY_RULE", Map.of()));

        assertTrue(ex.getMessage().contains("没有启用的明细"), ex.getMessage());
    }

    @Test
    void generate_无流水号或GUID明细_仅告警不拦截() {
        CodeRule rule = createRule("TEST_NO_UNIQUE_RULE");
        addItem(rule, CodeRuleTypeEnum.CONSTANT, new CodeRuleItemConfig().setValue("X"), 0);
        addItem(rule, CodeRuleTypeEnum.DATE, new CodeRuleItemConfig().setPattern("yyyy"), 1);

        String code = codeRuleService.generate("TEST_NO_UNIQUE_RULE", Map.of());

        assertEquals("X" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy")), code);
    }

    @Test
    void testGenerate_预览不占号且可重复() {
        CodeRule rule = createRule("TEST_PREVIEW_RULE");
        CodeRuleItem item = addItem(rule, CodeRuleTypeEnum.SERIAL, serialConfig(), 0);

        List<String> first = codeRuleService.testGenerate("TEST_PREVIEW_RULE", Map.of(), 1);
        List<String> second = codeRuleService.testGenerate("TEST_PREVIEW_RULE", Map.of(), 1);
        assertEquals(first, second, "预览两次结果应一致（未占号）");

        // 预览 3 个：模拟 0001-0003
        List<String> three = codeRuleService.testGenerate("TEST_PREVIEW_RULE", Map.of(), 3);
        assertEquals(List.of("0001", "0002", "0003"), three);

        // 流水号 key 不存在：预览没有写入 Redis
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertFalse(Boolean.TRUE.equals(stringRedisTemplate.hasKey(
                SnCkBuilder.builder(item.getId(), today).getKey())), "预览不应创建流水号 key");

        // 真实生成仍从 0001 开始
        assertEquals("0001", codeRuleService.generate("TEST_PREVIEW_RULE", Map.of()));
    }

    @Test
    void testGenerate_有当前值时模拟下一号() {
        CodeRule rule = createRule("TEST_PREVIEW_CUR_RULE");
        addItem(rule, CodeRuleTypeEnum.SERIAL, serialConfig(), 0);

        // 真实生成一个：Redis 当前值为 1
        assertEquals("0001", codeRuleService.generate("TEST_PREVIEW_CUR_RULE", Map.of()));

        // 预览按 当前值+step 模拟：0002、0003，但不推进真实值
        List<String> preview = codeRuleService.testGenerate("TEST_PREVIEW_CUR_RULE", Map.of(), 2);
        assertEquals(List.of("0002", "0003"), preview);

        // 真实生成的下一号仍是 0002
        assertEquals("0002", codeRuleService.generate("TEST_PREVIEW_CUR_RULE", Map.of()));
    }

    @Test
    void saveDto_编码重复报错() {
        createRule("TEST_DUP_RULE");
        CodeRuleDto dto = new CodeRuleDto().setCode("TEST_DUP_RULE").setName("重复规则").setState(true);

        ArgumentException ex = assertThrows(ArgumentException.class,
                () -> codeRuleService.saveDto(dto));

        assertTrue(ex.getMessage().contains("编码标识[TEST_DUP_RULE]已存在"), ex.getMessage());
    }

    @Test
    void updateDtoById_编码重复报错() {
        CodeRule first = createRule("TEST_UPD_A_RULE");
        CodeRule second = createRule("TEST_UPD_B_RULE");
        CodeRuleDto dto = new CodeRuleDto().setId(second.getId())
                .setCode("TEST_UPD_A_RULE").setName("改成别人的编码").setState(true);

        ArgumentException ex = assertThrows(ArgumentException.class,
                () -> codeRuleService.updateDtoById(dto));

        assertTrue(ex.getMessage().contains("编码标识[TEST_UPD_A_RULE]已存在"), ex.getMessage());
        // first 规则未被改动
        assertEquals("TEST_UPD_A_RULE", codeRuleService.getById(first.getId()).getCode());
    }

    @Test
    void updateDtoById_成功修改code后旧code缓存失效() {
        CodeRule rule = createRule("TEST_OLD_CODE_RULE");
        addItem(rule, CodeRuleTypeEnum.CONSTANT, new CodeRuleItemConfig().setValue("C-"), 0);
        addItem(rule, CodeRuleTypeEnum.SERIAL, serialConfig(), 1);
        createdCodes.add("TEST_NEW_CODE_RULE");

        // 改前先生成，使旧 code 的规则缓存加载并消耗一个流水号
        assertEquals("C-0001", codeRuleService.generate("TEST_OLD_CODE_RULE", Map.of()));
        String oldCacheKey = CodeRuleCkBuilder.builder("TEST_OLD_CODE_RULE").getKey();
        assertTrue(Boolean.TRUE.equals(stringRedisTemplate.hasKey(oldCacheKey)),
                "前置：旧 code 缓存应已写入");

        CodeRuleDto dto = new CodeRuleDto().setId(rule.getId())
                .setCode("TEST_NEW_CODE_RULE").setName("改名后规则").setState(true);
        codeRuleService.updateDtoById(dto);

        // 旧 code 的缓存键必须随更新失效，新 code 不应有残留缓存
        assertFalse(Boolean.TRUE.equals(stringRedisTemplate.hasKey(oldCacheKey)),
                "旧 code 缓存键应被删除");
        assertFalse(Boolean.TRUE.equals(stringRedisTemplate.hasKey(
                CodeRuleCkBuilder.builder("TEST_NEW_CODE_RULE").getKey())),
                "新 code 不应残留缓存");

        // DB 中已不存在旧 code，再生成必须报错，不能用陈旧缓存继续消耗流水段
        ArgumentException ex = assertThrows(ArgumentException.class,
                () -> codeRuleService.generate("TEST_OLD_CODE_RULE", Map.of()));
        assertTrue(ex.getMessage().contains("编码规则[TEST_OLD_CODE_RULE]不存在"), ex.getMessage());

        // 新 code 正常生成，明细未变，流水号续接 0002
        assertEquals("C-0002", codeRuleService.generate("TEST_NEW_CODE_RULE", Map.of()));
    }

    @Test
    void removeByIds_级联删除明细缓存与流水号数据() {
        CodeRule rule = createRule("TEST_DEL_RULE");
        addItem(rule, CodeRuleTypeEnum.CONSTANT,
                new CodeRuleItemConfig().setValue("SO-"), 0);
        CodeRuleItem serialItem = addItem(rule, CodeRuleTypeEnum.SERIAL, serialConfig(), 1);

        // 触发缓存加载与流水号分配
        codeRuleService.generate("TEST_DEL_RULE", Map.of());
        String cacheKey = CodeRuleCkBuilder.builder("TEST_DEL_RULE").getKey();
        assertTrue(Boolean.TRUE.equals(stringRedisTemplate.hasKey(cacheKey)), "前置：缓存应已写入");
        assertFalse(scanKeys(SnCkBuilder.pattern(serialItem.getId())).isEmpty(), "前置：流水号 key 应存在");

        boolean removed = codeRuleService.removeByIds(List.of(rule.getId()));
        assertTrue(removed);

        assertEquals(0L, codeRuleItemMapper.selectCountByQuery(
                QueryWrapper.create().eq(CodeRuleItem::getRuleId, rule.getId())), "明细应被级联删除");
        assertFalse(Boolean.TRUE.equals(stringRedisTemplate.hasKey(cacheKey)), "规则缓存应被删除");
        createdCodes.remove("TEST_DEL_RULE");
    }

    @Test
    void saveItems_全量替换并重排sort() {
        CodeRule rule = createRule("TEST_ITEMS_RULE");
        CodeRuleItem old1 = addItem(rule, CodeRuleTypeEnum.CONSTANT,
                new CodeRuleItemConfig().setValue("OLD1"), 0);
        CodeRuleItem old2 = addItem(rule, CodeRuleTypeEnum.CONSTANT,
                new CodeRuleItemConfig().setValue("OLD2"), 1);

        List<CodeRuleItem> newItems = List.of(
                newItem(CodeRuleTypeEnum.CONSTANT, new CodeRuleItemConfig().setValue("N1")),
                newItem(CodeRuleTypeEnum.CONSTANT, new CodeRuleItemConfig().setValue("N2")),
                newItem(CodeRuleTypeEnum.SERIAL, serialConfig()));

        List<CodeRuleItem> saved = codeRuleService.saveItems(rule.getId(), newItems);

        assertEquals(3, saved.size());
        assertEquals(List.of(0, 1, 2), saved.stream().map(CodeRuleItem::getSort).toList());
        assertTrue(saved.stream().allMatch(i -> i.getRuleId().equals(rule.getId())));
        // 旧明细已删除
        assertEquals(0L, codeRuleItemMapper.selectCountByQuery(QueryWrapper.create()
                .in(CodeRuleItem::getId, List.of(old1.getId(), old2.getId()))));
        // 缓存被失效：重新生成可以正常取到新明细
        assertEquals("N1N2", codeRuleService.generate("TEST_ITEMS_RULE", Map.of())
                .substring(0, 4));
    }

    @Test
    void saveItems_必填字段校验() {
        CodeRule rule = createRule("TEST_ITEMS_INVALID_RULE");

        // 常量缺固定值
        ArgumentException constantEx = assertThrows(ArgumentException.class,
                () -> codeRuleService.saveItems(rule.getId(), List.of(
                        newItem(CodeRuleTypeEnum.CONSTANT, new CodeRuleItemConfig()))));
        assertTrue(constantEx.getMessage().contains("常量明细必须配置固定值"), constantEx.getMessage());

        // 流水号缺归零规则
        ArgumentException serialEx = assertThrows(ArgumentException.class,
                () -> codeRuleService.saveItems(rule.getId(), List.of(
                        newItem(CodeRuleTypeEnum.SERIAL,
                                new CodeRuleItemConfig().setInitValue(1L).setStep(1L).setLength(4)))));
        assertTrue(serialEx.getMessage().contains("归零规则"), serialEx.getMessage());

        // 不支持的规则类别
        CodeRuleItem badType = new CodeRuleItem();
        badType.setRuleType(999);
        badType.setConfigJson(new CodeRuleItemConfig());
        ArgumentException typeEx = assertThrows(ArgumentException.class,
                () -> codeRuleService.saveItems(rule.getId(), List.of(badType)));
        assertTrue(typeEx.getMessage().contains("类别[999]不受支持"), typeEx.getMessage());

        // 分隔符截取缺分隔符
        ArgumentException cutEx = assertThrows(ArgumentException.class,
                () -> codeRuleService.saveItems(rule.getId(), List.of(
                        newItem(CodeRuleTypeEnum.PARENT_CUT, new CodeRuleItemConfig()
                                .setVarName("parentCode").setCutType("SEPARATOR")
                                .setSplitPosition(1)))));
        assertTrue(cutEx.getMessage().contains("必须配置分隔符"), cutEx.getMessage());
    }

    @Test
    void saveItems_相同内容流水号明细复用ID且流水续接() {
        CodeRule rule = createRule("TEST_REUSE_SN_RULE");
        addItem(rule, CodeRuleTypeEnum.CONSTANT,
                new CodeRuleItemConfig().setValue("SN-"), 0);
        CodeRuleItem serialItem = addItem(rule, CodeRuleTypeEnum.SERIAL, serialConfig(), 1);

        // 生成一次：流水号 Redis key 值为 1
        assertEquals("SN-0001", codeRuleService.generate("TEST_REUSE_SN_RULE", Map.of()));
        Long serialIdBefore = serialItem.getId();

        // 提交相同内容（前端回传的是全新对象，ID 为空）
        List<CodeRuleItem> sameItems = List.of(
                newItem(CodeRuleTypeEnum.CONSTANT, new CodeRuleItemConfig().setValue("SN-")),
                newItem(CodeRuleTypeEnum.SERIAL, serialConfig()));
        List<CodeRuleItem> saved = codeRuleService.saveItems(rule.getId(), sameItems);

        // 流水号明细 ID 必须稳定不变（常量明细按全量替换语义换 ID）
        CodeRuleItem savedSerial = saved.stream()
                .filter(i -> CodeRuleTypeEnum.SERIAL.getCode().equals(i.getRuleType()))
                .findFirst().orElseThrow();
        assertEquals(serialIdBefore, savedSerial.getId(), "内容未变更的流水号明细必须复用旧ID");

        // 再次生成：水位/Redis key 随旧 ID 续用，流水号不得重新起号
        assertEquals("SN-0002", codeRuleService.generate("TEST_REUSE_SN_RULE", Map.of()));
    }

    @Test
    void generateBatch_数量越界报错() {
        createRule("TEST_COUNT_RULE");

        ArgumentException zero = assertThrows(ArgumentException.class,
                () -> codeRuleService.generateBatch("TEST_COUNT_RULE", Map.of(), 0));
        assertTrue(zero.getMessage().contains("1-1000"), zero.getMessage());

        ArgumentException big = assertThrows(ArgumentException.class,
                () -> codeRuleService.generateBatch("TEST_COUNT_RULE", Map.of(), 1001));
        assertTrue(big.getMessage().contains("1-1000"), big.getMessage());
    }

    // ===== 测试夹具 =====

    private CodeRule createRule(String code) {
        return createRule(code, true);
    }

    private CodeRule createRule(String code, boolean state) {
        CodeRule rule = new CodeRule();
        rule.setCode(code);
        rule.setName(code);
        rule.setState(state);
        codeRuleService.save(rule);
        createdCodes.add(code);
        return rule;
    }

    @Test
    void 水位校准_重建被删的水位记录() {
        CodeRule rule = createRule("TEST_FLUSH_SN_RULE");
        addItem(rule, CodeRuleTypeEnum.CONSTANT,
                new CodeRuleItemConfig().setValue("EMP"), 10);
        addItem(rule, CodeRuleTypeEnum.DATE,
                new CodeRuleItemConfig().setPattern("yyyy"), 20);
        addItem(rule, CodeRuleTypeEnum.SERIAL,
                new CodeRuleItemConfig().setInitValue(1L).setStep(1L).setLength(4)
                        .setPadChar("0").setResetRule("NEVER").setOverflowStrategy("ERROR")
                        .setRecoverySkip(0), 30);

        // 生成两次：Redis 计数到 2
        codeRuleService.generate("TEST_FLUSH_SN_RULE", Map.of());
        codeRuleService.generate("TEST_FLUSH_SN_RULE", Map.of());
        CodeRuleItem serialItem = codeRuleService.listItemsByRuleId(rule.getId()).stream()
                .filter(i -> i.getRuleType().equals(CodeRuleTypeEnum.SERIAL.getCode()))
                .findFirst().orElseThrow();
        // 水位读取助手（CodeRuleServiceTest 无 readWaterMark，内联查询）
        java.util.function.Supplier<Long> waterMark = () -> {
            CodeSn sn = codeSnMapper.selectOneByQuery(QueryWrapper.create()
                    .eq(CodeSn::getRuleItemId, serialItem.getId()).eq(CodeSn::getPeriodKey, "FOREVER"));
            return sn == null ? null : sn.getCurrentValue();
        };
        assertTrue(waterMark.get() != null && waterMark.get() >= 2, "前置：水位已落库");

        // 模拟水位记录被删除
        codeSnMapper.deleteByQuery(QueryWrapper.create()
                .eq(CodeSn::getRuleItemId, serialItem.getId()).eq(CodeSn::getPeriodKey, "FOREVER"));

        // 水位校准：从 Redis 当前值重建
        Map<Long, Long> result = codeRuleService.flushSn("TEST_FLUSH_SN_RULE");
        assertTrue(result.containsKey(serialItem.getId()), "校准结果应包含流水号明细");
        assertEquals(2L, result.get(serialItem.getId()), "校准值应为 Redis 当前值");
        assertEquals(2L, waterMark.get(), "水位记录未重建");
    }

    private CodeRuleItem addItem(CodeRule rule, CodeRuleTypeEnum type, CodeRuleItemConfig config, int sort) {
        CodeRuleItem item = new CodeRuleItem();
        item.setRuleId(rule.getId());
        item.setRuleType(type.getCode());
        item.setState(true);
        item.setSort(sort);
        item.setConfigJson(config);
        codeRuleItemMapper.insert(item);
        return item;
    }

    private CodeRuleItem newItem(CodeRuleTypeEnum type, CodeRuleItemConfig config) {
        CodeRuleItem item = new CodeRuleItem();
        item.setRuleType(type.getCode());
        item.setState(true);
        item.setConfigJson(config);
        return item;
    }

    private CodeRuleItemConfig serialConfig() {
        return new CodeRuleItemConfig().setInitValue(1L).setStep(1L).setLength(4)
                .setResetRule("DAY").setOverflowStrategy("ERROR");
    }

    /**
     * 删除明细关联的流水号 Redis key 与水位记录
     */
    private void deleteSerialKeysAndWater(List<CodeRuleItem> items) {
        if (items.isEmpty()) {
            return;
        }
        List<Long> itemIds = items.stream().map(CodeRuleItem::getId).distinct().toList();
        codeSnMapper.deleteByQuery(QueryWrapper.create().in(CodeSn::getRuleItemId, itemIds));
        List<String> keys = new ArrayList<>();
        itemIds.forEach(itemId -> keys.addAll(scanKeys(SnCkBuilder.pattern(itemId))));
        if (!keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    /**
     * SCAN 查询 key（避免 KEYS 阻塞）
     */
    private List<String> scanKeys(String pattern) {
        return stringRedisTemplate.execute((RedisCallback<List<String>>) connection -> {
            List<String> keys = new ArrayList<>();
            Cursor<byte[]> cursor = connection.scan(
                    ScanOptions.scanOptions().match(pattern).count(500).build());
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
            cursor.close();
            return keys;
        });
    }
}
