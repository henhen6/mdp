package top.mddata.console.system.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
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
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.console.system.dto.ConfigDto;
import top.mddata.console.system.entity.Config;
import top.mddata.console.system.query.ConfigQuery;
import top.mddata.console.system.service.ConfigService;
import top.mddata.console.system.vo.ConfigVo;

import java.util.List;

/**
 * 系统配置 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 20:06:39
 */
@RestController
@Validated
@Tag(name = "系统配置")
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class ConfigController extends SuperController<ConfigService, Config> {
    /**
     * 添加系统配置。
     *
     * @param dto 系统配置
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存系统配置")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody ConfigDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除系统配置。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除系统配置")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新系统配置。
     *
     * @param dto 系统配置
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新系统配置")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody ConfigDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 分页查询系统配置。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询系统配置")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<ConfigVo>> page(@RequestBody @Validated PageParams<ConfigQuery> params) {
        Page<ConfigVo> page = Page.of(params.getCurrent(), params.getSize());
        Config entity = BeanUtil.toBean(params.getModel(), Config.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, ConfigVo.class);
        return R.success(page);
    }

    /**
     * 检查参数标识是否重复
     * @param uniqKey 标识
     * @param id 参数id
     * @return
     */
    @PostMapping("/check")
    @Operation(summary = "检查参数标识是否重复", description = "检查参数标识是否重复")
    @RequestLog(value = "检查参数标识是否重复")
    public R<Boolean> check(@RequestParam String uniqKey, @RequestParam(required = false) Long id) {
        return R.success(superService.check(uniqKey, null, id));
    }


    /**
     * 根据参数标识，查询系统参数VO
     * @param uniqKey 参数标识
     * @return 系统参数
     */
    @GetMapping("/getParam")
    public R<ConfigVo> getParam(@RequestParam String uniqKey) {
        return R.success(superService.getConfig(uniqKey));
    }

    /**
     * 根据参数标识，查询长整型系统参数
     * @param uniqKey 参数标识
     * @return 参数值
     */
    @GetMapping("/getLong")
    public R<Long> getLong(@RequestParam String uniqKey, @RequestParam(required = false) Long defaultValue) {
        return R.success(superService.getLong(uniqKey, defaultValue));
    }

    /**
     * 根据参数标识，查询整型系统参数
     * @param uniqKey 参数标识
     * @return 参数值
     */
    @GetMapping("/getInteger")
    public R<Integer> getInteger(@RequestParam String uniqKey, @RequestParam(required = false) Integer defaultValue) {
        return R.success(superService.getInteger(uniqKey, defaultValue));
    }

    /**
     * 根据参数标识，查询字符型系统参数
     * @param uniqKey 参数标识
     * @return 参数值
     */
    @GetMapping("/getString")
    public R<String> getString(@RequestParam String uniqKey, @RequestParam(required = false) String defaultValue) {
        return R.success(superService.getString(uniqKey, defaultValue));
    }

    /**
     * 根据参数标识，查询布尔型系统参数
     * @param uniqKey 参数标识
     * @return 参数值
     */
    @GetMapping("/getBoolean")
    public R<Boolean> getBoolean(@RequestParam String uniqKey, @RequestParam(required = false) Boolean defaultValue) {
        return R.success(superService.getBoolean(uniqKey, defaultValue));
    }
}
