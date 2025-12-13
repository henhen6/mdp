package top.mddata.workbench.controller;

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
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.base.utils.ContextUtil;
import top.mddata.workbench.entity.LoginLog;
import top.mddata.workbench.query.LoginLogQuery;
import top.mddata.workbench.service.LoginLogService;
import top.mddata.workbench.vo.LoginLogVo;

import java.util.List;

/**
 * 登录日志 控制层。
 *
 * @author henhen6
 * @since 2025-12-14 00:53:23
 */
@RestController
@Validated
@Tag(name = "登录日志")
@RequestMapping("//loginLog")
@RequiredArgsConstructor
public class LoginLogController extends SuperController<LoginLogService, LoginLog> {

    /**
     * 根据主键删除登录日志。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除登录日志")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }


    /**
     * 根据登录日志主键获取详细信息。
     *
     * @param id 登录日志主键
     * @return 登录日志详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取登录日志")
    @RequestLog("'单体查询:' + #id")
    public R<LoginLogVo> get(@RequestParam Long id) {
        LoginLog entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, LoginLogVo.class));
    }

    /**
     * 分页查询登录日志。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询登录日志")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<LoginLogVo>> page(@RequestBody @Validated PageParams<LoginLogQuery> params) {
        Page<LoginLogVo> page = Page.of(params.getCurrent(), params.getSize());
        LoginLog entity = BeanUtil.toBean(params.getModel(), LoginLog.class);
        entity.setCreatedBy(ContextUtil.getUserId());
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, LoginLogVo.class);
        return R.success(page);
    }
}
