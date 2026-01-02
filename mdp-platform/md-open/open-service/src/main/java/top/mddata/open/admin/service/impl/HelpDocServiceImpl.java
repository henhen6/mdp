package top.mddata.open.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.admin.entity.HelpDoc;
import top.mddata.open.admin.mapper.HelpDocMapper;
import top.mddata.open.admin.service.HelpDocService;

/**
 * 帮助文档 服务层实现。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HelpDocServiceImpl extends SuperServiceImpl<HelpDocMapper, HelpDoc> implements HelpDocService {

}
