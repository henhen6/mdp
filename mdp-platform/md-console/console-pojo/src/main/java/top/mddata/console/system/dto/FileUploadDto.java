package top.mddata.console.system.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 附件上传
 *
 * @author henhen6
 * @date 2025年9月23日09:04:42
 */
@Data
@Schema(description = "附件上传业务参数")
public class FileUploadDto implements Serializable {

    @Schema(description = "桶")
    private String bucket;

    @Schema(description = "存储平台")
    private String platform;

    @Schema(description = "是否生成缩略图")
    private Boolean thumbnail;
}
