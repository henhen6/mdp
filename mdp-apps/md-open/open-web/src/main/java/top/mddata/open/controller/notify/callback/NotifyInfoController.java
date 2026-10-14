package top.mddata.open.controller.notify.callback;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
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
import top.mddata.base.interfaces.echo.EchoService;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.common.dto.IdDto;
import top.mddata.open.controller.support.PageWrapperHelper;
import top.mddata.open.entity.admin.NotifyInfo;
import top.mddata.open.query.admin.NotifyInfoQuery;
import top.mddata.open.service.notify.callback.NotifyInfoService;
import top.mddata.open.vo.admin.NotifyInfoVo;

import java.util.List;

/**
 * 回调任务 控制层。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@RestController
@Validated
@Tag(name = "回调任务")
@RequestMapping("/admin/notifyInfo")
@RequiredArgsConstructor
public class NotifyInfoController extends SuperController<NotifyInfoService, NotifyInfo> {

    private final EchoService echoService;

    /**
     * 根据主键删除回调任务。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除回调任务")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }


    /**
     * 根据回调任务主键获取详细信息。
     *
     * @param id 回调任务主键
     * @return 回调任务详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取回调任务")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<NotifyInfoVo> get(@RequestParam Long id) {
        NotifyInfo entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, NotifyInfoVo.class));
    }

    /**
     * 分页查询回调任务。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询回调任务")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<NotifyInfoVo>> page(@RequestBody @Validated PageParams<NotifyInfoQuery> params) {
        Page<NotifyInfoVo> page = Page.of(params.getCurrent(), params.getSize());
        superService.pageAs(page, PageWrapperHelper.build(params, NotifyInfo.class), NotifyInfoVo.class);
        echoService.action(page);
        return R.success(page);
    }

    /**
     * 重新推送
     *
     * @param param 表单数据
     * @return 返回影响行数
     */
    @PostMapping("/push")
    @Operation(summary = "重新推送", description = "重新推送")
    @RequestLog(value = "'重新推送:' + #param.id", logType = RequestLog.LogType.OTHER)
    public R<Boolean> push(@Validated @RequestBody IdDto param) {
        return R.success(superService.push(param.getId(), null));
    }

    /**
     * 结束重试
     *
     * @param param 表单数据
     * @return 返回影响行数
     */
    @Operation(summary = "结束重试", description = "结束重试")
    @PostMapping("/end")
    @RequestLog(value = "'结束重试:' + #param.id", logType = RequestLog.LogType.OTHER)
    public R<Boolean> end(@Validated @RequestBody IdDto param) {
        return R.success(superService.end(param.getId()));
    }

}
