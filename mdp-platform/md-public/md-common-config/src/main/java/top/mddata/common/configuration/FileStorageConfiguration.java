package top.mddata.common.configuration;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.spring.SpringFileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.mddata.common.file.LocalPlusExtFileStorage;

import java.util.Collections;
import java.util.List;

/**
 * 扩展类
 */
@Slf4j
@Configuration
public class FileStorageConfiguration {
    @Autowired
    private SpringFileStorageProperties properties;

    /**
     * 华为云 OBS 存储 Bean ，注意返回值必须是个 List
     */
    @Bean
    public List<LocalPlusExtFileStorage> getLocalPlusExtFileStorageList() {
        List<? extends SpringFileStorageProperties.SpringLocalPlusConfig> localPlus = properties.getLocalPlus();

        SpringFileStorageProperties.SpringLocalPlusConfig config = BeanUtil.toBean(localPlus.get(0), SpringFileStorageProperties.SpringLocalPlusConfig.class);
        config.setPlatform("localPlusExt");

        LocalPlusExtFileStorage storage = new LocalPlusExtFileStorage(config);
        return Collections.singletonList(storage);
    }
}
