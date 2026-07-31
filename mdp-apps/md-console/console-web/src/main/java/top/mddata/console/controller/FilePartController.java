package top.mddata.console.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.mddata.base.base.R;
import top.mddata.console.dto.system.FilePartDto;
import top.mddata.console.service.system.FileService;

/**
 * 文件分片上传控制器
 *
 * <h2>接口调用流程（前端配合指南）</h2>
 *
 * <p>分片上传适用于大文件场景，将文件拆分为多个小块分别上传，提高传输稳定性和可靠性。</p>
 *
 * <ol>
 *   <li><b>Step 1 - 初始化</b>：前端调用 {@link #initUpload(FilePartDto.InitPartUploadDto)} 初始化上传
 *       <ul>
 *         <li>入参：文件名、文件大小(字节)、可选的文件哈希</li>
 *         <li>返回：uploadId（后续接口必须携带）、chunkSize（分片大小）、totalChunks（总分片数）</li>
 *       </ul>
 *   </li>
 *   <li><b>Step 2 - 上传分片</b>：前端按照分片号顺序调用 {@link #uploadPart(String, Integer, MultipartFile)} 上传每个分片
 *       <ul>
 *         <li>入参：uploadId（初始化返回）、partNumber（分片号，从1开始）、分片文件</li>
 *         <li>返回：分片号、ETag（用于校验）</li>
 *         <li>建议：分片号必须连续，可并行上传不同分片提升速度</li>
 *         <li>建议：每个分片大小应等于或略小于返回的 chunkSize</li>
 *       </ul>
 *   </li>
 *   <li><b>Step 3 - 查询进度（可选）</b>：前端可随时调用 {@link #getProgress(String)} 查询上传进度
 *       <ul>
 *         <li>入参：uploadId</li>
 *         <li>返回：已上传的分片列表、是否完成</li>
 *         <li>用途：断点续传、显示上传进度条</li>
 *       </ul>
 *   </li>
 *   <li><b>Step 4 - 完成上传</b>：所有分片上传成功后，调用 {@link #completeUpload(FilePartDto.CompletePartUploadDto)} 完成合并
 *       <ul>
 *         <li>入参：uploadId、objectType（业务对象类型，可为空）</li>
 *         <li>返回：fileId（文件ID）、url（访问地址）</li>
 *         <li>注意：必须等所有分片上传完成才能调用</li>
 *       </ul>
 *   </li>
 *   <li><b>异常处理 - 取消上传</b>：上传失败或用户取消时，调用 {@link #abortUpload(FilePartDto.AbortPartUploadDto)} 清理资源
 *       <ul>
 *         <li>入参：uploadId</li>
 *         <li>效果：删除已上传的分片文件和数据记录</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <h2>前端示例代码结构</h2>
 * <pre>
 * // 1. 初始化
 * const initResp = await api.post('/anyone/filePart/init', formData);
 * const { uploadId, chunkSize, totalChunks } = initResp.data;
 *
 * // 2. 上传所有分片（可并行）
 * const uploadPromises = [];
 * for (let i = 1; i <= totalChunks; i++) {
 *   uploadPromises.push(
 *     api.post('/anyone/filePart/upload', formData, {
 *       params: { uploadId, partNumber: i }
 *     })
 *   );
 * }
 * await Promise.all(uploadPromises);
 *
 * // 3. 完成上传
 * const completeResp = await api.post('/anyone/filePart/complete', { uploadId });
 * const { fileId, url } = completeResp.data;
 * </pre>
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
