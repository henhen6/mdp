package top.mddata.console.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.recorder.FileRecorder;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.stereotype.Component;
import top.mddata.base.utils.StrPool;
import top.mddata.console.system.convert.FileConvert;
import top.mddata.console.system.entity.File;
import top.mddata.console.system.mapper.FileMapper;
import top.mddata.console.system.service.FilePartService;

import java.util.List;

/**
 * 文件记录实现类
 *
 * @author henhen6
 * @since 2023/12/24 22:31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileRecorderImpl implements FileRecorder {
    private final FileMapper sysFileMapper;
    private final FilePartService filePartService;
    private final FileConvert fileConvert;

    @Override
    public boolean save(FileInfo info) {
        File detail = fileConvert.toSource(info);

        sysFileMapper.insert(detail);

        // 方便文件上传完成后获取文件信息
        info.setId(String.valueOf(detail.getId()));

//        if (!HttpUtil.isHttp(fileInfo.getUrl()) || !HttpUtil.isHttps(fileInfo.getUrl())) {
//            String prefix = storage.getUrlPrefix();
//            String url = URLUtil.normalize(prefix + fileInfo.getUrl(), false, true);
//            fileInfo.setUrl(url);
//            if (StrUtil.isNotBlank(fileInfo.getThUrl())) {
//                fileInfo.setThUrl(URLUtil.normalize(prefix + fileInfo.getThUrl(), false, true));
//            }
//        }
        return true;
    }

    @Override
    public FileInfo getByUrl(String url) {
        File file = this.getFileByUrl(url);
        if (file == null) {
            return null;
        }
        return fileConvert.toTarget(file);
    }


    @Override
    public boolean delete(String url) {
        File file = this.getFileByUrl(url);
        if (file == null) {
            return false;
        }
        return sysFileMapper.deleteById(file.getId()) > 0;

    }

    @Override
    public void update(FileInfo info) {
        File detail = fileConvert.toSource(info);
        detail.setId(Long.valueOf(info.getId()));
        sysFileMapper.update(detail);
    }

    /**
     * 保存文件分片信息
     * @param filePartInfo 文件分片信息
     */
    @Override
    public void saveFilePart(FilePartInfo filePartInfo) {
        filePartService.saveFilePart(filePartInfo);
    }

    /**
     * 删除文件分片信息
     */
    @Override
    public void deleteFilePartByUploadId(String uploadId) {
        filePartService.deleteFilePartByUploadId(uploadId);
    }

    /**
     * 根据 URL 查询文件
     *
     * @param url URL
     * @return 文件信息
     */
    private File getFileByUrl(String url) {
        QueryWrapper wrapper = QueryWrapper.create().eq(File::getFilename, StrUtil.subAfter(url, StrPool.SLASH, true));

        if (!HttpUtil.isHttp(url) || !HttpUtil.isHttps(url)) {
            wrapper.eq(File::getPath, StrUtil.prependIfMissing(url, StrPool.SLASH));
            return sysFileMapper.selectOneByQuery(wrapper);
        }
        List<File> list = sysFileMapper.selectListByQuery(wrapper);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }
}