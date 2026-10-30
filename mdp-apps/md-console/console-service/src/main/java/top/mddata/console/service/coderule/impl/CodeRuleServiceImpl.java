package top.mddata.console.service.coderule.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.exception.ArgumentException;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.console.CodeRuleCkBuilder;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.entity.coderule.CodeRule;
import top.mddata.console.entity.coderule.CodeRuleItem;
import top.mddata.console.entity.coderule.CodeSn;
import top.mddata.console.enumeration.coderule.CheckDigitAlgorithmEnum;
import top.mddata.console.enumeration.coderule.CodeRuleTypeEnum;
import top.mddata.console.enumeration.coderule.CutTypeEnum;
import top.mddata.console.enumeration.coderule.GuidTypeEnum;
import top.mddata.console.enumeration.coderule.OffsetUnitEnum;
import top.mddata.console.enumeration.coderule.OrgCodeSourceEnum;
import top.mddata.console.enumeration.coderule.OverflowStrategyEnum;
import top.mddata.console.enumeration.coderule.RandomCharsetEnum;
import top.mddata.console.enumeration.coderule.ResetRuleEnum;
import top.mddata.console.enumeration.coderule.VarTypeEnum;
import top.mddata.console.mapper.coderule.CodeRuleItemMapper;
import top.mddata.console.mapper.coderule.CodeRuleMapper;
import top.mddata.console.mapper.coderule.CodeSnMapper;
import top.mddata.console.service.coderule.CodeRuleService;
import top.mddata.console.service.coderule.generator.GenerateContext;
import top.mddata.console.service.coderule.generator.RuleItemGenerator;
import top.mddata.console.service.coderule.generator.RuleItemGeneratorRegistry;
import top.mddata.console.service.coderule.support.CodeRuleCache;
import top.mddata.console.service.coderule.support.PeriodKeyResolver;
import top.mddata.console.service.coderule.support.SnAllocator;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 编码规则 服务层实现。
 *
 * @author henhen
 * @since 2026/10/29
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CodeRuleServiceImpl extends SuperServiceImpl<CodeRuleMapper, CodeRule> implements CodeRuleService {

    private final CodeRuleItemMapper codeRuleItemMapper;
    private final CodeSnMapper codeSnMapper;
    private final SnAllocator snAllocator;
    private final RuleItemGeneratorRegistry generatorRegistry;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 更新链路内暂存旧规则编码。服务为单例，普通成员字段无法承载线程私有状态，
     * 故用 ThreadLocal 在 updateBefore 与 updateAfter 间传递，updateAfter 中必须 remove。
     */
    private final ThreadLocal<String> oldCodeHolder = new ThreadLocal<>();

    // ===== 生成入口 =====

    @Override
    public String generate(String ruleCode, Map<String, String> params) {
        Map<String, String> safeParams = params == null ? Collections.emptyMap() : params;
        return generateMulti(ruleCode, List.of(safeParams)).get(0);
    }

    @Override
    public List<String> generateBatch(String ruleCode, Map<String, String> params, int count) {
        ArgumentAssert.isTrue(count >= 1 && count <= 1000, "批量数量必须在 1-1000 之间");
        Map<String, String> safeParams = params == null ? Collections.emptyMap() : params;
        List<Map<String, String>> paramsList = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            paramsList.add(safeParams);
        }
        return doGenerate(ruleCode, paramsList, false);
    }

    @Override
    public List<String> generateMulti(String ruleCode, List<Map<String, String>> paramsList) {
        ArgumentAssert.notEmpty(paramsList, "参数列表不能为空");
        ArgumentAssert.isTrue(paramsList.size() <= 1000, "批量数量必须在 1-1000 之间");
        return doGenerate(ruleCode, paramsList, false);
    }

    @Override
    public List<String> testGenerate(String ruleCode, Map<String, String> params, int count) {
        ArgumentAssert.isTrue(count >= 1 && count <= 1000, "批量数量必须在 1-1000 之间");
        Map<String, String> safeParams = params == null ? Collections.emptyMap() : params;
        List<Map<String, String>> paramsList = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            paramsList.add(safeParams);
        }
        return doGenerate(ruleCode, paramsList, true);
    }

    // ===== 生成流水线 =====

    /**
     * 生成流水线：
     * 1. 加载规则缓存，不存在/禁用/无启用明细报错
     * 2. 无流水号/GUID 明细时仅告警（唯一性无保障，不拦截）
     * 3. 流水号段预分配（预览模式只读模拟）
     * 4. 逐参数 × 逐明细生成拼接
     */
    private List<String> doGenerate(String ruleCode, List<Map<String, String>> paramsList, boolean preview) {
        CodeRuleCache cache = loadRule(ruleCode);
        warnIfNoUniqueSegment(ruleCode, cache);

        int count = paramsList.size();
        LocalDateTime bizTime = LocalDateTime.now();
        Long userId = ContextUtil.getUserId();
        Map<Long, List<Long>> serialBlocks = allocateSerialBlocks(cache.getItems(), count, bizTime, preview);

        List<String> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            GenerateContext ctx = new GenerateContext(ruleCode, paramsList.get(i), bizTime, userId);
            int batchIndex = i;
            StringBuilder code = new StringBuilder();
            for (CodeRuleItem item : cache.getItems()) {
                ctx.setCurrentItemId(item.getId());
                ctx.setSerialSupplier(itemId -> serialBlocks.get(itemId).get(batchIndex));
                CodeRuleTypeEnum type = CodeRuleTypeEnum.of(item.getRuleType());
                ArgumentAssert.notNull(type, "规则明细[{}]的类别[{}]不受支持", item.getId(), item.getRuleType());
                RuleItemGenerator generator = generatorRegistry.get(type);
                String segment = generator.generate(item.getConfigJson(), ctx);
                ctx.appendPrefix(segment);
                code.append(segment);
            }
            result.add(code.toString());
        }
        return result;
    }

    /**
     * 加载规则缓存（cacheOps 防穿透：规则不存在缓存空值占位）。
     */
    private CodeRuleCache loadRule(String ruleCode) {
        ArgumentAssert.notBlank(ruleCode, "规则编码不能为空");
        CacheKey key = CodeRuleCkBuilder.builder(ruleCode);
        CacheResult<CodeRuleCache> result = cacheOps.get(key, k -> buildCache(ruleCode));
        CodeRuleCache cache = result.getValue();
        ArgumentAssert.notNull(cache, "编码规则[{}]不存在", ruleCode);
        ArgumentAssert.isTrue(Boolean.TRUE.equals(cache.getRule().getState()),
                "编码规则[{}]已禁用", ruleCode);
        ArgumentAssert.notEmpty(cache.getItems(), "编码规则[{}]没有启用的明细", ruleCode);
        return cache;
    }

    /**
     * 组装缓存结构：规则不存在返回 null（CacheOps 自动缓存空值）。
     */
    private CodeRuleCache buildCache(String ruleCode) {
        CodeRule rule = getOne(QueryWrapper.create().eq(CodeRule::getCode, ruleCode));
        if (rule == null) {
            return null;
        }
        List<CodeRuleItem> items = codeRuleItemMapper.selectListByQuery(QueryWrapper.create()
                .eq(CodeRuleItem::getRuleId, rule.getId())
                .eq(CodeRuleItem::getState, true)
                .orderBy(CodeRuleItem::getSort, true));
        return new CodeRuleCache().setRule(rule).setItems(items);
    }

    /**
     * 规则不含流水号/GUID 明细时，单号唯一性无保障，仅输出警告日志不拦截生成。
     */
    private void warnIfNoUniqueSegment(String ruleCode, CodeRuleCache cache) {
        boolean hasUniqueSegment = cache.getItems().stream().anyMatch(item ->
                CodeRuleTypeEnum.SERIAL.getCode().equals(item.getRuleType())
                        || CodeRuleTypeEnum.GUID.getCode().equals(item.getRuleType()));
        if (!hasUniqueSegment) {
            log.warn("规则[{}]不含流水号/GUID明细，单号唯一性无法保证", ruleCode);
        }
    }

    /**
     * 流水号段预分配：每个流水号明细一次性 INCRBY count 原子分配连续区间。
     * 预览模式不真实递增，读 Redis 当前值模拟（key 不存在按 initValue 起步）。
     */
    private Map<Long, List<Long>> allocateSerialBlocks(List<CodeRuleItem> items, int count,
                                                       LocalDateTime bizTime, boolean preview) {
        Map<Long, List<Long>> serialBlocks = new HashMap<>();
        for (CodeRuleItem item : items) {
            if (!CodeRuleTypeEnum.SERIAL.getCode().equals(item.getRuleType())) {
                continue;
            }
            CodeRuleItemConfig config = item.getConfigJson();
            ArgumentAssert.notNull(config, "流水号明细[{}]缺少配置", item.getId());
            long initValue = config.getInitValue() == null ? 1L : config.getInitValue();
            long step = config.getStep() == null ? 1L : config.getStep();
            ResetRuleEnum resetRule = parseEnum(ResetRuleEnum.class, config.getResetRule(),
                    StrUtil.format("流水号明细[{}]必须配置支持的归零规则", item.getId()));
            OffsetUnitEnum offsetUnit = StrUtil.isBlank(config.getOffsetUnit()) ? null
                    : parseEnum(OffsetUnitEnum.class, config.getOffsetUnit(),
                            StrUtil.format("流水号明细[{}]配置的偏移单位[{}]不受支持",
                                    item.getId(), config.getOffsetUnit()));
            String periodKey = PeriodKeyResolver.resolve(resetRule, config.getCycleOffset(), offsetUnit, bizTime);
            if (preview) {
                serialBlocks.put(item.getId(), peekSerialValues(item.getId(), periodKey, initValue, step, count));
            } else {
                long ttlMillis = resolveTtlMillis(resetRule);
                // 冷恢复跳跃量：未配置时按默认 1000（防 Redis 数据丢失重号）；0=精确恢复（Redis 丢失可能重号，用户自担）
                long recoverySkip = config.getRecoverySkip() == null
                        ? SnAllocator.DEFAULT_RECOVERY_SKIP : config.getRecoverySkip();
                serialBlocks.put(item.getId(), snAllocator.allocate(item.getId(), periodKey,
                        initValue, step, count, ttlMillis, recoverySkip));
            }
        }
        return serialBlocks;
    }

    /**
     * 预览取值：读流水号 key 当前值，模拟下一段连续值，只读不写。
     */
    private List<Long> peekSerialValues(Long ruleItemId, String periodKey,
                                        long initValue, long step, int count) {
        String key = "mdc:sn:" + ruleItemId + ":" + periodKey;
        String current = stringRedisTemplate.opsForValue().get(key);
        long first = current == null ? initValue : Long.parseLong(current) + step;
        List<Long> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            values.add(first + step * i);
        }
        return values;
    }

    /**
     * 归零规则 → Redis key TTL：取周期长度的安全余量，保证跨周期后旧 key 自动清理。
     * DAY=2天 WEEK=9天 MONTH=32天 YEAR=367天 NEVER=不过期。
     */
    private static long resolveTtlMillis(ResetRuleEnum resetRule) {
        return switch (resetRule) {
            case DAY -> Duration.ofDays(2).toMillis();
            case WEEK -> Duration.ofDays(9).toMillis();
            case MONTH -> Duration.ofDays(32).toMillis();
            case YEAR -> Duration.ofDays(367).toMillis();
            case NEVER -> -1L;
        };
    }

    // ===== 缓存失效 =====

    @Override
    public void evictCache(Long ruleId) {
        ArgumentAssert.notNull(ruleId, "规则ID不能为空");
        CodeRule rule = getById(ruleId);
        if (rule != null) {
            cacheOps.del(CodeRuleCkBuilder.builder(rule.getCode()));
        }
    }

    // ===== CRUD 增强 =====

    @Override
    protected CodeRule saveBefore(Object save) {
        CodeRule entity = super.saveBefore(save);
        ArgumentAssert.isFalse(existsByCode(entity.getCode(), null),
                "编码标识[{}]已存在", entity.getCode());
        return entity;
    }

    @Override
    protected void saveAfter(Object save, CodeRule entity) {
        // 必须失效：规则不存在时可能已缓存空值占位，不清掉会导致生成一直报不存在
        cacheOps.del(CodeRuleCkBuilder.builder(entity.getCode()));
    }

    @Override
    protected CodeRule updateBefore(Object update) {
        CodeRule entity = super.updateBefore(update);
        Long id = BeanUtil.getProperty(update, "id");
        String code = BeanUtil.getProperty(update, "code");
        ArgumentAssert.notNull(id, "规则ID不能为空");
        ArgumentAssert.notBlank(code, "规则编码不能为空");
        // 更新前记录旧编码：规则 code 变更后，旧 code 的缓存键必须在 updateAfter 一并失效
        CodeRule oldRule = getById(id);
        oldCodeHolder.set(oldRule == null ? null : oldRule.getCode());
        ArgumentAssert.isFalse(existsByCode(code, id), "编码标识[{}]已存在", code);
        return entity;
    }

    @Override
    protected void updateAfter(Object update, CodeRule entity) {
        String oldCode = oldCodeHolder.get();
        oldCodeHolder.remove();
        CodeRule fresh = getById(entity.getId());
        if (fresh != null) {
            cacheOps.del(CodeRuleCkBuilder.builder(fresh.getCode()));
        }
        // code 修改成功时旧键在 TTL 内仍命中陈旧缓存，必须同时删除（code 未变时两次删除幂等无害）
        if (StrUtil.isNotBlank(oldCode)) {
            cacheOps.del(CodeRuleCkBuilder.builder(oldCode));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        if (CollUtil.isEmpty(idList)) {
            return true;
        }
        List<CodeRule> rules = listByIds(idList);
        if (CollUtil.isEmpty(rules)) {
            return true;
        }
        List<Long> ruleIds = rules.stream().map(CodeRule::getId).toList();
        List<CodeRuleItem> items = codeRuleItemMapper.selectListByQuery(
                QueryWrapper.create().in(CodeRuleItem::getRuleId, ruleIds));

        // 先级联删明细及其流水号数据，再删规则与缓存
        codeRuleItemMapper.deleteByQuery(QueryWrapper.create().in(CodeRuleItem::getRuleId, ruleIds));
        deleteSerialData(items);
        boolean flag = super.removeByIds(ruleIds);
        List<CacheKey> cacheKeys = rules.stream()
                .map(rule -> CodeRuleCkBuilder.builder(rule.getCode())).toList();
        cacheOps.del(cacheKeys);
        return flag;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code, Long excludeId) {
        return count(QueryWrapper.create().eq(CodeRule::getCode, code)
                .ne(CodeRule::getId, excludeId)) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeRuleItem> listItemsByRuleId(Long ruleId) {
        ArgumentAssert.notNull(ruleId, "规则ID不能为空");
        return codeRuleItemMapper.selectListByQuery(QueryWrapper.create()
                .eq(CodeRuleItem::getRuleId, ruleId)
                .orderBy(CodeRuleItem::getSort, true));
    }

    // ===== 明细全量替换 =====

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CodeRuleItem> saveItems(Long ruleId, List<CodeRuleItem> items) {
        ArgumentAssert.notNull(ruleId, "规则ID不能为空");
        CodeRule rule = getById(ruleId);
        ArgumentAssert.notNull(rule, "编码规则[{}]不存在", ruleId);
        ArgumentAssert.notEmpty(items, "规则明细不能为空");

        // 先校验并装配全部新明细：任一不合法则不动旧数据（Redis 流水号删除不可回滚，顺序不能反）
        List<CodeRuleItem> toSave = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            CodeRuleItem item = items.get(i);
            ArgumentAssert.notNull(item, "第[{}]条明细为空", i + 1);
            validateItem(item);
            item.setRuleId(ruleId);
            item.setSort(i);
            if (item.getState() == null) {
                item.setState(true);
            }
            toSave.add(item);
        }

        List<CodeRuleItem> oldItems = codeRuleItemMapper.selectListByQuery(
                QueryWrapper.create().eq(CodeRuleItem::getRuleId, ruleId));

        // 流水号明细按 ruleType + configJson 内容匹配旧明细：内容未变更则复用旧 ID，
        // 水位/Redis key 随 ID 续用，避免换 ID 后从初始值重新起号。
        // 非流水号明细不参与匹配，保持原全量替换语义。
        List<CodeRuleItem> oldSerialPool = oldItems.stream()
                .filter(old -> CodeRuleTypeEnum.SERIAL.getCode().equals(old.getRuleType()))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        List<CodeRuleItem> reusedItems = new ArrayList<>();
        for (CodeRuleItem item : toSave) {
            if (!CodeRuleTypeEnum.SERIAL.getCode().equals(item.getRuleType())) {
                item.setId(null);
                continue;
            }
            CodeRuleItem matched = takeMatched(oldSerialPool, item);
            if (matched != null) {
                item.setId(matched.getId());
                reusedItems.add(item);
            } else {
                item.setId(null);
            }
        }

        // 仅删除未被复用的旧明细及其流水号数据
        List<Long> reusedIds = reusedItems.stream().map(CodeRuleItem::getId).toList();
        List<CodeRuleItem> toDelete = oldItems.stream()
                .filter(old -> !reusedIds.contains(old.getId())).toList();
        if (!toDelete.isEmpty()) {
            List<Long> deleteIds = toDelete.stream().map(CodeRuleItem::getId).toList();
            codeRuleItemMapper.deleteByQuery(
                    QueryWrapper.create().in(CodeRuleItem::getId, deleteIds));
            deleteSerialData(toDelete);
        }

        // 复用明细行已存在（内容相同仍 update 一次 sort/state），其余批量插入
        List<CodeRuleItem> toInsert = toSave.stream()
                .filter(item -> item.getId() == null).toList();
        if (!toInsert.isEmpty()) {
            codeRuleItemMapper.insertBatch(toInsert);
        }
        reusedItems.forEach(codeRuleItemMapper::update);
        evictCache(ruleId);
        return toSave;
    }

    /**
     * 从旧流水号明细池中取出一条 configJson 内容与目标相同的明细（每条旧明细最多匹配一次）。
     */
    private CodeRuleItem takeMatched(List<CodeRuleItem> pool, CodeRuleItem target) {
        var iterator = pool.iterator();
        while (iterator.hasNext()) {
            CodeRuleItem candidate = iterator.next();
            if (sameConfig(candidate.getConfigJson(), target.getConfigJson())) {
                iterator.remove();
                return candidate;
            }
        }
        return null;
    }

    /**
     * 判断两份明细配置内容是否一致。
     * Fastjson2 回读缺失的字符串字段会得到 ""，而前端提交为 null，
     * 故统一将 null 字符串序列化为 "" 后比较规范化 JSON（同类型字段顺序固定）。
     */
    private static boolean sameConfig(CodeRuleItemConfig a, CodeRuleItemConfig b) {
        return normalizeConfig(a).equals(normalizeConfig(b));
    }

    private static String normalizeConfig(CodeRuleItemConfig config) {
        return com.alibaba.fastjson2.JSON.toJSONString(config,
                com.alibaba.fastjson2.JSONWriter.Feature.WriteNullStringAsEmpty);
    }

    /**
     * 保存时按类别校验必填字段与枚举合法性（fail fast，宁报错不出错单号）。
     */
    private void validateItem(CodeRuleItem item) {
        CodeRuleTypeEnum type = CodeRuleTypeEnum.of(item.getRuleType());
        ArgumentAssert.notNull(type, "规则明细的类别[{}]不受支持", item.getRuleType());
        CodeRuleItemConfig config = item.getConfigJson();
        ArgumentAssert.notNull(config, "规则明细[{}]缺少配置", type.getDesc());
        switch (type) {
            case CONSTANT -> ArgumentAssert.notBlank(config.getValue(), "常量明细必须配置固定值");
            case DATE -> validateDate(config);
            case VARIABLE -> validateVariable(config);
            case SERIAL -> validateSerial(config);
            case RANDOM -> validateRandom(config);
            case CHECK_DIGIT -> parseEnum(CheckDigitAlgorithmEnum.class, config.getAlgorithm(),
                    "校验位明细必须配置支持的算法");
            case GUID -> parseEnum(GuidTypeEnum.class, config.getGuidType(),
                    "GUID明细必须配置支持的类型");
            case ORG_CODE -> parseEnum(OrgCodeSourceEnum.class, config.getSource(),
                    "组织编码明细必须配置支持的来源");
            case PARENT_CUT -> validateParentCut(config);
            // 枚举已全量覆盖，default 仅为防御未来新增类别
            default -> throw new ArgumentException("不支持的规则类别[{}]", type.getCode());
        }
    }

    private void validateDate(CodeRuleItemConfig config) {
        ArgumentAssert.notBlank(config.getPattern(), "日期明细必须配置日期格式");
        try {
            DateTimeFormatter.ofPattern(config.getPattern());
        } catch (IllegalArgumentException e) {
            throw new ArgumentException("日期格式[{}]不合法：{}", config.getPattern(), e.getMessage());
        }
    }

    private void validateVariable(CodeRuleItemConfig config) {
        ArgumentAssert.notBlank(config.getVarName(), "变量明细必须配置变量名");
        if (StrUtil.isNotBlank(config.getVarType())) {
            parseEnum(VarTypeEnum.class, config.getVarType(),
                    StrUtil.format("变量明细的变量类型[{}]不受支持", config.getVarType()));
        }
    }

    private void validateSerial(CodeRuleItemConfig config) {
        ArgumentAssert.notNull(config.getInitValue(), "流水号明细必须配置初始值");
        ArgumentAssert.notNull(config.getStep(), "流水号明细必须配置递增值");
        ArgumentAssert.notNull(config.getLength(), "流水号明细必须配置长度");
        parseEnum(ResetRuleEnum.class, config.getResetRule(), "流水号明细必须配置支持的归零规则");
        if (StrUtil.isNotBlank(config.getOverflowStrategy())) {
            parseEnum(OverflowStrategyEnum.class, config.getOverflowStrategy(),
                    StrUtil.format("流水号明细的超长策略[{}]不受支持", config.getOverflowStrategy()));
        }
        if (StrUtil.isNotBlank(config.getOffsetUnit())) {
            parseEnum(OffsetUnitEnum.class, config.getOffsetUnit(),
                    StrUtil.format("流水号明细的偏移单位[{}]不受支持", config.getOffsetUnit()));
        }
    }

    private void validateRandom(CodeRuleItemConfig config) {
        ArgumentAssert.notNull(config.getLength(), "随机码明细必须配置长度");
        if (StrUtil.isNotBlank(config.getCharset())) {
            parseEnum(RandomCharsetEnum.class, config.getCharset(),
                    StrUtil.format("随机码明细的字符集[{}]不受支持", config.getCharset()));
        }
    }

    private void validateParentCut(CodeRuleItemConfig config) {
        ArgumentAssert.notBlank(config.getVarName(), "父单号截取明细必须配置变量名");
        parseEnum(CutTypeEnum.class, config.getCutType(), "父单号截取明细必须配置支持的截取方式");
        if (CutTypeEnum.SEPARATOR.getCode().equals(config.getCutType())) {
            ArgumentAssert.notBlank(config.getSeparator(), "父单号截取明细必须配置分隔符");
            ArgumentAssert.notNull(config.getSplitPosition(), "父单号截取明细必须配置分隔符位置");
            ArgumentAssert.isTrue(config.getSplitPosition() != 0, "分隔符位置不能为 0");
        } else {
            ArgumentAssert.notNull(config.getStartIndex(), "父单号截取明细必须配置起始下标");
            ArgumentAssert.isTrue(config.getStartIndex() >= 0, "起始下标不能为负数");
        }
    }

    /**
     * 删除明细关联的流水号 Redis key 与水位记录。
     * 删 key 前必须先 flush Redis 当前值到 DB 水位表：Redis 删除不随事务回滚，
     * 一旦事务回滚，冷恢复会从水位+1000 起步，杜绝重号窗口。
     */
    private void deleteSerialData(List<CodeRuleItem> items) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        List<Long> itemIds = items.stream().map(CodeRuleItem::getId).distinct().toList();
        List<String> keys = new ArrayList<>();
        itemIds.forEach(itemId -> keys.addAll(scanKeys("mdc:sn:" + itemId + ":*")));
        keys.forEach(this::flushKeyWaterMark);
        if (!keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
        codeSnMapper.deleteByQuery(QueryWrapper.create().in(CodeSn::getRuleItemId, itemIds));
    }

    /**
     * 将单个流水号 key 的当前值 flush 到水位表。
     * key 格式：mdc:sn:{ruleItemId}:{periodKey}，periodKey 由 PeriodKeyResolver 生成，不含冒号。
     */
    private void flushKeyWaterMark(String key) {
        int prefixEnd = "mdc:sn:".length();
        int separator = key.indexOf(':', prefixEnd);
        Long ruleItemId = Long.parseLong(key.substring(prefixEnd, separator));
        String periodKey = key.substring(separator + 1);
        snAllocator.flushWaterMark(ruleItemId, periodKey);
    }

    /**
     * 按字符串 code 解析枚举（本功能所有字符串枚举的常量名与 code 一致）。
     *
     * @param type 枚举类型
     * @param code 编码
     * @param errorMsg 缺失或非法时的错误消息
     */
    private static <E extends Enum<E>> E parseEnum(Class<E> type, String code, String errorMsg) {
        ArgumentAssert.notBlank(code, errorMsg);
        try {
            return Enum.valueOf(type, code);
        } catch (IllegalArgumentException e) {
            throw new ArgumentException(errorMsg);
        }
    }

    /**
     * SCAN 查询 key（避免 KEYS 阻塞）。
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
