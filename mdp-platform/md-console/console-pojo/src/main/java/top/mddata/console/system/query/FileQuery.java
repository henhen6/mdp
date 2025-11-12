package top.mddata.console.system.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文件")
public class FileQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID")
    private Long id;

    /**
     * 业务id
     */
    @Schema(description = "业务id")
    private Long objectId;

    /**
     * 对象类型
     */
    @Schema(description = "对象类型")
    private String objectType;

    /**
     * 文件类型
     */
    @Schema(description = "文件类型")
    private Integer fileType;

    /**
     * 文件访问地址
     */
    @Schema(description = "文件访问地址")
    private String url;

    /**
     * 文件大小
     * 单位字节
     */
    @Schema(description = "文件大小")
    private Long fileSize;

    /**
     * 文件名称
     */
    @Schema(description = "文件名称")
    private String filename;

    /**
     * 原始文件名
     */
    @Schema(description = "原始文件名")
    private String originalFilename;

    /**
     * 桶
     */
    @Schema(description = "桶")
    private String bucket;

    /**
     * 基础存储路径
     */
    @Schema(description = "基础存储路径")
    private String basePath;

    /**
     * 存储路径
     */
    @Schema(description = "存储路径")
    private String path;

    /**
     * 文件扩展名
     */
    @Schema(description = "文件扩展名")
    private String ext;

    /**
     * MIME类型
     */
    @Schema(description = "MIME类型")
    private String contentType;

    /**
     * 存储平台
     */
    @Schema(description = "存储平台")
    private String platform;

    /**
     * 缩略图访问路径
     */
    @Schema(description = "缩略图访问路径")
    private String thUrl;

    /**
     * 缩略图名称
     */
    @Schema(description = "缩略图名称")
    private String thFilename;

    /**
     * 缩略图大小
     * 单位字节
     */
    @Schema(description = "缩略图大小")
    private Long thSize;

    /**
     * 缩略图MIME类型
     */
    @Schema(description = "缩略图MIME类型")
    private String thContentType;

    /**
     * 文件元数据
     */
    @Schema(description = "文件元数据")
    private String metadata;

    /**
     * 文件用户元数据
     */
    @Schema(description = "文件用户元数据")
    private String userMetadata;

    /**
     * 缩略图元数据
     */
    @Schema(description = "缩略图元数据")
    private String thMetadata;

    /**
     * 缩略图用户元数据
     */
    @Schema(description = "缩略图用户元数据")
    private String thUserMetadata;

    /**
     * 附加属性
     */
    @Schema(description = "附加属性")
    private String attr;

    /**
     * 文件ACL
     */
    @Schema(description = "文件ACL")
    private String fileAcl;

    /**
     * 缩略图文件ACL
     */
    @Schema(description = "缩略图文件ACL")
    private String thFileAcl;

    /**
     * 哈希信息
     */
    @Schema(description = "哈希信息")
    private String hashInfo;

    /**
     * 上传ID
     * 仅在手动分片上传时使用
     */
    @Schema(description = "上传ID")
    private String uploadId;

    /**
     * 上传状态
     * 仅在手动分片上传时使用，1：初始化完成，2：上传完成
     */
    @Schema(description = "上传状态")
    private Integer uploadStatus;

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
