package top.mddata.console.system.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.JsonUtil;
import top.mddata.console.system.entity.FilePart;
import top.mddata.console.system.mapper.FilePartMapper;
import top.mddata.console.system.service.FilePartService;

/**
 * 文件分片
 * 仅在手动分片上传时使用 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FilePartServiceImpl extends SuperServiceImpl<FilePartMapper, FilePart> implements FilePartService {
    @Override
    public void saveFilePart(FilePartInfo info) {
        FilePart detail = toFilePartDetail(info);
        if (save(detail)) {
            info.setId(String.valueOf(detail.getId()));
        }
    }

    @Override
    public void deleteFilePartByUploadId(String uploadId) {
        remove(QueryWrapper.create().eq(FilePart::getUploadId, uploadId));
    }


    /**
     * 将 FilePartInfo 转成 FilePartDetail
     * @param info 文件分片信息
     */
    private FilePart toFilePartDetail(FilePartInfo info) {
        FilePart detail = new FilePart();
        detail.setPlatform(info.getPlatform());
        detail.setUploadId(info.getUploadId());
        detail.setETag(info.getETag());
        detail.setPartNumber(info.getPartNumber());
        detail.setPartSize(info.getPartSize());
        detail.setHashInfo(JsonUtil.toJson(info.getHashInfo()));
        return detail;
    }
}
