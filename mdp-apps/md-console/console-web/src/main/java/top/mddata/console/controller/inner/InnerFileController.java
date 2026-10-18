package top.mddata.console.controller.inner;

import cn.hutool.core.collection.CollUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.console.dto.system.CopyFilesDto;
import top.mddata.console.dto.system.RelateFilesToBizDto;
import top.mddata.console.service.system.FileService;

import java.util.Collections;
import java.util.List;

/**
 * 内部-文件入口：仅供平台内部服务（workbench、open 等）经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-文件")
@RequestMapping("/inner/system/file")
@RequiredArgsConstructor
public class InnerFileController {

    private final FileService fileService;

    /**
     * 关联文件到业务 （新增、修改时调用）
     *
     * @param relateFilesToBizDto 参数
     */
    @Operation(hidden = true)
    @PostMapping(value = "/relateFilesToBiz")
    public R<Boolean> relateFilesToBiz(@Validated @RequestBody RelateFilesToBizDto relateFilesToBizDto) {
        fileService.relateFilesToBiz(relateFilesToBizDto);
        return R.success();
    }

    /**
     * 释放业务关联的全部附件 （删除业务时调用）
     *
     * @param dto 参数，仅需 objectType 与 objectId
     */
    @Operation(hidden = true)
    @PostMapping("/releaseFilesByBiz")
    public R<Boolean> releaseFilesByBiz(@RequestBody RelateFilesToBizDto dto) {
        // 批量优先取 objectIds，其次单个 objectId；都未传为空集合，service 层按无操作处理
        List<Long> objectIds = CollUtil.isNotEmpty(dto.getObjectIds())
                ? dto.getObjectIds()
                : (dto.getObjectId() != null ? Collections.singletonList(dto.getObjectId()) : Collections.emptyList());
        fileService.releaseFilesByBiz(dto.getObjectType(), objectIds);
        return R.success();
    }

    /**
     * 复制文件
     * @param copyFilesDto 复制参数
     */
    @Operation(hidden = true)
    @PostMapping(value = "/copyFile")
    public R<Boolean> copyFile(@Validated @RequestBody CopyFilesDto copyFilesDto) {
        return R.success(fileService.copyFile(copyFilesDto));
    }
}
