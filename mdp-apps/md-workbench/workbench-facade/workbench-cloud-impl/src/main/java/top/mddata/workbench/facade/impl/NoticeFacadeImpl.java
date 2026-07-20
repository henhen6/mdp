package top.mddata.workbench.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.util.ContextUtil;
import top.mddata.workbench.entity.Notice;
import top.mddata.workbench.entity.NoticeRecipient;
import top.mddata.workbench.facade.NoticeFacade;
import top.mddata.workbench.facade.api.NoticeApi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 跨服务-通知实现类
 * @author henhen6
 * @since 2025/12/26 10:21
 */
@Service
@RequiredArgsConstructor
public class NoticeFacadeImpl implements NoticeFacade {
    private final NoticeApi noticeApi;

    @Override
    public void saveBatchNoticeRecipient(List<NoticeRecipient> recipientList) {
        noticeApi.saveBatchNoticeRecipient(recipientList);
    }

    @Override
    public void save(Notice notice) {
        noticeApi.save(notice);
    }

    @Override
    public Long countByCategory(LocalDateTime startTime, Integer msgCategory) {
        return noticeApi.countByCategory(startTime, msgCategory).getData();
    }

    @Override
    public List<Map<String, Object>> countByCategoryDistribution() {
        return noticeApi.countByCategoryDistribution().getData();
    }

    @Override
    public Long countUnread() {
        Long userId = ContextUtil.getUserId();
        if (userId == null) {
            return 0L;
        }
        return noticeApi.countUnread(userId).getData();
    }
}
