package top.mddata.console.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.console.entity.system.FilePart;
import top.mddata.console.service.system.FilePartService;

/**
 * 文件分片
 * 仅在手动分片上传时使用 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 20:06:39
 */
@RestController
@Validated
@Tag(name = "文件分片")
@RequestMapping("/anyone/filePart")
@RequiredArgsConstructor
public class FilePartController extends SuperController<FilePartService, FilePart> {
}
