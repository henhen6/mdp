package top.mddata.console.service.coderule.support;

import lombok.Data;
import lombok.experimental.Accessors;
import top.mddata.console.entity.coderule.CodeRule;
import top.mddata.console.entity.coderule.CodeRuleItem;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 编码规则缓存结构：规则本体 + 启用明细（已按 sort 升序）。
 * 规则不存在时由 CacheOps 缓存空值占位，防止缓存穿透。
 *
 * @author henhen
 * @since 2026/10/29
 */
@Data
@Accessors(chain = true)
public class CodeRuleCache implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 规则本体
     */
    private CodeRule rule;

    /**
     * 启用明细（state=true，按 sort 升序）
     */
    private List<CodeRuleItem> items;
}
