package top.mddata.console.system.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.console.system.entity.base.FilePartBase;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件分片
 * 仅在手动分片上传时使用 VO类（通常用作Controller出参）。
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
@Table(FilePartBase.TABLE_NAME)
public class FilePartVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 分片id
     */
    @Id
    @Schema(description = "分片id")
    private Long id;

    /**
     * 存储平台
     */
    @Schema(description = "存储平台")
    private String platform;

    /**
     * 上传ID，仅在手动分片上传时使用
     */
    @Schema(description = "上传ID，仅在手动分片上传时使用")
    private String uploadId;

    /**
     * 分片 ETag
     */
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
    @Schema(description = "哈希信息")
    private String hashInfo;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;

    /**
     * 最后修改时间
     */
    @Schema(description = "最后修改时间")
    private LocalDateTime updatedAt;

    /**
     * 最后修改人
     */
    @Schema(description = "最后修改人")
    private Long updatedBy;

}
