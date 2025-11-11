package top.mddata.base.mvcflex.request;

import top.mddata.base.base.entity.BaseEntity;
import cn.hutool.core.map.MapUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 分页参数
 *
 * @param <T> 业务入参类型
 * @author henhen6
 * @since 2020年02月14日16:19:36
 */
@Data
@NoArgsConstructor
@Schema(description = "分页参数")
public class PageParams<T> {

    @NotNull(message = "查询对象model不能为空")
    @Schema(description = "查询参数", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private T model;

    @Schema(description = "每页显示数据", example = "10")
    private long size = 10;

    @Schema(description = "当前页", example = "1")
    private long current = 1;

    @Schema(description = "排序字段，默认id；请传递实体类字段", allowableValues = "id,createdAt,updatedAt", example = "id")
    private String sort = BaseEntity.ID_FIELD;

    @Schema(description = "排序规则，默认descending", allowableValues = "descending,ascending", example = "descending")
    private String order = "descending";

    @Schema(description = "扩展参数")
    private Map<String, Object> extra = MapUtil.newHashMap();


    public PageParams(long current, long size) {
        this.size = size;
        this.current = current;
    }


    /**
     * 计算当前分页偏移量
     */
    @JsonIgnore
    public long offset() {
        long curr = this.current;
        if (curr <= 1L) {
            return 0L;
        }
        return (curr - 1) * this.size;
    }

    @JsonIgnore
    public PageParams<T> put(String key, Object value) {
        if (this.extra == null) {
            this.extra = new HashMap<>(16);
        }
        this.extra.put(key, value);
        return this;
    }

    @JsonIgnore
    public PageParams<T> putAll(Map<String, Object> extra) {
        if (this.extra == null) {
            this.extra = new HashMap<>(16);
        }
        this.extra.putAll(extra);
        return this;
    }
}
