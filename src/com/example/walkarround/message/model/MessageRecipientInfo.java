package com.example.walkarround.message.model;

import java.util.List;

/**
 * 短消息接收者信息
 * Date: 2015-02-28
 *
 * @author mss
 */
public class MessageRecipientInfo {

    /*消息会话id*/
    private long mThreadId = -1;
    private int mConversationType;
    private List<String> mRecipientList;
    /*群聊sip参数中的conversationId*/
    private String mConversationId;
    /*群id*/
    private String mGroupId;

    private String mdisplayName;

    public long getThreadId() {
        return mThreadId;
    }

    public void setThreadId(long mThreadId) {
        this.mThreadId = mThreadId;
    }

    public int getConversationType() {
        return mConversationType;
    }

    public void setConversationType(int mConversationType) {
        this.mConversationType = mConversationType;
    }

    public List<String> getRecipientList() {
        return mRecipientList;
    }

    public void setRecipientList(List<String> mSendeeList) {
        this.mRecipientList = mSendeeList;
    }

    public String getConversationId() {
        return mConversationId;
    }

    public void setConversationId(String mConversationId) {
        this.mConversationId = mConversationId;
    }

    public String getGroupId() {
        return mGroupId;
    }

    public void setGroupId(String mGroupId) {
        this.mGroupId = mGroupId;
    }

    public String getDisplayName() {
        return mdisplayName;
    }

    public void setDisplayName(String mdisplayName) {
        this.mdisplayName = mdisplayName;
    }
}
