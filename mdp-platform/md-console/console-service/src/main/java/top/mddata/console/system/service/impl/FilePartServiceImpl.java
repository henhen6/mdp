package top.mddata.console.system.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.console.system.entity.FilePart;
import top.mddata.console.system.mapper.FilePartMapper;
import top.mddata.console.system.service.FilePartService;

/**
 * 文件分片
 * 仅在手动分片上传时使用 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FilePartServiceImpl extends SuperServiceImpl<FilePartMapper, FilePart> implements FilePartService {

}
