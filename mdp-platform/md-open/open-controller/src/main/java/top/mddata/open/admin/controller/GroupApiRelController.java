package top.mddata.open.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.open.admin.dto.GroupApiRelDto;
import top.mddata.open.admin.entity.GroupApiRel;
import top.mddata.open.admin.service.GroupApiRelService;

import java.util.List;

/**
 * 分组拥有的对外接口 控制层。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@RestController
@Validated
@Tag(name = "分组拥有的对外接口")
@RequestMapping("/admin/groupApiRel")
@RequiredArgsConstructor
public class GroupApiRelController extends SuperController<GroupApiRelService, GroupApiRel> {
    /**
     * 添加分组拥有的对外接口。
     *
     * @param dto 分组拥有的对外接口
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存分组拥有的对外接口")
    @RequestLog(value = "新增", request = false)
    public R<Boolean> save(@Validated @RequestBody GroupApiRelDto dto) {
        superService.saveDto(dto);
        return R.success();
    }

    /**
     * 根据主键删除分组拥有的对外接口。
     *
     * @param dto 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除分组拥有的对外接口")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@Validated @RequestBody GroupApiRelDto dto) {
        return R.success(superService.delete(dto.getGroupId(), dto.getApiIdList()));
    }

}
