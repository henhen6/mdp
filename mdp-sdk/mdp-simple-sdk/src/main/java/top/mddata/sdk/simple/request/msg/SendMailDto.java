package top.mddata.sdk.simple.request.msg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 根据模版编码发送消息任务
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
public class SendMailDto extends MsgSendDto implements Serializable {

    private static final long serialVersionUID = 1L;


    private List<String> recipientList;

    /**
     * 添加接收人
     *
     * @param email 接收人邮箱
     * @return this
     */
    public SendMailDto addRecipient(String email) {
        if (this.recipientList == null) {
            this.recipientList = new ArrayList<>();
        }
        this.recipientList.add(email);
        return this;
    }

    public List<String> getRecipientList() {
        return recipientList;
    }

    public SendMailDto setRecipientList(List<String> recipientList) {
        this.recipientList = recipientList;
        return this;
    }
}
