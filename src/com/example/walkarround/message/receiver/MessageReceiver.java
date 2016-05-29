package com.example.walkarround.message.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;
import com.avos.avoscloud.AVException;
import com.example.walkarround.R;
import com.example.walkarround.main.model.ContactInfo;
import com.example.walkarround.message.activity.BuildMessageActivity;
import com.example.walkarround.message.manager.ContactsManager;
import com.example.walkarround.message.manager.WalkArroundMsgManager;
import com.example.walkarround.message.model.ChatMsgBaseInfo;
import com.example.walkarround.message.util.EmojiParser;
import com.example.walkarround.message.util.MessageUtil;
import com.example.walkarround.message.util.MsgBroadcastConstants;
import com.example.walkarround.util.AsyncTaskListener;
import com.example.walkarround.util.CommonUtils;
import com.example.walkarround.util.Logger;
import com.example.walkarround.util.TimeFormattedUtil;
import com.example.walkarround.util.image.ImageLoaderManager;

/**
 * Listener for message.
 */
public class MessageReceiver extends BroadcastReceiver {

    private static final String DEFAULT_NOTIFICATION_ID = "12345";
    private static final Logger sLogger = Logger.getLogger(MessageReceiver.class.getSimpleName());
    Context mContext;
    private static final int MSG_UPDATE_NOTIFICATION = 0;
    private static final String MSG_UPDATE_NOTIFICATION_MSG_ID = "msg_id";

    private Handler mNotifyHandler = new android.os.Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_NOTIFICATION:
                    Bitmap photo = (Bitmap) msg.obj;
                    Bundle extra = msg.getData();
                    if (extra == null) {
                        return;
                    }
                    Long msgId = extra.getLong(MSG_UPDATE_NOTIFICATION_MSG_ID);
                    doNotification(mContext, msgId, photo);
                    mContext = null;
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
       if (MsgBroadcastConstants.ACTION_MESSAGE_NEW_RECEIVED.equals(action)) {
            // Get new 1v1 chat message.
            long messageId = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_MSG_ID, 0);
            String contact = intent.getStringExtra(MsgBroadcastConstants.BC_VAR_CONTACT); //User object id

            long threadId = intent.getLongExtra(MsgBroadcastConstants.BC_VAR_THREAD_ID,
                    BuildMessageActivity.CONVERSATION_DEFAULT_THREAD_ID);
            onReceivedNewImMsg(context, messageId, contact);
        }
    }

    /**
     * 收到新的IM消息，是否显示通知bar
     *
     * @param context
     * @param messageId
     */
    private void onReceivedNewImMsg(Context context, long messageId, String userId) {
        if (BuildMessageActivity.sCurrentReceiverNum != null
                && BuildMessageActivity.sCurrentReceiverNum.equals(userId)) {
            return;
        }

        //If there is no build message activity on foreground.
        if (MessageUtil.isNewMsgNotifyReceive()) {
            mContext = context;
            notification(context, userId, messageId);
        }
    }

    private void notification(Context context, String userId, long msgId) {
        //Get contact infor from server if local data doesn't contain this user.
        ContactInfo contact = ContactsManager.getInstance(context).getContactByUsrObjId(userId);
        if (contact != null) {
            //Get portrait and display notification.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap srcPhoto = null;
                    if(contact != null) {
                        srcPhoto = ImageLoaderManager.getSyncImage(contact.getPortrait().getUrl());
                    }
                    Bundle data = new Bundle();
                    Message msg = mNotifyHandler.obtainMessage();
                    msg.what = MSG_UPDATE_NOTIFICATION;
                    data.putLong(MSG_UPDATE_NOTIFICATION_MSG_ID, msgId);
                    if (srcPhoto != null) {
                        msg.obj = (Object) srcPhoto;
                    }
                    msg.setData(data);
                    mNotifyHandler.sendMessage(msg);
                }
            }).start();
        } else if(!TextUtils.isEmpty(userId)) {
            //Get contact infor from server.
            ContactsManager.getInstance(context).getContactFromServer(userId, new AsyncTaskListener() {
                @Override
                public void onSuccess(Object data) {
                    //Got contact infor & try to get contact portrait.
                    ContactsManager.getInstance(context).addContactInfo((ContactInfo) data);
                    notification(context, userId, msgId);
                }

                @Override
                public void onFailed(AVException e) {
                    //If we failed to get contact information, we will use default portrait.
                    doNotification(context, msgId, null);
                }
            });
        }
    }

    //Init notification UI.
    private void initNotifyView(Context context, RemoteViews panelView, ChatMsgBaseInfo message, ContactInfo contact, Bitmap srcPhoto) {
        if (contact != null) {
            Bitmap photo = ImageLoaderManager.createCircleImage(srcPhoto);
            if (srcPhoto != null) {
                srcPhoto.recycle();
            }
            if (photo != null) {
                panelView.setViewVisibility(R.id.notify_profile_logogram_tv, View.GONE);
                panelView.setViewVisibility(R.id.notify_profile_iv, View.VISIBLE);
                panelView.setImageViewBitmap(R.id.notify_profile_iv, photo);
            } else {
                panelView.setViewVisibility(R.id.notify_profile_iv, View.VISIBLE);
                panelView.setImageViewResource(R.id.notify_profile_iv,
                        CommonUtils.getPhotoBgResId(contact.getUsername()));
                panelView.setViewVisibility(R.id.notify_profile_logogram_tv, View.VISIBLE);
                panelView.setTextViewText(R.id.notify_profile_logogram_tv,
                        contact.getUsername().substring(contact.getUsername().length() - 1));
            }
            panelView.setTextViewText(R.id.notify_sender_tv, contact.getUsername());
        } else {
            panelView.setTextViewText(R.id.notify_sender_tv, message.getContact());
            panelView.setViewVisibility(R.id.notify_profile_logogram_tv, View.GONE);
            panelView.setViewVisibility(R.id.notify_profile_iv, View.VISIBLE);
            panelView.setImageViewResource(R.id.notify_profile_iv, R.drawable.contact_default_profile);
        }

        //TODO: ADD Emoji parser later.
        //panelView.setTextViewText(R.id.notify_msg_content_tv, EmojiParser.getInstance(context).getSmileyText(message.getData()));
        panelView.setTextViewText(R.id.notify_msg_content_tv, message.getData());
        panelView.setTextViewText(R.id.notify_time_tv,
                TimeFormattedUtil.getDetailDisplayTime(context, message.getTime()));
    }


    //Prepare notification data and display it.
    private void doNotification(Context context, Long messageId, Bitmap srcPhoto) {

        ChatMsgBaseInfo message = WalkArroundMsgManager.getInstance(context).getMessageById(messageId);

        if (message == null) {
            return;
        }

        ContactInfo contact = ContactsManager.getInstance(context).getContactByUsrObjId(message.getContact());

        Intent intent = new Intent();
        intent.setClass(context.getApplicationContext(), BuildMessageActivity.class);
        intent.putExtra(BuildMessageActivity.INTENT_RECEIVER_EDITABLE, false);
        intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_RECEIVER, message.getContact());
        intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_THREAD_ID, message.getMsgThreadId());
        intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_TYPE, message.getChatType());

        //TODO: contact should write to DB & we should crate a content provider for those contacts.
        if (contact != null) {
            intent.putExtra(BuildMessageActivity.INTENT_CONVERSATION_DISPLAY_NAME, contact.getUsername());
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int) message.getMsgThreadId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //If the contact is null, set the user name as empty.
        String displayName = (contact == null ? "" : contact.getUsername());
        message.setData(MessageUtil.getDisplayStr(context, displayName, message));
        Notification notification = new Notification();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.icon = R.drawable.msg_notify_icon;

        //TODO: Add Emoji parser later.
        notification.tickerText = EmojiParser.getInstance(context).getSmileyText(message.getData());
        notification.tickerText = message.getData();

        if (Build.VERSION.SDK_INT == 19) {
            contentIntent.cancel();
            contentIntent = PendingIntent.getActivity(context, (int) message.getMsgThreadId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        notification.contentIntent = contentIntent;
        notification.contentView = new RemoteViews(context.getPackageName(), R.layout.msg_notify_panel);
//        notification.defaults=Notification.DEFAULT_SOUND;
        initNotifyView(context, notification.contentView, message, contact, srcPhoto);
        try {
            String number = (contact == null ? DEFAULT_NOTIFICATION_ID : contact.getMobilePhoneNumber());
            int startPos = number.length() > 5 ? number.length() - 5 : 0;
            int id = Integer.parseInt(number.substring(startPos));
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(number, id, notification);
        } catch (NumberFormatException e) {
            sLogger.e("notification NumberFormatException:" + e.getMessage());
        }
    }
}
