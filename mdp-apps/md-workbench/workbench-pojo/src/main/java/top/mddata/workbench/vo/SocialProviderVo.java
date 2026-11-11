package top.mddata.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.mddata.workbench.enumeration.SocialProviderEnum;

/**
 * 第三方登录平台展示信息
 *
 * @author tangyh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SocialProviderVo", description = "第三方登录平台展示信息")
public class SocialProviderVo {

    /** 平台编码 */
    @Schema(description = "平台编码")
    private String code;

    /** 平台显示名称 */
    @Schema(description = "平台显示名称")
    private String label;

    /**
     * 由平台枚举构造展示对象
     *
     * @param provider 平台枚举
     * @return 展示信息
     */
    public static SocialProviderVo from(SocialProviderEnum provider) {
        return new SocialProviderVo(provider.getCode(), provider.getLabel());
    }
}
