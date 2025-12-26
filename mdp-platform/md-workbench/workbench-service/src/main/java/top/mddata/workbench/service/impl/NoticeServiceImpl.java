package top.mddata.workbench.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.workbench.entity.Notice;
import top.mddata.workbench.entity.NoticeRecipient;
import top.mddata.workbench.mapper.NoticeMapper;
import top.mddata.workbench.service.NoticeRecipientService;
import top.mddata.workbench.service.NoticeService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 站内通知 服务层实现。
 *
 * @author henhen6
 * @since 2025-12-26 09:47:55
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NoticeServiceImpl extends SuperServiceImpl<NoticeMapper, Notice> implements NoticeService {
    private final NoticeRecipientService noticeRecipientService;

    @Override
    public Boolean mark(List<Long> ids, Long userId) {
        if (CollectionUtil.isEmpty(ids) || userId == null) {
            return true;
        }
        NoticeRecipient recipient = new NoticeRecipient();
        recipient.setRead(true).setReadTime(LocalDateTime.now());
        QueryWrapper wrapper = QueryWrapper.create()
                .eq(NoticeRecipient::getUserId, userId)
                .in(NoticeRecipient::getNoticeId, ids);
        return noticeRecipientService.update(recipient, wrapper);
    }
}
