package top.mddata.console.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

/**
 * 文件分片
 * 仅在手动分片上传时使用 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 20:06:39
 */
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
    public R<FilePartDto.InitPartUploadResp> initUpload(@ModelAttribute FilePartDto.InitPartUploadDto dto) {
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
    public R<Boolean> abortUpload(@RequestBody FilePartDto.AbortPartUploadDto dto) {
        fileService.abortPartUpload(dto);
        return R.success();
    }
}
