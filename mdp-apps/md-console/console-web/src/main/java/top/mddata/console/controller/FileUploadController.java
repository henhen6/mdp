package top.mddata.console.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.console.dto.system.CopyFilesDto;
import top.mddata.console.dto.system.FileUploadDto;
import top.mddata.console.dto.system.RelateFilesToBizDto;
import top.mddata.console.service.system.FileService;
import top.mddata.console.vo.system.FileVo;

import java.util.List;
import java.util.Map;

/**
 * 文件 控制层。
 *
 * @author henhen6
 * @since 2025-10-22 09:22:00
 */
@RestController
@Validated
@Tag(name = "文件上传与下载")
@RequestMapping("/anyone/file")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {
    private final FileService fileService;


    /**
     * 上传文件
     */
    @Operation(summary = "上传文件", description = "上传文件")
    @Parameters({
            @Parameter(name = "file", description = "附件", schema = @Schema(name = "file", format = "binary"), in = ParameterIn.DEFAULT, required = true),
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequestLog(value = "上传文件", logType = RequestLog.LogType.ADD, request = false)
    public R<FileVo> upload(@RequestParam(value = "file") MultipartFile file, @Validated FileUploadDto fileUploadDto) {
        return R.success(fileService.upload(file, fileUploadDto));
    }


    @Operation(summary = "复制文件", description = "复制文件")
    @PostMapping(value = "/copyFile")
    @RequestLog(value = "复制文件", logType = RequestLog.LogType.ADD)
    public R<Boolean> copyFile(@Validated @RequestBody CopyFilesDto copyFilesDto) {
        return R.success(fileService.copyFile(copyFilesDto));
    }


    @Operation(summary = "关联文件到业务", description = "关联文件到业务")
    @PostMapping(value = "/relateFilesToBiz")
    @RequestLog(value = "关联文件到业务", logType = RequestLog.LogType.UPDATE)
    public R<Boolean> relateFilesToBiz(@Validated @RequestBody RelateFilesToBizDto relateFilesToBizDto) {
        fileService.relateFilesToBiz(relateFilesToBizDto);
        return R.success();
    }

    /**
     * 根据文件id，获取访问路径
     *
     * @param ids 文件id
     */
    @Operation(summary = "根据文件id查询文件的临时访问路径", description = "根据文件id查询文件的临时访问路径")
    @PostMapping(value = "/findUrlByIds")
    @RequestLog(value = "根据文件id查询文件临时访问路径", logType = RequestLog.LogType.QUERY)
    public R<Map<Long, FileVo>> findUrlByIds(@RequestBody List<Long> ids) {
        return R.success(fileService.findUrlByIds(ids));
    }

    /**
     * 根据业务类型和业务id，获取文件的访问路径
     *
     * @param objectType 业务类型
     * @param objectId 业务id
     */
    @Operation(summary = "根据业务类型和业务id，获取文件的访问路径", description = "根据业务类型和业务id，获取文件的访问路径")
    @PostMapping(value = "/findUrlByObject")
    @RequestLog(value = "根据业务类型和业务id获取文件访问路径", logType = RequestLog.LogType.QUERY)
    public R<Map<Long, FileVo>> findUrlByObject(@RequestParam String objectType, @RequestParam Long objectId) {
        return R.success(fileService.findUrlByObject(objectType, objectId));
    }

    /**
     * 下载一个文件或多个文件打包下载
     *
     * @param ids 文件id
     */
    @Operation(summary = "根据文件id打包下载文件", description = "根据文件id打包下载文件")
    @GetMapping(value = "/download", produces = "application/octet-stream")
    @RequestLog(value = "批量下载附件", logType = RequestLog.LogType.QUERY)
    public void download(@RequestParam List<Long> ids, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ArgumentAssert.notEmpty(ids, "请选择至少一个附件");
        fileService.download(request, response, ids);
    }

    /**
     * 根据文件id下载文件
     *
     * @param id 文件id
     */
    @Operation(summary = "根据文件id下载文件", description = "根据文件id下载文件")
    @GetMapping(value = "/down", produces = "application/octet-stream")
    @RequestLog(value = "下载附件", logType = RequestLog.LogType.QUERY)
    public void download(@RequestParam Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ArgumentAssert.notNull(id, "请选择至少一个附件");
        fileService.download(request, response, id);
    }

}
