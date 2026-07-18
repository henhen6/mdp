package top.mddata.console.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.console.service.system.ConfigService;
import top.mddata.console.service.system.DictItemService;
import top.mddata.console.vo.system.ConfigVo;
import top.mddata.console.vo.system.DictItemVo;

import java.util.List;
import java.util.Map;

/**
 * 通用 控制器
 *
 * @author henhen6
 * @date 2025年07月28日23:53:12
 */
@Slf4j
@RestController
@RequestMapping("/anyUser")
@Tag(name = "字典-枚举-参数-通用查询")
@RequiredArgsConstructor
public class GeneralController {

    private final DictItemService dictItemService;
    private final ConfigService configService;

    @Operation(summary = "根据参数标识，查询参数值", description = "根据参数标识，查询参数值")
    @PostMapping("/param/findConfigByUniqKey")
    @RequestLog(value = "根据参数标识查询参数值", logType = RequestLog.LogType.QUERY)
    public R<Map<String, ConfigVo>> findConfigByUniqKey(@RequestBody List<String> uniqKeys) {
        return R.success(configService.findConfigByUniqKey(uniqKeys));
    }

    @Operation(summary = "根据字典类型编码批量查询字典项", description = "根据字典类型编码批量查询字典项")
    @PostMapping("/dict/findDictItemByUniqKey")
    @RequestLog(value = "根据字典类型编码批量查询字典项", logType = RequestLog.LogType.QUERY)
    public R<Map<String, List<DictItemVo>>> findDictItemByUniqKey(@RequestBody List<String> query) {
        return R.success(dictItemService.findDictItemByUniqKey(query));
    }
}
