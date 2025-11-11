package top.mddata.common.file;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageProperties;
import org.dromara.x.file.storage.core.platform.LocalPlusFileStorage;
import org.dromara.x.file.storage.core.presigned.GeneratePresignedUrlPretreatment;
import org.dromara.x.file.storage.core.presigned.GeneratePresignedUrlResult;

/**
 * 本地文件存储升级版 增强
 */
@Getter
@Setter
@NoArgsConstructor
public class LocalPlusExtFileStorage extends LocalPlusFileStorage {
    public LocalPlusExtFileStorage(FileStorageProperties.LocalPlusConfig config) {
        super(config);
    }

    @Override
    public GeneratePresignedUrlResult generatePresignedUrl(GeneratePresignedUrlPretreatment pre) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setPlatform(pre.getPlatform()).setPath(pre.getPath()).setBasePath(this.getBasePath()).setFilename(pre.getFilename());
        String newFileKey = getFileKey(fileInfo);
        GeneratePresignedUrlResult result = new GeneratePresignedUrlResult(this.getPlatform(), this.getBasePath(), pre);
        result.setUrl(this.getDomain() + newFileKey);
        return result;
    }
}
