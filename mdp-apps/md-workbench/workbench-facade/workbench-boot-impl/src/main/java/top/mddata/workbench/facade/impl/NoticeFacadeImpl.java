package top.mddata.workbench.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.workbench.entity.Notice;
import top.mddata.workbench.entity.NoticeRecipient;
import top.mddata.workbench.facade.NoticeFacade;
import top.mddata.workbench.service.NoticeRecipientService;
import top.mddata.workbench.service.NoticeService;

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
    private final NoticeRecipientService noticeRecipientService;
    private final NoticeService noticeService;

    @Override
    public void saveBatchNoticeRecipient(List<NoticeRecipient> recipientList) {
        noticeRecipientService.saveBatch(recipientList);
    }

    @Override
    public void save(Notice notice) {
        noticeService.save(notice);
    }

    @Override
    public Long countByCategory(LocalDateTime startTime, Integer msgCategory) {
        return noticeService.countByCategory(startTime, msgCategory);
    }

    @Override
    public List<Map<String, Object>> countByCategoryDistribution() {
        return noticeService.countByCategoryDistribution();
    }
}