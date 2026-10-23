package top.mddata.open.service.notify.callback;

import top.mddata.base.base.R;
import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.dto.admin.NotifyInfoDto;
import top.mddata.open.entity.admin.NotifyInfo;

import java.time.LocalDateTime;

/**
 * 回调任务 服务层接口。
 */
public interface NotifyInfoService extends SuperService<NotifyInfo> {

    /** 生成回调任务并异步投递，返回任务 id */
    R<Long> notify(NotifyInfoDto request);

    /** 手动重推（可换回调地址），状态重置后异步投递 */
    Boolean push(Long id, String url);

    /** 手动结束重试（仅 FAIL 状态可结束） */
    Boolean end(Long id);

    /** 重试扫描：取到期 FAIL 与丢失 WAIT 任务提交线程池（PowerJob 定时调用） */
    void retry(LocalDateTime now, int limit);
}
