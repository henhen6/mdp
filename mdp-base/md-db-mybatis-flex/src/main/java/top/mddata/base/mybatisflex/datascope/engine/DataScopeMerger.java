package top.mddata.base.mybatisflex.datascope.engine;

import top.mddata.base.mybatisflex.datascope.model.DataScopeEnum;
import top.mddata.base.mybatisflex.datascope.model.DataScopeGrant;

import cn.hutool.core.collection.CollUtil;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 多角色授权合并器（纯函数）：并集取最高优先级档。
 *
 * <p>最高档为内置档时只保留该档（同档多角色可能返回多条，但生成的
 * SQL 条件相同，行为等价于一条）；最高档为自定义时保留全部自定义授权
 * （调用方对各实现条件 OR 合并）。</p>
 *
 * @author henhen
 * @since 2026年09月26日
 */
public final class DataScopeMerger {

    private DataScopeMerger() {
    }

    /**
     * 取生效授权：优先级最高的档位胜出
     *
     * @param grants 当前用户对目标菜单的授权集合
     * @return 生效授权列表，空入参返回空列表；入参中档位为 null 的记录被忽略
     */
    public static List<DataScopeGrant> selectEffective(Collection<DataScopeGrant> grants) {
        if (CollUtil.isEmpty(grants)) {
            return List.of();
        }
        List<DataScopeGrant> valid = grants.stream()
                .filter(grant -> grant.getScope() != null)
                .toList();
        if (valid.isEmpty()) {
            return List.of();
        }
        int maxPriority = valid.stream()
                .map(DataScopeGrant::getScope)
                .max(Comparator.comparingInt(DataScopeEnum::priority))
                .map(DataScopeEnum::priority)
                .orElse(0);
        return valid.stream()
                .filter(grant -> grant.getScope().priority() == maxPriority)
                .toList();
    }
}
