package top.mddata.console.service.system.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import top.mddata.console.dto.system.CopyFilesDto;
import top.mddata.console.dto.system.FilePartDto;
import top.mddata.console.dto.system.FileUploadDto;
import top.mddata.console.dto.system.RelateFilesToBizDto;
import top.mddata.console.entity.system.File;
import top.mddata.console.entity.system.FilePart;
import top.mddata.console.enumeration.system.FileTypeEnum;
import top.mddata.console.mapper.system.FileMapper;
import top.mddata.console.service.system.FilePartService;
import top.mddata.console.service.system.FileService;
import top.mddata.console.service.system.convert.FileConvert;
import top.mddata.console.service.system.properties.FileProperties;
import top.mddata.console.vo.system.FileVo;

import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private final FileConvert fileConvert;
    private final FilePartService filePartService;

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
                .setObjectType(StrUtil.isEmpty(fileUploadDto.getObjectType()) ? TEMP_OBJECT_TYPE : fileUploadDto.getObjectType());

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
        FileVo fileVo = toFileVo(fileInfo);

        Long fileId = Long.valueOf(fileInfo.getId());
        Map<Long, FileVo> map = findUrlByIds(List.of(fileId));
        if (map.containsKey(fileId)) {
            fileVo.setUrl(map.get(fileId).getUrl());
        }
        return fileVo;
    }

    private FileVo toFileVo(FileInfo info) {
        FileVo detail = BeanUtil.copyProperties(
                info, FileVo.class, "id", "metadata", "userMetadata", "thMetadata", "thUserMetadata", "attr", "hashInfo");
        detail.setId(Long.valueOf(info.getId()));
        detail.setFileType(FileTypeEnum.getByExtension(info.getExt()).getCode());
        detail.setFileSize(info.getSize());
        // 这里手动获 元数据 并转成 json 字符串，方便存储在数据库中
        detail.setMetadata(JSON.toJSONString(info.getMetadata()));
        detail.setUserMetadata(JSON.toJSONString(info.getUserMetadata()));
        detail.setThMetadata(JSON.toJSONString(info.getThMetadata()));
        detail.setThUserMetadata(JSON.toJSONString(info.getThUserMetadata()));
        // 这里手动获 取附加属性字典 并转成 json 字符串，方便存储在数据库中
        detail.setAttr(JSON.toJSONString(info.getAttr()));
        // 这里手动获 哈希信息 并转成 json 字符串，方便存储在数据库中
        detail.setHashInfo(JSON.toJSONString(info.getHashInfo()));
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
            originalFiles.forEach(original -> {
                targetFiles.forEach(targetFile -> {
                    // 相对路径
                    String path = getDateFolder();

                    // 从数据库数据构造出 原文件
                    FileInfo originalFileInfo = fileConvert.toTarget(original);
                    originalFileInfo.setPlatform(original.getPlatform()).setBasePath(original.getBasePath()).setPath(original.getPath()).setFilename(original.getFilename());

                    // 指定新文件的业务参数 （其实x-file-storage这里做的不算友好，有优化空间）
                    originalFileInfo.setObjectId(String.valueOf(targetFile.getObjectId())).setObjectType(targetFile.getObjectType());

                    // 传递新文件必要参数，并执行复制
                    String newFilename = IdUtil.objectId() + (StrUtil.isEmpty(original.getExt()) ? StrUtil.EMPTY : "." + original.getExt());
                    fileStorageService.copy(originalFileInfo)
                            .setPath(path)
                            .setPlatform(original.getPlatform())
                            .setFilename(newFilename)  // 需要指定新名字
                            .setProgressListener((progressSize, allSize) ->
                                    log.info("文件复制进度：{} {}%", progressSize, progressSize * 100 / allSize))
                            .copy();
                });
            });

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

    @Override
    public FilePartDto.InitPartUploadResp initPartUpload(FilePartDto.InitPartUploadDto dto) {
        // 验证文件后缀
        if (!fileProperties.validSuffix(dto.getFileName())) {
            throw new BizException("文件后缀不支持");
        }
        if (StrUtil.containsAny(dto.getFileName(), "../", "./")) {
            throw new BizException("文件名不能含有特殊字符");
        }

        // 生成 uploadId
        String uploadId = UUID.randomUUID().toString().replace("-", "");

        // 计算分片大小和总分片数
        long chunkSize = fileProperties.getChunkSize() * 1024 * 1024L;
        int totalChunks = (int) Math.ceil((double) dto.getFileSize() / chunkSize);

        // 创建临时目录存储分片
        String tempDir = getTempDir(uploadId);
        FileUtil.mkdir(new java.io.File(tempDir));

        // 保存原始文件名到元数据文件
        java.io.File metaFile = new java.io.File(tempDir, ".filename");
        try {
            FileUtil.writeString(dto.getFileName(), metaFile, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("保存文件名元数据失败: {}", e.getMessage());
        }

        // 返回初始化信息
        FilePartDto.InitPartUploadResp resp = new FilePartDto.InitPartUploadResp();
        resp.setUploadId(uploadId);
        resp.setChunkSize(chunkSize);
        resp.setTotalChunks(totalChunks);
        return resp;
    }

    @Override
    public FilePartDto.UploadPartResp uploadPart(String uploadId, Integer partNumber, MultipartFile file) {
        if (StrUtil.isEmpty(uploadId)) {
            throw new BizException("上传ID不能为空");
        }
        if (partNumber == null || partNumber < 1) {
            throw new BizException("分片号必须大于0");
        }
        if (file.isEmpty()) {
            throw new BizException("分片文件不能为空");
        }

        // 保存分片到临时目录
        String tempDir = getTempDir(uploadId);
        String partFileName = String.format("%05d", partNumber);
        java.io.File partFile = new java.io.File(tempDir, partFileName);

        try {
            file.transferTo(partFile);
        } catch (Exception e) {
            throw new BizException("保存分片失败: " + e.getMessage());
        }

        // 保存分片信息（使用 partFile.length() 而不是 file.getSize()，因为 transferTo 后临时文件可能被删除）
        FilePart filePart = new FilePart();
        filePart.setUploadId(uploadId);
        filePart.setPartNumber(partNumber);
        filePart.setPartSize(partFile.length());
        filePart.setETag(String.valueOf(partNumber));
        filePartService.save(filePart);

        // 返回响应
        FilePartDto.UploadPartResp resp = new FilePartDto.UploadPartResp();
        resp.setPartNumber(partNumber);
        resp.setETag(String.valueOf(partNumber));
        return resp;
    }

    @Override
    public FilePartDto.UploadProgressResp getUploadProgress(String uploadId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(FilePart::getUploadId, uploadId)
                .orderBy(FilePart::getPartNumber, true);
        List<FilePart> parts = filePartService.list(queryWrapper);

        List<FilePartDto.UploadedPart> uploadedParts = parts.stream().map(p -> {
            FilePartDto.UploadedPart up = new FilePartDto.UploadedPart();
            up.setPartNumber(p.getPartNumber());
            up.setETag(p.getETag());
            return up;
        }).toList();

        FilePartDto.UploadProgressResp resp = new FilePartDto.UploadProgressResp();
        resp.setUploadId(uploadId);
        resp.setUploadedParts(uploadedParts);
        resp.setIsComplete(false);
        return resp;
    }

    @Override
    public FilePartDto.CompletePartUploadResp completePartUpload(FilePartDto.CompletePartUploadDto dto) {
        String uploadId = dto.getUploadId();
        if (StrUtil.isEmpty(uploadId)) {
            throw new BizException("上传ID不能为空");
        }

        // 查询所有分片
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(FilePart::getUploadId, uploadId)
                .orderBy(FilePart::getPartNumber, true);
        List<FilePart> parts = filePartService.list(queryWrapper);
        if (parts.isEmpty()) {
            throw new BizException("未找到上传分片");
        }

        // 合并分片
        String tempDir = getTempDir(uploadId);
        java.io.File[] partFiles = new java.io.File(tempDir).listFiles();
        if (partFiles == null || partFiles.length == 0) {
            throw new BizException("分片文件不存在");
        }

        // 按分片号排序
        List<java.io.File> sortedFiles = new ArrayList<>();
        for (int i = 1; i <= parts.size(); i++) {
            for (java.io.File f : partFiles) {
                if (f.getName().equals(String.format("%05d", i))) {
                    sortedFiles.add(f);
                    break;
                }
            }
        }

        // 创建合并后的临时文件
        String mergedFileName = "merged_" + uploadId;
        java.io.File mergedFile = new java.io.File(tempDir, mergedFileName);

        try (RandomAccessFile raf = new RandomAccessFile(mergedFile, "rw")) {
            for (java.io.File partFile : sortedFiles) {
                byte[] bytes = Files.readAllBytes(partFile.toPath());
                raf.write(bytes);
            }
        } catch (Exception e) {
            throw new BizException("合并分片失败: " + e.getMessage());
        }

        // 上传到文件存储服务
        FileInfo fileInfo = fileStorageService.of(mergedFile)
                .setPath(getDateFolder())
                .setObjectType(StrUtil.isEmpty(dto.getObjectType()) ? TEMP_OBJECT_TYPE : dto.getObjectType())
                .setOriginalFilename(getOriginalFilenameFromParts(parts))
                .upload();

        // 清理临时文件
        FileUtil.del(new java.io.File(tempDir));
        filePartService.remove(QueryWrapper.create().eq(FilePart::getUploadId, uploadId));

        // 构建响应
        FilePartDto.CompletePartUploadResp resp = new FilePartDto.CompletePartUploadResp();
        resp.setFileId(Long.valueOf(fileInfo.getId()));
        resp.setUrl(fileInfo.getUrl());
        return resp;
    }

    @Override
    public void abortPartUpload(FilePartDto.AbortPartUploadDto dto) {
        String uploadId = dto.getUploadId();
        if (StrUtil.isEmpty(uploadId)) {
            throw new BizException("上传ID不能为空");
        }

        // 删除临时目录
        String tempDir = getTempDir(uploadId);
        FileUtil.del(new java.io.File(tempDir));

        // 删除分片记录
        filePartService.remove(QueryWrapper.create().eq(FilePart::getUploadId, uploadId));
    }

    private String getTempDir(String uploadId) {
        String basePath = StrUtil.isNotEmpty(fileProperties.getTempStoragePath())
                ? fileProperties.getTempStoragePath()
                : System.getProperty("java.io.tmpdir");
        return Paths.get(basePath, "/file-parts/", uploadId).toString();
    }

    private String getOriginalFilenameFromParts(List<FilePart> parts) {
        if (parts == null || parts.isEmpty()) {
            return "unknownfile";
        }
        // 从临时目录读取文件名元数据文件
        String uploadId = parts.get(0).getUploadId();
        String tempDir = getTempDir(uploadId);
        java.io.File metaFile = new java.io.File(tempDir, ".filename");
        if (metaFile.exists()) {
            try {
                return FileUtil.readString(metaFile, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.warn("读取文件名元数据失败: {}", e.getMessage());
            }
        }
        return "unknownfile";
    }

    /**
     * 获取年月日 2020/09/01
     *
     * @return 日期文件夹
     */
    protected String getDateFolder() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(SLASH_DATE_FORMAT + "/"));
    }

    @Override
    public void download(HttpServletRequest request, HttpServletResponse response, List<Long> ids) throws Exception {
        List<File> files = listByIds(ids);
        if (files.size() == 1) {
            download(response, files.get(0));
        } else {
            String zipName = "批量下载_共" + files.size() + "条_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".zip";
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(zipName, StandardCharsets.UTF_8));
            OutputStream outputStream = response.getOutputStream();
            ZipOutputStream zipOut = new ZipOutputStream(outputStream);
            Set<String> usedNames = new HashSet<>();
            for (File file : files) {
                addFileToZip(file, zipOut, usedNames);
            }
            zipOut.finish();
            zipOut.flush();
        }
    }

    @Override
    public void download(HttpServletRequest request, HttpServletResponse response, Long id) throws Exception {
        File file = getById(id);
        if (file == null) {
            throw new BizException("文件不存在");
        }
        download(response, file);
    }

    private void download(HttpServletResponse response, File file) throws Exception {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setPlatform(file.getPlatform()).setBasePath(file.getBasePath()).setPath(file.getPath()).setFilename(file.getFilename());
        String name = sanitizeFileName(file.getOriginalFilename());
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(name, StandardCharsets.UTF_8));
        fileStorageService.download(fileInfo).outputStream(response.getOutputStream());
    }

    private void addFileToZip(File file, ZipOutputStream zipOut, Set<String> usedNames) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setPlatform(file.getPlatform()).setBasePath(file.getBasePath()).setPath(file.getPath()).setFilename(file.getFilename());
        try {
            String originalName = sanitizeFileName(file.getOriginalFilename());
            String entryName = getUniqueFileName(originalName, usedNames);
            usedNames.add(entryName);
            zipOut.putNextEntry(new ZipEntry(entryName));
            fileStorageService.download(fileInfo).outputStream(zipOut);
            zipOut.closeEntry();
        } catch (Exception e) {
            log.error("添加文件到压缩包失败: {}", file.getId(), e);
        }
    }

    private String getUniqueFileName(String fileName, Set<String> usedNames) {
        if (!usedNames.contains(fileName)) {
            return fileName;
        }
        int dotIndex = fileName.lastIndexOf('.');
        String name;
        String ext;
        if (dotIndex > 0) {
            name = fileName.substring(0, dotIndex);
            ext = fileName.substring(dotIndex);
        } else {
            name = fileName;
            ext = "";
        }
        int index = 1;
        String newName;
        do {
            newName = name + "(" + index + ")" + ext;
            index++;
        } while (usedNames.contains(newName));
        return newName;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unnamed";
        }
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
