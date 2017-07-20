package com.example.walkarround.message.model;

import java.util.List;

import static com.example.walkarround.message.util.MessageConstant.*;

/**
 * 消息内容 Date: 2015-03-17
 *
 * @author mss
 */
public class ChatMessageInfo extends ChatMsgBaseInfo {

    private String messageContent;
    private int chatType = ChatType.CHAT_TYPE_ONE2ONE;
    private int msgType = MessageType.MSG_TYPE_TEXT;
    private int msgState = MessageState.MSG_STATE_SENT;
    private int sendReceive = MessageSendReceive.MSG_SEND;
    private long messageId;
    private boolean isRead = false;
    private long sendTime;
    private long planSendTime;
    private String fileUrlPath;
    private String thumbUrlPath;
    private String filePath;
    private String fileName;
    private String extraInfo;
    private int duration;
    private long fileSize;
    private String sendContact;
    private long threadId;
    private String thumbPath;
    private String conversationId;
    private String groupId;
    private double latitute;
    private double longitude;
    private String loacationLabel;
    private String packetId;
    private long msgCreateTime;
    private List<String> receiverList;
    private boolean isBurnAfter;
    private String title;

    private int imageWidth;
    private int imageHeight;

    @Override
    public void setChatMessage(Object message) {
    }

    @Override
    public int getChatType() {
        return chatType;
    }

    @Override
    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    @Override
    public int getMsgType() {
        return msgType;
    }

    @Override
    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    @Override
    public int getMsgState() {
        return msgState;
    }

    @Override
    public void setMsgState(int msgState) {
        this.msgState = msgState;
    }

    @Override
    public int getSendReceive() {
        return sendReceive;
    }

    @Override
    public void setSendReceive(int type) {
        sendReceive = type;
    }

    @Override
    public long getMsgId() {
        return messageId;
    }

    @Override
    public void setMsgId(long messageId) {
        this.messageId = messageId;
    }

    @Override
    public boolean getIsRead() {
        return isRead;
    }

    @Override
    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    @Override
    public long getTime() {
        return sendTime;
    }

    @Override
    public void setTime(long time) {
        sendTime = time;
    }

    @Override
    public long getPlanSendTime() {
        return planSendTime;
    }

    @Override
    public void setPlanSendTime(long time) {
        planSendTime = time;
    }

    @Override
    public String getFilepath() {
        return filePath;
    }

    @Override
    public void setFilePath(String path) {
        filePath = path;
        updateFilePercent();
    }

    @Override
    public long getFilesize() {
        return fileSize;
    }

    @Override
    public void setFileSize(long size) {
        fileSize = size;
        updateFilePercent();
    }

    @Override
    public String getData() {
        return messageContent;
    }

    @Override
    public void setData(String data) {
        messageContent = data;
    }

    @Override
    public String getFilename() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int getImageWidth() {
        return imageWidth;
    }

    @Override
    public void setImageWidth(int width) {
        imageWidth = width;
    }

    @Override
    public int getImageHeight() {
        return imageHeight;
    }

    @Override
    public void setImageHeight(int height) {
        imageHeight = height;
    }

    @Override
    public String getContact() {
        return sendContact;
    }

    @Override
    public void setContact(String contact) {
        sendContact = contact;
    }

    @Override
    public List<String> getReceiver() {
        return receiverList;
    }

    @Override
    public void setReceiver(List<String> contact) {
        receiverList = contact;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public long getMsgThreadId() {
        return threadId;
    }

    @Override
    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    @Override
    public String getThumbpath() {
        return thumbPath;
    }

    @Override
    public void setThumbPath(String path) {
        thumbPath = path;
    }

    @Override
    public boolean isBurnAfterMsg() {
        return isBurnAfter;
    }

    @Override
    public void setIsBurnAfter(boolean isBurnAfter) {
        this.isBurnAfter = isBurnAfter;
    }

    @Override
    public String getExtraInfo() {
        return extraInfo;
    }

    @Override
    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    @Override
    public double getLatitude() {
        return latitute;
    }

    @Override
    public void setLatitute(double latitute) {
        this.latitute = latitute;
    }


    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    @Override
    public String getLocationLabel() {
        return loacationLabel;
    }

    @Override
    public void setLocationLabel(String label) {
        this.loacationLabel = label;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String getConversationId() {
        return conversationId;
    }

    @Override
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    @Override
    public String getFileUrlPath() {
        return fileUrlPath;
    }

    @Override
    public void setFileUrlPath(String filePath) {
        fileUrlPath = filePath;
    }

    @Override
    public String getThumbUrlPath() {
        return thumbUrlPath;
    }

    @Override
    public void setThumbUrlPath(String filePath) {
        thumbUrlPath = filePath;
    }

    @Override
    public String getPacketId() {
        return packetId;
    }

    @Override
    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    @Override
    public String getItemTitle() {
        return title;
    }

    @Override
    public void setItemTitle(String title) {
        this.title = title;
    }

    @Override
    public void setMsgCreateTime(long time) {
        msgCreateTime = time;
    }

    @Override
    public long getMsgCreateTime() {
        return msgCreateTime;
    }

}
