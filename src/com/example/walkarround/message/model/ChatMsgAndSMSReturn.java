package com.example.walkarround.message.model;

import java.util.List;

/**
 * 消息列表（RCS消息和SMS短信）
 * Date: 2015-03-21
 *
 * @author mss
 */
public class ChatMsgAndSMSReturn {

    private long lastChatId;
    private long lastSmsId;
    private int searchMsgPos = -1;
    private List<ChatMsgBaseInfo> chatMessages;

    public ChatMsgAndSMSReturn(long lastChatId, long lastSmsId, List<ChatMsgBaseInfo> chatMessages) {
        super();
        this.lastChatId = lastChatId;
        this.lastSmsId = lastSmsId;
        this.chatMessages = chatMessages;
    }

    public long getLastChatId() {
        return lastChatId;
    }

    public void setLastChatId(long lastChatId) {
        this.lastChatId = lastChatId;
    }

    public long getLastSmsId() {
        return lastSmsId;
    }

    public void setLastSmsId(int lastSmsId) {
        this.lastSmsId = lastSmsId;
    }

    public List<ChatMsgBaseInfo> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(List<ChatMsgBaseInfo> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public int getSearchMsgPos() {
        return searchMsgPos;
    }

    public void setSearchMsgPos(int searchMsgPos) {
        this.searchMsgPos = searchMsgPos;
    }

    @Override
    public String toString() {
        return "ChatMsgAndSMSReturn [lastChatId=" + lastChatId + ", lastSmsId=" + lastSmsId + ", chatMessages="
                + chatMessages + "]";
    }
}
