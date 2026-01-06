package top.mddata.gateway.sop.service;

import org.apache.dubbo.common.config.ReferenceCache;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.SimpleReferenceCache;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * dubbo泛化调用
 *
 * @author 六如
 */
@Service
public class GenericServiceInvoker implements InitializingBean {
    private static final String TRUE = "true";

    private ApplicationConfig applicationConfig;

    @Value("${dubbo.registry.address:zookeeper://localhost:2181}")
    private String registerAddress;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${generic.timeout:5000}")
    private int timeout;

    @Override
    public void afterPropertiesSet() throws Exception {
        RegistryConfig registryConfig = buildRegistryConfig();
        applicationConfig = new ApplicationConfig();
        applicationConfig.setName(appName + "-generic");
        applicationConfig.setRegistry(registryConfig);
    }

    private RegistryConfig buildRegistryConfig() {
        RegistryConfig config = new RegistryConfig();
        config.setAddress(registerAddress);
        return config;
    }

    @SuppressWarnings("uncheck")
    public Object invoke(String interfaceName, String method, String[] parameterTypes, Object[] params) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setGeneric(TRUE);
        reference.setApplication(applicationConfig);
        reference.setInterface(interfaceName);
        reference.setTimeout(timeout);
        ReferenceCache referenceCache = SimpleReferenceCache.getCache();
        GenericService genericService = referenceCache.get(reference);
        return genericService.$invoke(method, parameterTypes, params);
    }

}
