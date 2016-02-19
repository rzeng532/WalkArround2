/**
 * Copyright (C) 2014-2016 CMCC All rights reserved
 */
package com.example.walkarround.message.handler;

import android.content.Context;
import android.util.Log;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessageHandler;

/**
 * TODO: description
 * Date: 2016-02-14
 * WalkArround
 * @author Richard
 */
public class WrTypedMsgHandler extends AVIMTypedMessageHandler<AVIMTypedMessage> {
    private Context mContext = null;
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
                Log.d("Test", "get message from myself.");
            } else {
                client.close(null);
            }
        } catch (IllegalStateException e) {
            client.close(null);
        }
    }

    //Test API for sending message.
    public void sendMessage() {

    }
}
