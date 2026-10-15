package top.mddata.open.vo.client;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 接口调试-组装后的请求参数
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "接口调试-组装后的请求参数")
public class DocDebugBuildParamVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 完整请求地址（环境网关地址 + 接口路径）
     */
    @Schema(description = "完整请求地址")
    private String url;

    /**
     * 按 mdp-sdk 规则组装并签名后的表单参数，可直接以 form-urlencoded 提交
     */
    @Schema(description = "可直接提交的表单参数")
    private Map<String, String> form;
}
