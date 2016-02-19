/**
 * Copyright (C) 2014-2016 Richard All rights reserved
 */
package com.example.walkarround.message.manager;

import android.util.Log;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;

import java.util.Arrays;
import java.util.List;

/**
 * TODO: description
 * Date: 2016-02-14
 *
 * @author Administrator
 */
public class WalkArroundMsgManager {
    private AVIMClient mAvimClient;
    private AVIMConversation mImConversation;

    private static WalkArroundMsgManager mInstance;

    private WalkArroundMsgManager() {

    }

    public static WalkArroundMsgManager getInstance() {
        if(mInstance == null) {
            synchronized (WalkArroundMsgManager.class){
                if(mInstance == null) {
                    mInstance = new WalkArroundMsgManager();
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

    public void sendTextMsg() {
        if(mInstance == null || mImConversation == null) {
            return;
        }

        if(mInstance.mAvimClient == null) {
            mInstance.mAvimClient = AVIMClient.getInstance(getClientId());
        }

        //Try to send a test message
        AVIMTextMessage msg = new AVIMTextMessage();
        msg.setText("Test");
        // 发送消息
        mImConversation.sendMessage(msg,  new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
                if (e == null) {
                    Log.d("Test", "发送成功！");
                }
            }
        });
    }

    public void getConversation(final String memberId) {
        final AVIMClient client = getMsgClient();
        AVIMConversationQuery conversationQuery = client.getQuery();
        conversationQuery.withMembers(Arrays.asList(memberId), true);
        //conversationQuery.whereEqualTo("customConversationType",1);
        conversationQuery.findInBackground(new AVIMConversationQueryCallback() {
            @Override
            public void done(List<AVIMConversation> list, AVIMException e) {
                if (e == null) {
                    if (null != list && list.size() > 0) {
                        //Set current conversation. We just need the first one if list size > 1.
                        mImConversation = list.get(0);
                    } else {
                        mInstance.mAvimClient.createConversation(Arrays.asList(memberId), null, null, false , new AVIMConversationCreatedCallback() {
                            @Override
                            public void done(AVIMConversation avimConversation, AVIMException e) {
                                if (e == null) {
                                    mImConversation = avimConversation;
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}
