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
public class SendSmsDto extends MsgSendDto implements Serializable {

    private static final long serialVersionUID = 1L;


    private List<String> recipientList;

    public List<String> getRecipientList() {
        return recipientList;
    }

    public SendSmsDto setRecipientList(List<String> recipientList) {
        this.recipientList = recipientList;
        return this;
    }

    /**
     * 添加接收人
     *
     * @param phone 接收人手机号
     * @return this
     */
    public SendSmsDto addRecipient(String phone) {
        if (this.recipientList == null) {
            this.recipientList = new ArrayList<>();
        }
        this.recipientList.add(phone);
        return this;
    }
}
