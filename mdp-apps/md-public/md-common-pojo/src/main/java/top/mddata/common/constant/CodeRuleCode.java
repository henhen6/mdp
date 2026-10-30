package top.mddata.common.constant;

/**
 * 编码规则标识（调用 CodeRuleFacade/CodeRuleService 生成单号时使用）
 * 常量值与 mdc_code_rule.code 一一对应
 *
 * @author henhen6
 * @since 2026-10-30
 */
public interface CodeRuleCode {

    /**
     * 人员编号（常量EMP+入职年份+累计流水，新增用户时自动生成，终生绑定不可修改）
     */
    String USER_NO = "user_no";
}
