package top.mddata.open.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.common.dto.IdDto;
import top.mddata.open.admin.entity.DocInfo;
import top.mddata.open.admin.service.DocInfoService;
import top.mddata.open.admin.vo.DocInfoVo;

import java.util.List;

/**
 * 文档信息 控制层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@RestController
@Validated
@Tag(name = "文档信息")
@RequestMapping("/admin/docInfo")
@RequiredArgsConstructor
public class DocInfoController extends SuperController<DocInfoService, DocInfo> {
    /**
     * 按树结构查询
     *
     * @param docGroupId 文档分组ID
     * @return 查询结果
     */
    @Operation(summary = "按树结构查询", description = "按树结构查询")
    @PostMapping("/tree")
    @RequestLog("按树结构查询")
    public R<List<DocInfoVo>> tree(@RequestParam Long docGroupId) {
        return R.success(superService.tree(docGroupId, null));
    }


    @Operation(summary = "修改文档状态", description = "修改文档状态")
    @PostMapping("updatePublish")
    @RequestLog("修改文档状态")
    public R<Boolean> updatePublish(@RequestParam Long id, @RequestParam Integer publish) {
        return R.success(superService.updatePublish(id, publish));
    }

    @Operation(summary = "同步所有文档", description = "同步所有文档")
    @PostMapping("syncAllDoc")
    @RequestLog("同步所有文档")
    public R<Boolean> syncAllDoc(@Validated @RequestBody IdDto param) {
        superService.syncAllDoc(param.getId());
        return R.success();
    }

    @Operation(summary = "同步文档", description = "同步文档")
    @PostMapping("syncDoc")
    @RequestLog("同步文档")
    public R<Boolean> syncDoc(@Validated @RequestBody IdDto param) {
        superService.syncDoc(param.getId());
        return R.success();
    }
}
