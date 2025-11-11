package top.mddata.base.log;

import org.slf4j.MyMdcAdapter;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;

/**
 * 初始化TtlMDCAdapter实例，并替换MDC中的adapter对象
 *
 * @author henhen6
 * @date 2020年03月09日16:46:47
 */
public class MyMdcAdapterInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        //加载TtlMDCAdapter实例
        MyMdcAdapter.getInstance();
    }
}
