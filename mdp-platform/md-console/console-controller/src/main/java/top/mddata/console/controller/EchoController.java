package top.mddata.console.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.console.system.service.DictItemService;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 回显实现类
 *
 * @author henhen6
 * @date 2025年07月26日10:48:36
 */
@Slf4j
@RestController
@AllArgsConstructor()
@RequestMapping("/echo")
@Tag(name = "数据回显实现类", description = "前端请勿调用")
@Hidden
public class EchoController {
    private final DictItemService dictItemService;

    @Operation(summary = "查询字典项", description = "根据字典编码查询字典项")
    @PostMapping("/dict/findByIds")
    public Map<Serializable, Object> findDictByIds(@RequestParam Set<Serializable> ids) {
        return this.dictItemService.findByIds(ids);
    }

}
