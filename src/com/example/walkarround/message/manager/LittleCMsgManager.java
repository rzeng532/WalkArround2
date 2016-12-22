/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.message.manager;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVGeoPoint;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.messages.*;
import com.example.walkarround.base.WalkArroundApp;
import com.example.walkarround.message.model.ChatMessageInfo;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.model.MessageRecipientInfo;
import com.example.walkarround.message.model.MessageSessionBaseModel;
import com.example.walkarround.message.task.FileDownLoadAsyncTask;
import com.example.walkarround.message.util.MessageConstant.MessageState;
import com.example.walkarround.message.util.MessageConstant.MessageType;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.message.util.MsgBroadcastConstants;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.AsyncTaskListener;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.image.CompressPicUtil;
import com.example.walkarround.util.image.ImageUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.walkarround.message.util.MessageConstant.ChatType;
import static com.example.walkarround.message.util.MessageConstant.MessageSendReceive;

/**
 * 小溪消息收发 Date: 2015-06-03
 *
 * @author mss
 */
public class LittleCMsgManager extends MessageAbstractManger {

    private Logger logger = Logger.getLogger(LittleCMsgManager.class.getSimpleName());
    private LittleCDbManager messageDbManager;
    private Context mContext;
    private AVIMConversation mCurConversation;

    public LittleCMsgManager(Context context) {
        mContext = context;
        messageDbManager = new LittleCDbManager(context);
    }

    /*
     * Invoker should set conversation before sending message.
     */
    public void setConversation(AVIMConversation conversation) {
        mCurConversation = conversation;
    }

    public AVIMConversation getConversation() { return mCurConversation; }

    /**
     * 发送纯文本短信
     *
     * @param recipientInfo
     * @param text, invoker should confirm text if NOT null !
     * @param isBurnAfter
     * @param burnTime
     */
    @Override
    public long sendPlainMessage(MessageRecipientInfo recipientInfo, String text, boolean isBurnAfter,
                                 int burnTime, String extraInfo)
            throws Exception {
        if (recipientInfo == null) {
            logger.d("sendVideoFile : illegal parameter, receiver is empty.");
            return -1L;
        }

        AVIMTextMessage msg = new AVIMTextMessage();
        msg.setText(text);

        if(!TextUtils.isEmpty(extraInfo)) {
            //Set extra information
            Map<String, Object> extra = new HashMap<>();
            extra.put(MessageUtil.EXTRA_INFOR_KEY, (Object)extraInfo);
            msg.setAttrs(extra);
        }

        if (TextUtils.isEmpty(text)) {
            sendMessage(recipientInfo, -1, msg, false, extraInfo);
            return -1;
        }
        if (recipientInfo.getThreadId() <= 0) {
            // 还没创建conversation
            long threadId = messageDbManager.getConversationId(recipientInfo.getConversationType(),
                    recipientInfo.getRecipientList());
            if (threadId <= 0) {
                // 创建threadId
                threadId = messageDbManager.createConversationId(recipientInfo.getConversationType(),
                        recipientInfo.getRecipientList());
            }
            recipientInfo.setThreadId(threadId);
        }

        ChatMsgBaseInfo msgInfo = new ChatMessageInfo();
        msgInfo.setContact(ProfileManager.getInstance().getCurUsrObjId());
        msgInfo.setReceiver(recipientInfo.getRecipientList());
        msgInfo.setMsgType(MessageType.MSG_TYPE_TEXT);
        msgInfo.setTime(System.currentTimeMillis());
        msgInfo.setSendReceive(MessageSendReceive.MSG_SEND);
        msgInfo.setMsgState(MessageState.MSG_STATE_SEND_ING);
        msgInfo.setIsRead(true);
        msgInfo.setData(text);
        msgInfo.setThreadId(recipientInfo.getThreadId());
        msgInfo.setChatType(recipientInfo.getConversationType());
        msgInfo.setIsBurnAfter(isBurnAfter);
        if(!TextUtils.isEmpty(extraInfo)) {
            msgInfo.setExtraInfo(extraInfo);
            msgInfo.setMsgType(MessageType.MSG_TYPE_NOTIFICATION);
        }

        Uri insertUri = messageDbManager.addMessage(msgInfo);
        if (insertUri == null) {
            logger.e("insert to message db fail");
            return -1L;
        }
        long id = ContentUris.parseId(insertUri);
        sendMessage(recipientInfo, id, msg, isBurnAfter, extraInfo);
        return id;
    }

    @Override
    public long sendTimePlainMessage(MessageRecipientInfo recipientInfo, String text, long time) throws Exception {
        if (recipientInfo == null) {
            logger.d("sendVideoFile : illegal parameter");
            return -1L;
        }
        if (recipientInfo.getThreadId() <= 0) {
            // 还没创建conversation，或者只有SMS消息时
            long threadId = messageDbManager.getConversationId(recipientInfo.getConversationType(),
                    recipientInfo.getRecipientList());
            if (threadId <= 0) {
                // 创建threadId
                threadId = messageDbManager.createConversationId(recipientInfo.getConversationType(),
                        recipientInfo.getRecipientList());
            }
            recipientInfo.setThreadId(threadId);
        }

        ChatMsgBaseInfo msgInfo = new ChatMessageInfo();
        msgInfo.setContact(ProfileManager.getInstance().getCurUsrObjId());
        msgInfo.setReceiver(recipientInfo.getRecipientList());
        msgInfo.setMsgType(MessageType.MSG_TYPE_TEXT);
        msgInfo.setTime(System.currentTimeMillis());
        msgInfo.setPlanSendTime(time);
        msgInfo.setSendReceive(MessageSendReceive.MSG_SEND);
        msgInfo.setMsgState(MessageState.MSG_STATE_SENT);
        msgInfo.setIsRead(true);
        msgInfo.setData(text);
        msgInfo.setThreadId(recipientInfo.getThreadId());
        msgInfo.setChatType(recipientInfo.getConversationType());
        msgInfo.setExtraInfo(ChatMsgBaseInfo.TIME_SEND_MSG_FLAG);
        Uri insertUri = messageDbManager.addMessage(msgInfo);
        if (insertUri == null) {
            logger.e("insert to message db fail");
            return -1L;
        }
        return ContentUris.parseId(insertUri);
    }

    /**
     * 发送图片(包括照片)
     *
     * @param recipientInfo
     * @param imageFilePath
     * @param isBurnAfter   ,
     * @param burnTime
     * @throws Exception
     */
    public long sendImageFiles(MessageRecipientInfo recipientInfo, String imageFilePath, boolean isBurnAfter,
                               int burnTime, int maxSendSize, String extraInfo) throws Exception {
        if (recipientInfo == null) {
            logger.d("sendVideoFile : illegal parameter");
            return -1L;
        }
        if (recipientInfo.getThreadId() <= 0) {
            // 还没创建conversation，或者只有SMS消息时
            long threadId = messageDbManager.getConversationId(recipientInfo.getConversationType(),
                    recipientInfo.getRecipientList());
            if (threadId <= 0) {
                // 创建threadId
                threadId = messageDbManager.createConversationId(recipientInfo.getConversationType(),
                        recipientInfo.getRecipientList());
            }
            recipientInfo.setThreadId(threadId);
        }

        ChatMsgBaseInfo msgInfo = new ChatMessageInfo();
        msgInfo.setContact(ProfileManager.getInstance().getCurUsrObjId());
        msgInfo.setReceiver(recipientInfo.getRecipientList());
        msgInfo.setMsgType(MessageType.MSG_TYPE_IMAGE);
        msgInfo.setTime(System.currentTimeMillis());
        msgInfo.setSendReceive(MessageSendReceive.MSG_SEND);
        msgInfo.setMsgState(MessageState.MSG_STATE_SEND_ING);
        msgInfo.setIsRead(true);
        File oldFile = new File(imageFilePath);
        File file = new File(CompressPicUtil.compressImage(imageFilePath, maxSendSize));
        msgInfo.setFileName(oldFile.getName());
        msgInfo.setFilePath(file.getAbsolutePath());
        msgInfo.setFileSize(file.length());
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), option);
        int width = option.outWidth;
        int height = option.outHeight;
        if (imageFilePath.equals(file.getAbsolutePath())) {
            // 图片没有做处理
            int degree = ImageUtil.readPictureDegree(file.getAbsolutePath());
            if (degree / 90 % 2 != 0) {
                int tempSwitch = width;
                width = height;
                height = tempSwitch;
            }
        }
        msgInfo.setData(width + LittleCDbManager.IMAGE_SIZE_SEPARATOR + height);
        msgInfo.setThreadId(recipientInfo.getThreadId());
        msgInfo.setChatType(recipientInfo.getConversationType());
        msgInfo.setIsBurnAfter(isBurnAfter);
        msgInfo.setExtraInfo(extraInfo);
        Uri insertUri = messageDbManager.addMessage(msgInfo);
        if (insertUri == null) {
            logger.e("insert to message db fail");
            return -1L;
        }
        long id = ContentUris.parseId(insertUri);
        AVIMImageMessage content = new AVIMImageMessage(file);
        sendMessage(recipientInfo, id, content, isBurnAfter, extraInfo);
        return id;
    }

    /**
     * 发送语音
     *
     * @param recipientInfo
     * @param audioFilePath
     * @param recordTime    本地记录最长时间为 180 seconds
     * @param isBurnAfter
     * @param burnTime
     * @throws Exception
     */
    public long sendAudioFile(MessageRecipientInfo recipientInfo, String audioFilePath, int recordTime,
                              boolean isBurnAfter, int burnTime, boolean isRecord) throws Exception {
        if (recipientInfo == null) {
            logger.d("sendVideoFile : illegal parameter");
            return -1L;
        }
        if (recipientInfo.getThreadId() <= 0) {
            // 还没创建conversation时
            long threadId = messageDbManager.getConversationId(recipientInfo.getConversationType(),
                    recipientInfo.getRecipientList());
            if (threadId <= 0) {
                // 创建threadId
                threadId = messageDbManager.createConversationId(recipientInfo.getConversationType(),
                        recipientInfo.getRecipientList());
            }
            recipientInfo.setThreadId(threadId);
        }

        ChatMsgBaseInfo msgInfo = new ChatMessageInfo();
        msgInfo.setContact(ProfileManager.getInstance().getCurUsrObjId());
        msgInfo.setReceiver(recipientInfo.getRecipientList());
        msgInfo.setMsgType(MessageType.MSG_TYPE_AUDIO);
        msgInfo.setTime(System.currentTimeMillis());
        msgInfo.setSendReceive(MessageSendReceive.MSG_SEND);
        msgInfo.setMsgState(MessageState.MSG_STATE_SEND_ING);
        msgInfo.setIsRead(true);
        File file = new File(audioFilePath);
        msgInfo.setFileName(file.getName());
        msgInfo.setFilePath(audioFilePath);
        msgInfo.setDuration(recordTime);
        msgInfo.setFileSize(file.length());
        msgInfo.setThreadId(recipientInfo.getThreadId());
        msgInfo.setChatType(recipientInfo.getConversationType());
        //msgInfo.setIsBurnAfter(isBurnAfter);
        Uri insertUri = messageDbManager.addMessage(msgInfo);
        if (insertUri == null) {
            logger.e("insert to message db fail");
            return -1L;
        }
        long id = ContentUris.parseId(insertUri);
        AVIMAudioMessage content = new AVIMAudioMessage(file);
        sendMessage(recipientInfo, id, content, isBurnAfter, null);
        return id;
    }

    /**
     * 发送视频
     *
     * @param recipientInfo
     * @param videoFilePath
     * @param videoLen      最长文件播放时长 90秒
     * @param isBurnAfter
     * @param burnTime
     * @throws Exception
     */
    public long sendVideoFile(MessageRecipientInfo recipientInfo, String videoFilePath, int videoLen,
                              boolean isBurnAfter, int burnTime, boolean isRecord) throws Exception {
        if (recipientInfo == null) {
            logger.d("sendVideoFile : illegal parameter");
            return -1L;
        }
        if (recipientInfo.getThreadId() <= 0) {
            // 还没创建conversation，或者只有SMS消息时
            long threadId = messageDbManager.getConversationId(recipientInfo.getConversationType(),
                    recipientInfo.getRecipientList());
            if (threadId <= 0) {
                // 创建threadId
                threadId = messageDbManager.createConversationId(recipientInfo.getConversationType(),
                        recipientInfo.getRecipientList());
            }
            recipientInfo.setThreadId(threadId);
        }

        ChatMsgBaseInfo msgInfo = new ChatMessageInfo();
        msgInfo.setContact(ProfileManager.getInstance().getCurUsrObjId());
        msgInfo.setReceiver(recipientInfo.getRecipientList());
        msgInfo.setMsgType(MessageType.MSG_TYPE_VIDEO);
        msgInfo.setTime(System.currentTimeMillis());
        msgInfo.setSendReceive(MessageSendReceive.MSG_SEND);
        msgInfo.setMsgState(MessageState.MSG_STATE_SEND_ING);
        msgInfo.setIsRead(true);
        File file = new File(videoFilePath);
        msgInfo.setFileName(file.getName());
        msgInfo.setFilePath(videoFilePath);
        msgInfo.setDuration(videoLen);
        msgInfo.setFileSize(file.length());
        msgInfo.setThumbPath(getVideoThumbnail(videoFilePath, 0, 0, MediaStore.Video.Thumbnails.MINI_KIND));
        msgInfo.setThreadId(recipientInfo.getThreadId());
        msgInfo.setChatType(recipientInfo.getConversationType());
        msgInfo.setIsBurnAfter(isBurnAfter);
        Uri insertUri = messageDbManager.addMessage(msgInfo);
        if (insertUri == null) {
            logger.e("insert to message db fail");
            return -1L;
        }
        long id = ContentUris.parseId(insertUri);
        AVIMVideoMessage content = new AVIMVideoMessage(file);
        sendMessage(recipientInfo, id, content, isBurnAfter, null);
        return id;
    }

    /**
     * 获取视频缩略图
     *
     * @param videoPath
     * @param width
     * @param height
     * @param kind
     * @return
     */
    private String getVideoThumbnail(String videoPath, int width, int height, int kind) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        if (width > 0 && height > 0) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        File folder = new File(WalkArroundApp.MTC_DATA_PATH + AppConstant.LOCATION_PIC_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String thumbPath = WalkArroundApp.MTC_DATA_PATH + AppConstant.LOCATION_PIC_PATH
                + System.currentTimeMillis() + CompressPicUtil.getFileType(videoPath);
        File file = new File(thumbPath);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            logger.e("getVideoThumbnail IOException:" + e.getMessage());
        }
        return thumbPath;
    }

    /**
     * 发送位置信息
     *
     * @param recipientInfo
     * @param lat           经度
     * @param lng           纬度
     * @param address       位置信息描述
     * @throws Exception
     */
    public long sendLocationInfo(MessageRecipientInfo recipientInfo, double lat, double lng, String address,
                                 String imagePath, String extraInfo) throws Exception {
        if (recipientInfo == null) {
            logger.d("sendVideoFile : illegal parameter");
            return -1L;
        }
        if (recipientInfo.getThreadId() <= 0) {
            // 还没创建conversation，或者只有SMS消息时
            long threadId = messageDbManager.getConversationId(recipientInfo.getConversationType(),
                    recipientInfo.getRecipientList());
            if (threadId <= 0) {
                // 创建threadId
                threadId = messageDbManager.createConversationId(recipientInfo.getConversationType(),
                        recipientInfo.getRecipientList());
            }
            recipientInfo.setThreadId(threadId);
        }

        ChatMsgBaseInfo msgInfo = new ChatMessageInfo();
        msgInfo.setContact(ProfileManager.getInstance().getCurUsrObjId());
        msgInfo.setReceiver(recipientInfo.getRecipientList());
        msgInfo.setMsgType(MessageType.MSG_TYPE_MAP);
        msgInfo.setTime(System.currentTimeMillis());
        msgInfo.setSendReceive(MessageSendReceive.MSG_SEND);
        msgInfo.setMsgState(MessageState.MSG_STATE_SEND_ING);
        msgInfo.setIsRead(true);
        msgInfo.setLocationLabel(address);
        msgInfo.setLatitute(lat);
        msgInfo.setLongitude(lng);
        if(!TextUtils.isEmpty(imagePath)) {
            File file = new File(imagePath);
            msgInfo.setFileName(file.getName());
            msgInfo.setFilePath(imagePath);
            msgInfo.setFileSize(file.length());
        }
        msgInfo.setThreadId(recipientInfo.getThreadId());
        msgInfo.setChatType(recipientInfo.getConversationType());
        msgInfo.setExtraInfo(extraInfo);
        Uri insertUri = messageDbManager.addMessage(msgInfo);
        if (insertUri == null) {
            logger.e("insert to message db fail");
            return -1L;
        }
        long id = ContentUris.parseId(insertUri);
        AVIMLocationMessage content = new AVIMLocationMessage();
        content.setLocation(new AVGeoPoint(lat, lng));
        content.setText(address);
        sendMessage(recipientInfo, id, content, false, extraInfo);
        return id;
    }

    /**
     * 发送消息
     *
     * @param recipientInfo
     * @param msgId
     * @param content
     */
    private void sendMessage(MessageRecipientInfo recipientInfo, long msgId, AVIMTypedMessage content,
                             boolean isBurnAfter, String extraInfo) {

        //If conversation is NULL or conversation is incorrect, we should find conversation before sending message.
        if(mCurConversation == null) {
            //Query conversation
            WalkArroundMsgManager.getInstance(mContext).getConversation(recipientInfo.getRecipientList().get(0), new AsyncTaskListener() {

                @Override
                public void onSuccess(Object data) {
                    sendMsgViaAVSSdk(content, msgId);
                }

                @Override
                public void onFailed(AVException e) {
                    onSendError(msgId);
                }
            });
            logger.e("sendMessage conversation is NULL!");
            return;
        }

        switch (recipientInfo.getConversationType()) {
            case ChatType.CHAT_TYPE_ONE2ONE:
                sendMsgViaAVSSdk(content, msgId);
                break;
            default:
                break;
        }
    }

    private void sendMsgViaAVSSdk(AVIMTypedMessage content, long msgId) {

        if(mCurConversation == null) {
            return;
        }

        // 一对一. send message here.
        mCurConversation.sendMessage(content,  new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
                if (e == null) {
                    onSendSuccess(msgId);
                    logger.d("发送成功！");
                } else {
                    onSendError(msgId);
                    logger.d("发送失败！");
                }
            }
        });
    }

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
    public List<ChatMsgBaseInfo> getMessageList(long thread_id, long beginChatId, boolean isBeforeChatId, int count)
            throws Exception {
        return messageDbManager.getMessageList(thread_id, beginChatId, isBeforeChatId, count);
    }

    @Override
    public List<ChatMsgBaseInfo> getMessageList(long thread_id, long time, int count) throws Exception {
        return messageDbManager.getMessageByTime(thread_id, time, count);
    }

    /**
     * 删除消息
     *
     * @param messageId
     * @throws Exception
     */
    public int deleteMessage(long messageId) throws Exception {
        return messageDbManager.deleteMsgById(messageId);
    }

    /**
     * 删除消息
     *
     * @param threadId
     * @throws Exception
     */
    public int deleteThreadMessage(long threadId) throws Exception {
        return messageDbManager.deleteMsgByThreadId(threadId);
    }

    @Override
    public void deleteConversation(long threadId) throws Exception {
        messageDbManager.deleteConversation(threadId);
    }

    @Override
    public int deleteMappingConversation() throws Exception {
        return messageDbManager.deleteMappingConversation();
    }

    /**
     * 获取消息内容
     *
     * @param messageId
     * @return
     * @throws Exception
     */
    public ChatMsgBaseInfo getMessageById(long messageId) throws Exception {
        return messageDbManager.getMsgById(messageId);
    }

    @Override
    public ChatMsgBaseInfo getDraftMessage(long threadId) throws Exception {
        return messageDbManager.getDraftMsg(threadId);
    }

    @Override
    public long updateDraftMessage(MessageRecipientInfo recipientInfo, String message) throws Exception {
        if (recipientInfo == null) {
            logger.d("updateDraftMessage : illegal parameter");
            return -1L;
        }
        if (recipientInfo.getThreadId() <= 0 && TextUtils.isEmpty(message)) {
            return -1L;
        }
        if (recipientInfo.getThreadId() <= 0) {
            // 还没创建conversation，或者只有SMS消息时
            long threadId = messageDbManager.getConversationId(recipientInfo.getConversationType(),
                    recipientInfo.getRecipientList());
            if (threadId <= 0) {
                // 创建threadId
                threadId = messageDbManager.createConversationId(recipientInfo.getConversationType(),
                        recipientInfo.getRecipientList());
            }
            recipientInfo.setThreadId(threadId);
        }
        return messageDbManager.updateDraftMsg(recipientInfo.getThreadId(), recipientInfo.getConversationType(),
                recipientInfo.getRecipientList(), message);
    }

    @Override
    public void setAllSendingMsgStatusFail() throws Exception {
        messageDbManager.updateAllSendingMsgStatusFail();
    }

    public boolean interruptDownloadVideo(ChatMsgBaseInfo message) throws Exception {
        return false;
    }

    public void acceptFile(ChatMsgBaseInfo message, boolean isCollectMsg) throws Exception {
        FileDownLoadAsyncTask task = new FileDownLoadAsyncTask(mContext, messageDbManager, isCollectMsg);
        task.execute(message);
    }

    /**
     * 阅后即焚
     *
     * @throws Exception
     */
    public void burnMessage(long messageId) throws Exception {
        messageDbManager.deleteMsgById(messageId);
    }

    /**
     * 重发消息
     *
     * @throws Exception
     */
    public void resendMessage(String groupId, long messageId) throws Exception {
        ChatMsgBaseInfo message = getMessageById(messageId);
        if (message == null) {
            return;
        }
        AVIMTypedMessage messageBody = null;
        int messageType = message.getMsgType();
        switch (messageType) {
            case MessageType.MSG_TYPE_TEXT:
//            case MessageType.MSG_TYPE_SMS:
                messageBody = new AVIMTextMessage();
                ((AVIMTextMessage)messageBody).setText(message.getData());
                break;
            //case MessageType.MSG_TYPE_CONTACT:
            //    messageBody = new VcardMessageBody(message.getData());
            //    break;
            case MessageType.MSG_TYPE_AUDIO:
                // 语音消息
                messageBody = new AVIMAudioMessage(new File(message.getFilepath()));
                break;
            case MessageType.MSG_TYPE_VIDEO:
                messageBody = new AVIMVideoMessage(new File(message.getFilepath()));
                break;
            case MessageType.MSG_TYPE_IMAGE:
                messageBody = new AVIMImageMessage(new File(message.getFilepath()));
                break;
            case MessageType.MSG_TYPE_MAP:
                messageBody = new AVIMLocationMessage();
                ((AVIMLocationMessage)messageBody).setLocation(new AVGeoPoint(message.getLatitude(), message.getLongitude()));
                ((AVIMLocationMessage)messageBody).setText(message.getLocationLabel());
                break;
            default:
                break;
        }
        if (messageBody != null) {
            MessageRecipientInfo recipientInfo = new MessageRecipientInfo();
            recipientInfo.setConversationType(message.getChatType());
            recipientInfo.setGroupId(groupId);
            recipientInfo.setThreadId(message.getMsgThreadId());
            recipientInfo.setRecipientList(message.getReceiver());
            messageDbManager.updateMessageStatus(messageId, null, MessageState.MSG_STATE_SEND_ING);
            messageDbManager.updateConversationStatus(messageId, MessageState.MSG_STATE_SEND_ING, 0);
            sendMessage(recipientInfo, message.getMsgId(), messageBody, message.isBurnAfterMsg(), null);
        }
    }

    /**
     * 设置消息为已读状态
     *
     * @return
     * @throws Exception
     */
    public int setMessageRead(long messageId) throws Exception {
        return messageDbManager.setMessageRead(messageId);
    }

    @Override
    public boolean isMessageAttachDownload(ChatMsgBaseInfo message) throws Exception {
        return !TextUtils.isEmpty(message.getFilepath())
                && message.getMsgState() == MessageState.MSG_STATE_RECEIVED;
    }

    /**
     * 将当前消息置顶或取消置顶
     *
     * @param threadId
     * @param isTop
     */
    public void setOrCancelTopMessage(long threadId, boolean isTop) throws Exception {
        messageDbManager.markConversationAsTop(threadId, isTop);
    }

    @Override
    public boolean isTopMsgByThreadId(long thread) throws Exception {
        return messageDbManager.isConversationTop(thread);
    }

    @Override
    public Map<String, String> queryMessageSession(String key) {
        return messageDbManager.queryMsgSession(key);
    }

    @Override
    public List<MessageSessionBaseModel> getMessageSessionList(boolean isNotifyMsg, int offset, int count) throws Exception {
        return messageDbManager.getMsgSession(isNotifyMsg, offset, count);
    }

    @Override
    public int delOtherConversionsOverParamTime(long time) throws Exception {
        return messageDbManager.delOtherConversionsOverParamTime(time);
    }

    public int getMsgUnreadCount(String strThreadId) throws Exception {
        int count = messageDbManager.getConversationUnreadCount(Integer.parseInt(strThreadId));
        return count;
    }

    public int getUnreadMsgCountByThreadId(String strThreadId) throws Exception {
        int count = messageDbManager.getConversationUnreadCount(Integer.parseInt(strThreadId));
        return count;
    }

    public void removeUnreadMessageByThreadId(String strThreadId) throws Exception {
        messageDbManager.doMarkConversationReaded(Integer.parseInt(strThreadId));
    }

    public int getAllUnreadCount() throws Exception {
        int count = messageDbManager.getAllConversationsUnreadCount();
        return count;
    }

    public List<ChatMessageInfo> searchMsgByKey(String key, List<String> numlist) {
        List<ChatMessageInfo> list = messageDbManager.searchMsgByKey(key, numlist);
        return list;
    }

    @Override
    public int getConversationId(int chatType, List<String> address) {
        return messageDbManager.getConversationId(chatType, address);
    }

    @Override
    public int createConversationId(int chatType, List<String> address) {
        return messageDbManager.createConversationId(chatType, address);
    }

    @Override
    public int createConversationId(int chatType, List<String> address, int status) {
        return messageDbManager.createConversationId(chatType, address);
    }

    @Override
    public boolean isConversationExist(long threadId) throws Exception {
        return messageDbManager.isConversationExist(threadId);
    }

    @Override
    public int batchDeleteThreadMessage(List<Long> threadIdList) {
        return messageDbManager.batchDeleteMsg(threadIdList);
    }

    @Override
    public int batchSetOrCancelTopMessage(List<Long> threadIdList, boolean b) throws Exception {
        if (threadIdList == null || threadIdList.isEmpty()) {
            return 0;
        }
        return messageDbManager.batchMarkConversationAsTop(threadIdList, b);
    }

    @Override
    public int batchRemoveUnreadMessage(List<Long> threadIdList) throws Exception {
        if (threadIdList == null || threadIdList.isEmpty()) {
            return 0;
        }
        return messageDbManager.batchDoMarkConversationRead(threadIdList);
    }

    @Override
    public void addMsgUnreadCountByThreadId(long threadId) throws Exception {
        messageDbManager.addUnreadCountByThreadId(threadId);
    }

    @Override
    public MessageSessionBaseModel getLatestSessionById(long threadId) throws Exception {
        return messageDbManager.getLatestSessionById(threadId);
    }

    @Override
    public MessageSessionBaseModel getLatestSessionByMessageId(long messageId) throws Exception {
        return messageDbManager.getLatestSessionByMessageId(messageId);
    }

    @Override
    public boolean isTopMsgByNum(int chatType, List<String> numList) throws Exception {
        return messageDbManager.isConversationTop(getConversationId(chatType, numList));
    }

    //@Override
    //public List<GroupInvitationBaseInfo> getGroupInvitationList(long beginId, int count) throws Exception {
    //    return messageDbManager.getGroupInvitationList(beginId, count);
    //}

    @Override
    public int getUnreadInvitationCount() throws Exception {
        return 0;
        //return messageDbManager.getUnreadInvitationCount();
    }

    @Override
    public int setInvitationRead() throws Exception {
        return 0;
        //return messageDbManager.setInvitationRead();
    }

    @Override
    public List<ChatMsgBaseInfo> getMessageByExtraInfo(String extraKey) throws Exception {
        return messageDbManager.getMessageByExtraInfo(extraKey);
    }

    @Override
    public void updateConversationMsgNotifyFlag() throws Exception {
        messageDbManager.updateConversationMsgNotifyFlag();
    }

    @Override
    public void updateConversationStatus(long threadid, int status) throws Exception {
        messageDbManager.updateConversationStatus(threadid, status);
    }

    @Override
    public int getIntentConversationStatus(long threadid) throws Exception {
        return messageDbManager.getConversationStatus(threadid);
    }

    @Override
    public void updateConversationColor(long threadid, int color) throws Exception {
        messageDbManager.updateConversationColor(threadid, color);
    }

    @Override
    public void updateConversationStatusAndColor(long threadid, int state, int color) throws Exception {
        messageDbManager.updateConversationStatusAndColor(threadid, state, color);
    }

    @Override
    public int getIntentConversationColor(long threadid) throws Exception {
        return messageDbManager.getConversationColor(threadid);
    }

    @Override
    public MessageSessionBaseModel getLatestNotifySession() throws Exception {
        return messageDbManager.getLatestNotifySession();
    }

    @Override
    public int getAllNotifyMsgUnreadCount() throws Exception {
        return messageDbManager.getAllNotifyMsgUnreadCount();
    }

    @Override
    public Uri saveMessage(ChatMsgBaseInfo message) {
        return messageDbManager.addMessage(message);
    }

    private void onSendSuccess(long messageId) {
        String filePath = null;
        String thumbNailPath = null;
        ChatMsgBaseInfo curMsg = null;
        try {
            curMsg = getMessageById(messageId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        messageDbManager.updateMessageStatus(curMsg.getMsgId(), curMsg.getPacketId(), MessageState.MSG_STATE_SENT);
        messageDbManager.updateConversationStatus(curMsg.getMsgId(), MessageState.MSG_STATE_SENT, System.currentTimeMillis());
//        MessageBody messageBody = curMsg.getMessageBody();
//        if (messageBody instanceof AudioMessageBody) {
//            filePath = ((AudioMessageBody) messageBody).getOriginalUri();
//        } else if (messageBody instanceof ImageMessageBody) {
//            filePath = ((ImageMessageBody) messageBody).getOriginalUri();
//            thumbNailPath = ((ImageMessageBody) messageBody).getSmallUri();
//        } else if (messageBody instanceof VideoMessageBody) {
//            filePath = ((VideoMessageBody) messageBody).getOriginalUri();
//            thumbNailPath = ((VideoMessageBody) messageBody).getThumbnailUrl();
//        } else if (messageBody instanceof LocationMessageBody) {
//            filePath = ((LocationMessageBody) messageBody).getOriginalUri();
//        }
        // 消息发送成功
        //String timeTamp = SdkUtils.restoreGuidToTime(cmMessage.getGuid());
        //long time = Long.parseLong(timeTamp);
        //messageDbManager.updateMessageFilePath(cmMessage.getId(), cmMessage.getPacketId(),
        //        filePath, thumbNailPath, MessageState.MSG_STATE_SENT, time);
        //messageDbManager.updateConversationStatus(cmMessage.getId(), MessageState.MSG_STATE_SENT);

        curMsg.setMsgState(MessageState.MSG_STATE_SENT);
        messageUpdate(MsgBroadcastConstants.ACTION_MESSAGE_STATUS_CHANGED, curMsg);
    }

    private void onSendError(long msgId) {
        if (msgId < 0) {
            return;
        }

        ChatMsgBaseInfo curMsg = null;
        try {
            curMsg = getMessageById(msgId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 消息发送失败
        messageDbManager.updateMessageStatus(msgId, curMsg.getPacketId(), MessageState.MSG_STATE_SEND_FAIL);
        messageDbManager.updateConversationStatus(msgId, MessageState.MSG_STATE_SEND_FAIL, System.currentTimeMillis());
        curMsg.setMsgState(MessageState.MSG_STATE_SEND_FAIL);
        messageUpdate(MsgBroadcastConstants.ACTION_MESSAGE_STATUS_CHANGED, curMsg);
    }

    private void messageUpdate(String action, ChatMsgBaseInfo cmMessage) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(MsgBroadcastConstants.BC_VAR_MSG_ID, cmMessage.getMsgId());
        intent.putExtra(MsgBroadcastConstants.BC_VAR_THREAD_ID, cmMessage.getMsgThreadId());
        intent.putExtra(MsgBroadcastConstants.BC_VAR_MSG_STATUS, cmMessage.getMsgState());
        intent.putExtra(MsgBroadcastConstants.BC_VAR_CONTACT, cmMessage.getContact());
        intent.putExtra(MsgBroadcastConstants.BC_VAR_GROUP_ID, cmMessage.getGroupId());
        mContext.sendBroadcast(intent);
    }
}
