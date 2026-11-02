package top.mddata.console.config;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import tech.powerjob.client.PowerJobClient;

/**
 * PowerJobClient 懒加载持有者。
 *
 * <p>PowerJobClient 构造器会立即请求 Server 做应用鉴权，Server 未启动时会导致整个应用
 * 无法启动。本类将构造推迟到首次使用（运行时），并带失败冷却：创建失败后 60 秒内的
 * 调用快速失败，避免每个请求都重试全部 Server 地址造成请求挂死。
 *
 * @author henhen6
 * @since 2026-10-31
 */
@Slf4j
public class PowerJobClientHolder {

    /** 创建失败后的冷却时长（毫秒） */
    private static final long FAILURE_COOLDOWN_MS = 60_000L;

    private final PowerJobServerProperties properties;
    private final Object lock = new Object();
    private volatile PowerJobClient client;
    private volatile long lastFailureTime;

    public PowerJobClientHolder(PowerJobServerProperties properties) {
        this.properties = properties;
    }

    /**
     * 获取客户端：首次调用时创建（此时才与 Server 交互），失败后进入冷却期快速失败。
     */
    public PowerJobClient getClient() {
        if (StrUtil.isBlank(properties.getServerAddress())) {
            throw new IllegalStateException("请联系管理员配置 powerjob.server.server-address 等参数，否则无法使用定时发送");
        }
        PowerJobClient c = client;
        if (c != null) {
            return c;
        }
        long now = System.currentTimeMillis();
        if (now - lastFailureTime < FAILURE_COOLDOWN_MS) {
            throw new IllegalStateException("PowerJob Server 暂不可用（冷却重试中），请稍后重试");
        }
        synchronized (lock) {
            if (client != null) {
                return client;
            }
            try {
                client = new PowerJobClient(properties.getServerAddress(),
                        properties.getAppName(), properties.getPassword());
                lastFailureTime = 0;
                log.info("[PowerJobClient] 客户端初始化成功, server={}", properties.getServerAddress());
                return client;
            } catch (Exception e) {
                lastFailureTime = System.currentTimeMillis();
                throw new IllegalStateException("PowerJob Server 连接失败（" + properties.getServerAddress()
                        + "）：" + e.getMessage(), e);
            }
        }
    }
}
