package top.mddata.console.controller.inner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.console.service.organization.PositionService;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 内部-岗位入口：仅供平台内部服务（@Echo 回显等）经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-岗位")
@RequestMapping("/inner/organization/position")
@RequiredArgsConstructor
public class InnerPositionController {

    private final PositionService positionService;

    /**
     * 根据id查询待回显参数
     *
     * @param ids 唯一键（可能不是主键ID)
     */
    @Operation(hidden = true)
    @PostMapping("/findByIds")
    public Map<Serializable, Object> findByIds(@RequestParam("ids") Set<Serializable> ids) {
        return positionService.findByIds(ids);
    }
}
