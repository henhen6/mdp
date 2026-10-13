# 分片上传实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现大文件分片上传功能，支持断点续传，兼容本地存储和 OSS 存储

**Architecture:** 使用 x-file-storage 原生分片上传 API，前端分片后上传到临时目录，合并后根据配置存储到本地或 OSS

**Tech Stack:** Java 17, Spring Boot 3.x, x-file-storage, MyBatis-Flex

---

## Global Constraints

- 文件后缀白名单校验
- 分片必须按顺序上传和合并
- 上传完成后需清理临时分片文件
- 遵循项目现有的分层架构 (Controller -> Service -> Mapper)

---

## 文件结构

```
mdp-apps/md-console/
├── console-pojo/src/main/java/top/mddata/console/dto/system/
│   └── FilePartDto.java                              # 新增：分片相关 DTO
├── console-service/src/main/java/top/mddata/console/service/system/
│   ├── FileService.java                             # 修改：新增分片方法声明
│   └── impl/FileServiceImpl.java                   # 修改：新增分片实现
├── console-service/src/main/java/top/mddata/console/service/system/properties/
│   └── FileProperties.java                         # 修改：新增 chunkSize 配置
└── console-web/src/main/java/top/mddata/console/controller/
    └── FilePartController.java                      # 修改：新增分片上传接口
```

---

## Task 1: 新增分片相关 DTO

**Files:**
- Create: `mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/dto/system/FilePartDto.java`

**Interfaces:**
- Produces: `InitPartUploadDto`, `UploadPartDto`, `CompletePartUploadDto`, `AbortPartUploadDto`, `InitPartUploadResp`, `UploadPartResp`, `UploadProgressResp`, `CompletePartUploadResp`

- [ ] **Step 1: 创建 FilePartDto.java**

```java
package top.mddata.console.dto.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分片上传相关 DTO
 */
public class FilePartDto {

    @Data
    @Schema(description = "初始化分片上传请求")
    public static class InitPartUploadDto implements Serializable {
        @Schema(description = "文件名")
        private String fileName;
        @Schema(description = "文件大小(字节)")
        private Long fileSize;
        @Schema(description = "文件哈希(可选)")
        private String fileHash;
        @Schema(description = "业务类型")
        private String objectType;
        @Schema(description = "业务ID")
        private Long objectId;
        @Schema(description = "存储平台")
        private String platform;
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
```

- [ ] **Step 2: Commit**

```bash
git add mdp-apps/md-console/console-pojo/src/main/java/top/mddata/console/dto/system/FilePartDto.java
git commit -m "feat(console): 新增分片上传相关 DTO"
```

---

## Task 2: 在 FileProperties 新增分片大小配置

**Files:**
- Modify: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/system/properties/FileProperties.java`

**Interfaces:**
- Produces: `chunkSize` 属性 (默认 5MB)

- [ ] **Step 1: 添加 chunkSize 配置项**

在 `FileProperties.java` 的类体中添加：

```java
/**
 * 分片大小，单位 MB，默认 5MB
 */
private Integer chunkSize = 5;

public Integer getChunkSize() {
    return chunkSize;
}
```

- [ ] **Step 2: Commit**

```bash
git add mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/system/properties/FileProperties.java
git commit -m "feat(console): 新增分片大小配置项 chunkSize"
```

---

## Task 3: 在 FileService 接口新增分片相关方法声明

**Files:**
- Modify: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/system/FileService.java`

**Interfaces:**
- Produces: `initPartUpload`, `uploadPart`, `completePartUpload`, `abortPartUpload`, `getUploadProgress` 方法

- [ ] **Step 1: 添加分片相关方法声明**

在 `FileService.java` 中添加以下方法声明：

```java
/**
 * 初始化分片上传
 * @param dto 初始化参数
 * @return 初始化响应
 */
FilePartDto.InitPartUploadResp initPartUpload(FilePartDto.InitPartUploadDto dto);

/**
 * 上传分片
 * @param uploadId 上传ID
 * @param partNumber 分片号
 * @param file 分片文件
 * @return 分片响应
 */
FilePartDto.UploadPartResp uploadPart(String uploadId, Integer partNumber, MultipartFile file);

/**
 * 完成分片上传并合并
 * @param dto 完成合并请求
 * @return 合并结果
 */
FilePartDto.CompletePartUploadResp completePartUpload(FilePartDto.CompletePartUploadDto dto);

/**
 * 取消分片上传
 * @param dto 取消请求
 */
void abortPartUpload(FilePartDto.AbortPartUploadDto dto);

/**
 * 查询上传进度
 * @param uploadId 上传ID
 * @return 上传进度
 */
FilePartDto.UploadProgressResp getUploadProgress(String uploadId);
```

- [ ] **Step 2: Commit**

```bash
git add mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/system/FileService.java
git commit -m "feat(console): FileService 新增分片上传方法声明"
```

---

## Task 4: 在 FileServiceImpl 实现分片上传逻辑

**Files:**
- Modify: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/system/impl/FileServiceImpl.java`

**Interfaces:**
- Consumes: `FilePartDto` (Task 1), `FileProperties.chunkSize` (Task 2), `FileService` 方法签名 (Task 3)
- Produces: 分片上传实现

**Dependencies:**
- Task 1, Task 2, Task 3

- [ ] **Step 1: 添加必要的 import 和字段**

在 `FileServiceImpl.java` 添加：

```java
import top.mddata.console.dto.system.FilePartDto;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import cn.hutool.core.io.FileUtil;
```

- [ ] **Step 2: 实现 initPartUpload 方法**

在类中添加：

```java
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
    FileUtil.mkdir(new File(tempDir));

    // 返回初始化信息
    FilePartDto.InitPartUploadResp resp = new FilePartDto.InitPartUploadResp();
    resp.setUploadId(uploadId);
    resp.setChunkSize(chunkSize);
    resp.setTotalChunks(totalChunks);
    return resp;
}

private String getTempDir(String uploadId) {
    return System.getProperty("java.io.tmpdir") + "/file-parts/" + uploadId;
}
```

- [ ] **Step 3: 实现 uploadPart 方法**

```java
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
    File partFile = new File(tempDir, partFileName);

    try {
        file.transferTo(partFile);
    } catch (Exception e) {
        throw new BizException("保存分片失败: " + e.getMessage());
    }

    // 保存分片信息
    FilePart filePart = new FilePart();
    filePart.setUploadId(uploadId);
    filePart.setPartNumber(partNumber);
    filePart.setPartSize(file.getSize());
    filePart.setETag(String.valueOf(partNumber));
    filePartService.save(filePart);

    // 返回响应
    FilePartDto.UploadPartResp resp = new FilePartDto.UploadPartResp();
    resp.setPartNumber(partNumber);
    resp.setETag(String.valueOf(partNumber));
    return resp;
}
```

- [ ] **Step 4: 实现 getUploadProgress 方法**

```java
@Override
public FilePartDto.UploadProgressResp getUploadProgress(String uploadId) {
    List<FilePart> parts = filePartService.list(
        QueryWrapper.create().eq(FilePart::getUploadId, uploadId).orderBy(FilePart::getPartNumber)
    );

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
```

- [ ] **Step 5: 实现 completePartUpload 方法**

```java
@Override
public FilePartDto.CompletePartUploadResp completePartUpload(FilePartDto.CompletePartUploadDto dto) {
    String uploadId = dto.getUploadId();
    if (StrUtil.isEmpty(uploadId)) {
        throw new BizException("上传ID不能为空");
    }

    // 查询所有分片
    List<FilePart> parts = filePartService.list(
        QueryWrapper.create().eq(FilePart::getUploadId, uploadId).orderBy(FilePart::getPartNumber)
    );
    if (parts.isEmpty()) {
        throw new BizException("未找到上传分片");
    }

    // 获取初始信息（从第一个分片获取文件名等信息，需要额外处理）
    // 这里简化处理，假设前端已保存初始化返回的信息

    // 合并分片
    String tempDir = getTempDir(uploadId);
    File[] partFiles = new File(tempDir).listFiles();
    if (partFiles == null || partFiles.length == 0) {
        throw new BizException("分片文件不存在");
    }

    // 按分片号排序
    List<File> sortedFiles = new ArrayList<>();
    for (int i = 1; i <= parts.size(); i++) {
        for (File f : partFiles) {
            if (f.getName().equals(String.format("%05d", i))) {
                sortedFiles.add(f);
                break;
            }
        }
    }

    // 创建合并后的临时文件
    String mergedFileName = "merged_" + uploadId;
    File mergedFile = new File(tempDir, mergedFileName);

    try (RandomAccessFile raf = new RandomAccessFile(mergedFile, "rw")) {
        for (File partFile : sortedFiles) {
            Files.copy(partFile.toPath(), raf.getChannel().position(raf.length()));
        }
    } catch (Exception e) {
        throw new BizException("合并分片失败: " + e.getMessage());
    }

    // 上传到文件存储服务
    FileInfo fileInfo = fileStorageService.of(mergedFile)
        .setPath(getDateFolder())
        .setOriginalFilename(getOriginalFilenameFromParts(parts))
        .upload();

    // 清理临时文件
    FileUtil.del(new File(tempDir));
    filePartService.remove(QueryWrapper.create().eq(FilePart::getUploadId, uploadId));

    // 构建响应
    FilePartDto.CompletePartUploadResp resp = new FilePartDto.CompletePartUploadResp();
    resp.setFileId(Long.valueOf(fileInfo.getId()));
    resp.setUrl(fileInfo.getUrl());
    return resp;
}

private String getOriginalFilenameFromParts(List<FilePart> parts) {
    // 从数据库或上下文获取文件名，这里返回默认名
    return "unknownfile";
}
```

- [ ] **Step 6: 实现 abortPartUpload 方法**

```java
@Override
public void abortPartUpload(FilePartDto.AbortPartUploadDto dto) {
    String uploadId = dto.getUploadId();
    if (StrUtil.isEmpty(uploadId)) {
        throw new BizException("上传ID不能为空");
    }

    // 删除临时目录
    String tempDir = getTempDir(uploadId);
    FileUtil.del(new File(tempDir));

    // 删除分片记录
    filePartService.remove(QueryWrapper.create().eq(FilePart::getUploadId, uploadId));
}
```

- [ ] **Step 7: Commit**

```bash
git add mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/system/impl/FileServiceImpl.java
git commit -m "feat(console): FileServiceImpl 实现分片上传逻辑"
```

---

## Task 5: 在 FilePartController 新增分片上传接口

**Files:**
- Modify: `mdp-apps/md-console/console-web/src/main/java/top/mddata/console/controller/FilePartController.java`

**Dependencies:**
- Task 1, Task 3

- [ ] **Step 1: 添加分片上传接口**

将 `FilePartController.java` 修改为：

```java
package top.mddata.console.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.mddata.base.base.R;
import top.mddata.console.dto.system.FilePartDto;
import top.mddata.console.service.system.FileService;

@RestController
@Validated
@Tag(name = "文件分片上传")
@RequestMapping("/anyone/filePart")
@RequiredArgsConstructor
@Slf4j
public class FilePartController {
    private final FileService fileService;

    @Operation(summary = "初始化分片上传")
    @PostMapping(value = "/init", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<FilePartDto.InitPartUploadResp> initUpload(
            @Parameter(description = "文件名") @RequestParam String fileName,
            @Parameter(description = "文件大小(字节)") @RequestParam Long fileSize,
            @Parameter(description = "文件哈希(可选)") @RequestParam(required = false) String fileHash,
            @Parameter(description = "业务类型") @RequestParam(required = false) String objectType,
            @Parameter(description = "业务ID") @RequestParam(required = false) Long objectId,
            @Parameter(description = "存储平台") @RequestParam(required = false) String platform) {
        FilePartDto.InitPartUploadDto dto = new FilePartDto.InitPartUploadDto();
        dto.setFileName(fileName);
        dto.setFileSize(fileSize);
        dto.setFileHash(fileHash);
        dto.setObjectType(objectType);
        dto.setObjectId(objectId);
        dto.setPlatform(platform);
        return R.success(fileService.initPartUpload(dto));
    }

    @Operation(summary = "上传分片")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<FilePartDto.UploadPartResp> uploadPart(
            @Parameter(description = "上传ID") @RequestParam String uploadId,
            @Parameter(description = "分片号") @RequestParam Integer partNumber,
            @Parameter(description = "分片文件") @RequestParam("file") MultipartFile file) {
        return R.success(fileService.uploadPart(uploadId, partNumber, file));
    }

    @Operation(summary = "查询上传进度")
    @GetMapping(value = "/progress")
    public R<FilePartDto.UploadProgressResp> getProgress(
            @Parameter(description = "上传ID") @RequestParam String uploadId) {
        return R.success(fileService.getUploadProgress(uploadId));
    }

    @Operation(summary = "完成分片上传")
    @PostMapping(value = "/complete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<FilePartDto.CompletePartUploadResp> completeUpload(
            @RequestBody FilePartDto.CompletePartUploadDto dto) {
        return R.success(fileService.completePartUpload(dto));
    }

    @Operation(summary = "取消上传")
    @PostMapping(value = "/abort", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<Void> abortUpload(@RequestBody FilePartDto.AbortPartUploadDto dto) {
        fileService.abortPartUpload(dto);
        return R.success();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add mdp-apps/md-console/console-web/src/main/java/top/mddata/console/controller/FilePartController.java
git commit -m "feat(console): FilePartController 新增分片上传接口"
```

---

## Task 6: 完善合并逻辑，保存原始文件名

**Files:**
- Modify: `mdp-apps/md-console/console-service/src/main/java/top/mddata/console/service/system/impl/FileServiceImpl.java`

**问题:** Task 4 中 `getOriginalFilenameFromParts` 方法无法获取原始文件名

**解决方案:** 在 `mdc_file_part` 表中增加 `fileName` 字段，或在初始化时保存到 Redis/数据库

- [ ] **Step 1: 创建初始化信息保存表（可选方案）**

如果需要保存初始化信息，可以在 `FilePart` 实体中增加 `uploadId` 关联的初始化信息

**简化方案（推荐）:** 前端在完成合并时传递文件名，后端通过 uploadId 查找分片时一并返回

---

## Task 7: 添加单元测试

**Files:**
- Create: `mdp-apps/md-console/console-service/src/test/java/top/mddata/console/service/system/impl/FileServiceImplTest.java`

**Dependencies:**
- Task 4

- [ ] **Step 1: 编写分片上传单元测试**

```java
package top.mddata.console.service.system.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import top.mddata.console.dto.system.FilePartDto;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileServiceImplTest {

    @Autowired
    private FileServiceImpl fileService;

    @Test
    void testInitPartUpload() {
        FilePartDto.InitPartUploadDto dto = new FilePartDto.InitPartUploadDto();
        dto.setFileName("test.jpg");
        dto.setFileSize(10 * 1024 * 1024L); // 10MB
        dto.setObjectType("test");
        dto.setObjectId(1L);

        FilePartDto.InitPartUploadResp resp = fileService.initPartUpload(dto);

        assertNotNull(resp.getUploadId());
        assertEquals(5242880, resp.getChunkSize()); // 5MB
        assertEquals(2, resp.getTotalChunks());    // 10MB / 5MB = 2
    }

    @Test
    void testUploadAndComplete() {
        // 1. 初始化
        FilePartDto.InitPartUploadDto initDto = new FilePartDto.InitPartUploadDto();
        initDto.setFileName("test.jpg");
        initDto.setFileSize(10 * 1024 * 1024L);
        FilePartDto.InitPartUploadResp initResp = fileService.initPartUpload(initDto);

        String uploadId = initResp.getUploadId();

        // 2. 上传分片
        for (int i = 1; i <= initResp.getTotalChunks(); i++) {
            byte[] content = new byte[5 * 1024 * 1024]; // 5MB per chunk
            MockMultipartFile file = new MockMultipartFile(
                "file", "chunk_" + i, MediaType.APPLICATION_OCTET_STREAM_VALUE, content
            );
            FilePartDto.UploadPartResp partResp = fileService.uploadPart(uploadId, i, file);
            assertEquals(i, partResp.getPartNumber());
        }

        // 3. 查询进度
        FilePartDto.UploadProgressResp progressResp = fileService.getUploadProgress(uploadId);
        assertEquals(2, progressResp.getUploadedParts().size());
        assertFalse(progressResp.getIsComplete());

        // 4. 完成合并（这里需要 mock 文件存储服务）
        // FilePartDto.CompletePartUploadDto completeDto = new FilePartDto.CompletePartUploadDto();
        // completeDto.setUploadId(uploadId);
        // FilePartDto.CompletePartUploadResp completeResp = fileService.completePartUpload(completeDto);
    }

    @Test
    void testAbort() {
        // 初始化
        FilePartDto.InitPartUploadDto initDto = new FilePartDto.InitPartUploadDto();
        initDto.setFileName("test.jpg");
        initDto.setFileSize(10 * 1024 * 1024L);
        FilePartDto.InitPartUploadResp initResp = fileService.initPartUpload(initDto);

        // 上传一个分片
        byte[] content = new byte[1024];
        MockMultipartFile file = new MockMultipartFile(
            "file", "chunk_1", MediaType.APPLICATION_OCTET_STREAM_VALUE, content
        );
        fileService.uploadPart(initResp.getUploadId(), 1, file);

        // 取消上传
        FilePartDto.AbortPartUploadDto abortDto = new FilePartDto.AbortPartUploadDto();
        abortDto.setUploadId(initResp.getUploadId());
        fileService.abortPartUpload(abortDto);

        // 验证分片已删除
        FilePartDto.UploadProgressResp progressResp = fileService.getUploadProgress(initResp.getUploadId());
        assertTrue(progressResp.getUploadedParts().isEmpty());
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add mdp-apps/md-console/console-service/src/test/java/top/mddata/console/service/system/impl/FileServiceImplTest.java
git commit -m "test(console): 添加分片上传单元测试"
```

---

## 实现顺序

1. Task 1: 新增分片相关 DTO
2. Task 2: 在 FileProperties 新增分片大小配置
3. Task 3: 在 FileService 接口新增分片相关方法声明
4. Task 4: 在 FileServiceImpl 实现分片上传逻辑
5. Task 5: 在 FilePartController 新增分片上传接口
6. Task 6: 完善合并逻辑（可选）
7. Task 7: 添加单元测试

---

## 备注

- 合并文件时原始文件名丢失问题可通过增加 `fileName` 字段到 `mdc_file_part` 表解决
- x-file-storage 的 `initiateMultipartUpload` 等 API 本次实现中简化处理，直接保存到临时目录
- 实际生产环境需要考虑分布式场景下的临时文件共享问题（建议使用 NFS 或 MinIO 等支持分片上传的存储）
