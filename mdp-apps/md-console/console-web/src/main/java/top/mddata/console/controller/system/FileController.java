package top.mddata.console.controller.system;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.paginate.Page;
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
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.console.dto.system.CopyFilesDto;
import top.mddata.console.dto.system.RelateFilesToBizDto;
import top.mddata.console.entity.system.File;
import top.mddata.console.query.system.FileQuery;
import top.mddata.console.service.system.FileService;
import top.mddata.console.vo.system.FileVo;

import java.util.Collections;
import java.util.List;

/**
 * 文件 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 20:06:39
 */
@RestController
@Validated
@Tag(name = "文件")
@RequestMapping("/system/file")
@RequiredArgsConstructor
public class FileController extends SuperController<FileService, File> {
    /**
     * 根据主键删除文件。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除文件")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 释放业务关联的全部附件。
     *
     * @param dto 参数，仅需 objectType 与 objectId
     * @return {@code true} 释放成功
     */
    @PostMapping("/releaseFilesByBiz")
    @Operation(summary = "释放业务附件", description = "释放业务关联的全部附件，删除业务时调用")
    @RequestLog(value = "释放业务附件", logType = RequestLog.LogType.DELETE)
    public R<Boolean> releaseFilesByBiz(@RequestBody RelateFilesToBizDto dto) {
        // 批量优先取 objectIds，其次单个 objectId；都未传为空集合，service 层按无操作处理
        List<Long> objectIds = CollUtil.isNotEmpty(dto.getObjectIds())
                ? dto.getObjectIds()
                : (dto.getObjectId() != null ? Collections.singletonList(dto.getObjectId()) : Collections.emptyList());
        superService.releaseFilesByBiz(dto.getObjectType(), objectIds);
        return R.success();
    }

    @Operation(summary = "复制文件", description = "复制文件")
    @PostMapping(value = "/copyFile")
    @RequestLog(value = "复制文件", logType = RequestLog.LogType.ADD)
    public R<Boolean> copyFile(@Validated @RequestBody CopyFilesDto copyFilesDto) {
        return R.success(superService.copyFile(copyFilesDto));
    }


    @Operation(summary = "关联文件到业务", description = "关联文件到业务")
    @PostMapping(value = "/relateFilesToBiz")
    @RequestLog(value = "关联文件到业务", logType = RequestLog.LogType.UPDATE)
    public R<Boolean> relateFilesToBiz(@Validated @RequestBody RelateFilesToBizDto relateFilesToBizDto) {
        superService.relateFilesToBiz(relateFilesToBizDto);
        return R.success();
    }


    /**
     * 分页查询文件。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询文件")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<FileVo>> page(@RequestBody @Validated PageParams<FileQuery> params) {
        Page<FileVo> page = Page.of(params.getCurrent(), params.getSize());
        File entity = BeanUtil.toBean(params.getModel(), File.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, FileVo.class);
        return R.success(page);
    }

}
