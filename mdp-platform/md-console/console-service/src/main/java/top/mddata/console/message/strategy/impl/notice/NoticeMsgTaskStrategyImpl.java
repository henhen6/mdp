package top.mddata.console.message.strategy.impl.notice;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.mybatisflex.core.query.QueryWrapper;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.common.entity.User;
import top.mddata.console.message.entity.MsgTask;
import top.mddata.console.message.entity.MsgTaskRecipient;
import top.mddata.console.message.entity.MsgTemplate;
import top.mddata.console.message.entity.Notice;
import top.mddata.console.message.entity.NoticeRecipient;
import top.mddata.console.message.enumeration.MsgRecipientScopeEnum;
import top.mddata.console.message.service.NoticeRecipientService;
import top.mddata.console.message.service.NoticeService;
import top.mddata.console.message.strategy.MsgTaskStrategy;
import top.mddata.console.message.strategy.dto.MsgResult;
import top.mddata.console.message.strategy.dto.MsgTaskParam;
import top.mddata.console.organization.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * 示例实现类
 * @author henhen
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeMsgTaskStrategyImpl implements MsgTaskStrategy {
    private final NoticeService noticeService;
    private final NoticeRecipientService noticeRecipientService;
    private final UserService userService;

    @Override
    public MsgResult exec(MsgTaskParam msgParam) {
        MsgTask msgTask = msgParam.getMsgTask();
        MsgTemplate msgTemplate = msgParam.getMsgTemplate();
        List<MsgTaskRecipient> taskRecipientList = msgParam.getRecipientList();
        MsgResult msgResult = replaceVariable(msgTask, msgTemplate);

        Notice notice = new Notice();
        notice.setTaskId(msgTask.getId())
                .setTitle(msgResult.getTitle())
                .setContent(msgResult.getContent())
                .setMsgCategory(msgTask.getMsgCategory())
                .setAuthor(msgTask.getAuthor())
                .setUrl(msgTask.getUrl());

        noticeService.save(notice);

        MsgRecipientScopeEnum recipientScope = MsgRecipientScopeEnum.getByCode(msgTask.getRecipientScope(), MsgRecipientScopeEnum.USER);
        List<Long> userIdList = null;
        switch (recipientScope) {
            case ALL: {
                List<User> userList = userService.list(QueryWrapper.create().eq(User::getState, true));
                userIdList = userList.stream().map(User::getId).toList();
                break;
            }
            case ROLE: {
                List<User> userList = userService.listByRoleIds(taskRecipientList.stream().map(MsgTaskRecipient::getRecipient).map(Convert::toLong).toList());
                userIdList = userList.stream().map(User::getId).toList();
                break;
            }
            case DEPT: {
                List<User> userList = userService.listByDeptIds(taskRecipientList.stream().map(MsgTaskRecipient::getRecipient).map(Convert::toLong).toList());
                userIdList = userList.stream().map(User::getId).toList();
                break;
            }
            case USER:
            default: {
                userIdList = taskRecipientList.stream().map(MsgTaskRecipient::getRecipient).map(Convert::toLong).toList();
                break;
            }

        }

        List<NoticeRecipient> recipientList = new ArrayList<>();
        if (CollUtil.isNotEmpty(userIdList)) {
            userIdList.forEach(userId -> {
                NoticeRecipient recipient = new NoticeRecipient();
                recipient.setNoticeId(notice.getId())
                        .setRead(false)
                        .setUserId(userId);
                recipientList.add(recipient);
            });
        }

        if (CollUtil.isNotEmpty(recipientList)) {
            noticeRecipientService.saveBatch(recipientList);
        }

        return MsgResult.builder().result(true).build();
    }

    @Override
    public boolean isSuccess(MsgResult result) {
        return (boolean) result.getResult();
    }
}
