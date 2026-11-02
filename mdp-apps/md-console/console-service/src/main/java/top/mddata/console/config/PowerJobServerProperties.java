package top.mddata.console.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PowerJob Server 连接配置（OpenAPI 客户端用）。
 *
 * <p>微服务版配置于 Nacos 的 common.yml，单体版配置于 boot-server 的 application.yml：
 * <pre>
 * powerjob:
 *   server:
 *     server-address: 127.0.0.1:7700
 *     app-name: mdp
 *     password: xxx
 *     msg-job-id: 1   # 消息定时发送的常驻 job（timeExpressionType=API），在 PowerJob 控制台创建
 * </pre>
 *
 * @author henhen6
 * @since 2026-10-31
 */
@Data
@ConfigurationProperties(prefix = "powerjob.server")
public class PowerJobServerProperties {

    /**
     * PowerJob Server 地址（ip:port，多个用逗号分隔）
     */
    private String serverAddress;

    /**
     * 应用名称（PowerJob 控制台创建的应用）
     */
    private String appName;

    /**
     * 应用密码
     */
    private String password;

    /**
     * 消息定时发送的常驻 job ID（timeExpressionType=API，processor=top.mddata.worker.processors.MsgTaskSendProcessor）
     */
    private Long msgJobId;
}
