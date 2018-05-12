/**
 * Copyright (C) 2014-2015 All rights reserved
 */
package com.example.walkarround.message.model;

import static com.example.walkarround.message.util.MessageConstant.*;

/**
 * 消息列表（base）
 * Date: 2015-06-03
 *
 * @author mss
 */
public abstract class MessageSessionBaseModel {
    public boolean isUnread;/* true未读，false已读 */
    public int unReadCount;/* 未读数量*/
    public long msgId;/* 消息的id*/
    public int msgStatus = MessageState.MSG_STATE_SENT;/* 消息的状态*/
    public String name;/*名字*/
    public String nameLastC;/*名字的最后一个字符*/
    public String profile;/*头像地址*/
    public int defaultResId;/*默认头像*/
    public int status; //会话状态, IM, 走走，评价等
    public int colorIndex;  //颜色索引

    private int conversationType = ConversationType.GENERAL;


    public abstract void setSessionModel(Object sessionModel);
    public abstract void setContact(String contact);
    public abstract void setChatType(int type);
    public abstract void setData(String data);
    public abstract void setThreadId(long id);
    public abstract void setLastTime(long time);
    public abstract void setSendReceive(int value);
    public abstract void setTime(long time);
    public abstract void setSubject(String subject);
    public abstract void setTop(int top);
    public abstract void setContentType(int type);

    public abstract String getData();
    public abstract long getLastTime();
    public abstract int getChatType();
    public abstract String getContact();
    public abstract long getThreadId();
    public abstract int getSendReceive();
    public abstract long getTime();
    public abstract String getSubject();
    public abstract int getTop();
    public abstract int getContentType();

    public abstract boolean isBurnAfterMsg();
    public abstract void setIsBurnAfterMsg(boolean isBurbAfter);

    public abstract boolean isNotifyConversation();
    public abstract void setNotifyConversation(boolean isNotify);

    public void setItemType(int type) {
        conversationType = type;
    }
    public int getItemType() {
        return conversationType;
    }

}
