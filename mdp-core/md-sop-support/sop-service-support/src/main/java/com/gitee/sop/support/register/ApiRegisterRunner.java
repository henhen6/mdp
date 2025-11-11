package com.gitee.sop.support.register;

import com.gitee.sop.support.service.ApiRegisterService;
import com.gitee.sop.support.service.dto.RegisterDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author 六如
 */
@Slf4j
public class ApiRegisterRunner {

    private static final int PERIOD = 5;
    private static ApiRegister apiRegister = null;
    private static ScheduledExecutorService scheduledExecutorService;

    public static void reg(
            String appName,
            ApiRegisterService apiRegisterService,
            Collection<Object> objects
    ) {
        if (apiRegister == null) {
            apiRegister = new ApiRegister(apiRegisterService);
        }
        try {
            apiRegister.reg(appName, objects);
        } catch (ApiRegException e) {
            log.warn("注册接口失败，{}秒后进行重试", PERIOD, e);
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(() -> retry(e.getRegisterDTOS()), PERIOD, PERIOD, TimeUnit.SECONDS);
        }
    }

    private static void retry(List<RegisterDTO> registerDTOS) {
        try {
            apiRegister.reg(registerDTOS);
            // 注册成功,关闭定时器
            scheduledExecutorService.shutdown();
        } catch (Exception e) {
            log.warn("注册接口失败，{}秒后进行重试, errorMsg={}", PERIOD, e.getMessage());
        }
    }

}
