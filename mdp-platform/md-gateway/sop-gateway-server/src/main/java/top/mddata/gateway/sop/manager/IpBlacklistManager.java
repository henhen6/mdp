package top.mddata.gateway.sop.manager;

/**
 * IP黑名单管理
 * @author tangyh
 * @since 2026/1/6 16:44
 */
public interface IpBlacklistManager {
    boolean contains(String ip);
}
