package top.mddata.open.controller.client;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.utils.MyTreeUtil;
import top.mddata.common.enumeration.BooleanEnum;
import top.mddata.open.dto.client.DocDebugBuildParamDto;
import top.mddata.open.entity.admin.DocGroup;
import top.mddata.open.entity.admin.HelpDoc;
import top.mddata.open.service.doc.DocDebugService;
import top.mddata.open.service.doc.DocGroupService;
import top.mddata.open.service.doc.DocInfoService;
import top.mddata.open.service.doc.HelpDocService;
import top.mddata.open.vo.admin.DocGroupVo;
import top.mddata.open.vo.admin.DocInfoViewVo;
import top.mddata.open.vo.admin.DocInfoVo;
import top.mddata.open.vo.admin.HelpDocVo;
import top.mddata.open.vo.client.DocDebugBuildParamVo;

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
    private final HelpDocService helpDocService;
    private final DocDebugService docDebugService;

    /**
     * 获取文档分组列表
     */
    @RequestLog(value = "获取文档分组列表", logType = RequestLog.LogType.QUERY)
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
    @RequestLog(value = "获取树型文档数据", logType = RequestLog.LogType.QUERY)
    @Operation(summary = "获取树型文档数据", description = "获取树型文档数据")
    @GetMapping(value = "/docInfo/tree")
    public R<List<DocInfoVo>> findDocTree(@RequestParam Long docGroupId) {
        return R.success(docInfoService.tree(docGroupId, BooleanEnum.TRUE.getInteger()));
    }

    /**
     * 按树结构查询
     *
     * @return 查询结果
     */
    @Operation(summary = "按树结构查询")
    @GetMapping("/help/tree")
    @RequestLog(value = "按树结构查询", logType = RequestLog.LogType.QUERY)
    public R<List<HelpDocVo>> tree() {
        List<HelpDoc> list = helpDocService.list(QueryWrapper.create().eq(HelpDoc::getState, true).orderBy(HelpDoc::getWeight, true));
        List<HelpDocVo> voList = BeanUtil.copyToList(list, HelpDocVo.class);
        return R.success(MyTreeUtil.buildTreeEntity(voList, HelpDocVo::new));
    }

    /**
     * 获取文档详情
     *
     * @param id 文档id
     */
    @RequestLog(value = "获取文档详情", logType = RequestLog.LogType.QUERY)
    @Operation(summary = "获取文档详情", description = "获取文档详情")
    @GetMapping("/docInfo/detail")
    public R<DocInfoViewVo> getDocDetail(@RequestParam Long id) {
        return R.success(docInfoService.getDocView(id));
    }

    /**
     * 组装在线调试请求参数
     *
     * @param dto 调试参数
     */
    @RequestLog(value = "'组装在线调试请求参数:' + #dto.docId + ':' + #dto.env", logType = RequestLog.LogType.QUERY, request = false, response = false)
    @Operation(summary = "组装在线调试请求参数", description = "按 mdp-sdk 规则组装并签名参数，最终请求由浏览器直连网关")
    @PostMapping("/debug/buildParam")
    public R<DocDebugBuildParamVo> buildDebugParam(@Validated @RequestBody DocDebugBuildParamDto dto) {
        return R.success(docDebugService.buildParam(dto));
    }
}
