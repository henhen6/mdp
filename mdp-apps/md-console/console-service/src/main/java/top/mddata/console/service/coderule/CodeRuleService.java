package top.mddata.console.service.coderule;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.entity.coderule.CodeRule;
import top.mddata.console.entity.coderule.CodeRuleItem;

import java.util.List;
import java.util.Map;

/**
 * 编码规则 服务层。
 *
 * @author henhen
 * @since 2026/10/29
 */
public interface CodeRuleService extends SuperService<CodeRule> {

    /**
     * 生成单个单号
     *
     * @param ruleCode 规则编码
     * @param params 变量参数（key 为变量名），可为空
     * @return 单号
     */
    String generate(String ruleCode, Map<String, String> params);

    /**
     * 批量生成单号（相同变量参数，流水号连续）
     *
     * @param ruleCode 规则编码
     * @param params 变量参数，可为空
     * @param count 生成数量（1-1000）
     * @return 单号列表
     */
    List<String> generateBatch(String ruleCode, Map<String, String> params, int count);

    /**
     * 批量生成单号（每个单号独立变量参数，流水号连续）
     *
     * @param ruleCode 规则编码
     * @param paramsList 各单号变量参数列表
     * @return 单号列表
     */
    List<String> generateMulti(String ruleCode, List<Map<String, String>> paramsList);

    /**
     * 试生成预览：流水号段不真实递增，按 Redis 当前值模拟，其余段正常生成
     *
     * @param ruleCode 规则编码
     * @param params 变量参数，可为空
     * @param count 生成数量（1-1000）
     * @return 预览单号列表
     */
    List<String> testGenerate(String ruleCode, Map<String, String> params, int count);

    /**
     * 失效规则缓存
     *
     * @param ruleId 规则ID
     */
    void evictCache(Long ruleId);

    /**
     * 全量替换规则明细：校验各类别必填字段 → 删旧 → 按列表顺序重排 sort 批量插入 → 失效缓存
     *
     * @param ruleId 规则ID
     * @param items 新明细列表（顺序即拼接顺序）
     * @return 落库后的明细列表
     */
    List<CodeRuleItem> saveItems(Long ruleId, List<CodeRuleItem> items);

    /**
     * 规则编码唯一性校验
     *
     * @param code 规则编码
     * @param excludeId 排除的规则ID（修改时传自身ID，新增时传null）
     * @return true=已存在
     */
    boolean existsByCode(String code, Long excludeId);

    /**
     * 查询规则明细列表（按 sort 升序）。
     *
     * @param ruleId 规则ID
     * @return 明细列表
     */
    List<CodeRuleItem> listItemsByRuleId(Long ruleId);
}
