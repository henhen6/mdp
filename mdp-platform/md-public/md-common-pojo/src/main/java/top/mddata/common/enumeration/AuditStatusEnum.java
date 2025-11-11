package top.mddata.common.enumeration;

import top.mddata.base.interfaces.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核状态
 * [0-初始化 1-申请中 2-通过 99-退回]
 * @author henhen6
 * @since 2021/4/16 11:26 上午
 */
@Getter
@AllArgsConstructor
@Schema(title = "AuditStatusEnum", description = "审核状态-枚举")
public enum AuditStatusEnum implements BaseEnum<Integer> {
    /**
     * 初始化
     */
    INITIALIZED(0, "待提交"),
    /**
     * 申请中
     */
    PENDING(1, "申请中"),
    APPROVED(2, "通过"),
    REJECTED(99, "退回");
    private final Integer code;
    private final String desc;

}
