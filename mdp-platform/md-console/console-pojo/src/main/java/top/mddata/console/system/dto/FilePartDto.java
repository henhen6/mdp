package top.mddata.console.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件分片
 * 仅在手动分片上传时使用 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件分片")
public class FilePartDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分片id
     */
    @NotNull(message = "请填写分片id", groups = BaseEntity.Update.class)
    @Schema(description = "分片id")
    private Long id;

    /**
     * 存储平台
     */
    @Size(max = 32, message = "存储平台长度不能超过{max}")
    @Schema(description = "存储平台")
    private String platform;

    /**
     * 上传ID，仅在手动分片上传时使用
     */
    @Size(max = 128, message = "上传ID，仅在手动分片上传时使用长度不能超过{max}")
    @Schema(description = "上传ID，仅在手动分片上传时使用")
    private String uploadId;

    /**
     * 分片 ETag
     */
    @Size(max = 255, message = "分片 ETag长度不能超过{max}")
    @Schema(description = "分片 ETag")
    private String eTag;

    /**
     * 分片号。每一个上传的分片都有一个分片号，一般情况下取值范围是1~10000
     */
    @Schema(description = "分片号。每一个上传的分片都有一个分片号，一般情况下取值范围是1~10000")
    private Integer partNumber;

    /**
     * 文件大小，单位字节
     */
    @Schema(description = "文件大小，单位字节")
    private Long partSize;

    /**
     * 哈希信息
     */
    @Size(max = 16383, message = "哈希信息长度不能超过{max}")
    @Schema(description = "哈希信息")
    private String hashInfo;

}
