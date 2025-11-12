package top.mddata.console.system.mapper;

import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.console.system.entity.FilePart;

/**
 * 文件分片
 * 仅在手动分片上传时使用 映射层。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Repository
public interface FilePartMapper extends SuperMapper<FilePart> {

}
