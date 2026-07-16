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
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @Override
    public Long countByCategory(LocalDateTime startTime, Integer msgCategory) {
        QueryWrapper wrapper = QueryWrapper.create()
                .eq(Notice::getMsgCategory, msgCategory)
                .ge(Notice::getCreatedAt, startTime);
        return mapper.selectCountByQuery(wrapper);
    }

    @Override
    public List<Map<String, Object>> countByCategoryDistribution() {
        QueryWrapper wrapper = QueryWrapper.create()
                .select(Notice::getMsgCategory, Notice::getId)
                .isNotNull(Notice::getMsgCategory);
        List<Notice> list = mapper.selectListByQuery(wrapper);
        if (CollectionUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        java.util.Map<Integer, Long> countMap = new java.util.HashMap<>();
        for (Notice n : list) {
            Integer cat = n.getMsgCategory();
            countMap.merge(cat, 1L, Long::sum);
        }
        List<Map<String, Object>> result = new java.util.ArrayList<>(countMap.size());
        for (java.util.Map.Entry<Integer, Long> e : countMap.entrySet()) {
            Map<String, Object> row = new java.util.HashMap<>(2);
            row.put("msgCategory", e.getKey());
            row.put("count", e.getValue());
            result.add(row);
        }
        return result;
    }
}