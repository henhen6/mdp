package top.mddata.console.permission.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
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
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.interfaces.echo.EchoService;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.utils.MyTreeUtil;
import top.mddata.console.permission.dto.ResourceMenuDto;
import top.mddata.console.permission.entity.ResourceMenu;
import top.mddata.console.permission.query.ResourceMenuQuery;
import top.mddata.console.permission.service.ResourceMenuService;
import top.mddata.console.permission.vo.ResourceMenuVo;

import java.util.List;

import static top.mddata.common.constant.SwaggerConstants.DATA_TYPE_LONG;
import static top.mddata.common.constant.SwaggerConstants.DATA_TYPE_STRING;

/**
 * 菜单 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 19:49:35
 */
@RestController
@Validated
@Tag(name = "菜单")
@RequestMapping("/permission/resourceMenu")
@RequiredArgsConstructor
public class ResourceMenuController extends SuperController<ResourceMenuService, ResourceMenu> {
    private final EchoService echoService;

    /**
     * 添加菜单。
     *
     * @param dto 菜单
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存菜单")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody ResourceMenuDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除菜单。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除菜单")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新菜单。
     *
     * @param dto 菜单
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新菜单")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody ResourceMenuDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 根据菜单主键获取详细信息。
     *
     * @param id 菜单主键
     * @return 菜单详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取菜单")
    @RequestLog("'单体查询:' + #id")
    public R<ResourceMenuVo> get(@RequestParam Long id) {
        ResourceMenu entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, ResourceMenuVo.class));
    }


    /**
     * 按照树结构查询系统的所有菜单
     */
    @Operation(summary = "按照树结构查询系统的所有菜单", description = "按照树结构查询系统的所有菜单")
    @PostMapping("/tree")
    @RequestLog("按照树结构查询系统的所有菜单")
    public R<List<ResourceMenuVo>> tree(@RequestBody ResourceMenuQuery query) {
        List<ResourceMenu> list = superService.list(new QueryWrapper().eq(ResourceMenu::getAppId, query.getAppId()).orderBy(ResourceMenu::getWeight, true));
        List<ResourceMenuVo> resultList = BeanUtil.copyToList(list, ResourceMenuVo.class);
        // 回显 @Echo 标记的字段
        echoService.action(resultList);
        return R.success(MyTreeUtil.buildTreeEntity(resultList, ResourceMenuVo::new));
    }


    @Parameters({
            @Parameter(name = "appId", description = "应用ID", schema = @Schema(type = DATA_TYPE_LONG), in = ParameterIn.QUERY),
            @Parameter(name = "id", description = "ID", schema = @Schema(type = DATA_TYPE_LONG), in = ParameterIn.QUERY),
            @Parameter(name = "code", description = "编码", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
    })
    @Operation(summary = "检测编码是否存在", description = "检测编码是否存在")
    @GetMapping("/checkCode")
    public R<Boolean> checkCode(@RequestParam Long appId, @RequestParam String code, @RequestParam(required = false) Long id) {
        return R.success(superService.checkCode(appId, code, id));
    }


    @Parameters({
            @Parameter(name = "appId", description = "应用ID", schema = @Schema(type = DATA_TYPE_LONG), in = ParameterIn.QUERY),
            @Parameter(name = "id", description = "ID", schema = @Schema(type = DATA_TYPE_LONG), in = ParameterIn.QUERY),
            @Parameter(name = "path", description = "路径", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
    })
    @Operation(summary = "检测路径是否存在", description = "检测路径是否存在")
    @GetMapping("/checkPath")
    public R<Boolean> checkPath(@RequestParam Long appId, @RequestParam String path, @RequestParam(required = false) Long id) {
        return R.success(superService.checkPath(appId, path, id));
    }

    @Parameters({
            @Parameter(name = "appId", description = "应用ID", schema = @Schema(type = DATA_TYPE_LONG), in = ParameterIn.QUERY),
            @Parameter(name = "id", description = "ID", schema = @Schema(type = DATA_TYPE_LONG), in = ParameterIn.QUERY),
            @Parameter(name = "name", description = "菜单名称", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
    })
    @Operation(summary = "检测名称是否存在", description = "检测名称是否存在")
    @GetMapping("/checkName")
    public R<Boolean> checkName(@RequestParam Long appId, @RequestParam String name, @RequestParam(required = false) Long id) {
        return R.success(superService.checkName(appId, name, id));
    }


    @Parameters({
            @Parameter(name = "id", description = "ID", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
    })
    @Operation(summary = "根据父ID获取菜单的默认值", description = "新增时，根据父ID获取菜单的默认值")
    @GetMapping("/getDefMenuByParentId")
    public R<ResourceMenuVo> getDefMenuByParentId(@RequestParam Long appId, @RequestParam(required = false) Long id) {
        return R.success(superService.getDefMenuByParentId(appId, id));
    }


    @Operation(summary = "移动菜单", description = "移动菜单")
    @PostMapping("/move")
    @RequestLog("移动菜单")
    public R<Boolean> move(@RequestParam Long sourceId, @RequestParam(required = false) Long targetId) {
        superService.move(sourceId, targetId);
        return R.success();
    }

}
