package com.gitee.sop.support.service;

import java.util.Collection;

/**
 * @author 六如
 */
public interface RefreshService {

    /**
     * 刷新api信息
     *
     * @param apiIds
     */
    void refreshApi(Collection<Long> apiIds);

    /**
     * 刷新isv
     *
     * @param appIds
     */
    void refreshIsv(Collection<String> appIds);

    /**
     * 刷新isv接口权限
     *
     * @param isvIds
     */
    void refreshIsvPerm(Collection<Long> isvIds);

    /**
     * 刷新secret
     *
     * @param isvIds
     */
    void refreshSecret(Collection<Long> isvIds);
}
