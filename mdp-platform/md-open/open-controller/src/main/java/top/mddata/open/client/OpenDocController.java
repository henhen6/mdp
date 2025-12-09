package top.mddata.open.client;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.common.enumeration.BooleanEnum;
import top.mddata.open.admin.entity.DocGroup;
import top.mddata.open.admin.service.DocGroupService;
import top.mddata.open.admin.service.DocInfoService;
import top.mddata.open.admin.vo.DocGroupVo;
import top.mddata.open.admin.vo.DocInfoViewVo;
import top.mddata.open.admin.vo.DocInfoVo;

import java.util.List;

/**
 * 开放文档-公开接口
 *
 * @author henhen6
 * @since 2025年12月09日22:32:07
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/anyUser/client/openDoc")
@Tag(name = "开放文档-公开接口")
public class OpenDocController {
    private final DocGroupService docGroupService;
    private final DocInfoService docInfoService;

    /**
     * 获取文档分组列表
     */
    @RequestLog("获取文档分组列表")
    @Operation(summary = "获取文档分组列表", description = "获取文档分组列表")
    @GetMapping(value = "/docGroup/list")
    public R<List<DocGroupVo>> findDocGroupList() {
        List<DocGroup> list = docGroupService.list(QueryWrapper.create().eq(DocGroup::getPublish, true));
        return R.success(BeanUtil.copyToList(list, DocGroupVo.class));
    }

    /**
     * 获取树型文档数据
     *
     * @param docGroupId 文档分组ID
     */
    @RequestLog("获取树型文档数据")
    @Operation(summary = "获取树型文档数据", description = "获取树型文档数据")
    @GetMapping(value = "/docInfo/tree")
    public R<List<DocInfoVo>> findDocTree(@RequestParam Long docGroupId) {
        return R.success(docInfoService.tree(docGroupId, BooleanEnum.TRUE.getInteger()));
    }

    /**
     * 获取文档详情
     *
     * @param id 文档id
     */
    @RequestLog("获取文档详情")
    @Operation(summary = "获取文档详情", description = "获取文档详情")
    @GetMapping("/docInfo/detail")
    public R<DocInfoViewVo> getDocDetail(@RequestParam Long id) {
        return R.success(docInfoService.getDocView(id));
    }
}
