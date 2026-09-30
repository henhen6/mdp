package top.mddata.sdk.simple.request.msg;


import java.io.Serializable;
import java.util.List;

/**
 * 根据模版编码发送消息任务
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
public class SendNoticeDto extends MsgSendDto implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 消息分类
     * 站内信专用
     * [1-待办 2-公告 3-预警]
     */

    private Integer msgCategory;

    /**
     * 发布人
     */


    private String author;


    private List<Long> recipientIdList;
    /**
     * 接收范围
     * [0-所有人 1-指定用户 2-指定角色 3-指定部门]
     */
    private Integer recipientScope;

    /**
     * 跳转地址
     */
    private String url;

    public Integer getMsgCategory() {
        return msgCategory;
    }

    public SendNoticeDto setMsgCategory(Integer msgCategory) {
        this.msgCategory = msgCategory;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public SendNoticeDto setAuthor(String author) {
        this.author = author;
        return this;
    }

    public List<Long> getRecipientIdList() {
        return recipientIdList;
    }

    public SendNoticeDto setRecipientIdList(List<Long> recipientIdList) {
        this.recipientIdList = recipientIdList;
        return this;
    }

    public Integer getRecipientScope() {
        return recipientScope;
    }

    public SendNoticeDto setRecipientScope(Integer recipientScope) {
        this.recipientScope = recipientScope;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public SendNoticeDto setUrl(String url) {
        this.url = url;
        return this;
    }
}
