/**
 * Copyright (C) 2014-2015 All rights reserved
 */
package com.awalk.walkarround.message.model;


import com.awalk.walkarround.message.util.MessageConstant;

/**
 * 消息列表 Date: 2015-06-03
 *
 * @author mss
 */
public class MessageSessionModelInfo extends MessageSessionBaseModel {

    private String contact;
    private String data;
    private long lastTime;
    private int sendReceive;
    private long threadId;
    private long time;
    private String subject;
    private int top;
    private int msgType = MessageConstant.MessageType.MSG_TYPE_TEXT;
    private int chatType = MessageConstant.ChatType.CHAT_TYPE_ONE2ONE;/* 消息类型：一对一、一对多、群聊 */
    private boolean isBurnAfter = false;

    @Override
    public void setSessionModel(Object sessionModel) {

    }

    @Override
    public void setContact(String contact) {
        this.contact = contact;
    }

    @Override
    public void setChatType(int type) {
        this.chatType = type;
    }

    @Override
    public void setData(String data) {
        this.data = data;
    }

    @Override
    public void setThreadId(long id) {
        this.threadId = id;
    }

    @Override
    public void setLastTime(long time) {
        this.lastTime = time;
    }

    @Override
    public void setSendReceive(int value) {
        this.sendReceive = value;
    }

    @Override
    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public void setSubject(String subject) {
        this.subject = subject;
    }

    
    public void setTop(int top) {
        this.top = top;
    }
    
    @Override
    public String getData() {
        return this.data;
    }

    @Override
    public long getLastTime() {
        return this.lastTime;
    }

    @Override
    public int getChatType() {
        return this.chatType;
    }

    @Override
    public String getContact() {
        return this.contact;
    }

    @Override
    public long getThreadId() {
        return this.threadId;
    }

    @Override
    public int getSendReceive() {
        return this.sendReceive;
    }

    @Override
    public long getTime() {
        return this.time;
    }

    @Override
    public String getSubject() {
        return this.subject;
    }
    @Override
    public int getTop() {
        return top;
    }
    
    @Override
    public void setContentType(int type) {
        msgType = type;
    }

    @Override
    public int getContentType() {
        return msgType;
    }

    @Override
    public boolean isBurnAfterMsg() {
        return isBurnAfter;
    }

    @Override
    public void setIsBurnAfterMsg(boolean isBurnAfter) {
        this.isBurnAfter = isBurnAfter;
    }

}
