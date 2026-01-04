package top.mddata.open.admin.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;

/**
 * 加密类型
 * [0-不加密 1-aes加密 2-sm4加密]
 * @author henhen
 * @since 2026/1/4 12:24
 */
@Getter
@AllArgsConstructor
@Schema(description = "加密类型-枚举")
public enum NotifyEncryptionTypeEnum implements BaseEnum<Integer> {
    NONE(0, "不加密"),
    AES(1, "aes加密"),
    SM4(2, "sm4加密");
    private final Integer code;
    private final String desc;

}
