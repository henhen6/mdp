package top.mddata.open.client;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
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
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.open.admin.entity.EventType;
import top.mddata.open.admin.query.EventTypeQuery;
import top.mddata.open.admin.service.EventTypeService;
import top.mddata.open.admin.vo.EventTypeVo;

import java.util.List;

/**
 * 开发者申请通过后的应用 控制层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@RestController
@Validated
@Tag(name = "事件管理")
@RequestMapping("/client/eventType")
@RequiredArgsConstructor
public class ClientEventTypeController extends SuperController<EventTypeService, EventType> {
    /**
     * 批量查询
     * @param params 查询参数
     * @return 集合
     */
    @PostMapping("/list")
    @Operation(summary = "批量查询", description = "批量查询")
    @RequestLog(value = "批量查询", response = false)
    public R<List<EventTypeVo>> list(@RequestBody @Validated EventTypeQuery params) {
        EventType entity = BeanUtil.toBean(params, EventType.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<EventTypeVo> listVo = superService.listAs(wrapper, EventTypeVo.class);
        return R.success(listVo);
    }

}
