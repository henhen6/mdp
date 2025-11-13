package top.mddata.workbench.oauth2.vo;

import cn.hutool.core.map.MapUtil;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 凭证式返回
 * @author henhen6
 * @since 2025/8/29 16:07
 */
@Data
@Schema(title = "ClientTokenVo", description = "凭证式返回")
// 自动将驼峰转换下划线
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ClientTokenVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * Token 类型
     */
    @Schema(description = "Token 类型")
    private String tokenType;
    /**
     * Client-Token 值
     */
    @Schema(description = "Client-Token 值")
    private String clientToken;
    /**
     * Access-Token 值
     */
    @Schema(description = "Access-Token 值")
    private String accessToken;
    /**
     * 获取：此 Access-Token 的剩余有效期（秒）
     */
    @Schema(description = "Access-Token 的剩余有效期")
    private Long expiresIn;

    /**
     * 应用id
     */
    @Schema(description = "应用id")
    private String clientId;
    /**
     * 授权范围
     * 逗号分割
     */
    @Schema(description = "授权范围")
    private String scope;

    @Schema(description = "扩展参数")
    private Map<String, Object> extra = MapUtil.newHashMap();

    // 添加动态属性
    @JsonAnySetter
    public void addExtra(String key, Object value) {
        this.extra.put(key, value);
    }

    // 省略 getter 和 setter 方法
    @JsonAnyGetter
    public Map<String, Object> getExtra() {
        return extra;
    }

}
