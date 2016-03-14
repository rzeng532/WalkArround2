/**
 * Copyright (C) 2014-2016 CMCC All rights reserved
 */
package com.example.walkarround.message.handler;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessageHandler;
import com.avos.avoscloud.im.v2.messages.*;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.model.MessageRecipientInfo;
import com.example.walkarround.message.util.MessageConstant;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.myself.manager.ProfileManager;
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
                if(message instanceof AVIMAudioMessage) {

                } else if(message instanceof AVIMImageMessage) {

                } else if(message instanceof AVIMLocationMessage) {

                } else if(message instanceof AVIMVideoMessage) {

                } else if(message instanceof AVIMTextMessage) {

                    Runnable saveMsg = new Runnable() {
                        @Override
                        public void run() {
                            List<String> receipient = new ArrayList<>();
                            receipient.add(message.getFrom());
                            try {
                                MessageRecipientInfo recipientInfo = WalkArroundMsgManager.getInstance(mContext).abstractReceiptInfo(receipient, MessageConstant.ChatType.CHAT_TYPE_ONE2ONE);
                                ChatMsgBaseInfo msgInfo = MessageUtil.convertMsg(message);
                                msgInfo.setReceiver(recipientInfo.getRecipientList());
                                msgInfo.setContact(ProfileManager.getInstance().getCurUsrObjId());
                                msgInfo.setThreadId(recipientInfo.getThreadId());

                                //we should do those operations on background.
                                Uri msgUri = WalkArroundMsgManager.getInstance(mContext).saveChatmsg(msgInfo);
                                if (msgUri != null) {
                                    msgInfo.setMsgId(ContentUris.parseId(msgUri));
                                    WalkArroundMsgManager.getInstance(mContext).addMsgUnreadCountByThreadId(msgInfo.getMsgThreadId());
                                    WalkArroundMsgManager.getInstance(mContext).onLoadMsgResult(msgInfo, null, true);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    new Thread(saveMsg).start();

                }
            } else {
                client.close(null);
            }
        } catch (IllegalStateException e) {
            client.close(null);
        }
    }
}
