package top.mddata.console.system.service;

import org.dromara.x.file.storage.core.upload.FilePartInfo;
import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.system.entity.FilePart;

/**
 * 文件分片
 * 仅在手动分片上传时使用 服务层。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
public interface FilePartService extends SuperService<FilePart> {
    /**
     * 保存分片信息
     * @param filePartInfo 分片信息
     */
    void saveFilePart(FilePartInfo filePartInfo);

    /**
     * 根据上传id 删除分片信息
     * @param uploadId 上传id
     */
    void deleteFilePartByUploadId(String uploadId);
}
