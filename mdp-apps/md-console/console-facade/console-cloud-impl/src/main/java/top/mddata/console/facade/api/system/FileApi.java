package top.mddata.console.facade.api.system;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;
import top.mddata.console.dto.system.CopyFilesDto;
import top.mddata.console.dto.system.RelateFilesToBizDto;
import top.mddata.console.facade.api.system.fallback.FileApiFallback;

/**
 *
 * @author henhen
 * @since 2026/5/10 23:07
 */
@FeignClient(name = AppConstants.CONSOLE_SERVER, fallback = FileApiFallback.class, path = "/system/file")
public interface FileApi {
    /**
     * 关联文件到业务 （新增、修改时调用）
     *
     * @param relateFilesToBizDto 参数
     */
    @PostMapping(value = "/relateFilesToBiz")
    R<Long> relateFilesToBiz(@RequestBody RelateFilesToBizDto relateFilesToBizDto);

    /**
     * 复制文件
     * @param copyFilesDto 复制参数
     */
    @PostMapping(value = "/copyFile")
    R<Boolean> copyFile(@RequestBody CopyFilesDto copyFilesDto);
}
