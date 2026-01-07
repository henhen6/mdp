package top.mddata.gateway.sop.manager.impl;

import org.springframework.stereotype.Service;
import top.mddata.gateway.sop.manager.IpBlacklistManager;

/**
 *
 * @author henhen
 * @since 2026/1/6 16:47
 */
@Service
public class IpBlacklistManagerImpl implements IpBlacklistManager {
    @Override
    public boolean contains(String ip) {
        return false;
    }
}
