/**
 * Copyright (C) 2014-2016 CMCC All rights reserved
 */
package com.example.walkarround.message.handler;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessageHandler;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.model.MessageRecipientInfo;
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.message.util.MessageConstant.MessageState;
import com.example.walkarround.message.util.MessageConstant.MessageType;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: description
 * Date: 2016-02-14
 * WalkArround
 * @author Richard
 */
public class WrTypedMsgHandler extends AVIMTypedMessageHandler<AVIMTypedMessage> {
    private Context mContext = null;
    private static Logger logger = Logger.getLogger(WrTypedMsgHandler.class.getSimpleName());
    private static WrTypedMsgHandler mInstance;

    private WrTypedMsgHandler(Context context) {
        this.mContext = context;
    }

    public static WrTypedMsgHandler getMsgHandlerInstance(Context context) {
        if(mInstance == null) {
            synchronized (WrTypedMsgHandler.class) {
                if(mInstance == null) {
                    mInstance = new WrTypedMsgHandler(context);
                }
            }
        }

        return mInstance;
    }

    @Override
    public void onMessage(AVIMTypedMessage message, AVIMConversation conversation, AVIMClient client) {

        try {
            AVUser avUser = AVUser.getCurrentUser();
            String clientID = (avUser == null ? null : avUser.getObjectId());
            if (clientID != null && client.getClientId().equals(clientID)) {
                logger.d("get message from " + message.getFrom());
                //Start a new thread to receive message.
                Runnable saveMsg = new Runnable() {
                    @Override
                    public void run() {
                        List<String> receipient = new ArrayList<>();
                        receipient.add(message.getFrom());
                        try {
                            MessageRecipientInfo recipientInfo = WalkArroundMsgManager.getInstance(mContext).abstractReceiptInfo(receipient, MessageConstant.ChatType.CHAT_TYPE_ONE2ONE);
                            ChatMsgBaseInfo msgInfo = MessageUtil.convertMsg(message);
                            msgInfo.setThreadId(recipientInfo.getThreadId());

                            //download message file or thumbnail.
                            downloadMsgFile(msgInfo);

                            //we should do those operations on background.
                            Uri msgUri = WalkArroundMsgManager.getInstance(mContext).saveChatmsg(msgInfo);
                            if (msgUri != null) {
                                msgInfo.setMsgId(ContentUris.parseId(msgUri));
                                WalkArroundMsgManager.getInstance(mContext).addMsgUnreadCountByThreadId(msgInfo.getMsgThreadId());
                                WalkArroundMsgManager.getInstance(mContext).onLoadMsgResult(msgInfo, null, true);
                                updateConversationInfor(msgInfo);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                new Thread(saveMsg).start();
            } else {
                client.close(null);
            }
        } catch (IllegalStateException e) {
            client.close(null);
        }
    }

    private void downloadMsgFile(ChatMsgBaseInfo msg) {
        if(!TextUtils.isEmpty(msg.getFileUrlPath())) {
            if(msg.getMsgType() == MessageType.MSG_TYPE_AUDIO
                || msg.getMsgType() == MessageType.MSG_TYPE_MAP) {

                String filePath = msg.getFileUrlPath();

                String localFilePath = MessageUtil.getMsgFileDownLoadPath(msg.getMsgType())
                        + System.currentTimeMillis();
                int dot = filePath.lastIndexOf('.');
                if ((dot > -1) && (dot < (filePath.length() - 1))) {
                    localFilePath += ".";
                    localFilePath += filePath.substring(dot + 1);
                }
                boolean isSuccess = MessageUtil.downloadFile(filePath, localFilePath);
                if (isSuccess) {
                    msg.setFilePath(localFilePath);
                    msg.setMsgState(MessageState.MSG_STATE_RECEIVED);
                }
            }
        }
    }

    /*
     * If msg type is notificaiton and there is extra information.
     * It means another user accept your place and agree to walk arround.
     * So we should update local conversation state and set color.
     */
    private void updateConversationInfor(ChatMsgBaseInfo msgInfo) {
        logger.d("msgInfor msg type: " + msgInfo.getMsgType());
        if(msgInfo.getMsgType() == MessageType.MSG_TYPE_NOTIFICATION) {
            String extra = msgInfo.getExtraInfo();
            logger.d("msgInfor extra: " + extra);
            if(!TextUtils.isEmpty(extra)) {
                String[] extraArray = extra.split(MessageUtil.EXTRA_AGREEMENT_2_WALKARROUND_SPLIT);
                if(extraArray != null && extraArray.length > 1) {
                    logger.d("msgInfor array 1: " + extraArray[1]);
                    int color = Integer.parseInt(extraArray[1]);
                    WalkArroundMsgManager.getInstance(mContext).updateConversationColor(msgInfo.getMsgThreadId(), color);
                    WalkArroundMsgManager.getInstance(mContext).updateConversationStatus(msgInfo.getMsgThreadId(), MessageUtil.WalkArroundState.STATE_WALK);
                }
            }
        }
    }
}
