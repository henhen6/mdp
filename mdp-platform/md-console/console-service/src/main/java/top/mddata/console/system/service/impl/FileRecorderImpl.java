package top.mddata.console.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.hash.HashInfo;
import org.dromara.x.file.storage.core.recorder.FileRecorder;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.stereotype.Component;
import top.mddata.base.utils.JsonUtil;
import top.mddata.base.utils.StrPool;
import top.mddata.console.system.entity.File;
import top.mddata.console.system.enumeration.FileTypeEnum;
import top.mddata.console.system.mapper.FileMapper;
import top.mddata.console.system.service.FilePartService;

import java.util.List;

/**
 * 文件记录实现类
 *
 * @author Charles7c
 * @since 2023/12/24 22:31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileRecorderImpl implements FileRecorder {
    private final FileMapper sysFileMapper;
    private final FilePartService filePartService;

    @Override
    public boolean save(FileInfo info) {
        File detail = toFileDetail(info);

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
        return toFileInfo(file);
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
        File detail = toFileDetail(info);
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

    private File toFileDetail(FileInfo info) {
        File detail = BeanUtil.copyProperties(
                info, File.class, "id", "metadata", "userMetadata", "thMetadata", "thUserMetadata", "attr", "hashInfo");

        detail.setFileType(FileTypeEnum.getByExtension(info.getExt()).getCode());
        detail.setFileSize(info.getSize());
        // 这里手动获 元数据 并转成 json 字符串，方便存储在数据库中
        detail.setMetadata(JsonUtil.toJson(info.getMetadata()));
        detail.setUserMetadata(JsonUtil.toJson(info.getUserMetadata()));
        detail.setThMetadata(JsonUtil.toJson(info.getThMetadata()));
        detail.setThUserMetadata(JsonUtil.toJson(info.getThUserMetadata()));
        // 这里手动获 取附加属性字典 并转成 json 字符串，方便存储在数据库中
        detail.setAttr(JsonUtil.toJson(info.getAttr()));
        // 这里手动获 哈希信息 并转成 json 字符串，方便存储在数据库中
        detail.setHashInfo(JsonUtil.toJson(info.getHashInfo()));
        return detail;
    }


    /**
     * 将 FileDetail 转为 FileInfo
     */
    private FileInfo toFileInfo(File detail) {
        FileInfo info = BeanUtil.copyProperties(
                detail, FileInfo.class, "id", "metadata", "userMetadata", "thMetadata", "thUserMetadata", "attr", "hashInfo");
        info.setSize(detail.getFileSize());
        info.setId(String.valueOf(detail.getId()));

        // 这里手动获取数据库中的 json 字符串 并转成 元数据，方便使用
        info.setMetadata(JsonUtil.parse(detail.getMetadata(), new TypeReference<>() {
        }));
        info.setUserMetadata(JsonUtil.parse(detail.getUserMetadata(), new TypeReference<>() {
        }));
        info.setThMetadata(JsonUtil.parse(detail.getThMetadata(), new TypeReference<>() {
        }));
        info.setThUserMetadata(JsonUtil.parse(detail.getThUserMetadata(), new TypeReference<>() {
        }));
        // 这里手动获取数据库中的 json 字符串 并转成 附加属性字典，方便使用
        info.setAttr(JsonUtil.parse(detail.getAttr(), Dict.class));
        // 这里手动获取数据库中的 json 字符串 并转成 哈希信息，方便使用
        info.setHashInfo(JsonUtil.parse(detail.getHashInfo(), HashInfo.class));
        return info;
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