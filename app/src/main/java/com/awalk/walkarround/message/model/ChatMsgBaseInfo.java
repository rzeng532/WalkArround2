package com.awalk.walkarround.message.model;

import android.text.TextUtils;
import com.awalk.walkarround.message.util.MessageConstant.MessageType;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * 消息内容 Date: 2015-06-02
 * 
 * @author mss
 */
public abstract class ChatMsgBaseInfo implements Serializable{

    private static final long serialVersionUID = -3768067371432944394L;

    public static final int WAIT_DOWNLOAD = 0; // 准备下载的状态
    public static final int DOWNLOADING = 1; // 正在下载/上传，可以被打断的状态
    public static final int LOADED = 2; // 可以去播放的状态
    // 定时短信
    public static final String TIME_SEND_MSG_FLAG = "timeSend";

    private boolean isChecked;

    private int downPercent; 
    private int downStatus = LOADED;
    private String displayName;
    private String namePinyin;
    private String profileKey;

    public abstract void setChatMessage(Object message);

    public abstract int getChatType();

    public abstract void setChatType(int chatType);

    public abstract int getMsgType();

    public abstract void setMsgType(int msgType);

    public abstract int getMsgState();

    public abstract void setMsgState(int msgState);

    public abstract int getSendReceive();

    public abstract void setSendReceive(int type);

    public abstract long getMsgId();

    public abstract void setMsgId(long messageId);

    public abstract boolean getIsRead();

    public abstract void setIsRead(boolean isRead);

    public abstract long getTime();
    public abstract void setTime(long time);

    public abstract long getPlanSendTime();
    public abstract void setPlanSendTime(long time);

    public abstract String getFilepath();
    public abstract void setFilePath(String path);

    public abstract long getFilesize();

    public abstract void setFileSize(long size);

    public abstract String getData();
    public abstract void setData(String data);

    public abstract String getFilename();
    public abstract void setFileName(String fileName);

    public abstract int getImageWidth();
    public abstract void setImageWidth(int width);

    public abstract int getImageHeight();
    public abstract void setImageHeight(int height);

    public abstract String getContact();
    public abstract void setContact(String contact);

    public abstract List<String> getReceiver();
    public abstract void setReceiver(List<String> contact);

    public abstract int getDuration();
    public abstract void setDuration(int duration);

    public abstract long getMsgThreadId();
    public abstract void setThreadId(long threadId);

    public abstract String getThumbpath();
    public abstract void setThumbPath(String path);

    public abstract boolean isBurnAfterMsg();

    public abstract void setIsBurnAfter(boolean isBurnAfter);

    public abstract String getExtraInfo();

    public abstract void setExtraInfo(String extraInfo);

    public abstract double getLatitude();
    public abstract void setLatitute(double latitute);
    public abstract double getLongitude();
    public abstract void setLongitude(double longitude);
    public abstract String getLocationLabel();
    public abstract void setLocationLabel(String label);

    public abstract String getGroupId();

    public abstract void setGroupId(String groupId);

    public abstract String getConversationId();

    public abstract void setConversationId(String conversationId);

    public abstract String getFileUrlPath();

    public abstract void setFileUrlPath(String filePath);

    public abstract String getThumbUrlPath();

    public abstract void setThumbUrlPath(String filePath);

    public abstract String getPacketId();
    public abstract void setPacketId(String packetId);

    public abstract String getItemTitle();
    public abstract void setItemTitle(String packetId);

    public void updateFilePercent() {
        if (!TextUtils.isEmpty(getFilepath())) {
            // 文件被删除了
            File file = new File(getFilepath());
            if (!file.exists()) {
                downStatus = WAIT_DOWNLOAD;
                return;
            }
        }
        if (getMsgType() == MessageType.MSG_TYPE_VIDEO
                || getMsgType() == MessageType.MSG_TYPE_IMAGE) {
            String filePath = getFilepath();
            if (filePath == null || getFilesize() == 0) {
                if (getFileUrlPath() != null) {
                    downStatus = WAIT_DOWNLOAD;
                }
                return;
            }
            File file = new File(filePath);
            long totalSize = getFilesize();
            long doneSize = file.length();
            if (totalSize == doneSize) {
                this.downStatus = LOADED;
                this.downPercent = 100;
            } else {
                this.downPercent = (int) ((doneSize * 1.0 / totalSize) * 100);
                this.downStatus = WAIT_DOWNLOAD;
            }
        }
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public int getDownPercent() {
        return downPercent;
    }

    public void setDownPercent(int downPrecent) {
        this.downPercent = downPrecent;
    }

    public int getDownStatus() {
        return downStatus;
    }

    public void setDownStatus(int downStatus) {
        this.downStatus = downStatus;
    }

    public boolean isTimeSendMsg() {
        return ChatMsgBaseInfo.TIME_SEND_MSG_FLAG.equals(getExtraInfo());
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNamePinyin() {
        return namePinyin;
    }

    public void setNamePinyin(String namePinyin) {
        this.namePinyin = namePinyin;
    }

    public String getProfileKey() {
        return profileKey;
    }

    public void setProfileKey(String profileKey) {
        this.profileKey = profileKey;
    }

    public abstract void setMsgCreateTime(long time);
    public abstract long getMsgCreateTime();

    public abstract boolean isExpire();

    public abstract void setExpire(boolean expire);

}
