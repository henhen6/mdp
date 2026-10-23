package top.mddata.api.impl;

import com.gitee.sop.support.dto.CommonFileData;
import com.gitee.sop.support.dto.FileData;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import top.mddata.api.DemoOpenService;
import top.mddata.api.dto.DemoUploadResp;

import java.nio.charset.StandardCharsets;

/**
 * demo 接口实现：不做真实存储，仅验证网关文件链路通断。
 *
 * @author henhen
 */
@DubboService
@Service
@Slf4j
public class DemoOpenServiceImpl implements DemoOpenService {

    @Override
    public DemoUploadResp uploadMore(FileData file) {
        log.info("[demo] 收到上传文件, name={}, originalFilename={}, size={}",
                file.getName(), file.getOriginalFilename(), file.getBytes() == null ? 0 : file.getBytes().length);
        return new DemoUploadResp()
                .setFileName(file.getOriginalFilename())
                .setSize(file.getBytes() == null ? 0 : file.getBytes().length);
    }

    @Override
    public FileData download() {
        CommonFileData fileData = new CommonFileData();
        fileData.setName("demo");
        fileData.setOriginalFilename("demo.txt");
        fileData.setContentType("text/plain");
        fileData.setData("hello open platform".getBytes(StandardCharsets.UTF_8));
        return fileData;
    }
}
