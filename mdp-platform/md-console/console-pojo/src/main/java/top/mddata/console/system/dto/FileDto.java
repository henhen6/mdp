package top.mddata.console.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
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
 * 文件 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件")
public class FileDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @NotNull(message = "请填写ID", groups = BaseEntity.Update.class)
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
    @NotEmpty(message = "请填写对象类型")
    @Size(max = 255, message = "对象类型长度不能超过{max}")
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
    @NotEmpty(message = "请填写文件访问地址")
    @Size(max = 512, message = "文件访问地址长度不能超过{max}")
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
    @Size(max = 255, message = "文件名称长度不能超过{max}")
    @Schema(description = "文件名称")
    private String filename;

    /**
     * 原始文件名
     */
    @Size(max = 255, message = "原始文件名长度不能超过{max}")
    @Schema(description = "原始文件名")
    private String originalFilename;

    /**
     * 桶
     */
    @Size(max = 255, message = "桶长度不能超过{max}")
    @Schema(description = "桶")
    private String bucket;

    /**
     * 基础存储路径
     */
    @Size(max = 255, message = "基础存储路径长度不能超过{max}")
    @Schema(description = "基础存储路径")
    private String basePath;

    /**
     * 存储路径
     */
    @Size(max = 255, message = "存储路径长度不能超过{max}")
    @Schema(description = "存储路径")
    private String path;

    /**
     * 文件扩展名
     */
    @Size(max = 32, message = "文件扩展名长度不能超过{max}")
    @Schema(description = "文件扩展名")
    private String ext;

    /**
     * MIME类型
     */
    @Size(max = 128, message = "MIME类型长度不能超过{max}")
    @Schema(description = "MIME类型")
    private String contentType;

    /**
     * 存储平台
     */
    @Size(max = 32, message = "存储平台长度不能超过{max}")
    @Schema(description = "存储平台")
    private String platform;

    /**
     * 缩略图访问路径
     */
    @Size(max = 512, message = "缩略图访问路径长度不能超过{max}")
    @Schema(description = "缩略图访问路径")
    private String thUrl;

    /**
     * 缩略图名称
     */
    @Size(max = 255, message = "缩略图名称长度不能超过{max}")
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
    @Size(max = 128, message = "缩略图MIME类型长度不能超过{max}")
    @Schema(description = "缩略图MIME类型")
    private String thContentType;

    /**
     * 文件元数据
     */
    @Size(max = 16383, message = "文件元数据长度不能超过{max}")
    @Schema(description = "文件元数据")
    private String metadata;

    /**
     * 文件用户元数据
     */
    @Size(max = 16383, message = "文件用户元数据长度不能超过{max}")
    @Schema(description = "文件用户元数据")
    private String userMetadata;

    /**
     * 缩略图元数据
     */
    @Size(max = 16383, message = "缩略图元数据长度不能超过{max}")
    @Schema(description = "缩略图元数据")
    private String thMetadata;

    /**
     * 缩略图用户元数据
     */
    @Size(max = 16383, message = "缩略图用户元数据长度不能超过{max}")
    @Schema(description = "缩略图用户元数据")
    private String thUserMetadata;

    /**
     * 附加属性
     */
    @Size(max = 16383, message = "附加属性长度不能超过{max}")
    @Schema(description = "附加属性")
    private String attr;

    /**
     * 文件ACL
     */
    @Size(max = 32, message = "文件ACL长度不能超过{max}")
    @Schema(description = "文件ACL")
    private String fileAcl;

    /**
     * 缩略图文件ACL
     */
    @Size(max = 32, message = "缩略图文件ACL长度不能超过{max}")
    @Schema(description = "缩略图文件ACL")
    private String thFileAcl;

    /**
     * 哈希信息
     */
    @Size(max = 16383, message = "哈希信息长度不能超过{max}")
    @Schema(description = "哈希信息")
    private String hashInfo;

    /**
     * 上传ID
     * 仅在手动分片上传时使用
     */
    @Size(max = 128, message = "上传ID长度不能超过{max}")
    @Schema(description = "上传ID")
    private String uploadId;

    /**
     * 上传状态
     * 仅在手动分片上传时使用，1：初始化完成，2：上传完成
     */
    @Schema(description = "上传状态")
    private Integer uploadStatus;

}
