package top.mddata.console.controller.permission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.console.dto.permission.ResourceApiBindDto;
import top.mddata.console.dto.permission.ResourceApiSaveDto;
import top.mddata.console.entity.permission.ResourceApi;
import top.mddata.console.service.permission.ResourceApiService;
import top.mddata.console.vo.permission.ResourceApiVo;

import java.util.List;

/**
 * 接口权限 控制层。
 *
 * @author henhen6
 * @since 2026-09-27
 */
@RestController
@Validated
@Tag(name = "接口权限")
@RequestMapping("/permission/resourceApi")
@RequiredArgsConstructor
public class ResourceApiController extends SuperController<ResourceApiService, ResourceApi> {

    @GetMapping("/list")
    @Operation(summary = "查询资源关联的接口", description = "按资源id查询已关联接口")
    @RequestLog(value = "查询资源关联的接口", logType = RequestLog.LogType.QUERY)
    public R<List<ResourceApiVo>> list(@RequestParam Long resourceId) {
        return R.success(superService.listByResource(resourceId));
    }

    @PostMapping("/save")
    @Operation(summary = "手动录入接口", description = "手动新增通配符接口并关联资源")
    @RequestLog(value = "手动录入接口", logType = RequestLog.LogType.ADD)
    public R<Boolean> save(@Validated @RequestBody ResourceApiSaveDto dto) {
        return R.success(superService.saveManual(dto));
    }

    @PostMapping("/bind")
    @Operation(summary = "选择接口", description = "批量绑定扫描接口到资源")
    @RequestLog(value = "选择接口", logType = RequestLog.LogType.ADD)
    public R<Integer> bind(@Validated @RequestBody ResourceApiBindDto dto) {
        return R.success(superService.bind(dto));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除接口", description = "按主键删除接口关联")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.deleteByIds(ids));
    }
}
