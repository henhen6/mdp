package top.mddata.console.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.console.system.dto.CopyFilesDto;
import top.mddata.console.system.dto.FileUploadDto;
import top.mddata.console.system.dto.RelateFilesToBizDto;
import top.mddata.console.system.service.FileService;
import top.mddata.console.system.vo.FileVo;

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
    @RequestLog(value = "上传文件", request = false)
    public R<FileVo> upload(@RequestParam(value = "file") MultipartFile file, @Validated FileUploadDto fileUploadDto) {
        return R.success(fileService.upload(file, fileUploadDto));
    }


    @Operation(summary = "复制文件", description = "复制文件")
    @PostMapping(value = "/copyFile")
    public R<Boolean> copyFile(@Validated @RequestBody CopyFilesDto copyFilesDto) {
        return R.success(fileService.copyFile(copyFilesDto));
    }


    @Operation(summary = "关联文件到业务", description = "关联文件到业务")
    @PostMapping(value = "/relateFilesToBiz")
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
    @RequestLog("根据文件id获取文件临时的访问路径")
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
    @RequestLog("根据业务类型和业务id，获取文件的访问路径")
    public R<Map<Long, FileVo>> findUrlByObject(@RequestParam String objectType, @RequestParam Long objectId) {
        return R.success(fileService.findUrlByObject(objectType, objectId));
    }


}
