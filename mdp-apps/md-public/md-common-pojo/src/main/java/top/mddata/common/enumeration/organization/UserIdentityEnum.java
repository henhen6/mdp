package top.mddata.common.enumeration.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户身份。
 *
 * <p>身份由用户绑定的角色决定，判定规则见 UserIdentityService.resolve，
 * 设计文档：docs/用户体系/用户角色组织权限体系设计.md 第 1 节。</p>
 */
@Getter
@AllArgsConstructor
@Schema(description = "用户身份-枚举")
public enum UserIdentityEnum {
    /**
     * 运营者（绑定性质99-运营 的管理员角色）
     */
    OPERATIONS_ADMIN("运营者"),
    /**
     * 开发者管理员（绑定性质90-开发者 的管理员角色）
     */
    DEVELOPER_ADMIN("开发者管理员"),
    /**
     * 开发者（绑定性质90-开发者 的其他角色）
     */
    DEVELOPER("开发者"),
    /**
     * 普通用户（其余情况）
     */
    USER("普通用户");

    /**
     * 身份描述
     */
    private final String desc;
}
