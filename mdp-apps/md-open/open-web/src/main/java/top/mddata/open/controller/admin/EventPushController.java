package top.mddata.open.controller.admin;

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
import top.mddata.common.dto.IdDto;
import top.mddata.open.entity.admin.EventPush;
import top.mddata.open.query.admin.EventPushQuery;
import top.mddata.open.service.admin.EventPushService;
import top.mddata.open.vo.admin.EventPushVo;

import java.util.List;

/**
 * 事件推送任务 控制层。
 *
 * @author henhen6
 * @since 2026-01-12 21:28:36
 */
@RestController
@Validated
@Tag(name = "事件推送任务")
@RequestMapping("/admin/eventPush")
@RequiredArgsConstructor
public class EventPushController extends SuperController<EventPushService, EventPush> {

    /**
     * 重新推送
     *
     * @param param 表单数据
     * @return 返回影响行数
     */
    @PostMapping("/push")
    @Operation(summary = "重新推送", description = "重新推送")
    @RequestLog(value = "'重新推送:' + #param.id", logType = RequestLog.LogType.OTHER)
    public R<Long> push(@Validated @RequestBody IdDto param) {
        return R.success(superService.executeImmediately(param.getId()));
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

    /**
     * 根据主键删除事件推送任务。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除事件推送任务")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }


    /**
     * 根据事件推送任务主键获取详细信息。
     *
     * @param id 事件推送任务主键
     * @return 事件推送任务详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取事件推送任务")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<EventPushVo> get(@RequestParam Long id) {
        EventPush entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, EventPushVo.class));
    }

    /**
     * 分页查询事件推送任务。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询事件推送任务")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<EventPushVo>> page(@RequestBody @Validated PageParams<EventPushQuery> params) {
        Page<EventPushVo> page = Page.of(params.getCurrent(), params.getSize());
        EventPush entity = BeanUtil.toBean(params.getModel(), EventPush.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, EventPushVo.class);
        return R.success(page);
    }

    /**
     * 批量查询
     * @param params 查询参数
     * @return 集合
     */
    @PostMapping("/list")
    @Operation(summary = "批量查询", description = "批量查询")
    @RequestLog(value = "批量查询", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<EventPushVo>> list(@RequestBody @Validated EventPushQuery params) {
        EventPush entity = BeanUtil.toBean(params, EventPush.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<EventPushVo> listVo = superService.listAs(wrapper, EventPushVo.class);
        return R.success(listVo);
    }
}
