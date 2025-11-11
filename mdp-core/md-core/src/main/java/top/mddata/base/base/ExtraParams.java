package top.mddata.base.base;

import cn.hutool.core.map.MapUtil;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 扩展参数
 *
 * @author henhen6
 * @since 2020年02月14日16:19:36
 */
@Data
@NoArgsConstructor
@Schema(description = "扩展参数")
public class ExtraParams {

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
