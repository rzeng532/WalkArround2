/**
 * Copyright (C) 2014-2016 Richard All rights reserved
 */
package com.example.walkarround.message.manager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.example.walkarround.message.activity.BuildMessageActivity;
import com.example.walkarround.message.model.ChatMsgAndSMSReturn;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.model.MessageRecipientInfo;
import com.example.walkarround.message.model.MessageSessionBaseModel;
import com.example.walkarround.message.util.MessageComparator;
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.message.util.MessageConstant.MessageState;
import com.example.walkarround.message.util.MsgBroadcastConstants;
import com.example.walkarround.util.AsyncTaskListener;
import com.example.walkarround.util.Logger;

import java.util.*;

/**
 * TODO: description
 * Date: 2016-02-14
 *
 * @author Administrator
 */
public class WalkArroundMsgManager {
    private Logger logger = Logger.getLogger(WalkArroundMsgManager.class.getSimpleName());

    private AVIMClient mAvimClient;
    private AVIMConversation mImConversation;
    private MessageAbstractManger mMsgManager;
    public static final int PAGE_COUNT = 15;
    private static WalkArroundMsgManager mInstance;
    private Context mContext = null;
    private HashMap<String, AVIMConversation> mConversationMap = new HashMap<>();

    private WalkArroundMsgManager(Context context) {
        this.mContext = context;
    }

    public static WalkArroundMsgManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (WalkArroundMsgManager.class) {
                if (mInstance == null) {
                    mInstance = new WalkArroundMsgManager(context);
                    mInstance.mMsgManager = new LittleCMsgManager(context);
                }
            }
        }

        return mInstance;
    }

    public void open(String clientId, AVIMClientCallback callback) {
        mInstance.mAvimClient = AVIMClient.getInstance(clientId);
        mInstance.mAvimClient.open(callback);
    }

    public AVIMClient getMsgClient() {
        return mInstance.mAvimClient;
    }

    public String getClientId() {
        AVUser avUser = AVUser.getCurrentUser();
        return avUser == null ? null : avUser.getObjectId();
    }

    /*
     * User should set a conversation object before start it.
     */
    //TODO: set / get conversation should add on abstract class.
    public void setConversation() {
        if (mMsgManager != null) {
            ((LittleCMsgManager) mMsgManager).setConversation(mInstance.mImConversation);
        }
    }

    public void clearCurConversation() {
        if (mMsgManager != null) {
            mInstance.mImConversation = null;
            ((LittleCMsgManager) mMsgManager).setConversation(mInstance.mImConversation);
        }
    }

    public void resetConversation() {
        if (mMsgManager != null) {
            ((LittleCMsgManager) mMsgManager).setConversation(null);
        }
    }

    public void sayHello(String receiver, String content) {

        if(TextUtils.isEmpty(receiver) || TextUtils.isEmpty(content)) {
            return;
        }

        List<String> recipient = new ArrayList<String>();
        recipient.add(receiver);

        long threadId = mInstance.mMsgManager.getConversationId(MessageConstant.ChatType.CHAT_TYPE_ONE2ONE, recipient);
        if (threadId > 0) {
            //If there is already a conversation, we just skip it.
            return;
        }

        //Check current conversation is correct or not
        if(mInstance.mImConversation != null) {
            List<String> members = mInstance.mImConversation.getMembers();
            int i = 0;
            for (String mem : members) {
                if(mem.equalsIgnoreCase(receiver)) {
                    i++;
                    break;
                }
            }

            //If current conversation do NOT contain user object ID,we set current conversation as NULL to trigger getConversation from cache / net.
            if(i == 0) {
                clearCurConversation();
            }
        }

        //We need a listener here to send Hello while we get conversation.
        //WalkArroundMsgManager.getInstance(mContext).getConversation(receiver, null);
        sendTextMsg(receiver, content);
    }

    /*
     * Send text message
     */
    public long sendTextMsg(String receiver, String content) {

        //Check environment.
        if (mInstance == null || mInstance.mMsgManager == null) {
            return -1;
        }

        //Generate a recipient information.
        MessageRecipientInfo recipientInfo = new MessageRecipientInfo();

        //Set chat type. 1v1 or Group chat
        recipientInfo.setConversationType(MessageConstant.ChatType.CHAT_TYPE_ONE2ONE);

        //Set receivers.
        List<String> recipient = new ArrayList<String>();
        recipient.add(receiver);
        recipientInfo.setRecipientList(recipient);

        //Set thread Id.
        long threadId = mInstance.mMsgManager.getConversationId(recipientInfo.getConversationType(), recipient);
        if (threadId < 0) {
            threadId = mInstance.mMsgManager.createConversationId(recipientInfo.getConversationType(), recipient);
        }
        recipientInfo.setThreadId(threadId);

        //Send message via msg manager.
        return sendTextMsg(recipientInfo, content);
    }

    public long sendTextMsg(MessageRecipientInfo receiver, String content) {
        try {
            return mInstance.mMsgManager.sendPlainMessage(receiver, content, false, 0, null);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void getConversation(final String memberId, AsyncTaskListener listener) {

        //Find cache conversation at first.
        if(mConversationMap.size() > 0 && mConversationMap.containsKey(memberId)) {
            mInstance.mImConversation = mInstance.mConversationMap.get(memberId);
            setConversation();
            if(listener != null) {
                listener.onSuccess(null);
            }
            return;
        }

        //Find it from server.
        final AVIMClient client = getMsgClient();
        AVIMConversationQuery conversationQuery = client.getQuery();
        conversationQuery.withMembers(Arrays.asList(memberId), true);
        //conversationQuery.whereEqualTo("customConversationType",1);

        //Clear conversation before there is result from server.
        resetConversation();
        conversationQuery.findInBackground(new AVIMConversationQueryCallback() {
            @Override
            public void done(List<AVIMConversation> list, AVIMException e) {
                if (e == null) {
                    if (null != list && list.size() > 0) {
                        //Set current conversation. We just need the first one if list size > 1.
                        mInstance.mImConversation = list.get(0);
                        mInstance.mConversationMap.put(memberId, mInstance.mImConversation);
                        setConversation();
                        listener.onSuccess(null);
                    } else {
                        mInstance.mAvimClient.createConversation(Arrays.asList(memberId), null, null, false, new AVIMConversationCreatedCallback() {
                            @Override
                            public void done(AVIMConversation avimConversation, AVIMException e) {
                                if (e == null) {
                                    mInstance.mImConversation = avimConversation;
                                    mInstance.mConversationMap.put(memberId, mInstance.mImConversation);
                                    setConversation();
                                    if(listener != null) {
                                        listener.onSuccess(null);
                                    }
                                } else {
                                    if(listener != null) {
                                        listener.onFailed(e);
                                    }
                                }
                            }
                        });
                    }
                } else {
                    if(listener != null) {
                        listener.onFailed(e);
                    }
                }
            }
        });
    }

    public long getConversationId(int chatType, List<String> numbers) {
        if (mInstance == null || mInstance.mMsgManager == null) {
            return -1;
        }

        return mInstance.mMsgManager.getConversationId(chatType, numbers);
    }

    public long createConversationId(int chatType, List<String> numbers) {
        if (mInstance == null || mInstance.mMsgManager == null) {
            return -1;
        }

        return mInstance.mMsgManager.createConversationId(chatType, numbers);
    }

    public ChatMsgBaseInfo getMessageById(long messageId) {
        if (mInstance == null || mInstance.mMsgManager == null) {
            return null;
        }
        try {
            return mInstance.mMsgManager.getMessageById(messageId);
        } catch (Exception e) {
            logger.e("getMessageById Exception: " + e.getMessage());
        }
        return null;
    }

    public void delMessageById(long messageId) {
        try {
            mInstance.mMsgManager.deleteMessage(messageId);
        } catch (Exception e) {
            logger.e("delMsgById Exception: " + e.getMessage());
        }
    }

    /**
     * 下载视频，支持断点续传
     */
    public void acceptFile(ChatMsgBaseInfo message, boolean isCollectMsg) {
        try {
            mInstance.mMsgManager.acceptFile(message, isCollectMsg);
        } catch (Exception e) {
            logger.e("acceptFile Exception: " + e.getMessage());
        }
    }

    /**
     * 获取与某一对象的对话消息列表
     *
     * @param context
     * @param thread_id
     * @param number
     * @param beginChatId
     * @param beginSmsId
     * @return
     */
    public ChatMsgAndSMSReturn getChatMsgList(Context context, long thread_id, String number, long beginChatId,
                                              long beginSmsId, boolean isUpBeginId) {
        List<ChatMsgBaseInfo> chatList = new ArrayList<ChatMsgBaseInfo>();
        if (thread_id > 0) {
            try {
                chatList = mInstance.mMsgManager.getMessageList(thread_id, beginChatId, isUpBeginId, PAGE_COUNT);
            } catch (Exception e) {
                logger.e("getChatMsgAndSMS Exception: " + e.getMessage());
            }
        }
        if (chatList == null) {
            chatList = new ArrayList<ChatMsgBaseInfo>();
        }

        // 排序，需ChatMessage继承Comparable接口
        if (chatList.size() > 0) {
            Collections.sort(chatList, new MessageComparator(MessageComparator.TIME_ASC));
        }

        // 删除多余项
        if (chatList.size() > PAGE_COUNT) {
            chatList = chatList.subList(chatList.size() - PAGE_COUNT, chatList.size());
        }

        // 获取lastId
        long lastChatId = beginChatId;
        long lastSmsId = beginSmsId;
        int start = isUpBeginId ? chatList.size() - 1 : 0;
        int end = isUpBeginId ? 0 : chatList.size() - 1;
        int step = isUpBeginId ? -1 : 1;
        for (int i = start; isUpBeginId ? i >= end : i <= end; i += step) {
            ChatMsgBaseInfo chatMessage = chatList.get(i);
            lastChatId = chatMessage.getMsgId();
        }

        return new ChatMsgAndSMSReturn(lastChatId, lastSmsId, chatList);
    }

    public void batchSetMsgRead(List<Long> threadIdList) {
        try {
            mInstance.mMsgManager.batchRemoveUnreadMessage(threadIdList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ChatMsgBaseInfo getDraftMessage(long threadId) {
        try {
            return mInstance.mMsgManager.getDraftMessage(threadId);
        } catch (Exception e) {
            logger.e("getDraftMessage Exception: " + e.getMessage());
        }
        return null;
    }

    /**
     * 发送视频(一对一、一对多和群聊模式) 不带阅后即焚功能
     *
     * @param recipientInfo 消息会话id
     * @param videoFilePath 图片文件路径
     * @param videoLen      音频录音时间，接收者会显示此时间
     * @param isRecord      是否拍摄，true == 拍摄文件，false == 本地文件
     */
    public long sendVideoFile(MessageRecipientInfo recipientInfo, String videoFilePath, int videoLen, boolean isRecord) {

        return sendVideoFile(recipientInfo, videoFilePath, videoLen, false, 0, isRecord);
    }

    /**
     * 发送视频(一对一、一对多和群聊模式)
     *
     * @param recipientInfo 消息会话id
     * @param videoFilePath 视频文件路径
     * @param isBurnAfter   是否阅后即焚
     * @param burnTime      阅后即焚开启后接收方的阅读时间
     * @param isRecord      是否拍摄，true == 拍摄文件，false == 本地文件
     */
    public long sendVideoFile(MessageRecipientInfo recipientInfo, String videoFilePath, int videoLen,
                              boolean isBurnAfter, int burnTime, boolean isRecord) {
        try {
            return mInstance.mMsgManager.sendVideoFile(recipientInfo, videoFilePath, videoLen, isBurnAfter, burnTime, isRecord);
        } catch (Exception e) {
            logger.e("sendVideoFile Exception: " + e.getMessage());
        }

        return -1;
    }

    public boolean isTopMessage(long threadId) {
        try {
            return mInstance.mMsgManager.isTopMsgByThreadId(threadId);
        } catch (Exception e) {
            logger.e("setOrCancelTopMessage Exception: " + e.getMessage());
        }
        return false;
    }

    /**
     * 删除消息
     *
     * @param messageList 消息Id
     */
    public void deleteMessages(Context context, List<ChatMsgBaseInfo> messageList) {
        try {
            for (ChatMsgBaseInfo message : messageList) {
                mInstance.mMsgManager.deleteMessage(message.getMsgId());
            }
        } catch (Exception e) {
            logger.e("deleteMessages Exception: " + e.getMessage());
        }
    }

    /**
     * 删除消息
     *
     * @param messageId 消息Id
     */
    public void deleteRCSMessagesById(long messageId) {
        try {
            mInstance.mMsgManager.deleteMessage(messageId);
        } catch (Exception e) {
            logger.e("deleteMessages Exception: " + e.getMessage());
        }
    }

    /**
     * 删除一整个会话的消息
     *
     * @param threadId 消息Id
     */
    public void deleteThreadMessages(Context context, long threadId, String number) {
        try {
            mInstance.mMsgManager.deleteThreadMessage(threadId);
        } catch (Exception e) {
            logger.e("deleteThreadMessages Exception: " + e.getMessage());
        }
    }

    /**
     * 将当前消息置顶或取消置顶
     *
     * @param threadId
     * @param isTop
     */
    public void setOrCancelTopMessage(Context context, long threadId, String number, boolean isTop) {
        try {
            mInstance.mMsgManager.setOrCancelTopMessage(threadId, isTop);
            if (!isTop && !TextUtils.isEmpty(number)) {
                List<ChatMsgBaseInfo> chatMsgList = mInstance.mMsgManager.getMessageList(threadId, 0, false, 1);
                if (chatMsgList == null || chatMsgList.size() == 0) {
                    mInstance.mMsgManager.deleteConversation(threadId);
                }
            }
        } catch (Exception e) {
            logger.e("setOrCancelTopMessage Exception: " + e.getMessage());
        }
    }

    /**
     * 设置消息为已读状态
     *
     * @return
     */
    public int setMessageRead(long messageId) {
        try {
            return mInstance.mMsgManager.setMessageRead(messageId);
        } catch (Exception Exception) {
            logger.e("setMessageRead Exception: " + Exception.getMessage());
        }
        return -1;
    }

    /**
     * 发送位置信息(一对一、一对多和群聊模式)
     *
     * @param recipientInfo 消息会话id
     */
    public long sendLocation(MessageRecipientInfo recipientInfo, double Lat, double lng, String address,
                             String imagePath) {
        return sendLocation(recipientInfo, Lat, lng, address, imagePath, null);
    }

    /**
     * 发送位置信息(一对一、一对多和群聊模式)
     *
     * @param recipientInfo 消息会话id
     */
    public long sendLocation(MessageRecipientInfo recipientInfo, double Lat, double lng, String address,
                             String imagePath, String extraInfo) {
        try {
            return mInstance.mMsgManager.sendLocationInfo(recipientInfo, Lat, lng, address, imagePath, extraInfo);
        } catch (Exception exception) {
            logger.e("sendLocation Exception: " + exception.getMessage());
        }
        return -1;
    }

    /**
     * 发送语音(一对一、一对多和群聊模式) 不带阅后即焚功能
     *
     * @param recipientInfo 消息会话id
     * @param audioFilePath 图片文件路径
     * @param recordTime    音频录音时间，接收者会显示此时间
     * @param isRecord      是否录音，true == 录音文件，false == 本地文件
     */
    public long sendAudioFile(MessageRecipientInfo recipientInfo, String audioFilePath, int recordTime, boolean isRecord) {

        return sendAudioFile(recipientInfo, audioFilePath, recordTime, false, 0, isRecord);
    }

    /**
     * 发送语音(一对一、一对多和群聊模式)
     *
     * @param recipientInfo 消息会话id
     * @param audioFilePath 图片文件路径
     * @param isBurnAfter   是否阅后即焚
     * @param burnTime      阅后即焚开启后接收方的阅读时间
     * @param isRecord      是否录音，true == 录音文件，false == 本地文件
     */
    public long sendAudioFile(MessageRecipientInfo recipientInfo, String audioFilePath, int recordTime,
                              boolean isBurnAfter, int burnTime, boolean isRecord) {
        try {
            return mInstance.mMsgManager.sendAudioFile(recipientInfo, audioFilePath, recordTime, isBurnAfter, burnTime, isRecord);
        } catch (Exception e) {
            logger.e("sendAudioFile Exception: " + e.getMessage());
        }

        return -1;
    }

    /**
     * 会话是否存在
     *
     * @param threadId 会话id
     * @return
     */
    public boolean isConversationExist(long threadId) {
        try {
            return mInstance.mMsgManager.isConversationExist(threadId);
        } catch (Exception e) {
            logger.e("isConversationExist Exception: " + e.getMessage());
        }
        return false;
    }

    /**
     * 重发消息
     */
    public void resendMessage(ChatMsgBaseInfo message) {
        try {
            mInstance.mMsgManager.resendMessage(message.getGroupId(), message.getMsgId());
        } catch (Exception e) {
            logger.e("resendMessage Exception: " + e.getMessage());
        }
    }

    /**
     * 发送图片(一对一、一对多和群聊模式) 不带阅后即焚功能
     *
     * @param recipientInfo 消息会话id
     * @param imageFilePath 图片文件路径
     */
    public long sendImageFiles(MessageRecipientInfo recipientInfo, String imageFilePath, int quality) {
        return sendImageFiles(recipientInfo, imageFilePath, false, 0, quality);
    }

    /**
     * 发送图片(一对一、一对多和群聊模式)
     *
     * @param recipientInfo 消息会话id
     * @param imageFilePath 图片文件路径
     * @param isBurnAfter   是否阅后即焚
     * @param burnTime      阅后即焚开启后接收方的阅读时间
     */
    public long sendImageFiles(MessageRecipientInfo recipientInfo, String imageFilePath, boolean isBurnAfter,
                               int burnTime) {
        return sendImageFiles(recipientInfo, imageFilePath, isBurnAfter, burnTime, MessageConstant.MSG_IMAGE_COMPRESS_QUALITY);
    }

    public long sendImageFiles(MessageRecipientInfo recipientInfo, String imageFilePath, boolean isBurnAfter,
                               int burnTime, int maxSendSize) {
        return sendImageFiles(recipientInfo, imageFilePath, isBurnAfter, burnTime, maxSendSize, null);
    }

    public long sendImageFiles(MessageRecipientInfo recipientInfo, String imageFilePath, boolean isBurnAfter,
                               int burnTime, int maxSendSize, String extraInfo) {
        try {
            return mInstance.mMsgManager.sendImageFiles(recipientInfo, imageFilePath, isBurnAfter, burnTime, maxSendSize, extraInfo);
        } catch (Exception e) {
            logger.e("sendPlainText Exception: " + e.getMessage());
        }

        return -1;
    }

    /**
     * 更新草稿消息
     *
     * @return 是否成功
     */
    public long updateDraftMessage(MessageRecipientInfo recipientInfo, String message) {
        try {
            return mInstance.mMsgManager.updateDraftMessage(recipientInfo, message);
        } catch (Exception e) {
            logger.e("updateDraftMessage Exception: " + e.getMessage());
        }
        return -1;
    }

    public void setMsgReadByThreadId(long threadId) {
        if (threadId != -2) {
            String strThreadId = String.valueOf(threadId);

            try {
                mInstance.mMsgManager.removeUnreadMessageByThreadId(strThreadId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取指定时间上下的消息
     *
     * @param context
     * @param thread_id
     * @param number
     * @return
     */
    public ChatMsgAndSMSReturn getChatMsgListByTime(Context context, long thread_id, String number,
                                                    long msgId, int msgFromType) {
        List<ChatMsgBaseInfo> chatList = new ArrayList<ChatMsgBaseInfo>();
        int msgCount = PAGE_COUNT * 2;
        ChatMsgBaseInfo msg = null;
        msg = getMessageById(msgId);

        if ((msg == null) || (msg.getMsgState() == MessageState.MSG_STATE_SEND_DRAFT)) {
            return getChatMsgList(context, thread_id, number, 0, 0, true);
        }
        long time = msg.getTime();
        if (thread_id > 0) {
            try {
                chatList = mInstance.mMsgManager.getMessageList(thread_id, time, msgCount);
            } catch (Exception e) {
                logger.e("getChatMsgAndSMS Exception: " + e.getMessage());
            }
        }
        if (chatList == null) {
            chatList = new ArrayList<ChatMsgBaseInfo>();
        }

        // 排序，需ChatMessage继承Comparable接口
        if (chatList != null && chatList.size() > 0) {
            Collections.sort(chatList, new MessageComparator(MessageComparator.TIME_ASC));
        }

        // 删除多余项
        int searchMsgPos = getSearchMsgPos(chatList, msgId, msgFromType);
        if (chatList.size() > msgCount + 1) {
            int startPos = searchMsgPos - msgCount / 2;
            startPos = startPos >= 0 ? searchMsgPos : 0;
            int endPos = startPos + msgCount;
            endPos = endPos > chatList.size() ? chatList.size() : endPos;
            chatList = chatList.subList(startPos, endPos);
            searchMsgPos -= startPos;
        }

        // 获取lastId
        long lastChatId = 0;
        long lastSmsId = 0;
        for (ChatMsgBaseInfo chatMessage : chatList) {
            lastChatId = chatMessage.getMsgId();
        }
        ChatMsgAndSMSReturn returnList = new ChatMsgAndSMSReturn(lastChatId, lastSmsId, chatList);
        returnList.setSearchMsgPos(searchMsgPos);
        return returnList;
    }

    /**
     * 定位消息位置
     *
     * @param chatList
     * @param msgId
     * @param msgFromType
     * @return
     */
    private int getSearchMsgPos(List<ChatMsgBaseInfo> chatList, long msgId, int msgFromType) {
        int searchMsgPos = -1;
        for (int i = 0; i < chatList.size(); i++) {
            ChatMsgBaseInfo msgBaseInfo = chatList.get(i);
            if (msgBaseInfo.getMsgId() == msgId) {
                if (msgFromType == BuildMessageActivity.MSG_FROM_TYPE_RCS) {
                    searchMsgPos = i;
                    break;
                }
            }
        }
        return searchMsgPos;
    }

    /**
     * 获取会话消息
     *
     * @param offset 偏移个数
     * @param count  获取个数
     * @return
     */
    public List<MessageSessionBaseModel> getConversationList(boolean isNotify, int offset, int count) {
        try {
            return mInstance.mMsgManager.getMessageSessionList(isNotify, offset, count);
        } catch (Exception e) {
            logger.e("getConversationList Exception:" + e.getMessage());
        }

        return null;
    }

    public boolean markConversationRead(Context context, MessageSessionBaseModel model) {
        int count = 0;
        try {
            List<Long> threadList = new ArrayList<Long>();
            threadList.add(model.getThreadId());
            count = mInstance.mMsgManager.batchRemoveUnreadMessage(threadList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (count > 0) {
            return true;
        }

        return false;
    }

    /**
     * 删除会话（如果没有SMS消息）
     *
     * @param context
     * @param model
     * @return
     */
    public long removeConversation(Context context, MessageSessionBaseModel model) {
        try {
            List<Long> list = new ArrayList<Long>();
            list.add(model.getThreadId());
            mInstance.mMsgManager.batchDeleteThreadMessage(list);
        } catch (Exception e) {
            logger.e("removeConversation Exception:" + e.getMessage());
            return model.getThreadId();
        }
        return -1;
    }

    public List<ChatMsgBaseInfo> searchSmsAndMsgByKey(Context context, String key, boolean isNotify, Map<String, String> numMap) {
        List<ChatMsgBaseInfo> list = new ArrayList<ChatMsgBaseInfo>();

        if (numMap == null || numMap.size() == 0) {
            if (!isNotify) {
                list.addAll(mInstance.mMsgManager.searchMsgByKey(key, null));
            }
        } else {
            List<String> exnumlist = new ArrayList<String>();
            for (Map.Entry<String, String> entry : numMap.entrySet()) {
                exnumlist.add(entry.getValue());
            }

            if (!isNotify) {
                list.addAll(mInstance.mMsgManager.searchMsgByKey(key, exnumlist));
            }
        }

        Collections.sort(list, new MessageComparator(MessageComparator.TIME_DESC));

        return list;
    }

    public Map<String, String> queryMsgSession(Context context, String key) {
        Map<String, String> map = mInstance.mMsgManager.queryMessageSession(key);
        return map;
    }

    /**
     * 获取最新的一条通知消息内容
     *
     * @return
     */
    public MessageSessionBaseModel getLatestNotifySession() {
        try {
            return mInstance.mMsgManager.getLatestNotifySession();
        } catch (Exception e) {
            logger.e("getLatestNotifySession Exception:" + e.getMessage());
        }
        return null;
    }

    /**
     * 获取所有未读通知消息数
     *
     * @return
     */
    public int getAllNotifyMsgUnreadCount() {
        try {
            return mInstance.mMsgManager.getAllNotifyMsgUnreadCount();
        } catch (Exception e) {
            logger.e("getAllNotifyMsgUnreadCount Exception:" + e.getMessage());
        }
        return 0;
    }

    /**
     * 根据会话id获取会话内容
     *
     * @param threadId 会话id
     * @return
     */
    public MessageSessionBaseModel getSessionByThreadId(long threadId) {
        if (threadId > 0) {
            return getLatestMsgSessionByThreadId(threadId);
        }
        return null;
    }

    public int getAllUnreadCount() {
        try {
            return mInstance.mMsgManager.getAllUnreadCount();
        } catch (Exception e) {
            logger.e("getAllNotifyMsgUnreadCount Exception:" + e.getMessage());
        }
        return 0;
    }

    public int getMsgUnreadCount(String strThreadId) {
        try {
            return mInstance.mMsgManager.getMsgUnreadCount(strThreadId);
        } catch (Exception e) {
            logger.e("getAllNotifyMsgUnreadCount Exception:" + e.getMessage());
        }
        return 0;
    }

    private MessageSessionBaseModel getLatestMsgSessionByThreadId(long threadId) {
        try {
            return mInstance.mMsgManager.getLatestSessionById(threadId);
        } catch (Exception e) {
            logger.e("getLatestMsgSessionByThreadId Exception:" + e.getMessage());
        }
        return null;
    }

    /**
     * 设置所有发送中的消息状态为发送失败
     */
    public void setAllSendingMsgStatusFail() {
        try {
            mInstance.mMsgManager.setAllSendingMsgStatusFail();
        } catch (Exception e) {
            logger.e("setAllSendingMsgStatusFail Exception: " + e.getMessage());
        }
    }

    public MessageRecipientInfo abstractReceiptInfo(List<String> addrList, int chatType) throws Exception {
        MessageRecipientInfo recipientInfo = new MessageRecipientInfo();
        ArrayList<String> numList = new ArrayList<String>();
        numList.addAll(addrList);
        recipientInfo.setRecipientList(numList);
        recipientInfo.setConversationType(chatType);
        long threadId = 0;
        List<String> numbers = new ArrayList<String>();
        numbers.addAll(addrList);
        threadId = mInstance.mMsgManager.getConversationId(recipientInfo.getConversationType(), numbers);
        if (threadId <= 0) {
            // 创建threadId
            threadId = mInstance.mMsgManager.createConversationId(recipientInfo.getConversationType(), numList);
        }
        recipientInfo.setThreadId(threadId);

        return recipientInfo;
    }

    public Uri saveChatmsg(ChatMsgBaseInfo msg) {
        return mInstance.mMsgManager.saveMessage(msg);
    }

    public void addMsgUnreadCountByThreadId(long threadId) {
        if (threadId == -2) {
            return;
        }
        try {
            mInstance.mMsgManager.addMsgUnreadCountByThreadId(threadId);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 消息下载成功发广播通知UI
     *
     * @param msgInfo
     * @param isSuccess
     */
    public void onLoadMsgResult(ChatMsgBaseInfo msgInfo, List<Long> idList,boolean isSuccess) {
        // 下载消息成功
        if (isSuccess) {
            String action = msgInfo.getChatType() == MessageConstant.ChatType.CHAT_TYPE_GROUP ?
                    MsgBroadcastConstants.ACTION_GROUP_MESSAGE_NEW_RECEIVED
                    : MsgBroadcastConstants.ACTION_MESSAGE_NEW_RECEIVED;
            Intent intent = new Intent();
            intent.setAction(action);
            intent.putExtra(MsgBroadcastConstants.BC_VAR_MSG_ID, msgInfo.getMsgId());
            intent.putExtra(MsgBroadcastConstants.BC_VAR_THREAD_ID, msgInfo.getMsgThreadId());
            intent.putExtra(MsgBroadcastConstants.BC_VAR_MSG_STATUS, msgInfo.getMsgState());
            intent.putExtra(MsgBroadcastConstants.BC_VAR_MSG_TYPE, msgInfo.getMsgType());
            intent.putExtra(MsgBroadcastConstants.BC_VAR_MSG_EXTRA, msgInfo.getExtraInfo());
            intent.putExtra(MsgBroadcastConstants.BC_VAR_CONTACT, msgInfo.getContact());
            intent.putExtra(MsgBroadcastConstants.BC_VAR_GROUP_ID, msgInfo.getGroupId());
            intent.putExtra(MsgBroadcastConstants.BC_VAR_MSG_COUNT, msgInfo.getDownPercent());
            if (idList != null && idList.size() > 0) {
                long[] ids = new long[idList.size()];
                for (int index = 0; index < idList.size(); index++) {
                    ids[index] = idList.get(index);
                }
                intent.putExtra(MsgBroadcastConstants.BC_VAR_MSG_ID_LIST, ids);
            }
            mContext.sendBroadcast(intent);
        }
    }

}
