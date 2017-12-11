/**
 * Copyright (C) 2014-2015 CMCC All rights reserved
 */
package com.example.walkarround.message.manager;

import android.app.NotificationManager;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.text.TextUtils;
import com.example.walkarround.base.WalkArroundApp;
import com.example.walkarround.message.model.ChatMessageInfo;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.model.MessageSessionBaseModel;
import com.example.walkarround.message.model.MessageSessionModelInfo;
import com.example.walkarround.message.provider.MessageDatabase;
import com.example.walkarround.message.provider.MessageDatabase.Conversation;
import com.example.walkarround.message.provider.MessageDatabase.Message;
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.message.util.MessageConstant.*;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.myself.manager.ProfileManager;
import com.example.walkarround.util.AppConstant;
import com.example.walkarround.util.Logger;

import java.io.File;
import java.util.*;

/**
 * 数据库管理
 *
 * @author
 */
public class AVSDbManager {
    public static final String IMAGE_SIZE_SEPARATOR = "，";
    private static final byte[] sLock = new byte[0];
    private static Logger logger = Logger.getLogger(AVSDbManager.class.getSimpleName());
    private Context mContext;

    public AVSDbManager(Context context) {
        mContext = context;
    }

    /**
     * 获取消息列表
     *
     * @param thread_id      消息会话ID
     * @param beginChatId    开始ID
     * @param isBeforeChatId 是否小于开始ID
     * @param count          获取的个数
     * @return 消息列表
     */
    public List<ChatMsgBaseInfo> getMessageList(long thread_id, long beginChatId, boolean isBeforeChatId, int count) {
        List<ChatMsgBaseInfo> msgList = new ArrayList<ChatMsgBaseInfo>();
        String where;
        String[] selectionArgs;
        if (beginChatId > 0) {
            String beforeOrAfter = isBeforeChatId ? " < " : " > ";
            where = Message._CONVERSATION_ID + "=? and " + Message._ID + beforeOrAfter + "? AND "
                    + Message._STATUS + "<>?";
            selectionArgs = new String[]{thread_id + "", beginChatId + "", "" + MessageState.MSG_STATE_SEND_DRAFT};
        } else {
            where = Message._CONVERSATION_ID + "=? AND " + Message._STATUS + "<>?";
            selectionArgs = new String[]{thread_id + "", "" + MessageState.MSG_STATE_SEND_DRAFT};
        }
        Cursor cursor;
        if (isBeforeChatId) {
            cursor = mContext.getContentResolver().query(Message.CONTENT_URI, null, where, selectionArgs,
                    Message._SEND_TIME + " desc limit 0," + count);
        } else {
            cursor = mContext.getContentResolver().query(Message.CONTENT_URI, null, where, selectionArgs,
                    Message._SEND_TIME + " asc limit 0," + count);
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    msgList.add(getChatMsgByCursor(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return msgList;
    }

    public List<ChatMsgBaseInfo> getMessageByTime(long thread_id, long time, int count) {
        List<ChatMsgBaseInfo> msgList = new ArrayList<ChatMsgBaseInfo>();
        String where = Message._CONVERSATION_ID + "=? and " + Message._SEND_TIME + " <= ? AND " + Message._STATUS + "<>?";
        String[] selectionArgs = new String[]{thread_id + "", time + "", "" + MessageState.MSG_STATE_SEND_DRAFT};

        int upCount = count / 2;
        Cursor cursor = mContext.getContentResolver().query(Message.CONTENT_URI, null, where, selectionArgs,
                Message._SEND_TIME + " desc limit 0," + (upCount + 1));
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    msgList.add(getChatMsgByCursor(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        where = Message._CONVERSATION_ID + "=? and " + Message._SEND_TIME + "> ? AND " + Message._STATUS + "<>?";
        cursor = mContext.getContentResolver().query(Message.CONTENT_URI, null, where, selectionArgs,
                Message._SEND_TIME + " asc limit 0," + (count - upCount));
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    msgList.add(getChatMsgByCursor(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return msgList;
    }

    /**
     * 根据消息threadId获取草稿消息
     *
     * @param threadId 消息threadId
     * @return 消息详情
     */
    public ChatMsgBaseInfo getDraftMsg(long threadId) {
        String where = Message._CONVERSATION_ID + " =? AND " + Message._STATUS + " =?";
        String[] args = new String[]{threadId + "", MessageState.MSG_STATE_SEND_DRAFT + ""};
        ChatMsgBaseInfo messageInfo = null;
        Cursor cursor = mContext.getContentResolver().query(Message.CONTENT_URI, null, where, args, null);
        if (null != cursor) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                messageInfo = getChatMsgByCursor(cursor);
            }
            cursor.close();
        }
        return messageInfo;
    }

    /**
     * 根据_EXTRA_INFO获取消息
     *
     * @param extraKey
     * @return
     */
    public List<ChatMsgBaseInfo> getMessageByExtraInfo(String extraKey) {
        String where = Message._EXTRA_INFO + " like ?";
        String[] args = new String[]{"%" + extraKey};
        List<ChatMsgBaseInfo> messageInfo = new ArrayList<ChatMsgBaseInfo>();
        Cursor cursor = mContext.getContentResolver().query(Message.CONTENT_URI, null, where, args, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                do {
                    messageInfo.add(getChatMsgByCursor(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return messageInfo;
    }

    /**
     * 更新草稿信息
     *
     * @param threadId 消息threadId
     * @return 消息id
     */
    public long updateDraftMsg(long threadId, int chatType, List<String> receiver, String message) {
        String where = Message._CONVERSATION_ID + " =? AND " + Message._STATUS + " =?";
        String[] args = new String[]{threadId + "", MessageState.MSG_STATE_SEND_DRAFT + ""};
        if (TextUtils.isEmpty(message)) {
            mContext.getContentResolver().delete(Message.CONTENT_URI, where, args);
            updateConversationDraftMsg(threadId, null);
            return -1;
        }
        updateConversationDraftMsg(threadId, message);
        // 插入数据库
        ContentValues values = new ContentValues();

        //TODO: Add message sender / receiver getting method.
        values.put(Message._SENDER, ProfileManager.getInstance().getCurUsrObjId());
        values.put(Message._ADDRESS, MessageUtil.gsontoJson(receiver));
        values.put(Message._SEND_TIME, System.currentTimeMillis());
        values.put(Message._CHAT_TYPE, chatType);
        values.put(Message._CONVERSATION_ID, threadId);
        values.put(Message._CONTENT, message);
        Cursor cursor = mContext.getContentResolver().query(Message.CONTENT_URI, new String[]{Message._ID},
                where, args, null);
        long msgId = 0;
        if (null != cursor) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                msgId = cursor.getLong(cursor.getColumnIndex(Message._ID));
                mContext.getContentResolver().update(Message.CONTENT_URI, values, where, args);
            } else {
                values.put(Message._STATUS, MessageState.MSG_STATE_SEND_DRAFT);
                values.put(Message._READ, Message.MSG_READ);
                values.put(Message._CONTENT_TYPE, MessageType.MSG_TYPE_TEXT);
                values.put(Message._SEND_RECV, MessageSendReceive.MSG_SEND);
                Uri uri = mContext.getContentResolver().insert(Message.CONTENT_URI, values);
                if (uri != null) {
                    msgId = ContentUris.parseId(uri);
                }
            }
            cursor.close();
        } else {
            values.put(Message._STATUS, MessageState.MSG_STATE_SEND_DRAFT);
            values.put(Message._READ, Message.MSG_READ);
            values.put(Message._CONTENT_TYPE, MessageType.MSG_TYPE_TEXT);
            values.put(Message._SEND_RECV, MessageSendReceive.MSG_SEND);
            Uri uri = mContext.getContentResolver().insert(Message.CONTENT_URI, values);
            if (uri != null) {
                msgId = ContentUris.parseId(uri);
            }
        }

        return msgId;
    }

    /**
     * 根据msgId获取消息
     *
     * @param msgId 消息ID
     * @return 消息详情
     */
    public ChatMsgBaseInfo getMsgById(long msgId) {
        String where = Message._ID + " =? ";
        String[] args = new String[]{msgId + ""};
        ChatMsgBaseInfo messageInfo = null;
        Cursor cursor = mContext.getContentResolver().query(Message.CONTENT_URI, null, where, args, null);
        if (null != cursor) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                messageInfo = getChatMsgByCursor(cursor);
            }
            cursor.close();
        }
        return messageInfo;
    }

    /**
     * 删除SMS信息
     *
     * @param msgId 消息ID
     * @return 删除的个数
     */
    public int deleteMsgById(long msgId) {
        String where = Message._ID + "=?";
        String[] args = new String[]{Long.toString(msgId)};
        deleteFile(where, args);
        return mContext.getContentResolver().delete(Message.CONTENT_URI, where, args);
    }

    /**
     * 删除消息对应的文件
     *
     * @param where 检索条件
     * @param args  检索条件对应值
     * @return 检索结果
     */
    private int deleteFile(String where, String[] args) {
        String[] project = new String[]{Message._FILE_PATH, Message._CONTENT_TYPE, Message._THUMBNAIL_PATH,
                Message._SEND_RECV};
        StringBuilder selection = new StringBuilder();
        selection.append("(");
        selection.append(where);
        selection.append(") AND (");
        selection.append(Message._FILE_PATH);
        selection.append(" NOT NULL or ");
        selection.append(Message._THUMBNAIL_PATH);
        selection.append(" NOT NULL)");
        int count = 0;
        Cursor cursor = mContext.getContentResolver().query(Message.CONTENT_URI, project, selection.toString(), args,
                null);
        if (null != cursor) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    String filePath = cursor.getString(cursor.getColumnIndex(Message._FILE_PATH));
                    if (!TextUtils.isEmpty(filePath)) {
                        int sendReceive = cursor.getInt(cursor.getColumnIndex(Message._SEND_RECV));
                        int msgType = cursor.getInt(cursor.getColumnIndex(Message._CONTENT_TYPE));
                        if ((sendReceive == MessageSendReceive.MSG_SEND)
                                && (msgType == MessageType.MSG_TYPE_IMAGE)) {
                            if (!filePath.startsWith(WalkArroundApp.MTC_DATA_PATH + AppConstant.CAMERA_TAKE_PIC_PATH)) {
                                continue;
                            }
                        }
                        File file = new File(filePath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                    String thumbPath = cursor.getString(cursor.getColumnIndex(Message._THUMBNAIL_PATH));
                    if (!TextUtils.isEmpty(thumbPath)) {
                        File file = new File(thumbPath);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                    count++;
                } while (cursor.moveToNext());
            }

            cursor.close();
        }
        return count;
    }

    /**
     * 删除信息
     *
     * @param threadId 消息对应的会话ID
     * @return 删除的个数
     */
    public int deleteMsgByThreadId(long threadId) {
        String where = Message._CONVERSATION_ID + "=?";
        String[] args = new String[]{Long.toString(threadId)};
        deleteFile(where, args);
        updateConversationDraftMsg(threadId, null);
        return mContext.getContentResolver().delete(Message.CONTENT_URI, where, args);
    }

    /**
     * 删除信息
     *
     * @param idList 删除消息的ID
     * @return 删除的个数
     */
    public int deleteMsgById(List<String> idList) {
        StringBuilder selection = new StringBuilder();
        String lastItem = idList.get(idList.size() - 1);
        for (String id : idList) {
            selection.append(Message._ID + "=?");
            if (id.equals(lastItem)) {
                break;
            }
            selection.append(" or ");
        }
        String[] args = new String[idList.size()];
        idList.toArray(args);

        int deleteCount = mContext.getContentResolver().delete(Message.CONTENT_URI, selection.toString(), args);
        deleteFile(selection.toString(), args);
        return deleteCount;
    }

    /**
     * 根据cursor获取数据库消息数据
     *
     * @param cursor 数据库cursor
     * @return 消息数据
     */
    private ChatMsgBaseInfo getChatMsgByCursor(Cursor cursor) {
        ChatMsgBaseInfo message = new ChatMessageInfo();

        long id = cursor.getLong(cursor.getColumnIndex(Message._ID));
        message.setMsgId(id);

        String address = cursor.getString(cursor.getColumnIndex(Message._SENDER));
        message.setContact(address);

        String receiverStr = cursor.getString(cursor.getColumnIndex(Message._ADDRESS));
        message.setReceiver(MessageUtil.jsontoGson(receiverStr));

        String body = cursor.getString(cursor.getColumnIndex(Message._CONTENT));
        message.setData(body);

        long date = cursor.getLong(cursor.getColumnIndex(Message._SEND_TIME));
        message.setTime(date);

        date = cursor.getLong(cursor.getColumnIndex(Message._PLAN_SEND_TIME));
        message.setPlanSendTime(date);

        int read = cursor.getInt(cursor.getColumnIndex(Message._READ));
        message.setIsRead(read == Message.MSG_READ);

        int burn = cursor.getInt(cursor.getColumnIndex(Message._HIDE));
        message.setIsBurnAfter(burn == Message.HIDE);

        int type = cursor.getInt(cursor.getColumnIndex(Message._CONTENT_TYPE));
        message.setMsgType(type);

        int sendReceive = cursor.getInt(cursor.getColumnIndex(Message._SEND_RECV));
        message.setSendReceive(sendReceive);

        int status = cursor.getInt(cursor.getColumnIndex(Message._STATUS));
        message.setMsgState(status);

        int duration = cursor.getInt(cursor.getColumnIndex(Message._DURATION));
        message.setDuration(duration);

        String fileName = cursor.getString(cursor.getColumnIndex(Message._FILENAME));
        message.setFileName(fileName);

        String fileUrlPath = cursor.getString(cursor.getColumnIndex(Message._ORIGINAL_URL));
        message.setFileUrlPath(fileUrlPath);

        String filePath = cursor.getString(cursor.getColumnIndex(Message._FILE_PATH));
        message.setFilePath(filePath);

        String thumbUrlPath = cursor.getString(cursor.getColumnIndex(Message._THUMBNAIL_URL));
        message.setThumbUrlPath(thumbUrlPath);

        String thumbPath = cursor.getString(cursor.getColumnIndex(Message._THUMBNAIL_PATH));
        message.setThumbPath(thumbPath);

        long fileSize = cursor.getLong(cursor.getColumnIndex(Message._FILELENGTH));
        message.setFileSize(fileSize);

        message.setExtraInfo(cursor.getString(cursor.getColumnIndex(Message._EXTRA_INFO)));

        double latitude = cursor.getDouble(cursor.getColumnIndex(Message._LATITUDE));
        message.setLatitute(latitude);
        double longitude = cursor.getDouble(cursor.getColumnIndex(Message._LONGITUDE));
        message.setLongitude(longitude);
        String locationLabel = cursor.getString(cursor.getColumnIndex(Message._LOCATION_ADDRESS));
        message.setLocationLabel(locationLabel);

        message.setThreadId(cursor.getLong(cursor.getColumnIndex(Message._CONVERSATION_ID)));
        message.setChatType(cursor.getInt(cursor.getColumnIndex(Message._CHAT_TYPE)));

        if (type == MessageType.MSG_TYPE_IMAGE && !TextUtils.isEmpty(body)) {
            try {
                String[] imageSize = body.split(IMAGE_SIZE_SEPARATOR);
                message.setImageWidth(Integer.parseInt(imageSize[0]));
                message.setImageHeight(Integer.parseInt(imageSize[1]));
            } catch (NumberFormatException e) {
                logger.e("getChatMsgByCursor parse image size Exception");
            }
        }

        return message;
    }


    /**
     * 添加消息
     *
     * @param message 消息内容
     */
    public Uri addMessage(ChatMsgBaseInfo message) {

//        if (MessageSendReceive.MSG_RECEIVE == message.getSendReceive()) {
//            // 避免弱网环境下多次收到同一条消息
//            String where = Message._PACKET_ID + " =? AND " + Message._SEND_RECV + " =?";
//            String[] args = new String[]{message.getPacketId(), Integer.toString(MessageSendReceive.MSG_RECEIVE)};
//            Cursor cursor = mContext.getContentResolver().query(Message.CONTENT_URI, new String[]{Message._ID}, where, args, null);
//            if (null != cursor) {
//                if (cursor.getCount() > 0) {
//                    // 已收到发送过来的消息
//                    cursor.close();
//                    return null;
//                }
//                cursor.close();
//            }
//        }

        Uri uri = Message.CONTENT_URI;
        // 插入数据库
        ContentValues values = new ContentValues();
        values.put(Message._SENDER, message.getContact());
        values.put(Message._ADDRESS, MessageUtil.gsontoJson(message.getReceiver()));
        values.put(Message._SEND_TIME, message.getTime());
        values.put(Message._PLAN_SEND_TIME, message.getPlanSendTime());
        values.put(Message._CONVERSATION_ID, message.getMsgThreadId());
        values.put(Message._CHAT_TYPE, message.getChatType());
        values.put(Message._STATUS, message.getMsgState());
        values.put(Message._READ, message.getIsRead() ? Message.MSG_READ : Message.MSG_UNREAD);
        values.put(Message._HIDE, message.isBurnAfterMsg() ? Message.HIDE : Message.NOT_HIDE);
        values.put(Message._CONTENT_TYPE, message.getMsgType());
        values.put(Message._CONTENT, message.getData());
        values.put(Message._SEND_RECV, message.getSendReceive());
        values.put(Message._ORIGINAL_URL, message.getFileUrlPath());
        values.put(Message._THUMBNAIL_URL, message.getThumbUrlPath());
        values.put(Message._FILE_PATH, message.getFilepath());
        values.put(Message._THUMBNAIL_PATH, message.getThumbpath());
        values.put(Message._FILENAME, message.getFilename());
        values.put(Message._FILELENGTH, message.getFilesize());
        values.put(Message._DURATION, message.getDuration());
        values.put(Message._PACKET_ID, message.getPacketId());
        values.put(Message._LATITUDE, message.getLatitude());
        values.put(Message._LONGITUDE, message.getLongitude());
        values.put(Message._LOCATION_ADDRESS, message.getLocationLabel());
        values.put(Message._EXTRA_INFO, message.getExtraInfo());

        return mContext.getContentResolver().insert(uri, values);
    }

    /**
     * 更新消息状态
     *
     * @param msgId    消息ID
     * @param packetID 消息传送ID
     * @param status   消息状态
     */
    public void updateMessageStatus(long msgId, String packetID, int status) {
        if (packetID == null && msgId == -1) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(Message._STATUS, status);
        String where;
        String[] arg;
        if (msgId != -1) {
            // 重发
            values.put(Message._PACKET_ID, packetID);
            where = Message._ID + "=?";
            arg = new String[]{msgId + ""};
        } else {
            where = Message._PACKET_ID + "=?";
            arg = new String[]{packetID};

        }
        mContext.getContentResolver().update(Message.CONTENT_URI, values, where, arg);
    }

    /**
     * 设置所有发送中的消息状态为发送失败
     */
    public void updateAllSendingMsgStatusFail() {
        ContentValues val = new ContentValues();
        // 会话表
        val.put(Conversation._MSG_STATUS, MessageState.MSG_STATE_SEND_FAIL);
        String where = Conversation._MSG_STATUS + "=?";
        String[] arg = new String[]{MessageState.MSG_STATE_SEND_ING + ""};
        mContext.getContentResolver().update(Conversation.CONTENT_URI, val, where, arg);
        // 消息表
        where = Message._STATUS + "=?";
        val.remove(Conversation._MSG_STATUS);
        val.put(Message._STATUS, MessageState.MSG_STATE_SEND_FAIL);
        mContext.getContentResolver().update(Message.CONTENT_URI, val, where, arg);
    }

    /**
     * 更新消息状态
     *
     * @param msgId  消息ID
     * @param status 消息状态
     */
    public void updateConversationStatus(long msgId, int status, long time) {
        if (msgId == -1) {
            return;
        }

        ContentValues val = new ContentValues();
        val.put(Conversation._MSG_STATUS, status);
        if (time > 0) {
            val.put(Conversation._DATE, time);
        }
        String where = Conversation._MSG_ID + "=?";
        String[] arg = new String[]{msgId + ""};
        mContext.getContentResolver().update(Conversation.CONTENT_URI, val, where, arg);
    }

    /**
     * 更新草稿消息
     *
     * @param threadId 消息ID
     * @param message  消息内容
     */
    public void updateConversationDraftMsg(long threadId, String message) {
        String where = Conversation._ID + "=?";
        String[] arg = new String[]{threadId + ""};
        ContentValues conversationValues = new ContentValues();
        conversationValues.put(Conversation._DRAFT_MSG_CONTENT, message);
        if (TextUtils.isEmpty(message)) {
            Cursor cursor = mContext.getContentResolver().query(Conversation.CONTENT_URI,
                    new String[]{Conversation._MSG_ID, Conversation._DRAFT_MSG_CONTENT}, where, arg, null);
            boolean deleteCon = false;
            if (cursor == null) {
                return;
            }
            if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
                cursor.close();
                return;
            }
            if (TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(Conversation._DRAFT_MSG_CONTENT)))) {
                cursor.close();
                return;
            }
            long msgId = cursor.getLong(cursor.getColumnIndex(Conversation._MSG_ID));
            if (msgId > 0) {
                Cursor msgCursor = mContext.getContentResolver().query(Message.CONTENT_URI,
                        new String[]{Message._SEND_TIME, Message._STATUS, Message._HIDE},
                        Message._ID + "=?", new String[]{msgId + ""}, null);
                if (msgCursor != null) {
                    if (msgCursor.moveToFirst()) {
                        int status = msgCursor.getInt(msgCursor.getColumnIndex(Message._STATUS));
                        if (MessageState.MSG_STATE_SEND_DRAFT != status) {
                            // 更新消息时间
                            long msgTime = msgCursor.getLong(msgCursor.getColumnIndex(Message._SEND_TIME));
                            int hide = msgCursor.getInt(msgCursor.getColumnIndex(Message._HIDE));
                            conversationValues.put(Conversation._HIDE, hide);
                            conversationValues.put(Conversation._DATE, msgTime);
                        }
                    }
                    msgCursor.close();
                }
            } else {
                // 空会话
                deleteCon = true;
            }
            if (deleteCon) {
                mContext.getContentResolver().delete(Conversation.CONTENT_URI, where, arg);
            } else {
                mContext.getContentResolver().update(Conversation.CONTENT_URI, conversationValues, where, arg);
            }
            cursor.close();
        } else {
            long time = System.currentTimeMillis();
            conversationValues.put(Conversation._DRAFT_MSG_TIME, time);
            conversationValues.put(Conversation._DATE, time);
            conversationValues.put(Conversation._HIDE, Conversation.NOT_HIDE);
            mContext.getContentResolver().update(Conversation.CONTENT_URI, conversationValues, where, arg);
        }
    }

    /**
     * 更新消息状态
     *
     * @param msgId    消息ID
     * @param packetID 消息传送ID
     * @param status   消息状态
     */
    public void updateMessageFilePath(int msgId, String packetID, String filePath, String thumbnailPath,
                                      int status, long time) {
        if (packetID == null && msgId == -1) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(Message._STATUS, status);
        values.put(Message._THUMBNAIL_URL, thumbnailPath);
        values.put(Message._ORIGINAL_URL, filePath);
        values.put(Message._SEND_TIME, time);
        String where;
        String[] arg;
        if (msgId != -1) {
            // 重发
            values.put(Message._PACKET_ID, packetID);
            where = Message._ID + "=?";
            arg = new String[]{msgId + ""};
        } else {
            where = Message._PACKET_ID + "=?";
            arg = new String[]{packetID};

        }
        mContext.getContentResolver().update(Message.CONTENT_URI, values, where, arg);
    }

    /**
     * 更新消息文件和状态
     *
     * @param msgId    消息ID
     * @param packetID 消息传送ID
     * @param status   消息状态
     */
    public void updateMessageFileLocalPath(long msgId, String packetID, String filePath, int status) {
        if (packetID == null && msgId == -1) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(Message._STATUS, status);
        values.put(Message._FILE_PATH, filePath);
        String where;
        String[] arg;
        if (msgId != -1) {
            // 重发
            values.put(Message._PACKET_ID, packetID);
            where = Message._ID + "=?";
            arg = new String[]{msgId + ""};
        } else {
            where = Message._PACKET_ID + "=?";
            arg = new String[]{packetID};

        }
        mContext.getContentResolver().update(Message.CONTENT_URI, values, where, arg);
    }

    /**
     * 设置消息为已读状态
     *
     * @param msgId 消息ID
     * @return 设置成功个数
     */
    public int setMessageRead(long msgId) {
        String where = Message._ID + " =? ";
        String[] args = new String[]{msgId + ""};
        ContentValues values = new ContentValues();
        values.put(Message._READ, Message.MSG_READ);
        return mContext.getContentResolver().update(Message.CONTENT_URI, values, where, args);
    }

    public int getConversationUnreadCount(long threadId) {
        if (threadId < 0) {
            logger.i("conversationId is error");
            return 0;
        }
        int result = 0;
        ContentResolver resolver = mContext.getContentResolver();
        Uri uri = Conversation.CONTENT_URI;
        String[] projection = new String[]{Conversation._UNREAD_COUNT};
        String selection = Conversation._ID + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(threadId)};
        Cursor c = resolver.query(uri, projection, selection, selectionArgs, null);
        if (c != null) {
            if (c.moveToNext()) {
                result = c.getInt(c.getColumnIndex(Conversation._UNREAD_COUNT));
            }
            c.close();
        }
        return result;
    }

    /**
     * get sum (unread) in all conversations
     *
     * @return
     */
    public int getAllConversationsUnreadCount() {
        int result = 0;
        ContentResolver resolver = mContext.getContentResolver();
        Uri uri = Conversation.CONTENT_URI;
        String[] projection = new String[]{"sum(" + Conversation._UNREAD_COUNT + ")"};
        String selection = Conversation._UNREAD_COUNT + " != ? ";
        String[] selectionArgs = new String[]{"" + MessageConstant.MessageReadState.MSG_READ};
        Cursor c = resolver.query(uri, projection, selection, selectionArgs, null);
        if (c != null) {
            if (c.moveToNext()) {
                result = c.getInt(0);
            }
            c.close();
        }
        return result;
    }

    public Map<String, String> queryMsgSession(String key) {
        Map<String, String> map = new HashMap<String, String>();

        ContentResolver resolver = mContext.getContentResolver();
        Uri uri = Conversation.CONTENT_URI;

        String[] projection = new String[]{Conversation._RECIPIENT_ADDRESS};
        String selection = Conversation._RECIPIENT_ADDRESS + " like ? ";
        String[] selectionArgs = new String[]{"%" + key + "%"};

        Cursor cur = resolver.query(uri, projection, selection, selectionArgs, null);

        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    String recipient_address = cur.getString(cur.getColumnIndex(Conversation._RECIPIENT_ADDRESS));
                    map.put(recipient_address, recipient_address);
                } while (cur.moveToNext());
            }

            cur.close();
        }
        return map;
    }

    /**
     * 获取会话/通知消息列表
     *
     * @param isNotifyMsg
     * @param offset
     * @param count
     * @return
     */
    public List<MessageSessionBaseModel> getMsgSession(boolean isNotifyMsg, int offset, int count) {
        String sortOrder;
        String where;
        String[] args;
        if (isNotifyMsg) {
            // 通知类消息
            sortOrder = Conversation._DATE + " DESC";
            if (count > 0) {
                sortOrder += " limit " + Integer.toString(count);
                sortOrder += " offset " + offset ;
            }
            where = Conversation._DATA1 + " = ? AND " + Conversation._TOP + " = ?";
            args = new String[]{Conversation.NOTIFY_MSG, Integer.toString(Conversation.NOT_TOP)};
        } else {
            sortOrder = Conversation._TOP + " DESC , " + Conversation._DATE + " DESC";
            if (count > 0) {
                sortOrder += " limit " + Integer.toString(count);
                sortOrder += " offset " + offset;
            }
            where = Conversation._DATA1 + " = ? or (" + Conversation._DATA1 + " = ? AND " + Conversation._TOP + " = ?)";
            args = new String[]{Conversation.COMMON_MSG, Conversation.NOTIFY_MSG, Integer.toString(Conversation.TOP)};
        }
        Cursor cur = mContext.getContentResolver().query(Conversation.CONTENT_URI, null, where, args, sortOrder);

        List<MessageSessionBaseModel> list = new ArrayList<MessageSessionBaseModel>();
        if (cur != null) {
            if (cur.getCount() > offset && cur.moveToPosition(offset)) {
                do {
                    list.add(getSessionByCursor(cur));
                } while (cur.moveToNext());
            }
            cur.close();
        }
        return list;
    }

    public List<MessageSessionBaseModel> getFriendsSessionList() {
        String sortOrder;
        String where;
        String[] args;

        sortOrder = Conversation._DATE + " DESC";
        where = Conversation._DATA1 + " = ? AND (" +
                    Conversation._CONVERSATION_STATUS + " = ? OR " +
                    Conversation._CONVERSATION_STATUS + " = ?)";
        args = new String[]{Conversation.COMMON_MSG,
                            String.valueOf(MessageUtil.WalkArroundState.STATE_END),
                            String.valueOf(MessageUtil.WalkArroundState.STATE_END_IMPRESSION)};

        Cursor cur = mContext.getContentResolver().query(Conversation.CONTENT_URI, null, where, args, sortOrder);

        List<MessageSessionBaseModel> list = new ArrayList<MessageSessionBaseModel>();
        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    list.add(getSessionByCursor(cur));
                } while (cur.moveToNext());
            }
            cur.close();
        }

        return list;
    }

    /*
     * While (Current time)  - (Conversation last message time) > time, we will delete it.
     * Default value is 24 hours. (24 * 60 * 60 * 1000 ms)s
     */
    public int delOtherConversionsOverParamTime(long time) {
        if(time <= 0) {
            return -1;
        }

        long curTime = System.currentTimeMillis();
        Cursor cursor = mContext.getContentResolver().query(Conversation.CONTENT_URI,
                new String[]{Conversation._ID, Conversation._DATE, Conversation._CONVERSATION_STATUS},
                "(" + Conversation._CONVERSATION_STATUS + " = ? OR " + Conversation._CONVERSATION_STATUS + " = ? )" +
                        " AND " + Conversation._DATE + " < ?",
                new String[]{String.valueOf(MessageUtil.WalkArroundState.STATE_POP),
                        String.valueOf(MessageUtil.WalkArroundState.STATE_INIT),
                        String.valueOf(curTime - time)},
                null);

        List<Long> threadIdList = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int convState = cursor.getInt(cursor.getColumnIndex(Conversation._CONVERSATION_STATUS));
                    long convDate = cursor.getLong(cursor.getColumnIndex(Conversation._DATE));
                    logger.d(convState + " + " + convDate + " + " + (curTime - time));
                    threadIdList.add(cursor.getLong(cursor.getColumnIndex(Conversation._ID)));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        if(threadIdList.size() > 0) {
            batchDeleteMsg(threadIdList);
        }

        return threadIdList.size();
    }

    public List<MessageSessionBaseModel> getLocalPopImpressionConv() {
        String sortOrder;
        String where;
        String[] args;

        sortOrder = Conversation._DATE + " DESC";
        where = Conversation._CONVERSATION_STATUS + " = ? OR " + Conversation._CONVERSATION_STATUS + " = ?";
        args = new String[]{String.valueOf(MessageUtil.WalkArroundState.STATE_POP_IMPRESSION), String.valueOf(MessageUtil.WalkArroundState.STATE_END_IMPRESSION)};

        Cursor cur = mContext.getContentResolver().query(Conversation.CONTENT_URI, null, where, args, sortOrder);

        List<MessageSessionBaseModel> target = new ArrayList<>();
        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    target.add(getSessionByCursor(cur));
                } while (cur.moveToNext());
            }
            cur.close();
        }

        return target;
    }

    /**
     * 根据cursor获取会话消息内容
     *
     * @return
     */
    private MessageSessionModelInfo getSessionByCursor(Cursor cur) {
        String msg_content = cur.getString(cur.getColumnIndex(Conversation._MSG_CONTENT));
        int top = cur.getInt(cur.getColumnIndex(Conversation._TOP));
        int type = cur.getInt(cur.getColumnIndex(Conversation._TYPE));
        String itemTypeStr = cur.getString(cur.getColumnIndex(Conversation._DATA1));
        String recipient_address = cur.getString(cur.getColumnIndex(Conversation._RECIPIENT_ADDRESS));
        int read = cur.getInt(cur.getColumnIndex(Conversation._READ));
        int unread_count = cur.getInt(cur.getColumnIndex(Conversation._UNREAD_COUNT));
        long date = cur.getLong(cur.getColumnIndex(Conversation._DATE));
        long id = cur.getLong(cur.getColumnIndex(Conversation._ID));
        long msgId = cur.getLong(cur.getColumnIndex(Conversation._MSG_ID));
        int msgStatus = cur.getInt(cur.getColumnIndex(Conversation._MSG_STATUS));
        int hide = cur.getInt(cur.getColumnIndex(Conversation._HIDE));
        int conversationState = cur.getInt(cur.getColumnIndex(Conversation._CONVERSATION_STATUS));
        int colorIndex = cur.getInt(cur.getColumnIndex(Conversation._COLOR));
        int msgType = cur.getInt(cur.getColumnIndex(Conversation._MSG_CONTENT_TYPE));
        String draftMsg = cur.getString(cur.getColumnIndex(Conversation._DRAFT_MSG_CONTENT));
        if (!TextUtils.isEmpty(draftMsg)) {
            msg_content = draftMsg;
            msgType = MessageType.MSG_TYPE_TEXT;
            msgStatus = MessageState.MSG_STATE_SEND_DRAFT;
        }

        MessageSessionModelInfo model = new MessageSessionModelInfo();
        model.setChatType(type);
        int itemType = ConversationType.GENERAL;
        if (Conversation.NOTIFY_MSG.equals(itemTypeStr)) {
            itemType = ConversationType.NOTICES_MSG;
        }
        model.setItemType(itemType);
        model.setContact(recipient_address);
        model.setData(msg_content);
        model.setLastTime(date);
        model.setThreadId(id);
        model.setTop(top);
        model.setContentType(msgType);
        model.setIsBurnAfterMsg(hide == Conversation.HIDE);

        if (read == MessageConstant.MessageReadState.MSG_READ) {
            model.isUnread = false;
        } else {
            model.isUnread = true;
        }
        model.msgId = msgId;
        model.msgStatus = msgStatus;
        model.unReadCount = unread_count;

        model.status = conversationState;
        model.colorIndex = colorIndex;

        return model;
    }

    /**
     * 更新数据库中未有是否通知会话标记的会话
     */
    public void updateConversationMsgNotifyFlag() {
        String where = Conversation._DATA1 + " is NULL";
        Cursor cur = mContext.getContentResolver().query(Conversation.CONTENT_URI, new String[]{Conversation._ID,
                        Conversation._RECIPIENT_ADDRESS}, where, null, null
        );
        if (cur != null) {
            if (cur.moveToFirst()) {
                ContentValues values = new ContentValues();
                where = Conversation._ID + " =?";
                String[] args = new String[1];
                do {
                    String id = cur.getString(cur.getColumnIndex(Conversation._ID));
                    args[0] = id;
                    String number = cur.getString(cur.getColumnIndex(Conversation._RECIPIENT_ADDRESS));
                    values.clear();
                    //String flag = CommonUtil.isNoticeNum(number) ? Conversation.NOTIFY_MSG : Conversation.COMMON_MSG;
                    String flag = Conversation.COMMON_MSG;
                    values.put(Conversation._DATA1, flag);
                    mContext.getContentResolver().update(Conversation.CONTENT_URI, values, where, args);
                } while (cur.moveToNext());
            }
            cur.close();
        }
    }

    public void updateConversationStatus(long threadId, int newState) {

        if (threadId < 0) {
            logger.i("conversationId is error");
            return;
        }

        int oldState = getConversationStatus(threadId);

        if(oldState == newState) {
            return;
        }

        String where = Conversation._ID + "=?";
        String[] arg = new String[]{threadId + ""};
        ContentValues conversationValues = new ContentValues();

        if(newState < oldState
                && oldState >= MessageUtil.WalkArroundState.STATE_IM
                && oldState <= MessageUtil.WalkArroundState.STATE_END) {
            return;
        }

        conversationValues.put(Conversation._CONVERSATION_STATUS, newState);

        //If conversation state == WalkArroundState.STATE_END
        logger.d("updateConversationStatus, old = " + oldState + ", newState = " + newState);
        if(newState >= MessageUtil.WalkArroundState.STATE_END) {
            logger.d("State is END & clear top ");
            conversationValues.put(Conversation._TOP, Conversation.NOT_TOP);
        }

        mContext.getContentResolver().update(Conversation.CONTENT_URI, conversationValues, where, arg);
    }

    public void updateConversationColor(long threadId, int newColor) {

        if (threadId < 0) {
            logger.i("conversationId is error");
            return;
        }

        String where = Conversation._ID + "=?";
        String[] arg = new String[]{threadId + ""};
        ContentValues conversationValues = new ContentValues();
        conversationValues.put(Conversation._COLOR, newColor);

        mContext.getContentResolver().update(Conversation.CONTENT_URI, conversationValues, where, arg);
    }

    public void updateConversationStatusAndColor(long threadId, int newStatus, int newColor) {

        if (threadId < 0) {
            logger.i("conversationId is error");
            return;
        }

        int oldState = getConversationStatus(threadId);

        String where = Conversation._ID + "=?";
        String[] arg = new String[]{threadId + ""};
        ContentValues conversationValues = new ContentValues();
        conversationValues.put(Conversation._COLOR, newColor);

        if(newStatus <= oldState
                && oldState >= MessageUtil.WalkArroundState.STATE_IM
                && oldState <= MessageUtil.WalkArroundState.STATE_END) {
            newStatus = oldState;
        }

        conversationValues.put(Conversation._CONVERSATION_STATUS, newStatus);

        //If conversation state == WalkArroundState.IMPRESSION
        logger.d("updateConversationStatus, old = " + oldState + ", newState = " + newStatus);
        if(newStatus >= MessageUtil.WalkArroundState.STATE_END) {
            logger.d("updateConversationStatus, clear top.");
            conversationValues.put(Conversation._TOP, Conversation.NOT_TOP);
        }

        mContext.getContentResolver().update(Conversation.CONTENT_URI, conversationValues, where, arg);
    }


    public int getConversationStatus(long threadId) {

        if (threadId < 0) {
            logger.i("conversationId is error");
            return 0;
        }
        int result = 0;
        ContentResolver resolver = mContext.getContentResolver();
        Uri uri = Conversation.CONTENT_URI;
        String[] projection = new String[]{Conversation._CONVERSATION_STATUS};
        String selection = Conversation._ID + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(threadId)};
        Cursor c = resolver.query(uri, projection, selection, selectionArgs, null);
        if (c != null) {
            if (c.moveToNext()) {
                result = c.getInt(c.getColumnIndex(Conversation._CONVERSATION_STATUS));
            }
            c.close();
        }

        return result;
    }

    public int getConversationColor(long threadId) {

        if (threadId < 0) {
            logger.i("conversationId is error");
            return -1;
        }
        int result = -1;
        ContentResolver resolver = mContext.getContentResolver();
        Uri uri = Conversation.CONTENT_URI;
        String[] projection = new String[]{Conversation._COLOR};
        String selection = Conversation._ID + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(threadId)};
        Cursor c = resolver.query(uri, projection, selection, selectionArgs, null);
        if (c != null) {
            if (c.moveToNext()) {
                result = c.getInt(c.getColumnIndex(Conversation._COLOR));
            }
            c.close();
        }

        return result;
    }

    /**
     * 最新一条通知消息会话
     *
     * @return
     */
    public MessageSessionBaseModel getLatestNotifySession() {
        String sortOrder = Conversation._DATE + " DESC limit 1";
        String where = Conversation._DATA1 + " = ? AND " + Conversation._TOP + " = ?";
        String[] args = new String[]{Conversation.NOTIFY_MSG, Integer.toString(Conversation.NOT_TOP)};
        Cursor cur = mContext.getContentResolver().query(Conversation.CONTENT_URI, null, where, args, sortOrder);
        MessageSessionModelInfo modelInfo = null;
        if (cur != null) {
            if (cur.moveToFirst()) {
                modelInfo = getSessionByCursor(cur);
            }
            cur.close();
        }
        return modelInfo;
    }

    /**
     * 获取所有通知消息未读个数
     *
     * @return
     */
    public int getAllNotifyMsgUnreadCount() {
        String where = Conversation._DATA1 + " = ? AND " + Conversation._TOP + " = ? AND "
                + Conversation._UNREAD_COUNT + " > 0";
        String[] args = new String[]{Conversation.NOTIFY_MSG, Integer.toString(Conversation.NOT_TOP)};
        Cursor cur = mContext.getContentResolver().query(Conversation.CONTENT_URI, new String[]{Conversation._UNREAD_COUNT},
                where, args, null);
        int count = 0;
        if (cur != null) {
            if (cur.moveToFirst()) {
                do {
                    int unreadCount = cur.getInt(cur.getColumnIndex(Conversation._UNREAD_COUNT));
                    count += unreadCount;
                } while (cur.moveToNext());
            }
            cur.close();
        }
        return count;
    }

    /**
     * @param conversationId
     * @param updateConversation
     * @方法名：deleteAllMessagesInConversation
     * @描述：清空消息
     * @输出：void
     * @作者：caizhibiao
     */
    public void deleteAllMessagesInConversation(int conversationId, boolean updateConversation) {
        if (conversationId < 0)
            return;
        ContentResolver resolver = mContext.getContentResolver();

        // 1. delete file if this message is a multimedia message, not do it
        final String where = Message._CONVERSATION_ID + " = ? ";
        final String[] args = new String[]{String.valueOf(conversationId)};

        // 2. delete all messages
        int deleted = resolver.delete(Message.CONTENT_URI, where, args);

        if (deleted > 0 && updateConversation) {
            ContentValues values = new ContentValues();
            values.put(Conversation._TOTAL_COUNT, 0);
            values.put(Conversation._UNREAD_COUNT, 0);
            values.put(Conversation._MSG_CONTENT, "");
            values.put(Conversation._DRAFT_MSG_CONTENT, "");
            values.put(Conversation._MSG_CONTENT_TYPE, MessageType.MSG_TYPE_TEXT);
            // Fix: user may delete all message when there is a message is
            // sending!
            values.put(Conversation._MSG_STATUS, MessageState.MSG_STATE_RECEIVED);
            resolver.update(ContentUris.withAppendedId(Conversation.CONTENT_URI, conversationId), values, null, null);
        }
    }

    public void doDeleteConversation(int conversationId) {
        logger.d("MS<doDeleteConversation> id: " + conversationId);
        if (conversationId == -1)
            return;
        deleteAllMessagesInConversation(conversationId, false);
        mContext.getContentResolver().delete(ContentUris.withAppendedId(Conversation.CONTENT_URI, conversationId),
                null, null);
    }

    public int deleteConversation(long threadId) {
        String where = Conversation._ID + " = ? ";
        String[] args = new String[]{String.valueOf(threadId)};
        int ret = mContext.getContentResolver().delete(Conversation.CONTENT_URI, where, args);
        return ret;
    }

    public int deleteMappingConversation() {

        Cursor cursor = mContext.getContentResolver().query(Conversation.CONTENT_URI,
                new String[]{Conversation._ID}, Conversation._CONVERSATION_STATUS + " < ? AND " + Conversation._CONVERSATION_STATUS + " >= ?",
                new String[]{String.valueOf(MessageUtil.WalkArroundState.STATE_END), String.valueOf(MessageUtil.WalkArroundState.STATE_IM)}, null);

        List<Long> threadIdList = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    threadIdList.add(cursor.getLong(cursor.getColumnIndex(Conversation._ID)));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        if(threadIdList.size() > 0) {
            batchDeleteMsg(threadIdList);
        }

        return threadIdList.size();
    }

    public void doMarkConversationReaded(int conversationId) {
        logger.v("id: " + conversationId);
        if (conversationId < 0) {
            logger.v("conversationId is error");
            return;
        }
        Cursor cursor = mContext.getContentResolver().query(Conversation.CONTENT_URI,
                new String[]{Conversation._UNREAD_COUNT}, Conversation._ID + " = ? ",
                new String[]{String.valueOf(conversationId)}, null);
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToNext()) {
                if (cursor.getInt(0) > 0) {
                    ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
                }
            }
            cursor.close();
        }

        // all messages belong to this conversation will be marked as read by
        // trigger
        // 未读数量置0，未读标记置已读
        ContentValues values = new ContentValues();
        values.put(Conversation._UNREAD_COUNT, 0);
        values.put(Conversation._READ, MessageConstant.MessageReadState.MSG_READ);
        mContext.getContentResolver().update(Conversation.CONTENT_URI, values, Conversation._ID + " = ? ",
                new String[]{String.valueOf(conversationId)});

    }

    public void markConversationAsTop(long conversationId, boolean top) {
        if (conversationId < 0) {
            return;
        }
        ContentValues values = new ContentValues();
        String where = Conversation._ID + " = " + String.valueOf(conversationId);
        int value = top ? Conversation.TOP : Conversation.NOT_TOP;
        values.put(Conversation._TOP, value);
        mContext.getContentResolver().update(Conversation.CONTENT_URI, values, where, null);
    }

    public boolean isConversationTop(long conversationId) {
        if (conversationId < 0) {
            logger.v("conversationId is error");
            return false;
        }
        int result = 0;
        ContentResolver resolver = mContext.getContentResolver();
        Uri uri = Conversation.CONTENT_URI;
        String[] projection = new String[]{Conversation._TOP};
        String selection = Conversation._ID + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(conversationId)};
        Cursor c = resolver.query(uri, projection, selection, selectionArgs, null);
        if (c != null) {
            if (c.moveToNext()) {
                result = c.getInt(c.getColumnIndex(Conversation._TOP));
            }
            c.close();
        }
        return result == MessageConstant.TopState.TOP;
    }

    /**
     * Find recipient by the address.
     *
     * @param addresses
     * @return
     */
    public int getConversationId(int chatType, List<String> addresses) {
        synchronized (sLock) {
            StringBuilder builder = new StringBuilder();
            Collections.sort(addresses);
            for (String address : addresses) {
                // find recipient id
                builder.append(address);
                builder.append(",");
            }
            builder.deleteCharAt(builder.length() - 1);

            // find conversation id
            int conversationId = -1;
            ContentResolver resolver = mContext.getContentResolver();
            String[] projection = new String[]{Conversation._ID};
            String selection = Conversation._RECIPIENT_ADDRESS + " = ? AND " + Conversation._TYPE + " = ? ";
            String[] args = new String[]{builder.toString(), "" + chatType};
            Cursor c = resolver.query(Conversation.CONTENT_URI, projection, selection, args, null);
            if (c != null) {
                if (c.moveToNext()) {
                    // conversation exists
                    conversationId = c.getInt(c.getColumnIndex(Conversation._ID));
                }
                c.close();
            }

            logger.v("conversationId: " + conversationId);
            return conversationId;
        }
    }

    /**
     * 会话是否存在
     *
     * @param threadId
     * @return
     */
    public boolean isConversationExist(long threadId) {
        Cursor c = mContext.getContentResolver().query(Conversation.CONTENT_URI, new String[]{Conversation._ID},
                Conversation._ID + " = ? ", new String[]{Long.toString(threadId)}, null);
        if (c != null) {
            int count = c.getCount();
            c.close();
            return count > 0;
        }
        return false;
    }

    public int createConversationId(int chatType, List<String> addressList) {
        return createConversationId(chatType, addressList, MessageUtil.WalkArroundState.STATE_INIT);
    }

    public int createConversationId(int chatType, List<String> addressList, int status) {
        synchronized (sLock) {
            List<String> addresses = new ArrayList<String>();
            addresses.addAll(addressList);
            StringBuilder builder = new StringBuilder();
            Collections.sort(addresses);
            for (String address : addresses) {
                // find recipient id
                builder.append(address);
                builder.append(",");
            }
            builder.deleteCharAt(builder.length() - 1);

            // find conversation id
            int conversationId = -1;
            ContentResolver resolver = mContext.getContentResolver();

            // conversation no exits, create it
            ContentValues values = new ContentValues();
            values.put(Conversation._RECIPIENT_ADDRESS, builder.toString());
            values.put(Conversation._TYPE, chatType);
            String notifyFlag = Conversation.COMMON_MSG;
            values.put(Conversation._DATA1, notifyFlag);

            // FIX: Receive INVITE from Group, but no message, the
            // conversation should be at the top by time
            values.put(Conversation._MSG_CONTENT_TYPE, MessageType.MSG_TYPE_TEXT);
            values.put(Conversation._MSG_CONTENT, "");
            values.put(Conversation._MSG_STATUS, -1);
            values.put(Conversation._CONVERSATION_STATUS, status);

            Uri uri = resolver.insert(Conversation.CONTENT_URI, values);
            String idString = uri.getPathSegments().get(1);
            conversationId = Integer.parseInt(idString);

            logger.v("conversationId: " + conversationId);
            return conversationId;
        }
    }

    public List<ChatMessageInfo> searchMsgByKey(String key, List<String> numlist) {
        List<ChatMessageInfo> msgList = new ArrayList<ChatMessageInfo>();

        StringBuilder selection = new StringBuilder("");
        String[] args = null;
        if (numlist == null || numlist.size() == 0) {
            selection.append(Message._SENDER + "||" + Message._CONTENT + " like ?");
            args = new String[]{"%" + key + "%"};
        } else {
            selection.append(Message._CONTENT + " like ? OR (");
            for (int i = 0; i < numlist.size() - 1; i++) {
                selection.append(Message._SENDER + " like ? OR ");
                selection.append(Message._ADDRESS + " like ? OR ");
            }
            selection.append(Message._SENDER + " like ? OR " + Message._ADDRESS + " like ? )");

            args = new String[2 * numlist.size() + 1];
            args[0] = "%" + key + "%";
            for (int i = 0; i < numlist.size(); i++) {
                args[2 * i + 1] = "%" + numlist.get(i) + "%";
                args[2 * i + 2] = "%" + numlist.get(i) + "%";
            }
        }

        String[] projection = new String[]{Message._ID, Message._ADDRESS, Message._HIDE, Message._SENDER, Message._CONTENT, Message._CHAT_TYPE,
                Message._SEND_TIME, Message._READ, Message._CONTENT_TYPE, Message._STATUS, Message._SEND_RECV, Message._CONVERSATION_ID};

        Uri mMsgUri = Message.CONTENT_URI;
        try {
            Cursor cursor = mContext.getContentResolver().query(mMsgUri, projection, selection.toString(), args,
                    Message._SEND_TIME + " desc limit 0," + Integer.MAX_VALUE);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        msgList.add(getExChatMsgByStkMessageCursor(cursor));
                    } while (cursor.moveToNext());
                }

                cursor.close();
            }
        } catch (Exception e) {
            logger.e("searchMsgByKey Exception:" + e.getMessage());
        }
        return msgList;
    }

    private ChatMessageInfo getExChatMsgByStkMessageCursor(Cursor cursor) {
        //
        int id = cursor.getInt(cursor.getColumnIndex(Message._ID));
        String contact = cursor.getString(cursor.getColumnIndex(Message._SENDER));
        String receive = cursor.getString(cursor.getColumnIndex(Message._ADDRESS));
        String data = cursor.getString(cursor.getColumnIndex(Message._CONTENT));
        long time = cursor.getLong(cursor.getColumnIndex(Message._SEND_TIME));
        int isRead = cursor.getInt(cursor.getColumnIndex(Message._READ));
        int burn = cursor.getInt(cursor.getColumnIndex(Message._HIDE));
        int msgType = cursor.getInt(cursor.getColumnIndex(Message._CONTENT_TYPE));
        int msgStatus = cursor.getInt(cursor.getColumnIndex(Message._STATUS));
        int sendReceive = cursor.getInt(cursor.getColumnIndex(Message._SEND_RECV));
        long threadId = cursor.getLong(cursor.getColumnIndex(Message._CONVERSATION_ID));

        ChatMessageInfo message = new ChatMessageInfo();
        message.setMsgId(id);
        message.setData(data);
        message.setChatType(ChatType.CHAT_TYPE_ONE2ONE);
        message.setContact(contact);
        message.setReceiver(MessageUtil.jsontoGson(receive));
        message.setIsRead(isRead == Message.MSG_READ);
        message.setIsBurnAfter(burn == Message.HIDE);
        message.setMsgState(msgStatus);
        message.setMsgType(msgType);
        message.setTime(time);
        message.setSendReceive(sendReceive);
        message.setThreadId(threadId);
        message.setChatType(cursor.getInt(cursor.getColumnIndex(Message._CHAT_TYPE)));

        return message;
    }

    public long getCurrentTime() {
        return System.currentTimeMillis() / 1000L * 1000L;
    }

    public int deleteConversationByThreadId(long threadId) {
        //消息DB
        String selection = Message._CONVERSATION_ID + "=? ";
        String[] args = new String[]{Long.toString(threadId)};
        mContext.getContentResolver().delete(Message.CONTENT_URI, selection, args);

        deleteFile(selection, args);        //会话DB
        selection = Conversation._ID + "=? ";
        return mContext.getContentResolver().delete(Conversation.CONTENT_URI, selection, args);
    }

    public int batchDeleteMsg(List<Long> threadIdList) {
        if (threadIdList == null || threadIdList.isEmpty()) {
            return -1;
        }
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation operation;

        for (long item : threadIdList) {
            // Message DB
            String selection = Message._CONVERSATION_ID + "=? ";
            String[] args = new String[]{Long.toString(item)};
            operation = ContentProviderOperation.newDelete(Message.CONTENT_URI).withSelection(selection, args).build();
            deleteFile(selection, args);
            operations.add(operation);
            // Conversation DB
            selection = Conversation._ID + "=? ";
            operation = ContentProviderOperation.newDelete(Conversation.CONTENT_URI).withSelection(selection, args)
                    .build();
            operations.add(operation);
        }

        int count = 0;
        try {
            ContentProviderResult[] result = mContext.getContentResolver().applyBatch(MessageDatabase.AUTHORITY,
                    operations);
            for (ContentProviderResult itemContentProviderResult : result) {
                count = +itemContentProviderResult.count;
            }
        } catch (RemoteException e) {

        } catch (OperationApplicationException e) {

        }
        return count;
    }

    public int batchMarkConversationAsTop(List<Long> threadIdList, boolean top) {
        if (threadIdList == null || threadIdList.isEmpty()) {
            return 0;
        }
        int count = 0;
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation operation;

        for (long item : threadIdList) {

            ContentValues values = new ContentValues();
            if (top) {
                values.put(Conversation._TOP, TopState.TOP);
            } else {
                values.put(Conversation._TOP, TopState.NOT_TOP);
            }

            String selection = Conversation._ID + "=? ";
            String[] args = new String[]{Long.toString(item)};
            operation = ContentProviderOperation.newUpdate(Conversation.CONTENT_URI).withSelection(selection, args)
                    .withValues(values).build();
            operations.add(operation);
        }
        try {
            ContentProviderResult[] result = mContext.getContentResolver().applyBatch(MessageDatabase.AUTHORITY, operations);
            for (ContentProviderResult itemContentProviderResult : result) {
                count += itemContentProviderResult.count;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * @param threadIdList
     * @方法名：batchDoMarkConversationRead
     * @描述：批量设置为已读
     * @输出：void
     * @作者：shijunfeng
     */
    public int batchDoMarkConversationRead(List<Long> threadIdList) {
        if (threadIdList == null || threadIdList.isEmpty()) {
            return 0;
        }
        int count = 0;
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation operation;
        for (long conversationId : threadIdList) {
            if (conversationId < 0) {
                break;
            }
            // Conversation DB
            ContentValues conversationValues = new ContentValues();
            conversationValues.put(Conversation._UNREAD_COUNT, 0);
            conversationValues.put(Conversation._READ, MessageReadState.MSG_READ);

            operation = ContentProviderOperation.newUpdate(Conversation.CONTENT_URI)
                    .withSelection(Conversation._ID + " = ? ", new String[]{String.valueOf(conversationId)})
                    .withValues(conversationValues).build();
            operations.add(operation);

            Cursor cursor = mContext.getContentResolver().query(Conversation.CONTENT_URI,
                    new String[]{Conversation._UNREAD_COUNT}, Conversation._ID + " = ? ",
                    new String[]{String.valueOf(conversationId)}, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToNext();
                    if (cursor.getInt(0) > 0) {
                        ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
                    }
                }
                cursor.close();
            }

        }

        try {
            ContentProviderResult[] result = mContext.getContentResolver().applyBatch(MessageDatabase.AUTHORITY, operations);
            for (ContentProviderResult item : result) {
                count += item.count;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * @param threadId
     * @方法名：addUnreadCountByThreadId
     * @描述：会话内未读消息的数量
     * @输出：void
     * @作者：sjf
     */
    public void addUnreadCountByThreadId(long threadId) {
        if (threadId < 0) {
            return;
        }

        ContentValues values = new ContentValues();
        String[] projection = new String[]{Conversation._UNREAD_COUNT};
        String where = Conversation._ID + " = " + String.valueOf(threadId);
        Cursor cursor = mContext.getContentResolver().query(Conversation.CONTENT_URI, projection, where, null, null);
        int oldCount = 0;
        if (cursor != null) {
            if (cursor.moveToNext()) {
                oldCount = cursor.getInt(cursor.getColumnIndex(Conversation._UNREAD_COUNT));
                logger.d("<addUnreadCountByThreadId>  oldcount: " + oldCount);
            }
            cursor.close();
        }
        oldCount++;
        values.put(Conversation._UNREAD_COUNT, oldCount);
        values.put(Conversation._READ, MessageReadState.MSG_UNREAD);

        int ret = mContext.getContentResolver().update(Conversation.CONTENT_URI, values, where, null);
        logger.d("<addUnreadCountByThreadId>  oldcount: " + oldCount + " ret: " + ret);
    }

    public MessageSessionBaseModel getLatestSessionById(long threadId) {
        String where = Conversation._ID + " = " + Long.toString(threadId);
        Cursor cur = mContext.getContentResolver().query(Conversation.CONTENT_URI, null, where, null, null);
        if (cur == null) {
            return null;
        }

        MessageSessionBaseModel result = null;
        if (cur.moveToFirst()) {
            result = getSessionByCursor(cur);
            if (TextUtils.isEmpty(result.getData())) {
                List<ChatMsgBaseInfo> chatMessages = getMessageList(threadId, 0, true, 1);
                if (null != chatMessages && 0 != chatMessages.size()) {
                    result.setData(chatMessages.get(0).getData());
                    result.setContentType(chatMessages.get(0).getMsgType());
                    result.setTime(chatMessages.get(0).getTime());
                    result.msgStatus = chatMessages.get(0).getMsgState();
                    result.setIsBurnAfterMsg(chatMessages.get(0).isBurnAfterMsg());
                }
            }
        }
        cur.close();

        return result;
    }

    public MessageSessionBaseModel getLatestSessionByMessageId(long messageId) {
        String where = Conversation._MSG_ID + " = " + Long.toString(messageId);
        Cursor cur = mContext.getContentResolver().query(Conversation.CONTENT_URI, null, where, null, null);
        if (cur == null) {
            return null;
        }
        MessageSessionBaseModel result = null;
        if (cur.moveToFirst()) {
            result = getSessionByCursor(cur);
        }
        cur.close();
        return result;
    }
}
