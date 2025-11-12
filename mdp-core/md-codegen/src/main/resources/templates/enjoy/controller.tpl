#set(tableComment = table.getComment())
#set(swaggerComment = table.getSwaggerComment())
#set(primaryKeyType = table.getPrimaryKey().getPropertySimpleType())
#set(entityClassName = table.buildEntityClassName())
#set(dtoClassName = table.buildDtoClassName())
#set(voClassName = table.buildVoClassName())
#set(queryClassName = table.buildQueryClassName())
#set(entityVarName = firstCharToLowerCase(entityClassName))
package #(packageConfig.controllerPackage);

#for(importClass : table.buildControllerImports())
import #(importClass);
#end

/**
 * #(tableComment) 控制层。
 *
 * @author #(javadocConfig.getAuthor())
 * @since #(javadocConfig.getSince())
 */
#if(controllerConfig.restStyle)
@RestController
#else
@Controller
#end
@Validated
#if(swaggerVersion.getName() == "FOX")
@Api("#(swaggerComment)")
#end
#if(swaggerVersion.getName() == "DOC")
@Tag(name = "#(swaggerComment)")
#end
@RequestMapping("#(table.buildControllerRequestMappingPrefix())/#(firstCharToLowerCase(entityClassName))")
@RequiredArgsConstructor
public class #(controllerClassName) #if(controllerConfig.superClass)extends #(controllerConfig.buildSuperClassName(table)) #end {
#if(controllerConfig.withCrud)
#if(controllerConfig.superClass == null)
    private final #(table.buildServiceClassName()) #(serviceVarName);
#end
    /**
     * 添加#(tableComment)。
     *
     * @param dto #(swaggerComment)
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    #if(swaggerVersion.getName() == "FOX")
    @ApiOperation("新增")
    #end
    #if(swaggerVersion.getName() == "DOC")
    @Operation(summary = "新增", description = "保存#(swaggerComment)")
    #end
    @RequestLog(value = "新增", request = false)
    public R<#(primaryKeyType)> save(@Validated @RequestBody #(dtoClassName) dto) {
        return R.success(#(serviceVarName).saveDto(dto).getId());
    }

    /**
     * 根据主键删除#(tableComment)。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    #if(swaggerVersion.getName() == "FOX")
    @ApiOperation("删除")
    #end
    #if(swaggerVersion.getName() == "DOC")
    @Operation(summary = "删除", description = "根据主键删除#(swaggerComment)")
    #end
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<#(primaryKeyType)> ids) {
        return R.success(#(serviceVarName).removeByIds(ids));
    }

    /**
     * 根据主键更新#(tableComment)。
     *
     * @param dto #(swaggerComment)
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    #if(swaggerVersion.getName() == "FOX")
    @ApiOperation("修改")
    #end
    #if(swaggerVersion.getName() == "DOC")
    @Operation(summary = "修改", description = "根据主键更新#(swaggerComment)")
    #end
    @RequestLog(value = "修改", request = false)
    public R<#(primaryKeyType)> update(@Validated(BaseEntity.Update.class) @RequestBody #(dtoClassName) dto) {
        return R.success(#(serviceVarName).updateDtoById(dto).getId());
    }

    /**
     * 根据#(tableComment)主键获取详细信息。
     *
     * @param id #(swaggerComment)主键
     * @return #(swaggerComment)详情
     */
    @GetMapping("/getById")
    #if(swaggerVersion.getName() == "FOX")
    @ApiOperation("单体查询")
    #end
    #if(swaggerVersion.getName() == "DOC")
    @Operation(summary = "单体查询", description = "根据主键获取#(swaggerComment)")
    #end
    @RequestLog("'单体查询:' + #id")
    public R<#(voClassName)> get(@RequestParam #if(swaggerVersion.getName() == "FOX")@ApiParam("#(swaggerComment)主键") #if(swaggerVersion.getName() == "DOC")@Parameter(description = "#(swaggerComment)主键")#end#end #(primaryKeyType) id) {
        #(entityClassName) entity = #(serviceVarName).getById(id);
        return R.success(BeanUtil.toBean(entity, #(voClassName).class));
    }

    /**
     * 分页查询#(tableComment)。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    #if(swaggerVersion.getName() == "FOX")
    @ApiOperation("分页列表查询")
    #end
    #if(swaggerVersion.getName() == "DOC")
    @Operation(summary = "分页列表查询", description = "分页查询#(swaggerComment)")
    #end
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<#(voClassName)>> page(@RequestBody @Validated PageParams<#(queryClassName)> params) {
        Page<#(voClassName)> page = Page.of(params.getCurrent(), params.getSize());
        #(entityClassName) entity = BeanUtil.toBean(params.getModel(), #(entityClassName).class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        #(serviceVarName).pageAs(page, wrapper, #(voClassName).class);
        return R.success(page);
    }

    /**
     * 批量查询
     * @param params 查询参数
     * @return 集合
     */
    @PostMapping("/list")
    @Operation(summary = "批量查询", description = "批量查询")
    @RequestLog(value = "批量查询", response = false)
    public R<List<#(voClassName)>> list(@RequestBody @Validated #(queryClassName) params) {
        #(entityClassName) entity = BeanUtil.toBean(params, #(entityClassName).class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<#(voClassName)> listVo = #(serviceVarName).listAs(wrapper, #(voClassName).class);
        return R.success(listVo);
    }
#end
}
