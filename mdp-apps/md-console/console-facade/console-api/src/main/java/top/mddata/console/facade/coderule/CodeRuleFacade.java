package top.mddata.console.facade.coderule;

import java.util.List;
import java.util.Map;

/**
 * 编码规则（单号生成）门面。
 * 仅平台内部服务调用，不对第三方开发者开放。
 */
public interface CodeRuleFacade {

    /**
     * 生成单个单号
     *
     * @param ruleCode 编码标识
     * @param params 变量参数（可空）
     * @return 单号
     */
    String generate(String ruleCode, Map<String, String> params);

    /**
     * 同参批量生成（流水号连续分配）
     *
     * @param ruleCode 编码标识
     * @param params 变量参数（可空）
     * @param count 数量（1-1000）
     * @return 单号列表
     */
    List<String> generateBatch(String ruleCode, Map<String, String> params, int count);

    /**
     * 逐参批量生成（每组参数一个单号，流水号一次原子分配连续区间）
     *
     * @param ruleCode 编码标识
     * @param paramsList 变量参数列表（1-1000）
     * @return 单号列表
     */
    List<String> generateMulti(String ruleCode, List<Map<String, String>> paramsList);
}