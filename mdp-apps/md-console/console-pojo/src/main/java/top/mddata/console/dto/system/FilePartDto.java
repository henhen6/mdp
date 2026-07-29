package top.mddata.console.dto.system;

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
import java.util.List;

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

    @Data
    @Schema(description = "初始化分片上传请求")
    public static class InitPartUploadDto implements Serializable {
        @Schema(description = "文件名")
        private String fileName;
        @Schema(description = "文件大小(字节)")
        private Long fileSize;
        @Schema(description = "文件哈希(可选)")
        private String fileHash;
    }

    @Data
    @Schema(description = "初始化分片上传响应")
    public static class InitPartUploadResp implements Serializable {
        @Schema(description = "上传ID")
        private String uploadId;
        @Schema(description = "分片大小(字节)")
        private Long chunkSize;
        @Schema(description = "总分片数")
        private Integer totalChunks;
    }

    @Data
    @Schema(description = "分片上传请求")
    public static class UploadPartDto implements Serializable {
        @Schema(description = "上传ID")
        private String uploadId;
        @Schema(description = "分片号(从1开始)")
        private Integer partNumber;
    }

    @Data
    @Schema(description = "分片上传响应")
    public static class UploadPartResp implements Serializable {
        @Schema(description = "分片号")
        private Integer partNumber;
        @Schema(description = "分片标识")
        private String eTag;
    }

    @Data
    @Schema(description = "已上传分片信息")
    public static class UploadedPart implements Serializable {
        @Schema(description = "分片号")
        private Integer partNumber;
        @Schema(description = "分片标识")
        private String eTag;
    }

    @Data
    @Schema(description = "查询上传进度响应")
    public static class UploadProgressResp implements Serializable {
        @Schema(description = "上传ID")
        private String uploadId;
        @Schema(description = "已上传分片列表")
        private List<UploadedPart> uploadedParts;
        @Schema(description = "是否完成")
        private Boolean isComplete;
    }

    @Data
    @Schema(description = "完成合并请求")
    public static class CompletePartUploadDto implements Serializable {
        @Schema(description = "上传ID")
        private String uploadId;

        @Schema(description = "对象类型")
        private String objectType;

    }

    @Data
    @Schema(description = "完成合并响应")
    public static class CompletePartUploadResp implements Serializable {
        @Schema(description = "文件ID")
        private Long fileId;
        @Schema(description = "访问URL")
        private String url;
    }

    @Data
    @Schema(description = "取消上传请求")
    public static class AbortPartUploadDto implements Serializable {
        @Schema(description = "上传ID")
        private String uploadId;
    }

}
