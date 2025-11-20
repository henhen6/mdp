package top.mddata.open.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.admin.entity.AppApply;
import top.mddata.open.admin.mapper.AppApplyMapper;
import top.mddata.open.admin.service.AppApplyService;

/**
 * 应用申请 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppApplyServiceImpl extends SuperServiceImpl<AppApplyMapper, AppApply> implements AppApplyService {

}
