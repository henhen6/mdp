package top.mddata.console.system.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import top.mddata.base.utils.ArgumentAssert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 复制文件
 *
 * @author henhen6
 * @date 2025年11月27日03:11:17
 */
@Data
@Schema(description = "复制文件")
@Builder
public class CopyFilesDto implements Serializable {

    @Schema(description = "原业务对象业务类型")
    private String objectType;
    @Schema(description = "原业务对象业务id")
    private Long objectId;
    @Schema(description = "新业务对象")
    private List<CopyFilesDto> targetFiles;

    public CopyFilesDto addTargetFiles(String objectType, Long objectId) {
        ArgumentAssert.notNull(objectId, "新业务对象业务id不能为空");
        ArgumentAssert.notEmpty(objectType, "新业务对象业务类型不能为空");
        if (targetFiles == null) {
            targetFiles = new ArrayList<>();
        }
        targetFiles.add(CopyFilesDto.builder().objectType(objectType).objectId(objectId).build());
        return this;
    }

}
