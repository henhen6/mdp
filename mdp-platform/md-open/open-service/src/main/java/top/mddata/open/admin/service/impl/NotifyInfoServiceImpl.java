package top.mddata.open.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.admin.entity.NotifyInfo;
import top.mddata.open.admin.mapper.NotifyInfoMapper;
import top.mddata.open.admin.service.NotifyInfoService;

/**
 * 回调任务 服务层实现。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyInfoServiceImpl extends SuperServiceImpl<NotifyInfoMapper, NotifyInfo> implements NotifyInfoService {

}
