package top.mddata.console.controller.inner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.console.service.system.DictItemService;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 内部-字典项入口：仅供平台内部服务（@Echo 回显等）经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-字典项")
@RequestMapping("/inner/system/dictItem")
@RequiredArgsConstructor
public class InnerDictItemController {

    private final DictItemService dictItemService;

    /**
     * 查询字典项
     *
     * @param ids 字典key
     * @return 字典key#字典项key -> 字典
     */
    @Operation(hidden = true)
    @PostMapping("/findByIds")
    public Map<Serializable, Object> findByIds(@RequestParam("ids") Set<Serializable> ids) {
        return dictItemService.findByIds(ids);
    }
}
