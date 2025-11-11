package com.gitee.sop.support.springboot.config;

import com.gitee.sop.support.message.OpenMessageFactory;
import com.gitee.sop.support.register.ApiRegisterRunner;
import com.gitee.sop.support.service.ApiRegisterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Map;


/**
 * @author 六如
 */
@Configuration
@Slf4j
public class SopAutoConfiguration implements InitializingBean {

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    @DubboReference
    private ApiRegisterService apiRegisterService;

    @Override
    public void afterPropertiesSet() throws Exception {
        String appName = environment.getProperty("spring.application.name");
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(DubboService.class);
        // 注册接口
        ApiRegisterRunner.reg(appName, apiRegisterService, beanMap.values());
        // 初始化国际化
        OpenMessageFactory.initMessage();
    }


}
