package top.mddata.console.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.ProgressListener;
import org.dromara.x.file.storage.core.upload.UploadPretreatment;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.mddata.base.exception.BizException;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.CollHelper;
import top.mddata.base.utils.JsonUtil;
import top.mddata.console.system.dto.CopyFilesDto;
import top.mddata.console.system.dto.FileUploadDto;
import top.mddata.console.system.dto.RelateFilesToBizDto;
import top.mddata.console.system.entity.File;
import top.mddata.console.system.enumeration.FileTypeEnum;
import top.mddata.console.system.mapper.FileMapper;
import top.mddata.console.system.properties.FileProperties;
import top.mddata.console.system.service.FileService;
import top.mddata.console.system.vo.FileVo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static top.mddata.base.utils.DateUtils.SLASH_DATE_FORMAT;
import static top.mddata.common.constant.FileObjectType.TEMP_OBJECT_TYPE;

/**
 * 文件 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(FileProperties.class)
public class FileServiceImpl extends SuperServiceImpl<FileMapper, File> implements FileService {
    private final FileStorageService fileStorageService;
    private final FileProperties fileProperties;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FileVo upload(MultipartFile file, FileUploadDto fileUploadDto) {
        // 忽略路径字段,只处理文件类型
        if (file.isEmpty()) {
            throw new BizException("请上传有效文件");
        }
        if (!fileProperties.validSuffix(file.getOriginalFilename())) {
            throw new BizException("文件后缀不支持");
        }
        if (StrUtil.containsAny(file.getOriginalFilename(), "../", "./")) {
            throw new BizException("文件名不能含有特殊字符");
        }


        // 相对路径
        String path = getDateFolder();

        UploadPretreatment uploadPretreatment = fileStorageService.of(file)
                .setPlatform(StrUtil.isNotEmpty(fileUploadDto.getPlatform()), fileUploadDto.getPlatform())
                .setHashCalculatorSha256(true)
                .setPath(path)
                .setObjectType(TEMP_OBJECT_TYPE);

        uploadPretreatment.setProgressMonitor(new ProgressListener() {
            @Override
            public void start() {
                log.info("开始上传");
            }

            @Override
            public void progress(long progressSize, Long allSize) {
                log.info("已上传 [{}]，总大小 [{}]", progressSize, allSize);
            }

            @Override
            public void finish() {
                log.info("上传结束");
            }
        });

        String extName = FileNameUtil.extName(file.getOriginalFilename());
        // 图片文件生成缩略图
        if (FileTypeEnum.IMAGE.getExtensions().contains(extName) && fileUploadDto.getThumbnail() != null && fileUploadDto.getThumbnail()) {
            uploadPretreatment.setIgnoreThumbnailException(true, true);
            uploadPretreatment.thumbnail(img -> img.size(100, 100));
        }

        FileInfo fileInfo = uploadPretreatment.upload();
        return toFileVo(fileInfo);
    }

    private FileVo toFileVo(FileInfo info) {
        FileVo detail = BeanUtil.copyProperties(
                info, FileVo.class, "id", "metadata", "userMetadata", "thMetadata", "thUserMetadata", "attr", "hashInfo");
        detail.setId(Long.valueOf(info.getId()));
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean copyFile(CopyFilesDto copyFilesDto) {
        String objectType = copyFilesDto.getObjectType();
        Long objectId = copyFilesDto.getObjectId();
        List<CopyFilesDto> targetFiles = copyFilesDto.getTargetFiles();
        ArgumentAssert.notNull(objectId, "原业务对象业务id不能为空");
        ArgumentAssert.notEmpty(objectType, "原业务对象业务类型不能为空");
        ArgumentAssert.notEmpty(targetFiles, "新业务对象不能为空");
        List<File> originalFiles = list(QueryWrapper.create().eq(File::getObjectType, objectType).eq(File::getObjectId, objectId));
        if (originalFiles.isEmpty()) {
            log.info("未找到【{}--{}】对应的附件", copyFilesDto.getObjectType(), copyFilesDto.getObjectId());
            return false;
        }

        try {
            List<File> targetFileList = new ArrayList<>();
            originalFiles.forEach(original -> {
                targetFiles.forEach(targetFile -> {
                    CopyOptions copyOptions = CopyOptions.create().setIgnoreProperties(File::getId, File::getCreatedAt, File::getCreatedBy, File::getUpdatedAt, File::getUpdatedBy);
                    File newFile = BeanUtil.toBean(original, File.class, copyOptions);

                    // 相对路径
                    String path = getDateFolder();

                    // 构造原始文件
                    FileInfo originalFileInfo = new FileInfo();
                    originalFileInfo.setPlatform(original.getPlatform()).setBasePath(original.getBasePath()).setPath(original.getPath()).setFilename(original.getFilename());

                    // 构造新文件，并执行复制
                    FileInfo newFileInfo = fileStorageService.copy(originalFileInfo)
                            .setPath(path)
                            .setPlatform(original.getPlatform())
//                            .setFilename(targetFile.getFilename())
                            .setProgressListener((progressSize, allSize) ->
                                    log.info("文件复制进度：{} {}%", progressSize, progressSize * 100 / allSize))
                            .copy();

                    newFile.setUrl(newFileInfo.getUrl());
                    newFile.setFilename(newFileInfo.getFilename());
                    newFile.setPath(newFileInfo.getPath());

                    // 重置分片上传相关字段（复制后的附件为完整文件，无需分片状态）
                    newFile.setUploadId(null);
                    newFile.setUploadStatus(null);

                    // 设置新业务标识
                    newFile.setObjectType(targetFile.getObjectType());
                    newFile.setObjectId(targetFile.getObjectId());
                    targetFileList.add(newFile);
                });
            });

            saveBatch(targetFileList);
            return true;
        } catch (Exception e) {
            log.error("文件复制失败", e);
            return false;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void relateFilesToBiz(RelateFilesToBizDto relateFilesToBizDto) {
        String objectType = relateFilesToBizDto.getObjectType();
        Long objectId = relateFilesToBizDto.getObjectId();
        List<Long> keepFileIds = relateFilesToBizDto.getKeepFileIds() == null ? Collections.emptyList() : relateFilesToBizDto.getKeepFileIds();
        // 1. 查询该业务原有的所有文件
        List<File> oldFiles = list(QueryWrapper.create().eq(File::getObjectType, objectType).eq(File::getObjectId, objectId));
        List<Long> oldFileIds = oldFiles.stream().map(File::getId).toList();

        // 2. 处理需要删除的文件（原文件不在保留列表中）
        List<Long> deleteIds = oldFileIds.stream().filter(id -> !keepFileIds.contains(id)).toList();
        if (!deleteIds.isEmpty()) {
            removeByIds(deleteIds);
        }

        // 3. 处理需要新增的文件（保留列表中不在原文件列表的文件）
        List<Long> addIds = keepFileIds.stream().filter(id -> !oldFileIds.contains(id)).toList();
        if (!addIds.isEmpty()) {
            List<File> addFiles = listByIds(addIds);
            addFiles.forEach(file -> {
                file.setObjectType(objectType);
                file.setObjectId(objectId);
            });
            updateBatch(addFiles);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, FileVo> findUrlByIds(List<Long> ids) {
        List<File> sysFiles = listByIds(ids);
        Map<Long, FileVo> map = CollHelper.buildMap(sysFiles, File::getId, item -> BeanUtil.toBean(item, FileVo.class));
        map.forEach((id, file) -> {
            if (file == null) {
                return;
            }
            FileInfo fileInfo = new FileInfo();
            fileInfo.setPlatform(file.getPlatform()).setPath(file.getPath()).setFilename(file.getFilename());
            // 有效期1小时
            Date expiration = DateUtil.offsetHour(new Date(), 1);
            file.setUrl(fileStorageService.generatePresignedUrl(fileInfo, expiration));
        });
        return map;
    }

    @Override

    @Transactional(readOnly = true)
    public Map<Long, FileVo> findUrlByObject(String objectType, Long objectId) {
        List<File> sysFiles = list(QueryWrapper.create().eq(File::getObjectType, objectType).eq(File::getObjectId, objectId));
        Map<Long, FileVo> map = CollHelper.buildMap(sysFiles, File::getId, item -> BeanUtil.toBean(item, FileVo.class));
        map.forEach((id, file) -> {
            if (file == null) {
                return;
            }
            FileInfo fileInfo = new FileInfo();
            fileInfo.setPlatform(file.getPlatform()).setPath(file.getPath()).setFilename(file.getFilename());
            // 有效期1小时
            Date expiration = DateUtil.offsetHour(new Date(), 1);
            file.setUrl(fileStorageService.generatePresignedUrl(fileInfo, expiration));
        });
        return map;
    }


    /**
     * 删除临时文件（未关联业务的文件，可定时任务调用）
     */
    public void cleanTempFiles() {
        // 删除30天前的 未关联文件
        List<File> tempFiles = list(QueryWrapper.create().eq(File::getObjectType, TEMP_OBJECT_TYPE).isNull(File::getObjectId)
                .le(File::getCreatedAt, LocalDateTime.now().minusDays(30)));

        List<Long> deleteIds = new ArrayList<>();
        tempFiles.forEach(file -> {
            if (fileProperties.getDelFile()) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setObjectType(file.getObjectType());
                fileInfo.setObjectId(file.getObjectId() == null ? null : String.valueOf(file.getObjectId()));
                fileInfo.setPlatform(file.getPlatform());
                fileStorageService.delete(fileInfo);
            }
            // 标记删除
            deleteIds.add(file.getId());
        });
        removeByIds(deleteIds);
    }

    /**
     * 获取年月日 2020/09/01
     *
     * @return 日期文件夹
     */
    protected String getDateFolder() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(SLASH_DATE_FORMAT + "/"));
    }
}
