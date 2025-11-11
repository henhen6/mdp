package top.mddata.common.enumeration;

import top.mddata.common.vo.Option;
import top.mddata.base.base.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 枚举接口
 * @author henhen6
 * @since 2025/9/23 20:18
 */
@Slf4j
@RestController
@RequestMapping("/enumeration")
@Tag(name = "枚举查询")
@RequiredArgsConstructor
public class EnumController {
    private final EnumService enumService;

    @Operation(summary = "返回指定目录下的所有枚举类", description = "只能扫描实现了BaseEnum类的枚举")
    @PostMapping("/findAll")
    public R<Map<Option, List<Option>>> findAll(@RequestParam(required = false) Boolean rescan) {
        return R.success(enumService.findAll(rescan));
    }
}
