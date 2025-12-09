package top.mddata.console.system.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件实体类。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
public class FileBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_file";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 业务id
     */
    private Long objectId;

    /**
     * 对象类型
     */
    private String objectType;

    /**
     * 文件类型
     */
    private Integer fileType;

    /**
     * 文件访问地址
     */
    private String url;

    /**
     * 文件大小
     * 单位字节
     */
    private Long fileSize;

    /**
     * 文件名称
     */
    private String filename;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 桶
     */
    private String bucket;

    /**
     * 基础存储路径
     */
    private String basePath;

    /**
     * 存储路径
     */
    private String path;

    /**
     * 文件扩展名
     */
    private String ext;

    /**
     * MIME类型
     */
    private String contentType;

    /**
     * 存储平台
     */
    private String platform;

    /**
     * 缩略图访问路径
     */
    private String thUrl;

    /**
     * 缩略图名称
     */
    private String thFilename;

    /**
     * 缩略图大小
     * 单位字节
     */
    private Long thSize;

    /**
     * 缩略图MIME类型
     */
    private String thContentType;

    /**
     * 文件元数据
     */
    private String metadata;

    /**
     * 文件用户元数据
     */
    private String userMetadata;

    /**
     * 缩略图元数据
     */
    private String thMetadata;

    /**
     * 缩略图用户元数据
     */
    private String thUserMetadata;

    /**
     * 附加属性
     */
    private String attr;

    /**
     * 文件ACL
     */
    private String fileAcl;

    /**
     * 缩略图文件ACL
     */
    private String thFileAcl;

    /**
     * 哈希信息
     */
    private String hashInfo;

    /**
     * 上传ID
     * 仅在手动分片上传时使用
     */
    private String uploadId;

    /**
     * 上传状态
     * 仅在手动分片上传时使用，1：初始化完成，2：上传完成
     */
    private Integer uploadStatus;

}
