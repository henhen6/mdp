package top.mddata.base.mybatisflex.datascope.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一条（角色 × 菜单）数据范围授权。
 *
 * @author henhen
 * @since 2026年09月26日
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataScopeGrant {
    /** 角色id */
    private Long roleId;
    /** 数据范围档位 */
    private DataScopeEnum scope;
    /** 自定义实现的 Spring Bean 名（仅 scope=CUSTOM 时非空） */
    private String scopeImpl;
}
