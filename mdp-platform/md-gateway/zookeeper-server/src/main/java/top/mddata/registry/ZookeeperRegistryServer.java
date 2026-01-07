package top.mddata.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 内置的建议注册中心 ，生产勿用
 *
 * @author henhen
 */
public class ZookeeperRegistryServer {

    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperRegistryServer.class);

    /**
     * 内置简单的注册中心,基于zookeeper,用来开发演示使用.
     *  生产环境不可使用!
     * @param args 入参
     */
    public static void main(String[] args) {
        int zkPort = 2181;
        LOG.warn("启动内置zookeeper注册中心(仅在开发环境下使用),port={}", zkPort);
        new EmbeddedZooKeeper(zkPort, false).start();
    }
}
