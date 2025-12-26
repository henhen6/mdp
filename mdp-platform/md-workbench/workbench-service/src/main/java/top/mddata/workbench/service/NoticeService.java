package top.mddata.workbench.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.workbench.entity.Notice;

import java.util.List;

/**
 * 站内通知 服务层。
 *
 * @author henhen6
 * @since 2025-12-26 09:47:55
 */
public interface NoticeService extends SuperService<Notice> {

    /**
     * 标记已读。
     *
     * @param ids 通知id
     * @param userId 用户id
     * @return 是否成功
     */
    Boolean mark(List<Long> ids, Long userId);
}
