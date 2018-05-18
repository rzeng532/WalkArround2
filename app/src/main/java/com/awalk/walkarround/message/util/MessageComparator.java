package com.awalk.walkarround.message.util;


import com.awalk.walkarround.message.model.ChatMsgBaseInfo;

import java.util.Comparator;

public class MessageComparator implements Comparator<ChatMsgBaseInfo> {

    public static final int TIME_ASC = 1;
    public static final int TIME_DESC = 2;

    private int sortOrder = TIME_DESC;

    public MessageComparator(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(ChatMsgBaseInfo chatMsgBaseInfo, ChatMsgBaseInfo chatMsgBaseInfo2) {
        if (sortOrder == TIME_ASC) {
            return Long.compare(chatMsgBaseInfo.getTime(), chatMsgBaseInfo2.getTime());
        } else {
            return Long.compare(chatMsgBaseInfo2.getTime(), chatMsgBaseInfo.getTime());
        }
    }
}
