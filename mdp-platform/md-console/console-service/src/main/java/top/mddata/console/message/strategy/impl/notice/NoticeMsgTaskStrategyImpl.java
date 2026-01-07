package top.mddata.console.message.strategy.impl.notice;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.mybatisflex.core.query.QueryWrapper;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.common.entity.User;
import top.mddata.common.properties.MsgProperties;
import top.mddata.console.message.entity.MsgTask;
import top.mddata.console.message.entity.MsgTaskRecipient;
import top.mddata.console.message.entity.MsgTemplate;
import top.mddata.console.message.enumeration.MsgRecipientScopeEnum;
import top.mddata.console.message.strategy.AbstractMsgTaskStrategy;
import top.mddata.console.message.strategy.dto.MsgResult;
import top.mddata.console.message.strategy.dto.MsgTaskParam;
import top.mddata.console.organization.service.UserService;
import top.mddata.workbench.entity.Notice;
import top.mddata.workbench.entity.NoticeRecipient;
import top.mddata.workbench.facade.NoticeFacade;

import java.util.ArrayList;
import java.util.List;

/**
 * 站内信 实现类
 * @author henhen
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeMsgTaskStrategyImpl extends AbstractMsgTaskStrategy {
    private final NoticeFacade noticeFacade;
    private final UserService userService;
    private final MsgProperties msgProperties;

    @Override
    public MsgResult exec(MsgTaskParam msgParam) {
        validParam(msgParam);

        MsgTask msgTask = msgParam.getMsgTask();
        MsgTemplate msgTemplate = msgParam.getMsgTemplate();
        List<MsgTaskRecipient> taskRecipientList = msgParam.getRecipientList();
        MsgResult msgResult = replaceVariable(msgTask, msgTemplate, msgProperties);

        Notice notice = new Notice();
        notice.setTaskId(msgTask.getId())
                .setTitle(msgResult.getTitle())
                .setContent(msgResult.getContent())
                .setMsgCategory(msgTask.getMsgCategory())
                .setAuthor(msgTask.getAuthor())
                .setUrl(msgTask.getUrl());

        noticeFacade.save(notice);

        MsgRecipientScopeEnum recipientScope = MsgRecipientScopeEnum.match(msgTask.getRecipientScope(), MsgRecipientScopeEnum.USER);
        List<Long> userIdList = null;
        List<User> userList = null;
        switch (recipientScope) {
            case ALL:
                userList = userService.list(QueryWrapper.create().eq(User::getState, true));
                userIdList = userList.stream().map(User::getId).toList();
                break;

            case ROLE:
                userList = userService.listByRoleIds(taskRecipientList.stream().map(MsgTaskRecipient::getRecipient).map(Convert::toLong).toList());
                userIdList = userList.stream().map(User::getId).toList();
                break;

            case DEPT:
                userList = userService.listByDeptIds(taskRecipientList.stream().map(MsgTaskRecipient::getRecipient).map(Convert::toLong).toList());
                userIdList = userList.stream().map(User::getId).toList();
                break;
            case USER:
            default:
                userIdList = taskRecipientList.stream().map(MsgTaskRecipient::getRecipient).map(Convert::toLong).toList();
                break;

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
            noticeFacade.saveBatchNoticeRecipient(recipientList);
        }

        msgResult.setResult(true);
        return msgResult;
    }

    @Override
    public boolean isSuccess(MsgResult result) {
        return (boolean) result.getResult();
    }
}
