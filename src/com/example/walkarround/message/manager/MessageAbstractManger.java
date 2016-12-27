package com.example.walkarround.message.manager;

import android.net.Uri;
import com.example.walkarround.message.model.ChatMessageInfo;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.model.MessageRecipientInfo;
import com.example.walkarround.message.model.MessageSessionBaseModel;
import com.example.walkarround.util.Logger;

import java.util.List;
import java.util.Map;

/**
 * 小溪消息收发管理
 */
public abstract class MessageAbstractManger {

    private Logger logger = Logger.getLogger(MessageAbstractManger.class.getSimpleName());

    /**
     * 发送纯文本短信
     *
     * @param recipientInfo
     * @param text
     * @param isBurnAfter
     * @param burnTime
     */
    public abstract long sendPlainMessage(MessageRecipientInfo recipientInfo, String text, boolean isBurnAfter,
                                          int burnTime, String extraInfo) throws Exception;

    /**
     * 发送定时消息
     *
     * @param recipientInfo
     * @param text
     */
    public abstract long sendTimePlainMessage(MessageRecipientInfo recipientInfo, String text, long time) throws Exception;

    /**
     * 发送联系人名片
     *
     * @param recipientInfo
     * @param vcardFilePath
     */
    //public abstract long sendVcardFile(MessageRecipientInfo recipientInfo, String vcardFilePath, String vCardDisplayName)
    //        throws Exception;

    /**
     * 发送图片(包括照片)
     *
     * @param recipientInfo
     * @param imageFilePath
     * @param isBurnAfter
     *            ,
     * @param burnTime
     * @throws Exception
     */
    public abstract long sendImageFiles(MessageRecipientInfo recipientInfo, String imageFilePath, boolean isBurnAfter,
                                        int burnTime, int maxSendSize, String extraInfo) throws Exception;

    /**
     * 发送语音
     *
     * @param recipientInfo
     * @param audioFilePath
     * @param recordTime
     *            本地记录最长时间为 180 seconds
     * @param isBurnAfter
     * @param burnTime
     * @throws Exception
     */
    public abstract long sendAudioFile(MessageRecipientInfo recipientInfo, String audioFilePath, int recordTime,
                                       boolean isBurnAfter, int burnTime, boolean isRecord) throws Exception;

    /**
     * 发送视频
     *
     * @param recipientInfo
     * @param videoFilePath
     * @param videoLen
     *            最长文件播放时长 90秒
     * @param isBurnAfter
     * @param burnTime
     * @throws Exception
     */
    public abstract long sendVideoFile(MessageRecipientInfo recipientInfo, String videoFilePath, int videoLen,
                                       boolean isBurnAfter, int burnTime, boolean isRecord) throws Exception;

    /**
     * 发送位置信息
     *
     * @param recipientInfo
     * @param lat
     *            经度
     * @param lng
     *            纬度
     * @param address
     *            位置信息描述
     * @throws Exception
     */
    public abstract long sendLocationInfo(MessageRecipientInfo recipientInfo, double lat, double lng, String address,
                                          String imagePath, String extraInfo) throws Exception;

    /**
     * 获取与某一对象的消息记录
     *
     * @param thread_id
     * @param beginChatId
     * @param isBeforeChatId
     * @param count
     * @return
     * @throws Exception
     */
    public abstract List<ChatMsgBaseInfo> getMessageList(long thread_id, long beginChatId, boolean isBeforeChatId,
                                                         int count) throws Exception;

    /**
     * 获取指定时间上下count条的数据
     *
     * @param thread_id
     * @param time
     * @param count
     * @return
     * @throws Exception
     */
    public List<ChatMsgBaseInfo> getMessageList(long thread_id, long time,
                                                int count) throws Exception {
        return null;
    }

    /**
     * 删除消息
     *
     * @param messageId
     * @throws Exception
     */
    public abstract int deleteMessage(long messageId) throws Exception;

    /**
     * 删除消息
     *
     * @param threadId
     * @throws Exception
     */
    public abstract int deleteThreadMessage(long threadId) throws Exception;

    public abstract void deleteConversation(long threadId)  throws Exception;

    public abstract int deleteMappingConversation()  throws Exception;

    /**
     * 获取消息内容
     *
     * @param messageId
     * @return
     * @throws Exception
     */
    public abstract ChatMsgBaseInfo getMessageById(long messageId) throws Exception;

    /**
     * 获取草稿信息
     *
     * @throws Exception
     */
    public abstract ChatMsgBaseInfo getDraftMessage(long threadId) throws Exception;

    /**
     * 获取草稿信息
     *
     * @throws Exception
     */
    public abstract long updateDraftMessage(MessageRecipientInfo recipientInfo, String message) throws Exception;

    public abstract void setAllSendingMsgStatusFail() throws Exception;

    public abstract boolean interruptDownloadVideo(ChatMsgBaseInfo message) throws Exception;

    public abstract void acceptFile(ChatMsgBaseInfo message, boolean isCollectMsg) throws Exception;

    /**
     * 阅后即焚
     *
     * @throws Exception
     */
    public abstract void burnMessage(long messageId) throws Exception;

    /**
     * 重发消息
     *
     * @throws Exception
     */
    public abstract void resendMessage(String groupId, long messageId) throws Exception;

    /**
     * 设置消息为已读状态
     *
     * @return
     * @throws Exception
     */
    public abstract int setMessageRead(long messageId) throws Exception;

    /**
     * 设置消息为已读状态
     *
     * @return
     * @throws Exception
     */
    public abstract boolean isMessageAttachDownload(ChatMsgBaseInfo message) throws Exception;

    /**
     * 将当前消息置顶或取消置顶
     *
     * @param threadId
     * @param isTop
     */
    public abstract void setOrCancelTopMessage(long threadId, boolean isTop) throws Exception;

    /**
     * 是否置顶消息
     *
     * @param thread
     * @return
     * @throws Exception
     */
    public abstract boolean isTopMsgByThreadId(long thread) throws Exception;

    public abstract Map<String, String> queryMessageSession(String key);

    public abstract List<MessageSessionBaseModel> getMessageSessionList(boolean isNotifyMsg, int offset, int count) throws Exception;

    public abstract List<MessageSessionBaseModel> getFriendsSessionList() throws Exception;

    public abstract int delOtherConversionsOverParamTime(long time) throws Exception;

    public abstract int getMsgUnreadCount(String strThreadId) throws Exception;

    public abstract int getUnreadMsgCountByThreadId(String strThreadId) throws Exception;

    public abstract void removeUnreadMessageByThreadId(String strThreadId) throws Exception;

    public abstract int getAllUnreadCount() throws Exception;

    public abstract List<ChatMessageInfo> searchMsgByKey(String key, List<String> numlist);

    public abstract int getConversationId(int chatType, List<String> address);

    public abstract int createConversationId(int chatType, List<String> address);

    public abstract int createConversationId(int chatType, List<String> address, int convStatus);

    public abstract boolean isConversationExist(long threadId) throws Exception;

    public abstract int batchDeleteThreadMessage(List<Long> threadIdList) throws Exception;

    public abstract int batchSetOrCancelTopMessage(List<Long> threadIdList, boolean b) throws Exception;

    public abstract int batchRemoveUnreadMessage(List<Long> threadIdList) throws Exception;

    public abstract void addMsgUnreadCountByThreadId(long threadId) throws Exception;

    public abstract MessageSessionBaseModel getLatestSessionById(long threadId) throws Exception;

    public abstract MessageSessionBaseModel getLatestSessionByMessageId(long messageId) throws Exception;
    
    public abstract boolean isTopMsgByNum(int chatType, List<String> numList)  throws Exception;

    public abstract Uri saveMessage(ChatMsgBaseInfo message);

    //public List<GroupInvitationBaseInfo> getGroupInvitationList(long beginId, int count) throws Exception {
    //    return null;
    //}

    public int getUnreadInvitationCount() throws Exception {
        return 0;
    }

    public int setInvitationRead() throws Exception {
        return 0;
    }

    public List<ChatMsgBaseInfo> getMessageByExtraInfo(String extraKey) throws Exception {
        return null;
    }

    public void updateConversationMsgNotifyFlag() throws Exception {
    }

    public void updateConversationStatus(long threadid, int status) throws Exception {
    }

    public int getIntentConversationStatus(long threadid) throws Exception {
        return 0;
    }

    public void updateConversationColor(long threadid, int color) throws Exception {
    }

    public void updateConversationStatusAndColor(long threadid, int state, int color) throws Exception {
    }


    public int getIntentConversationColor(long threadid) throws Exception {
        return -1;
    }

    public MessageSessionBaseModel getLatestNotifySession() throws Exception {
        return null;
    }

    public int getAllNotifyMsgUnreadCount() throws Exception {
        return 0;
    }

//    public Uri saveMessage(ChatMsgBaseInfo message){
//        return null;
//    }

}
