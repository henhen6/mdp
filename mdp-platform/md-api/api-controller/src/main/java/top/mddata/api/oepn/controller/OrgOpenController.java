package top.mddata.api.oepn.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.api.oepn.OrgOpenService;
import top.mddata.api.oepn.UserOpenService;
import top.mddata.api.oepn.dto.OrgSaveDto;
import top.mddata.api.oepn.dto.UserBatchSaveDto;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;

/**
 *
 * @author henhen
 * @since 2026/1/8 08:56
 */
@RestController
@Validated
@Tag(name = "test")
@RequestMapping("/orgOpen")
@RequiredArgsConstructor
public class OrgOpenController {
    private final OrgOpenService orgOpenService;
    private final UserOpenService userOpenService;

    @PostMapping("/batchSave")
    @Operation(summary = "新增user", description = "保存文档分组")
    public R<Object> batchSave(@Validated @RequestBody UserBatchSaveDto dto) {
        return R.success(userOpenService.batchSave(dto));
    }

    @PostMapping("/save")
    @Operation(summary = "新增org", description = "保存文档分组")
    public R<Object> save(@Validated @RequestBody OrgSaveDto dto) {
        return R.success(orgOpenService.save(dto));
    }

}
