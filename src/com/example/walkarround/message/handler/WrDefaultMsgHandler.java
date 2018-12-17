/**
 * Copyright (C) 2014-2016 All rights reserved
 */
package com.example.walkarround.message.handler;

import android.util.Log;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageHandler;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;

/**
 * TODO: description
 * Date: 2016-02-18
 *
 * @author Administrator
 */
public class WrDefaultMsgHandler extends AVIMMessageHandler {
    @Override
    public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client){
        Log.d("Test", "onMessage, get message from myself.");
        if(message instanceof AVIMTextMessage){
            Log.d("Test", "onMessage, get text message from myself.");
        }
    }

    @Override
    public void onMessageReceipt(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
        Log.d("Test", "onMessageReceipt, get message from myself.");
        if(message instanceof AVIMTextMessage){
            Log.d("Test", "onMessageReceipt, get text message from myself.");
        }
    }
}
