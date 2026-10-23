package top.mddata.console.dto.system;


import cn.hutool.core.util.ObjUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 关联文件到业务
 *
 * @author henhen6
 * @date 2025年10月23日09:04:34
 */
@Data
@Schema(description = "关联文件到业务")
@Builder
public class RelateFilesToBizDto implements Serializable {

    @NotEmpty(message = "业务类型不能为空")
    @Schema(description = "业务类型")
    private String objectType;
    @NotNull(message = "业务id不能为空")
    @Schema(description = "业务id")
    private Long objectId;
    @Schema(description = "业务id集合（批量释放附件时使用，优先级高于 objectId）")
    private List<Long> objectIds;
    @Schema(description = "当前关联文件")
    private List<Long> keepFileIds;

    public RelateFilesToBizDto addKeepFileIds(List<Long> keepFileIds) {
        if (keepFileIds == null) {
            keepFileIds = Collections.emptyList();
        } else {
            keepFileIds = keepFileIds.stream().filter(ObjUtil::isNotNull).toList();
        }
        this.keepFileIds = keepFileIds;
        return this;
    }

    public RelateFilesToBizDto setKeepFileIds(Long keepFileIds) {
        if (keepFileIds != null) {
            this.keepFileIds = Collections.singletonList(keepFileIds);
        }
        return this;
    }
}
