package top.mddata.workbench.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.workbench.entity.NoticeRecipient;
import top.mddata.workbench.mapper.NoticeRecipientMapper;
import top.mddata.workbench.service.NoticeRecipientService;

/**
 * 通知接收人 服务层实现。
 *
 * @author henhen6
 * @since 2025-12-26 09:55:35
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NoticeRecipientServiceImpl extends SuperServiceImpl<NoticeRecipientMapper, NoticeRecipient> implements NoticeRecipientService {

}
