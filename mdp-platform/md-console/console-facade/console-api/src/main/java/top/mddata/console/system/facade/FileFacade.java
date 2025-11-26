package top.mddata.console.system.facade;

import top.mddata.console.system.dto.CopyFilesDto;
import top.mddata.console.system.dto.RelateFilesToBizDto;

/**
 * 业务文件接口
 */
public interface FileFacade {
    /**
     * 关联文件到业务 （新增、修改时调用）
     *
     * @param relateFilesToBizDto 参数
     */
    void relateFilesToBiz(RelateFilesToBizDto relateFilesToBizDto);

    /**
     * 复制文件
     * @param copyFilesDto 复制参数
     */
    Boolean copyFile(CopyFilesDto copyFilesDto);
}
